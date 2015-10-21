package kr.ac.kaist.hybridroid.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import kr.ac.kaist.hybridroid.util.data.Pair;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAGotoInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.util.intset.OrdinalSet;

public class FieldDefAnalysis {
	private Map<CGNode, Set<PointerKey>> localNodeToFields;
	private Map<PointerKey, Set<CGNode>> localFieldToNodes;
	private Map<CGNode, Set<PointerKey>> closureNodeToFields;
	private Map<PointerKey, Set<CGNode>> closureFieldToNodes;
	
	private PointerAnalysis pa;
	public FieldDefAnalysis(CallGraph cg, PointerAnalysis pa){
		this.pa = pa;
		
		localNodeToFields = new HashMap<CGNode, Set<PointerKey>>();
		localFieldToNodes = new HashMap<PointerKey, Set<CGNode>>();
		closureNodeToFields = new HashMap<CGNode, Set<PointerKey>>();
		closureFieldToNodes = new HashMap<PointerKey, Set<CGNode>>();

		for(CGNode node : cg){
			Set<PointerKey> fields = getLocalFieldDef(pa, node);
			addNodeToFieldsMap(localNodeToFields, node, fields);
			addFieldToNodesMap(localFieldToNodes, node, fields);
		}
		
		calcClosure(cg);
	}
	
	private void calcClosure(CallGraph cg){
		fCache = new HashMap<CGNode, Set<PointerKey>>();
		for(CGNode node : cg){
			if(fCache.containsKey(node)){
				addNodeToFieldsMap(closureNodeToFields, node, fCache.get(node));
				addFieldToNodesMap(closureFieldToNodes, node, fCache.get(node));
				continue;
			}
			Set<PointerKey> fields = localNodeToFields.get(node);
			Set<Integer> visited = new HashSet<Integer>();
			fields.addAll(getFieldDefBySuccessors(cg, node, visited));
			addNodeToFieldsMap(closureNodeToFields, node, fields);
			addFieldToNodesMap(closureFieldToNodes, node, fields);
		}
	}
	
	private Map<CGNode, Set<PointerKey>> fCache;
	private Set<PointerKey> getFieldDefBySuccessors(CallGraph cg, CGNode node, Set<Integer> visited){
		Set<PointerKey> fields = new HashSet<PointerKey>();
		int nodeNum = cg.getNumber(node);
		if(!visited.contains(nodeNum)){
			visited.add(nodeNum);
			Iterator<CGNode> iSucc = cg.getSuccNodes(node);
			while(iSucc.hasNext()){
				CGNode succ = iSucc.next();
				if(fCache.containsKey(succ)){
					fields.addAll(fCache.get(succ));
				}else{
					fields.addAll(localNodeToFields.get(succ));
					fields.addAll(getFieldDefBySuccessors(cg, succ, visited));
					fCache.put(succ, fields);
				}
			}
			visited.remove(nodeNum);
		}
		return fields;
	}
	
	private void addNodeToFieldsMap(Map<CGNode, Set<PointerKey>> map, CGNode node, Set<PointerKey> fields){
		if(!map.containsKey(node))
			map.put(node, new HashSet<PointerKey>());
		
		map.get(node).addAll(fields);
	}
	
	private void addFieldToNodesMap(Map<PointerKey, Set<CGNode>> map, CGNode node, Set<PointerKey> fields){
		for(PointerKey field : fields){
			if(!map.containsKey(field))
				map.put(field, new HashSet<CGNode>());
			map.get(field).add(node);
		}
	}
	
	private Set<PointerKey> getLocalFieldDef(PointerAnalysis pa, CGNode node){
		IClassHierarchy cha = pa.getClassHierarchy();
		
		Set<PointerKey> fields = new HashSet<PointerKey>();
		
		IR ir = node.getIR();
		
		if(ir != null){
			for(SSAInstruction inst : ir.getInstructions()){
				if(inst instanceof SSAPutInstruction){
					SSAPutInstruction putInst = (SSAPutInstruction) inst;
					FieldReference fr = putInst.getDeclaredField();
					
					if(putInst.isStatic()){
						fields.add(pa.getHeapModel().getPointerKeyForStaticField(cha.resolveField(fr)));
					}else{
						int owner = putInst.getUse(0);
						
						PointerKey ownerPK = pa.getHeapModel().getPointerKeyForLocal(node, owner);
						for(InstanceKey ownerIK : (OrdinalSet<InstanceKey>)pa.getPointsToSet(ownerPK)){
							fields.add(pa.getHeapModel().getPointerKeyForInstanceField(ownerIK, cha.resolveField(fr)));
						}
					}
				}
			}
		}
		return fields;
	}
	
	public Set<CGNode> getFieldDefNodes(CGNode node, SSAGetInstruction inst){
		Set<CGNode> nodes = new HashSet<CGNode>();
		IClassHierarchy cha = pa.getClassHierarchy();
		FieldReference fr = inst.getDeclaredField();
		
		if(inst.isStatic()){
			nodes.addAll(closureFieldToNodes.get(pa.getHeapModel().getPointerKeyForStaticField(cha.resolveField(fr))));
		}else{
			int owner = inst.getUse(0);
			
			PointerKey ownerPK = pa.getHeapModel().getPointerKeyForLocal(node, owner);
			for(InstanceKey ownerIK : (OrdinalSet<InstanceKey>)pa.getPointsToSet(ownerPK)){
				nodes.addAll(closureFieldToNodes.get(pa.getHeapModel().getPointerKeyForInstanceField(ownerIK, cha.resolveField(fr))));
			} 
		}
		return nodes;
	}

