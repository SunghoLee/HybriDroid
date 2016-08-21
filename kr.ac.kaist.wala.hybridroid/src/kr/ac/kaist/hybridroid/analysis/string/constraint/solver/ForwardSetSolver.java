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
package kr.ac.kaist.hybridroid.analysis.string.constraint.solver;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.debug.UnimplementedError;

import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IConstraintEdge;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IConstraintNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IOperatorNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.OrderedEdge;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ParamBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.VarBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.BooleanDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.DoubleSetDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IBooleanDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IDoubleDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IIntegerDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.ILongDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IStringDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IntegerSetDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.LongSetDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.StringSetDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BooleanTopValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.DoubleTopValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IntegerTopValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringTopValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.ISolverMonitor;

public class ForwardSetSolver implements IStringConstraintSolver<IConstraintNode, IValue> {
	public static boolean DO_NOT_STOP = true;
	
	private IStringDomain strDomain;
	private IIntegerDomain intDomain;
	private IDoubleDomain doubleDomain;
	private IBooleanDomain booleanDomain;
	private ILongDomain longDomain;
	
	private Map<IConstraintNode, IValue> strMap;
	private Map<IConstraintEdge, IValue> strHeap;
	
	private ISolverMonitor monitor;
	
	public ForwardSetSolver(){ 
		strMap = new HashMap<IConstraintNode, IValue>();
		strHeap = new HashMap<IConstraintEdge, IValue>();
		
		strDomain = StringSetDomain.getDomain();
		intDomain = IntegerSetDomain.getDomain();
		doubleDomain = DoubleSetDomain.getDomain();
		booleanDomain = BooleanDomain.getDomain();
		longDomain = LongSetDomain.getDomain();
		monitor = new IterationSolverMonitor();
	}
	
	private void initHeap(ConstraintGraph graph){
		for(IConstraintNode n : graph){
			strMap.put(n, BotValue.getInstance());
		}
	}
	
	private Queue<IConstraintNode> initWorkList(ConstraintGraph graph){
		Queue<IConstraintNode> worklist = new LinkedBlockingQueue<IConstraintNode>();
		Set<IConstraintNode> init = graph.getInnermostNodes();
		worklist.addAll(init);
		
		return worklist;
	}
	
	@SuppressWarnings("unchecked")
	private boolean walk(ConstraintGraph graph, IConstraintNode n){
		IValue nVal = null;
		int nindex = graph.getIndex(n);
		
		if(n instanceof IBox){
			if(n instanceof ConstBox){
				ConstBox cb = (ConstBox) n;
				switch(cb.getType()){
				case LONG:
					nVal = longDomain.getOperator().alpha(cb.getValue());
					break;
				case STRING:
					nVal = strDomain.getOperator().alpha(cb.getValue());
					break;
				case INT:
					nVal = intDomain.getOperator().alpha(cb.getValue());
					break;
				case BOOL:
					nVal = booleanDomain.getOperator().alpha(cb.getValue());
					break;
				case DOUBLE:
					if(cb.getValue() instanceof Float){
						Float f = (Float) cb.getValue();
						nVal = doubleDomain.getOperator().alpha(f.doubleValue());
					}else
						nVal = doubleDomain.getOperator().alpha(cb.getValue());
					break;
				case UNKNOWN:
					// we handle with unknown type value as string top.
				case STRING_TOP:
					nVal = StringTopValue.getInstance();
					break;
				case INT_TOP:
					nVal = IntegerTopValue.getInstance();
					break;
				case BOOL_TOP:
					nVal = BooleanTopValue.getInstance();
					break;
				case DOUBLE_TOP:
					nVal = DoubleTopValue.getInstance();
					break;
				case STRING_BOT:
					nVal = StringBotValue.getInstance();
					break;
				default:
					throw new InternalError("Unknown Const Type: " + cb.getType());
				}
			}else if(n instanceof ParamBox){
				Set<IConstraintNode> preds = graph.getPredecessors(n);
				if(preds.size() != 1)
					throw new InternalError("Incorrected Graph: Besides OperatorNode, all nodes must have only one predecessor: [" + nindex + "] " + n + ", PREDS: " + preds.size());
				IConstraintNode pred = preds.iterator().next();
				nVal = strMap.get(pred);
			}else if(n instanceof VarBox){
				Set<IConstraintNode> preds = graph.getPredecessors(n);
				if(preds.size() != 1){
					String s = "";
					for(IConstraintNode nn : preds)
						s += " " + nn;
					throw new InternalError("Incorrected Graph: Besides OperatorNode, all nodes must have only one predecessor: [" + nindex + "] " + n + ", PREDS: " + preds.size() + " => " + s);
				}else{
					IConstraintNode pred = preds.iterator().next();
					nVal = strMap.get(pred);
				}
			}else
				throw new InternalError("UnknownBox: IBox node must be ConstBox, ParamBox or VarBox: " + n.getClass().getName());
		}else if(n instanceof IOperatorNode){
			IOperatorNode opn = (IOperatorNode) n;
			Set<IConstraintEdge> inEdges = graph.getInEdges(n);
			
			IValue[] vs = new IValue[inEdges.size()];

			for(IConstraintEdge inEdge : inEdges){
				if(inEdge instanceof OrderedEdge){
					OrderedEdge oe = (OrderedEdge) inEdge;
					vs[oe.getOrder()-1] = strMap.get(oe.from());
				}else{
					if(vs[0] != null)
						Assertions.UNREACHABLE("Not ordered edge must be used for single in edge.");
					vs[0] = strMap.get(inEdge.from());
				}				
			}
			try{
			nVal = opn.apply(vs);
			}catch(UnimplementedError e){
				if(DO_NOT_STOP)
					nVal = StringTopValue.getInstance();
				else
					e.printStackTrace();
			}
		}
		
		if(nVal == null){
			Assertions.UNREACHABLE("Cannot null: " + n);
		}
		
		IValue preVal = strMap.get(n);
		IValue afterVal = preVal.weakUpdate(nVal);
		
		if(preVal.equals(afterVal)){
			return false;
		}else{
			strMap.put(n, afterVal);
			return true;
		}
	}
	
	private void updateWorkList(Queue<IConstraintNode> worklist, ConstraintGraph graph, IConstraintNode n){
		Set<IConstraintEdge> outEdges = graph.getOutEdges(n);
		// add all successor nodes to worklist.
		for(IConstraintNode to : graph.getSuccessors(n)){
			if(!worklist.contains(to))
				worklist.add(to);
		}
	}
	
	@Override
	public Map<IConstraintNode, IValue> solve(ConstraintGraph graph) {
		// TODO Auto-generated method stub
		initHeap(graph);
		Queue<IConstraintNode> worklist = initWorkList(graph);
		
		while(!worklist.isEmpty()){
			IConstraintNode n = worklist.poll();
			IValue v = strMap.get(n);
			boolean updateNeed = walk(graph, n); 
			if(updateNeed){
				updateWorkList(worklist, graph, n);
			}
			
			if(monitor != null)
				monitor.monitor(worklist, n, v, strMap.get(n), updateNeed);
		}
		
		return strMap;
	}
	
	
	
}
