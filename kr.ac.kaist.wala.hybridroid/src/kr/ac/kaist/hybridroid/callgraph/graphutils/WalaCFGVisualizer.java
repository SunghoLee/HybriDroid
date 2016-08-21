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

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import kr.ac.kaist.hybridroid.util.graph.visualize.Visualizer;
import kr.ac.kaist.hybridroid.util.graph.visualize.Visualizer.GraphType;

import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAInstruction;

public class WalaCFGVisualizer {
	
	private Map<ISSABasicBlock, String> labelMap;
	
	public WalaCFGVisualizer(){
		labelMap = new HashMap<ISSABasicBlock, String>();
	}
	
	public File visualize(SSACFG cfg, String out){
		Visualizer vis = Visualizer.getInstance();
		vis.clear();
		vis.setType(GraphType.Digraph);
		
		for(ISSABasicBlock node : cfg){
			for(ISSABasicBlock predNode : cfg.getNormalPredecessors(node)){
				vis.fromAtoB(getLabel(cfg, predNode), getLabel(cfg, node));
			}
		}
		vis.printGraph(out);
		File outFile = new File(out);
		return outFile;
	}
	
	private String getLabel(SSACFG cfg, ISSABasicBlock node){
		String label = "";
		
		if(labelMap.containsKey(node))
			return labelMap.get(node);
		
		label += node.toString() + "\\l";
		
		if(node.isEntryBlock())
			label += "entry\\l";
		if(node.isExitBlock())
			label += "exit\\l";
		else{
			int firstIndex = node.getFirstInstructionIndex();
			SSAInstruction firstInst = null;
			if(!(firstIndex < 0))
				firstInst = cfg.getInstructions()[firstIndex];
			
			int lastIndex = node.getLastInstructionIndex();
			
			SSAInstruction lastInst = null;
			if(!(lastIndex < 0))
				lastInst = node.getLastInstruction();
			
			label += "[" + firstIndex + "] " + firstInst + "\\l";
			label += "...\\l";
			label += "[" + lastIndex + "] " + lastInst + "\\l";
		}
		labelMap.put(node, label);
		return label;
	}
}
