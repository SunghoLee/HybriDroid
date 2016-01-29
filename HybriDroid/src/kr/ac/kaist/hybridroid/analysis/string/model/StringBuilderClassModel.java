package kr.ac.kaist.hybridroid.analysis.string.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.AppendOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.AssignOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstType;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ToStringOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.VarBox;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;

public class StringBuilderClassModel implements IClassModel{
	private static StringBuilderClassModel instance;
	
	private Map<String, IMethodModel> methodMap;
	
	public static StringBuilderClassModel getInstance(){
		if(instance == null)
			instance = new StringBuilderClassModel();
		return instance;
	}
	
	private StringBuilderClassModel(){
		methodMap = new HashMap<String, IMethodModel>();
		init();
	}
	
	private void init(){
		methodMap.put("<init>", new Init());
		methodMap.put("toString", new ToString());
		methodMap.put("append", new Append());
	}
	
	@Override
	public IMethodModel getMethod(String methodName){
		if(methodMap.containsKey(methodName))
			return methodMap.get(methodName);
		System.err.println("Unkwon 'StringBuilder' method: " + methodName);
		return null;
	}
	
	class ToString implements IMethodModel<Set<IBox>>{
		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			switch(invokeInst.getNumberOfUses()){
			case 1:
				return arg1(graph, def, caller, invokeInst);
			default : 
				StringModel.setWarning("Unknown StringBuilder toString: #arg is " + invokeInst.getNumberOfUses(), true);
			}			
			return Collections.emptySet();
		}
		
		private Set<IBox> arg1(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst){
			Set<IBox> boxSet = new HashSet<IBox>();
			int useVar = invokeInst.getUse(0);
			IBox use = new VarBox(caller, invokeInst.iindex, useVar);
			if(graph.addEdge(new ToStringOpNode(), def, use))
					boxSet.add(use);
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: StringBuilder.toString";
		}
	}
	
	class Append implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			// TODO Auto-generated method stub
			switch(invokeInst.getNumberOfUses()){
			case 2:
				return arg2(graph, def, caller, invokeInst);
			default : 
				StringModel.setWarning("Unknown StringBuilder append: #arg is " + invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}
		
		private Set<IBox> arg2(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst){
			Set<IBox> boxSet = new HashSet<IBox>();
			int fstUseVar = invokeInst.getUse(0);
			int sndUseVar = invokeInst.getUse(1);
			IBox fstUse = new VarBox(caller, invokeInst.iindex, fstUseVar);
			IBox sndUse = new VarBox(caller, invokeInst.iindex, sndUseVar);
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
	
	class Init implements IMethodModel<Set<IBox>>{
		
		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			// TODO Auto-generated method stub
			switch(invokeInst.getNumberOfUses()){
			case 1:
				return arg1(graph, def, caller, invokeInst);
			case 2:
				return arg2(graph, def, caller, invokeInst);
			default : 
				StringModel.setWarning("Unknown StringBuilder <init>: #arg is " + invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}
		
		private Set<IBox> arg1(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst){
			Set<IBox> boxSet = new HashSet<IBox>();
			IBox use = new ConstBox(caller, "\"\"", ConstType.STRING);
			if(graph.addEdge(new AssignOpNode(), def, use))
					boxSet.add(use);
			
			return boxSet;
		}
		
		private Set<IBox> arg2(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst){
			Set<IBox> boxSet = new HashSet<IBox>();
			int useVar = invokeInst.getUse(1);
			IBox use = new VarBox(caller, invokeInst.iindex, useVar);
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



