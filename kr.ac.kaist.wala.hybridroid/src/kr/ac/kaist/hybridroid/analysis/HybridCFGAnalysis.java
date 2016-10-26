/*******************************************************************************
* Copyright (c) 2016 IBM Corporation and KAIST.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* KAIST - initial API and implementation
*******************************************************************************/
package kr.ac.kaist.hybridroid.analysis;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.MethodTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.ComposedEntrypoints;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IRFactory;
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
import kr.ac.kaist.hybridroid.models.AndroidHybridAppModel;
import kr.ac.kaist.hybridroid.util.file.FileCollector;
import kr.ac.kaist.hybridroid.util.file.FileWriter;
import kr.ac.kaist.hybridroid.util.file.YMLParser;
import kr.ac.kaist.hybridroid.util.file.YMLParser.YMLData;

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
	private Set<String> warnings;
	
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

	private AndroidResourceAnalysis analyzeResource(String targetPath){
		return new AndroidResourceAnalysis(targetPath);
	}
	
	private AndroidStringAnalysis analyzeString(String libPath, String targetPath, AndroidResourceAnalysis ara) throws ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException{
		AndroidStringAnalysis asa = new AndroidStringAnalysis(ara);
		asa.setupAndroidLibs(libPath);
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
	
	private AndroidHybridAnalysisScope makeScope(String libPath, String targetPath, AndroidResourceAnalysis ara, AndroidStringAnalysis asa) throws IOException{
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
		
		if(jsFiles.isEmpty()){
			System.out.println("It does not have local html files or js codes");
			System.exit(-1);
		}
		
		AnalysisScopeBuilder scopeBuilder = AnalysisScopeBuilder.build(
				dirPath, new File(targetPath), false, jsFiles);
		
		return scopeBuilder.makeScope(libPath);
	}
	
	private void xmlAnalyze(String targetPath) {
		// Need to use manifest analysis? No need to improve this now.
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
	public Pair<CallGraph, PointerAnalysis<InstanceKey>> main(String targetPath, String libPath) throws IOException,
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
		AndroidStringAnalysis asa = analyzeString(libPath, targetPath, ara);
		AndroidHybridAnalysisScope scope = makeScope(libPath, targetPath, ara, asa);
		
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
		
		warnings = b.getWarning();
		//print warning
		System.out.println("===== Not Found Error =====");
		for(String s : warnings){
			System.out.println(s);
		}
		System.out.println("===========================");

		return Pair.make(cg, pa);
	}
	
	public Set<String> getWarnings(){
		return warnings;
	}
}
