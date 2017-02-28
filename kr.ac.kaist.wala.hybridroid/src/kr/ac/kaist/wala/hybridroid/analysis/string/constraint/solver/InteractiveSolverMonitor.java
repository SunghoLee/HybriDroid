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
package kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;

import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.IConstraintEdge;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.IConstraintNode;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.OrderedEdge;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.model.ISolverMonitor;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInstruction;

public class InteractiveSolverMonitor implements ISolverMonitor {

	private int iter = 1;
	private int target = -1;
	
	public void mornitor(ConstraintGraph graph,
			Map<IConstraintNode, IValue> heap, IConstraintNode n, IValue preV,
			IValue newV) {
		// TODO Auto-generated method stub		
		System.out.println("#Iter(" + (iter++) + "): [" + graph.getIndex(n) + "] " + n);
		
		if(target > 0 && target != iter-1)
			return;
		
		target = -1;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String s = null;
		try{
			while((s = br.readLine()) != null){
				if(!exec(graph, heap, n, preV, newV, s))
					break;
			}
		}catch (Exception e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private boolean exec(ConstraintGraph graph,
			Map<IConstraintNode, IValue> heap, IConstraintNode n, IValue preV,
			IValue newV, String cmd){
		StringTokenizer st = new StringTokenizer(cmd);
		
		if(st.countTokens() == 0)
			return false;
		
		String mainCmd = st.nextToken();
		
		switch(mainCmd){
		case "eval":
			eval(graph, n, preV, newV);
			break;
		case "predecessors":
			printPreds(graph, n);
			break;
		case "jump":
			if(st.hasMoreTokens()){
				String strTarget = st.nextToken();
				try{
					target = Integer.parseInt(strTarget);
				}catch(Exception e){
					return true;
				}
				return false;
			}else
				return true;
		case "value":
			if(st.hasMoreTokens()){
				String strTarget = st.nextToken();
				try{
					int num = Integer.parseInt(strTarget);
					printValue(graph, heap, num);
				}catch(Exception e){
					return true;
				}
				return true;
			}else
				return true;
		case "insts":
			if(st.hasMoreTokens()){
				String strTarget = st.nextToken();
				try{
					int num = Integer.parseInt(strTarget);
					printInstructions(graph.getNode(num));
				}catch(Exception e){
					return true;
				}
				return true;
			}else
				return true;
		default:
			return true;
		}
		return true;
	}
	
	private void eval(ConstraintGraph graph, IConstraintNode n, IValue preV, IValue newV){
		System.out.println("#Node: " +  n);
		System.out.println("#preValue: " +  preV);
		System.out.println("#newValue: " +  newV);
	}
	
	private void printPreds(ConstraintGraph graph, IConstraintNode n ){
		Set<IConstraintEdge> edges = graph.getInEdges(n);
		if(edges.size() > 1){
			OrderedEdge[] oEdges = new OrderedEdge[edges.size()];
			for(IConstraintEdge e : edges){
				OrderedEdge oe = (OrderedEdge) e;
				oEdges[oe.getOrder() - 1] = oe;
			}
			
			for(OrderedEdge e : oEdges){
				System.out.println("(" + e.getOrder() + ") [" + graph.getIndex(e.from()) + "] " + e.from());	
			}
		}else if(edges.isEmpty()){
			System.out.println("No predecessors.");
		}else{
			System.out.println(edges.iterator().next());
		}
	}
	
	private void printValue(ConstraintGraph graph, Map<IConstraintNode, IValue> heap, int num){
		try{
			System.out.println("#value: " + heap.get(graph.getNode(num)));
		}catch(Exception e){
			System.out.println("[" + num + "] node does not exist.");
		}
	}
	
	private void printInstructions(IConstraintNode n){
		if(n instanceof IBox){
			CGNode node = ((IBox) n).getNode();
			SSAInstruction[] insts = node.getIR().getInstructions();
			int index = 1;
			System.out.println("====================");
			for(SSAInstruction inst : insts){
				System.out.println("(" + (index++) + ") " + inst);
			}
			System.out.println("====================");
		}else{
			System.out.println("The node is not real node in the program.");
		}
	}

	@Override
	public void monitor(Queue<IConstraintNode> worklist, IConstraintNode n, IValue preV, IValue newV, boolean isUpdate) {
		// TODO Auto-generated method stub
		
	}
}
