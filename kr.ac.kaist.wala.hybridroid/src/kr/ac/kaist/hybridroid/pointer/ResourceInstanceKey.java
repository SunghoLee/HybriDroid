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
package kr.ac.kaist.hybridroid.pointer;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.util.collections.Pair;

public class ResourceInstanceKey implements InstanceKey {

	private CGNode node;
	private IClass declaredType;
	private int iindex;
	private NewSiteReference refSite;
	private int resNum;
	
	public ResourceInstanceKey(CGNode node, IClass declaredType, int iindex, int resNum){
		this.node = node;
		this.declaredType = declaredType;
		this.iindex = iindex;
		this.resNum = resNum;
		this.refSite = NewSiteReference.make(iindex, declaredType.getReference());
	}
	
	@Override
	public IClass getConcreteType() {
		// TODO Auto-generated method stub
		return declaredType;
	}

	@Override
	public Iterator<Pair<CGNode, NewSiteReference>> getCreationSites(CallGraph CG) {
		// TODO Auto-generated method stub
		List<Pair<CGNode, NewSiteReference>> l = new ArrayList<Pair<CGNode, NewSiteReference>>();
		l.add(Pair.make(node, refSite));
		return l.iterator();
	}
	
	@Override
	public int hashCode(){
		return node.hashCode() + resNum;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof ResourceInstanceKey){
			ResourceInstanceKey rik = (ResourceInstanceKey) o;
			// ResourceInstanceKeys are equal when the resource numbers are same;
			if(rik.resNum == resNum){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString(){
		return "Resource #" + resNum;
	}
}
