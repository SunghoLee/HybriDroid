package kr.ac.kaist.hybridroid.analysis.string.constraint;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.IOperationModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.UriParseOpSetModel;

public class UriParseOpNode implements IOperatorNode {
	
	private static IOperationModel m;
	
	static{
		m = UriParseOpSetModel.getInstance();
	}
	
	public UriParseOpNode(){}
	
	@Override
	public String toString(){
		return "Uri.parse";
	}

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		return m.apply(args);
	}
}
