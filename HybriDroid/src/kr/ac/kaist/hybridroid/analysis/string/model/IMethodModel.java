package kr.ac.kaist.hybridroid.analysis.string.model;

import kr.ac.kaist.hybridroid.analysis.resource.AndroidResourceAnalysis;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;

public interface IMethodModel<T> {
	public T draw(ConstraintGraph graph, IBox def, CGNode caller, SSAInvokeInstruction invokeInst);
}
