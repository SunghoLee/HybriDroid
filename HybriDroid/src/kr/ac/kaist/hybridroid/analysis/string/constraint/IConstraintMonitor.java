package kr.ac.kaist.hybridroid.analysis.string.constraint;

import java.util.Set;

public interface IConstraintMonitor {
	public void monitor(int iter, ConstraintGraph graph, IBox b, Set<IBox> boxes);
}
