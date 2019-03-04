package kr.ac.kaist.wala.hybridroid.test.annotation;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import kr.ac.kaist.wala.hybridroid.analysis.HybridCFGAnalysis;
import kr.ac.kaist.wala.hybridroid.test.FileCollector;
import kr.ac.kaist.wala.hybridroid.test.TestConfig;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AnnotationTest {
	
	public static String TEST_DIR = "annotation";
	
	
@Test
	public void missingAnnotationMethodShouldInvisible() throws ClassHierarchyException, IllegalArgumentException, IOException, CancelException{
		File[] tests = FileCollector.getAPKsInDir(TestConfig.getTestDir() + File.separator + TEST_DIR);
		boolean AnnotationTest_apk = false;
		for(File f : tests){
			String testName = f.getName();
			HybridCFGAnalysis cfgAnalysis = new HybridCFGAnalysis();
			cfgAnalysis.main(f.getCanonicalPath(), TestConfig.getLibPath());
								System.out.println("name: DONE??");
			for(String s: cfgAnalysis.getWarnings()){
				switch(testName){
				case "AnnotationTest.apk":
					AnnotationTest_apk = true;
					assertEquals(testName + ": ", "[Error] the [[JAVA_JS_BRIDGE:<Application,Lkr/ac/kaist/wala/hybridroid/test/annotation/JSBridge>],<field getLastName>] is not matched.", s);
					break;
				}
			}
		}
		
		assertTrue(AnnotationTest_apk);
	}
	
	
@Test
	public void ignoreAnnotationInfoBeforeKitkat(){
		//TODO: implement test apps
	}	
}
