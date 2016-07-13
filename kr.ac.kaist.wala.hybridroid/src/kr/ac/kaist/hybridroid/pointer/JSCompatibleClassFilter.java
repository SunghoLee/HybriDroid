/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package kr.ac.kaist.hybridroid.pointer;

import java.util.HashSet;
import java.util.Set;

import kr.ac.kaist.hybridroid.checker.HybridAPIMisusesChecker;
import kr.ac.kaist.hybridroid.types.AndroidJavaJavaScriptTypeMap;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.ConcreteTypeKey;
import com.ibm.wala.ipa.callgraph.propagation.ConstantKey;
import com.ibm.wala.ipa.callgraph.propagation.FilteredPointerKey.SingleClassFilter;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointsToSetVariable;
import com.ibm.wala.ipa.callgraph.propagation.PropagationSystem;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.IntSetAction;
import com.ibm.wala.util.intset.IntSetUtil;

/**
 * JavaScript to Android Java type compatible class filter.
 * This filter can be used for a parameter of bridge methods.
 * The input JavaScript argument is converted to corresponding Android Java value.
 * If type checker is set, the type checker collects mis-matched type warnings.  
 * @author Sungho Lee
 */

public class JSCompatibleClassFilter extends SingleClassFilter {
	final private CGNode caller;
	final private SSAAbstractInvokeInstruction inst;
	final private CGNode target;
	final private int argNum;
	final private HybridAPIMisusesChecker typeChecker;
	
	
	public static JSCompatibleClassFilter make(CGNode caller, SSAAbstractInvokeInstruction inst, CGNode target, int argNum, HybridAPIMisusesChecker typeChecker, IClass concreteClass){
		if(!AndroidJavaJavaScriptTypeMap.isJava2JsTypeCompatible(concreteClass.getReference()))
			Assertions.UNREACHABLE("cannot convert this type to a JS type : " + concreteClass);
		
		return new JSCompatibleClassFilter(caller, inst, target, argNum, typeChecker, concreteClass);
	}
	
	private JSCompatibleClassFilter(CGNode caller, SSAAbstractInvokeInstruction inst, CGNode target, int argNum, HybridAPIMisusesChecker typeChecker, IClass concreteType){
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
					
					TypeReference jsType = ik.getConcreteType().getReference();
					TypeReference javaType = getConcreteType().getReference();

					//type conversion from JavaScript to Java
					if(AndroidJavaJavaScriptTypeMap.isJs2JavaTypeCompatible(jsType, javaType)){
						InstanceKey convertedKey = null;
						
						if(ik instanceof ConcreteTypeKey){
							convertedKey = new ConcreteTypeKey(getConcreteType());
						}else if(ik instanceof ConstantKey){
							convertedKey = new ConstantKey(((ConstantKey)ik).getValue(), getConcreteType());
						}else
							Assertions.UNREACHABLE("instance key must be either ConcreteTypeKey or ConstantKey.");
						
						int newX = system.findOrCreateIndexForInstanceKey(convertedKey);
						xs.add(newX);
					}else if(typeChecker != null) //argument type checking
						typeChecker.argTypeCheck(caller, inst, argNum, target.getMethod(), getConcreteType().getReference(), ik.getConcreteType().getReference());
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
