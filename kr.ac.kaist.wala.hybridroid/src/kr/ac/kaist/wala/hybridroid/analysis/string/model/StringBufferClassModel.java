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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.Selector;

import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.AppendOpNode;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.ToStringOpNode;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.VarBox;

public class StringBufferClassModel extends AbstractClassModel{
	private static StringBufferClassModel instance;
		
	public static StringBufferClassModel getInstance(){
		if(instance == null)
			instance = new StringBufferClassModel();
		return instance;
	}
	
	protected void init(){
		methodMap.put(Selector.make("toString()Ljava/lang/String;"), new ToString());
//		methodMap.put("append", new Append());
	}
		
	//toString()Ljava/lang/String;
	class ToString implements IMethodModel<Set<IBox>>{
		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int useVar = invokeInst.getUse(0);
			IBox use = new VarBox(caller, invokeInst.iindex, useVar);
			if(graph.addEdge(new ToStringOpNode(), def, use))
					boxSet.add(use);
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: StringBuffer.toString()Ljava/lang/String;";
		}
	}
	
	class Append implements IMethodModel<Set<IBox>>{
		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			switch(invokeInst.getNumberOfUses()){
			case 2:
				return arg2(graph, def, caller, invokeInst);
			default : 
				StringModel.setWarning("Unknown StringBuffer append: #arg is " + invokeInst.getNumberOfUses(), true);
			}
			return Collections.emptySet();
		}
		
		private Set<IBox> arg2(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			// TODO Auto-generated method stub
			Set<IBox> boxSet = new HashSet<IBox>();
			int fstUseVar = invokeInst.getUse(0);
			int sndUseVar = invokeInst.getUse(1);
			IBox fstUse = new VarBox(caller, invokeInst.iindex, fstUseVar);
			IBox sndUse = new VarBox(caller, invokeInst.iindex, sndUseVar);
			if(graph.addEdge(new AppendOpNode(), def, fstUse, sndUse)){
					boxSet.add(fstUse);
					boxSet.add(sndUse);
			}
			return boxSet;
		}
		
		@Override
		public String toString(){
			return "Constraint Graph Method Model: StringBuffer.append";
		}
	}
}



