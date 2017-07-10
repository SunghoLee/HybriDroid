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
package kr.ac.kaist.wala.hybridroid.frontend;

import com.ibm.wala.cast.ir.translator.TranslatorToCAst.Error;
import com.ibm.wala.cast.js.html.DefaultSourceExtractor;
import com.ibm.wala.cast.js.html.WebUtil;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import kr.ac.kaist.wala.hybridroid.analysis.resource.AndroidResourceAnalysis;
import kr.ac.kaist.wala.hybridroid.analysis.string.AndroidStringAnalysis;
import kr.ac.kaist.wala.hybridroid.analysis.string.AndroidStringAnalysis.HotspotDescriptor;
import kr.ac.kaist.wala.hybridroid.analysis.string.ArgumentHotspot;
import kr.ac.kaist.wala.hybridroid.analysis.string.Hotspot;
import kr.ac.kaist.wala.hybridroid.frontend.bridge.BridgeInfo;
import kr.ac.kaist.wala.hybridroid.frontend.bridge.ClassInfo;
import kr.ac.kaist.wala.hybridroid.types.HybriDroidTypes;
import kr.ac.kaist.wala.hybridroid.util.file.FileCollector;
import kr.ac.kaist.wala.hybridroid.util.file.FilePrinter;
import kr.ac.kaist.wala.hybridroid.util.file.FileWriter;
import kr.ac.kaist.wala.hybridroid.util.file.YMLParser;
import kr.ac.kaist.wala.hybridroid.utils.LocalFileReader;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Driver is a basic step for type checking. This class finds all JavaScript
 * files loaded from an Android hybrid application and maps bridges to the
 * JavaScript files.
 * 
 * @author Sungho Lee
 *
 */
public class Driver {
	private AndroidResourceAnalysis analyzeResource(String targetPath){
		return new AndroidResourceAnalysis(targetPath);
	}
	
