package kr.ac.kaist.hybridroid.analysis.string.model;

import java.util.HashSet;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.AssignOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.Box;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.VarBox;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;

public class StringBufferClassModel implements StringModel{
	private static StringBufferClassModel instance;
	
	public static StringBufferClassModel getInstance(){
		if(instance == null)
			instance = new StringBufferClassModel();
		return instance;
	}
	
	private StringBufferClassModel(){}
	
	public Set<Box> toString(ConstraintGraph graph, Box def, CGNode caller, SSAInvokeInstruction invokeInst){
		Set<Box> boxSet = new HashSet<Box>();
		int useVar = invokeInst.getUse(0);
		Box use = new VarBox(caller, invokeInst.iindex, useVar);
		if(graph.addEdge(new AssignOpNode(), def, use))
				boxSet.add(use);
		return boxSet;
	}
}
