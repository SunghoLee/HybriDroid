package kr.ac.kaist.hybridroid.analysis.string.resource.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.resource.AndroidResourceAnalysis;
import kr.ac.kaist.hybridroid.analysis.string.constraint.AssignOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstType;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.hybridroid.analysis.string.model.IClassModel;
import kr.ac.kaist.hybridroid.analysis.string.model.IMethodModel;
import kr.ac.kaist.hybridroid.analysis.string.model.StringModel;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SymbolTable;

public class ContextClassModel implements IClassModel {
	private static ContextClassModel instance;

	private Map<String, IMethodModel> methodMap;
	private AndroidResourceAnalysis ra;
	private String region;
	
	public static ContextClassModel getInstance(AndroidResourceAnalysis ra, String region) {
		if (instance == null || instance.hasAndroidResourceAnalysis() == false){
			instance = new ContextClassModel(ra, region);
		}
		return instance;
	}

	public static ContextClassModel getInstance() {
		if (instance == null)
			instance = new ContextClassModel();
		return instance;
	}
	
	private boolean hasAndroidResourceAnalysis(){
		if(ra == null)
			return false;
		return true;
	}
	
	private ContextClassModel() {
		methodMap = new HashMap<String, IMethodModel>();
		init();
	}

	private ContextClassModel(AndroidResourceAnalysis ra, String region) {
		this();
		this.ra = ra;
		this.region = region;
	}
	
	private String getString(CGNode node, int var){
		SymbolTable symTab = node.getIR().getSymbolTable();
		if(ra != null && symTab.isConstant(var)){
			int addr = (Integer)symTab.getConstantValue(var);
			String value = null;
			if(region != null)
				value = ra.getRegionString(addr, region);
			else
				value = ra.getCommonString(addr);
			
			if(value != null)
				return value;
			else{
				System.err.println("[Warning] the undefined string resource for " + addr);
				return "RESOURCE";
			}
		}
		
		System.err.println("" + (ra != null));
		System.err.println("[Warning] resource access by using unconstant value.");
		// if the variable does not have constant value, we do not calculate it; just return "RESOURCE" string value.
		return "RESOURCE";
	}
	
	private void init() {
		methodMap.put("getString", new GetString());
	}

	@Override
	public IMethodModel getMethod(String methodName) {
		if (methodMap.containsKey(methodName))
			return methodMap.get(methodName);
		System.err.println("Unkwon 'Context' method: " + methodName);
		return null;
	}

	class GetString implements IMethodModel<Set<IBox>> {

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			switch (invokeInst.getNumberOfUses()) {
			case 1:
				return arg1(graph, def, caller, invokeInst);
			case 2:
				return arg2(graph, def, caller, invokeInst);
			default:
				StringModel.setWarning("Unknown Context getString: #arg is "
						+ invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}

		public Set<IBox> arg1(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
//			int useVar = invokeInst.getUse(1);
			IBox use = new ConstBox(caller, "RESOURCE", ConstType.STRING);
			if (graph.addEdge(new AssignOpNode(), def, use))
				boxSet.add(use);
			return boxSet;
		}

		public Set<IBox> arg2(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int useVar = invokeInst.getUse(1);
			String value = getString(caller, useVar);
			IBox useBox = new ConstBox(caller, value, ConstType.STRING);
			System.out.println("" + useBox);
			if (graph.addEdge(new AssignOpNode(), def, useBox)){
				boxSet.add(useBox);
			}
			return boxSet;
		}
		
		@Override
		public String toString() {
			return "Constraint Graph Method Model: Context.getString";
		}

	}
}
