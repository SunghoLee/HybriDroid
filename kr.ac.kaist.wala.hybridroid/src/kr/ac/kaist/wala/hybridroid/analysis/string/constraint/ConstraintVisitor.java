/*******************************************************************************
* Copyright (c) 2016 IBM Corporation and KAIST.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* KAIST - initial API and implementation
*******************************************************************************/
package kr.ac.kaist.wala.hybridroid.analysis.string.constraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeBT.IUnaryOpInstruction.IOperator;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAArrayLoadInstruction;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSAConversionInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSAUnaryOpInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;

import kr.ac.kaist.wala.hybridroid.analysis.FieldDefAnalysis;
import kr.ac.kaist.wala.hybridroid.analysis.string.model.IMethodModel;
import kr.ac.kaist.wala.hybridroid.analysis.string.model.StringModel;
import kr.ac.kaist.wala.hybridroid.util.data.Pair;

final public class ConstraintVisitor implements IBoxVisitor<Set<IBox>> {
	private final CallGraph cg;
	private final FieldDefAnalysis fda;
	private final ConstraintGraph graph;
	private final Set<String> warnings;
	private final IClassHierarchy cha;
	private IConstraintMonitor monitor;

	private int iter = 0;
	public ConstraintVisitor(CallGraph cg, FieldDefAnalysis fda, ConstraintGraph graph, IConstraintMonitor monitor){
		this.cg = cg;
		this.fda = fda;
		this.graph = graph;
		this.cha = cg.getClassHierarchy();
		warnings = new HashSet<String>();
		returnInstCache = new HashMap<CGNode, List<SSAReturnInstruction>>();
		callInstCache = new HashMap<Pair<CGNode, CGNode>, List<SSAInvokeInstruction>>();
		
		if(monitor != null)
			this.monitor = monitor;
	}
	
	public Set<String> getWarnings(){
		return warnings;
	}
	
	private void setWarning(String msg, boolean print){
		warnings.add(msg);
//		if(print)
//			System.out.println("[Warning] " + msg);
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
			if(inst instanceof SSAInvokeInstruction){
				CallSiteReference csr = ((SSAInvokeInstruction) inst).getCallSite();
				if(cg.getPossibleTargets(caller, csr).contains(target))
					callList.add((SSAInvokeInstruction)inst);
			}
		}
		
		callInstCache.put(nodePair, callList);
		