	/**
	 * Analyze string values of the arguments of 'WebView.loadUrl'.
	 * @param libPath  Android library path.
	 * @param targetPath apk file path. 
	 * @param ara Resource analysis result.
	 * @return String analysis results.
	 * @throws IllegalArgumentException
	 */
	private AndroidStringAnalysis analyzeString(String libPath, String targetPath, AndroidResourceAnalysis ara) throws IllegalArgumentException{
		AndroidStringAnalysis asa = new AndroidStringAnalysis(ara);
		asa.setupAndroidLibs(libPath);
		asa.setExclusion(CallGraphTestUtil.REGRESSION_EXCLUSIONS);
		asa.addAnalysisScope(targetPath);
		List<Hotspot> hotspots = new ArrayList<Hotspot>();
		hotspots.add(new ArgumentHotspot(ClassLoaderReference.Application, "android/webkit/WebView", "loadUrl(Ljava/lang/String;)V", 0));
		try {
			asa.analyze(hotspots);
		} catch (ClassHierarchyException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (CallGraphBuilderCancelException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		
		return asa;
	}
	
	/**
	 * Update a map for WebView.
	 * @param m a map related with WebView. 
	 * @param k an instance key representing an object of WebView.
	 * @param t an object which is inserted to the map.
	 */
	private <T> void putWebViewMap(Map<InstanceKey, Set<T>> m, InstanceKey k, T t){
		if(!m.containsKey(k))
			m.put(k, new HashSet<T>());
		m.get(k).add(t);
	}
	
	/**
	 * Make a map representing the relation between WebView and JavaScript file loaded in the WebView.
	 * @param pa a pointer analysis result.
	 * @param asa a string analysis result.
	 * @param dirPath a directory path in which decompiled apk stores. 
	 * @return A map from an object representing a WebView to a set of JavaScript files. 
	 * @throws IOException Throws if a html file does not exist.
	 * @throws Error Throws if WebUtil is failed to crawl a JavaScript file from a html file.
	 */
	private Map<InstanceKey, Set<File>> getWebViewJavaScriptMapping(PointerAnalysis<InstanceKey> pa, AndroidStringAnalysis asa, String dirPath) throws IOException, Error{
		Map<InstanceKey, Set<File>> m = new HashMap<InstanceKey,Set<File>>();
		
		Set<File> htmls = FileCollector.collectFiles(new File(dirPath), "html", "htm");
		Map<File, File> htmlToJsMap = new HashMap<File, File>();
		Set<File> allFiles = new HashSet<File>();

		for(File html : htmls){
			File f = WebUtil.extractScriptFromHTML(html.toURI().toURL(), DefaultSourceExtractor.factory).snd;
			FilePrinter.print(f, new FileOutputStream(dirPath + File.separator + f.getName()));
			File nFile = new File(dirPath + File.separator + f.getName());
			allFiles.add(nFile);
			htmlToJsMap.put(html, nFile);
		}

		//make javascript code as seperate js file
		int i = 1;
		for(HotspotDescriptor hd : asa.getAllDescriptors()){
			Set<String> vs = hd.getValues();
			CGNode n = hd.getNode();
			SSAInstruction inst = hd.getInstruction();
			PointerKey wvPK = pa.getHeapModel().getPointerKeyForLocal(n, inst.getUse(0));
			for(InstanceKey ik : pa.getPointsToSet(wvPK)){
					for(String v : vs){
						if(v.startsWith("javascript:")){ // if it is javascript code, then
							String outJSName = "js_" + (i++) + ".html";
							File js = FileWriter.makeHtmlFile(dirPath, outJSName, v.substring(v.indexOf(":")+1));
							putWebViewMap(m, ik, js);
							allFiles.add(js);
						}else if(v.startsWith("http")){ // if it is online html file, then
							URL url = new URL(v);
							try {
								File jsFile = WebUtil.extractScriptFromHTML(url, DefaultSourceExtractor.factory).snd;
								FilePrinter.print(jsFile, new FileOutputStream(dirPath + File.separator + jsFile.getName()));
								File nFile = new File(dirPath + File.separator + jsFile.getName());
								putWebViewMap(m, ik, nFile);
								allFiles.add(nFile);
							}catch(RuntimeException e){
								System.err.println("Cannot get response from this url: " + url);
							}
						}else if(v.startsWith("file:///")){
							String nPath = dirPath + File.separator + v.replace("file:///", "").replace("android_asset", "assets");
							if(htmlToJsMap.containsKey(new File(nPath))){
								putWebViewMap(m, ik, htmlToJsMap.get(new File(nPath)));
							}
						}
					}
//				}
			}
		}

		for(HotspotDescriptor hd : asa.getAllDescriptors()){
			Set<String> vs = hd.getValues();
			CGNode n = hd.getNode();
			SSAInstruction inst = hd.getInstruction();
			PointerKey wvPK = pa.getHeapModel().getPointerKeyForLocal(n, inst.getUse(0));
			for(InstanceKey ik : pa.getPointsToSet(wvPK)){
				if(vs.isEmpty()){
					for(File js : allFiles){
						putWebViewMap(m, ik, js);
					}
				}
			}
		}
		return m;
	}

//	private boolean isPossibleTarget( cha, MethodReference call, MethodReference target){
//
//		return false;
//	}

	/**
	 * Make a map representing the relation between WebView and bridge information attached to the WebView.
	 * @param pa a pointer analysis result.
	 * @param cg a call graph.
	 * @return A map from an object representing a WebView to a set of bridge informations.
	 */
	private Map<InstanceKey, Set<BridgeInfo>> getWebViewBridgeMapping(PointerAnalysis<InstanceKey> pa, CallGraph cg, String dir){
		Map<InstanceKey, Set<BridgeInfo>> m = new HashMap<InstanceKey, Set<BridgeInfo>>();
		MethodReference addJsM = HybriDroidTypes.ADDJAVASCRIPTINTERFACE_APP_METHODREFERENCE;
		IMethod mm = cg.getClassHierarchy().resolveMethod(addJsM);
		CGNode addJsIM = cg.getNode(mm, Everywhere.EVERYWHERE);
		if(addJsIM == null){
			System.err.println("Cannot find call-sites to addJavascriptInterface.");
			return m;
		}
		YMLParser.YMLData data = getAppData(dir);
		boolean isAboveJELLYBEAN = isEqualOrAboveJELLY_BEAN_MR1(data);

		Iterator<CGNode> in = cg.getPredNodes(addJsIM);
		while (in.hasNext()) {
			CGNode n = in.next();
			Iterator<CallSiteReference> icsr = n.iterateCallSites();
			while (icsr.hasNext()) {
				CallSiteReference csr = icsr.next();
				if(!cg.getPossibleTargets(n, csr).contains(addJsIM))
					continue;

				IR ir = n.getIR();
				if (ir != null) {
					SymbolTable symTab = ir.getSymbolTable();
					for (SSAAbstractInvokeInstruction invokeInst : ir.getCalls(csr)) {
						PointerKey wvPK = pa.getHeapModel().getPointerKeyForLocal(n, invokeInst.getUse(0));
						String bridgeName = "unknown";
						
						if (symTab.isStringConstant(invokeInst.getUse(2))) {
							bridgeName = symTab.getStringValue(invokeInst.getUse(2));
						}

						PointerKey bridgePK = pa.getHeapModel().getPointerKeyForLocal(n, invokeInst.getUse(1));

						Set<IClass> bridgeClassSet = new HashSet<IClass>();
						for (InstanceKey ik : pa.getPointsToSet(bridgePK)) {
							bridgeClassSet.add(ik.getConcreteType());
						}

						for (InstanceKey ik : pa.getPointsToSet(wvPK)) {
							for (IClass c : bridgeClassSet)
								putWebViewMap(m, ik, new BridgeInfo(bridgeName, new ClassInfo(c, isAboveJELLYBEAN)));
						}
					}
				}
			}
		}

		return m;
	}

	private YMLParser.YMLData getAppData(String dir){
		YMLParser parser = new YMLParser(new File(dir + File.separator + "apktool.yml"));
		YMLParser.YMLData data = null;
		try {
			data = parser.parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return data;
	}
	private boolean isEqualOrAboveJELLY_BEAN_MR1(YMLParser.YMLData data){
		String name = data.getName();

		if(name.equals("targetSdkVersion")){
			int version = Integer.parseInt(data.getValue().replace("'", ""));
			if(version > 16)
				return true;
			else
				return false;
		}else{
			boolean is = true;
			for(YMLParser.YMLData child : data.getChildren())
				is &= isEqualOrAboveJELLY_BEAN_MR1(child);
			return is;
		}
	}

	/**
	 * Analyze what bridges are attached to which JavaScript files.
	 * @param apkPath a apk file path for analysis.
	 * @return A map from a JavaScript file to a set of bridge informations which could be used in the JavaScript file.
	 */
	public Map<File, Set<BridgeInfo>> analyzeBridgeMapping(String apkPath, String property){
		Properties p;
		File propFile = new File(property);
		p = new Properties();
		try {
			p.load(new FileInputStream(propFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		AndroidResourceAnalysis ara = analyzeResource(apkPath);
		AndroidStringAnalysis asa = analyzeString(LocalFileReader.androidJar(p).getPath(), apkPath, ara);
		System.out.println("#StringAnalysisEndTime: " + (System.currentTimeMillis() - Shell.START));
		CallGraph cg = asa.getCGusedInSA();
		PointerAnalysis<InstanceKey> pa = asa.getPAusedInSA();
		Map<InstanceKey, Set<File>> wvToJs = Collections.emptyMap();
		try {
			wvToJs = getWebViewJavaScriptMapping(pa, asa, ara.getDir());
		} catch (Error e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Map<InstanceKey, Set<BridgeInfo>> wvToBridge = getWebViewBridgeMapping(pa, cg, ara.getDir());
		
		Map<File, Set<BridgeInfo>> m = new HashMap<File, Set<BridgeInfo>>();
		
		for(InstanceKey wv : wvToJs.keySet()){
			Set<File> fs = wvToJs.get(wv);
			Set<BridgeInfo> bs =  wvToBridge.get(wv);
			for(File f : fs){
				if(bs != null && !bs.isEmpty()) {
					if (!m.containsKey(f))
						m.put(f, new HashSet<BridgeInfo>());
					m.get(f).addAll(bs);
				}
			}
		}	
		return m;
	}
}
