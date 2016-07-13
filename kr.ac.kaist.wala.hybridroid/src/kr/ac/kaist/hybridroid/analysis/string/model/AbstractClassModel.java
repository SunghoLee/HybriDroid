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
package kr.ac.kaist.hybridroid.analysis.string.model;

import java.util.HashMap;
import java.util.Map;

import com.ibm.wala.types.Selector;

abstract public class AbstractClassModel implements IClassModel {
	protected Map<Selector, IMethodModel> methodMap;
	
	protected AbstractClassModel(){
		methodMap = new HashMap<Selector, IMethodModel>();
		init();
	}
	
	abstract protected void init();
	
	@Override
	public IMethodModel getMethod(Selector mSelector) {
		// TODO Auto-generated method stub
		if(methodMap.containsKey(mSelector))
			return methodMap.get(mSelector);
//		System.err.println("Unkwon method: " + mSelector);
		return null;
	}

}
