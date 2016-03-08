package kr.ac.kaist.hybridroid.analysis.string.constraint;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.IOperationModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.IndexOfOpSetModel;

public class IndexOfOpNode implements IOperatorNode {
	
	private static IOperationModel m;
	
	static{
		m = IndexOfOpSetModel.getInstance();
	}
	
	public IndexOfOpNode(){}
	
	@Override
	public String toString(){
		return "indexOf";
	}

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		return m.apply(args);
	}
}
