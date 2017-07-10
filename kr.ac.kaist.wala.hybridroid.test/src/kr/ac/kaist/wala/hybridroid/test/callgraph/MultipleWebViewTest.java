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

import kr.ac.kaist.wala.hybridroid.analysis.HybridCFGAnalysis;
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
	public void loadMultiplePagesOnOneWebView() throws ClassHierarchyException, IllegalArgumentException, IOException, CancelException{
		File[] tests = {new File(HybriDroidTestRunner.getTestDir() + File.separator + TEST_DIR + File.separator + "MultipleWebViewAndDiffBridgeTest.apk")};
		
		for(File f : tests){
			String testName = f.getName();
			HybridCFGAnalysis cfgAnalysis = new HybridCFGAnalysis();
			Pair<CallGraph, PointerAnalysis<InstanceKey>> p = cfgAnalysis.main(f.getCanonicalPath(), HybriDroidTestRunner.getLibPath());
			CallGraph cg = p.fst;
			
			switch(testName){
			case "MultipleWebViewAndDiffBridgeTest.apk":
				IClass bridge1C = cg.getClassHierarchy().lookupClass(TypeReference.find(ClassLoaderReference.Application, "Lkr/ac/kaist/wala/hybridroid/test/multiplewebviewanddiffbridgetest/JSBridge1"));
				IClass bridge2C = cg.getClassHierarchy().lookupClass(TypeReference.find(ClassLoaderReference.Application, "Lkr/ac/kaist/wala/hybridroid/test/multiplewebviewanddiffbridgetest/JSBridge2"));
				
				IMethod getName1M = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridge1C.getReference(), Selector.make("getName()Ljava/lang/String;")));
				IMethod getLastName1M = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridge1C.getReference(), Selector.make("getLastName()Ljava/lang/String;")));
				IMethod getFirstName1M = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridge1C.getReference(), Selector.make("getFirstName()Ljava/lang/String;")));
				
				IMethod getName2M = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridge2C.getReference(), Selector.make("getName()Ljava/lang/String;")));
				IMethod getLastName2M = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridge2C.getReference(), Selector.make("getLastName()Ljava/lang/String;")));
				IMethod getFirstName2M = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridge2C.getReference(), Selector.make("getFirstName()Ljava/lang/String;")));
				
				CGNode getName1N = cg.getNode(getName1M, Everywhere.EVERYWHERE);
				CGNode getLastName1N = cg.getNode(getLastName1M, Everywhere.EVERYWHERE);
				CGNode getFirstName1N = cg.getNode(getFirstName1M, Everywhere.EVERYWHERE);
				
				CGNode getName2N = cg.getNode(getName2M, Everywhere.EVERYWHERE);
				CGNode getLastName2N = cg.getNode(getLastName2M, Everywhere.EVERYWHERE);
				CGNode getFirstName2N = cg.getNode(getFirstName2M, Everywhere.EVERYWHERE);
				
				Set<CGNode> name1Preds = new HashSet<CGNode>();
				
				for(Iterator<CGNode> iPred = cg.getPredNodes(getName1N); iPred.hasNext();)
					name1Preds.add(iPred.next());
				
				assertTrue("getName(of first bridge) method must have two predecessors.", name1Preds.size() == 2);
				
				boolean index1 = false;
				boolean index2 = false;
				for(CGNode n : name1Preds){
					if(!index1 && n.toString().contains("index1.html")){
						index1 = true;
					}else{
						index2 = true;
					}
				}
				
				assertTrue("getName(of first bridge)'s predecessors must come from two different htmls.", index1 && index2);
				
				Set<CGNode> name2Preds = new HashSet<CGNode>();
				
				for(Iterator<CGNode> iPred = cg.getPredNodes(getName2N); iPred.hasNext();)
					name2Preds.add(iPred.next());
				
				assertTrue("getName(of second bridge) method must one predecessor.", name2Preds.size() == 1);
				
				index1 = false;
				index2 = false;
				for(CGNode n : name2Preds){
					if(!index1 && n.toString().contains("index1.html")){
						index1 = true;
					}else{
						index2 = true;
					}
				}
				
				assertTrue("getName(of second bridge)'s predecessor must come from index2.html.", !index1 && index2);
				
				Set<CGNode> first1Preds = new HashSet<CGNode>();
				
				for(Iterator<CGNode> iPred = cg.getPredNodes(getFirstName1N); iPred.hasNext();)
					first1Preds.add(iPred.next());
				
				assertTrue("getFirstName(of first bridge) method must have two predecessors.", first1Preds.size() == 2);
				
				index1 = false;
				index2 = false;
				for(CGNode n : first1Preds){
					if(!index1 && n.toString().contains("index1.html")){
						index1 = true;
					}else{
						index2 = true;
					}
				}
				
				assertTrue("getFirstName(of first bridge)'s predecessor must come from two different htmls.", index1 && index2);
				
				Set<CGNode> first2Preds = new HashSet<CGNode>();
				
				for(Iterator<CGNode> iPred = cg.getPredNodes(getFirstName2N); iPred.hasNext();)
					first2Preds.add(iPred.next());
				
				assertTrue("getFirstName(of second bridge) method must one predecessor.", first2Preds.size() == 1);
				
				index1 = false;
				index2 = false;
				for(CGNode n : first2Preds){
					if(!index1 && n.toString().contains("index1.html")){
						index1 = true;
					}else{
						index2 = true;
					}
				}
				
				assertTrue("getFirstName(of second bridge)'s predecessor must come from index2.html.", !index1 && index2);
				
				
				Set<CGNode> last1Preds = new HashSet<CGNode>();
				
				for(Iterator<CGNode> iPred = cg.getPredNodes(getLastName1N); iPred.hasNext();)
					last1Preds.add(iPred.next());
				
				assertTrue("getLastName(of first bridge) method must have two predecessors.", last1Preds.size() == 2);
				
				index1 = false;
				index2 = false;
				for(CGNode n : last1Preds){
					if(!index1 && n.toString().contains("index1.html")){
						index1 = true;
					}else{
						index2 = true;
					}
				}
				
				assertTrue("getLastName(of first bridge)'s predecessor must come from two different htmls.", index1 && index2);
				
				Set<CGNode> last2Preds = new HashSet<CGNode>();
				
				for(Iterator<CGNode> iPred = cg.getPredNodes(getLastName2N); iPred.hasNext();)
					last2Preds.add(iPred.next());
				
				assertTrue("getLastName(of second bridge) method must one predecessor.", last2Preds.size() == 1);
				
				index1 = false;
				index2 = false;
				for(CGNode n : last2Preds){
					if(!index1 && n.toString().contains("index1.html")){
						index1 = true;
					}else{
						index2 = true;
					}
				}
				
				assertTrue("getLastName(of second bridge)'s predecessor must come from index2.html.", !index1 && index2);
			}
		}
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
	