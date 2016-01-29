package kr.ac.kaist.hybridroid.analysis.string.constraint.solver;

import java.util.Map;

import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;

public interface IStringConstraintSolver <S, T>{
	public Map<S, T> solve(ConstraintGraph graph);
}
