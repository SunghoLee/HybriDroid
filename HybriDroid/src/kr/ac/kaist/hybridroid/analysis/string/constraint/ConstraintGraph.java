package kr.ac.kaist.hybridroid.analysis.string.constraint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.util.collections.Pair;

public class ConstraintGraph {
	private int nodeNum = 1;
	private int edgeNum = 1;
	private Map<ConstraintNode, Integer> nodeIndexMap;
	private Map<ConstraintEdge, Integer> edgeIndexMap;
	private Map<Integer, Set<Integer>> inEdges;
	private Map<Integer, Set<Integer>> outEdges; 
	private Set<Pair<Integer, Integer>> relations;
	
	public ConstraintGraph(){
		nodeIndexMap = new HashMap<ConstraintNode, Integer>();
		edgeIndexMap = new HashMap<ConstraintEdge, Integer>();
		inEdges = new HashMap<Integer, Set<Integer>>();
		outEdges = new HashMap<Integer, Set<Integer>>();
		relations = new HashSet<Pair<Integer, Integer>>();
	}
		
	public boolean addEdge(ConstraintNode op, ConstraintNode to, ConstraintNode... froms){
		
		for(ConstraintNode from : froms)
			if(!nodeIndexMap.containsKey(from))
				nodeIndexMap.put(from, nodeNum++);
		
		if(!nodeIndexMap.containsKey(to)){
			nodeIndexMap.put(to, nodeNum++);
		}
		
		if(!nodeIndexMap.containsKey(op)){
			nodeIndexMap.put(op, nodeNum++);
		}

		int[] fromIndexes = new int[froms.length];
		for(int i=0; i<froms.length; i++)
			fromIndexes[i] = nodeIndexMap.get(froms[i]);
		
		int toIndex = nodeIndexMap.get(to);
		int opIndex = nodeIndexMap.get(op);
		
		//In SSA form, the value of a box cannot be decided by multiple variables in a method. 
		for(int from : fromIndexes){
			if(relations.contains(Pair.make(from, toIndex)))
				return false;
		}
		
		int opToEdge = edgeNum++;
		edgeIndexMap.put(new PropagationEdge(op, to), opToEdge);
		addEdge(opIndex, toIndex, opToEdge);
		
		boolean ordered = (fromIndexes.length > 1)? true : false;
		
		int order = 0;
		for(int i=0; i<froms.length; i++){
			ConstraintNode from = froms[i];
			int fromIndex = fromIndexes[i];
			order++;
			int fromOpEdge = edgeNum++;
			if(ordered){
				edgeIndexMap.put(new OrderedEdge(from, op, order), fromOpEdge);
			}else{
				edgeIndexMap.put(new PropagationEdge(from, op), fromOpEdge);
			}
			addEdge(fromIndex, opIndex, fromOpEdge);
			relations.add(Pair.make(fromIndex, toIndex));
		}
		
		return true;
	}		
	
	private void addEdge(int from, int to, int edge){
		if(!inEdges.containsKey(to)){
			inEdges.put(to, new HashSet<Integer>());
		}
		
		if(!outEdges.containsKey(from)){
			outEdges.put(from, new HashSet<Integer>());
		}
		
		inEdges.get(to).add(edge);
		outEdges.get(from).add(edge);
	}
}
