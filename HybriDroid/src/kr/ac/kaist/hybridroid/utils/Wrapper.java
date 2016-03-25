package kr.ac.kaist.hybridroid.utils;

public class Wrapper<T> {
	private T obj;
	
	public Wrapper(){
	}
	
	public Wrapper(T obj){
		this.obj = obj;
	}
	
	public void setObject(T obj){
		this.obj = obj;
	}
	
	public T getObject(){
		return obj;
	}
	
	public boolean has(){
		return obj != null;
	}
}
