package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IntegerBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IntegerTopValue;

public interface IIntegerDomain extends IDomain {
	public static IntegerTopValue TOP = IntegerTopValue.getInstance();
	public static IntegerBotValue BOT = IntegerBotValue.getInstance();
}
