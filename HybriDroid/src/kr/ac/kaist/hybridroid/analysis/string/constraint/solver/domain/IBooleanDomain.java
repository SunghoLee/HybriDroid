package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BooleanBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BooleanTopValue;

public interface IBooleanDomain extends IDomain {
	public static BooleanTopValue TOP = BooleanTopValue.getInstance();
	public static BooleanBotValue BOT = BooleanBotValue.getInstance();
}
