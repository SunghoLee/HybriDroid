package kr.ac.kaist.hybridroid.util.data;

public interface Option<T> {
	
	public boolean isSome();
	public boolean isNone();
	public T get();
}
