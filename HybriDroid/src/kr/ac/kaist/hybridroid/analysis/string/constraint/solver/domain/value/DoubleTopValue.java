package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value;

public class DoubleTopValue extends TopValue implements IDoubleValue {
	private static DoubleTopValue instance;
	public static DoubleTopValue getInstance(){
		if(instance == null)
			instance = new DoubleTopValue();
		return instance;
	}
	
	private DoubleTopValue(){}
	
	@Override
	public IValue clone() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public IValue weakUpdate(IValue v) {
		// TODO Auto-generated method stub
		if(v instanceof IDoubleValue)
			return this;
		else
			return TopValue.getInstance();
	}

}
