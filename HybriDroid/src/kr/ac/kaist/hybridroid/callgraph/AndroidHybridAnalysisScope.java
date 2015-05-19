package kr.ac.kaist.hybridroid.callgraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Set;
import java.util.jar.JarFile;

import com.ibm.wala.cast.js.loader.JavaScriptLoader;
import com.ibm.wala.cast.js.test.JSCallGraphBuilderUtil;
import com.ibm.wala.cast.js.types.JavaScriptTypes;
import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.classLoader.SourceURLModule;
import com.ibm.wala.dalvik.classLoader.DexFileModule;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.io.FileProvider;

public class AndroidHybridAnalysisScope extends AnalysisScope {
  private static final Set<Language> languages;

  static {
    languages = HashSetFactory.make();

    languages.add(Language.JAVA);
    languages.add(JavaScriptLoader.JS);
  }

  public AndroidHybridAnalysisScope() {
    super(languages);
    this.initForJava();

    ClassLoaderReference jsLoader = JavaScriptTypes.jsLoader;
    loadersByName.put(JavaScriptTypes.jsLoaderName, jsLoader);
  }

  public static AndroidHybridAnalysisScope setUpAndroidAnalysisScope(URI classpath, String exclusions, URI... androidLib)
      throws IOException {
    AndroidHybridAnalysisScope scope;

    scope = new AndroidHybridAnalysisScope();

    File exclusionsFile = new File(exclusions);
    InputStream fs = exclusionsFile.exists() ? new FileInputStream(exclusionsFile) : FileProvider.class.getClassLoader()
        .getResourceAsStream(exclusionsFile.getName());
    scope.setExclusions(new FileOfClasses(fs));

    scope.setLoaderImpl(ClassLoaderReference.Primordial, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");

    for (URI al : androidLib) {
      try {
        scope.addToScope(ClassLoaderReference.Primordial, DexFileModule.make(new File(al)));
      } catch (Exception e) {
        e.printStackTrace();
        scope.addToScope(ClassLoaderReference.Primordial, new JarFileModule(new JarFile(new File(al))));
      }
    }

    scope.setLoaderImpl(ClassLoaderReference.Application, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");
    
    File apkFile = new File(classpath);
    
    scope.addToScope(ClassLoaderReference.Application, DexFileModule.make(apkFile));

    scope.addToScope(scope.getJavaScriptLoader(), JSCallGraphBuilderUtil.getPrologueFile("prologue.js"));
        
    JsAnalysisScopeReader jsScopeReader = new JsAnalysisScopeReader(apkFile);
    
    //TODO: support HTML file
    for (int i = 0; i < jsScopeReader.getJSList().size(); i++) {
		URL script = new File(jsScopeReader.getJSList().get(i)).toURI().toURL();
//    	URL script = new File("/Users/LeeSH/projects/hybridroid/apps/assets/www/name.js").toURI().toURL();
		scope.addToScope(scope.getJavaScriptLoader(), new SourceURLModule(script));
	}
//		for (int i = 0; i < jsScopeReader.getHTMLList().size(); i++) {
//			System.out.println("@file: "+jsScopeReader.getHTMLList().get(i));
//			URL script = new File(jsScopeReader.getHTMLList().get(i)).toURI().toURL();
//			scope.addToScope(scope.getJavaScriptLoader(), new SourceURLModule(script));
//		}
          
    fs.close();
    
    return scope;
  }

  public ClassLoaderReference getJavaScriptLoader() {
    return getLoader(JavaScriptTypes.jsLoaderName);
  }

}
