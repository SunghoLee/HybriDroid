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

import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.DoubleSetDomain.DoubleSetValue;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.IDomain;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.IntegerSetDomain.IntegerSetValue;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.LongSetDomain.LongSetValue;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.StringSetDomain;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.StringSetDomain.StringSetValue;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.value.*;

import java.util.HashSet;
import java.util.Set;

public class AppendOpSetModel implements IOperationModel {

	private static AppendOpSetModel instance;
	
	public static AppendOpSetModel getInstance(){
		if(instance == null)
			instance = new AppendOpSetModel();
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		if(args.length != 2)
			throw new InternalError("Append operation must have only two in-edge.");
		
		IValue absFront = (IValue) args[0];
		IValue absBack = (IValue) args[1];
		
		if(absFront instanceof BotValue || absBack instanceof BotValue)
			return StringBotValue.getInstance();
		else if(absFront instanceof TopValue || absBack instanceof TopValue)
			return StringTopValue.getInstance();
		else if(absFront instanceof StringSetValue && absBack instanceof StringSetValue){ // this part is domain specific!
			IDomain domain = absFront.getDomain();
			Set<String> front = (Set<String>) domain.getOperator().gamma(absFront);
			Set<String> back = (Set<String>) domain.getOperator().gamma(absBack);
			Set<String> res = new HashSet<String>();
			for(String f : front){
				for(String b : back){
					res.add(f + b);
				}
			}
			return domain.getOperator().alpha(res);
		}else if(absFront instanceof StringSetValue && absBack instanceof IntegerSetValue){ // this part is domain specific!;
			Set<String> front = (Set<String>) absFront.getDomain().getOperator().gamma(absFront);
			Set<Integer> back = (Set<Integer>) absBack.getDomain().getOperator().gamma(absBack);
			Set<String> res = new HashSet<String>();
			
			if(back.size() == 1 && back.contains(0)) //back is null
				return StringBotValue.getInstance();
			
			for(String f : front){
				for(Integer b : back){
					res.add(f + b);
				}
			}
			return StringSetDomain.op().alpha(res);
		}else if(absFront instanceof StringSetValue && absBack instanceof DoubleSetValue){ // this part is domain specific!;
			Set<String> front = (Set<String>) absFront.getDomain().getOperator().gamma(absFront);
			Set<Double> back = (Set<Double>) absBack.getDomain().getOperator().gamma(absBack);
			Set<String> res = new HashSet<String>();
			
			for(String f : front){
				for(Double b : back){
					res.add(f + b);
				}
			}
			return StringSetDomain.op().alpha(res);
		}else if(absFront instanceof StringSetValue && absBack instanceof LongSetValue){ // this part is domain specific!;
			Set<String> front = (Set<String>) absFront.getDomain().getOperator().gamma(absFront);
			Set<Long> back = (Set<Long>) absBack.getDomain().getOperator().gamma(absBack);
			Set<String> res = new HashSet<String>();
			
			if(back.size() == 1 && back.contains(0)) //back is null
				return StringBotValue.getInstance();
			
			for(String f : front){
				for(Long b : back){
					res.add(f + b);
				}
			}
			return StringSetDomain.op().alpha(res);
		}else if(absFront instanceof IntegerSetValue && absBack instanceof StringSetValue){ // this part is domain specific!;
			Set<Integer> front = (Set<Integer>) absFront.getDomain().getOperator().gamma(absFront);
			Set<String> back = (Set<String>) absBack.getDomain().getOperator().gamma(absBack);
			Set<String> res = new HashSet<String>();
			
			if(front.size() == 1 && front.contains(0)) //front is null
				return StringBotValue.getInstance();
			
			for(Integer f : front){
				for(String b : back){
					res.add(f + b);
				}
			}
			return StringSetDomain.op().alpha(res);
		}else
			if(CRASH)
				Assertions.UNREACHABLE("incorrect args(arg1: " + absFront.getClass().getName() + ", arg2: " + absBack.getClass().getName() + ")");
			else
				return BotValue.getInstance();
		return null;
	}
}
