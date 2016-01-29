package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model;

import java.util.HashSet;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringTopValue;

public class AppendOpSetModel implements IOperationModel<IValue> {

	@SuppressWarnings("unchecked")
	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		if(args.length != 2)
			throw new InternalError("Append operation must have only two in-edge.");
		
		System.out.println("args[0]: " + args[0]);
		System.out.println("args[1]: " + args[1]);
		
		IValue absFront = (IValue) args[0];
		IValue absBack = (IValue) args[1];
		
		if(absFront instanceof BotValue || absBack instanceof BotValue)
			return BotValue.getInstance();
		else if(absFront instanceof StringTopValue || absBack instanceof StringTopValue)
			return StringTopValue.getInstance();
		else if(absFront instanceof StringBotValue || absBack instanceof StringBotValue)
			throw new InternalError("Argument is bottom.");
		else{ // this part is domain specific!
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
		}
	}
}
