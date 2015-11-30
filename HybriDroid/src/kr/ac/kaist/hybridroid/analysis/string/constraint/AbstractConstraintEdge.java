package kr.ac.kaist.hybridroid.analysis.string.constraint;

public class AbstractConstraintEdge implements IConstraintEdge{
	private IConstraintNode from;
	private IConstraintNode to;
	
	protected AbstractConstraintEdge(IConstraintNode from, IConstraintNode to){
		this.from = from;
		this.to = to;
	}
	
	@Override
	public IConstraintNode from(){
		return from;
	}
	
	@Override
	public IConstraintNode to(){
		return to;
	}
}
