package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value;

public class BooleanBotValue extends TopValue implements IBooleanValue {

	private static BooleanBotValue instance;
	public static BooleanBotValue getInstance(){
		if(instance == null)
			instance = new BooleanBotValue();
		return instance;
	}
	
	private BooleanBotValue(){}
	
	@Override
	public IValue clone() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public IValue weakUpdate(IValue v) {
		// TODO Auto-generated method stub
		if(v instanceof IBooleanValue)
			return v;
		else if(v instanceof BotValue)
			return this;
		return TopValue.getInstance();
	}
}
