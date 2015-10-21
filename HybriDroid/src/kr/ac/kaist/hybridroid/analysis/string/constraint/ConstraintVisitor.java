package kr.ac.kaist.hybridroid.analysis.string.constraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.FieldDefAnalysis;
import kr.ac.kaist.hybridroid.analysis.string.model.MethodModel;
import kr.ac.kaist.hybridroid.analysis.string.model.StringModel;
import kr.ac.kaist.hybridroid.util.data.Pair;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSAUnaryOpInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.MethodReference;

public class ConstraintVisitor implements BoxVisitor<Set<Box>> {
	private CallGraph cg;
	private FieldDefAnalysis fda;
	private ConstraintGraph graph;
	public ConstraintVisitor(CallGraph cg, FieldDefAnalysis fda, ConstraintGraph graph){
		this.cg = cg;
		this.fda = fda;
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
		Object constant = null;
		
		if(isArg(node, var)){
			res.add(new ParamBox(node, var));
		}else if((constant = getConstant(node, var)) != null){
			graph.addEdge(new AssignOpNode(), b, new ConstBox(node, constant, getConstType(constant)));
		}else{
			DefUse du = node.getDU();
			SSAInstruction defInst = du.getDef(var);
			if(defInst instanceof SSAInvokeInstruction){
				SSAInvokeInstruction invokeInst = (SSAInvokeInstruction)defInst;
				MethodModel<Set<Box>> m = StringModel.getTargetMethod(invokeInst);
				if(m != null){
					res.addAll(m.draw(graph, b, node, invokeInst));
				}else{
					for(CGNode target : cg.getPossibleTargets(node, invokeInst.getCallSite())){
						for(SSAReturnInstruction retInst : getReturnInstructions(target)){
							if(retInst.getNumberOfUses() > 0){
								VarBox retBox = new VarBox(target, retInst.iindex, retInst.getUse(0));
								graph.addEdge(new AssignOpNode(), b, retBox);
								res.add(retBox);
							}
						}
					}
				}
			}else if(defInst instanceof SSAUnaryOpInstruction){
				System.out.println("Unary: " + defInst);
			}else if(defInst instanceof SSABinaryOpInstruction){
				System.out.println("Binary: " + defInst);
			}else if(defInst instanceof SSAGetInstruction){
				SSAGetInstruction getInst = (SSAGetInstruction) defInst;
				Set<Pair<CGNode, Set<SSAPutInstruction>>> defSet = fda.getFSFieldDefInstructions(cg, node, getInst);
				for(Pair<CGNode, Set<SSAPutInstruction>> p : defSet){
					System.out.println("GetInst: " + p);
				}
				
			}else if(defInst instanceof SSAPutInstruction){
				System.out.println("PutInst: " + defInst);
			}else if(defInst instanceof SSANewInstruction){
				SSANewInstruction newInst = (SSANewInstruction) defInst;
				String className = newInst.getConcreteType().getName().getClassName().toString();
				if(!StringModel.isClassModeled(className))
					throw new InternalError("an object of not modeled class is created: " + defInst);
				int defVar = newInst.getDef();
				SSAInvokeInstruction initCall = getInitInst(node, defVar);
				MethodModel<Set<Box>> m = StringModel.getTargetMethod(initCall);
				res.addAll(m.draw(graph, b, node, initCall));
			}else{
				throw new InternalError("not defined instruction: " + defInst);
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
		
		if((method.getNumberOfParameters()) >= var)
			return true;
//		if(method.isStatic() && method.getNumberOfParameters() >= var)
//			return true;
//		else if((method.getNumberOfParameters() + 1) >= var)
//			return true;
		
		return false;
	}
	
	private Object getConstant(CGNode node, int var){
		IR ir = node.getIR();
		SymbolTable symTab = ir.getSymbolTable();
		if(symTab.isConstant(var)){
			return symTab.getConstantValue(var);
		}
		return null;
	}
	
	private ConstType getConstType(Object v){
		if(v instanceof String)
			return ConstType.STRING;
		else if(v instanceof Boolean)
			return ConstType.BOOL;
		else if(v instanceof Integer)
			return ConstType.INT;
		else if(v instanceof Double)
			return ConstType.DOUBLE;
		
		throw new InternalError("Unknown constant type: " + v);
	}
	
	private SSAInvokeInstruction getInitInst(CGNode node, int def){
		
		DefUse du = node.getDU();
		Iterator<SSAInstruction> iInst = du.getUses(def);
		while(iInst.hasNext()){
			SSAInstruction inst = iInst.next();
			if(inst instanceof SSAInvokeInstruction){
				SSAInvokeInstruction invokeInst = (SSAInvokeInstruction) inst;
				MethodReference mr = invokeInst.getDeclaredTarget(); 
				if(mr.isInit() && invokeInst.getUse(0) == def)
					return invokeInst;
			}
		}
		
		throw new InternalError("Cannot find v" + def + " init call at " + node);
	}
}
