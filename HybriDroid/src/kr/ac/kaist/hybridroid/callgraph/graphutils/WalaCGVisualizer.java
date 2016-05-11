package kr.ac.kaist.hybridroid.callgraph.graphutils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;

import kr.ac.kaist.hybridroid.util.graph.visualize.Visualizer;
import kr.ac.kaist.hybridroid.util.graph.visualize.Visualizer.GraphType;

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
	
	public File visualize(CallGraph cg, String out, Set<CGNode> s){
	Visualizer vis = Visualizer.getInstance();
		
		vis.clear();
		vis.setType(GraphType.Digraph);
		
		for(CGNode node : cg){
			if(node.toString().contains("preamble.js") || node.toString().contains("prologue.js") || node.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial) || node.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Extension))
				continue;
			if(!s.contains(node) && isAndroidLibrary(node))
				continue;
			Iterator<CGNode> iPredNodes = cg.getPredNodes(node);
			while(iPredNodes.hasNext()){
				CGNode predNode = iPredNodes.next();
				if(predNode.toString().contains("preamble.js") || predNode.toString().contains("prologue.js") || node.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial) || node.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Extension))
					continue;
				vis.fromAtoB(getLabel(cg, predNode), getLabel(cg, node));
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
			if(node.toString().contains("preamble.js") || node.toString().contains("prologue.js") || node.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial) || node.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Extension))
				continue;
			Iterator<CGNode> iPredNodes = cg.getPredNodes(node);
			while(iPredNodes.hasNext()){
				CGNode predNode = iPredNodes.next();
				if(predNode.toString().contains("preamble.js") || predNode.toString().contains("prologue.js") || node.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial) || node.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Extension))
					continue;
				vis.fromAtoB(getLabel(cg, predNode), getLabel(cg, node));
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
}
