package kr.ac.kaist.hybridroid.callgraph.graphutils;

import java.io.File;

import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IConstraintEdge;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IConstraintNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.OrderedEdge;
import kr.ac.kaist.hybridroid.util.graph.visuailize.VisualizerGS.BoxColor;
import kr.ac.kaist.hybridroid.util.graph.visuailize.VisualizerGS.BoxType;
import kr.ac.kaist.hybridroid.util.graph.visuailize.VisualizerGS.GraphType;
import kr.ac.kaist.hybridroid.util.graph.visuailize.VisualizerGS;

public class ConstraintGraphVisualizer {
	public ConstraintGraphVisualizer(){
	}
	
	public File visualize(ConstraintGraph graph, String out, IBox... spots){
		VisualizerGS vis = VisualizerGS.getInstance();
		vis.clear();
		vis.setType(GraphType.Digraph);
		
		for(IBox spot : spots)
			vis.setColor(spot, BoxColor.RED);
		
		for(IConstraintNode from : graph){
			if(from instanceof IBox){
				vis.setShape(from, BoxType.RECT);
			}else{
				vis.setShape(from, BoxType.CIRCLE);
			}
			for(IConstraintEdge outEdge : graph.getOutEdges(from)){
				IConstraintNode to = outEdge.to();
				
				if(to instanceof IBox){
					vis.setShape(to, BoxType.RECT);
				}else{
					vis.setShape(to, BoxType.CIRCLE);
				}
				
				if(outEdge instanceof OrderedEdge)
					vis.fromAtoB(from, to, ((OrderedEdge)outEdge).getOrder() + "");
				else
					vis.fromAtoB(from, to);
			}
		}
		vis.printGraph(out);
		File outFile = new File(out);
		return outFile;
	}
}
