package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain;

import java.util.HashSet;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.BooleanDomain.BooleanValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IntegerSetDomain.IntegerSetValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IStringValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringTopValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.TopValue;

public final class StringSetDomain implements IStringDomain {

	private static int MAX_SET_SIZE = 10;  
	private static StringDomainOp OP_INSTANCE;
	
	public static StringSetDomain domain;
	
	public static StringSetDomain getDomain(){
		if(domain == null)
			domain = new StringSetDomain();
		return domain;
	}
	
	private StringSetDomain(){}
	
	public static void setMaxSetSize(int size){
		MAX_SET_SIZE = size;
	}
	
	public static StringDomainOp op(){
		if(OP_INSTANCE == null)
			OP_INSTANCE = new StringDomainOp();
		return OP_INSTANCE;
	}
	
	@Override
	public StringDomainOp getOperator(){
		return op();
	}
	
	public static class StringDomainOp implements IDomainOp<String, Set<String>>{
		private StringDomainOp(){}
		@Override
		public IStringValue alpha(String cv) {
			// TODO Auto-generated method stub
			return new StringSetValue(cv);
		}
		
		@Override
		public IStringValue alpha(Set<String> cvs){
			if(cvs.size() > MAX_SET_SIZE)
				return StringTopValue.getInstance();
			else
				return new StringSetValue(cvs);
		}
		
		@Override
		public Set<String> gamma(IValue v) {
			// TODO Auto-generated method stub
			if(v instanceof StringBotValue){
				throw new InternalError("Gamma function of String domain operation is applied to StringBotValue.");
			}else if(v instanceof StringTopValue){
				throw new InternalError("Gamma function of String domain operation is applied to StringTopValue.");
			}else if(v instanceof BotValue){
				throw new InternalError("Gamma function of String domain operation is applied to BotValue.");
			}else if(v instanceof TopValue){
				throw new InternalError("Gamma function of String domain operation is applied to TopValue.");
			}else if(v instanceof StringSetValue){
				StringSetValue ssv = (StringSetValue) v;
				return ssv.values;
			}else{
				throw new InternalError("String domain operation is not compatible to " + v.getClass().getName());
			}
		}
	}
	
	public static class StringSetValue implements IStringValue {
		private Set<String> values;
		private IDomain domain;
		
		private StringSetValue(){
			values = new HashSet<String>();
			domain = StringSetDomain.getDomain();
		}
		
		private StringSetValue(Set<String> values){
			this();
			this.values.addAll(values);
		}
		
		private StringSetValue(String v){
			this();
			values.add(v);
		}
		
		@Override
		public IStringValue clone() {
			// TODO Auto-generated method stub 
			return new StringSetValue(values);
		}

		@Override
		public IValue weakUpdate(IValue v) {
			// TODO Auto-generated method stub
			if(v instanceof StringSetValue){
				StringSetValue ssv = (StringSetValue) v;
				values.addAll(ssv.values);
				if(MAX_SET_SIZE < values.size())
					return StringTopValue.getInstance();
				return this;
			}else if(v instanceof IntegerSetValue){
				// this case is only for 'null'.
				IntegerSetValue isv = (IntegerSetValue) v;
				Set<Integer> ints = (Set<Integer>) isv.getDomain().getOperator().gamma(isv);
				if(ints.size() == 1 && ints.iterator().next() == 0){
					// we handle with the null value as 'null' string value.
					values.add("null");
					return this;
				}else
					return TopValue.getInstance();
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
		public String toString(){
			return "StringSet: " + values.toString();
		}
	}

	@Override
	public boolean isInstanceof(IValue v) {
		// TODO Auto-generated method stub
		return (v instanceof StringSetValue);
	}

}
