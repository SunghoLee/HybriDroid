package kr.ac.kaist.hybridroid.analysis.string.constraint;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.AppendOpSetModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.IOperationModel;

public class AppendOpNode implements IOperatorNode {
	static private IOperationModel m;
	
	static{
		m = AppendOpSetModel.getInstance();
	}

	public AppendOpNode(){}
	
	@Override
	public String toString(){
		return "append";
	}

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		return m.apply(args);
	}
}
