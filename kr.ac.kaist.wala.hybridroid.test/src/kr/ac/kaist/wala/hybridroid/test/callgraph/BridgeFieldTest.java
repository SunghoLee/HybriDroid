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
import kr.ac.kaist.wala.hybridroid.test.HybriDroidTestRunner;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.junit.Assert.assertTrue;

public class BridgeFieldTest {
	public static String TEST_DIR = "callgraph" + File.separator + "bridgefield";
	
	@Test
	public void subWebViewBridgeInteraction() throws ClassHierarchyException, IllegalArgumentException, IOException, CancelException{
		File[] tests = {new File(HybriDroidTestRunner.getTestDir() + File.separator + TEST_DIR + File.separator + "BridgeFieldTest.apk")};
		
		for(File f : tests){
			String testName = f.getName();
			HybridCFGAnalysis cfgAnalysis = new HybridCFGAnalysis();
			Pair<CallGraph, PointerAnalysis<InstanceKey>> p = cfgAnalysis.main(f.getCanonicalPath(), HybriDroidTestRunner.getLibPath());
			CallGraph cg = p.fst;
			
			switch(testName){
			case "BridgeFieldTest.apk":
				IClass bridgeC = cg.getClassHierarchy().lookupClass(TypeReference.find(ClassLoaderReference.Application, "Lkr/ac/kaist/wala/hybridroid/bridgefieldaccess/JSBridge"));
				
				IMethod getName = cg.getClassHierarchy().resolveMethod(MethodReference.findOrCreate(bridgeC.getReference(), Selector.make("getInfo()Ljava/lang/String;")));
				
				CGNode getNameN = cg.getNode(getName, Everywhere.EVERYWHERE);

				assertTrue("getInfo method must have 5 successors.", cg.getSuccNodeCount(getNameN) == 5);

				boolean isInit = false;
				boolean isAppend = false;
				boolean isGetName = false;
				boolean isGetPhoneNumber = false;
				boolean isGetMessage = false;

				Iterator<CGNode> iSucc = cg.getSuccNodes(getNameN);
				while(iSucc.hasNext()){
					CGNode succ = iSucc.next();
					if(succ.toString().contains("Node: < Primordial, Ljava/lang/StringBuilder, <init>()V > Context: Everywhere"))
						isInit = true;
					else if(succ.toString().contains("Node: < Primordial, Ljava/lang/StringBuilder, append(Ljava/lang/String;)Ljava/lang/StringBuilder; > Context: Everywhere"))
						isAppend = true;
					else if(succ.toString().contains("Node: < Application, Lkr/ac/kaist/wala/hybridroid/bridgefieldaccess/DataHandler, getName()Ljava/lang/String; > Context: Everywhere"))
						isGetName = true;
					else if(succ.toString().contains("Node: < Application, Lkr/ac/kaist/wala/hybridroid/bridgefieldaccess/DataHandler, getPhoneNumber()Ljava/lang/String; > Context: Everywhere"))
						isGetPhoneNumber = true;
					else if(succ.toString().contains("Node: < Application, Lkr/ac/kaist/wala/hybridroid/bridgefieldaccess/MessageHandler, getMessage()Ljava/lang/String; > Context: Everywhere"))
						isGetMessage = true;
				}

				assertTrue("<init> of StringBuilder must be called.", isInit);
				assertTrue("append of StringBuilder must be called.", isAppend);
				assertTrue("getName of DataHandler must be called.", isGetName);
				assertTrue("getPhoneNumber of DataHandler must be called.", isGetPhoneNumber);
				assertTrue("getMessage of MessageHandler must be called.", isGetMessage);
			}
		}
	}
}
