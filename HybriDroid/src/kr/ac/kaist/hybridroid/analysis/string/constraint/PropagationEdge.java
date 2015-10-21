package kr.ac.kaist.hybridroid.analysis.string.constraint;

public class PropagationEdge extends AbstractConstraintEdge {
	public PropagationEdge(ConstraintNode from, ConstraintNode to){
		super(from, to);
	}		
	
	@Override
	public String toString(){
		return from() + " -> " + to();
	}
}
