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

import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IConstraintEdge;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IConstraintNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.OrderedEdge;
import kr.ac.kaist.hybridroid.util.graph.visualize.VisualizerGS;
import kr.ac.kaist.hybridroid.util.graph.visualize.VisualizerGS.BoxColorGS;
import kr.ac.kaist.hybridroid.util.graph.visualize.VisualizerGS.BoxTypeGS;
import kr.ac.kaist.hybridroid.util.graph.visualize.VisualizerGS.GraphTypeGS;

public class ConstraintGraphVisualizerGS {
	private VisualizerGS vis;
	public ConstraintGraphVisualizerGS(){
	}
	
	public File visualize(ConstraintGraph graph, String out, IBox... spots){
		vis = VisualizerGS.getInstance();
		vis.clear();
		vis.setType(GraphTypeGS.Digraph);
		if(spots != null)
			for(IBox spot : spots)
				vis.setColor(spot, BoxColorGS.RED);
		
		for(IConstraintNode from : graph){
			if(from instanceof IBox){
				vis.setShape(from, BoxTypeGS.RECT);
			}else{
				vis.setShape(from, BoxTypeGS.CIRCLE);
			}
			for(IConstraintEdge outEdge : graph.getOutEdges(from)){
				IConstraintNode to = outEdge.to();
				
				if(to instanceof IBox){
					vis.setShape(to, BoxTypeGS.RECT);
				}else{
					vis.setShape(to, BoxTypeGS.CIRCLE);
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
	
	public void display(){
		vis.display();
	}
}
