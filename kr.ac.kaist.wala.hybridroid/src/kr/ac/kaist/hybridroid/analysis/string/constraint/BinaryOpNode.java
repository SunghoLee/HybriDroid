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
package kr.ac.kaist.hybridroid.analysis.string.constraint;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;

public class BinaryOpNode implements IOperatorNode {
	public static enum BinaryOperator{
		PLUS{
			@Override
			public String toString(){
				return "+";
			}
		},
		MINUS{
			@Override
			public String toString(){
				return "-";
			}
		},
		MULTIPLY{
			@Override
			public String toString(){
				return "X";
			}
		},
		DIVIDE{
			@Override
			public String toString(){
				return "/";
			}
		},
		AND{
			@Override
			public String toString(){
				return "&&";
			}
		},
		OR{
			@Override
			public String toString(){
				return "||";
			}
		}
	};
	
	private BinaryOperator o;
	
	public BinaryOpNode(BinaryOperator o){
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
