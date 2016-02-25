package kr.ac.kaist.hybridroid.analysis.string.model;

import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.Selector;

import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ToStringOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.VarBox;
import kr.ac.kaist.hybridroid.analysis.string.model.ObjectClassModel.ToString1;

public class CharsetProviderClassModel extends AbstractClassModel {
	private static CharsetProviderClassModel instance;
	
	public static CharsetProviderClassModel getInstance(){
		if(instance == null)
			instance = new CharsetProviderClassModel();
		return instance;
	}
	
	protected void init(){
		methodMap.put(Selector.make("charsetForName(Ljava/lang/String;)Ljava/nio/charset/Charset;"), new CharsetForName());
		methodMap.put(Selector.make("toString()Ljava/lang/String;"), new ToString1());
//		methodMap.put(Selector.make("charsetForName(Ljava/lang/String;)Ljava/nio/charset/Charset;"), new ToString1());
		//charsetForName(Ljava/lang/String;)Ljava/nio/charset/Charset;
	}
		
	//charsetForName(Ljava/lang/String;)Ljava/nio/charset/Charset;
	class CharsetForName implements IMethodModel<Set<IBox>>{
		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int objVar = invokeInst.getUse(0);
			IBox objBox = new VarBox(caller, invokeInst.iindex, objVar);
			if(graph.addEdge(new ToStringOpNode(), def, objBox)){
					boxSet.add(objBox);
			}
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: Object.toString";
		}
	}
}
