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
package kr.ac.kaist.wala.hybridroid.pointer;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.ipa.callgraph.propagation.FilteredPointerKey.TypeFilter;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.*;
import kr.ac.kaist.wala.hybridroid.checker.HybridAPIMisusesChecker;
import kr.ac.kaist.wala.hybridroid.types.AndroidJavaJavaScriptTypeMap;

import java.util.HashSet;
import java.util.Set;

/**
 * Android Java to JavaScript type compatible class filter.
 * This filter can be used for a return value of bridge methods.
 * The Android Java return value is converted to corresponding JavaScript value.
 * If type checker is set, the type checker collects mis-matched type warnings.  
 * @author Sungho Lee
 */

public class JavaCompatibleClassFilter implements TypeFilter {
	final private static boolean DEBUG_CLASS_FILTER = false;
	
	final private CGNode caller;
	final private SSAAbstractInvokeInstruction inst;
	final private CGNode target;
	final private HybridAPIMisusesChecker typeChecker;
	
	
	public static JavaCompatibleClassFilter make(CGNode caller, SSAAbstractInvokeInstruction inst, CGNode target, HybridAPIMisusesChecker typeChecker){
		return new JavaCompatibleClassFilter(caller, inst, target, typeChecker);
	}
	
	private JavaCompatibleClassFilter(CGNode caller, SSAAbstractInvokeInstruction inst, CGNode target, HybridAPIMisusesChecker typeChecker){
		this.caller = caller;
		this.inst = inst;
		this.target = target;
		this.typeChecker = typeChecker;
	}

	private PointsToSetVariable convert2JsType(final PropagationSystem system, final PointsToSetVariable R, final Set<IClass> pClasses){
		final Set<Integer> xs = new HashSet<Integer>();
		
		if(R.getValue() != null){
        	R.getValue().foreach(new IntSetAction(){
				@Override
				public void act(int x) {
					// TODO Auto-generated method stub
					InstanceKey ik = system.getInstanceKey(x);
					
					TypeReference javaType = ik.getConcreteType().getReference();

					//type conversion from Java to JavaScript
					if(AndroidJavaJavaScriptTypeMap.isJava2JsTypeCompatible(javaType)){
						TypeReference jsType = AndroidJavaJavaScriptTypeMap.java2JsTypeConvert(javaType);
						InstanceKey convertedKey = null;
						IClassHierarchy cha = caller.getClassHierarchy();
						IClass jsClass = cha.lookupClass(jsType);
						pClasses.add(jsClass);
						
						if(DEBUG_CLASS_FILTER){
							System.err.println("Java type(" + javaType + ") can be converted to JavaScript type(" + jsType + ")");
						}
						
						if(ik instanceof ConcreteTypeKey){
							convertedKey = new ConcreteTypeKey(jsClass);
						}else if(ik instanceof ConstantKey){
							convertedKey = new ConstantKey(((ConstantKey)ik).getValue(), jsClass);
						}else
							Assertions.UNREACHABLE("instance key must be either ConcreteTypeKey or ConstantKey.");
						
						if(DEBUG_CLASS_FILTER){
							System.err.println("IK(" + ik + ") is converted to IK(" + convertedKey + ")");
						}
						
						int newX = system.findOrCreateIndexForInstanceKey(convertedKey);
						MutableIntSet set = (MutableIntSet)system.getInstanceKeysForClass(jsClass);
						set.add(newX);
						xs.add(newX);
					}else if(DEBUG_CLASS_FILTER){
						System.err.println("IK(" + ik + ") is not compatible with JavaScript.");
					}

					if(typeChecker != null) // return type check
			    		typeChecker.returnTypeCheck(caller, inst, target.getMethod());
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
		Set<IClass> possibleClasses = new HashSet<IClass>();
		
		PointsToSetVariable convertedR = convert2JsType(system, R, possibleClasses);
		IntSet f = null;
		
		for(IClass pClass : possibleClasses){
			
			if(f == null)
				f = system.getInstanceKeysForClass(pClass);
			else
				f = f.union(system.getInstanceKeysForClass(pClass));
		}
		
		if(DEBUG_CLASS_FILTER){
			System.err.println(system.getInstanceKey(1056));
			IntIterator intItor = f.intIterator();
			while(intItor.hasNext()){
				int index = intItor.next();
				System.err.println("\t"+system.getInstanceKey(index));
			}
		}
		return (f == null) ? false : L.addAllInIntersection(convertedR, f);
	}

	@Override
	public boolean addInverseFiltered(PropagationSystem system,
			PointsToSetVariable L, PointsToSetVariable R) {
		// TODO Auto-generated method stub
		Set<IClass> possibleClasses = new HashSet<IClass>();
		  PointsToSetVariable convertedR = convert2JsType(system, R, possibleClasses);
	      IntSet f = null;
	      
	      for(IClass pClass : possibleClasses){
				if(f == null)
					f = system.getInstanceKeysForClass(pClass);
				else
					f = f.union(system.getInstanceKeysForClass(pClass));
			}
	    
	      if(DEBUG_CLASS_FILTER)
				System.err.println(f);
	      // SJF: this is horribly inefficient. we really don't want to do
	      // diffs in here. TODO: fix it. probably keep not(f) cached and
	      // use addAllInIntersection
	      System.out.println("IL: " + L);
			System.out.println("IR: " + R);
	      return (f == null) ? L.addAll(convertedR) : L.addAll(IntSetUtil.diff(convertedR.getValue(), f));
	}

	@Override
	public boolean isRootFilter() {
		// TODO Auto-generated method stub
		return false;
	}
}
