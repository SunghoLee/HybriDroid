package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value;

public class DoubleBotValue extends BotValue implements IDoubleValue {

	private static DoubleBotValue instance;
	public static DoubleBotValue getInstance(){
		if(instance == null)
			instance = new DoubleBotValue();
		return instance;
	}
	
	private DoubleBotValue(){}
	
	@Override
	public IValue clone() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public IValue weakUpdate(IValue v) {
		// TODO Auto-generated method stub
		if(v instanceof IDoubleValue)
			return v;
		else if(v instanceof BotValue)
			return this;
		else
			return TopValue.getInstance();
	}

}
