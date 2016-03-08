package kr.ac.kaist.hybridroid.analysis.string.constraint;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;

public class UnaryOpNode implements IOperatorNode {
	public static enum UnaryOperator{
		MINUS{
			@Override
			public String toString(){
				return "-";
			}
		},
		NOT{
			@Override
			public String toString(){
				return "!";
			}
		}
	};
	
	private UnaryOperator o;
	
	public UnaryOpNode(UnaryOperator o){
		this.o = o;
	}
	
	@Override
	public String toString(){
		return o.toString();
	}

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		return null;
	}
}
