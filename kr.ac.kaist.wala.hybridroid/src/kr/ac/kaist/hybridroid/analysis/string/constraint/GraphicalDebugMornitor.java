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
package kr.ac.kaist.hybridroid.analysis.string.constraint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import kr.ac.kaist.hybridroid.callgraph.graphutils.ConstraintGraphVisualizer;
import kr.ac.kaist.hybridroid.callgraph.graphutils.ConstraintGraphVisualizerGS;

public class GraphicalDebugMornitor implements IConstraintMonitor {
	private ConstraintGraphVisualizerGS cgvis;
	private IBox[] hotspots;
	public GraphicalDebugMornitor(){
		cgvis = new ConstraintGraphVisualizerGS();
	}
	
	public GraphicalDebugMornitor(IBox... hotspots){
		this();
		this.hotspots = hotspots;
	}
	
	@Override
	public void monitor(int iter, ConstraintGraph graph, IBox b, Set<IBox> boxes) {
		// TODO Auto-generated method stub
		cgvis.visualize(graph, "out", hotspots);
		if(iter == 1)
			cgvis.display();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		String s;
		try {
			while((s = br.readLine()) != null){
				if(!execCmd(s, graph))break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean execCmd(String cmd, ConstraintGraph graph){
		switch(cmd){
		case "":
			cgvis.visualize(graph, "ttt", hotspots);
			return false;
		}
		return true;
	}
}
