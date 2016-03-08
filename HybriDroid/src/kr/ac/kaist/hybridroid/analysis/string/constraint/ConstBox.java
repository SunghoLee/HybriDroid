package kr.ac.kaist.hybridroid.analysis.string.constraint;

import com.ibm.wala.ipa.callgraph.CGNode;

public class ConstBox implements IBox {
	private CGNode node;
	private Object value;
	private ConstType type;
	
	public ConstBox(CGNode node, Object value, ConstType type){
		this.node = node;
		this.value = value;
		this.type = type;
	}
	
	private ConstType getConstType(Object v){
		if(v instanceof String)
			return ConstType.STRING;
		else if(v instanceof Boolean)
			return ConstType.BOOL;
		else if(v instanceof Integer)
			return ConstType.INT;
		else if(v instanceof Double)
			return ConstType.DOUBLE;
		else if(v instanceof Float)
			return ConstType.DOUBLE;
		else if(v instanceof Long)
			return ConstType.LONG;
		
		return ConstType.UNKNOWN;
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
	public <T> T visit(IBoxVisitor<T> v){
		return v.visit(this);
	}

	@Override
	public CGNode getNode() {
		// TODO Auto-generated method stub
		return node;
	}
	
	@Override
	public int hashCode(){
		return node.hashCode() + value.hashCode() + type.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof ConstBox){
			ConstBox v = (ConstBox)o;
			if(node.equals(v.getNode()) && value.equals(v.getValue()) && type.equals(v.getType()))
				return true;
		}
		return false;
	}
}
