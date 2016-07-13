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

import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.util.debug.Assertions;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IntegerSetDomain.IntegerSetValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.StringSetDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.StringSetDomain.StringSetValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringTopValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.TopValue;
public class ToStringOpSetModel implements IOperationModel {

	private static ToStringOpSetModel instance;
	
	public static ToStringOpSetModel getInstance(){
		if(instance == null)
			instance = new ToStringOpSetModel();
		return instance;
	}
	
	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		if(args.length != 1)
			throw new InternalError("ToString operation must have only one in-edge.");
		
		IValue arg = args[0];
		
		if(arg instanceof TopValue)
			return StringTopValue.getInstance();
		else if(arg instanceof BotValue){
			return StringBotValue.getInstance();
		}else if(arg instanceof StringSetValue){
			//this part is domain specific!
			Set<Object> cons = (Set<Object>)arg.getDomain().getOperator().gamma(arg);
			Set<String> strCons = new HashSet<String>();
			for(Object con : cons){
				strCons.add(String.valueOf(con));
			}
			return StringSetDomain.op().alpha(strCons);
		}else if(arg instanceof IntegerSetValue){
			Set<Integer> cons = (Set<Integer>)arg.getDomain().getOperator().gamma(arg);
			Set<String> strCons = new HashSet<String>();
			for(Integer con : cons){
				strCons.add(String.valueOf(con));
			}
			return StringSetDomain.op().alpha(strCons);
		}else
			if(CRASH)
				Assertions.UNREACHABLE("incorrect arg(arg: " + arg.getClass().getName() + ")");
			else
				return BotValue.getInstance();
		return null;
	}
}
