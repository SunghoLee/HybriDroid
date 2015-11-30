package kr.ac.kaist.hybridroid.analysis.string.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.AssignOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstType;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ReplaceOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.VarBox;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;

public class StringClassModel implements IClassModel{
	private static StringClassModel instance;
	
	private Map<String, IMethodModel> methodMap;
	
	public static StringClassModel getInstance(){
		if(instance == null)
			instance = new StringClassModel();
		return instance;
	}
	
	private StringClassModel(){
		methodMap = new HashMap<String, IMethodModel>();
		init();
	}
	
	private void init(){
		methodMap.put("valueOf", new ValueOf());
		methodMap.put("replace", new Replace());
		methodMap.put("<init>", new Init());
		methodMap.put("substring", new Substring());
		methodMap.put("length", new Length());
		methodMap.put("indexOf", new IndexOf());
		methodMap.put("toString", new ToString());
		methodMap.put("format", new Format());
		methodMap.put("copyValueOf", new CopyValueOf());
	}
	
	@Override
	public IMethodModel getMethod(String methodName){
		if(methodMap.containsKey(methodName))
			return methodMap.get(methodName);
		System.err.println("Unkwon 'String' method: " + methodName);
		return null;
	}
	
	class ValueOf implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			switch(invokeInst.getNumberOfUses()){
			case 1:
				return arg1(graph, def, caller, invokeInst);
			default : 
				StringModel.setWarning("Unknown String ValueOf: #arg is " + invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}
		
		public Set<IBox> arg1(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int useVar = invokeInst.getUse(0);
			IBox use = new VarBox(caller, invokeInst.iindex, useVar);
			if(graph.addEdge(new AssignOpNode(), def, use))
					boxSet.add(use);
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: String.valueOf";
		}
		
	}
	
	class Replace implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			switch(invokeInst.getNumberOfUses()){
			case 3:
				return arg3(graph, def, caller, invokeInst);
			default : 
				StringModel.setWarning("Unknown String Replace: #arg is " + invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}
		
		private Set<IBox> arg3(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int strVar = invokeInst.getUse(0);
			int toBeSubstedVar = invokeInst.getUse(1);
			int toSubstVar = invokeInst.getUse(2);
			
			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);
			IBox toBeSubstedBox = new VarBox(caller, invokeInst.iindex, toBeSubstedVar);
			IBox toSubstBox = new VarBox(caller, invokeInst.iindex, toSubstVar);
			
			if(graph.addEdge(new ReplaceOpNode(), def, strBox, toBeSubstedBox, toSubstBox)){
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
	
	class Init implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			// TODO Auto-generated method stub
			switch(invokeInst.getNumberOfUses()){
			case 0:
				return arg0(graph, def, caller, invokeInst);
			case 1:
				return arg1(graph, def, caller, invokeInst);
			default : 
				StringModel.setWarning("Unknown String <init>: #arg is " + invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}
		
		private Set<IBox> arg0(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst){
			Set<IBox> boxSet = new HashSet<IBox>();
			IBox strBox = new ConstBox(caller, "", ConstType.STRING);
			if(graph.addEdge(new AssignOpNode(), def, strBox)){
				boxSet.add(strBox);
			}
			
			return boxSet;
		}
		
		private Set<IBox> arg1(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst){
			Set<IBox> boxSet = new HashSet<IBox>();
//			int strVar = invokeInst.getUse(0);
//			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);
			IBox strBox = new ConstBox(caller, "", ConstType.STRING);
			if(graph.addEdge(new AssignOpNode(), def, strBox)){
				boxSet.add(strBox);
			}
			
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: String.<init>";
		}
	}
	
	class Substring implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			switch(invokeInst.getNumberOfUses()){
			case 2:
				return arg2(graph, def, caller, invokeInst);
			case 3:
				return arg3(graph, def, caller, invokeInst);
			default : 
				StringModel.setWarning("Unknown String Substring: #arg is " + invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}
		
		private Set<IBox> arg2(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int strVar = invokeInst.getUse(0);
			int beginVar = invokeInst.getUse(1);
			
			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);
			IBox beginBox = new VarBox(caller, invokeInst.iindex, beginVar);
			
			if(graph.addEdge(new ReplaceOpNode(), def, strBox, beginBox)){
				boxSet.add(strBox);
				boxSet.add(beginBox);
			}
					
			return boxSet;
		}
		
		private Set<IBox> arg3(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int strVar = invokeInst.getUse(0);
			int beginVar = invokeInst.getUse(1);
			int endVar = invokeInst.getUse(2);
			
			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);
			IBox beginBox = new VarBox(caller, invokeInst.iindex, beginVar);
			IBox endBox = new VarBox(caller, invokeInst.iindex, endVar);
			
			if(graph.addEdge(new ReplaceOpNode(), def, strBox, beginBox, endBox)){
				boxSet.add(strBox);
				boxSet.add(beginBox);
				boxSet.add(endBox);
			}
					
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: String.substring";
		}
	}
	
	class Length implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			switch(invokeInst.getNumberOfUses()){
			case 1:
				return arg1(graph, def, caller, invokeInst);
			default : 
				StringModel.setWarning("Unknown String Length: #arg is " + invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}
		
