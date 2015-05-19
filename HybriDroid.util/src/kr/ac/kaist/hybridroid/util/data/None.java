package kr.ac.kaist.hybridroid.util.data;

public class None<T> implements Option<T> {

	@Override
	public boolean isSome() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNone() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public T get() {
		// TODO Auto-generated method stub
		throw new InternalError("'None' object can not be gotten.");
	}
}
