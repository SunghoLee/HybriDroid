package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringTopValue;

public interface IStringDomain extends IDomain{
	public static StringTopValue TOP = StringTopValue.getInstance();
	public static StringBotValue BOT = StringBotValue.getInstance();
}
