package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value;

public class StringTopValue extends TopValue implements IStringValue {

	private static StringTopValue instance;
	public static StringTopValue getInstance(){
		if(instance == null)
			instance = new StringTopValue();
		return instance;
	}
	
	private StringTopValue(){
	}
	
	@Override
	public IStringValue clone() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public IValue weakUpdate(IValue v) {
		// TODO Auto-generated method stub
		if(v instanceof IStringValue){
			return this;
		}else
			return TopValue.getInstance();
	}
	
	@Override
	public String toString(){
		return "StringTop";
	}
	
}
