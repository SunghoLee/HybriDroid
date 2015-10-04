package kr.ac.kaist.hybridroid.util.files;

import static com.ibm.wala.properties.WalaProperties.ANDROID_RT_JAVA_JAR;
import static com.ibm.wala.properties.WalaProperties.DROIDEL_ANDROID_JAR;

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
		System.err.println("#lib: "+libFile.getAbsolutePath()+"("+libFile.isDirectory()+")");
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
	
	public static URI androidJar(Properties walaProperties) {
		File libFile = new File(walaProperties.getProperty(ANDROID_RT_JAVA_JAR));
		System.err.println("#lib: "+libFile.getAbsolutePath()+"("+libFile.exists()+")");
		
		return libFile.toURI();
	}
	
	public static File droidelAndroidLib(Properties walaProperties) {
		File libFile = new File(walaProperties.getProperty(DROIDEL_ANDROID_JAR));
		System.err.println("#android_lib: "+libFile.getAbsolutePath());
		return libFile;
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
