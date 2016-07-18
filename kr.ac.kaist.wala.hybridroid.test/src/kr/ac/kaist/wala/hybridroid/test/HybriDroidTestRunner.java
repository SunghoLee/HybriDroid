package kr.ac.kaist.wala.hybridroid.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;

import kr.ac.kaist.wala.hybridroid.test.annotation.AnnotationTest;

public class HybriDroidTestRunner {

	public static Properties testProperties; 
	public static String TEST_DIR = "test_dir";
	public static String LIB_JAR = "android_jar";
	
	static{
		testProperties = new Properties();
		try {
			testProperties.load(new FileInputStream(new File("test.config")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		walaProperties.load(new FileInputStream(propFile));
	}
	
	public static String getTestDir(){
		return testProperties.getProperty(TEST_DIR);
	}
	
	public static String getLibPath(){
		return testProperties.getProperty(LIB_JAR);
	}
	public static void main(String[] args) throws ClassHierarchyException, IllegalArgumentException, IOException, CancelException{

		Result result = JUnitCore.runClasses(AnnotationTest.class);
	    for (Failure failure : result.getFailures()) {
	      System.out.println(failure.toString());
	    }
	}
}
