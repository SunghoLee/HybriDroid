package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model;

import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.util.debug.Assertions;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IntegerSetDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IntegerSetDomain.IntegerSetValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.StringSetDomain.StringSetValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IntegerBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IntegerTopValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.TopValue;

public class LengthOpSetModel implements IOperationModel {

	private static LengthOpSetModel instance;
	
	public static LengthOpSetModel getInstance(){
		if(instance == null)
			instance = new LengthOpSetModel();
		return instance;
	}
	
	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		if(args.length != 1)
			Assertions.UNREACHABLE("LengthOp must have only one arg: " + args);
		
		IValue str = args[0];
		
		if(str instanceof TopValue){
			return IntegerTopValue.getInstance();
		}else if(str instanceof BotValue){
			return IntegerBotValue.getInstance();
		}else if(str instanceof StringSetValue){
			Set<String> ss = (Set<String>) str.getDomain().getOperator().gamma(str);
			Set<Integer> res = new HashSet<Integer>();
			for(String s : ss){
				res.add(s.length());
			}
			return IntegerSetDomain.getDomain().getOperator().alpha(res);
		}else if(str instanceof IntegerSetValue){ //str is 'null' case
			 Set<Integer> ss = ((Set<Integer>)(((IntegerSetValue)str).getDomain().getOperator().gamma(str)));
			 if(ss.size() == 1 && ss.contains(0))
				 return IntegerBotValue.getInstance();
			 else if(CRASH)
				 Assertions.UNREACHABLE("Incorrect args(arg1: " + str.getClass().getName() + ")");
			 else
				 return BotValue.getInstance();
		}else
			if(CRASH)
				Assertions.UNREACHABLE("Argument type is not correct(" + str.getClass().getName() + ")");
			else
				return BotValue.getInstance();
			
		return null;
	}

}
