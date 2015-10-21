package kr.ac.kaist.hybridroid.soot.graphutils;

import java.io.File;

import kr.ac.kaist.hybridroid.util.graph.visuailize.Visualizer;
import kr.ac.kaist.hybridroid.util.graph.visuailize.Visualizer.GraphType;
import dk.brics.string.intermediate.Application;
import dk.brics.string.intermediate.Call;
import dk.brics.string.intermediate.Method;

public class SootGraphVisualizer {
	public SootGraphVisualizer(){
		
	}
	
	public File visualize(Application app, String out){
		Visualizer vis = Visualizer.getInstance();
		vis.clear();
		vis.setType(GraphType.Digraph);
		
		for(Method m : app.getMethods()){
			for(Call call : m.getCallSites()){
				Method from = call.getMethod();
				vis.fromAtoB(from.getName(), m.getName());
			}
		}
		vis.printGraph(out);
		File outFile = new File(out);
		return outFile;
	}
}
