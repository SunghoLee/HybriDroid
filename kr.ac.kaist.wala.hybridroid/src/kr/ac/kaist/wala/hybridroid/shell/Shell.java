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
package kr.ac.kaist.wala.hybridroid.shell;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.Pair;
import kr.ac.kaist.wala.hybridroid.analysis.HybridCFGAnalysis;
import kr.ac.kaist.wala.hybridroid.command.CommandArguments;
import kr.ac.kaist.wala.hybridroid.test.PrivateLeakageDetector;
import kr.ac.kaist.wala.hybridroid.utils.LocalFileReader;
import org.apache.commons.cli.ParseException;
import org.omg.CORBA.DynAnyPackage.Invalid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * HybriDroid is a Android hybrid application analysis framework based-on WALA.
 * Now, HybriDroid supports bridge communication only. It includes bug detection
 * and taint analysis modules. This process may use DROIDEL project as
 * front-end, to improve analysis accuracy and model some Android framework API.
 * 
 * @author Sungho Lee
 */
public class Shell {
	public static CommandArguments args;
	public static Properties walaProperties;
	public static long START;
	public static long END;
	/**
	 * HybriDroid main function. Now, There is CFG-building option only in
	 * HybriDroid.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws CancelException
	 * @throws ParseException
	 * @throws Invalid
	 * @throws WalaException 
	 */
	public static void main(String[] args) throws IOException,
			IllegalArgumentException, CancelException,
			ParseException, Invalid, WalaException {
			Shell.args = new CommandArguments(args);
		// Load wala property. Now, 'PROP_ARG' is essential option, so else
		// branch cannot be reached.
		if (Shell.args.has(CommandArguments.PROP_ARG)) {
			String propertyfile = Shell.args.get(CommandArguments.PROP_ARG);
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
		String targetPath = Shell.args.get(CommandArguments.TARGET_ARG);
		long startTime = System.currentTimeMillis();
		START = startTime;
		/**
		 * Below is the switch case for HybriDroid functions. One function of
		 * the CommandLineOptionGroup must be one case in below.
		 */
		// Build Control-flow Graph.
		if (Shell.args.has(CommandArguments.CFG_ARG)) {
			if(Shell.args.has(CommandArguments.ONLY_JS_ARG)){
//				File analysisfile = new File(targetPath);
//					    URL url = analysisfile.toURI().toURL();
//					    // Setting WALA analyzer
//					    CAstRhinoTranslatorFactory translatorFactory = new CAstRhinoTranslatorFactory();
//					    JSCallGraphUtil.setTranslatorFactory(translatorFactory);
//					    // make actual file name and directory
//					    JSCFABuilder b = JSCallGraphBuilderUtil.makeHTMLCGBuilder(url);
//					    CallGraph callGraph = b.makeCallGraph(b.getOptions());
//					    PointerAnalysis<InstanceKey> pa = b.getPointerAnalysis();
//					    WalaCGVisualizer vis = new WalaCGVisualizer();
//					    vis.visualize(callGraph, "cfg.dot");
//					    vis.printLabel("jslabel.txt");
			}else{
				
//				Shell.START = System.currentTimeMillis();
				HybridCFGAnalysis cfgAnalysis = new HybridCFGAnalysis();
				Pair<CallGraph, PointerAnalysis<InstanceKey>> p = cfgAnalysis.main(targetPath, LocalFileReader.androidJar(Shell.walaProperties).getPath());
				CallGraph cg = p.fst;
				PointerAnalysis<InstanceKey> pa = p.snd;

//				PointerAnalysis<InstanceKey> pa = p.snd;
//				Shell.END = System.currentTimeMillis();
//				System.err.println("#time: " + (((double)(Shell.END - Shell.START))/1000d) + "s");

				System.err.println("Graph Modeling for taint...");
//				ModeledCallGraphForTaint mcg = new ModeledCallGraphForTaint(p.fst);
				System.err.println("Taint analysis...");
				PrivateLeakageDetector pld = new PrivateLeakageDetector(p.fst, p.snd);
				pld.analyze();
//				Shell.END = System.currentTimeMillis();
				System.err.println("#time: " + (((double)(Shell.END - Shell.START))/1000d) + "s");
				for(PrivateLeakageDetector.LeakWarning w : pld.getWarnings()){
					System.out.println("=========");
					System.out.println(w);
					System.out.println("=========");
//					w.printPathFlow("leak.dot");
				}
			}
		} else {
			// TODO: support several functions
		}
		long endTime = System.currentTimeMillis();
		System.out.println("#Time: " + (endTime - startTime));
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
