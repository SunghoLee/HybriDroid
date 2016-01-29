package kr.ac.kaist.hybridroid.analysis.string.constraint.solver;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import kr.ac.kaist.hybridroid.analysis.string.constraint.AppendOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.AssignOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IConstraintEdge;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IConstraintNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IOperatorNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.JoinOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.OrderedEdge;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ParamBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ReplaceOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.UriCodecDecodeOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.UriGetQueryParameterOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.UriParseOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.VarBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.BooleanDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.DoubleSetDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IBooleanDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IDoubleDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IIntegerDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IStringDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IntegerSetDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.StringSetDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BooleanTopValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.DoubleTopValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IntegerTopValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringTopValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.AppendOpSetModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.AssignOpSetModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.IOperationModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.ISolverMonitor;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.JoinOpSetModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.ReplaceOpSetModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.UriCodecDecodeOpSetModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.UriGetQueryParameterOpSetModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.UriParseOpSetModel;

public class ForwardSetSolver implements IStringConstraintSolver<IConstraintNode, IValue> {
	private IStringDomain strDomain;
	private IIntegerDomain intDomain;
	private IDoubleDomain doubleDomain;
	private IBooleanDomain booleanDomain;
	
	private Map<IConstraintNode, IValue> strMap;
	private Map<IConstraintEdge, IValue> strHeap;
	
	private ISolverMonitor monitor;
	
	private static Map<Class, IOperationModel> models;
	
	static{
		models = new HashMap<Class, IOperationModel>();
		loadModels();
	}
	
	private static void loadModels(){
		models.put(AssignOpNode.class, new AssignOpSetModel());
		models.put(AppendOpNode.class, new AppendOpSetModel());
		models.put(JoinOpNode.class, new JoinOpSetModel());
		models.put(ReplaceOpNode.class, new ReplaceOpSetModel());
		models.put(UriCodecDecodeOpNode.class, new UriCodecDecodeOpSetModel());
		models.put(UriGetQueryParameterOpNode.class, new UriGetQueryParameterOpSetModel());
		models.put(UriParseOpNode.class, new UriParseOpSetModel());
	}

