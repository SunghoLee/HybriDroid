package kr.ac.kaist.hybridroid.soot.phantom;

import soot.SootClass;
import soot.util.Chain;

public class PhantomClassManager {
	private Chain<SootClass> phantomClasses;
	
	static private PhantomClassManager instance;
	
	static public PhantomClassManager getInstance(Chain<SootClass> phantomClasses){
		if(instance == null)
			instance = new PhantomClassManager(phantomClasses);
		return instance;
	}
	
	private PhantomClassManager(Chain<SootClass> phantomClasses){
		this.phantomClasses = phantomClasses;
	}
	
	public void setCommonSuperClass(SootClass cls){
		for(SootClass pcls : phantomClasses)
			pcls.setSuperclass(cls);
	}
}
