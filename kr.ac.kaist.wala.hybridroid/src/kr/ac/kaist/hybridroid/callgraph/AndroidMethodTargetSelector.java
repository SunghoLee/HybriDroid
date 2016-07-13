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

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyMethodTargetSelector;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;

public class AndroidMethodTargetSelector extends ClassHierarchyMethodTargetSelector {

	private IClass activityClass;
	private IClass threadClass;
	private IClass asyncTaskClass;
	private IClassHierarchy cha;
	
	public AndroidMethodTargetSelector(IClassHierarchy cha) {
		super(cha);
		this.cha = cha;
		TypeReference activityTR = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/app/Activity");
		TypeReference threadTR = TypeReference.find(ClassLoaderReference.Primordial, "Ljava/lang/Thread");
		TypeReference asyncTaskTR = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/AsyncTask");
		this.activityClass = cha.lookupClass(activityTR);
		this.threadClass = cha.lookupClass(threadTR);
		this.asyncTaskClass = cha.lookupClass(asyncTaskTR);
//		System.out.println("ASYNCTASKTR: " + asyncTaskTR);
//		System.out.println("ASYNCTASKCLASS: " + asyncTaskClass);
//		for(IMethod m : asyncTaskClass.getAllMethods()){
//			System.out.println("\t" + m);
//		}
		// TODO Auto-generated constructor stub
	}

	@Override
	public IMethod getCalleeTarget(CGNode caller, CallSiteReference call, IClass receiver) {
		// TODO Auto-generated method stub
//		if(receiver != null && ((activityClass.equals(receiver) || cha.isSubclassOf(receiver, activityClass)) ||
//				(threadClass.equals(receiver) || cha.isSubclassOf(receiver, threadClass)))){
//			IMethod m = super.getCalleeTarget(caller, call, receiver);
//			if(caller.toString().contains("supersonic") && (call.toString().contains("runOnUiThread") ||
//					call.toString().contains("init") || 
//					call.toString().contains("run"))){
//				System.out.println("----");
//				System.out.println("#caller: " + caller);
//				System.out.println("#call: " + call);
//				System.out.println("#receiver: " + receiver);
//				System.out.println("#target: " + m);
//				System.out.println("----");
//			}
//			
//			if(m != null && m.toString().contains("supersonic")){
//				System.out.println("----");
//				System.out.println("#called Sonic method: " + m);
//				System.out.println("----");
//			}
//			
//			return m;
//		}else
			return super.getCalleeTarget(caller, call, receiver);
	}
}
