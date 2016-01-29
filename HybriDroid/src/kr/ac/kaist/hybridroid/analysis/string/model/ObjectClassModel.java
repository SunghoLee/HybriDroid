package kr.ac.kaist.hybridroid.analysis.string.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ToStringOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.VarBox;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;

public class ObjectClassModel implements IClassModel{
	private static ObjectClassModel instance;
	
	private Map<String, IMethodModel> methodMap;
	
	public static ObjectClassModel getInstance(){
		if(instance == null)
			instance = new ObjectClassModel();
		return instance;
	}
	
	private ObjectClassModel(){
		methodMap = new HashMap<String, IMethodModel>();
		init();
	}
	
	private void init(){
		methodMap.put("toString", new ToString());
	}
	
	@Override
	public IMethodModel getMethod(String methodName){
		if(methodMap.containsKey(methodName))
			return methodMap.get(methodName);
		System.err.println("Unkwon 'Object' method: " + methodName);
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
				StringModel.setWarning("Unknown Object ToString: #arg is " + invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}
		
		public Set<IBox> arg1(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int objVar = invokeInst.getUse(0);
			IBox objBox = new VarBox(caller, invokeInst.iindex, objVar);
			if(graph.addEdge(new ToStringOpNode(), def, objBox)){
					boxSet.add(objBox);
			}
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: Object.toString";
		}
		
	}
}

