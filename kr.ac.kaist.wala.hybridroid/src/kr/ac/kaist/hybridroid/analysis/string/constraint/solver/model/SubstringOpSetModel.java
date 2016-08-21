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
package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model;

import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.util.debug.Assertions;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IntegerSetDomain.IntegerSetValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.StringSetDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.StringSetDomain.StringSetValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IntegerBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringTopValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.TopValue;

public class SubstringOpSetModel implements IOperationModel {

	private static SubstringOpSetModel instance;
	public static SubstringOpSetModel getInstance(){
		if(instance == null)
			instance = new SubstringOpSetModel();
		return instance;
	}
	
	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		if(args.length != 2 && args.length != 3)
			Assertions.UNREACHABLE("SubstringOp must have three args: " + args);
		
		IValue str = args[0];
		IValue begin = args[1];
		
		if(str instanceof IntegerSetValue){ //str is 'null' case
			 Set<Integer> ss = ((Set<Integer>)(((IntegerSetValue)str).getDomain().getOperator().gamma(str)));
			 if(ss.size() == 1 && ss.contains(0))
				 return IntegerBotValue.getInstance();
			 else if(CRASH)
				 Assertions.UNREACHABLE("Incorrect args(arg1: " + str.getClass().getName() + ", arg2: " + begin.getClass().getName() + ")");
			 else
				 return BotValue.getInstance();
		}
		
		if(args.length == 2){
			if(str instanceof TopValue || begin instanceof TopValue)
				return StringTopValue.getInstance();
			else if(str instanceof BotValue || begin instanceof BotValue)
				return StringBotValue.getInstance();
			else if(str instanceof StringSetValue && begin instanceof IntegerSetValue){
				Set<String> ss = (Set<String>) str.getDomain().getOperator().gamma(str);
				Set<Integer> is = (Set<Integer>) begin.getDomain().getOperator().gamma(begin);
				Set<String> res = new HashSet<String>();
				for(String s : ss){
					for(Integer i : is){
						if(s.length() >= i && i > -1)
							res.add(s.substring(i));
					}
				}
				return StringSetDomain.op().alpha(res);
			}else
				Assertions.UNREACHABLE("Incorrect args types(arg1: " + str.getClass().getName() + ", arg2: " + begin.getClass().getName());
		}else if(args.length == 3){
			IValue end = args[2];
			if(str instanceof TopValue || begin instanceof TopValue || end instanceof TopValue)
				return StringTopValue.getInstance();
			else if(str instanceof BotValue || begin instanceof BotValue || end instanceof BotValue)
				return StringBotValue.getInstance();
			else if(str instanceof StringSetValue && begin instanceof IntegerSetValue && end instanceof IntegerSetValue){
				Set<String> ss = (Set<String>) str.getDomain().getOperator().gamma(str);
				Set<Integer> bis = (Set<Integer>) begin.getDomain().getOperator().gamma(begin);
				Set<Integer> eis = (Set<Integer>) end.getDomain().getOperator().gamma(end);
				Set<String> res = new HashSet<String>();
				for(String s : ss){
					for(Integer bi : bis){
						for(Integer ei : eis){
							if(s.length() >= ei && bi <= ei && bi > -1)
								res.add(s.substring(bi, ei));	
						}
					}
				}
				return StringSetDomain.op().alpha(res);
			}else
				if(CRASH)
					Assertions.UNREACHABLE("Incorrect args types(arg1: " + str.getClass().getName() + ", arg2: " + begin.getClass().getName() + ", arg3: " + end.getClass().getName());
				else
					return BotValue.getInstance();
		}
		return null;
	}

}
