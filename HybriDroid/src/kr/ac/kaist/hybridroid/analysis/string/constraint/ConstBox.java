package kr.ac.kaist.hybridroid.analysis.string.constraint;

import com.ibm.wala.ipa.callgraph.CGNode;

public class ConstBox implements Box {
	private CGNode node;
	private Object value;
	private ConstType type;
	
	public ConstBox(CGNode node, Object value, ConstType type){
		this.node = node;
		this.value = value;
		this.type = type;
	}
	
	@Override
	public String toString(){
		String str = "ConstBox[value: ";
		str += value.toString().replace("\"", "\\\"");
		str += ", type: " + type + "] declared in ";
		str += node.getMethod().getName().toString();
		return str;
	}
	
	public Object getValue(){
		return value;
	}
	
	public ConstType getType(){
		return type;
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
