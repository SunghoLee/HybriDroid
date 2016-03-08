package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;

public class JoinOpSetModel implements IOperationModel {

	private static JoinOpSetModel instance;
	
	public static JoinOpSetModel getInstance(){
		if(instance == null)
			instance = new JoinOpSetModel();
		return instance;
	}
	
	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		if(args.length < 2)
			throw new InternalError("Join operation must have two in-edge at least.");
		
		IValue arg = args[0].clone();
		
		for(int i=1; i<args.length; i++){
			arg = arg.weakUpdate(args[i].clone());
		}
		return arg;
	}
}
