package kr.ac.kaist.hybridroid.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import kr.ac.kaist.hybridroid.callgraph.graphutils.WalaCFGVisualizer;
import kr.ac.kaist.hybridroid.util.data.Pair;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.OrdinalSet;

/**
 * Analyze which fields are defined in which nodes. This analysis result can be
 * used for finding instructions that define the field of an object.
 * 
 * @author LeeSH
 */
public class FieldDefAnalysis {
	//entry basic block number in CFG.
	private final static int ENTRY = -1;
	//exit basic block number in CFG.
	private final static int EXIT = -2;

	//map from a CGNode to field set. 
	private Map<CGNode, Set<PointerKey>> localNodeToFields;
	//map from a field to CGNode set. 
	private Map<PointerKey, Set<CGNode>> localFieldToNodes;
	private Map<PointerKey, Set<Pair<CGNode, SSAPutInstruction>>> localFieldToNodesTest;
	private Map<PointerKey, Set<CGNode>> closureFieldToNodes;
	
	//CallGraph used for finding closure information in this analysis.
	private CallGraph cg;
	//PointerAnalysis used for finding what field is used at an instruction in this analysis.
	private PointerAnalysis<InstanceKey> pa;
	
	public FieldDefAnalysis(CallGraph cg, PointerAnalysis<InstanceKey> pa){
		this.pa = pa;
		this.cg = cg;
//		test(cg);
		localFieldToNodes = new HashMap<PointerKey, Set<CGNode>>();
		localNodeToFields = new HashMap<CGNode, Set<PointerKey>>();
		localFieldToNodesTest = new HashMap<PointerKey, Set<Pair<CGNode, SSAPutInstruction>>>();
		closureFieldToNodes = new HashMap<PointerKey, Set<CGNode>>();
		for(CGNode node : cg){
			Set<PointerKey> fields = getLocalFieldDef(pa, node);
			addFieldToNodesMap(localFieldToNodes, node, fields);
			addNodeToFieldsMap(localNodeToFields, node, fields);
		}
	}
	
	/**
	 * only for testing.
	 * @param cg CallGraph used for this testing.
	 */
	private void test(CallGraph cg){
		int index = 1;
		
		for(CGNode t : cg){
			if(t.toString().contains("Node: synthetic < Primordial, Lcom/ibm/wala/FakeRootClass, fakeRootMethod()V > Context: Everywhere")){
		System.out.println(t);
		IR ir = t.getIR();
		for(SSAInstruction inst : ir.getInstructions()){
			System.out.println("(" + (index++) + ") " + inst);
		}
			}
		}
//		for(CGNode node : cg){
//			IMethod m = node.getMethod();
//			IClass c = node.getMethod().getDeclaringClass();
//			if(m.getName().toString().equals("toString") && c.getName().getClassName().toString().equals("Locale")){
////					m.getName().toString().equals("failedBoundsCheck") && c.getName().getClassName().toString().equals("String") ||
////					m.getName().toString().equals("<init>") && c.getName().getClassName().toString().equals("StringIndexOutOfBoundsException") ||
////					m.getName().toString().equals("toString") && c.getName().getClassName().toString().equals("StringBuilder") ||
////					m.getName().toString().equals("toString") && c.getName().getClassName().toString().equals("AbstractStringBuilder") ||
////					m.getName().toString().equals("arraycopy") && c.getName().getClassName().toString().equals("System")){
//				if(node.getIR()!=null){
//					WalaCFGVisualizer vis = new WalaCFGVisualizer();
//					vis.visualize(node.getIR().getControlFlowGraph(), c.getName().getClassName().toString() + "_" + m.getName().toString() + (index++) + ".dot");
//				}
//			}
//		}
		System.out.println("Test is done.");
		System.exit(-1);
	}
	
	private Set<CGNode> getFieldDefNodesClosure(Set<PointerKey> fields){
		Set<CGNode> nodes = new HashSet<CGNode>();
		
		for(PointerKey field : fields){			
			nodes.addAll(getFieldDefNodesClosure(field));
		}
		return nodes;
	}
	
