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

import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.Selector;

import kr.ac.kaist.hybridroid.analysis.string.constraint.AppendOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.AssignOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstType;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ToStringOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.VarBox;

public class StringBuilderClassModel extends AbstractClassModel{
	private static StringBuilderClassModel instance;
	
	public static StringBuilderClassModel getInstance(){
		if(instance == null)
			instance = new StringBuilderClassModel();
		return instance;
	}
		
	protected void init(){
		methodMap.put(Selector.make("<init>()V"), new Init1());
		methodMap.put(Selector.make("<init>(I)V"), new Init1());
		methodMap.put(Selector.make("<init>(Ljava/lang/String;)V"), new Init2());
		methodMap.put(Selector.make("toString()Ljava/lang/String;"), new ToString());
		methodMap.put(Selector.make("append(Ljava/lang/String;)Ljava/lang/StringBuilder;"), new Append());
		methodMap.put(Selector.make("append(Ljava/lang/Object;)Ljava/lang/StringBuilder;"), new Append());
		methodMap.put(Selector.make("append(I)Ljava/lang/StringBuilder;"), new Append());
		methodMap.put(Selector.make("append(C)Ljava/lang/StringBuilder;"), new Append());
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
			return "Constraint Graph Method Model: StringBuilder.toString()Ljava/lang/String;";
		}
	}
	
	//append(Ljava/lang/String;)Ljava/lang/StringBuilder
	//append(I)Ljava/lang/StringBuilder
	//append(Ljava/lang/Object;)Ljava/lang/StringBuilder;
	class Append implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
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
			return "Constraint Graph Method Model: StringBuilder.append(Ljava/lang/String;)Ljava/lang/StringBuilder";
		}
	}
	
	//<init>(I)V
	//<init>()V
	class Init1 implements IMethodModel<Set<IBox>>{
		
		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			IBox use = new ConstBox(caller, "", ConstType.STRING);
			if(graph.addEdge(new AssignOpNode(), def, use))
					boxSet.add(use);
			
			return boxSet;
		}
			
		@Override
		public String toString(){
			return "Constraint Graph Method Model: StringBuilder.<init>()V";
		}
	}

	//<init>(Ljava/lang/String;)V
	class Init2 implements IMethodModel<Set<IBox>>{
		
		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int useVar = invokeInst.getUse(1);
			IBox use = new VarBox(caller, invokeInst.iindex, useVar);
			if(graph.addEdge(new AssignOpNode(), def, use))
					boxSet.add(use);
			
			return boxSet;
		}
	
		@Override
		public String toString(){
			return "Constraint Graph Method Model: StringBuilder.<init>(Ljava/lang/String;)V";
		}
	}
}



