package kr.ac.kaist.hybridroid.analysis.string.constraint;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.IOperationModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.ToStringOpSetModel;

public class ToStringOpNode implements IOperatorNode {
	
	private static IOperationModel m;
	
	static{
		m = ToStringOpSetModel.getInstance();
	}
	
	public ToStringOpNode(){}
	
	@Override
	public String toString(){
		return "toString";
	}

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		return m.apply(args);
	}
}
