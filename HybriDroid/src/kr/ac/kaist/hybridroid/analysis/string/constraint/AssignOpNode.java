package kr.ac.kaist.hybridroid.analysis.string.constraint;

import com.ibm.wala.util.debug.Assertions;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;

public class AssignOpNode implements IOperatorNode {	
	public AssignOpNode(){
	}
	
	@Override
	public String toString(){
		return "=";
	}

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		if(args.length != 1)
			Assertions.UNREACHABLE("AssignOpNode must have only one predecessor: " + args);
		
		return args[0];
	}
}
