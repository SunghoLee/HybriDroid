package kr.ac.kaist.wala.hybridroid.test.callgraph;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.junit.Test;

import com.ibm.wala.cast.js.types.JavaScriptTypes;
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
import kr.ac.kaist.wala.hybridroid.test.FileCollector;
import kr.ac.kaist.wala.hybridroid.test.HybriDroidTestRunner;

public class ReachableBridgeTest {
	public static String TEST_DIR = "callgraph" + File.separator + "reachability";
	
	@Test
	public void haveEdgesToReachableJavaMethodFromJavaScript() throws ClassHierarchyException, IllegalArgumentException, IOException, CancelException{
		File[] tests = FileCollector.getAPKsInDir(HybriDroidTestRunner.getTestDir() + File.separator + TEST_DIR);
		
		for(File f : tests){
			String testName = f.getName();
			HybridCFGAnalysis cfgAnalysis = new HybridCFGAnalysis();
			Pair<CallGraph, PointerAnalysis<InstanceKey>> p = cfgAnalysis.main(f.getCanonicalPath(), HybriDroidTestRunner.getLibPath());
			CallGraph cg = p.fst;
			PointerAnalysis<InstanceKey> pa = p.snd;
			
			switch(testName){
			case "ReachableMethodTest.apk":
			    IClass bridgeC = cg.getClassHierarchy().lookupClass(TypeReference.find(ClassLoaderReference.Application, "Lkr/ac/kaist/wala/hybridroid/test/reachability/JSBridge"));
			    
			    IMethod initM = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridgeC.getReference(), Selector.make("<init>()V")));
			    CGNode initNode = cg.getNode(initM, Everywhere.EVERYWHERE);
			    
			    IMethod sendM = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridgeC.getReference(), Selector.make("sendName(Ljava/lang/String;)V")));
			    CGNode sendNode = cg.getNode(sendM, Everywhere.EVERYWHERE);
			    
			    IMethod getM = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridgeC.getReference(), Selector.make("getName()Ljava/lang/String;")));
			    CGNode getNode = cg.getNode(getM, Everywhere.EVERYWHERE);
			    			   		    
			    assertNotNull("No bridge init method", initNode);
			    assertNotNull("No bridge send method", sendNode);
			    assertNotNull("No bridge get method", getNode);
				
			    assertTrue("Only one successor is needed for init method", cg.getSuccNodeCount(initNode) == 1);
			    assertTrue("Two successors are needed for send method", cg.getSuccNodeCount(sendNode) == 2);
			    assertTrue("Two one successors are needed for get method", cg.getSuccNodeCount(getNode) == 2);
			    
			    assertTrue("Send method must to be called by JS code", (cg.getPredNodes(sendNode).next()).getMethod().getDeclaringClass().getClassLoader().getReference().equals(JavaScriptTypes.jsLoader));
			    assertTrue("Get method must to be called by JS code", (cg.getPredNodes(getNode).next()).getMethod().getDeclaringClass().getClassLoader().getReference().equals(JavaScriptTypes.jsLoader));
			    
			    IClass networkC = cg.getClassHierarchy().lookupClass(TypeReference.find(ClassLoaderReference.Application, "Lkr/ac/kaist/wala/hybridroid/test/reachability/NetworkClass"));
			    IClass databaseC = cg.getClassHierarchy().lookupClass(TypeReference.find(ClassLoaderReference.Application, "Lkr/ac/kaist/wala/hybridroid/test/reachability/DatabaseClass"));
			    
			    IMethod initNWM = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(networkC.getReference(), Selector.make("<init>()V")));
			    CGNode initNWNode = cg.getNode(initNWM, Everywhere.EVERYWHERE);
			    
			    IMethod sendNWM = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(networkC.getReference(), Selector.make("send(Ljava/lang/String;)V")));
			    CGNode sendNWNode = cg.getNode(sendNWM, Everywhere.EVERYWHERE);
			    
			    IMethod initDBM = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(databaseC.getReference(), Selector.make("<init>()V")));
			    CGNode initDBNode = cg.getNode(initDBM, Everywhere.EVERYWHERE);
			    
			    IMethod getDBM = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(databaseC.getReference(), Selector.make("getName()Ljava/lang/String;")));
			    CGNode getDBNode = cg.getNode(getDBM, Everywhere.EVERYWHERE);
			    
			    for(Iterator<CGNode> iSucc = cg.getSuccNodes(sendNode); iSucc.hasNext(); ){
			    	CGNode succ = iSucc.next();
			    	
			    	assertTrue("Successor of send method must be one of init network method or send network method.", (succ.equals(initNWNode) || succ.equals(sendNWNode)));			    	
			    }
			    
			    for(Iterator<CGNode> iSucc = cg.getSuccNodes(getNode); iSucc.hasNext(); ){
			    	CGNode succ = iSucc.next();
			    	
			    	assertTrue("Successor of get method must be one of init database method or get database method.", (succ.equals(initDBNode) || succ.equals(getDBNode)));			    	
			    }
				break;
			}
		}
	}
}
