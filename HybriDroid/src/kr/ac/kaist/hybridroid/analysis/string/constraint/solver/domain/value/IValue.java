package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IDomain;

public interface IValue {
	public IValue clone();
	public IValue weakUpdate(IValue v);
	public IDomain getDomain();
}
