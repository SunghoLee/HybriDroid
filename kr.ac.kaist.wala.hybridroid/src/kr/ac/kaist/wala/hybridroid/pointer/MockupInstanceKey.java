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
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.util.collections.Pair;

import java.util.Iterator;

public final class MockupInstanceKey implements InstanceKey {
	private IClass mockupClass; 
	
	public MockupInstanceKey(IMethod method){
		this.mockupClass = MockupClass.findOrCreateMockup(method);
	}
	
	@Override
	public IClass getConcreteType() {
		// TODO Auto-generated method stub
		return mockupClass;
	}

	@Override
	public Iterator<Pair<CGNode, NewSiteReference>> getCreationSites(
			CallGraph CG) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return mockupClass.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		//instanceof is possible because this class is final.
		if(obj instanceof MockupInstanceKey){
			return ((MockupInstanceKey)obj).getConcreteType().equals(mockupClass);
		}
		return false;
	}
}
