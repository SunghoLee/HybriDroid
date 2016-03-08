package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;

public interface IOperationModel {
	public static boolean CRASH = true;
	public IValue apply(IValue... args);
}
