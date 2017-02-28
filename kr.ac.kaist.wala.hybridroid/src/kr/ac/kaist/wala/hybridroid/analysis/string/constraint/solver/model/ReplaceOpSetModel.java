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

import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.util.debug.Assertions;

import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.IDomain;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.IntegerSetDomain.IntegerSetValue;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.StringSetDomain.StringSetValue;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.value.BotValue;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.value.IntegerBotValue;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.value.StringBotValue;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.value.StringTopValue;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.value.TopValue;

public class ReplaceOpSetModel implements IOperationModel{

	private static ReplaceOpSetModel instance;
	
	public static ReplaceOpSetModel getInstance(){
		if(instance == null)
			instance = new ReplaceOpSetModel();
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		if(args.length != 3)
			throw new InternalError("Replace operation must have three in-edge.");
		
		IValue absStr = (IValue)args[0].clone();
		IValue absTarget = (IValue)args[1].clone();
		IValue absSubst = (IValue)args[2].clone();
		
		if(absStr instanceof BotValue || absTarget instanceof BotValue || absSubst instanceof BotValue)
			return StringBotValue.getInstance();
		else if(absStr instanceof TopValue || absTarget instanceof TopValue || absSubst instanceof TopValue)
			return StringTopValue.getInstance();
		else if(absStr instanceof StringSetValue && absTarget instanceof StringSetValue && absSubst instanceof StringSetValue){
			// this part is domain specific!
			IDomain domain = absStr.getDomain();
			Set<String> str = (Set<String>)domain.getOperator().gamma(absStr);
			Set<String> target = (Set<String>)domain.getOperator().gamma(absTarget);
			Set<String> subst = (Set<String>)domain.getOperator().gamma(absSubst);
			Set<String> res = new HashSet<String>();
			
			for(String s : str){
				for(String t : target){
					for(String su : subst){
						res.add(s.replace(t, su));
					}
				}
			}
			
			return domain.getOperator().alpha(res);
		}else if(absStr instanceof IntegerSetValue){ //str is 'null' case
			 Set<Integer> ss = ((Set<Integer>)(((IntegerSetValue)absStr).getDomain().getOperator().gamma(absStr)));
			 if(ss.size() == 1 && ss.contains(0))
				 return IntegerBotValue.getInstance();
			 else if(CRASH)
				 Assertions.UNREACHABLE("Incorrect args(arg1: " + absStr.getClass().getName() + ", arg2: " + absTarget.getClass().getName() + ", arg3: " + absSubst.getClass().getName() + ")");
			 else
				 return BotValue.getInstance();
		}else
			if(CRASH)
				Assertions.UNREACHABLE("incorrect args(arg1: " + absStr.getClass().getName() + ", arg2: " + absTarget.getClass().getName() + ", arg3: " + absSubst.getClass().getName() + ")");
			else
				return BotValue.getInstance();
		return null;
	}
}
