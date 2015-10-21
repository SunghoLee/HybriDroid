package kr.ac.kaist.hybridroid.util.data;

public class Pair<X,Y> {
	private X fst;
	private Y snd;
	
	static public <X,Y> Pair<X,Y> make(X fst, Y snd){
		return new Pair<X, Y>(fst, snd);
	}
	
	private Pair(X fst, Y snd){
		this.fst = fst;
		this.snd = snd;
	}
	
	public X fst(){
		return fst;
	}
	
	public Y snd(){
		return snd;
	}
	
	@Override
	public int hashCode(){
		return fst.hashCode() + snd.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Pair){
			if(fst.equals(((Pair) o).fst()) && snd.equals(((Pair) o).snd()))
				return true;
		}
			
		return false;
	}
	
	@Override
	public String toString(){
		return "[ " + fst + ", " + snd + "]";
	}
}
