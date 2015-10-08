package kr.ac.kaist.hybridroid.util.graph.visuailize;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Visualizer {
	static private Visualizer instance;
	private Map<String, Integer> indexMap;
	private Map<Integer, Set<Integer>> fromTo;
	private GraphType type;
	private int nodeIndex = 1;
	
	public enum GraphType{
		Digraph,
		Undigraph
	}
	
	static public Visualizer getInstance(){
		if(instance == null)
			instance = new Visualizer();
		return instance;
	}
	
	private Visualizer(){
		indexMap = new HashMap<String, Integer>();
		fromTo = new HashMap<Integer, Set<Integer>>();
	}
	
	public void fromAtoB(String a, String b){
		int aIndex;
		int bIndex;
		
		if(hasIndex(a))
			aIndex = getIndex(a);
		else
			aIndex = setNewIndex(a);
		if(hasIndex(b))
			bIndex = getIndex(b);
		else
			bIndex = setNewIndex(b);
		
		addEdge(aIndex, bIndex);
	}
	
	public void clear(){
		indexMap.clear();
		fromTo.clear();
		type = null;
		nodeIndex = 1;
	}
	
	public void setType(GraphType type){
		this.type = type;
	}
	
	public void printGraph(String out){
		String path = out;
		String edge = "";
		
		if(type == null)
			throw new InternalError("GraphType is not decided.");
		else
			switch(type){
			case Digraph:
				edge = "->";
				break;
			case Undigraph:
				edge = "--";
				break;
			}
			
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(path));
			//open the graph
			bw.write(typeToString(type)+"{\n");
			
			//print all node labels
			for(String node : indexMap.keySet()){
				int index = indexMap.get(node);
				bw.write(index + " [label=\"" + node + "\"]\n");
			}
			
			//print all edges
			for(int from : fromTo.keySet()){
				Set<Integer> toSet = fromTo.get(from);
				for(int to : toSet){
					bw.write(from + " " + edge + " " + to +"\n");
				}
			}
			
			//close the graph
			bw.write("}");
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		clear();
	}
	
	private void addEdge(int a, int b){
		if(!fromTo.containsKey(a))
			fromTo.put(a, new HashSet<Integer>());
		fromTo.get(a).add(b);
	}
	
	private int setNewIndex(String node){
		int newIndex = newIndex();
		indexMap.put(node, newIndex);
		return newIndex;
	}
	
	private int getIndex(String node){
		if(hasIndex(node))
			return indexMap.get(node);
		throw new InternalError("the node has no index: " + node);
	}
	
	private boolean hasIndex(String node){
		return indexMap.containsKey(node);
	}
	
	private String typeToString(GraphType type){
		switch(type){
		case Digraph:
			return "digraph";
		case Undigraph:
			return "graph";
		default:
		}
		throw new InternalError("Graph must be either Digraph or Undigraph.");
	}
	
	private int newIndex(){
		return nodeIndex++;
	}
}
