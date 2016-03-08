package kr.ac.kaist.hybridroid.analysis.string.constraint;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.IOperationModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.JoinOpSetModel;

public class JoinOpNode implements IOperatorNode {
	
	private static IOperationModel m;
	
	static{
		m = JoinOpSetModel.getInstance();
	}
	
	public JoinOpNode(){
	}
	
	@Override
	public String toString(){
		return "join";
	}

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		return m.apply(args);
	}
}
