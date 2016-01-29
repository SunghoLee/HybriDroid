package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model;

import java.util.HashSet;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.StringSetDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringTopValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.TopValue;
public class ToStringOpSetModel implements IOperationModel<IValue> {

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		if(args.length != 1)
			throw new InternalError("ToString operation must have only one in-edge.");
		
		IValue arg = args[0];
		
		if(arg instanceof TopValue)
			return StringTopValue.getInstance();
		else if(arg instanceof BotValue){
			return BotValue.getInstance();
		}else{
			//this part is domain specific!
			Set<Object> cons = (Set<Object>)arg.getDomain().getOperator().gamma(arg);
			Set<String> strCons = new HashSet<String>();
			for(Object con : cons){
				strCons.add(String.valueOf(con));
			}
			return StringSetDomain.op().alpha(strCons);
		}
	}
}
