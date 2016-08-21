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
package kr.ac.kaist.hybridroid.callgraph;

import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dalvik.classLoader.DexIMethod;
import com.ibm.wala.dalvik.classLoader.DexIRFactory;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ssa.DefaultIRFactory;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.IRFactory;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSAOptions;

public class HybridIRFactory extends DefaultIRFactory {

	private IRFactory<IMethod> jsIRFactory;
	private IRFactory<IMethod> dexIRFactory;
	
	public HybridIRFactory(){
		 jsIRFactory = AstIRFactory.makeDefaultFactory();
		 dexIRFactory = new DexIRFactory();
		 SSAInvokeInstruction a;
	}

	@Override
	public IR makeIR(IMethod method, Context c, SSAOptions options)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		if(method instanceof DexIMethod)
			return dexIRFactory.makeIR(method, c, options);
		else
			return jsIRFactory.makeIR(method, c, options);
	}

	@Override
	public boolean contextIsIrrelevant(IMethod method) {
		// TODO Auto-generated method stub
		if(method instanceof DexIMethod)
			return dexIRFactory.contextIsIrrelevant(method);
		else
			return jsIRFactory.contextIsIrrelevant(method);
	}
	
	
}
