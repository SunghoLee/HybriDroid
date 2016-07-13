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
package kr.ac.kaist.hybridroid.analysis.string.resource.model;

import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.Selector;

import kr.ac.kaist.hybridroid.analysis.resource.AndroidResourceAnalysis;
import kr.ac.kaist.hybridroid.analysis.string.constraint.AssignOpNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstType;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.hybridroid.analysis.string.model.AbstractClassModel;
import kr.ac.kaist.hybridroid.analysis.string.model.IMethodModel;

public class ContextClassModel extends AbstractClassModel{
	public static boolean DEBUG = false;
	private static ContextClassModel instance;

	private AndroidResourceAnalysis ra;
	private String region;
	
	protected ContextClassModel(){}
	
	public static ContextClassModel getInstance(AndroidResourceAnalysis ra, String region) {
		if (instance == null || instance.hasAndroidResourceAnalysis() == false){
			instance = new ContextClassModel(ra, region);
		}
		return instance;
	}

	public static ContextClassModel getInstance() {
		if (instance == null)
			instance = new ContextClassModel();
		return instance;
	}
	
	private boolean hasAndroidResourceAnalysis(){
		if(ra == null)
			return false;
		return true;
	}
	
	private ContextClassModel(AndroidResourceAnalysis ra, String region) {
		this.ra = ra;
		this.region = region;
	}
	
	private String getString(CGNode node, int var){
		SymbolTable symTab = node.getIR().getSymbolTable();
		if(ra != null && symTab.isConstant(var)){
			int addr = (Integer)symTab.getConstantValue(var);
			String value = null;
			if(region != null)
				value = ra.getRegionString(addr, region);
			else
				value = ra.getCommonString(addr);
			
			if(value != null)
				return value;
			else{
				if(DEBUG)
					System.err.println("[Warning] the undefined string resource for " + addr);
				return "RESOURCE";
			}
		}
		
		if(DEBUG){
			System.err.println("" + (ra != null));
			System.err.println("[Warning] resource access by using unconstant value.");
		}
		// if the variable does not have constant value, we do not calculate it; just return "RESOURCE" string value.
		return "RESOURCE";
	}
	
	protected void init() {
		methodMap.put(Selector.make("getString(I)Ljava/lang/String"), new GetString1());
	}

	//getString(I)Ljava/lang/String
	class GetString1 implements IMethodModel<Set<IBox>> {

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int useVar = invokeInst.getUse(1);
			String value = getString(caller, useVar);
			IBox useBox = new ConstBox(caller, value, ConstType.STRING);
			
			if (graph.addEdge(new AssignOpNode(), def, useBox))
				boxSet.add(useBox);
			return boxSet;
		}
		
		@Override
		public String toString() {
			return "Constraint Graph Method Model: Context.getString(I)Ljava/lang/String";
		}

	}
	
	//?
	class GetString2 implements IMethodModel<Set<IBox>>{
		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int useVar = invokeInst.getUse(1);
			String value = getString(caller, useVar);
			IBox useBox = new ConstBox(caller, value, ConstType.STRING);
			System.out.println("" + useBox);
			if (graph.addEdge(new AssignOpNode(), def, useBox)){
				boxSet.add(useBox);
			}
			return boxSet;
		}
		
		@Override
		public String toString() {
			return "Constraint Graph Method Model: Context.getString?";
		}
	}
}
