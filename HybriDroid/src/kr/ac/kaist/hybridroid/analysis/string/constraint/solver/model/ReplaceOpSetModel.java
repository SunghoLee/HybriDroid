package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model;

import java.util.HashSet;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IStringValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringTopValue;

public class ReplaceOpSetModel implements IOperationModel<IValue> {

	@SuppressWarnings("unchecked")
	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		if(args.length != 3)
			throw new InternalError("Replace operation must have three in-edge.");
		
		IStringValue absStr = (IStringValue)args[0].clone();
		IStringValue absTarget = (IStringValue)args[1].clone();
		IStringValue absSubst = (IStringValue)args[2].clone();
		
		if(absStr instanceof BotValue || absTarget instanceof BotValue || absSubst instanceof BotValue)
			return BotValue.getInstance();
		else if(absStr instanceof StringTopValue || absTarget instanceof StringTopValue || absSubst instanceof StringTopValue)
			return StringTopValue.getInstance();
		else if(absStr instanceof StringBotValue || absTarget instanceof StringBotValue || absSubst instanceof StringBotValue)
			throw new InternalError("Argument is bottom.");
		else{
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
		}
	}
}
