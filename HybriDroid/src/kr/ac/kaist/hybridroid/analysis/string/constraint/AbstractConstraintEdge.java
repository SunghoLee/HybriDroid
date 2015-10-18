package kr.ac.kaist.hybridroid.analysis.string.constraint;

public class AbstractConstraintEdge implements ConstraintEdge{
	private ConstraintNode from;
	private ConstraintNode to;
	
	protected AbstractConstraintEdge(ConstraintNode from, ConstraintNode to){
		this.from = from;
		this.to = to;
	}
	
	@Override
	public ConstraintNode from(){
		return from;
	}
	
	@Override
	public ConstraintNode to(){
		return to;
	}
}
