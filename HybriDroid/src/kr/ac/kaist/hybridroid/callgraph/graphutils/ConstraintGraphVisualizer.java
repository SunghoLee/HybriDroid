package kr.ac.kaist.hybridroid.callgraph.graphutils;

import java.io.File;

import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IConstraintEdge;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IConstraintNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.OrderedEdge;
import kr.ac.kaist.hybridroid.util.graph.visualize.Visualizer.GraphType;
import kr.ac.kaist.hybridroid.util.graph.visualize.Visualizer;
import kr.ac.kaist.hybridroid.util.graph.visualize.Visualizer.BoxColor;
import kr.ac.kaist.hybridroid.util.graph.visualize.Visualizer.BoxType;

public class ConstraintGraphVisualizer {
	private Visualizer vis;
	public ConstraintGraphVisualizer(){
	}
	
	public File visualize(ConstraintGraph graph, String out, IBox... spots){
		vis = Visualizer.getInstance();
		vis.clear();
		vis.setType(Visualizer.GraphType.Digraph);
		
		if(spots != null)
			for(IBox spot : spots)
				vis.setColor(spot, Visualizer.BoxColor.RED);
		
		for(IConstraintNode from : graph){
			if(from instanceof IBox){
				vis.setShape(from, Visualizer.BoxType.RECT);
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
