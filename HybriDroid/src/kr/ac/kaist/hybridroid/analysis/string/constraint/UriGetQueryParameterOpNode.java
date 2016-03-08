package kr.ac.kaist.hybridroid.analysis.string.constraint;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.IOperationModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.UriGetQueryParameterOpSetModel;

public class UriGetQueryParameterOpNode implements IOperatorNode {
	private static IOperationModel m;
	
	static{
		m = UriGetQueryParameterOpSetModel.getInstance();
	}
	
	public UriGetQueryParameterOpNode(){}
	
	@Override
	public String toString(){
		return "Uri.getQueryParameter";
	}

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		return m.apply(args);
	}
}
