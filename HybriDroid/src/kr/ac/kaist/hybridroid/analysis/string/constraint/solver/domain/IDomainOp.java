package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain;

import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;

public interface IDomainOp<S, T> {
	public IValue alpha(S cv);
	public IValue alpha(Set<S> cv);
	public T gamma(IValue v);
}
