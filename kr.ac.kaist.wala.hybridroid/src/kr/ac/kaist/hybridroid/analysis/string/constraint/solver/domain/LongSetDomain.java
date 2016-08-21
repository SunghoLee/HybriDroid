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
package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain;

import java.util.HashSet;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IntegerSetDomain.IntegerSetValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.StringSetDomain.StringSetValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.ILongValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.LongBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.LongTopValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.TopValue;

public class LongSetDomain implements ILongDomain {

	private static int MAX_SET_SIZE = 10;  
	private static LongDomainOp OP_INSTANCE;
	
	private static LongSetDomain domain;
	
	public static LongSetDomain getDomain(){
		if(domain == null)
			domain = new LongSetDomain();
		return domain;
	}
	
	private LongSetDomain(){}
	
	public static void setMaxSetSize(int size){
		MAX_SET_SIZE = size;
	}
	
	public static LongDomainOp op(){
		if(OP_INSTANCE == null)
			OP_INSTANCE = new LongDomainOp();
		return OP_INSTANCE;
	}
	
	@Override
	public LongDomainOp getOperator(){
		return op();
	}
	
	static class LongDomainOp implements IDomainOp<Long, Set<Long>>{
		private LongDomainOp(){}
		
		@Override
		public ILongValue alpha(Long cv) {
			// TODO Auto-generated method stub
			return new LongSetValue(cv);
		}
		
		@Override
		public ILongValue alpha(Set<Long> cvs) {
			// TODO Auto-generated method stub
			if(cvs.size() > MAX_SET_SIZE)
				return LongTopValue.getInstance();
			else
				return new LongSetValue(cvs);
		}
		
		@Override
		public Set<Long> gamma(IValue v) {
			// TODO Auto-generated method stub
			if(v instanceof LongBotValue){
				throw new InternalError("Gamma function of String domain operation is applied to LongBotValue.");
			}else if(v instanceof LongTopValue){
				throw new InternalError("Gamma function of String domain operation is applied to LongTopValue.");
			}else if(v instanceof BotValue){
				throw new InternalError("Gamma function of String domain operation is applied to BotValue.");
			}else if(v instanceof TopValue){
				throw new InternalError("Gamma function of String domain operation is applied to TopValue.");
			}else if(v instanceof LongSetValue){
				LongSetValue ssv = (LongSetValue) v;
				return ssv.values;
			}else{
				throw new InternalError("Long domain operation is not compatible to " + v.getClass().getName());
			}
		}
	}
	
	public static class LongSetValue implements ILongValue {
		private Set<Long> values;
		private IDomain domain;
		
		private LongSetValue(){
			values = new HashSet<Long>();
			domain = LongSetDomain.getDomain();
		}
		
		private LongSetValue(Set<Long> values){
			this();
			this.values.addAll(values);
		}
		
		public LongSetValue(Long v){
			this();
			values.add(v);
		}
		
		@Override
		public ILongValue clone() {
			// TODO Auto-generated method stub 
			return new LongSetValue(values);
		}

		@Override
		public IValue weakUpdate(IValue v) {
			// TODO Auto-generated method stub
			if(v instanceof BotValue)
				return this;
			else if(v instanceof ILongValue){
				if(v instanceof LongBotValue)
					return this;
				else if(v instanceof LongTopValue)
					return v;
				else{
					LongSetValue ssv = (LongSetValue) v;
					Set<Long> newValues = new HashSet<Long>();
					newValues.addAll(values);
					newValues.addAll(ssv.values);
					if(MAX_SET_SIZE < newValues.size())
						return LongTopValue.getInstance();
					return new LongSetValue(newValues);
				}
			}else{
				return TopValue.getInstance();
			}
		}

		@Override
		public IDomain getDomain() {
			// TODO Auto-generated method stub
			return domain;
		}
		
		@Override
		public boolean equals(Object o){
			if(o instanceof LongSetValue){
				LongSetValue d = (LongSetValue)o;
				if(d.values.size() == this.values.size()){
					for(Long dd : this.values){
						if(!d.values.contains(dd)){
							return false;
						}
					}
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode(){
			return values.hashCode();
		}
		
		@Override
		public String toString(){
			return "IntegerSet: " + values;
		}
	}

	@Override
	public boolean isInstanceof(IValue v) {
		// TODO Auto-generated method stub
		return (v instanceof LongSetValue);
	}
}
