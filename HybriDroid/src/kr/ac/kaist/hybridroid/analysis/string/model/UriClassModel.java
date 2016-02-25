package kr.ac.kaist.hybridroid.analysis.string.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.Selector;

import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.UriGetQueryParameterOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.UriParseOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.VarBox;

public class UriClassModel  extends AbstractClassModel{
	private static UriClassModel instance;
	
	public static UriClassModel getInstance(){
		if(instance == null)
			instance = new UriClassModel();
		return instance;
	}
	
	protected void init(){
		methodMap.put(Selector.make("toString()Ljava/lang/String;"), new ToString());
//		methodMap.put("getQueryParameter", new GetQueryParameter());
//		methodMap.put("parse", new Parse());
		
	}
		
	//toString()Ljava/lang/String;
	class ToString implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller, SSAInvokeInstruction invokeInst) {
			// TODO Auto-generated method stub
			Set<IBox> boxSet = new HashSet<IBox>();
			int uriVar = invokeInst.getUse(0);
			IBox uriBox = new VarBox(caller, invokeInst.iindex, uriVar);
			if(graph.addEdge(new UriGetQueryParameterOpNode(), def, uriBox)){
					boxSet.add(uriBox);
			}
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: Uri.toString()Ljava/lang/String;";
		}
	}
	
	class GetQueryParameter implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			switch(invokeInst.getNumberOfUses()){
			case 2:
				return arg2(graph, def, caller, invokeInst);
			default : 
				StringModel.setWarning("Unknown Uri GetQueryParameter: #arg is " + invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}
		
		public Set<IBox> arg2(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int uriVar = invokeInst.getUse(0);
			int keyVar = invokeInst.getUse(1);
			IBox uriBox = new VarBox(caller, invokeInst.iindex, uriVar);
			IBox keyBox = new VarBox(caller, invokeInst.iindex, keyVar);
			if(graph.addEdge(new UriGetQueryParameterOpNode(), def, uriBox, keyBox)){
					boxSet.add(uriBox);
					boxSet.add(keyBox);
			}
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: Uri.getQueryParameter";
		}
		
	}
	
	class Parse implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			switch(invokeInst.getNumberOfUses()){
			case 1:
				return arg1(graph, def, caller, invokeInst);
			default : 
				StringModel.setWarning("Unknown Uri parse: #arg is " + invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}
		
		public Set<IBox> arg1(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int strVar = invokeInst.getUse(0);
			IBox strBox = new VarBox(caller, invokeInst.iindex, strVar);
			if(graph.addEdge(new UriParseOpNode(), def, strBox)){
					boxSet.add(strBox);
			}
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: Uri.parse";
		}
		
	}
}

