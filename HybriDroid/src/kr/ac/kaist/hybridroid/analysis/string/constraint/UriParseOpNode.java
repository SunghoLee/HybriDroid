package kr.ac.kaist.hybridroid.analysis.string.constraint;

public class UriParseOpNode implements IConstraintNode {
	public UriParseOpNode(){}
	
	@Override
	public String toString(){
		return "Uri.parse";
	}
}
