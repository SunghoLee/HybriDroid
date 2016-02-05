package kr.ac.kaist.hybridroid.js.loader;

import com.ibm.wala.cast.ir.translator.ExposedNamesCollector;
import com.ibm.wala.cast.js.loader.JavaScriptLoader;
import com.ibm.wala.cast.js.translator.JSAstTranslator;
import com.ibm.wala.cast.tree.CAstEntity;
import com.ibm.wala.classLoader.ModuleEntry;

public class MultipleJSAstTranslator extends JSAstTranslator {
	
	public MultipleJSAstTranslator(JavaScriptLoader loader) {
		super(loader);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	  public void translate(final CAstEntity N, final ModuleEntry module) {
	    if (DEBUG_TOP)
	      System.err.println(("translating " + module.getName()));
	    // this.inlinedSourceMap = inlinedSourceMap;
	    final ExposedNamesCollector exposedNamesCollector = new ExposedNamesCollector();
	    exposedNamesCollector.run(N);
	    entity2ExposedNames = exposedNamesCollector.getEntity2ExposedNames();
	    // CAstEntity rewrite = (new ExposedParamRenamer(new CAstImpl(),
	    // entity2ExposedNames)).rewrite(N);
	    walkEntities(N, new RootContext(N, module));
	  }
	
	@Override
	public void translate(final CAstEntity N, final WalkContext context) {
	    final ExposedNamesCollector exposedNamesCollector = new ExposedNamesCollector();
	    exposedNamesCollector.run(N);
	    entity2ExposedNames = exposedNamesCollector.getEntity2ExposedNames();
	    walkEntities(N, context);
	  }
}
