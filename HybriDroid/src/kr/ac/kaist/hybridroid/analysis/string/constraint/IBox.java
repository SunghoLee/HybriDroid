package kr.ac.kaist.hybridroid.analysis.string.constraint;

import com.ibm.wala.ipa.callgraph.CGNode;

public interface IBox extends IConstraintNode {
	public <T> T visit(IBoxVisitor<T> v);
	public CGNode getNode();
}
