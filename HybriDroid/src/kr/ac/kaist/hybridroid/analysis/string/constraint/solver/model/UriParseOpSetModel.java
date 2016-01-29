package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;

public class UriParseOpSetModel implements IOperationModel<IValue> {

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		if(args.length != 1)
			throw new InternalError("UriParse method must have only one in-edge.");
		
		// just pass the string value.
		return args[0].clone();
	}
}
