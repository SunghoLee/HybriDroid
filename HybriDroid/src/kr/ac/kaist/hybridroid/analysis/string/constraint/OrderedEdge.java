package kr.ac.kaist.hybridroid.analysis.string.constraint;

public class OrderedEdge extends AbstractConstraintEdge {
	private int order;
	
	public OrderedEdge(ConstraintNode from, ConstraintNode to, int order){
		super(from, to);
		this.order = order;
	}
	
	public int getOrder(){
		return order;
	}
}
