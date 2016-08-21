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
package kr.ac.kaist.hybridroid.analysis.string;

import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;

public class ArgumentHotspot implements Hotspot {
	private TypeReference cDescriptor;
	private Selector mSelector;
	private int argIndex;
	
	public ArgumentHotspot(ClassLoaderReference cRef, String cDescriptor, String mDescriptor, int argIndex){
		this.argIndex = argIndex;
		
		this.cDescriptor = TypeReference.findOrCreateClass(cRef, cDescriptor.substring(0, cDescriptor.lastIndexOf("/")), cDescriptor.substring(cDescriptor.lastIndexOf("/")+1));
		this.mSelector = Selector.make(mDescriptor);
	}

	public TypeReference getClassDescriptor() {
		return this.cDescriptor;
	}
	
	public Selector getMethodDescriptor() {
		return this.mSelector;
	}

	public int getArgIndex() {
		return argIndex;
	}	
	
	@Override
	public int index(){
		return getArgIndex();
	}
	
	@Override
	public String toString(){
		return "ArgSpot[" + cDescriptor + " " + mSelector + " (@" + argIndex + ")]";
	}
}
