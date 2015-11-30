package kr.ac.kaist.hybridroid.analysis.string.constraint;

public class OrderedEdge extends AbstractConstraintEdge {
	private int order;
	
	public OrderedEdge(IConstraintNode from, IConstraintNode to, int order){
		super(from, to);
		this.order = order;
	}
	
	public int getOrder(){
		return order;
	}
	
	@Override
	public String toString(){
		return from() + " -> " + to() + " (order: " + order + ")";
	}
}
