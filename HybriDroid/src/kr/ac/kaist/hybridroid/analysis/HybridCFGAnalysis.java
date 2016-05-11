package kr.ac.kaist.hybridroid.analysis;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.cast.ipa.callgraph.GlobalObjectKey;
import com.ibm.wala.cast.ipa.callgraph.StandardFunctionTargetSelector;
import com.ibm.wala.cast.ipa.cha.CrossLanguageClassHierarchy;
import com.ibm.wala.cast.js.ipa.callgraph.JSCallGraphUtil;
import com.ibm.wala.cast.js.ipa.callgraph.JavaScriptConstructTargetSelector;
import com.ibm.wala.cast.js.loader.JavaScriptLoader;
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory;
import com.ibm.wala.cast.js.types.JavaScriptTypes;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisOptions.ReflectionOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.MethodTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.ComposedEntrypoints;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.IRFactory;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.strings.Atom;

import kr.ac.kaist.hybridroid.analysis.resource.AndroidResourceAnalysis;
import kr.ac.kaist.hybridroid.analysis.string.AndroidStringAnalysis;
import kr.ac.kaist.hybridroid.analysis.string.AndroidStringAnalysis.HotspotDescriptor;
import kr.ac.kaist.hybridroid.analysis.string.ArgumentHotspot;
import kr.ac.kaist.hybridroid.analysis.string.Hotspot;
import kr.ac.kaist.hybridroid.appinfo.XMLManifestReader;
import kr.ac.kaist.hybridroid.callgraph.AndroidHybridAnalysisScope;
import kr.ac.kaist.hybridroid.callgraph.AndroidHybridCallGraphBuilder;
import kr.ac.kaist.hybridroid.callgraph.AndroidHybridMethodTargetSelector;
import kr.ac.kaist.hybridroid.callgraph.HybridClassLoaderFactory;
import kr.ac.kaist.hybridroid.callgraph.HybridIRFactory;
import kr.ac.kaist.hybridroid.checker.HybridAPIMisusesChecker;
import kr.ac.kaist.hybridroid.checker.HybridAPIMisusesChecker.Warning;
import kr.ac.kaist.hybridroid.command.CommandArguments;
import kr.ac.kaist.hybridroid.models.AndroidHybridAppModel;
import kr.ac.kaist.hybridroid.shell.Shell;
import kr.ac.kaist.hybridroid.test.TaintAnalysisForHybrid;
import kr.ac.kaist.hybridroid.util.file.FileCollector;
import kr.ac.kaist.hybridroid.util.file.FileWriter;
import kr.ac.kaist.hybridroid.util.file.YMLParser;
import kr.ac.kaist.hybridroid.util.file.YMLParser.YMLData;
import kr.ac.kaist.hybridroid.utils.LocalFileReader;

/**
 * Build Control-flow graph for the target Android hybrid application. Now, it
 * supports 'addJavascriptInterface' API only to deal with communication between
 * Android Java and JavaScript. This analysis has two step. 1) Pre string
 * analysis for some string value. This phase is needed to get argument values
 * for 'loadUrl' and 'addJavascriptInterface'. 2) Build Control-flow graph. Now,
 * the string analysis can deal with constant values only. We must improve this
 * analysis for accurate Control-flow graph. We analyzed the communication
 * behaviors between Android Java and JavaScript in Android_5.0.2_r3 for this
 * implementation.
 * 
 * @author Sungho Lee
 */
public class HybridCFGAnalysis {
	
	public HybridCFGAnalysis() {

	}

