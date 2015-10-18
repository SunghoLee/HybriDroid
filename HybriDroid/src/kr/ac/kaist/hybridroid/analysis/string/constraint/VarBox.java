package kr.ac.kaist.hybridroid.analysis.string.constraint;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInstruction;

public class VarBox implements Box {
	private CGNode node;
	private int iindex;
	private int var;
	
	public VarBox(CGNode node, int iindex, int var){
		this.node = node;
		this.iindex = iindex;
		this.var = var;
	}
	
	@Override
	public String toString(){
		String str = "VarBox[";
		SSAInstruction inst = node.getIR().getInstructions()[iindex];
		str += var;
		str += "] @ " + inst;
		str += " in " + node;
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
