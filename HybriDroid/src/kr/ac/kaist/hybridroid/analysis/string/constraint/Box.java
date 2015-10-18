package kr.ac.kaist.hybridroid.analysis.string.constraint;

import com.ibm.wala.ipa.callgraph.CGNode;

public interface Box extends ConstraintNode {
	public <T> T visit(BoxVisitor<T> v);
	public CGNode getNode();
}