	/**
	 * Set MethodTargetSelector to the AnalysisOptions.
	 * @param options
	 * @param scope
	 * @param cha
	 */
	public static void addHybridDispatchLogic(AnalysisOptions options,
			AnalysisScope scope, IClassHierarchy cha) {
		com.ibm.wala.ipa.callgraph.impl.Util.addDefaultSelectors(options, cha);

		Map<Atom, MethodTargetSelector> methodTargetSelectors = HashMapFactory
				.make();
		methodTargetSelectors.put(
				JavaScriptLoader.JS.getName(),
				new JavaScriptConstructTargetSelector(cha,
						new StandardFunctionTargetSelector(cha, options
								.getMethodTargetSelector())));
		methodTargetSelectors.put(Language.JAVA.getName(),
				options.getMethodTargetSelector());

		options.setSelector(new AndroidHybridMethodTargetSelector(
				methodTargetSelectors));
		options.setUseConstantSpecificKeys(true);
	}

	/**
	 * For debugging.
	 * @param cg
	 * @param pa
	 */
	public static void check(CallGraph cg, PointerAnalysis<InstanceKey> pa) {
		boolean find = false;
		for (CGNode node : cg) {

			if (node.toString()
					.contains(
							"< Application, Lcom/example/hellohybrid/JavascriptBridge, sendName(Ljava/lang/String;)V > Context: Everywhere")) {
				System.out.println("===========================>");
				find = true;
				for (int i = 1; i < 4; i++) {
					PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(
							node, i);
					Iterator<InstanceKey> ikIter = pa.getPointsToSet(pk)
							.iterator();
					System.out.println("#" + i + ": "
							+ pa.getPointsToSet(pk).size());
					while (ikIter.hasNext()) {
						InstanceKey ik = ikIter.next();
						System.out.println("### v" + i + ": " + ik);
						System.out.println("\t" + ik.getClass().getName());
					}
				}
				System.out.println("<===========================");
			}
		}
		if (find == false) {
			System.out.println(" Incomplete Call Graph!");
		}
	}

	private AndroidResourceAnalysis analyzeResource(String targetPath){
		return new AndroidResourceAnalysis(targetPath);
	}
	
	private AndroidStringAnalysis analyzeString(String targetPath, AndroidResourceAnalysis ara) throws ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException{
		AndroidStringAnalysis asa = new AndroidStringAnalysis(ara);
		asa.setupAndroidLibs(LocalFileReader.androidJar(Shell.walaProperties).getPath());
		asa.setExclusion(CallGraphTestUtil.REGRESSION_EXCLUSIONS);
		asa.addAnalysisScope(targetPath);
		List<Hotspot> hotspots = new ArrayList<Hotspot>();
		hotspots.add(new ArgumentHotspot(ClassLoaderReference.Application, "android/webkit/WebView", "loadUrl(Ljava/lang/String;)V", 0));
//		hotspots.add(new ArgumentHotspot(ClassLoaderReference.Application, "android/webkit/WebView", "loadData(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", 0));
//		hotspots.add(new ArgumentHotspot(ClassLoaderReference.Application, "android/webkit/WebView", "loadDataWithBaseURL(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", 0));
//		hotspots.add(new ArgumentHotspot(ClassLoaderReference.Application, "android/webkit/WebView", "loadDataWithBaseURL(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", 1));
//		hotspots.add(new ArgumentHotspot(ClassLoaderReference.Application, "android/webkit/WebView", "loadDataWithBaseURL(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", 2));
//		hotspots.add(new ArgumentHotspot(ClassLoaderReference.Application, "android/webkit/WebView", "loadDataWithBaseURL(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", 3));
		asa.analyze(hotspots);
		
		return asa;
	}
	