	public ForwardSetSolver(){ 
		strMap = new HashMap<IConstraintNode, IValue>();
		strHeap = new HashMap<IConstraintEdge, IValue>();
		
		strDomain = StringSetDomain.getDomain();
		intDomain = IntegerSetDomain.getDomain();
		doubleDomain = DoubleSetDomain.getDomain();
		booleanDomain = BooleanDomain.getDomain();
		
//		monitor = new InteractiveSolverMonitor();
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
				if(preds.size() != 1)
					throw new InternalError("Incorrected Graph: Besides OperatorNode, all nodes must have only one predecessor: [" + nindex + "] " + n + ", PREDS: " + preds.size());
				IConstraintNode pred = preds.iterator().next();
				nVal = strMap.get(pred);
			}else
				throw new InternalError("UnknownBox: IBox node must be ConstBox, ParamBox or VarBox: " + n.getClass().getName());
		}else if(n instanceof IOperatorNode){
			if(n instanceof AssignOpNode){
				Set<IConstraintNode> preds = graph.getPredecessors(n);
				if(preds.size() != 1)
					throw new InternalError("Incorrected Graph: Besides OperatorNode, all nodes must have only one predecessor: [" + nindex + "] " + n + ", PREDS: " + preds.size());
				IConstraintNode pred = preds.iterator().next();
				nVal = strMap.get(pred);
			}else if(n instanceof AppendOpNode){
				Set<IConstraintEdge> inEdges = graph.getInEdges(n);
				if(inEdges.size() != 2)
					throw new InternalError("Incorrected Graph: Append operator must have two in edges: [" + nindex + "] " + n + ", PREDS: " + inEdges.size());
				IConstraintNode front = null;
				IConstraintNode back = null;
				for(IConstraintEdge e : inEdges){
					if((e instanceof OrderedEdge) == false)
						throw new InternalError("Incorrected Graph: All in-edges of append operator must be ordered edges: [" + nindex + "] " + e);
					OrderedEdge oe = (OrderedEdge) e;
					if(oe.getOrder() == 1)
						front = oe.from();
					else if(oe.getOrder() == 2)
						back = oe.from();
				}
				System.out.println("qwrqwr+1: " + strMap.get(front));
				System.out.println("qwrqwr+2: " + strMap.get(back));
				nVal = (IValue)models.get(AppendOpNode.class).apply(strMap.get(front), strMap.get(back));
			}else if(n instanceof JoinOpNode){
				Set<IConstraintNode> preds = graph.getPredecessors(n);
				if(preds.size() < 2)
					throw new InternalError("Incorrected Graph: Join operator must have two in edges at least: [" + nindex + "] " + n + ", PREDS: " + preds.size());
				
				IValue[] values = new IValue[preds.size()];
				
				int index = 0;
				for(IConstraintNode pred : preds){
					values[index] = strMap.get(pred);
					index++;
				}
				
				nVal = (IValue)models.get(JoinOpNode.class).apply(values);
			}else if(n instanceof ReplaceOpNode){
				Set<IConstraintEdge> inEdges = graph.getInEdges(n);
				if(inEdges.size() != 3)
					throw new InternalError("Incorrected Graph: Replace operator must have three in edges: [" + nindex + "] " + n + ", PREDS: " + inEdges.size());
				IConstraintNode str = null;
				IConstraintNode target = null;
				IConstraintNode subst = null;
				
				for(IConstraintEdge e : inEdges){
					if((e instanceof OrderedEdge) == false)
						throw new InternalError("Incorrected Graph: All in-edges of replace operator must be ordered edges: [" + nindex + "] " + e);
					OrderedEdge oe = (OrderedEdge) e;
					if(oe.getOrder() == 1)
						str = oe.from();
					else if(oe.getOrder() == 2)
						target = oe.from();
					else if(oe.getOrder() == 3)
						subst = oe.from();
				}
				
				nVal = (IValue)models.get(ReplaceOpNode.class).apply(strMap.get(str), strMap.get(target), strMap.get(subst));
			}else if(n instanceof UriCodecDecodeOpNode){
				Set<IConstraintEdge> inEdges = graph.getInEdges(n);
				if(inEdges.size() != 4)
					throw new InternalError("Incorrected Graph: UriCodecDecode operator must have four in edges: [" + nindex + "] " + n + ", PREDS: " + inEdges.size());
				IConstraintNode str = null;
				IConstraintNode cv = null;
				IConstraintNode cs = null;
				IConstraintNode tof = null;
				
				for(IConstraintEdge e : inEdges){
					if((e instanceof OrderedEdge) == false)
						throw new InternalError("Incorrected Graph: All in-edges of uricodecdecode operator must be ordered edges: [" + nindex + "] " + e);
					OrderedEdge oe = (OrderedEdge) e;
					if(oe.getOrder() == 1)
						str = oe.from();
					else if(oe.getOrder() == 2)
						cv = oe.from();
					else if(oe.getOrder() == 3)
						cs = oe.from();
					else if(oe.getOrder() == 4)
						tof = oe.from();
				}
				
				nVal = (IValue)models.get(UriCodecDecodeOpNode.class).apply(strMap.get(str), strMap.get(cv), strMap.get(cs), strMap.get(tof));
			}else if(n instanceof UriGetQueryParameterOpNode){
				Set<IConstraintEdge> inEdges = graph.getInEdges(n);
				if(inEdges.size() != 2)
					throw new InternalError("Incorrected Graph: UriGetQueryParameter operator must have two in edges: [" + nindex + "] " + n + ", PREDS: " + inEdges.size());
				IConstraintNode uri = null;
				IConstraintNode res = null;
								
				for(IConstraintEdge e : inEdges){
					if((e instanceof OrderedEdge) == false)
						throw new InternalError("Incorrected Graph: All in-edges of urigetqueryparameter operator must be ordered edges: [" + nindex + "] " + e);
					OrderedEdge oe = (OrderedEdge) e;
					if(oe.getOrder() == 1)
						uri = oe.from();
					else if(oe.getOrder() == 2)
						res = oe.from();
				}
				System.out.println("qwrqwr-1: " + strMap.get(uri));
				System.out.println("qwrqwr-2: " + strMap.get(res));
				nVal = (IValue)models.get(UriGetQueryParameterOpNode.class).apply(strMap.get(uri), strMap.get(res));
			}else if(n instanceof UriParseOpNode){
				Set<IConstraintEdge> inEdges = graph.getInEdges(n);
				if(inEdges.size() != 1)
					throw new InternalError("Incorrected Graph: UriParse operator must have one in edges: [" + nindex + "] " + n);
				IConstraintNode uri = inEdges.iterator().next().from();
				
				nVal = (IValue)models.get(UriParseOpNode.class).apply(strMap.get(uri));
			}else
				throw new InternalError("UnknownOperator: Unknown operator node: [" + nindex + "] " + n.getClass().getName());
		}
		
		if(monitor != null)
			monitor.mornitor(graph, strMap, n, strMap.get(n), nVal);
		
		IValue preVal = strMap.put(n, nVal);
		if(preVal.equals(nVal))
			return false;
		else
			return true;
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
			if(walk(graph, n)){
				updateWorkList(worklist, graph, n);
			}
		}
		
		return strMap;
	}
	
	
	
}
