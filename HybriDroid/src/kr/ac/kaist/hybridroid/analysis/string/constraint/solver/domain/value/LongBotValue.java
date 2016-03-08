package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value;

public class LongBotValue extends BotValue implements ILongValue {
	private static LongBotValue instance;
	public static LongBotValue getInstance(){
		if(instance == null)
			instance = new LongBotValue();
		return instance;
	}
	
	private LongBotValue(){}
	
	@Override
	public IValue clone() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public IValue weakUpdate(IValue v) {
		// TODO Auto-generated method stub
		if(v instanceof ILongValue)
			return v;
		else if(v instanceof BotValue)
			return this;
		else
			return TopValue.getInstance();
	}
}
