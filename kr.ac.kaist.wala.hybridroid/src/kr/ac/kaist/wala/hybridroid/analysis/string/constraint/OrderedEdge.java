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
package kr.ac.kaist.wala.hybridroid.analysis.string.constraint;

public class OrderedEdge extends AbstractConstraintEdge {
	private int order;
	
	public OrderedEdge(IConstraintNode from, IConstraintNode to, int order){
		super(from, to);
		this.order = order;
	}
	
	public int getOrder(){
		return order;
	}
	
	@Override
	public String toString(){
		return from() + " -> " + to() + " (order: " + order + ")";
	}
}
