package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value;

public class IntegerBotValue extends BotValue implements IIntegerValue {

	private static IntegerBotValue instance;
	public static IntegerBotValue getInstance(){
		if(instance == null)
			instance = new IntegerBotValue();
		return instance;
	}
	
	private IntegerBotValue(){}
	
	@Override
	public IValue clone() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public IValue weakUpdate(IValue v) {
		// TODO Auto-generated method stub
		if(v instanceof IIntegerValue)
			return v;
		else
			return TopValue.getInstance();
	}

}