	private Set<CGNode> getFieldDefNodesClosure(PointerKey field){
		Set<CGNode> nodes = new HashSet<CGNode>();
					
		if(!closureFieldToNodes.containsKey(field)){
			Set<CGNode> fieldNodes = new HashSet<CGNode>();
			Queue<CGNode> queue = new LinkedBlockingQueue<CGNode>();
			queue.addAll(localFieldToNodes.get(field));
			
			while(!queue.isEmpty()){
				CGNode target = queue.poll();
				if(!fieldNodes.contains(target)){
					if(fieldNodes.add(target)){
						Iterator<CGNode> iPred = cg.getPredNodes(target);
						while(iPred.hasNext()){
							CGNode pred = iPred.next();
							queue.add(pred);
						}
					}
				}
			}
			closureFieldToNodes.put(field, fieldNodes);
		}
		nodes.addAll(closureFieldToNodes.get(field));
		
		return nodes;
	}
	
	private Set<CGNode> getFieldDefNodesLocal(Set<PointerKey> fields){
		Set<CGNode> nodes = new HashSet<CGNode>();
		for(PointerKey pk : fields){
			nodes.addAll(localFieldToNodes.get(pk));
		}
		return nodes;
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
						PointerKey pk = pa.getHeapModel().getPointerKeyForStaticField(cha.resolveField(fr));
						if(!localFieldToNodesTest.containsKey(pk))
							localFieldToNodesTest.put(pk, new HashSet<Pair<CGNode, SSAPutInstruction>>());
						localFieldToNodesTest.get(pk).add(Pair.make(node, putInst));
						fields.add(pa.getHeapModel().getPointerKeyForStaticField(cha.resolveField(fr)));
					}else{
						int owner = putInst.getUse(0);
						
						PointerKey ownerPK = pa.getHeapModel().getPointerKeyForLocal(node, owner);
						for(InstanceKey ownerIK : (OrdinalSet<InstanceKey>)pa.getPointsToSet(ownerPK)){
							PointerKey pk = pa.getHeapModel().getPointerKeyForInstanceField(ownerIK, cha.resolveField(fr));
							if(!localFieldToNodesTest.containsKey(pk))
								localFieldToNodesTest.put(pk, new HashSet<Pair<CGNode, SSAPutInstruction>>());
							localFieldToNodesTest.get(pk).add(Pair.make(node, putInst));
							fields.add(pk);
						}
					}
				}
			}
		}
		return fields;
	}

	private boolean isFallThrough(CGNode node, int to, int... targets){
		SSACFG cfg = node.getIR().getControlFlowGraph();
		Queue<Integer> queue = new LinkedBlockingQueue<Integer>();
		int entry = cfg.getNumber(cfg.entry());
		int start = -1;
		if(to == EXIT)
			start = cfg.getNumber(cfg.exit());
		else
			start = cfg.getNumber(cfg.getBlockForInstruction(to));
		
		Set<Integer> passes = new HashSet<Integer>();
		Set<Integer> visited = new HashSet<Integer>();
		for(int target : targets){
			passes.add(cfg.getNumber(cfg.getBlockForInstruction(target)));
		}
		 
		queue.add(start);
		
		WalaCFGVisualizer vis = new WalaCFGVisualizer();
		vis.visualize(cfg, node.getMethod().getName().toString() + ".dot");
//		System.out.println("--> FOUND: " + targets);
		while(!queue.isEmpty()){
			int bb = queue.poll();
			if(!visited.add(bb)){
				continue;
			}
			for(ISSABasicBlock pred : cfg.getNormalPredecessors(cfg.getBasicBlock(bb))){
				int predNum = cfg.getNumber(pred);
				
				if(predNum == entry)
					return false;
				if(!passes.contains(predNum))
					queue.add(predNum);
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
	
	private boolean hasLocalDef(CGNode node, PointerKey field){
		if(localNodeToFields.containsKey(node)){
			return localNodeToFields.get(node).contains(field);
		}
		return false;
	}
	
	private boolean hasClosureDefsAtLeastOne(CGNode node, Set<PointerKey> fields){
		Set<CGNode> nodes = getFieldDefNodesClosure(fields);
		if(nodes.contains(node))
			return true;
		return false;
	}
	
	private boolean hasClosureDefs(CGNode node, PointerKey field){
		Set<CGNode> nodes = getFieldDefNodesClosure(field);
		if(nodes.contains(node))
			return true;
		return false;
	}
	
	private Set<Pair<CGNode, Set<SSAPutInstruction>>> getForwardFieldDefInstructions(CallGraph cg, CGNode node, Set<PointerKey> fields){
		IR ir = node.getIR();
		if(ir == null)
			throw new InternalError("The node does not have any instructions: " + node);
		
		Set<Pair<CGNode, Set<SSAPutInstruction>>> res = new HashSet<Pair<CGNode, Set<SSAPutInstruction>>>();
		
		if(forwardVisited.containsKey(node)){
			if(forwardVisited.get(node).containsAll(fields))
				return res;
			
			Set<PointerKey> alreadyFields = forwardVisited.get(node);
			for(PointerKey field : alreadyFields){
				if(fields.contains(field))
					fields.remove(field);
			}
			alreadyFields.addAll(fields);
		}
		
//		System.out.println("Forward: " +node);
		
		forwardVisited.put(node, setCopy(fields));
//		System.out.println("-Forward: " + node + ", " + fields);
		boolean hasDef = false;
		for(PointerKey field : fields){
			if(hasLocalDef(node, field)){
				hasDef = true;
			}
		}
		Set<SSAPutInstruction> putInsts = new HashSet<SSAPutInstruction>();
		SSAInstruction[] insts = ir.getInstructions();
		Map<PointerKey, Set<Integer>> defIndexes = new HashMap<PointerKey, Set<Integer>>();
		for(int i = insts.length-1; i >= 0 && !fields.isEmpty(); i--){
			SSAInstruction inst = insts[i];
			
			if(inst == null)
				continue;
			
			if(hasDef && inst instanceof SSAPutInstruction){
				SSAPutInstruction putInst = (SSAPutInstruction) inst;
				Set<PointerKey> accFields = getFieldPK(node, putInst);
							
				for(PointerKey field : accFields){
					if(fields.contains(field)){
						putInsts.add(putInst);
						System.out.println("FOUND--("+(foundNum++)+"): " + field);
						System.out.println("\tNode: " + node);
						System.out.println("\tInst: " + putInst);
						if(!defIndexes.containsKey(field))
							defIndexes.put(field, new HashSet<Integer>());
						
						defIndexes.get(field).add(putInst.iindex);
						
						Set<Integer> defSet = defIndexes.get(field);
						int[] defs = new int[defSet.size()];
						int index = 0;
						for(int def : defSet){
							defs[index++] = def;
						}
						
						if(isFallThrough(node, EXIT, defs))
							fields.remove(field);	
					}
				}
			}else if(inst instanceof SSAInvokeInstruction){
				SSAInvokeInstruction invokeInst = (SSAInvokeInstruction) inst;
				CallSiteReference csr = invokeInst.getCallSite();
					
				Set<PointerKey> remainsFieldsInSuccs = new HashSet<PointerKey>();
				
				for(CGNode succ : cg.getPossibleTargets(node, csr)){
					Set<PointerKey> toFindFieldsInSucc = new HashSet<PointerKey>();
					for(PointerKey targetField : fields){
						if(hasClosureDefs(succ, targetField))
							toFindFieldsInSucc.add(targetField);
						else
							remainsFieldsInSuccs.add(targetField);
					}
					
					if(!toFindFieldsInSucc.isEmpty()){
						res.addAll(getForwardFieldDefInstructions(cg, succ, toFindFieldsInSucc));
						remainsFieldsInSuccs.addAll(toFindFieldsInSucc);
					}
				}
				
				for(PointerKey field : setCopy(fields)){
					if(!remainsFieldsInSuccs.contains(field)){
						if(!defIndexes.containsKey(field))
							defIndexes.put(field, new HashSet<Integer>());
						
						defIndexes.get(field).add(invokeInst.iindex);
						
						Set<Integer> defSet = defIndexes.get(field);
						int[] defs = new int[defSet.size()];
						int index = 0;
						for(int def : defSet){
							defs[index++] = def;
						}
						
						if(isFallThrough(node, EXIT, defs))
							fields.remove(field);		
					}
				}
			}
		}
		
		if(!putInsts.isEmpty())
			res.add(Pair.make(node, putInsts));
		return res;
	}
	
	private Map<CGNode, Set<PointerKey>> forwardVisited;
	private int foundNum = 0;
	public Set<Pair<CGNode, Set<SSAPutInstruction>>> getFSFieldDefInstructions(CallGraph cg, CGNode node, SSAGetInstruction inst){
		Set<Pair<CGNode, Set<SSAPutInstruction>>> res = new HashSet<Pair<CGNode, Set<SSAPutInstruction>>>();
		Set<PointerKey> fieldKeys = getFieldPK(node, inst);
		forwardVisited = new HashMap<CGNode, Set<PointerKey>>();
		int defNum = 0;
		foundNum = 0;
//		System.out.println("---- To find ----");
//		for(PointerKey field : fieldKeys){
//			if(localFieldToNodesTest.containsKey(field))
//			for(Pair<CGNode, SSAPutInstruction> p : localFieldToNodesTest.get(field)){
//				defNum++;
//				System.out.println("NODE: " + p.fst());
//				System.out.println("INST: " + p.snd());
//				System.out.println("--");
//			}
//			else
//				System.out.println("Unknown Field: " + field);
//		}
//		System.out.println("-----------------");
//		System.out.println("#Total Def Inst: " + defNum);
		// flow-insensitive! 
		//TODO: make flow sensitive using ICFG!
		boolean allContains = true;
		for(PointerKey field : fieldKeys){
			if(localFieldToNodesTest.containsKey(field)){
				Set<Pair<CGNode, SSAPutInstruction>> pSet = localFieldToNodesTest.get(field);
				Map<CGNode, Set<SSAPutInstruction>> map = new HashMap<CGNode, Set<SSAPutInstruction>>();
				for(Pair<CGNode, SSAPutInstruction> p : pSet){
					CGNode tn = p.fst();
					SSAPutInstruction ti = p.snd();
					if(!map.containsKey(tn))
						map.put(tn, new HashSet<SSAPutInstruction>());
					map.get(tn).add(ti);
				}
				for(CGNode n : map.keySet()){
					res.add(Pair.make(n, map.get(n)));
				}
			} else
				allContains = false;
		}
		if(allContains){
//			System.out.println("all contains!");
			return res;
		}else{
			System.out.println("fail contains!");
			return res;
		}
		//
//		Queue<Pair<Pair<CGNode, Integer>, Set<PointerKey>>> toVisit = new LinkedBlockingQueue<Pair<Pair<CGNode, Integer>, Set<PointerKey>>>();
//		toVisit.add(Pair.make(Pair.make(node, inst.iindex), fieldKeys));
//		
//		System.out.println("\tPK: " + fieldKeys);
//		Map<Integer, Set<PointerKey>> checked = new HashMap<Integer, Set<PointerKey>>();
//		
//		while(!toVisit.isEmpty()){
//			Pair<Pair<CGNode, Integer>, Set<PointerKey>> p = toVisit.poll(); 
//			CGNode target = p.fst().fst();
//			int lastIndex = p.fst().snd();
//			Set<PointerKey> targetFields = p.snd();
//			Set<PointerKey> toFindFields = new HashSet<PointerKey>();
//			
////			System.out.println("FS: " +target);
//			
//			if(checked.containsKey(cg.getNumber(target))){
//				Set<PointerKey> checkedFields =checked.get(cg.getNumber(target));
//				if(checkedFields.containsAll(targetFields))
//					continue;
//				for(PointerKey checkedField : checkedFields)
//					if(targetFields.contains(checkedField))
//						targetFields.remove(checkedField);
//			}else
//				checked.put(cg.getNumber(target), targetFields);
//			
//			Set<PointerKey> remainsFields = setCopy(targetFields);
//			Map<PointerKey, Set<Integer>> defIndexes = new HashMap<PointerKey, Set<Integer>>();
//			
//			boolean hasDef = false;
//			for(PointerKey targetField : targetFields){
//				if(hasClosureDefs(target, targetField)){
//					toFindFields.add(targetField);
//					if(hasLocalDef(target, targetField))
//						hasDef = true;
//				}
//			}
//			
//			remainsFields.removeAll(toFindFields);
//			
//			IR ir = target.getIR();
//			SSAInstruction[] insts = ir.getInstructions();
//			
//			Set<SSAPutInstruction> putInsts = new HashSet<SSAPutInstruction>();
//			for(int i = lastIndex - 1; i >= 0 && !toFindFields.isEmpty(); i--){
//				SSAInstruction preInst = insts[i];
//
//				if(preInst != null){
//					if(hasDef && preInst instanceof SSAPutInstruction){
//						SSAPutInstruction putInst = (SSAPutInstruction) preInst;
//						Set<PointerKey> accFields = getFieldPK(target, putInst);
//						//TODO: Do I have to check 'fallthrough' from a first instruction to the put instruction? 
//						for(PointerKey field : accFields){
//							System.out.println("\t\t=> " + field);
//							if(toFindFields.contains(field)){
//								putInsts.add(putInst);
//								System.out.println("FOUND--("+(foundNum++)+"): " + field);
//								System.out.println("\tNode: " + target);
//								System.out.println("\tInst: " + putInst);
//								if(!defIndexes.containsKey(field))
//									defIndexes.put(field, new HashSet<Integer>());
//								
//								defIndexes.get(field).add(putInst.iindex);
//								
//								Integer[] defArray = defIndexes.get(field).toArray(new Integer[0]);
//								int[] defs = new int[defArray.length];
//								for(int defi=0; defi<defArray.length; defi++){
//									defs[defi] = defArray[defi];
//								}
//								
//								if(isFallThrough(target, lastIndex, defs))
//									toFindFields.remove(field);								
//							}
//						}
//					}else if(preInst instanceof SSAInvokeInstruction){
//						SSAInvokeInstruction invokeInst = (SSAInvokeInstruction) preInst;
//						CallSiteReference csr = invokeInst.getCallSite();
//						Set<PointerKey> remainsFieldsInSuccs = new HashSet<PointerKey>();
//						
//						for(CGNode succ : cg.getPossibleTargets(target, csr)){
//							Set<PointerKey> toFindFieldsInSucc = new HashSet<PointerKey>();
//							for(PointerKey targetField : toFindFields){
//								if(hasClosureDefs(succ, targetField))
//									toFindFieldsInSucc.add(targetField);
//								else
//									remainsFieldsInSuccs.add(targetField);
//							}
//							
//							if(!toFindFieldsInSucc.isEmpty()){
//								res.addAll(getForwardFieldDefInstructions(cg, succ, toFindFieldsInSucc));
//								remainsFieldsInSuccs.addAll(toFindFieldsInSucc);
//							}
//						}
//						
//						for(PointerKey field : setCopy(toFindFields)){
//							if(!remainsFieldsInSuccs.contains(field)){
//								if(!defIndexes.containsKey(field))
//									defIndexes.put(field, new HashSet<Integer>());
//								
//								defIndexes.get(field).add(invokeInst.iindex);
//								
//								Set<Integer> defSet = defIndexes.get(field);
//								int[] defs = new int[defSet.size()];
//								int index = 0;
//								for(int def : defSet){
//									defs[index++] = def;
//								}
//								
//								if(isFallThrough(target, lastIndex, defs))
//									toFindFields.remove(field);		
//							}
//						}
//					}
//				}
//			}
//			
//			remainsFields.addAll(toFindFields);
//			
//			if(!putInsts.isEmpty())
//				res.add(Pair.make(target, putInsts));
//			
//			if(!remainsFields.isEmpty()){
//				for(Pair<CGNode, Integer> pred : getAllPreds(cg, target)){
//					toVisit.add(Pair.make(pred, setCopy(remainsFields)));
//				}
//			}
//		}
//		forwardVisited.clear();
//		forwardVisited = null;
//		System.out.println("\t" + inst + " => " + res.size());
////		System.exit(-1);
//		return res;
	}
	
	private Set<Pair<CGNode, Integer>> getAllPreds(CallGraph cg, CGNode node){
		Set<Pair<CGNode, Integer>> preds = new HashSet<Pair<CGNode, Integer>>();
		Iterator<CGNode> iPred = cg.getPredNodes(node);
		
		while(iPred.hasNext()){
			CGNode pred = iPred.next();
			Iterator<CallSiteReference> iCsr = cg.getPossibleSites(pred, node);
			int maxIindex = -1;
			while(iCsr.hasNext()){
				CallSiteReference csr = iCsr.next();
				IntIterator iIindex = pred.getIR().getCallInstructionIndices(csr).intIterator();
				while(iIindex.hasNext()){
					int iindex = iIindex.next();
					if(maxIindex < iindex)
						maxIindex = iindex;
				}
			}
			preds.add(Pair.make(pred, maxIindex));
		}
		return preds;
	}
	
	private Set<PointerKey> setCopy(Set<PointerKey> set){
		Set<PointerKey> newSet = new HashSet<PointerKey>();
		newSet.addAll(set);
		return newSet;
	}
}
