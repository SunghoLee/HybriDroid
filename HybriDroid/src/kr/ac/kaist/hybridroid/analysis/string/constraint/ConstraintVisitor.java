package kr.ac.kaist.hybridroid.analysis.string.constraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSAUnaryOpInstruction;
import com.ibm.wala.util.collections.Pair;

public class ConstraintVisitor implements BoxVisitor<Set<Box>> {
	private CallGraph cg;
	private ConstraintGraph graph;
	public ConstraintVisitor(CallGraph cg, ConstraintGraph graph){
		this.cg = cg;
		this.graph = graph;
		returnInstCache = new HashMap<CGNode, List<SSAReturnInstruction>>();
		callInstCache = new HashMap<Pair<CGNode, CGNode>, List<SSAInvokeInstruction>>();
	}
	
	private Map<CGNode, List<SSAReturnInstruction>> returnInstCache;
	private Map<Pair<CGNode, CGNode>, List<SSAInvokeInstruction>> callInstCache;
	
	private List<SSAReturnInstruction> getReturnInstructions(CGNode node){
		if(returnInstCache.containsKey(node))
			return returnInstCache.get(node);
		
		List<SSAReturnInstruction> returnList = new ArrayList<SSAReturnInstruction>();
		IR ir = node.getIR();
		
		if(ir == null)
			return returnList;
		
		SSAInstruction[] insts = ir.getInstructions();
		for(SSAInstruction inst : insts){
			if(inst == null)
				continue;
			if(inst instanceof SSAReturnInstruction)
				returnList.add((SSAReturnInstruction)inst);
		}
		
		returnInstCache.put(node, returnList);
		
		return returnList;
	}
	
	private List<SSAInvokeInstruction> getCallInstructions(CGNode caller, CGNode target){
		Pair<CGNode, CGNode> nodePair = Pair.make(caller, target);
		if(callInstCache.containsKey(nodePair))
			return callInstCache.get(nodePair);
		
		List<SSAInvokeInstruction> callList = new ArrayList<SSAInvokeInstruction>();
		IR ir = caller.getIR();
		
		if(ir == null)
			return callList;
		
		SSAInstruction[] insts = ir.getInstructions();
		for(SSAInstruction inst : insts){
			if(inst == null)
				continue;
			if(inst instanceof SSAInvokeInstruction)
				callList.add((SSAInvokeInstruction)inst);
		}
		
		callInstCache.put(nodePair, callList);
		
		return callList;
	}
	
	@Override
	public Set<Box> visit(VarBox b) {
		// TODO Auto-generated method stub
		CGNode node = b.getNode();
		int var = b.getVar();
		Set<Box> res = new HashSet<Box>();
		if(isArg(node, var)){
			res.add(new ParamBox(node, var));
		}else{
			DefUse du = node.getDU();
			SSAInstruction defInst = du.getDef(var);
			if(defInst instanceof SSAInvokeInstruction){
				SSAInvokeInstruction invokeInst = (SSAInvokeInstruction)defInst;
				for(CGNode target : cg.getPossibleTargets(node, invokeInst.getCallSite())){
					for(SSAReturnInstruction retInst : getReturnInstructions(target)){
						if(retInst.getNumberOfUses() > 0)
							res.add(new VarBox(target, retInst.iindex, retInst.getUse(0)));
					}
				}
			}else if(defInst instanceof SSAUnaryOpInstruction){
				System.out.println("Unary: " + defInst);
			}else if(defInst instanceof SSABinaryOpInstruction){
				System.out.println("Binary: " + defInst);
			}else if(defInst instanceof SSAGetInstruction){
				System.out.println("GetInst: " + defInst);
			}else if(defInst instanceof SSAPutInstruction){
				System.out.println("PutInst: " + defInst);
			}else{
				throw new InternalError("not defined instruction: " + defInst.getClass().getName());
			}
		}
		return res;
	}

	@Override
	public Set<Box> visit(ParamBox b) {
		// TODO Auto-generated method stub
		Set<Box> res = new HashSet<Box>();
		CGNode node = b.getNode();
		int index = b.getVar();
		Iterator<CGNode> iPredNode = cg.getPredNodes(node);
		while(iPredNode.hasNext()){
			CGNode predNode = iPredNode.next();
			List<SSAInvokeInstruction> callInsts = getCallInstructions(predNode, node);
			for(SSAInvokeInstruction callInst : callInsts){
				res.add(new VarBox(predNode, callInst.iindex, callInst.getUse(index - 1)));
			}
		}
		return res;
	}

	@Override
	public Set<Box> visit(ConstBox b) {
		// TODO Auto-generated method stub
		return Collections.emptySet();
	}
	
	private boolean isArg(CGNode node, int var){
		IMethod method = node.getMethod();
		
		if(method.isStatic() && method.getNumberOfParameters() >= var)
			return true;
		else if((method.getNumberOfParameters() + 1) >= var)
			return true;
		
		return false;
	}
}
