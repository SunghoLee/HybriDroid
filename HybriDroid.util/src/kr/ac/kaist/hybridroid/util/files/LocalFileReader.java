package kr.ac.kaist.hybridroid.util.files;

import static com.ibm.wala.properties.WalaProperties.ANDROID_RT_JAVA_JAR;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class LocalFileReader {
	public static URI[] androidDexLibs(Properties walaProperties) {
		List<URI> libs = new ArrayList<URI>();
		File libFile = new File(walaProperties.getProperty(ANDROID_RT_JAVA_JAR));
		System.out.println("#lib: "+libFile.getAbsolutePath()+"("+libFile.isDirectory()+")");
		for(File lib : libFile.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("dex");
			} 
		})) {
			libs.add(lib.toURI());
		}
		return libs.toArray(new URI[ libs.size() ]);
	}
	
	public static URI[] androidJarLibs(Properties walaProperties) {
		List<URI> libs = new ArrayList<URI>();
		File libFile = new File(walaProperties.getProperty(ANDROID_RT_JAVA_JAR));
		System.out.println("#lib: "+libFile.getAbsolutePath()+"("+libFile.isDirectory()+")");
		for(File lib : libFile.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("jar");
			} 
		})) {
			libs.add(lib.toURI());
		}
		return libs.toArray(new URI[ libs.size() ]);
	}
}
