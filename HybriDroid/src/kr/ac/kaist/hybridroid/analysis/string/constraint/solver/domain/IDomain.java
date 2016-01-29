package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.TopValue;

public interface IDomain {
	public static TopValue TOP = TopValue.getInstance();
	public static BotValue BOT = BotValue.getInstance();
	public IDomainOp getOperator();
	public boolean isInstanceof(IValue v);
}
