package kr.ac.kaist.wala.hybridroid.test;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import kr.ac.kaist.wala.hybridroid.test.annotation.AnnotationTest;
import kr.ac.kaist.wala.hybridroid.test.callgraph.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

public class TestConfig {

	public static Properties testProperties; 
	public static String TEST_DIR = "test_dir";
	public static String LIB_JAR = "android_jar";
	
	static{
		testProperties = new Properties();
		try {
			testProperties.load(new FileInputStream(new File("test.config")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		    System.out.println("Working Dir: " + new File(".").getAbsolutePath());
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getTestDir(){
		return testProperties.getProperty(TEST_DIR);
	}
	
	public static String getLibPath(){
		return testProperties.getProperty(LIB_JAR);
	}
}
