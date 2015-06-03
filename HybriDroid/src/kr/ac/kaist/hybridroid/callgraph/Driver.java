package kr.ac.kaist.hybridroid.callgraph;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import kr.ac.kaist.hybridroid.models.AndroidHybridAppModel;
import kr.ac.kaist.hybridroid.util.files.LocalFileReader;

import com.ibm.wala.cast.ipa.callgraph.CrossLanguageMethodTargetSelector;
import com.ibm.wala.cast.ipa.callgraph.StandardFunctionTargetSelector;
import com.ibm.wala.cast.ipa.cha.CrossLanguageClassHierarchy;
import com.ibm.wala.cast.js.ipa.callgraph.JSCallGraphUtil;
import com.ibm.wala.cast.js.ipa.callgraph.JavaScriptConstructTargetSelector;
import com.ibm.wala.cast.js.loader.JavaScriptLoader;
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisOptions.ReflectionOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.MethodTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.ComposedEntrypoints;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.ssa.IRFactory;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.strings.Atom;

public class Driver {

	public static Properties walaProperties;
	
	static {
		try {
			walaProperties = WalaProperties.loadProperties();
		} catch (WalaException e) {
			walaProperties = null;
			assert false : e;
		}
	}
	
  public static void addDefaultDispatchLogic(AnalysisOptions options, AnalysisScope scope, IClassHierarchy cha) {
    com.ibm.wala.ipa.callgraph.impl.Util.addDefaultSelectors(options, cha);

    Map<Atom,MethodTargetSelector> methodTargetSelectors = HashMapFactory.make();
    methodTargetSelectors.put(JavaScriptLoader.JS.getName(), new JavaScriptConstructTargetSelector(cha,
        new StandardFunctionTargetSelector(cha, options.getMethodTargetSelector())));
    methodTargetSelectors.put(Language.JAVA.getName(), options.getMethodTargetSelector());
    
    
    options.setSelector(new CrossLanguageMethodTargetSelector(methodTargetSelectors));
  }

  public static void addHybridDispatchLogic(AnalysisOptions options, AnalysisScope scope, IClassHierarchy cha){
	  com.ibm.wala.ipa.callgraph.impl.Util.addDefaultSelectors(options, cha);

	    Map<Atom,MethodTargetSelector> methodTargetSelectors = HashMapFactory.make();
	    methodTargetSelectors.put(JavaScriptLoader.JS.getName(), new JavaScriptConstructTargetSelector(cha,
	        new StandardFunctionTargetSelector(cha, options.getMethodTargetSelector())));
	    methodTargetSelectors.put(Language.JAVA.getName(), options.getMethodTargetSelector());
	    
	    options.setSelector(new AndroidHybridMethodTargetSelector(methodTargetSelectors));
	    options.setUseConstantSpecificKeys(true);
  }
  
  public static void check(CallGraph cg, PointerAnalysis<InstanceKey> pa){
	  boolean find = false;
	  for(CGNode node : cg){
		  
		  if(node.toString().contains("< Application, Lcom/example/hellohybrid/JavascriptBridge, sendName(Ljava/lang/String;)V > Context: Everywhere")){
			  System.out.println("===========================>");
			  find = true;
			  for(int i=1; i<4; i++){
				  PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(node, i);
				  Iterator<InstanceKey> ikIter = pa.getPointsToSet(pk).iterator();
				  System.out.println("#"+i+": " + pa.getPointsToSet(pk).size());
				  while(ikIter.hasNext()){
					  InstanceKey ik = ikIter.next();
					  System.out.println("### v"+i+": " + ik);
					  System.out.println("\t" + ik.getClass().getName());
				  }
			  }
			  System.out.println("<===========================");
		  }
	  }
	  if(find == false){
		  System.out.println(" Incomplete Call Graph!");
	  }
  }
  
  public static void main(String[] args) throws IOException, ClassHierarchyException, IllegalArgumentException, CancelException {
    JSCallGraphUtil.setTranslatorFactory(new CAstRhinoTranslatorFactory());

    HybridClassLoaderFactory loaders = new HybridClassLoaderFactory();

    File classPath = new File(args[0]);
    
    AndroidHybridAnalysisScope scope = AndroidHybridAnalysisScope.setUpAndroidAnalysisScope(classPath.toURI(),
			CallGraphTestUtil.REGRESSION_EXCLUSIONS, LocalFileReader.androidDexLibs(walaProperties));
    
    IClassHierarchy cha = CrossLanguageClassHierarchy.make(scope, loaders);
    			
    ComposedEntrypoints roots = AndroidHybridAppModel.getEntrypoints(cha, scope);

    AnalysisOptions options = new AnalysisOptions(scope, roots);
    
    options.setReflectionOptions(ReflectionOptions.NONE);

    addHybridDispatchLogic(options, scope, cha);
//    addDefaultDispatchLogic(options, scope, cha);
    
    IRFactory<IMethod> factory = new HybridIRFactory();
    
    AnalysisCache cache = new AnalysisCache(factory);

    AndroidHybridCallGraphBuilder b = new AndroidHybridCallGraphBuilder(cha, options, cache);
    
    CallGraph cg = b.makeCallGraph(options);

    System.out.println("Done");
//    VisualizeCGTest.visualizeCallGraph(cg, "/Users/leesh/tmp/cg_dex", true);
    
    check(cg, b.getPointerAnalysis());
//    System.err.println(cg);
  }
}
