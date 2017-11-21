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

public class HybriDroidTestRunner {

	public static Properties testProperties; 
	public static String TEST_DIR = "test_dir";
	public static String LIB_JAR = "android_jar";
	
	static{
		testProperties = new Properties();
		try {
			testProperties.load(new FileInputStream(new File("kr.ac.kaist.wala.hybridroid.test/test.config")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		    System.out.println("Working Dir: " + new File(".").getAbsolutePath());
			e.printStackTrace();
			System.exit(-1);
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

        private static boolean isSucceeded = true;
	private static void runTest(Class c, String test){
	    
	     PrintStream tmpOutStream = System.out;
	     PrintStream tmpErrStream = System.err;
   	     PrintStream privPrintStream = new PrintStream(new ByteArrayOutputStream()){
	     	   @Override
	     	   public void println(String x){
	     	   }
	     	};

	       System.out.println(test + " test...");
	     System.setOut(privPrintStream);
	     System.setErr(privPrintStream);
	     	Result result = JUnitCore.runClasses(c);
		 System.setOut(tmpOutStream);
		 System.setErr(tmpErrStream);
		if(result.getFailures().isEmpty()){
			System.out.println("Pass all " + test + " tests.");
		}else{
		    isSucceeded = false;
			System.out.println("Failed in " + test + " tests: ");
		    for (Failure failure : result.getFailures()) {
		      System.out.println(failure.toString());
		    }
		}
	}
	
	public static void main(String[] args) throws ClassHierarchyException, IllegalArgumentException, IOException, CancelException{
		runTest(AnnotationTest.class, "annotation");
		runTest(ReachableBridgeTest.class, "reachable bridge");
		runTest(MultipleHTMLLoadTest.class, "multiple pages load");
		runTest(DynamicJSExectionTest.class, "dynamic js execution");
		runTest(MultipleWebViewTest.class, "multiple webview execution");
		runTest(SubClassWebViewTest.class, "sub WebView class");
		//		runTest(BridgeFieldTest.class, "bridge field access");
		if(!isSucceeded)
		    System.exit(-1);
	}
}
