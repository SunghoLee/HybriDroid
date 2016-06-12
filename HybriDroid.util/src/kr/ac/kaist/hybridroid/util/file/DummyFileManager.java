package kr.ac.kaist.hybridroid.util.file;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class DummyFileManager {
	private static Set<File> dummies = new HashSet<File>();
	
	public static void addDummyFile(File f){
		dummies.add(f);
	}

//	public static void removeAllDummies(){
//		for(dummies.)
//		
//	}
}
