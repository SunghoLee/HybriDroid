package kr.ac.kaist.hybridroid.callgraph.graphutils;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import kr.ac.kaist.hybridroid.util.graph.visuailize.Visualizer;
import kr.ac.kaist.hybridroid.util.graph.visuailize.Visualizer.GraphType;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;

public class WalaCGVisualizer {
	
	private Map<CGNode, String> labelMap;
	
	public WalaCGVisualizer(){
		labelMap = new HashMap<CGNode, String>();
	}
	
	public File visualize(CallGraph cg, String out){
		Visualizer vis = Visualizer.getInstance();
		vis.clear();
		vis.setType(GraphType.Digraph);
		
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
	
	private String getLabel(CGNode node){
		String label = "";
		
		if(labelMap.containsKey(node))
			return labelMap.get(node);
		
		label += node.toString() + "\\l";
		IR ir = node.getIR();
		
		if(ir == null){
			label += "null";
		}else{
			for(SSAInstruction inst : ir.getInstructions()){
				if(inst != null){
					label += "[" + inst.iindex + "] " + inst + "\\l";
				}
			}
		}
		
		labelMap.put(node, label);
		return label;
	}
}