	private boolean isFallThrough(SSAInstruction[] insts, int from, int to){
		for(int i=from; i<to; i++){
			SSAInstruction inst = insts[i];
			if(inst != null){
				if(inst instanceof SSAConditionalBranchInstruction){
					SSAConditionalBranchInstruction condInst = (SSAConditionalBranchInstruction) inst;
					int jmpTarget = condInst.getTarget();
					if(jmpTarget > to)
						return false;
				}else if(inst instanceof SSAGotoInstruction){
					SSAGotoInstruction gotoInst = (SSAGotoInstruction) inst;
					int jmpTarget = gotoInst.getTarget();
					if(jmpTarget > to)
						return false;
				}
			}
		}
		return true;
	}
	
	private Set<PointerKey> getFieldPK(CGNode node, SSAFieldAccessInstruction inst){
		Set<PointerKey> fieldKeys = new HashSet<PointerKey>();
		IClassHierarchy cha = pa.getClassHierarchy();
		FieldReference fr = inst.getDeclaredField();
		
		if(inst.isStatic()){
			PointerKey fieldPK = pa.getHeapModel().getPointerKeyForStaticField(cha.resolveField(fr));
			fieldKeys.add(fieldPK);
		}else{
			int owner = inst.getUse(0);
			
			PointerKey ownerPK = pa.getHeapModel().getPointerKeyForLocal(node, owner);
			for(InstanceKey ownerIK : (OrdinalSet<InstanceKey>)pa.getPointsToSet(ownerPK)){
				PointerKey fieldPK = pa.getHeapModel().getPointerKeyForInstanceField(ownerIK, cha.resolveField(fr));
				fieldKeys.add(fieldPK);
			} 
		}
		
		return fieldKeys;
	}
	
	public Set<Pair<CGNode, Set<SSAPutInstruction>>> getFSFieldDefInstructions(CallGraph cg, CGNode node, SSAGetInstruction inst){
		Set<Pair<CGNode, Set<SSAPutInstruction>>> res = new HashSet<Pair<CGNode, Set<SSAPutInstruction>>>();
		Set<CGNode> nodes = new HashSet<CGNode>();
		Set<PointerKey> fieldKeys = getFieldPK(node, inst);
		
		for(PointerKey field : fieldKeys)
			nodes.addAll(closureFieldToNodes.get(field));
		
		Queue<Pair<CGNode, Set<PointerKey>>> toVisit = new LinkedBlockingQueue<Pair<CGNode, Set<PointerKey>>>();
		toVisit.add(Pair.make(node, fieldKeys));
		
		boolean thisNode = true;
		System.out.println("\tPK: " + fieldKeys);
		Set<Integer> checked = new HashSet<Integer>();
		
		while(!toVisit.isEmpty()){
			Pair<CGNode, Set<PointerKey>> p = toVisit.poll(); 
			CGNode target = p.fst();
			Set<PointerKey> targetFields = p.snd();
			if(checked.contains(cg.getNumber(target)))
				continue;
			if(!nodes.contains(target)){
				for(CGNode pred : getAllPreds(cg, target)){
					toVisit.add(Pair.make(pred, setCopy(targetFields)));
				}
				continue;
			}
			IR ir = target.getIR();
			SSAInstruction[] insts = ir.getInstructions();
			int lastIndex = insts.length-1;
			if(thisNode){
				lastIndex = inst.iindex;
				thisNode = false;
			}
			Set<SSAPutInstruction> putInsts = new HashSet<SSAPutInstruction>();
			for(int i = lastIndex - 1; i >= 0; i--){
				SSAInstruction preInst = insts[i];
				if(preInst != null){
					if(preInst instanceof SSAPutInstruction){
						SSAPutInstruction putInst = (SSAPutInstruction) preInst;
						Set<PointerKey> accFields = getFieldPK(node, putInst);
						boolean fallThrough = isFallThrough(insts, putInst.iindex, inst.iindex);
						//TODO: Do I have to check 'fallthrough' from a first instruction to the put instruction? 
						for(PointerKey field : accFields){
							System.out.println("\t\t=> " + field);
							if(targetFields.contains(field))
								putInsts.add(putInst);
							if(fallThrough)
								targetFields.remove(field);								
						}
					}else if(preInst instanceof SSAInvokeInstruction){
						
					}
				}
			}
			
			if(!putInsts.isEmpty())
				res.add(Pair.make(target, putInsts));
			
			if(!targetFields.isEmpty()){
				for(CGNode pred : getAllPreds(cg, target)){
					toVisit.add(Pair.make(pred, setCopy(targetFields)));
				}
			}
		}
		System.out.println("\t" + inst + " => " + res.size());
		return res;
	}
	
	private Set<CGNode> getAllPreds(CallGraph cg, CGNode node){
		Set<CGNode> preds = new HashSet<CGNode>();
		Iterator<CGNode> iPred = cg.getPredNodes(node);
		
		while(iPred.hasNext()){
			preds.add(iPred.next());
		}
		return preds;
	}
	
	private Set<PointerKey> setCopy(Set<PointerKey> set){
		Set<PointerKey> newSet = new HashSet<PointerKey>();
		newSet.addAll(set);
		return newSet;
	}
}
