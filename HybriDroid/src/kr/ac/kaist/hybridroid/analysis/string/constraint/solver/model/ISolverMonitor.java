package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model;

import java.util.Queue;

import kr.ac.kaist.hybridroid.analysis.string.constraint.IConstraintNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;

public interface ISolverMonitor {
	public void monitor(Queue<IConstraintNode> worklist, IConstraintNode n, IValue preV, IValue newV, boolean isUpdate);
}
