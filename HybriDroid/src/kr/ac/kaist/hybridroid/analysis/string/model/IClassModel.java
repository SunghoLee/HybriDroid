package kr.ac.kaist.hybridroid.analysis.string.model;

import com.ibm.wala.types.Selector;

public interface IClassModel {
	public IMethodModel getMethod(Selector mSelector);
}
