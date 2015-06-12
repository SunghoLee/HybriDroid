package kr.ac.kaist.hybridroid.pointer;

import java.util.HashSet;
import java.util.Set;

import kr.ac.kaist.hybridroid.checker.Js2JavaCompatibleTypeChecker;
import kr.ac.kaist.hybridroid.types.AndroidJavaJavaScriptTypeMap;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.FilteredPointerKey.SingleClassFilter;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointsToSetVariable;
import com.ibm.wala.ipa.callgraph.propagation.PropagationSystem;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.IntSetAction;
import com.ibm.wala.util.intset.IntSetUtil;

public class JSCompatibleClassFilter extends SingleClassFilter {
	final private CGNode caller;
	final private SSAAbstractInvokeInstruction inst;
	final private CGNode target;
	final private int argNum;
	final private Js2JavaCompatibleTypeChecker typeChecker;
	
	public static JSCompatibleClassFilter make(CGNode caller, SSAAbstractInvokeInstruction inst, CGNode target, int argNum, Js2JavaCompatibleTypeChecker typeChecker, IClass concreteType){
		if(!AndroidJavaJavaScriptTypeMap.isJava2JSConvertable(concreteType))
			Assertions.UNREACHABLE("cannot convert this type to a JS type : " + concreteType);
		
		return new JSCompatibleClassFilter(caller, inst, target, argNum, typeChecker, concreteType);
	}
	
	private JSCompatibleClassFilter(CGNode caller, SSAAbstractInvokeInstruction inst, CGNode target, int argNum, Js2JavaCompatibleTypeChecker typeChecker, IClass concreteType){
		super(concreteType);
		this.caller = caller;
		this.inst = inst;
		this.target = target;
		this.argNum = argNum;
		this.typeChecker = typeChecker;
	}

	private PointsToSetVariable convert2JSType(final PropagationSystem system, PointsToSetVariable R){
		final Set<Integer> xs = new HashSet<Integer>();
		
		if(R.getValue() != null){
        	R.getValue().foreach(new IntSetAction(){
				@Override
				public void act(int x) {
					// TODO Auto-generated method stub
					InstanceKey ik = system.getInstanceKey(x);
					
					InstanceKey convertedIK = AndroidJavaJavaScriptTypeMap.js2JavaTypeConvert(ik, getConcreteType());
					
					//argument type checking
					if(typeChecker != null)
						typeChecker.argTypeCheck(caller, inst, argNum, target.getMethod(), getConcreteType().getReference(), ik.getConcreteType().getReference());

					int newX = system.findOrCreateIndexForInstanceKey(convertedIK);
					xs.add(newX);
				}
           	});
        }
        
        PointsToSetVariable convertedR = new PointsToSetVariable(R.getPointerKey());
        for(int value : xs){
        	convertedR.add(value);
        }
        
        return convertedR;
	}
	
	@Override
	public boolean addFiltered(final PropagationSystem system, PointsToSetVariable L,
			PointsToSetVariable R) {
		// TODO Auto-generated method stub
		IntSet f = system.getInstanceKeysForClass(super.getConcreteType());
        PointsToSetVariable convertedR = convert2JSType(system, R);
        
        return (f == null) ? false : L.addAllInIntersection(convertedR, f);
	}

	@Override
	public boolean addInverseFiltered(PropagationSystem system,
			PointsToSetVariable L, PointsToSetVariable R) {
		// TODO Auto-generated method stub
	      IntSet f = system.getInstanceKeysForClass(super.getConcreteType());
	      PointsToSetVariable convertedR = convert2JSType(system, R);
	      // SJF: this is horribly inefficient. we really don't want to do
	      // diffs in here. TODO: fix it. probably keep not(f) cached and
	      // use addAllInIntersection
	      return (f == null) ? L.addAll(convertedR) : L.addAll(IntSetUtil.diff(convertedR.getValue(), f));
	}
}
