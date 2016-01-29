package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IDomain;

public class TopValue implements IValue {
	
	private static TopValue instance;
	public static TopValue getInstance(){
		if(instance == null)
			instance = new TopValue();
		return instance;
	}
	
	protected TopValue(){}
	
	@Override
	public IValue clone() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public IValue weakUpdate(IValue v) {
		// TODO Auto-generated method stub
		return this;
	}
	
	@Override
	public IDomain getDomain(){
		return null;
	}
	
	@Override
	public String toString(){
		return "TOP";
	}
}
