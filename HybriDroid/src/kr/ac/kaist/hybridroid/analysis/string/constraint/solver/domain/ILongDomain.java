package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.LongBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.LongTopValue;

public interface ILongDomain extends IDomain {
	public static LongTopValue TOP = LongTopValue.getInstance();
	public static LongBotValue BOT = LongBotValue.getInstance();
}
