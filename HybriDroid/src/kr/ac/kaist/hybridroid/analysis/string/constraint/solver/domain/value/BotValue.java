package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IDomain;

public class BotValue implements IValue {

	private static BotValue instance;
	public static BotValue getInstance(){
		if(instance == null)
			instance = new BotValue();
		return instance;
	}
	
	protected BotValue(){}
	
	@Override
	public IValue clone() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public IValue weakUpdate(IValue v) {
		// TODO Auto-generated method stub
		return v;
	}

	@Override
	public IDomain getDomain() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString(){
		return "BOT";
	}
}
