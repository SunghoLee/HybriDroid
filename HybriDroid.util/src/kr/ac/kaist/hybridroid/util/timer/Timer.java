package kr.ac.kaist.hybridroid.util.timer;

public class Timer {
	private long start;
	private long end;
	static public Timer start(){
		return new Timer(System.currentTimeMillis());
	}

	private Timer(long start){
		this.start = start;
	}
	
	public long end(){
		this.end = System.currentTimeMillis();
		return end - start;
	}
}
