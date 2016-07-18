package kr.ac.kaist.wala.hybridroid.test.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.junit.Test;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;

import kr.ac.kaist.hybridroid.analysis.HybridCFGAnalysis;
import kr.ac.kaist.wala.hybridroid.test.HybriDroidTestRunner;

public class AnnotationTest {
	
	public static String TEST_DIR = "annotation";
	
	@Test
	public void missingAnnotationMethodShouldInvisible() throws ClassHierarchyException, IllegalArgumentException, IOException, CancelException{
		String dirPath = HybriDroidTestRunner.getTestDir() + File.separator + TEST_DIR;
		File dir = new File(dirPath);
		
		for(File f : dir.listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				return pathname.getName().endsWith(".apk");
			}})){
			
			HybridCFGAnalysis cfgAnalysis = new HybridCFGAnalysis();
			cfgAnalysis.main(f.getCanonicalPath(), HybriDroidTestRunner.getLibPath());
			for(String s: cfgAnalysis.getWarnings()){
				assertEquals("not passed!", "[Error] the [[JAVA_JS_BRIDGE:<Application,Lkr/ac/kaist/wala/hybridroid/test/annotation/JSBridge>],<field getLastName>] is not matched.", s);
			}
		}
	}
	
	@Test
	public void ignoreAnnotationInfoBeforeKitkat(){
		fail("this is test!");
	}
}
