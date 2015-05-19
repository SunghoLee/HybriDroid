package kr.ac.kaist.hybridroid.util.data;

public class Some<T> implements Option<T> {

	private T o;
	
	public Some(T o){
		this.o = o;
	}
	
	@Override
	public boolean isSome() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isNone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public T get() {
		// TODO Auto-generated method stub
		return o;
	}

}
