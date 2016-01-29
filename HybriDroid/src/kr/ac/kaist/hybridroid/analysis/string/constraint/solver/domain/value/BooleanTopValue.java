package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value;

public class BooleanTopValue extends TopValue implements IBooleanValue {
	private static BooleanTopValue instance;
	public static BooleanTopValue getInstance(){
		if(instance == null)
			instance = new BooleanTopValue();
		return instance;
	}
	
	private BooleanTopValue(){}
	
	@Override
	public IValue clone() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public IValue weakUpdate(IValue v) {
		// TODO Auto-generated method stub
		if(v instanceof IBooleanValue)
			return this;
		else
			return TopValue.getInstance();
	}

}
