package kr.ac.kaist.hybridroid.preanalysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dalvik.classLoader.DexIRFactory;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.debug.Assertions;

/**
 * 
 * @author Sungho Lee
 */
public class StringAnalyzer {
	private IClassHierarchy cha;
	private TargetArgPool targetPool;
	private DexIRFactory irFactory;
	private Map<IMethod, IR> irCache;
	
	public StringAnalyzer(IClassHierarchy cha){
		this.cha = cha;
		this.irFactory = new DexIRFactory();
		irCache = new HashMap<IMethod, IR>();
		targetPool = new TargetArgPool();
	}
	
	public boolean addTarget(MethodReference mr, int... argPos){
		return targetPool.addTarget(mr, argPos);
	}
	
	public StringAnalysisResult analyze(){
		StringAnalysisResult res = new StringAnalysisResult();
		for(Pair<IMethod, SSAAbstractInvokeInstruction> point : lookForInvocationPoints()){
			IMethod m = point.fst;
			SSAAbstractInvokeInstruction callInst = point.snd;
			IR ir = getIR(m);
			if(ir != null){
				SymbolTable symTab = ir.getSymbolTable();
				Set<Integer> targetArgPos = targetPool.getTargetArgPos(m.getReference());
				if(targetArgPos != null){
					for(int argPos : targetArgPos){
						int arg = callInst.getUse(argPos);
						if(symTab.isStringConstant(arg)){
							String v = (String)symTab.getConstantValue(arg);
							res.setResult(m.getReference(), callInst.iindex, argPos, v);
						}
					}
				}
			}
		}
		return res;
	}
	
	private Set<Pair<IMethod, SSAAbstractInvokeInstruction>> lookForInvocationPoints(){
		Set<Pair<IMethod, SSAAbstractInvokeInstruction>> points = new HashSet<Pair<IMethod, SSAAbstractInvokeInstruction>>();
		
		for(IClass c : cha){
			for(IMethod m : c.getAllMethods()){
				IR ir = getIR(m);
				if(ir != null){
					Iterator<CallSiteReference> callSiteIter = ir.iterateCallSites();
					while(callSiteIter.hasNext()){
						CallSiteReference callSiteRef = callSiteIter.next();
						SSAAbstractInvokeInstruction[] callInsts = ir.getCalls(callSiteRef);
						for(SSAAbstractInvokeInstruction callInst : callInsts){
							MethodReference calleeRef = callInst.getDeclaredTarget();
							if(isTargetCall(calleeRef)){
								points.add(Pair.make(m, callInst));
							}
						}
					}
				}
			}
		}
		return points;
	}
	
	private boolean isParam(IMethod m, int var){
		if(var <= m.getNumberOfParameters())
			return true;
		return false;
	}
	
	private SSAInstruction getLocalDefInstruction(IMethod m, int index, int var){
		IR ir = getIR(m);
		if(ir == null)
			Assertions.UNREACHABLE("IR cannot be null");
		
		if(isParam(m, var))
			return null;
		
		SSAInstruction[] insts = ir.getInstructions();
		
//		for(int i = index; i >= 0; )
		return null;
	}
	
	private void addIntraConstraints(IMethod m){
		
	}
	
	/**
	 * Get IR for the method. If caching IR exists, just returns it. 
	 * @param m the target method for the IR.
	 * @return IR for the target method.
	 */
	private IR getIR(IMethod m){
		IR ir = null;
		
		if(irCache.containsKey(m))
			ir = irCache.get(m);
		else{
			ir = irFactory.makeIR(m, Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
			irCache.put(m, ir);
		}
		return ir;
	}
	
	private boolean isTargetCall(MethodReference callee){
		return targetPool.isTargetMethod(callee);
	}
	
	class TargetArgPool{
		private Map<MethodReference, Set<Integer>> argPool;
		
		public TargetArgPool(){
			argPool = new HashMap<MethodReference, Set<Integer>>();
		}
		
		public boolean addTarget(MethodReference mr, int... argPos){
			if(argPool.containsKey(mr))
				return false;
			
			Set<Integer> argPosSet = new HashSet<Integer>();
			for(int arg : argPos)
				argPosSet.add(arg);
			argPool.put(mr, argPosSet);
			
			return true;
		}
		
		public boolean isTargetMethod(MethodReference mr){
			return argPool.containsKey(mr);
		}
		
		public Set<Integer> getTargetArgPos(MethodReference mr){
			return argPool.get(mr);
		}
	}
}
