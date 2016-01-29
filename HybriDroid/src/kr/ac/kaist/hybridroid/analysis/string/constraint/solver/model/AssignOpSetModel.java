package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;

public class AssignOpSetModel implements IOperationModel<IValue> {

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		if(args.length != 1)
			throw new InternalError("Assign operation must have only one in-edge.");
		
		return args[0].clone();
	}
}
