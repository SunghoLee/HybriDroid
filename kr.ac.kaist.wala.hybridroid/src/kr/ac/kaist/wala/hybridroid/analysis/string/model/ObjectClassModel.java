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

public class ObjectClassModel extends AbstractClassModel{
	private static ObjectClassModel instance;
	
	public static ObjectClassModel getInstance(){
		if(instance == null)
			instance = new ObjectClassModel();
		return instance;
	}
	
	protected void init(){
		methodMap.put(Selector.make("toString(V)Ljava/lang/String;"), new ToString1());
		methodMap.put(Selector.make("toString()Ljava/lang/String;"), new ToString1());
//		methodMap.put(Selector.make("charsetForName(Ljava/lang/String;)Ljava/nio/charset/Charset;"), new ToString1());
		//charsetForName(Ljava/lang/String;)Ljava/nio/charset/Charset;
	}
		
	//toString(V)Ljava/lang/String;
	//toString()Ljava/lang/String;
	class ToString1 implements IMethodModel<Set<IBox>>{
		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int objVar = invokeInst.getUse(0);
			IBox objBox = new VarBox(caller, invokeInst.iindex, objVar);
			if(graph.addEdge(new ToStringOpNode(), def, objBox)){
					boxSet.add(objBox);
			}
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: Object.toString";
		}
	}
}

