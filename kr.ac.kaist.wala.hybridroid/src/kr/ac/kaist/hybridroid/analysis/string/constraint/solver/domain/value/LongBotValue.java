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
package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value;

public class LongBotValue extends BotValue implements ILongValue {
	private static LongBotValue instance;
	public static LongBotValue getInstance(){
		if(instance == null)
			instance = new LongBotValue();
		return instance;
	}
	
	private LongBotValue(){}
	
	@Override
	public IValue clone() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public IValue weakUpdate(IValue v) {
		// TODO Auto-generated method stub
		if(v instanceof ILongValue)
			return v;
		else if(v instanceof BotValue)
			return this;
		else
			return TopValue.getInstance();
	}
}
