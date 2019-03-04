package kr.ac.kaist.wala.hybridroid.test.callgraph;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
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

public class SubClassWebViewTest {
	public static String TEST_DIR = "callgraph" + File.separator + "subwebview";
	
	
@Test
	public void subWebViewBridgeInteraction() throws ClassHierarchyException, IllegalArgumentException, IOException, CancelException{
		File[] tests = {new File(TestConfig.getTestDir() + File.separator + TEST_DIR + File.separator + "SubWebViewTest.apk")};
		
		for(File f : tests){
			String testName = f.getName();
			HybridCFGAnalysis cfgAnalysis = new HybridCFGAnalysis();
			Pair<CallGraph, PointerAnalysis<InstanceKey>> p = cfgAnalysis.main(f.getCanonicalPath(), TestConfig.getLibPath());
			CallGraph cg = p.fst;
			
			switch(testName){
			case "SubWebViewTest.apk":
				IClass bridgeC = cg.getClassHierarchy().lookupClass(TypeReference.find(ClassLoaderReference.Application, "Lkr/ac/kaist/wala/hybridroid/subwebviewtest/JSBridge"));
				
				IMethod getName = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridgeC.getReference(), Selector.make("getName()Ljava/lang/String;")));
				
				CGNode getNameN = cg.getNode(getName, Everywhere.EVERYWHERE);
				
				Set<CGNode> lastPreds = new HashSet<CGNode>();
				
				for(Iterator<CGNode> iPred = cg.getPredNodes(getNameN); iPred.hasNext();)
					lastPreds.add(iPred.next());
				
				assertTrue("getName method must have only one predecessor.", lastPreds.size() == 1);

				boolean isCalled = false;

				for(CGNode n : lastPreds){
					if(n.toString().contains("index.html")){
						Iterator<CallSiteReference> icsr = cg.getPossibleSites(n, getNameN);
						while(icsr.hasNext()){
							CallSiteReference csr = icsr.next();
							for(SSAAbstractInvokeInstruction invokeInst : n.getIR().getCalls(csr)){
								isCalled = true;
								assertTrue("getName method must be called by the instruction: 25 = dispatch 24@20 23 exception:26", invokeInst.toString().contains("25 = dispatch 24@20 23 exception:26"));
							}
						}
					}
				}

				assertTrue("getName method must be called at one time.", isCalled);
			}
		}
	}
}
