package kr.ac.kaist.hybridroid.analysis.string.constraint.solver;

import java.util.Queue;

import kr.ac.kaist.hybridroid.analysis.string.constraint.IConstraintNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.ISolverMonitor;

public class IterationSolverMonitor implements ISolverMonitor {

	private int iter = 1;
	@Override
	public void monitor(Queue<IConstraintNode> worklist, IConstraintNode n, IValue preV, IValue newV, boolean isUpdate) {
		// TODO Auto-generated method stub
		String print = "#" + (iter++) + ": ";
		print += "(" + worklist.size() + ")";
		print += n +" ";
		print += preV + " => " + newV + " [" + isUpdate + "]";
//		System.out.println(print);
	}
}
