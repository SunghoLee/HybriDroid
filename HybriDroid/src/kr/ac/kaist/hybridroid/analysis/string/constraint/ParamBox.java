package kr.ac.kaist.hybridroid.analysis.string.constraint;

import com.ibm.wala.ipa.callgraph.CGNode;

public class ParamBox implements Box {
	private CGNode node;
	private int var;
	
	public ParamBox(CGNode node, int var){
		if(var < 1)
			throw new InternalError("parameter variable cannot be less than 1: var[" + var + "] in " + node);
		this.node = node;
		this.var = var;
	}
	
	@Override
	public String toString(){
		String str = "ParamBox[";
		str += var;
		str += "] of " + node.getMethod().getName().toString();
		return str;
	}
	
	public int getVar(){
		return var;
	}
	
	@Override
	public <T> T visit(BoxVisitor<T> v){
		return v.visit(this);
	}

	@Override
	public CGNode getNode() {
		// TODO Auto-generated method stub
		return node;
	}
}
