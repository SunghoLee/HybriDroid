package kr.ac.kaist.wala.hybridroid.test.callgraph;

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
import kr.ac.kaist.wala.hybridroid.test.TestConfig;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class MultipleHTMLLoadTest {
	public static String TEST_DIR = "callgraph" + File.separator + "reachability";
	
	
@Test
	public void loadSameJavaMethodFromDifferentHTMLPage() throws ClassHierarchyException, IllegalArgumentException, IOException, CancelException{
		File[] tests = {new File(TestConfig.getTestDir() + File.separator + TEST_DIR + File.separator + "MultiplePageLoadTest.apk")};
		
		for(File f : tests){
			String testName = f.getName();
			HybridCFGAnalysis cfgAnalysis = new HybridCFGAnalysis();
			Pair<CallGraph, PointerAnalysis<InstanceKey>> p = cfgAnalysis.main(f.getCanonicalPath(), TestConfig.getLibPath());
			CallGraph cg = p.fst;
			
			switch(testName){
			case "MultiplePageLoadTest.apk":
				IClass bridgeC = cg.getClassHierarchy().lookupClass(TypeReference.find(ClassLoaderReference.Application, "Lkr/ac/kaist/wala/hybridroid/test/multiplepagetest/JSBridge"));
				
				IMethod getLastNameM = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridgeC.getReference(), Selector.make("getLastName()Ljava/lang/String;")));
				IMethod getFirstNameM = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridgeC.getReference(), Selector.make("getFirstName()Ljava/lang/String;")));
				
				CGNode getLastNameN = cg.getNode(getLastNameM, Everywhere.EVERYWHERE);
				CGNode getFirstNameN = cg.getNode(getFirstNameM, Everywhere.EVERYWHERE);

				Set<CGNode> lastPreds = new HashSet<CGNode>();
				
				for(Iterator<CGNode> iPred = cg.getPredNodes(getLastNameN); iPred.hasNext();)
					lastPreds.add(iPred.next());

				assertTrue("getLastName method must have two predecessors.", lastPreds.size() == 2);
				
				boolean index1 = false;
				
				for(CGNode n : lastPreds){
					if(!index1 && n.toString().contains("index1.html")){
						index1 = true;
						assertTrue("index1's function must be getName.", n.toString().contains("getName"));
					}else{
						assertTrue("index2's function must be getLastName.", n.toString().contains("getLastName"));
					}
				}
				
				assertTrue("getLastName's predecessors must come from two different htmls.", index1);
				
				Set<CGNode> firstPreds = new HashSet<CGNode>();
				
				for(Iterator<CGNode> iPred = cg.getPredNodes(getFirstNameN); iPred.hasNext();)
					firstPreds.add(iPred.next());
				
				assertTrue("getFirstName method must have only one predecessor.", firstPreds.size() == 1);
				
				for(CGNode n : firstPreds){
					assertTrue("getFirstName method must be called at getName function from index1.html.", n.toString().contains("getName") && n.toString().contains("index1.html"));
				}
			}
		}
	}
}
