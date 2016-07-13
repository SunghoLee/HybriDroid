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
package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;

public class UriParseOpSetModel implements IOperationModel{
	private static UriParseOpSetModel instance;
	
	public static UriParseOpSetModel getInstance(){
		if(instance == null)
			instance = new UriParseOpSetModel();
		return instance;
	}
	
	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		if(args.length != 1)
			throw new InternalError("UriParse method must have only one in-edge.");
		
		// just pass the string value.
		return args[0].clone();
	}
}
