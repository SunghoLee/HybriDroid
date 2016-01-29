package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model;

import java.util.Map;

import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IConstraintNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;

public interface ISolverMonitor {
	public void mornitor(ConstraintGraph graph, Map<IConstraintNode, IValue> heap, IConstraintNode n, IValue preV, IValue newV);
}
