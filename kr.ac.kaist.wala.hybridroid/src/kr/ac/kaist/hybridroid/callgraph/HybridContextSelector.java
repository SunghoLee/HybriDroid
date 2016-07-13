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
package kr.ac.kaist.hybridroid.callgraph;

import com.ibm.wala.cast.js.types.JavaScriptTypes;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.util.intset.IntSet;

public class HybridContextSelector implements ContextSelector {

	private ContextSelector delegateForJava;
	private ContextSelector delegateForJS;
	
	public HybridContextSelector(ContextSelector delegateForJava, ContextSelector delegateForJS){
		this.delegateForJava = delegateForJava;
		this.delegateForJS = delegateForJS;
	}
	
	@Override
	public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee,
			InstanceKey[] actualParameters) {
		// TODO Auto-generated method stub
		if(isJavaMethod(caller.getMethod()))
			return delegateForJava.getCalleeTarget(caller, site, callee, actualParameters);
		else
			return delegateForJS.getCalleeTarget(caller, site, callee, actualParameters);
	}

	@Override
	public IntSet getRelevantParameters(CGNode caller, CallSiteReference site) {
		// TODO Auto-generated method stub
		if(isJavaMethod(caller.getMethod()))
			return delegateForJava.getRelevantParameters(caller, site);
		else
			return delegateForJS.getRelevantParameters(caller, site);
	}

	private boolean isJavaMethod(IMethod m){
		if(m.getDeclaringClass().getClassLoader().equals(JavaScriptTypes.jsLoader))
			return false;
		return true;
	}
}
