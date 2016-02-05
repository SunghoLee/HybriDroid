package kr.ac.kaist.hybridroid.js.loader;

import com.ibm.wala.cast.js.ipa.callgraph.JSSSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.propagation.PointerKeyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;

public class MultipleJSSSAPropagationCallGraphBuilder extends JSSSAPropagationCallGraphBuilder {

	protected MultipleJSSSAPropagationCallGraphBuilder(IClassHierarchy cha, AnalysisOptions options,
			AnalysisCache cache, PointerKeyFactory pointerKeyFactory) {
		super(cha, options, cache, pointerKeyFactory);
		// TODO Auto-generated constructor stub
	}

}
