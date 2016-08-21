package kr.ac.kaist.wala.hybridroid.test.callgraph;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.Pair;

import kr.ac.kaist.hybridroid.analysis.HybridCFGAnalysis;
import kr.ac.kaist.wala.hybridroid.test.HybriDroidTestRunner;

public class MultipleWebViewTest {
	public static String TEST_DIR = "callgraph" + File.separator + "webview";
	
	@Test
	public void shareOneBridgeByTwoWebView() throws ClassHierarchyException, IllegalArgumentException, IOException, CancelException{
		File[] tests = {new File(HybriDroidTestRunner.getTestDir() + File.separator + TEST_DIR + File.separator + "MultipleWebViewTest.apk")};
		
		for(File f : tests){
			String testName = f.getName();
			HybridCFGAnalysis cfgAnalysis = new HybridCFGAnalysis();
			Pair<CallGraph, PointerAnalysis<InstanceKey>> p = cfgAnalysis.main(f.getCanonicalPath(), HybriDroidTestRunner.getLibPath());
			CallGraph cg = p.fst;
			
			switch(testName){
			case "MultipleWebViewTest.apk":
				IClass bridgeC = cg.getClassHierarchy().lookupClass(TypeReference.find(ClassLoaderReference.Application, "Lkr/ac/kaist/wala/hybridroid/test/multiplewebviewtest/JSBridge"));
				
				IMethod getNameM = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridgeC.getReference(), Selector.make("getName()Ljava/lang/String;")));
				IMethod getLastNameM = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridgeC.getReference(), Selector.make("getLastName()Ljava/lang/String;")));
				IMethod getFirstNameM = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridgeC.getReference(), Selector.make("getFirstName()Ljava/lang/String;")));
				
				CGNode getNameN = cg.getNode(getNameM, Everywhere.EVERYWHERE);
				CGNode getLastNameN = cg.getNode(getLastNameM, Everywhere.EVERYWHERE);
				CGNode getFirstNameN = cg.getNode(getFirstNameM, Everywhere.EVERYWHERE);
				
				Set<CGNode> namePreds = new HashSet<CGNode>();
				
				for(Iterator<CGNode> iPred = cg.getPredNodes(getNameN); iPred.hasNext();)
					namePreds.add(iPred.next());
				
				assertTrue("getName method must have two predecessors.", namePreds.size() == 2);
				
				boolean index1 = false;
				boolean index2 = false;
				for(CGNode n : namePreds){
					if(!index1 && n.toString().contains("index1.html")){
						index1 = true;
					}else{
						index2 = true;
					}
				}
				
				assertTrue("getName's predecessors must come from two different htmls.", index1 && index2);
				
				Set<CGNode> firstPreds = new HashSet<CGNode>();
				
				for(Iterator<CGNode> iPred = cg.getPredNodes(getFirstNameN); iPred.hasNext();)
					firstPreds.add(iPred.next());
				
				assertTrue("getFirstName method must have only one predecessor.", firstPreds.size() == 1);
				
				for(CGNode n : firstPreds){
					assertTrue("getFirstName method must be called at getName function from index1.html.", n.toString().contains("index1.html"));
				}
				
				Set<CGNode> lastPreds = new HashSet<CGNode>();
				
				for(Iterator<CGNode> iPred = cg.getPredNodes(getLastNameN); iPred.hasNext();)
					lastPreds.add(iPred.next());
				
				assertTrue("getLastName method must have only one predecessor.", lastPreds.size() == 1);
				
				for(CGNode n : lastPreds){
					assertTrue("getLastName method must be called at getName function from index2.html.", n.toString().contains("index2.html"));
				}
			}
		}
	}
	
	@Test
	public void loadMultiplePagesOnOneWebView(){
		//TODO: implements test app
	}
	
	@Test
	public void loadOnePagePerWebView(){
		//TODO: implements test app
	}
	
	@Test
	public void loadMultiplePagesPerWebView(){
		//TODO: implements test app
	}
}
	