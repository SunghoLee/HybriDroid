package kr.ac.kaist.hybridroid.analysis.string.constraint;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.util.collections.Pair;

public class ConstraintGraph implements Iterable<IConstraintNode>{
	private int nodeNum = 1;
	private int edgeNum = 1;
	private Map<IConstraintNode, Integer> nodeIndexMap;
	private Map<Integer, IConstraintNode> indexNodeMap;
	private Map<IConstraintEdge, Integer> edgeIndexMap;
	private Map<Integer, IConstraintEdge> indexEdgeMap;
	private Map<Integer, Set<Integer>> inEdges;
	private Map<Integer, Set<Integer>> outEdges; 
	private Set<Pair<Integer, Integer>> relations;
	
	public ConstraintGraph(){
		nodeIndexMap = new HashMap<IConstraintNode, Integer>();
		edgeIndexMap = new HashMap<IConstraintEdge, Integer>();
		indexNodeMap = new HashMap<Integer, IConstraintNode>();
		indexEdgeMap = new HashMap<Integer, IConstraintEdge>();
		inEdges = new HashMap<Integer, Set<Integer>>();
		outEdges = new HashMap<Integer, Set<Integer>>();
		relations = new HashSet<Pair<Integer, Integer>>();
	}
		
	public int getIndex(IConstraintNode n){
		return nodeIndexMap.get(n);
	}
	public Set<IConstraintNode> getPredecessors(IConstraintNode n){
		Set<IConstraintEdge> edges = this.getInEdges(n);
		Set<IConstraintNode> predecessors = new HashSet<IConstraintNode>();
		for(IConstraintEdge e : edges)
			predecessors.add(e.from());
		return predecessors;
	}
	
	public Set<IConstraintNode> getSuccessors(IConstraintNode n){
		Set<IConstraintEdge> edges = this.getOutEdges(n);
		Set<IConstraintNode> successors = new HashSet<IConstraintNode>();
		for(IConstraintEdge e : edges)
			successors.add(e.to());
		return successors;
	}
	
	public IConstraintNode getNode(int index){
		return indexNodeMap.get(index);
	}
	
	public IConstraintEdge getEdge(int index){
		return indexEdgeMap.get(index);
	}
	
	public Set<IConstraintNode> getInnermostNodes(){
		Set<IConstraintNode> nodes = new HashSet<IConstraintNode>();
		for(IConstraintNode node : nodeIndexMap.keySet()){
			int index = nodeIndexMap.get(node);
			if(outEdges.containsKey(index) && !inEdges.containsKey(index))
				nodes.add(node);
		}
		return nodes;
	}
	
	public Set<IConstraintNode> getOuttermodeNodes(){
		Set<IConstraintNode> nodes = new HashSet<IConstraintNode>();
		for(IConstraintNode node : nodeIndexMap.keySet()){
			int index = nodeIndexMap.get(node);
			if(!outEdges.containsKey(index) && inEdges.containsKey(index))
				nodes.add(node);
		}
		return nodes;
	}
	
	public Set<IConstraintEdge> getOutEdges(IConstraintNode node){
		Set<IConstraintEdge> edges = new HashSet<IConstraintEdge>();
		if(!nodeIndexMap.containsKey(node))
			return Collections.emptySet();
		
		int nodeIndex = nodeIndexMap.get(node);
		
		if(!outEdges.containsKey(nodeIndex))
			return Collections.emptySet();
		
		for(int edgeIndex : outEdges.get(nodeIndex)){
			edges.add(indexEdgeMap.get(edgeIndex));
		}
		return edges;
	}
	
	public Set<IConstraintEdge> getInEdges(IConstraintNode node){
		Set<IConstraintEdge> edges = new HashSet<IConstraintEdge>();
		if(!nodeIndexMap.containsKey(node))
			return Collections.emptySet();
		
		int nodeIndex = nodeIndexMap.get(node);
		
		if(!inEdges.containsKey(nodeIndex))
			return Collections.emptySet();
		
		for(int edgeIndex : inEdges.get(nodeIndex)){
			edges.add(indexEdgeMap.get(edgeIndex));
		}
		return edges;
	}
	