	private AndroidHybridAnalysisScope makeScope(String targetPath, AndroidResourceAnalysis ara, AndroidStringAnalysis asa) throws IOException{
		String dirPath = ara.getDir();
		Set<URL> jsFiles = new HashSet<URL>();
		
		Set<File> htmls = FileCollector.collectFiles(new File(dirPath), "html", "htm");
		Map<File,URL> htmlToJsMap = new HashMap<File, URL>();
		
		for(File f : htmls){
			URL js = f.toURI().toURL();
			jsFiles.add(js);
			htmlToJsMap.put(f, js);
		}
		
		//make javascript code as seperate js file
		int i = 1;
		for(HotspotDescriptor hd : asa.getAllDescriptors()){
			System.out.println(hd);
			Set<String> vs = hd.getValues();
			Map<String, String> chMap = new HashMap<String, String>();
			
			for(String v : vs){
				if(v.startsWith("javascript:")){ // if it is javascript code, then
					String outJSName = "js_" + (i++) + ".html";
					File js = FileWriter.makeHtmlFile(dirPath, outJSName, v.substring(v.indexOf(":")+1));
					jsFiles.add(js.toURI().toURL());
					chMap.put(v, js.toURI().toURL().toString()); 
				}else if(v.startsWith("http")){ // if it is online html file, then
					URL url = new URL(v);
					jsFiles.add(new URL(v));
					chMap.put(v, url.toString());
				}else if(v.startsWith("file:///")){
					String nPath = dirPath + File.separator + v.replace("file:///", "").replace("android_asset", "assets");
					if(htmlToJsMap.containsKey(new File(nPath))){
						chMap.put(v, htmlToJsMap.get(new File(nPath)).toString());
					}
				}
			}
			
			for(String s : chMap.keySet()){
				vs.remove(s);
				vs.add(chMap.get(s));
			}
		}
		if(Shell.args.has(CommandArguments.MODEL_ARG)){
			String modelingPath = Shell.args.get(CommandArguments.MODEL_ARG);
			jsFiles.add(new File(modelingPath).toURI().toURL());
		}
		
		if(jsFiles.isEmpty()){
			System.out.println("It does not have local html files or js codes");
			System.exit(-1);
		}
		
		AnalysisScopeBuilder scopeBuilder = AnalysisScopeBuilder.build(
				dirPath, new File(targetPath), false, jsFiles);
		
		return scopeBuilder.makeScope();
	}
	
	private void xmlAnalyze(String targetPath) {
		// Using manifest analysis? Not give any improvement now.
		XMLManifestReader mr = new XMLManifestReader(targetPath);
		System.out.println(mr.rootProperty());
	}
	
