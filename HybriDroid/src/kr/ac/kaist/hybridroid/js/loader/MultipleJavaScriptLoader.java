package kr.ac.kaist.hybridroid.js.loader;

import com.ibm.wala.cast.ir.translator.TranslatorToIR;
import com.ibm.wala.cast.js.loader.JavaScriptLoader;
import com.ibm.wala.cast.js.translator.JavaScriptTranslatorFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;

public class MultipleJavaScriptLoader extends JavaScriptLoader {

	public MultipleJavaScriptLoader(IClassHierarchy cha, JavaScriptTranslatorFactory translatorFactory) {
	    super(cha, translatorFactory, null);
	  }

	@Override
	protected TranslatorToIR initTranslator() {
		// TODO Auto-generated method stub
		return new MultipleJSAstTranslator(this);
	}
}
