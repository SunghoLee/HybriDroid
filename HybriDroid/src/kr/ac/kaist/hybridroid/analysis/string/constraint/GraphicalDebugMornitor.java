package kr.ac.kaist.hybridroid.analysis.string.constraint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import kr.ac.kaist.hybridroid.callgraph.graphutils.ConstraintGraphVisualizer;

public class GraphicalDebugMornitor implements IConstraintMonitor {
	private ConstraintGraphVisualizer cgvis;
	private IBox[] hotspots;
	public GraphicalDebugMornitor(){
		cgvis = new ConstraintGraphVisualizer();
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
