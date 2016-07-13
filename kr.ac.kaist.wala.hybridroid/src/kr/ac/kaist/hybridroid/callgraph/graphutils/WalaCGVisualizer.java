/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package kr.ac.kaist.hybridroid.callgraph.graphutils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import kr.ac.kaist.hybridroid.util.graph.visualize.Visualizer;
import kr.ac.kaist.hybridroid.util.graph.visualize.Visualizer.GraphType;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.ClassLoaderReference;

public class WalaCGVisualizer {
	
	private Map<CGNode, String> labelMap;
	
	public WalaCGVisualizer(){
		labelMap = new HashMap<CGNode, String>();
	}
	
	private boolean isAndroidLibrary(CGNode n){

		if(n.getMethod().getDeclaringClass() != null && n.getMethod().getDeclaringClass().getReference().getName().getPackage() != null)
			if(n.getMethod().getDeclaringClass().getReference().getName().getPackage().toString().startsWith("android/support/") || n.getMethod().getDeclaringClass().getReference().getName().getPackage().toString().startsWith("com/google/"))
				return true;
		return false;
	}
	
	private boolean isFiltered(CGNode n){
		if(n.toString().contains("ctor") || n.toString().contains("preamble.js") || n.toString().contains("prologue.js") || n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial) || n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Extension))
			return true;
		return false;
	}
	
	private Set<CGNode> getNonFilteredNodes(CallGraph cg, CGNode n){
		Set<CGNode> s = new HashSet<CGNode>();
		
		for(Iterator<CGNode> ipred = cg.getPredNodes(n); ipred.hasNext(); ){
			CGNode pred = ipred.next();
			if(isFiltered(pred)){
				s.addAll(getNonFilteredNodes(cg, pred));
			}else{
				s.add(pred);
			}
		}
		return s;
	}
	
	public File visualize(CallGraph cg, String out, Set<CGNode> s){
	Visualizer vis = Visualizer.getInstance();
		
		vis.clear();
		vis.setType(GraphType.Digraph);
		
		for(CGNode node : cg){
			if(!isFiltered(node)){
				Iterator<CGNode> iPredNodes = cg.getPredNodes(node);
				while(iPredNodes.hasNext()){
					CGNode predNode = iPredNodes.next();
					if(isFiltered(predNode)){
						for(CGNode n : getNonFilteredNodes(cg, predNode)){
							vis.fromAtoB(getLabel(cg, n), getLabel(cg, node));
						}
					}else
						vis.fromAtoB(getLabel(cg, predNode), getLabel(cg, node));
				}
			}
		}
		vis.printGraph(out);
		File outFile = new File(out);
		return outFile;
	}
	
	public File visualize(CallGraph cg, String out){
		Visualizer vis = Visualizer.getInstance();
		
		vis.clear();
		vis.setType(GraphType.Digraph);
		
		for(CGNode node : cg){
			if(!isFiltered(node)){
				Iterator<CGNode> iPredNodes = cg.getPredNodes(node);
				while(iPredNodes.hasNext()){
					CGNode predNode = iPredNodes.next();
					if(isFiltered(predNode)){
						for(CGNode n : getNonFilteredNodes(cg, predNode)){
							vis.fromAtoB(getLabel(cg, n), getLabel(cg, node));
						}
					}else
						vis.fromAtoB(getLabel(cg, predNode), getLabel(cg, node));
				}
			}
		}
		vis.printGraph(out);
		File outFile = new File(out);
		return outFile;
	}
	
	private int index = 1;
	
	private String getLabel(CallGraph cg, CGNode node){
		String label = "";
		
		if(labelMap.containsKey(node))
			return labelMap.get(node);
		
		label += node.toString() + "\\l";
//		label = (index++) + "";
		IR ir = node.getIR();
		
		if(ir == null){
			label += "null";
		}else{
			for(SSAInstruction inst : ir.getInstructions()){
				if(inst != null){
					label += "(" + inst.iindex + ") " + inst + "\\l";
				}
			}
		}
		
		label += "<Successor Nodes>\\l";
		for(Iterator<CGNode> in = cg.getSuccNodes(node); in.hasNext(); ){
			CGNode n = in.next();
			label += n + "\\l";
		}
		
		labelMap.put(node, label);
		return label;
	}
	
	public void printLabel(String out){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(out)));
			for(Entry<CGNode, String> e : labelMap.entrySet()){
				bw.write("[" + e.getValue() + "]" + " -> " + e.getKey() + "\n");
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public File transforms(File f, String tname) throws IOException{
		String dir = f.getCanonicalPath().substring(0, f.getCanonicalPath().lastIndexOf("/"));
		ProcessBuilder pb = new ProcessBuilder("/usr/local/bin/dot","-Tsvg","-o", tname, "-v", f.getCanonicalPath());
		pb.directory(new File(dir));
		Map<String,String> envs = pb.environment();
//		envs.put("PATH", envs.get("PATH") + ":" + "/usr/local/bin/");
		System.err.println("#translating " + f.getCanonicalPath() + " to " + dir + File.separator + tname);
		try {
			Process tr = pb.start();
			int result = tr.waitFor();
//			System.err.println("\tTranslation result: " + result);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new File(dir + File.separator + tname);
	}
}