	private YMLData getAppData(String dir){
		YMLParser parser = new YMLParser(new File(dir + File.separator + "apktool.yml"));
		YMLData data = null;
		try {
			data = parser.parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return data;
	}
	
	private boolean isEqualOrAboveJELLY_BEAN_MR1(YMLData data){
		String name = data.getName();
		
		if(name.equals("targetSdkVersion")){
			int version = Integer.parseInt(data.getValue().replace("'", ""));
			if(version > 16)
				return true;
			else
				return false;
		}else{
			boolean is = true;
			for(YMLData child : data.getChildren())
				is &= isEqualOrAboveJELLY_BEAN_MR1(child);
			return is;
		}
	}
	/**
	 * Build Control-flow graph for the AnalysisScope.
	 * @param scope the scope that includes all target files.
	 * @throws IOException
	 * @throws ClassHierarchyException
	 * @throws IllegalArgumentException
	 * @throws CancelException
	 */
	public Pair<CallGraph, PointerAnalysis<InstanceKey>> main(String targetPath) throws IOException,
			ClassHierarchyException, IllegalArgumentException, CancelException {
		
		AndroidResourceAnalysis ara = analyzeResource(targetPath);
		String dir = ara.getDir();
		YMLData appData = getAppData(dir);
		boolean annVersion = isEqualOrAboveJELLY_BEAN_MR1(appData);
		
		System.out.println("===== App Data =====");
		System.out.println(appData);
		System.out.println("====================");
		if(!annVersion)
			System.out.println("[Warning] the target of this app is less than Android JELLY_BEAN_MR1; all public bridge methods is allowed by JS access");
		AndroidStringAnalysis asa = analyzeString(targetPath, ara);
		AndroidHybridAnalysisScope scope = makeScope(targetPath, ara, asa);
		
		JSCallGraphUtil.setTranslatorFactory(new CAstRhinoTranslatorFactory());
		HybridClassLoaderFactory loaders = new HybridClassLoaderFactory();
		AnalysisOptions options = new AnalysisOptions(scope, null);
		IClassHierarchy cha = CrossLanguageClassHierarchy.make(scope, loaders);
		IRFactory<IMethod> factory = new HybridIRFactory();
		AnalysisCache cache = new AnalysisCache(factory);
		ComposedEntrypoints roots = AndroidHybridAppModel.getEntrypoints(cha,
				scope, options, cache);
		options.setEntrypoints(roots);
		options.setReflectionOptions(ReflectionOptions.NONE);
		addHybridDispatchLogic(options, scope, cha);

		AndroidHybridCallGraphBuilder b = new AndroidHybridCallGraphBuilder(
				cha, options, cache, HybridAPIMisusesChecker.getInstance(), asa, ara, annVersion);

		CallGraph cg = b.makeCallGraph(options);
		PointerAnalysis<InstanceKey> pa = b.getPointerAnalysis();
		
		System.out.println("Done");

//		VisualizeCGTest.visualizeCallGraph(cg, "cg_dex", true);
		System.out.println("===== Not Found Error =====");
		for(String s : b.getWarning()){
			System.out.println(s);
		}
		System.out.println("===========================");
		
		printTypeWarning(b.getWarnings());

		// printNodeInsts(cg, null, "send");

		// taintTest(cg);
		
//		for(GlobalObjectKey gok : b.getGlobalObjects(JavaScriptTypes.jsName)){
//			pa.getHeapModel().
//		}
		return Pair.make(cg, pa);
	}

	private static void taintTest(CallGraph cg) {
		TaintAnalysisForHybrid tAnalyzer = new TaintAnalysisForHybrid(cg);
		tAnalyzer.analyze();
	}

	private static Set<CGNode> printNodeInsts(CallGraph cg, String nodeStr) {
		Set<CGNode> nodes = new HashSet<CGNode>();
		// System.out.println(cg);
		for (CGNode node : cg) {
			if (node.toString().contains(nodeStr)) {
				nodes.add(node);
				System.out.println("=======");
				System.out.println("\t#Node: " + node);
				IR ir = node.getIR();
				if (ir != null) {
					int index = 1;
					for (SSAInstruction inst : ir.getInstructions()) {
						System.out.println("\t\t(" + (index++) + ") " + inst);
					}
				}
				System.out.println("=======");
				System.out.println();
			}
		}
		if (nodes.size() == 0)
			System.out.println("Not found the Method: " + nodeStr);

		return nodes;
	}

	private static Set<CGNode> printNodeInsts(CallGraph cg, String className,
			String methodName) {
		Set<CGNode> nodes = new HashSet<CGNode>();
		// System.out.println(cg);
		for (CGNode node : cg) {
			if (((methodName != null) ? node.getMethod().getName().toString()
					.contains(methodName) : true)
					&& ((className != null) ? node.getMethod()
							.getDeclaringClass().getName().getClassName()
							.toString().contains(className) : true)) {
				nodes.add(node);
				System.out.println("=======");
				System.out.println("\t#Node: " + node);
				IR ir = node.getIR();
				if (ir != null) {
					int index = 1;
					for (SSAInstruction inst : ir.getInstructions()) {
						System.out.println("\t\t(" + (index++) + ") " + inst);
					}
				}
				System.out.println("=======");
				System.out.println();
			}
		}
		if (nodes.size() == 0)
			System.out.println("Not found the Method: " + methodName + "( "
					+ className + " )");

		return nodes;
	}

	private static void printTypeWarning(Set<Warning> warnings) {
		// TODO Auto-generated method stub
		System.out.println("=== Type mismatch warnings ===");
		for (Warning tw : warnings) {
			System.out.println(tw);
		}
		System.out.println("==============================");
	}
}
