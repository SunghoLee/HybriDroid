package kr.ac.kaist.hybridroid.js.loader;

import com.ibm.wala.cast.js.loader.JavaScriptLoader;
import com.ibm.wala.cast.js.translator.JavaScriptTranslatorFactory;
import com.ibm.wala.cast.tree.rewrite.CAstRewriterFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;

public class MultipleJavaScriptLoader extends JavaScriptLoader {

	public MultipleJavaScriptLoader(IClassHierarchy cha, JavaScriptTranslatorFactory translatorFactory,
			CAstRewriterFactory preprocessor) {
		super(cha, translatorFactory, preprocessor);
		// TODO Auto-generated constructor stub
	}

}
