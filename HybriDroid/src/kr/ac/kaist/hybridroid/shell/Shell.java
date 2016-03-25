package kr.ac.kaist.hybridroid.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.ParseException;
import org.omg.CORBA.DynAnyPackage.Invalid;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

import kr.ac.kaist.hybridroid.analysis.AnalysisScopeBuilder;
import kr.ac.kaist.hybridroid.analysis.HybridCFGAnalysis;
import kr.ac.kaist.hybridroid.analysis.resource.AndroidResourceAnalysis;
import kr.ac.kaist.hybridroid.analysis.string.AndroidStringAnalysis;
import kr.ac.kaist.hybridroid.analysis.string.AndroidStringAnalysis.HotspotDescriptor;
import kr.ac.kaist.hybridroid.analysis.string.ArgumentHotspot;
import kr.ac.kaist.hybridroid.analysis.string.Hotspot;
import kr.ac.kaist.hybridroid.appinfo.XMLManifestReader;
import kr.ac.kaist.hybridroid.command.CommandArguments;
import kr.ac.kaist.hybridroid.util.file.FileCollector;
import kr.ac.kaist.hybridroid.util.file.FileWriter;
import kr.ac.kaist.hybridroid.utils.LocalFileReader;

/**
 * HybriDroid is a framework to analyze Android hybrid applications. It is
 * based-on WALA. Now, HybriDroid supports CFG-building only. It includes
 * API-misuses checking. This process can use DROIDEL project as front-end, to
 * improve analysis accuracy and model some Android framework API.
 * 
 * @author Sungho Lee
 */
public class Shell {

	public static Properties walaProperties;

	/**
	 * HybriDroid main function. Now, There is CFG-building option only in
	 * HybriDroid.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws ClassHierarchyException
	 * @throws IllegalArgumentException
	 * @throws CancelException
	 * @throws ParseException
	 * @throws Invalid
	 */
	public static void main(String[] args) throws IOException,
			ClassHierarchyException, IllegalArgumentException, CancelException,
			ParseException, Invalid {
		CommandArguments cArgs = new CommandArguments(args);
		// Load wala property. Now, 'PROP_ARG' is essential option, so else
		// branch cannot be reached.
		if (cArgs.has(CommandArguments.PROP_ARG)) {
			String propertyfile = cArgs.get(CommandArguments.PROP_ARG);
			File propFile = new File(propertyfile);
			walaProperties = new Properties();
			walaProperties.load(new FileInputStream(propFile));
		} else {
			try {
				walaProperties = WalaProperties.loadProperties();
			} catch (WalaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Load target file for analysis.
		String targetPath = cArgs.get(CommandArguments.TARGET_ARG);
		File target = getTargetFile(targetPath);

		/**
		 * Below is the switch case for HybriDroid functions. One function of
		 * the CommandLineOptionGroup must be one case in below.
		 */
		// Build Control-flow Graph.
		if (cArgs.has(CommandArguments.CFG_ARG)) {
			AndroidResourceAnalysis ra = new AndroidResourceAnalysis(targetPath);
			String dirPath = ra.getDir();
			Set<URL> jsFiles = new HashSet<URL>();
			
			Set<File> htmls = FileCollector.collectFiles(new File(dirPath), "html", "htm");
			Map<File,URL> htmlToJsMap = new HashMap<File, URL>();
			
			for(File f : htmls){
				URL js = f.toURI().toURL();
				jsFiles.add(js);
				htmlToJsMap.put(f, js);
			}
			
			AndroidStringAnalysis asa = new AndroidStringAnalysis(ra);
			asa.setupAndroidLibs(LocalFileReader.androidJar(Shell.walaProperties).getPath());
			asa.setExclusion(CallGraphTestUtil.REGRESSION_EXCLUSIONS);
			asa.addAnalysisScope(targetPath);
			List<Hotspot> hotspots = new ArrayList<Hotspot>();
			hotspots.add(new ArgumentHotspot(ClassLoaderReference.Application, "android/webkit/WebView", "loadUrl(Ljava/lang/String;)V", 0));
//			hotspots.add(new ArgumentHotspot(ClassLoaderReference.Application, "android/webkit/WebView", "loadData(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", 0));
//			hotspots.add(new ArgumentHotspot(ClassLoaderReference.Application, "android/webkit/WebView", "loadDataWithBaseURL(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", 0));
//			hotspots.add(new ArgumentHotspot(ClassLoaderReference.Application, "android/webkit/WebView", "loadDataWithBaseURL(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", 1));
//			hotspots.add(new ArgumentHotspot(ClassLoaderReference.Application, "android/webkit/WebView", "loadDataWithBaseURL(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", 2));
//			hotspots.add(new ArgumentHotspot(ClassLoaderReference.Application, "android/webkit/WebView", "loadDataWithBaseURL(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", 3));
			asa.analyze(hotspots);		
			
			//make javascript code as seperate js file
			int i = 1;
			for(HotspotDescriptor hd : asa.getAllDescriptors()){
				System.out.println(hd);
				Set<String> vs = hd.getValues();
				Map<String, String> chMap = new HashMap<String, String>();
				
				for(String v : vs){
					if(v.startsWith("javascript:")){ // if it is javascript code, then
						File js = FileWriter.makeFile(dirPath, "js_" + (i++) + ".js", v.substring(v.indexOf(":")+1));
						jsFiles.add(js.toURI().toURL());
						chMap.put(v, js.getCanonicalPath()); 
					}else if(v.startsWith("http")){ // if it is online html file, then
						URL url = new URL(v);
						jsFiles.add(new URL(v));
						chMap.put(v, url.getFile());
					}else if(v.startsWith("file:///")){
						String nPath = dirPath + File.separator + v.replace("file:///", "").replace("android_asset", "assets");
						if(htmlToJsMap.containsKey(new File(nPath))){
							chMap.put(v, htmlToJsMap.get(new File(nPath)).getFile());
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
					target, cArgs.has(CommandArguments.DROIDEL_ARG), jsFiles);
			
			// Using manifest analysis? Not give any improvement now.
			if (cArgs.has(CommandArguments.MANIFEST_ARG)) {
				XMLManifestReader mr = new XMLManifestReader(targetPath);
				System.out.println(mr.rootProperty());
			}

			HybridCFGAnalysis cfgAnalysis = new HybridCFGAnalysis();
			cfgAnalysis.main(scopeBuilder.makeScope(), asa, ra);
		} else {
			// TODO: support several functions
		}
	}

	/**
	 * Read the target file from the disk.
	 * 
	 * @param target
	 *            the path that indicates the target file.
	 * @return target file for analysis.
	 * @throws Invalid
	 *             the file is not apk file.
	 */
	private static File getTargetFile(String target) throws Invalid {
		if (!target.endsWith(".apk"))
			throw new Invalid("target file must be 'apk' file. TARGET: "
					+ target);

		return new File(target);
	}

	/**
	 * For multiple file analysis. Not support now.
	 * 
	 * @param target
	 *            the directory path that includes the target files.
	 * @return list of target files.
	 */
	private static List<File> getTargetFiles(String target) {
		File targetFile = new File(target);
		List<File> fileList = new ArrayList<File>();

		if (targetFile.isDirectory()) {
			File[] tmpList = targetFile.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					// TODO Auto-generated method stub
					if (name.endsWith(".apk"))
						return true;
					else
						return false;
				}
			});
			for (File f : tmpList)
				fileList.add(f);
		} else {
			fileList.add(targetFile);
		}
		return fileList;
	}
}
