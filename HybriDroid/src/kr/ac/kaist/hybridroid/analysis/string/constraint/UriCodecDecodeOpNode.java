package kr.ac.kaist.hybridroid.analysis.string.constraint;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.IOperationModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.UriCodecDecodeOpSetModel;

public class UriCodecDecodeOpNode implements IOperatorNode {
	private static IOperationModel m;
	
	static{
		m = UriCodecDecodeOpSetModel.getInstance();
	}
	
	public UriCodecDecodeOpNode(){}
	
	@Override
	public String toString(){
		return "UriCodec.decode";
	}

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		return m.apply(args);
	}
}
