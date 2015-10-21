package kr.ac.kaist.hybridroid.util.graph.visuailize;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kr.ac.kaist.hybridroid.util.data.Pair;

public class Visualizer {
	static private Visualizer instance;
	private Map<Object, Integer> indexMap;
	private Map<Integer, Set<Integer>> fromTo;
	private GraphType type;
	private int nodeIndex = 1;
	private Map<Pair<Integer, Integer>, String> edgeLabelMap;
	private Map<Integer, String> shapeMap;
	private Map<Integer, String> colorMap;
	
	public enum GraphType{
		Digraph,
		Undigraph
	}
	
	public enum BoxType{
		RECT,
		CIRCLE
	}
	
	public enum BoxColor{
		BLACK,
		RED,
		BLUE
	}
	
	static public Visualizer getInstance(){
		if(instance == null)
			instance = new Visualizer();
		return instance;
	}
	
	private Visualizer(){
		indexMap = new HashMap<Object, Integer>();
		fromTo = new HashMap<Integer, Set<Integer>>();
		edgeLabelMap = new HashMap<Pair<Integer, Integer>, String>();
		shapeMap = new HashMap<Integer, String>();
		colorMap = new HashMap<Integer, String>();
	}
	
	public void setShape(Object node, BoxType shape){
		int nodeIndex;
		
		if(hasIndex(node))
			nodeIndex = getIndex(node);
		else
			nodeIndex = setNewIndex(node);
		
		switch(shape){
		case RECT:
			shapeMap.put(nodeIndex, "box");
			break;
		case CIRCLE:
			shapeMap.put(nodeIndex, "circle");
			break;
		}
	}
	
	public void setColor(Object node, BoxColor color){
		int nodeIndex;
		
		if(hasIndex(node))
			nodeIndex = getIndex(node);
		else
			nodeIndex = setNewIndex(node);
		
		switch(color){
		case BLACK:
			break;
		case RED:
			colorMap.put(nodeIndex, "red");
			break;
		case BLUE:
			colorMap.put(nodeIndex, "blue");
			break;
		}
	}
	
	public void fromAtoB(Object a, Object b){
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
	
	public void fromAtoB(Object a, Object b, String label){
		fromAtoB(a, b);
		int aIndex = getIndex(a);
		int bIndex = getIndex(b);
		edgeLabelMap.put((Pair<Integer, Integer>)Pair.make(aIndex, bIndex), label);
	}
	
	public void clear(){
		indexMap.clear();
		fromTo.clear();
		shapeMap.clear();
		edgeLabelMap.clear();
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
			for(Object node : indexMap.keySet()){
				int index = indexMap.get(node);
				bw.write(index + " [label=\"" + node + "\", shape=" + getShape(index) + ", color=" + getColor(index) + "]\n");
			}
			
			//print all edges
			for(int from : fromTo.keySet()){
				Set<Integer> toSet = fromTo.get(from);
				for(int to : toSet){
					bw.write(from + " " + edge + " " + to + ((hasLabel(from, to))? " [label=\"" + getLabel(from, to) + "\"]": "") + "\n");
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
	
	private int setNewIndex(Object node){
		int newIndex = newIndex();
		indexMap.put(node, newIndex);
		return newIndex;
	}
	
	private int getIndex(Object node){
		if(hasIndex(node))
			return indexMap.get(node);
		throw new InternalError("the node has no index: " + node);
	}
	
	private boolean hasIndex(Object node){
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
	
	private String getShape(int nodeIndex){
		if(shapeMap.containsKey(nodeIndex))
			return shapeMap.get(nodeIndex);
		return "box";
	}
	
	private boolean hasLabel(int from, int to){
		return edgeLabelMap.containsKey(Pair.make(from, to));
	}
	
	private String getLabel(int from, int to){
		return edgeLabelMap.get(Pair.make(from, to));
	}
	
	private String getColor(int nodeIndex){
		if(colorMap.containsKey(nodeIndex))
			return colorMap.get(nodeIndex);
		return "black";
	}
}
