package kr.ac.kaist.hybridroid.analysis.string.constraint;

import com.ibm.wala.ipa.callgraph.CGNode;

public class VarBox implements Box {
	private CGNode node;
	private int iindex;
	private int var;
	
	public VarBox(CGNode node, int iindex, int var){
		if(var < 1){
			String msg = "variable cannot be less than 1: var[" + var + "] in " + node;
			if(iindex > -1)
				 msg += "\nat " + node.getIR().getInstructions()[iindex];
			throw new InternalError(msg);
		}
		
		this.node = node;
		this.iindex = iindex;
		this.var = var;
	}
	
	@Override
	public String toString(){
		String str = "VarBox[";
//		SSAInstruction inst = node.getIR().getInstructions()[iindex];
		str += var;
		str += "] ";//@ " + inst;
		str += " in " + node.getMethod().getName().toString() + " of " + node.getMethod().getDeclaringClass().getName().getClassName();
		return str;
	}
	
	public int getVar(){
		return var;
	}
	
	public int getIndex(){
		return iindex;
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
