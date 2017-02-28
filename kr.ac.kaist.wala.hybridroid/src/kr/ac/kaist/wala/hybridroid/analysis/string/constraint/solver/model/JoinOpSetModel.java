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
package kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.model;

import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.value.IValue;

public class JoinOpSetModel implements IOperationModel {

	private static JoinOpSetModel instance;
	
	public static JoinOpSetModel getInstance(){
		if(instance == null)
			instance = new JoinOpSetModel();
		return instance;
	}
	
	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		if(args.length < 2)
			throw new InternalError("Join operation must have two in-edge at least.");
		
		IValue arg = args[0].clone();
		
		for(int i=1; i<args.length; i++){
			arg = arg.weakUpdate(args[i].clone());
		}
		return arg;
	}
}
