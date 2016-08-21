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

public class UnaryOpNode implements IOperatorNode {
	public static enum UnaryOperator{
		MINUS{
			@Override
			public String toString(){
				return "-";
			}
		},
		NOT{
			@Override
			public String toString(){
				return "!";
			}
		}
	};
	
	private UnaryOperator o;
	
	public UnaryOpNode(UnaryOperator o){
		this.o = o;
	}
	
	@Override
	public String toString(){
		return o.toString();
	}

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		return null;
	}
}
