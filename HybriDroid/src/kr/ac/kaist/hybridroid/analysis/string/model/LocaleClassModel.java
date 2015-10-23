package kr.ac.kaist.hybridroid.analysis.string.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.AssignOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.Box;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.VarBox;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;

public class LocaleClassModel implements ClassModel{
	private static LocaleClassModel instance;
	
	private Map<String, MethodModel> methodMap;
	
	public static LocaleClassModel getInstance(){
		if(instance == null)
			instance = new LocaleClassModel();
		return instance;
	}
	
	private LocaleClassModel(){
		methodMap = new HashMap<String, MethodModel>();
		init();
	}
	
	private void init(){
		methodMap.put("valueOf", new ValueOf());
		methodMap.put("replace", new Replace());
	}
	
	@Override
	public MethodModel getMethod(String methodName){
		if(methodMap.containsKey(methodName))
			return methodMap.get(methodName);
		System.err.println("Unkwon 'String' method: " + methodName);
		return null;
	}
	
	class ValueOf implements MethodModel<Set<Box>>{

		@Override
		public Set<Box> draw(ConstraintGraph graph, Box def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<Box> boxSet = new HashSet<Box>();
			int useVar = invokeInst.getUse(0);
			Box use = new VarBox(caller, invokeInst.iindex, useVar);
			if(graph.addEdge(new AssignOpNode(), def, use))
					boxSet.add(use);
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: String.valueOf";
		}
		
	}
	
	class Replace implements MethodModel<Set<Box>>{

		@Override
		public Set<Box> draw(ConstraintGraph graph, Box def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<Box> boxSet = new HashSet<Box>();
			int strVar = invokeInst.getUse(0);
			int toBeSubstedVar = invokeInst.getUse(1);
			int toSubstVar = invokeInst.getUse(2);
			
			Box strBox = new VarBox(caller, invokeInst.iindex, strVar);
			Box toBeSubstedBox = new VarBox(caller, invokeInst.iindex, toBeSubstedVar);
			Box toSubstBox = new VarBox(caller, invokeInst.iindex, toSubstVar);
			
			if(graph.addEdge(new AssignOpNode(), def, strBox, toBeSubstedBox, toSubstBox)){
				boxSet.add(strBox);
				boxSet.add(toBeSubstedBox);
				boxSet.add(toSubstBox);
			}
					
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: String.replace";
		}
		
	}
}



