package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain;

import java.util.HashSet;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.BooleanDomain.BooleanValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.DoubleBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.DoubleTopValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IDoubleValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.TopValue;

public final class DoubleSetDomain implements IDoubleDomain {

	private static int MAX_SET_SIZE = 10;  
	private static DoubleDomainOp OP_INSTANCE;
	
	private static DoubleSetDomain domain;
	
	public static DoubleSetDomain getDomain(){
		if(domain == null)
			domain = new DoubleSetDomain();
		return domain;
	}
	
	private DoubleSetDomain(){}
	
	public static void setMaxSetSize(int size){
		MAX_SET_SIZE = size;
	}
	
	public static DoubleDomainOp op(){
		if(OP_INSTANCE == null)
			OP_INSTANCE = new DoubleDomainOp();
		return OP_INSTANCE;
	}
	
	@Override
	public DoubleDomainOp getOperator(){
		return op();
	}
	
	static class DoubleDomainOp implements IDomainOp<Double, Set<Double>>{
		private DoubleDomainOp(){}
		@Override
		public IDoubleValue alpha(Double cv) {
			// TODO Auto-generated method stub
			return new DoubleSetValue(cv);
		}

		@Override
		public IDoubleValue alpha(Set<Double> cvs) {
			// TODO Auto-generated method stub
			if(cvs.size() > MAX_SET_SIZE)
				return DoubleTopValue.getInstance();
			else
				return new DoubleSetValue(cvs);
		}
		
		@Override
		public Set<Double> gamma(IValue v) {
			// TODO Auto-generated method stub
			if(v instanceof DoubleBotValue){
				throw new InternalError("Gamma function of String domain operation is applied to StringBotValue.");
			}else if(v instanceof DoubleTopValue){
				throw new InternalError("Gamma function of String domain operation is applied to StringTopValue.");
			}else if(v instanceof BotValue){
				throw new InternalError("Gamma function of String domain operation is applied to BotValue.");
			}else if(v instanceof TopValue){
				throw new InternalError("Gamma function of String domain operation is applied to TopValue.");
			}else if(v instanceof DoubleSetValue){
				DoubleSetValue ssv = (DoubleSetValue) v;
				return ssv.values;
			}else{
				throw new InternalError("String domain operation is not compatible to " + v.getClass().getName());
			}
		}
	}
	
	public static class DoubleSetValue implements IDoubleValue {
		private Set<Double> values;
		private DoubleSetDomain domain;
		
		private DoubleSetValue(){
			values = new HashSet<Double>();
			domain = DoubleSetDomain.getDomain();
		}
		
		private DoubleSetValue(Set<Double> values){
			this();
			this.values.addAll(values);
		}
		
		public DoubleSetValue(Double v){
			this();
			values.add(v);
		}
		
		@Override
		public IDoubleValue clone() {
			// TODO Auto-generated method stub 
			return new DoubleSetValue(values);
		}

		@Override
		public IValue weakUpdate(IValue v) {
			// TODO Auto-generated method stub
			if(v instanceof BotValue)
				return this;
			else if(v instanceof IDoubleValue){
				if(v instanceof DoubleBotValue)
					return this;
				else if(v instanceof DoubleTopValue)
					return v;
				else{
					DoubleSetValue ssv = (DoubleSetValue) v;
					Set<Double> newValues = new HashSet<Double>();
					newValues.addAll(values);
					newValues.addAll(ssv.values);
					
					if(newValues.size() > MAX_SET_SIZE)
						return DoubleTopValue.getInstance();
					return new DoubleSetValue(newValues);
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
			if(o instanceof DoubleSetValue){
				DoubleSetValue d = (DoubleSetValue)o;
				if(d.values.size() == this.values.size()){
					for(Double dd : this.values){
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
	}

	@Override
	public boolean isInstanceof(IValue v) {
		// TODO Auto-generated method stub
		return (v instanceof DoubleSetValue);
	}
}
