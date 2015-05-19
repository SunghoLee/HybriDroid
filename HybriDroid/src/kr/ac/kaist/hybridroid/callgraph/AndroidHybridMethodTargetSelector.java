package kr.ac.kaist.hybridroid.callgraph;

import java.util.Map;

import kr.ac.kaist.hybridroid.pointer.MockupClass;

import com.ibm.wala.cast.ipa.callgraph.CrossLanguageMethodTargetSelector;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.MethodTargetSelector;
import com.ibm.wala.util.strings.Atom;

public class AndroidHybridMethodTargetSelector extends
		CrossLanguageMethodTargetSelector {
	
	public AndroidHybridMethodTargetSelector(
			Map<Atom, MethodTargetSelector> languageSelectors) {
		super(languageSelectors);
	}
	
	@Override
	public IMethod getCalleeTarget(CGNode caller, CallSiteReference site,
			IClass receiver) {

		if(receiver instanceof MockupClass){
			return ((MockupClass)receiver).getMethod();
		}else{
			return super.getCalleeTarget(caller, site, receiver);
		}
		
	}
	
}
