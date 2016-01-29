package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.DoubleBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.DoubleTopValue;

public interface IDoubleDomain extends IDomain {
	public static DoubleTopValue TOP = DoubleTopValue.getInstance();
	public static DoubleBotValue BOT = DoubleBotValue.getInstance();
}
