package kr.ac.kaist.hybridroid.analysis.string.model;

import java.util.HashMap;
import java.util.Map;

import com.ibm.wala.types.Selector;

abstract public class AbstractClassModel implements IClassModel {
	protected Map<Selector, IMethodModel> methodMap;
	
	protected AbstractClassModel(){
		methodMap = new HashMap<Selector, IMethodModel>();
		init();
	}
	
	abstract protected void init();
	
	@Override
	public IMethodModel getMethod(Selector mSelector) {
		// TODO Auto-generated method stub
		if(methodMap.containsKey(mSelector))
			return methodMap.get(mSelector);
		System.err.println("Unkwon method: " + mSelector);
		return null;
	}

}
