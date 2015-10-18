package kr.ac.kaist.hybridroid.callgraph.graphutils;

import java.io.File;
import java.util.Iterator;

import kr.ac.kaist.hybridroid.util.graph.visuailize.Visualizer;
import kr.ac.kaist.hybridroid.util.graph.visuailize.Visualizer.GraphType;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;

public class WalaCGVisualizer {
	public WalaCGVisualizer(){
		
	}
	
	public File visualize(CallGraph cg, String out){
		Visualizer vis = Visualizer.getInstance();
		vis.setType(GraphType.Digraph);
		
		for(CGNode node : cg){
			Iterator<CGNode> iPredNodes = cg.getPredNodes(node);
			while(iPredNodes.hasNext()){
				CGNode predNode = iPredNodes.next();
				vis.fromAtoB(predNode.toString(), node.toString());
			}
		}
		vis.printGraph(out);
		File outFile = new File(out);
		return outFile;
	}
}
