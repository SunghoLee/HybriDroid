package kr.ac.kaist.hybridroid.analysis.string.model;

import kr.ac.kaist.hybridroid.analysis.string.constraint.Box;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;

public interface MethodModel<T> {
	public T draw(ConstraintGraph graph, Box def, CGNode caller, SSAInvokeInstruction invokeInst);
}