		private Set<IBox> arg1(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int strVar = invokeInst.getUse(0);
			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);
			if(graph.addEdge(new ReplaceOpNode(), def, strBox)){
				boxSet.add(strBox);
			}
			
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: String.length";
		}
	}
	
	class IndexOf implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			switch(invokeInst.getNumberOfUses()){
			case 2:
				return arg2(graph, def, caller, invokeInst);
			case 3:
				return arg3(graph, def, caller, invokeInst);
			default : 
				StringModel.setWarning("Unknown String IndexOf: #arg is " + invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}
		
		private Set<IBox> arg2(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int strVar = invokeInst.getUse(0);
			int charVar = invokeInst.getUse(1);
			
			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);
			IBox charBox = new VarBox(caller, invokeInst.iindex, charVar);
			if(graph.addEdge(new ReplaceOpNode(), def, strBox, charBox)){
				boxSet.add(strBox);
				boxSet.add(charBox);
			}
			
			return boxSet;
		}
		
		
		private Set<IBox> arg3(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int strVar = invokeInst.getUse(0);
			int charVar = invokeInst.getUse(1);
			int fromVar = invokeInst.getUse(2);
			
			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);
			IBox charBox = new VarBox(caller, invokeInst.iindex, charVar);
			IBox fromBox = new VarBox(caller, invokeInst.iindex, fromVar);
			if(graph.addEdge(new ReplaceOpNode(), def, strBox, charBox, fromBox)){
				boxSet.add(strBox);
				boxSet.add(charBox);
				boxSet.add(fromBox);
			}
			
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: String.indexOf";
		}
	}
	
	class ToString implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			switch(invokeInst.getNumberOfUses()){
			case 1:
				return arg1(graph, def, caller, invokeInst);
			default : 
				StringModel.setWarning("Unknown String ToString: #arg is " + invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}
		
		private Set<IBox> arg1(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int strVar = invokeInst.getUse(0);
			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);
			
			if(graph.addEdge(new ReplaceOpNode(), def, strBox)){
				boxSet.add(strBox);
			}
			
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: String.toString";
		}
	}
	
	class Format implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			switch(invokeInst.getNumberOfUses()){
			case 3:
				return arg3(graph, def, caller, invokeInst);
			case 4:
				return arg4(graph, def, caller, invokeInst);
			default : 
				StringModel.setWarning("Unknown String Format: #arg is " + invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}
		
		private Set<IBox> arg4(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int strVar = invokeInst.getUse(0);
			int localeVar = invokeInst.getUse(1);
			int formatVar = invokeInst.getUse(2);
			int objArrVar = invokeInst.getUse(3);
			
			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);
			IBox localeBox = new VarBox(caller, invokeInst.iindex, localeVar);
			IBox formatBox = new VarBox(caller, invokeInst.iindex, formatVar);
			IBox objArrBox = new VarBox(caller, invokeInst.iindex, objArrVar);
			if(graph.addEdge(new ReplaceOpNode(), def, strBox, localeBox, formatBox, objArrBox)){
				boxSet.add(strBox);
				boxSet.add(localeBox);
				boxSet.add(formatBox);
				boxSet.add(objArrBox);
			}
			
			return boxSet;
		}
		
		
		private Set<IBox> arg3(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int strVar = invokeInst.getUse(0);
			int formatVar = invokeInst.getUse(1);
			int objArrVar = invokeInst.getUse(2);
			
			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);
			IBox formatBox = new VarBox(caller, invokeInst.iindex, formatVar);
			IBox objArrBox = new VarBox(caller, invokeInst.iindex, objArrVar);
			if(graph.addEdge(new ReplaceOpNode(), def, strBox, formatBox, objArrBox)){
				boxSet.add(strBox);
				boxSet.add(formatBox);
				boxSet.add(objArrBox);
			}
			
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: String.format";
		}
	}
	
	class CopyValueOf implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			switch(invokeInst.getNumberOfUses()){
			case 2:
				return arg2(graph, def, caller, invokeInst);
			case 4:
				return arg4(graph, def, caller, invokeInst);
			default : 
				StringModel.setWarning("Unknown String Format: #arg is " + invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}
		
		private Set<IBox> arg4(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int strVar = invokeInst.getUse(0);
			int dataVar = invokeInst.getUse(1);
			int offsetVar = invokeInst.getUse(2);
			int countVar = invokeInst.getUse(3);
			
			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);
			IBox dataBox = new VarBox(caller, invokeInst.iindex, dataVar);
			IBox offsetBox = new VarBox(caller, invokeInst.iindex, offsetVar);
			IBox countBox = new VarBox(caller, invokeInst.iindex, countVar);
			if(graph.addEdge(new ReplaceOpNode(), def, strBox, dataBox, offsetBox, countBox)){
				boxSet.add(strBox);
				boxSet.add(dataBox);
				boxSet.add(offsetBox);
				boxSet.add(countBox);
			}
			
			return boxSet;
		}
		
		
		private Set<IBox> arg2(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int strVar = invokeInst.getUse(0);
			int dataVar = invokeInst.getUse(1);
			
			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);
			IBox dataBox = new VarBox(caller, invokeInst.iindex, dataVar);
			if(graph.addEdge(new ReplaceOpNode(), def, strBox, dataBox)){
				boxSet.add(strBox);
				boxSet.add(dataBox);
			}
			
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: String.format";
		}
	}
}

