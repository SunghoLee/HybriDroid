package kr.ac.kaist.hybridroid.analysis.string.resource.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.AssignOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstType;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.model.IClassModel;
import kr.ac.kaist.hybridroid.analysis.string.model.IMethodModel;
import kr.ac.kaist.hybridroid.analysis.string.model.StringModel;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;

public class ContextClassModel implements IClassModel {
	private static ContextClassModel instance;

	private Map<String, IMethodModel> methodMap;

	public static ContextClassModel getInstance() {
		if (instance == null)
			instance = new ContextClassModel();
		return instance;
	}

	private ContextClassModel() {
		methodMap = new HashMap<String, IMethodModel>();
		init();
	}

	private void init() {
		methodMap.put("getString", new GetString());
	}

	@Override
	public IMethodModel getMethod(String methodName) {
		if (methodMap.containsKey(methodName))
			return methodMap.get(methodName);
		System.err.println("Unkwon 'Context' method: " + methodName);
		return null;
	}

	class GetString implements IMethodModel<Set<IBox>> {

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			switch (invokeInst.getNumberOfUses()) {
			case 1:
				return arg1(graph, def, caller, invokeInst);
			case 2:
				return arg2(graph, def, caller, invokeInst);
			default:
				StringModel.setWarning("Unknown Context getString: #arg is "
						+ invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}

		public Set<IBox> arg1(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			IBox use = new ConstBox(caller, "RESOURCE", ConstType.STRING);
			if (graph.addEdge(new AssignOpNode(), def, use))
				boxSet.add(use);
			return boxSet;
		}

		public Set<IBox> arg2(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			IBox use = new ConstBox(caller, "RESOURCE", ConstType.STRING);
			if (graph.addEdge(new AssignOpNode(), def, use))
				boxSet.add(use);
			return boxSet;
		}
		
		@Override
		public String toString() {
			return "Constraint Graph Method Model: Context.getString";
		}

	}
}
