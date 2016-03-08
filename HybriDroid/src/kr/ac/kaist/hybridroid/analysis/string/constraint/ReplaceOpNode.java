package kr.ac.kaist.hybridroid.analysis.string.constraint;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.IOperationModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.ReplaceOpSetModel;

public class ReplaceOpNode implements IOperatorNode {
	private static IOperationModel m ;
	
	static{
		m = ReplaceOpSetModel.getInstance();
	}
	
	public ReplaceOpNode(){}
	
	@Override
	public String toString(){
		return "replace";
	}

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		return m.apply(args);
	}
}
