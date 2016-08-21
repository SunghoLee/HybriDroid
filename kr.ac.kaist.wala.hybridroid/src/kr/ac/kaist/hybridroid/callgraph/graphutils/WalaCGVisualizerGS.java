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
package kr.ac.kaist.hybridroid.callgraph.graphutils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import kr.ac.kaist.hybridroid.util.graph.visualize.VisualizerGS;
import kr.ac.kaist.hybridroid.util.graph.visualize.VisualizerGS.GraphTypeGS;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;

public class WalaCGVisualizerGS{
	
	private Map<CGNode, String> labelMap;
	
	public WalaCGVisualizerGS(){
		labelMap = new HashMap<CGNode, String>();
	}
	
	public File visualize(CallGraph cg, String out){
		VisualizerGS vis = VisualizerGS.getInstance();
		vis.clear();
		vis.setType(GraphTypeGS.Digraph);
		
		for(CGNode node : cg){
			Iterator<CGNode> iPredNodes = cg.getPredNodes(node);
			while(iPredNodes.hasNext()){
				CGNode predNode = iPredNodes.next();
				vis.fromAtoB(getLabel(predNode), getLabel(node));
			}
		}
		vis.printGraph(out);
		File outFile = new File(out);
		return outFile;
	}
	
	private int index = 1;
	
	private String getLabel(CGNode node){
		String label = "";
		
		if(labelMap.containsKey(node))
			return labelMap.get(node);
		
//		label += node.toString() + "\\l";
		label = (index++) + "";
//		IR ir = node.getIR();
//		
//		if(ir == null){
//			label += "null";
//		}else{
//			for(SSAInstruction inst : ir.getInstructions()){
//				if(inst != null){
//					label += "[" + inst.iindex + "] " + inst + "\\l";
//				}
//			}
//		}
		
		labelMap.put(node, label);
		return label;
	}
	
	public void printLabel(String out){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(out)));
			for(Entry<CGNode, String> e : labelMap.entrySet()){
				bw.write(e.getValue() + " -> " + e.getKey() + "\n");
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
