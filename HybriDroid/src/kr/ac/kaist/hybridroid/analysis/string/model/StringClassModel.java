package kr.ac.kaist.hybridroid.analysis.string.model;

import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;

import kr.ac.kaist.hybridroid.analysis.string.constraint.AssignOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstType;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IndexOfOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.LengthOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ReplaceOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.SubstringOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ToStringOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.VarBox;

public class StringClassModel extends AbstractClassModel{
	private static StringClassModel instance;
		
	public static StringClassModel getInstance(){
		if(instance == null)
			instance = new StringClassModel();
		return instance;
	}
	
	protected void init(){
		methodMap.put(Selector.make("valueOf(C)Ljava/lang/String;"), new ValueOf());
		methodMap.put(Selector.make("valueOf(Ljava/lang/Object;)Ljava/lang/String;"), new ValueOf());
		methodMap.put(Selector.make("valueOf(Z)Ljava/lang/String;"), new ValueOf());
		methodMap.put(Selector.make("replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;"), new Replace());
		methodMap.put(Selector.make("<init>()V"), new Init1());
		methodMap.put(Selector.make("<init>(II[C)V"), new Init2());
		methodMap.put(Selector.make("<init>([CII)V"), new Init2());
		methodMap.put(Selector.make("<init>([BII)V"), new Init2());
		methodMap.put(Selector.make("substring(II)Ljava/lang/String;"), new Substring1());
		methodMap.put(Selector.make("substring(I)Ljava/lang/String;"), new Substring2());
		methodMap.put(Selector.make("length()I"), new Length());
		methodMap.put(Selector.make("indexOf(II)I"), new IndexOf1());
		methodMap.put(Selector.make("indexOf(Ljava/lang/String;I)I"), new IndexOf1());
		methodMap.put(Selector.make("toString()Ljava/lang/String;"), new ToString());
//		methodMap.put(Selector.make("fastIndexOf(II)I"), new FastIndexOf());
		
//		methodMap.put("format", new Format());
//		methodMap.put("copyValueOf", new CopyValueOf());
	}
	
//	//fastIndexOf(II)I
//	class FastIndexOf implements IMethodModel<Set<IBox>>{
//
//		@Override
//		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller, SSAInvokeInstruction invokeInst) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//		
//		@Override
//		public String toString(){
//			return "Constraint Graph Method Model: String.fastIndexOf(II)I";
//		}
//	}
	
	//valueOf(C)Ljava/lang/String;
	//valueOf(Ljava/lang/Object;)Ljava/lang/String;
	//valueOf(Z)Ljava/lang/String;
	class ValueOf implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
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
			return "Constraint Graph Method Model: String.valueOf(C)Ljava/lang/String;";
		}
		
	}
	
	//replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
	class Replace implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
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
	
	//<init>()V
	class Init1 implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			System.err.println("\tinit1: " + invokeInst);
			for(int i=0; i < invokeInst.getNumberOfParameters()-1; i++){
				TypeReference tr = invokeInst.getDeclaredTarget().getParameterType(i);
				System.out.println("#("+i+") " + tr);
			}
			Set<IBox> boxSet = new HashSet<IBox>();
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
	
	//<init>(II[C)V
	//<init>([CII)V
	//<init>([BII)V
	class Init2 implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			System.err.println("\tinit2: " + invokeInst);
			for(int i=0; i < invokeInst.getNumberOfParameters()-1; i++){
				TypeReference tr = invokeInst.getDeclaredTarget().getParameterType(i);
				System.out.println("#("+i+") " + tr);
			}
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
	
	//substring(II)Ljava/lang/String;
	class Substring1 implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int strVar = invokeInst.getUse(0);
			int beginVar = invokeInst.getUse(1);
			int endVar = invokeInst.getUse(2);
			
			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);
			IBox beginBox = new VarBox(caller, invokeInst.iindex, beginVar);
			IBox endBox = new VarBox(caller, invokeInst.iindex, endVar);
			
			if(graph.addEdge(new SubstringOpNode(), def, strBox, beginBox, endBox)){
				boxSet.add(strBox);
				boxSet.add(beginBox);
				boxSet.add(endBox);
			}
					
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: String.substring(II)Ljava/lang/String;";
		}
	}
	
	// substring(I)Ljava/lang/String;
	class Substring2 implements IMethodModel<Set<IBox>> {

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller, SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int strVar = invokeInst.getUse(0);
			int beginVar = invokeInst.getUse(1);

			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);
			IBox beginBox = new VarBox(caller, invokeInst.iindex, beginVar);

			if (graph.addEdge(new SubstringOpNode(), def, strBox, beginBox)) {
				boxSet.add(strBox);
				boxSet.add(beginBox);
			}

			return boxSet;
		}

		@Override
		public String toString() {
			return "Constraint Graph Method Model: String.substring(I)Ljava/lang/String;";
		}
	}

	//length()I
	class Length implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int strVar = invokeInst.getUse(0);
			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);
			if(graph.addEdge(new LengthOpNode(), def, strBox)){
				boxSet.add(strBox);
			}
			
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: String.length()I";
		}
	}
	
	//indexOf(II)I
	class IndexOf1 implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int strVar = invokeInst.getUse(0);
			int charVar = invokeInst.getUse(1);
			int fromVar = invokeInst.getUse(2);
			
			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);
			IBox charBox = new VarBox(caller, invokeInst.iindex, charVar);
			IBox fromBox = new VarBox(caller, invokeInst.iindex, fromVar);
			if(graph.addEdge(new IndexOfOpNode(), def, strBox, charBox, fromBox)){
				boxSet.add(strBox);
				boxSet.add(charBox);
				boxSet.add(fromBox);
			}
			
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: String.indexOf(II)I";
		}
	}
		
	// toString()Ljava/lang/String;
	class ToString implements IMethodModel<Set<IBox>> {

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller, SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int strVar = invokeInst.getUse(0);
			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);

			if (graph.addEdge(new ToStringOpNode(), def, strBox)) {
				boxSet.add(strBox);
			}

			return boxSet;
		}

		@Override
		public String toString() {
			return "Constraint Graph Method Model: String.toString()Ljava/lang/String;";
		}
	}
}

