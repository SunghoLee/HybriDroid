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
package kr.ac.kaist.wala.hybridroid.callgraph;

import com.ibm.wala.cast.ipa.callgraph.CrossLanguageMethodTargetSelector;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.MethodTargetSelector;
import com.ibm.wala.util.strings.Atom;
import kr.ac.kaist.wala.hybridroid.pointer.MockupClass;

import java.util.Map;

public class AndroidHybridMethodTargetSelector extends
		CrossLanguageMethodTargetSelector {
	public static boolean DEBUG = false;
	
	public AndroidHybridMethodTargetSelector(
			Map<Atom, MethodTargetSelector> languageSelectors) {
		super(languageSelectors);
	}
	
	@Override
	public IMethod getCalleeTarget(CGNode caller, CallSiteReference site,
			IClass receiver) {
		
		if(receiver instanceof MockupClass){
			IMethod target = ((MockupClass)receiver).getMethod();
			if(DEBUG){
				System.err.println("@caller: " + caller);
				System.err.println("@site: " + site);
				System.err.println("@receiver: " + receiver);
				System.err.println("@target: " + target);
			}
			return target;
		}
//		else if(caller.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application)){
//			return super.getCalleeTarget(caller, site, receiver);
//		}
		else{
			return super.getCalleeTarget(caller, site, receiver);
		}
	}	
}