	public boolean addEdge(IConstraintNode op, IConstraintNode to, IConstraintNode... froms){
//		System.out.println("\tto: " + to);
		for(IConstraintNode from : froms){
//			System.out.println("\t\t<- " + from);
			if(!nodeIndexMap.containsKey(from)){
				nodeIndexMap.put(from, nodeNum);
				indexNodeMap.put(nodeNum++, from);
			}
		}
		
		if(!nodeIndexMap.containsKey(to)){
			nodeIndexMap.put(to, nodeNum); 
			indexNodeMap.put(nodeNum++, to);
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
		
//		for(ConstraintNode from : froms){
//			System.err.println(from + " --> " + to);
//		}
		
		int opToEdge = edgeNum++;
		IConstraintEdge edge = new PropagationEdge(op, to); 
		edgeIndexMap.put(edge, opToEdge);
		indexEdgeMap.put(opToEdge, edge);
		
		//we do not make self cycle.
		if(froms.length == 1 && toIndex == fromIndexes[0]){
			nodeIndexMap.remove(op);
			indexNodeMap.remove(opIndex);
			return false;
		}
		addEdge(opIndex, toIndex, opToEdge);
		
		boolean ordered = (fromIndexes.length > 1)? true : false;
		
		int order = 0;
		for(int i=0; i<froms.length; i++){
			IConstraintNode from = froms[i];
			int fromIndex = fromIndexes[i];
			order++;
			int fromOpEdgeNum = edgeNum++;
			if(ordered){
				IConstraintEdge fromOpEdge = new OrderedEdge(from, op, order);
				edgeIndexMap.put(fromOpEdge, fromOpEdgeNum);
				indexEdgeMap.put(fromOpEdgeNum, fromOpEdge);
			}else{
				IConstraintEdge fromOpEdge = new PropagationEdge(from, op);
				edgeIndexMap.put(fromOpEdge, fromOpEdgeNum);
				indexEdgeMap.put(fromOpEdgeNum, fromOpEdge);
			}
			addEdge(fromIndex, opIndex, fromOpEdgeNum);
			relations.add(Pair.make(fromIndex, toIndex));
		}
		
		return true;
	}		
	
	public boolean isDrawed(IConstraintNode to){
		if(nodeIndexMap.containsKey(to)){
			int toIndex = nodeIndexMap.get(to);
			return inEdges.containsKey(toIndex);
		}
		return false;
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
	public Iterator<IConstraintNode> iterator() {
		// TODO Auto-generated method stub
		return nodeIndexMap.keySet().iterator();
	}
	
	public void optimize(){
		Map<Integer, IConstraintNode> tmap = mapClone(indexNodeMap);
		for(int nodeIndex : tmap.keySet()){
			IConstraintNode node = indexNodeMap.get(nodeIndex);
			if(!(node instanceof IBox))
				continue;
			Set<IConstraintNode> ops = getFromNodes(node, new ConstraintFilter(){
				@Override
				public boolean filter(IConstraintNode n){
					if(n instanceof AssignOpNode)
						return true;
					return false;
				}
			});
			if(ops.size() > 1){
				Set<IConstraintNode> froms = new HashSet<IConstraintNode>();
				for(IConstraintNode op : ops){
					removeEdges(op, node);
					Set<IConstraintNode> localFroms = getFromNodes(op, null);
					for(IConstraintNode from : localFroms){
						relations.remove(Pair.make(nodeIndexMap.get(from), nodeIndexMap.get(node)));
						removeEdges(from, op);
					}
					
					froms.addAll(localFroms);
				}
				
				IConstraintNode[] fromArray = froms.toArray(new IConstraintNode[0]);
				addEdge(new JoinOpNode(), node, fromArray);
			}
		}
		
		Set<Integer> nodes = new HashSet<Integer>();
		Set<Integer> edges = new HashSet<Integer>();
		nodes.addAll(inEdges.keySet());
		nodes.addAll(outEdges.keySet());
		
		for(Set<Integer> edgeSet : inEdges.values()){
			edges.addAll(edgeSet);
		}
		
		for(Set<Integer> edgeSet : outEdges.values()){
			edges.addAll(edgeSet);
		}
		
		Map<Integer, IConstraintNode> tIndexNodeMap = mapClone(indexNodeMap);
		for(int nodeIndex : tIndexNodeMap.keySet()){
			if(!nodes.contains(nodeIndex)){
				IConstraintNode node = indexNodeMap.get(nodeIndex);
				indexNodeMap.remove(nodeIndex);
				nodeIndexMap.remove(node);
			}
		}
		
		Map<Integer, IConstraintEdge> tIndexEdgeMap = mapClone(indexEdgeMap);
		for(int edgeIndex : tIndexEdgeMap.keySet()){
			if(!edges.contains(edgeIndex)){
				IConstraintEdge edge = indexEdgeMap.get(edgeIndex);
				indexEdgeMap.remove(edgeIndex);
				edgeIndexMap.remove(edge);
			}
		}
	}
	
	private void removeEdges(IConstraintNode from, IConstraintNode to){
		int fromIndex = nodeIndexMap.get(from);
		int toIndex = nodeIndexMap.get(to);
		
		Set<Integer> rEdges = new HashSet<Integer>();
		for(int edgeIndex : outEdges.get(fromIndex)){
			IConstraintEdge edge = indexEdgeMap.get(edgeIndex);
			if(edge.to().equals(to)){
				rEdges.add(edgeIndex);
			}
		}
		
		for(int edgeIndex : rEdges){
			outEdges.get(fromIndex).remove(edgeIndex);
			inEdges.get(toIndex).remove(edgeIndex);
		}
		
		if(outEdges.get(fromIndex).size() == 0)
			outEdges.remove(fromIndex);
		if(inEdges.get(toIndex).size() == 0)
			inEdges.remove(toIndex);
	}
	
	private Set<IConstraintNode> getFromNodes(IConstraintNode to, ConstraintFilter filter){
		int toIndex = nodeIndexMap.get(to);
		Set<IConstraintNode> res = new HashSet<IConstraintNode>();
		if(inEdges.containsKey(toIndex))
			for(int edgeIndex : inEdges.get(toIndex)){
				IConstraintNode from = indexEdgeMap.get(edgeIndex).from();
				if(filter == null)
					res.add(from);
				else if(filter.filter(from))
					res.add(from);
			}
		return res;
	}
	
	@SuppressWarnings("unchecked")
	private <T,S> Map<T,S> mapClone(Map<T,S> map){
		Map<T,S> newMap = new HashMap<T,S>();
		
		for(T key : map.keySet()){
			S value = map.get(key);
			if(value instanceof Set){
				@SuppressWarnings("rawtypes")
				Set set = (Set) value;
				newMap.put(key, (S)setClone(set));
			}else
				newMap.put(key, value);
		}
		return newMap;
	}
	
	private <T> Set<T> setClone(Set<T> set){
		Set<T> newSet = new HashSet<T>();
		
		for(T elem : set){
			newSet.add(elem);
		}
		
		return newSet;
	}
	
	interface ConstraintFilter{
		public boolean filter(IConstraintNode n);
	}
}
