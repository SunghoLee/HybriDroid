package kr.ac.kaist.hybridroid.analysis.string.constraint;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.IOperationModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.LengthOpSetModel;

public class LengthOpNode implements IOperatorNode {
	
	private static IOperationModel m;
	
	static{
		m = LengthOpSetModel.getInstance();
	}
	
	public LengthOpNode(){}
	
	@Override
	public String toString(){
		return "length";
	}

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		return m.apply(args);
	}
}
