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
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.TopValue;

public class IndexOfOpSetModel implements IOperationModel {
	private static IndexOfOpSetModel instance;
	
	public static IndexOfOpSetModel getInstance(){
		if(instance == null)
			instance = new IndexOfOpSetModel();
		return instance;
	}
	
	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		if(args.length != 2 && args.length != 3)
			Assertions.UNREACHABLE("IndexOf operation must have two args: " + args);
		
		IValue str = args[0];
		IValue charac = args[1];
		
		if(args.length == 2){
			if(str instanceof TopValue || charac instanceof TopValue)
				return TopValue.getInstance();
			else if(str instanceof BotValue || charac instanceof BotValue)
				return BotValue.getInstance();
			else if(str instanceof StringSetValue && charac instanceof StringSetValue){
				Set<String> ss = (Set<String>)str.getDomain().getOperator().gamma(str);
				Set<String> cc = (Set<String>)charac.getDomain().getOperator().gamma(charac);
				
				Set<Integer> res = new HashSet<Integer>();
				
				for(String s : ss){
					for(String c : cc){
						res.add(s.indexOf(c));
					}
				}
				return IntegerSetDomain.getDomain().getOperator().alpha(res);
			}else if(str instanceof StringSetValue && charac instanceof IntegerSetValue){
				Set<String> ss = (Set<String>)str.getDomain().getOperator().gamma(str);
				Set<Integer> cc = (Set<Integer>)charac.getDomain().getOperator().gamma(charac);
				
				Set<Integer> res = new HashSet<Integer>();
				
				for(String s : ss){
					for(Integer c : cc){
						res.add(s.indexOf(c));
					}
				}
				return IntegerSetDomain.getDomain().getOperator().alpha(res);
			}else
				Assertions.UNREACHABLE("Domain is not correct(arg1: " + str.getClass().getName() + ", arg2: " + str.getClass().getName() + ")");
		}else if(args.length == 3){
			IValue from = args[2];
			if(str instanceof TopValue || charac instanceof TopValue || from instanceof TopValue)
				return TopValue.getInstance();
			else if(str instanceof BotValue || charac instanceof BotValue || from instanceof BotValue)
				return BotValue.getInstance();
			else if(str instanceof StringSetValue && charac instanceof StringSetValue && from instanceof IntegerSetValue){
				Set<String> ss = (Set<String>)str.getDomain().getOperator().gamma(str);
				Set<String> cc = (Set<String>)charac.getDomain().getOperator().gamma(charac);
				Set<Integer> fs = (Set<Integer>)from.getDomain().getOperator().gamma(from);
				Set<Integer> res = new HashSet<Integer>();
				
				for(String s : ss){
					for(String c : cc){
						for(Integer f : fs){
							res.add(s.indexOf(c, f));
						}
					}
				}
				return IntegerSetDomain.getDomain().getOperator().alpha(res);
			}else if(str instanceof StringSetValue && charac instanceof IntegerSetValue && from instanceof IntegerSetValue){
				Set<String> ss = (Set<String>)str.getDomain().getOperator().gamma(str);
				Set<Integer> cc = (Set<Integer>)charac.getDomain().getOperator().gamma(charac);
				Set<Integer> fs = (Set<Integer>)from.getDomain().getOperator().gamma(from);
				Set<Integer> res = new HashSet<Integer>();
				
				for(String s : ss){
					for(Integer c : cc){
						for(Integer f : fs){
							res.add(s.indexOf(c, f));
						}
					}
				}
				return IntegerSetDomain.getDomain().getOperator().alpha(res);
			}else if(str instanceof IntegerSetValue){ //str is 'null' case
				 Set<Integer> ss = ((Set<Integer>)(((IntegerSetValue)str).getDomain().getOperator().gamma(str)));
				 if(ss.size() == 1 && ss.contains(0))
					 return IntegerBotValue.getInstance();
				 else if(CRASH)
					 Assertions.UNREACHABLE("Incorrect args(arg1: " + str.getClass().getName() + ", arg2: " + charac.getClass().getName() + ", arg3: " + from.getClass().getName() + ")");
				 else
					 return BotValue.getInstance();
			}else
				if(CRASH){
					Assertions.UNREACHABLE("Incorrect args(arg1: " + str.getClass().getName() + ", arg2: " + charac.getClass().getName() + ", arg3: " + from.getClass().getName() + ")");
				}else
					return BotValue.getInstance();
		}
		return null;
	}

}
