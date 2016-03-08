package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain;

import java.util.HashSet;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.DoubleSetDomain.DoubleSetValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.StringSetDomain.StringSetValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IIntegerValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IntegerBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IntegerTopValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.TopValue;

public final class IntegerSetDomain implements IIntegerDomain {

	private static int MAX_SET_SIZE = 10;  
	private static IntegerDomainOp OP_INSTANCE;
	
	private static IntegerSetDomain domain;
	
	public static IntegerSetDomain getDomain(){
		if(domain == null)
			domain = new IntegerSetDomain();
		return domain;
	}
	
	private IntegerSetDomain(){}
	
	public static void setMaxSetSize(int size){
		MAX_SET_SIZE = size;
	}
	
	public static IntegerDomainOp op(){
		if(OP_INSTANCE == null)
			OP_INSTANCE = new IntegerDomainOp();
		return OP_INSTANCE;
	}
	
	@Override
	public IntegerDomainOp getOperator(){
		return op();
	}
	
	public static class IntegerDomainOp implements IDomainOp<Integer, Set<Integer>>{
		private IntegerDomainOp(){}
		
		@Override
		public IIntegerValue alpha(Integer cv) {
			// TODO Auto-generated method stub
			return new IntegerSetValue(cv);
		}
		
		@Override
		public IIntegerValue alpha(Set<Integer> cvs) {
			// TODO Auto-generated method stub
			if(cvs.size() > MAX_SET_SIZE)
				return IntegerTopValue.getInstance();
			else
				return new IntegerSetValue(cvs);
		}
		
		@Override
		public Set<Integer> gamma(IValue v) {
			// TODO Auto-generated method stub
			if(v instanceof IntegerBotValue){
				throw new InternalError("Gamma function of String domain operation is applied to StringBotValue.");
			}else if(v instanceof IntegerTopValue){
				throw new InternalError("Gamma function of String domain operation is applied to StringTopValue.");
			}else if(v instanceof BotValue){
				throw new InternalError("Gamma function of String domain operation is applied to BotValue.");
			}else if(v instanceof TopValue){
				throw new InternalError("Gamma function of String domain operation is applied to TopValue.");
			}else if(v instanceof IntegerSetValue){
				IntegerSetValue ssv = (IntegerSetValue) v;
				return ssv.values;
			}else{
				throw new InternalError("String domain operation is not compatible to " + v.getClass().getName());
			}
		}
	}
	
	public static class IntegerSetValue implements IIntegerValue {
		private Set<Integer> values;
		private IDomain domain;
		
		private IntegerSetValue(){
			values = new HashSet<Integer>();
			domain = IntegerSetDomain.getDomain();
		}
		
		private IntegerSetValue(Set<Integer> values){
			this();
			this.values.addAll(values);
		}
		
		public IntegerSetValue(Integer v){
			this();
			values.add(v);
		}
		
		@Override
		public IIntegerValue clone() {
			// TODO Auto-generated method stub 
			return new IntegerSetValue(values);
		}

		@Override
		public IValue weakUpdate(IValue v) {
			// TODO Auto-generated method stub
			if(v instanceof BotValue)
				return this;
			else if(v instanceof IIntegerValue){
				if(v instanceof IntegerBotValue)
					return this;
				else if(v instanceof IntegerTopValue)
					return v;
				else if(v instanceof StringSetValue){
					//this is only for 'null' value.
					return v.weakUpdate(this);
				}else{
					IntegerSetValue ssv = (IntegerSetValue) v;
					Set<Integer> newValues = new HashSet<Integer>();
					newValues.addAll(values);
					newValues.addAll(ssv.values);
					
					if(MAX_SET_SIZE < newValues.size())
						return IntegerTopValue.getInstance();
					return new IntegerSetValue(newValues);
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
			if(o instanceof IntegerSetValue){
				IntegerSetValue d = (IntegerSetValue)o;
				if(d.values.size() == this.values.size()){
					for(Integer dd : this.values){
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
		return (v instanceof IntegerSetValue);
	}
}
