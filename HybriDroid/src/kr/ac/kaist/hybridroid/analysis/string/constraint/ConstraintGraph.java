package kr.ac.kaist.hybridroid.analysis.string.constraint;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.util.collections.Pair;

public class ConstraintGraph implements Iterable<ConstraintNode>{
	private int nodeNum = 1;
	private int edgeNum = 1;
	private Map<ConstraintNode, Integer> nodeIndexMap;
	private Map<Integer, ConstraintNode> indexNodeMap;
	private Map<ConstraintEdge, Integer> edgeIndexMap;
	private Map<Integer, ConstraintEdge> indexEdgeMap;
	private Map<Integer, Set<Integer>> inEdges;
	private Map<Integer, Set<Integer>> outEdges; 
	private Set<Pair<Integer, Integer>> relations;
	
	public ConstraintGraph(){
		nodeIndexMap = new HashMap<ConstraintNode, Integer>();
		edgeIndexMap = new HashMap<ConstraintEdge, Integer>();
		indexNodeMap = new HashMap<Integer, ConstraintNode>();
		indexEdgeMap = new HashMap<Integer, ConstraintEdge>();
		inEdges = new HashMap<Integer, Set<Integer>>();
		outEdges = new HashMap<Integer, Set<Integer>>();
		relations = new HashSet<Pair<Integer, Integer>>();
	}
		
	public Set<ConstraintEdge> getOutEdges(ConstraintNode node){
		Set<ConstraintEdge> edges = new HashSet<ConstraintEdge>();
		int nodeIndex = nodeIndexMap.get(node);
		
		if(!outEdges.containsKey(nodeIndex))
			return Collections.emptySet();
		
		for(int edgeIndex : outEdges.get(nodeIndex)){
			edges.add(indexEdgeMap.get(edgeIndex));
		}
		return edges;
	}
	
	public Set<ConstraintEdge> getInEdges(ConstraintNode node){
		Set<ConstraintEdge> edges = new HashSet<ConstraintEdge>();
		int nodeIndex = nodeIndexMap.get(node);
		
		if(!inEdges.containsKey(nodeIndex))
			return Collections.emptySet();
		
		for(int edgeIndex : inEdges.get(nodeIndex)){
			edges.add(indexEdgeMap.get(edgeIndex));
		}
		return edges;
	}
	
	public boolean addEdge(ConstraintNode op, ConstraintNode to, ConstraintNode... froms){
		
		for(ConstraintNode from : froms)
			if(!nodeIndexMap.containsKey(from)){
				nodeIndexMap.put(from, nodeNum);
				indexNodeMap.put(nodeNum++, from);
			}
		
		if(!nodeIndexMap.containsKey(to)){
			nodeIndexMap.put(to, nodeNum);
			indexNodeMap.put(nodeNum++, to);
		}
		
		if(op instanceof AssignOpNode){
			System.out.println("ALREADY? " + nodeIndexMap.containsKey(op));
		}
		if(!nodeIndexMap.containsKey(op)){
			nodeIndexMap.put(op, nodeNum);
			indexNodeMap.put(nodeNum++, op);
		}
		
		int[] fromIndexes = new int[froms.length];
		for(int i=0; i<froms.length; i++)
			fromIndexes[i] = nodeIndexMap.get(froms[i]);
		
		int toIndex = nodeIndexMap.get(to);
		int opIndex = nodeIndexMap.get(op);
		
		//In SSA form, the value of a box cannot be decided by multiple variables in a method. 
		for(int from : fromIndexes){
			if(relations.contains(Pair.make(from, toIndex))){
				return false;
			}
		}
		
		int opToEdge = edgeNum++;
		ConstraintEdge edge = new PropagationEdge(op, to); 
		edgeIndexMap.put(edge, opToEdge);
		indexEdgeMap.put(opToEdge, edge);
		
		addEdge(opIndex, toIndex, opToEdge);
		
		boolean ordered = (fromIndexes.length > 1)? true : false;
		
		int order = 0;
		for(int i=0; i<froms.length; i++){
			ConstraintNode from = froms[i];
			int fromIndex = fromIndexes[i];
			order++;
			int fromOpEdgeNum = edgeNum++;
			if(ordered){
				ConstraintEdge fromOpEdge = new OrderedEdge(from, op, order);
				edgeIndexMap.put(fromOpEdge, fromOpEdgeNum);
				indexEdgeMap.put(fromOpEdgeNum, fromOpEdge);
			}else{
				ConstraintEdge fromOpEdge = new PropagationEdge(from, op);
				edgeIndexMap.put(fromOpEdge, fromOpEdgeNum);
				indexEdgeMap.put(fromOpEdgeNum, fromOpEdge);
			}
			addEdge(fromIndex, opIndex, fromOpEdgeNum);
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

	@Override
	public Iterator<ConstraintNode> iterator() {
		// TODO Auto-generated method stub
		return nodeIndexMap.keySet().iterator();
	}
}
