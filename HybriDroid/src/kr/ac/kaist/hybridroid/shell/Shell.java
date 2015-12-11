package kr.ac.kaist.hybridroid.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import kr.ac.kaist.hybridroid.analysis.AnalysisScopeBuilder;
import kr.ac.kaist.hybridroid.analysis.HybridCFGAnalysis;
import kr.ac.kaist.hybridroid.analysis.resource.AndroidResourceAnalysis;
import kr.ac.kaist.hybridroid.analysis.string.AndroidStringAnalysis;
import kr.ac.kaist.hybridroid.analysis.string.ArgumentHotspot;
import kr.ac.kaist.hybridroid.analysis.string.Hotspot;
import kr.ac.kaist.hybridroid.appinfo.XMLManifestReader;
import kr.ac.kaist.hybridroid.command.CommandArguments;
import kr.ac.kaist.hybridroid.util.files.LocalFileReader;
import kr.ac.kaist.hybridroid.util.timer.Timer;

import org.apache.commons.cli.ParseException;
import org.omg.CORBA.DynAnyPackage.Invalid;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

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
			
			AndroidStringAnalysis strAnalyzer = new AndroidStringAnalysis(ra);
			strAnalyzer.setupAndroidLibs(LocalFileReader.androidJar(Shell.walaProperties).getPath());
			strAnalyzer.setExclusion(CallGraphTestUtil.REGRESSION_EXCLUSIONS);
//			strAnalyzer.addAnalysisScope(LocalFileReader.androidJar(Shell.walaProperties).getPath());
			strAnalyzer.addAnalysisScope(targetPath);
			List<Hotspot> hotspots = new ArrayList<Hotspot>();
			hotspots.add(new ArgumentHotspot("loadUrl", 1, 0));
			Timer t = Timer.start();
			strAnalyzer.analyze(hotspots);
			System.out.println("END: " + (t.end()/1000.0));
//			System.exit(-1);
//			strAnal
//			AnalysisScopeBuilder scopeBuilder = AnalysisScopeBuilder.build(
//					target, cArgs.has(CommandArguments.DROIDEL_ARG));
//
//			// Using manifest analysis? Not give any improvement now.
//			if (cArgs.has(CommandArguments.MANIFEST_ARG)) {
//				XMLManifestReader mr = new XMLManifestReader(targetPath);
//				System.out.println(mr.rootProperty());
//			}
//
//			HybridCFGAnalysis cfgAnalysis = new HybridCFGAnalysis();
//			cfgAnalysis.main(scopeBuilder.makeScope());
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
