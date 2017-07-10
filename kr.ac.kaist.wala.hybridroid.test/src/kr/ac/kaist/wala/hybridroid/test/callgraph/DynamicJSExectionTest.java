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

public class DynamicJSExectionTest {
	public static String TEST_DIR = "callgraph" + File.separator + "reachability";
	
	@Test
	public void reachToBridgeMethodFromDynamicExecutedJS() throws ClassHierarchyException, IllegalArgumentException, IOException, CancelException{
		File[] tests = {new File(HybriDroidTestRunner.getTestDir() + File.separator + TEST_DIR + File.separator + "DynamicJSExecutionTest.apk")};
		
		for(File f : tests){
			String testName = f.getName();
			HybridCFGAnalysis cfgAnalysis = new HybridCFGAnalysis();
			Pair<CallGraph, PointerAnalysis<InstanceKey>> p = cfgAnalysis.main(f.getCanonicalPath(), HybriDroidTestRunner.getLibPath());
			CallGraph cg = p.fst;
			
			switch(testName){
			case "DynamicJSExecutionTest.apk":
				IClass bridgeC = cg.getClassHierarchy().lookupClass(TypeReference.find(ClassLoaderReference.Application, "Lkr/ac/kaist/wala/hybridroid/test/dynamicjsexecutiontest/JSBridge"));
				
				IMethod getNameM = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridgeC.getReference(), Selector.make("getName()Ljava/lang/String;")));
				IMethod getAnotherNameM = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridgeC.getReference(), Selector.make("getAnotherName()Ljava/lang/String;")));
				
				CGNode getNameN = cg.getNode(getNameM, Everywhere.EVERYWHERE);
				CGNode getAnotherNameN = cg.getNode(getAnotherNameM, Everywhere.EVERYWHERE);
				
				Set<CGNode> namePreds = new HashSet<CGNode>();
				
				for(Iterator<CGNode> iPred = cg.getPredNodes(getNameN); iPred.hasNext();)
					namePreds.add(iPred.next());
				
				assertTrue("getName method must have one predecessor.", namePreds.size() == 1);
				
				Set<CGNode> anotherPreds = new HashSet<CGNode>();
				
				for(Iterator<CGNode> iPred = cg.getPredNodes(getAnotherNameN); iPred.hasNext();)
					anotherPreds.add(iPred.next());

				assertTrue("getAnotherName method must have one predecessor.", anotherPreds.size() == 1);				
			}
		}
	}

}
