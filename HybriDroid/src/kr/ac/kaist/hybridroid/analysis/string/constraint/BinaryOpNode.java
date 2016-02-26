package kr.ac.kaist.hybridroid.analysis.string.constraint;

public class BinaryOpNode implements IOperatorNode {
	public static enum BinaryOperator{
		PLUS{
			@Override
			public String toString(){
				return "+";
			}
		},
		MINUS{
			@Override
			public String toString(){
				return "-";
			}
		},
		MULTIPLY{
			@Override
			public String toString(){
				return "X";
			}
		},
		DIVIDE{
			@Override
			public String toString(){
				return "/";
			}
		},
		AND{
			@Override
			public String toString(){
				return "&&";
			}
		},
		OR{
			@Override
			public String toString(){
				return "||";
			}
		}
	};
	
	private BinaryOperator o;
	
	public BinaryOpNode(BinaryOperator o){
		this.o = o;
	}
	
	@Override
	public String toString(){
		return o.toString();
	}
}
