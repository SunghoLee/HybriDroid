package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value;

public class IntegerTopValue extends TopValue implements IIntegerValue {

	private static IntegerTopValue instance;
	public static IntegerTopValue getInstance(){
		if(instance == null)
			instance = new IntegerTopValue();
		return instance;
	}
	
	private IntegerTopValue(){}
	
	@Override
	public IValue clone() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public IValue weakUpdate(IValue v) {
		// TODO Auto-generated method stub
		if(v instanceof IIntegerValue)
			return this;
		else
			return TopValue.getInstance();
	}
	
	@Override
	public String toString(){
		return "IntegerTop";
	}
}
