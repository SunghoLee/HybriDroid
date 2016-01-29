package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value;

public class StringBotValue extends BotValue implements IStringValue {

	private static StringBotValue instance;
	public static StringBotValue getInstance(){
		if(instance == null)
			instance = new StringBotValue();
		return instance;
	}
	
	private StringBotValue(){}
	
	@Override
	public IValue clone() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public IValue weakUpdate(IValue v) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString(){
		return "StringBot";
	}
}
