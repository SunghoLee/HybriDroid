package kr.ac.kaist.hybridroid.exception;

public class InternalError extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 363458200365316710L;
	private String msg;
	public InternalError(String msg){
		this.msg = msg;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return msg;
	}
}
