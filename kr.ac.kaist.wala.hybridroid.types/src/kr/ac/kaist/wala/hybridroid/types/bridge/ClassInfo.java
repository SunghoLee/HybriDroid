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
package kr.ac.kaist.wala.hybridroid.types.bridge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.types.annotations.Annotation;

import kr.ac.kaist.hybridroid.types.HybriDroidTypes;

/**
 * Data structure denotes a class containing all methods declared in the class. 
 * @author Sungho Lee
 *
 */
public class ClassInfo{
	private IClass c;
	private List<MethodInfo> methodList;
	private final IClass jsinterAnnClass;
	
	public ClassInfo(IClass c, boolean isAboveJELLYBEAN){
		methodList = new ArrayList<MethodInfo>();
		this.c = c;
		TypeReference jsinterAnnTR = HybriDroidTypes.JAVASCRIPT_INTERFACE_ANNOTATION;
		this.jsinterAnnClass = c.getClassHierarchy().lookupClass(jsinterAnnTR);
		initClass(isAboveJELLYBEAN);
	}
	
	/**
	 * Get the object representing this class.
	 * @return a class object.
	 */
	public IClass getClassObject(){
		return c;
	}
	
	/**
	 * Get the name of this class.
	 * @return a string value for the name.
	 */
	public String getClassName(){
		return c.getName().getClassName().toString();
	}
	
	/**
	 * Get all methods declared in this class.
	 * @return a set of methods
	 */
	public Set<MethodInfo> getAllMethods(){
		return new HashSet<MethodInfo>(methodList);
	}
	
	/**
	 * Get all methods accessible from a bridge object
	 * @return a set of methods
	 */
	public Set<MethodInfo> getAllAccessibleMethods(){
		Set<MethodInfo> s = getAllMethods();
		s.removeIf(new Predicate<MethodInfo>(){
			@Override
			public boolean test(MethodInfo t) {
				// TODO Auto-generated method stub
				return !(t.isAccessible());
			}
		});
		return s;
	}
	
	/**
	 * Initialize a method list containing all methods delcared in the class.
	 */
	private void initClass(boolean isAboveJELLYBEAN){
		for(IMethod m : c.getAllMethods()){
			if(m.isInit() || m.isClinit())
				continue;
			if(isAboveJELLYBEAN && hasJavascriptInterfaceAnnotation(m) || !isAboveJELLYBEAN) {
				methodList.add(new MethodInfo(m, true));
			}else
				methodList.add(new MethodInfo(m, false));
		}
	}
	
	/**
	 * Check which a method has the 'JavascriptInterface' annotation or not. 
	 * @param m a target method
	 * @return true if the method has the 'JavascriptInterface' annotation, otherwise false.
	 */
	private boolean hasJavascriptInterfaceAnnotation(IMethod m) {
		if(m.getAnnotations() != null){
			for (Annotation ann : m.getAnnotations()) {
				TypeReference annTr= ann.getType();
				if (jsinterAnnClass.equals(c.getClassHierarchy().lookupClass(annTr)))
					return true;
			}
			return false;
		}else
			return true;
	}
	
	public String toString(){
		String res = "";
		res += c;
//		res += " : " + methodList;
		res += " : " + getAllAccessibleMethods();
		return res;
	}
}
