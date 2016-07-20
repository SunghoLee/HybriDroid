package kr.ac.kaist.wala.hybridroid.test;

import java.io.File;
import java.io.FileFilter;

public class FileCollector {
	public static File[] getAPKsInDir(String dirPath){
		File dir = new File(dirPath);
		
		return dir.listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				return pathname.getName().endsWith(".apk");
			}});
	}
}
