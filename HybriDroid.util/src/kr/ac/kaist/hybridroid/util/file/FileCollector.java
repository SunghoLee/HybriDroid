package kr.ac.kaist.hybridroid.util.file;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class FileCollector {
	public static Set<File> collectFiles(File dir, String... exts){
		Set<File> res = new HashSet<File>();
		for(File f : dir.listFiles()){
			if(f.isDirectory())
				res.addAll(collectFiles(f, exts));
			else{
				for(String ext : exts)
					if(f.getName().endsWith(ext))
						res.add(f);
			} 
		}
		return res;
	}
}
