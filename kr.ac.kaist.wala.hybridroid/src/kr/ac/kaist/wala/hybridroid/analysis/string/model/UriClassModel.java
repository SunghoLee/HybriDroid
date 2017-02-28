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
package kr.ac.kaist.wala.hybridroid.analysis.string.model;

import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.Selector;

import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.ToStringOpNode;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.VarBox;

public class UriClassModel  extends AbstractClassModel{
	private static UriClassModel instance;
	
	public static UriClassModel getInstance(){
		if(instance == null)
			instance = new UriClassModel();
		return instance;
	}
	
	protected void init(){
		methodMap.put(Selector.make("toString()Ljava/lang/String;"), new ToString());
//		methodMap.put("getQueryParameter", new GetQueryParameter());
//		methodMap.put("parse", new Parse());
		
	}
		
	//toString()Ljava/lang/String;
	class ToString implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller, SSAInvokeInstruction invokeInst) {
			// TODO Auto-generated method stub
			Set<IBox> boxSet = new HashSet<IBox>();
			int uriVar = invokeInst.getUse(0);
			IBox uriBox = new VarBox(caller, invokeInst.iindex, uriVar);
			if(graph.addEdge(new ToStringOpNode(), def, uriBox)){
					boxSet.add(uriBox);
			}
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: Uri.toString()Ljava/lang/String;";
		}
	}
}

