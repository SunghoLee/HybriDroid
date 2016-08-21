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
package kr.ac.kaist.hybridroid.analysis.string.constraint;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.AppendOpSetModel;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model.IOperationModel;

public class AppendOpNode implements IOperatorNode {
	static private IOperationModel m;
	
	static{
		m = AppendOpSetModel.getInstance();
	}

	public AppendOpNode(){}
	
	@Override
	public String toString(){
		return "append";
	}

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		return m.apply(args);
	}
}
