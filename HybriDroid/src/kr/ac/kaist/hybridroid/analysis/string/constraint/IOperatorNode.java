package kr.ac.kaist.hybridroid.analysis.string.constraint;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;

public interface IOperatorNode extends IConstraintNode {
	public IValue apply(IValue... args);
}
