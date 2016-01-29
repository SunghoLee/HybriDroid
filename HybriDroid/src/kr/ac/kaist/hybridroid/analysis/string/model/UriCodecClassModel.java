package kr.ac.kaist.hybridroid.analysis.string.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.UriCodecDecodeOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.VarBox;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;

public class UriCodecClassModel implements IClassModel{
	private static UriCodecClassModel instance;
	
	private Map<String, IMethodModel> methodMap;
	
	public static UriCodecClassModel getInstance(){
		if(instance == null)
			instance = new UriCodecClassModel();
		return instance;
	}
	
	private UriCodecClassModel(){
		methodMap = new HashMap<String, IMethodModel>();
		init();
	}
	
	private void init(){
		methodMap.put("decode", new Decode());
	}
	
	@Override
	public IMethodModel getMethod(String methodName){
		if(methodMap.containsKey(methodName))
			return methodMap.get(methodName);
		System.err.println("Unkwon 'UriCodec' method: " + methodName);
		return null;
	}
	
	class Decode implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			switch(invokeInst.getNumberOfUses()){
			case 4:
				return arg4(graph, def, caller, invokeInst);
			default : 
				StringModel.setWarning("Unknown UriCodec decode: #arg is " + invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}
		
		public Set<IBox> arg4(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int stringVar = invokeInst.getUse(0);
			int convertPlusVar = invokeInst.getUse(1);
			int charsetVar = invokeInst.getUse(2);
			int throwVar = invokeInst.getUse(3);
			
			IBox stringBox = new VarBox(caller, invokeInst.iindex, stringVar);
			IBox convertPlusBox = new VarBox(caller, invokeInst.iindex, convertPlusVar);
			IBox charsetBox = new VarBox(caller, invokeInst.iindex, charsetVar);
			IBox throwBox = new VarBox(caller, invokeInst.iindex, throwVar);
			
			if(graph.addEdge(new UriCodecDecodeOpNode(), def, stringBox, convertPlusBox, charsetBox, throwBox)){
					boxSet.add(stringBox);
					boxSet.add(convertPlusBox);
					boxSet.add(charsetBox);
					boxSet.add(throwBox);
			}
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: UriCodec.decode";
		}
		
	}
}

