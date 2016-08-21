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
package kr.ac.kaist.hybridroid.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.ibm.wala.cast.js.types.JavaScriptTypes;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.types.ClassLoaderReference;

public class VisualizeCGTest {
	private static String DOT_PATH;
	
	static{
		if (System.getProperty("os.name").matches(".*Mac.*"))
			DOT_PATH = "/usr/local/bin/dot";
		else if (System.getProperty("os.name").contains("Windows"))
			DOT_PATH = "\"C:\\Program Files (x86)\\Graphviz2.38\\bin\\dot.exe\"";
	}
	
	private static boolean fromLoader(IClass declClass, ClassLoaderReference clr) {
		ClassLoaderReference nodeClRef = declClass.getClassLoader()
				.getReference();

		return nodeClRef.equals(clr);
    }
	
	private static boolean isVisualizable(CGNode _node){
		if (!_node.toString().contains("preamble.js")
				&& !_node.toString().contains("prologue.js")) {
//			if(fromLoader(_node.getMethod().getDeclaringClass(), ClassLoaderReference.Primordial))
//				return false;
//			if(fromLoader(_node.getMethod().getDeclaringClass(), ClassLoaderReference.Extension))
//				return false;
//			else
//				return true;
			if(fromLoader(_node.getMethod().getDeclaringClass(), JavaScriptTypes.jsLoader))
				return true;
			else if(fromLoader(_node.getMethod().getDeclaringClass(), ClassLoaderReference.Primordial)){
				if(_node.toString().contains("setWebView") || _node.toString().contains("addJavascriptInterface"))
					return true;
				return false;
			}
			else if(fromLoader(_node.getMethod().getDeclaringClass(), ClassLoaderReference.Extension))
				return false;
			else if(fromLoader(_node.getMethod().getDeclaringClass(), ClassLoaderReference.Application))
				return true;
			else
				return false;
		}
		return false;
		// return true;
	}
	
	private static boolean isContentPrintable(CGNode _node){
		if (!_node.toString().contains("preamble.js")
				&& !_node.toString().contains("prologue.js")) {
			if(fromLoader(_node.getMethod().getDeclaringClass(), JavaScriptTypes.jsLoader))
				return true;
			else if(fromLoader(_node.getMethod().getDeclaringClass(), ClassLoaderReference.Primordial)){
				if(_node.toString().contains("setWebView") || _node.toString().contains("addJavascriptInterface"))
					return true;
				return false;
			}
//			if(fromLoader(_node.getMethod().getDeclaringClass(), ClassLoaderReference.Primordial))
//				return false;
//			if(fromLoader(_node.getMethod().getDeclaringClass(), ClassLoaderReference.Extension))
//				return false;
			else if(fromLoader(_node.getMethod().getDeclaringClass(), ClassLoaderReference.Application))
				return true;
			else
				return false;

		}
		return false;
	}
	
	private static void assignNodeName(CallGraph cg, FileOutputStream outStream) throws IOException{
		for (CGNode node : cg) {
			if (isVisualizable(node)) {
				String irs = "";
				if (node.getIR() != null)
					irs = node.getIR().toString();
				int num = 0;
				String before = irs;
				while (irs.indexOf("\n\n") > 0) {
					irs = irs.replace("\n\n", "\n");
					num++;
				}
				irs = irs.replace("\n", "\\l");
				irs += "\n\n";
				irs += "<SuccessNodes: " + cg.getSuccNodeCount(node) + ">\\l";
				Iterator<CGNode> iSuccNode = cg.getSuccNodes(node);
				while (iSuccNode.hasNext()) {
					CGNode succNode = iSuccNode.next();
					irs += succNode.toString();
					if (iSuccNode.hasNext())
						irs += "\\l";
				}
				// irs = irs.replace("@", "(at)");
				if (isContentPrintable(node))
					outStream.write((cg.getNumber(node) + " [label=\"" + node
							+ "\n" + irs + "\" shape=box]\n").getBytes());
				else
					outStream
							.write((cg.getNumber(node) + " [label=\"" + node + "\" shape=box]\n")
									.getBytes());
			}
		}
	}
	
	public static void visualizeCallGraph(CallGraph cg, String _outputFileName, boolean convert){
		File folder = new File("cfg");
		File visOut = new File("cfg/" + _outputFileName + ".dot");
		
		if(folder.exists())
			folder.delete();
		folder.mkdirs();
		
		try {
			visOut.createNewFile();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		System.out.println("#making " + _outputFileName + ".dot...");
		try {
			FileOutputStream outStream = new FileOutputStream(visOut);
			outStream.write(("digraph callgraph{\n").getBytes());

			assignNodeName(cg, outStream);

			for (CGNode node : cg) {
				if (isVisualizable(node))
					for (Iterator<CGNode> succI = cg.getSuccNodes(node); succI
							.hasNext();) {
						CGNode succNode = succI.next();
						if (isVisualizable(succNode)) {
							outStream
									.write((cg.getNumber(node) + " -> "
											+ cg.getNumber(succNode) + "\n")
											.getBytes());
						}
					}
			}
			outStream.write(("}").getBytes());
			outStream.close();
			System.out.println("\tdone.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		visOut.setReadable(true);
		
		if (convert) {
			String filepath = "";
			String dirpath = "";
			try {
				dirpath = folder.getCanonicalPath();
				filepath = visOut.getCanonicalPath();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			ProcessBuilder pb = new ProcessBuilder(DOT_PATH,"-Tsvg","-o",dirpath + File.separator + _outputFileName + ".svg", "-v", filepath);
			pb.directory(folder);
			Map<String,String> envs = pb.environment();
//			envs.put("PATH", envs.get("PATH") + ":" + "/usr/local/bin/");
			System.err.println("#translating to " + dirpath + File.separator + _outputFileName + ".svg");
			try {
				Process tr = pb.start();
				int result = tr.waitFor();
				System.err.println("\tTranslation result: " + result);
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("\tdone.");
		}
	}
}
