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
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.types.annotations.Annotation;

/**
 * Data structure denotes a class containing all methods declared in the class. 
 * @author Sungho Lee
 *
 */
public class ClassInfo{
	private IClass c;
	private List<MethodInfo> methodList;
	private final IClass jsinterAnnClass;
	
	public ClassInfo(IClass c){
		methodList = new ArrayList<MethodInfo>();
		this.c = c;
		TypeReference jsinterAnnTR = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/webkit/JavascriptInterface");
		this.jsinterAnnClass = c.getClassHierarchy().lookupClass(jsinterAnnTR);
		initClass();
	}
	
	/**
	 * Initialize a method list containing all methods delcared in the class.
	 */
	private void initClass(){
		for(IMethod m : c.getAllMethods()){
			if(hasJavascriptInterfaceAnnotation(m))
				methodList.add(new MethodInfo(m, true));
			else
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
		res += " : " + methodList;
		return res;
	}
}
