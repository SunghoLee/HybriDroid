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
import kr.ac.kaist.hybridroid.analysis.resource.AndroidResourceAnalysis;
import kr.ac.kaist.hybridroid.analysis.string.model.IMethodModel;
import kr.ac.kaist.hybridroid.analysis.string.model.StringModel;
import kr.ac.kaist.hybridroid.util.data.Pair;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
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
import com.ibm.wala.types.TypeReference;

public class ConstraintVisitor implements IBoxVisitor<Set<IBox>> {
	private CallGraph cg;
	private FieldDefAnalysis fda;
	private ConstraintGraph graph;
	private Set<String> warnings;
	private IConstraintMonitor monitor;
	
	private int iter = 0;
	public ConstraintVisitor(CallGraph cg, FieldDefAnalysis fda, ConstraintGraph graph, IConstraintMonitor monitor){
		this.cg = cg;
		this.fda = fda;
		this.graph = graph;
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
			System.out.println("[SKIPPED] " + b);
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
						IMethodModel<Set<IBox>> m = StringModel.getTargetMethod(target);
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
					String mn = invokeInst.getDeclaredTarget().getName().toString();
					
					IClassHierarchy cha = cg.getClassHierarchy();
					TypeReference tr = invokeInst.getDeclaredTarget().getDeclaringClass();
					IClass klass = cha.lookupClass(tr);
					IMethodModel<Set<IBox>> m = null;
					while(klass != null){
						String cn = klass.getName().getClassName().toString();
						m = StringModel.getTargetMethod(cn, mn);
						if(m != null){
							res.addAll(m.draw(graph, b, node, invokeInst));
							break;
						}else
							klass = klass.getSuperclass();
					}
				}
			}else if(defInst instanceof SSAUnaryOpInstruction){
				System.out.println("Unary: " + defInst);
			}else if(defInst instanceof SSABinaryOpInstruction){
				System.out.println("Binary: " + defInst);
			}else if(defInst instanceof SSAGetInstruction){
				SSAGetInstruction getInst = (SSAGetInstruction) defInst;
//				try {
//					System.in.read();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				Set<Pair<CGNode, Set<SSAPutInstruction>>> defSet = fda.getFSFieldDefInstructions(cg, node, getInst);
				
//				if(getInst.toString().contains("3 = getfield < Primordial, Ljava/util/Locale, cachedToStringResult, <Primordial,Ljava/lang/String> > 1")){
//					for(Pair<CGNode, Set<SSAPutInstruction>> ppp : defSet){
//						CGNode nnn = ppp.fst();
//						System.err.println("##" + nnn);
//						System.err.println("-----");
//						IR rrr = nnn.getIR();
//						int jjj = 1;
//						for(SSAInstruction ijij : rrr.getInstructions()){
//							System.err.println("(" + (jjj++) +") " + ijij);
//						}
//						System.err.println("-----");
//						
//						for(SSAPutInstruction iii : ppp.snd()){
//							System.err.println("\t" + iii);
//						}
//					}
//				}
				for(Pair<CGNode, Set<SSAPutInstruction>> p : defSet){
					CGNode defNode = p.fst();
					for(SSAPutInstruction defPutInst : p.snd()){
						VarBox putBox = new VarBox(defNode, defPutInst.iindex, defPutInst.getUse(1));
						if(graph.addEdge(new AssignOpNode(), b, putBox))
							res.add(putBox);
					}					
				}
			}else if(defInst instanceof SSANewInstruction){
				SSANewInstruction newInst = (SSANewInstruction) defInst;
				String className = newInst.getConcreteType().getName().getClassName().toString();
				if(!StringModel.isClassModeled(className)){
//					throw new InternalError("an object of not modeled class is created: " + defInst);
					System.out.println("an object of not modeled class is created: " + defInst);
					StringModel.setWarning("an object of not modeled class is created: " + defInst, true);
					return res;
				}
				int defVar = newInst.getDef();
				SSAInvokeInstruction initCall = getInitInst(node, defVar);
				if(initCall == null){
					return res;
				}
				
				for(CGNode target : cg.getPossibleTargets(node, initCall.getCallSite())){
					IMethodModel<Set<IBox>> m = StringModel.getTargetMethod(target);
					if(m != null){
						res.addAll(m.draw(graph, b, node, initCall));
					}
				}
			}else if(defInst instanceof SSAPhiInstruction){
				SSAPhiInstruction phiInst = (SSAPhiInstruction) defInst;
				System.out.println("PHI: " + phiInst);
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
			}else{
//				throw new InternalError("not defined instruction: " + defInst);
				setWarning("not defined instruction: " + defInst, true);
			}
		}
//		if(res.contains(null)){
//			System.out.println("NULL?");
//			try {
//				System.in.read();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		if(monitor != null)
			monitor.monitor(iter, graph, b, res);
		
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
		if(monitor != null)
			monitor.monitor(iter, graph, b, Collections.emptySet());
		
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
		
		setWarning("Unknown type: " + v, true);
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