		return callList;
	}
	
	private void iter(){
		iter++;
	}
	@Override
	public Set<IBox> visit(VarBox b) {
		// TODO Auto-generated method stub
		iter();
		Set<IBox> res = new HashSet<IBox>();
		
		CGNode node = b.getNode();
		int var = b.getVar();
		
		Object constant = null;
		
		if(graph.isDrawed(b)){
//			System.out.println("[SKIPPED] " + b);
			return res;
		}
		
		if(isArg(node, var)){
			IBox box = new ParamBox(node, var);
			if(graph.addEdge(new AssignOpNode(), b, box))
				res.add(box);
		}else if((constant = getConstant(node, var)) != null){
			IBox box = new ConstBox(node, constant, getConstType(constant));
			if(graph.addEdge(new AssignOpNode(), b, box))
				res.add(box);
		}else{
			DefUse du = node.getDU();
			SSAInstruction defInst = du.getDef(var);
			if(defInst instanceof SSAInvokeInstruction){
				SSAInvokeInstruction invokeInst = (SSAInvokeInstruction)defInst;
				if(cg.getPossibleTargets(node, invokeInst.getCallSite()).size() != 0){
					for (CGNode target : cg.getPossibleTargets(node, invokeInst.getCallSite())) {
						IClass tClass = target.getMethod().getDeclaringClass();
						Selector tMethodSelector = target.getMethod().getSelector(); 
						IMethodModel<Set<IBox>> m = StringModel.getTargetMethod(tClass, tMethodSelector);
						//We use our modeling method rather than original method.
//						System.out.println("\tT: " + target);
						if(m != null){
							res.addAll(m.draw(graph, b, node, invokeInst));
//							System.out.println("M!: " + res.size());
						}else{
							for (SSAReturnInstruction retInst : getReturnInstructions(target)) {
								if (retInst.getNumberOfUses() > 0) {
									VarBox retBox = new VarBox(target, retInst.iindex,
											retInst.getUse(0));
									if (graph.addEdge(new AssignOpNode(), b, retBox))
										res.add(retBox);
								}
							}
						}
					}
				}else{ //If there is no target method, we use our modeling that is targeted to declaring target class and method.
					//If the method is not declared in the class, we find the method in it super class.
					//TODO: Does it make false edge for this invocation?
					Selector tMethodSelector = invokeInst.getDeclaredTarget().getSelector();
					
					IClassHierarchy cha = cg.getClassHierarchy();
					TypeReference tr = invokeInst.getDeclaredTarget().getDeclaringClass();
					IClass klass = cha.lookupClass(tr);
					IMethodModel<Set<IBox>> m = null;
					
//					System.err.println("\tTryTo: " + klass);
//					System.err.println("\tM: " + tMethodSelector);
					while(klass != null){
						IClass tClass = klass;
						m = StringModel.getTargetMethod(tClass, tMethodSelector);
						if(m != null){
							res.addAll(m.draw(graph, b, node, invokeInst));
							break;
						}else
							klass = klass.getSuperclass();
					}
					if(m == null){
//						StringModel.setWarning("the method does not have a body: " + defInst, true);
						ConstBox box = new ConstBox(node, "", ConstType.STRING_BOT);
						if(graph.addEdge(new AssignOpNode(), b, box))
							res.add(box);
					}
				}
				
				if(res.size() == 0){
					ConstBox box = new ConstBox(node, "", ConstType.STRING_BOT);
					if(graph.addEdge(new AssignOpNode(), b, box))
						res.add(box);
				}
			}else if(defInst instanceof SSAUnaryOpInstruction){
				//TODO: Concretize!
				SSAUnaryOpInstruction uopInst = (SSAUnaryOpInstruction) defInst;
				int use = uopInst.getUse(0);
				IOperator op = uopInst.getOpcode();
				IBox opBox = null;
				
				ConstBox box = new ConstBox(node, "", ConstType.STRING_BOT);
				if(graph.addEdge(new AssignOpNode(), b, box))
					res.add(box);
//				System.out.println("Unary: " + defInst + " , " + op.getClass().getName());
			}else if(defInst instanceof SSABinaryOpInstruction){
				//TODO: Concretize!
//				System.out.println("Binary: " + defInst);
				ConstBox box = new ConstBox(node, "", ConstType.STRING_BOT);
				if(graph.addEdge(new AssignOpNode(), b, box))
					res.add(box);
				
			}else if(defInst instanceof SSAGetInstruction){
				SSAGetInstruction getInst = (SSAGetInstruction) defInst;
				Set<Pair<CGNode, Set<SSAPutInstruction>>> defSet = fda.getFSFieldDefInstructions(cg, node, getInst);
				boolean assigned = false;
				for(Pair<CGNode, Set<SSAPutInstruction>> p : defSet){
					CGNode defNode = p.fst();
					for(SSAPutInstruction defPutInst : p.snd()){
						VarBox putBox = new VarBox(defNode, defPutInst.iindex, defPutInst.getUse(1));
						if(graph.addEdge(new AssignOpNode(), b, putBox)){
							res.add(putBox);
							assigned = true;
						}
					}					
				}
				
				//if the field analysis does not contain the target field, just string bot is returned.
				if(!assigned){
					ConstBox box = new ConstBox(node, "", ConstType.STRING_BOT);
					if(graph.addEdge(new AssignOpNode(), b, box))
						res.add(box);
				}
			}else if(defInst instanceof SSANewInstruction){
				SSANewInstruction newInst = (SSANewInstruction) defInst;
				IClass nClass = cha.lookupClass(newInst.getConcreteType());
				if(!StringModel.isClassModeled(nClass)){
//					throw new InternalError("an object of not modeled class is created: " + defInst);
//					System.out.println("an object of not modeled class is created: " + defInst);
//					StringModel.setWarning("an object of not modeled class is created: " + defInst, true);
					ConstBox box = new ConstBox(node, "", ConstType.STRING_TOP);
					if(graph.addEdge(new AssignOpNode(), b, box))
						res.add(box);
					return res;
				}
				int defVar = newInst.getDef();
				SSAInvokeInstruction initCall = getInitInst(node, defVar);
				
				if(initCall != null){
					for(CGNode target : cg.getPossibleTargets(node, initCall.getCallSite())){
						IClass tClass = target.getMethod().getDeclaringClass();
						Selector tMethodSelector = target.getMethod().getSelector();
						IMethodModel<Set<IBox>> m = StringModel.getTargetMethod(tClass, tMethodSelector);
						if(m != null){
							res.addAll(m.draw(graph, b, node, initCall));
						}else{ //if the new class is not modeled, just string bot is returned.
							ConstBox box = new ConstBox(node, "", ConstType.STRING_BOT);
							if(graph.addEdge(new AssignOpNode(), b, box))
								res.add(box);
						}
					}
				}else{ //if the created object does not be know, just string bot is returned.
					ConstBox box = new ConstBox(node, "", ConstType.STRING_BOT);
					if(graph.addEdge(new AssignOpNode(), b, box))
						res.add(box);
				}
			}else if(defInst instanceof SSAPhiInstruction){
				SSAPhiInstruction phiInst = (SSAPhiInstruction) defInst;
				IBox[] boxes = new IBox[phiInst.getNumberOfUses()];
				for(int i=0; i<phiInst.getNumberOfUses(); i++){					
					if(phiInst.getUse(i) < 0){
						setWarning("-1?: " + phiInst, true);
						boxes[i] = new ConstBox(node, "", ConstType.STRING_TOP);
						continue;
					}
					VarBox varBox = new VarBox(node, phiInst.iindex, phiInst.getUse(i));
					boxes[i] = varBox;
				}
				if(graph.addEdge(new JoinOpNode(), b, boxes)){
					for(int i=0; i<boxes.length; i++)
						res.add(boxes[i]);
				}
			}else if(defInst instanceof SSAConversionInstruction){
				int use = defInst.getUse(0);
				VarBox varBox = new VarBox(node, defInst.iindex, use);
				if(graph.addEdge(new AssignOpNode(), b, varBox))
					res.add(varBox);
			}else if(defInst instanceof SSAArrayLoadInstruction){
				SSAArrayLoadInstruction loadInst = (SSAArrayLoadInstruction) defInst;
				ConstBox box = null;
				
//				System.out.println("load: " + loadInst.getElementType().getName().getClassName().toString());
				switch(loadInst.getElementType().getName().getClassName().toString()){
				case "String":
					box = new ConstBox(node, "", ConstType.STRING_TOP);
					break;
				default:
					box = new ConstBox(node, "", ConstType.UNKNOWN);
					break;
				}
				if(graph.addEdge(new AssignOpNode(), b, box))
					res.add(box);
			}else{
//				throw new InternalError("not defined instruction: " + defInst);
//				setWarning("not defined instruction: " + defInst, true);
				ConstBox box = new ConstBox(node, "", ConstType.STRING_BOT);
				if(graph.addEdge(new AssignOpNode(), b, box))
					res.add(box);
			}
		}
		if(monitor != null)
			monitor.monitor(iter, graph, b, res);
		
		if(res.size() == 0){
			Assertions.UNREACHABLE("res must contains one or more boxes: " + node.getDU().getDef(var));
		}
		return res;
	}

	@Override
	public Set<IBox> visit(ParamBox b) {
		// TODO Auto-generated method stub
		iter();
		Set<IBox> res = new HashSet<IBox>();
		CGNode node = b.getNode();
		int index = b.getVar();
		Iterator<CGNode> iPredNode = cg.getPredNodes(node);
		while(iPredNode.hasNext()){
			CGNode predNode = iPredNode.next();
			List<SSAInvokeInstruction> callInsts = getCallInstructions(predNode, node);
			for(SSAInvokeInstruction callInst : callInsts){
				IBox box = new VarBox(predNode, callInst.iindex, callInst.getUse(index - 1));
				if(graph.addEdge(new AssignOpNode(), b, box))
					res.add(box);
			}
		}
		if(monitor != null)
			monitor.monitor(iter, graph, b, res);
		
		return res;
	}

	@Override
	public Set<IBox> visit(ConstBox b) {
		// TODO Auto-generated method stub
		iter();
		if(monitor != null){
			monitor.monitor(iter, graph, b, new HashSet<IBox>());
		}
		
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
			if(symTab.isNullConstant(var))
				return "null";
			else if(symTab.isFalse(var))
				return false;
			else if(symTab.isTrue(var))
				return true;
			else
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
		else if(v instanceof Float)
			return ConstType.DOUBLE;
		else if(v instanceof Long)
			return ConstType.LONG;
		
		setWarning("Unknown type: " + v + "(" + v.getClass().getName() + ")", true);
		return ConstType.UNKNOWN;
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
//		throw new InternalError("Cannot find v" + def + " init call at " + node);
		setWarning("Cannot find v" + def + " init call at " + node, true);
		return null;
		
	}
}
