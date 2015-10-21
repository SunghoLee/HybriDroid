package kr.ac.kaist.hybridroid.analysis.string.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.AppendOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.AssignOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.Box;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.VarBox;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;

public class StringBuilderClassModel implements ClassModel{
	private static StringBuilderClassModel instance;
	
	private Map<String, MethodModel> methodMap;
	
	public static StringBuilderClassModel getInstance(){
		if(instance == null)
			instance = new StringBuilderClassModel();
		return instance;
	}
	
	private StringBuilderClassModel(){
		methodMap = new HashMap<String, MethodModel>();
		init();
	}
	
	private void init(){
		methodMap.put("<init>", new Init());
		methodMap.put("toString", new ToString());
		methodMap.put("append", new Append());
	}
	
	@Override
	public MethodModel getMethod(String methodName){
		if(methodMap.containsKey(methodName))
			return methodMap.get(methodName);
		System.err.println("Unkwon 'StringBuilder' method: " + methodName);
		return null;
	}
	
	class ToString implements MethodModel<Set<Box>>{
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
			return "Constraint Graph Method Model: StringBuilder.toString";
		}
	}
	
	class Append implements MethodModel<Set<Box>>{
		@Override
		public Set<Box> draw(ConstraintGraph graph, Box def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			// TODO Auto-generated method stub
			Set<Box> boxSet = new HashSet<Box>();
			int fstUseVar = invokeInst.getUse(0);
			int sndUseVar = invokeInst.getUse(1);
			Box fstUse = new VarBox(caller, invokeInst.iindex, fstUseVar);
			Box sndUse = new VarBox(caller, invokeInst.iindex, sndUseVar);
			if(graph.addEdge(new AppendOpNode(), def, fstUse, sndUse)){
					boxSet.add(fstUse);
					boxSet.add(sndUse);
			}
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: StringBuilder.append";
		}
	}
	
	class Init implements MethodModel<Set<Box>>{
		@Override
		public Set<Box> draw(ConstraintGraph graph, Box def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			// TODO Auto-generated method stub
			Set<Box> boxSet = new HashSet<Box>();
			int useVar = invokeInst.getUse(1);
			Box use = new VarBox(caller, invokeInst.iindex, useVar);
			if(graph.addEdge(new AssignOpNode(), def, use))
					boxSet.add(use);
			
			return boxSet;
		}
	
		@Override
		public String toString(){
			return "Constraint Graph Method Model: StringBuilder.<init>";
		}
	}
}



