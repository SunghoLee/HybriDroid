package kr.ac.kaist.hybridroid.analysis.string.constraint;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.IOperationModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.SubstringOpSetModel;

public class SubstringOpNode implements IOperatorNode {
	
	private static IOperationModel m;
	
	static{
		m = SubstringOpSetModel.getInstance();
	}
	public SubstringOpNode(){}
	
	@Override
	public String toString(){
		return "subString";
	}

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		return m.apply(args);
	}
}
