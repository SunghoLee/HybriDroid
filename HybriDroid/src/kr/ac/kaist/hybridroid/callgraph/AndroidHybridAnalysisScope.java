package kr.ac.kaist.hybridroid.callgraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.Atom;

/**
 * AnalysisScope for Android Hybrid application. This class supports DROIDEL Dex
 * scope or original Android platform scope as well as JavaScript scope.
 * 
 * @author Sungho Lee
 */

public class AndroidHybridAnalysisScope extends AnalysisScope {
  private static final Set<Language> languages;

  static private Set<Atom> jsFileNames;
  
  static {
    languages = HashSetFactory.make();

    languages.add(Language.JAVA);
    languages.add(JavaScriptLoader.JS);
    
    jsFileNames = HashSetFactory.make();
  }
  
  public AndroidHybridAnalysisScope() {
    super(languages);
    this.initForJava();
    jsLoaderMap = new HashMap<Atom, ClassLoaderReference>();
    ClassLoaderReference jsLoader = JavaScriptTypes.jsLoader;
    loadersByName.put(JavaScriptTypes.jsLoaderName, jsLoader);
  }

	/**
	 * Find all class files in the directory. It can find classes in the
	 * directory and the sub directories recursively.
	 * 
	 * @param root the root directory file.
	 * @return list of class files in the directory.
	 */
  private static List<File> getAllClassesInDir(File root){
	 List<File> list = new ArrayList<File>();
	 File[] internals = root.listFiles();
	 
	 for(File f : internals){
		 if(f.isDirectory())
			 list.addAll(getAllClassesInDir(f));
		 else if(f.getName().endsWith(".class"))
			 list.add(f);
	 }
	 return list;
  }
  
	/**
	 * Make AnalysisScope for Android hybrid application using DROIDEL.
	 * 
	 * @param classpath the target apk file uri.
	 * @param exclusions the exclusion file.
	 * @param droidelLibpath the DROIDEL library path.
	 * @param androidLib the Android framework directory uri.
	 * @return AnalysisScope for Android hybrid application.
	 * @throws IOException 
	 */
  public static AndroidHybridAnalysisScope setUpDroidelAnalysisScope(URI classpath, String exclusions, String droidelLibpath, URI... androidLib) throws IOException{
	    AndroidHybridAnalysisScope scope;

	    scope = new AndroidHybridAnalysisScope();

	    File exclusionsFile = new File(exclusions);
	    InputStream fs = exclusionsFile.exists() ? new FileInputStream(exclusionsFile) : FileProvider.class.getClassLoader()
	        .getResourceAsStream(exclusionsFile.getName());
	    scope.setExclusions(new FileOfClasses(fs));

	    scope.setLoaderImpl(ClassLoaderReference.Primordial, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");

	    File libdir = new File(droidelLibpath);
	    List<File> classes = getAllClassesInDir(libdir);
	    
	    for (File f : classes) {
	        try {
				scope.addClassFileToScope(ClassLoaderReference.Primordial, f);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidClassFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    for (URI al : androidLib) {
	      try {
	    		  if(al.toString().contains("core_classes.dex"))
	    			  scope.addToScope(ClassLoaderReference.Primordial, DexFileModule.make(new File(al)));
	    	  }catch (Exception e) {
	    		  e.printStackTrace();
	    		  scope.addToScope(ClassLoaderReference.Primordial, new JarFileModule(new JarFile(new File(al))));
	    	  }
	    }
	    
	    scope.setLoaderImpl(ClassLoaderReference.Application, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");
	    
	    scope.addToScope(ClassLoaderReference.Application, DexFileModule.make(new File(classpath)));
	    
	    scope = setUpJsAnalysisScope(scope, classpath);
	    
	    fs.close();
	    
	    return scope;
  }
  
	/**
	 * Make AnalysisScope for Android hybrid application. If you want to use
	 * DROIDEL as front-end, use setUpDroidelAnalysisScope method instead of
	 * this.
	 * 
	 * @param classpath the target apk file uri.
	 * @param exclusions the exclusion file.
	 * @param androidLib the Android framework directory uri.
	 * @return AnalysisScope for Android hybrid application.
	 * @throws IOException
	 */
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
    
    scope.addToScope(ClassLoaderReference.Application, DexFileModule.make(new File(classpath)));
    
    scope = setUpJsAnalysisScope(scope, classpath);
    
    fs.close();
    
    return scope;
  }

  /**
	 * Make AnalysisScope for Android hybrid application. If you want to use
	 * DROIDEL as front-end, use setUpDroidelAnalysisScope method instead of
	 * this.
	 * 
	 * @param classpath the target apk file uri.
	 * @param jsFiles JavaScript files contained in the scope.
	 * @param exclusions the exclusion file.
	 * @param androidLib the Android framework directory uri.
	 * @return AnalysisScope for Android hybrid application.
	 * @throws IOException
	 */
public static AndroidHybridAnalysisScope setUpAndroidHybridAnalysisScope(URI classpath, Set<File> jsFiles, String exclusions, URI... androidLib)
    throws IOException {
  AndroidHybridAnalysisScope scope;

  scope = new AndroidHybridAnalysisScope();

  File exclusionsFile = new File(exclusions);
  InputStream fs = exclusionsFile.exists() ? new FileInputStream(exclusionsFile) : FileProvider.class.getClassLoader()
      .getResourceAsStream(exclusionsFile.getName());
  scope.setExclusions(new FileOfClasses(fs));
  
  scope.setLoaderImpl(ClassLoaderReference.Primordial, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");
  
  for (URI al : androidLib) {
	  if(al.getPath().endsWith(".dex"))
		  scope.addToScope(ClassLoaderReference.Primordial, DexFileModule.make(new File(al)));
	  else if(al.getPath().endsWith(".jar"))
		  scope.addToScope(ClassLoaderReference.Primordial, new JarFileModule(new JarFile(new File(al))));
	  else
		  throw new InternalError("Android library must be either dex or jar file: " + al.getPath());
  }
  
  scope.setLoaderImpl(ClassLoaderReference.Application, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");
  
  scope.addToScope(ClassLoaderReference.Application, DexFileModule.make(new File(classpath)));
  
  scope = setUpJsAnalysisScope(scope, jsFiles);
  
  fs.close();
  
  return scope;
}

	/**
	 * Add JavaScript AnalysisScope for Android hybrid application.
	 * 
	 * @param scope the basic AndroidHybridAnalysisScope.
	 * @param classpath the apk file uri.
	 * @return AndroidHybridAnalysisScope which includes JavaScript scope for the Android hybrid applicartion.
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
  private static AndroidHybridAnalysisScope setUpJsAnalysisScope(AndroidHybridAnalysisScope scope, URI classpath) throws IllegalArgumentException, IOException{
	    File apkFile = new File(classpath);
	    
	    scope.addToScope(scope.getJavaScriptLoader(), JSCallGraphBuilderUtil.getPrologueFile("prologue.js"));
	    
	    
	    JsAnalysisScopeReader jsScopeReader = new JsAnalysisScopeReader(apkFile);
	    
	    //TODO: support HTML file
	    for (int i = 0; i < jsScopeReader.getJSList().size(); i++) {
			URL script = new File(jsScopeReader.getJSList().get(i)).toURI().toURL();
			scope.addToScope(scope.getJavaScriptLoader(), new SourceURLModule(script));
		}
//			for (int i = 0; i < jsScopeReader.getHTMLList().size(); i++) {
//				System.out.println("@file: "+jsScopeReader.getHTMLList().get(i));
//				URL script = new File(jsScopeReader.getHTMLList().get(i)).toURI().toURL();
//				scope.addToScope(scope.getJavaScriptLoader(), new SourceURLModule(script));
//			}
	    	    
	    return scope;
  }
  
  
  private static AndroidHybridAnalysisScope setUpJsAnalysisScope(AndroidHybridAnalysisScope scope, Set<File> jsFiles) throws IllegalArgumentException, IOException{
	    scope.addToScope(scope.getJavaScriptLoader(), JSCallGraphBuilderUtil.getPrologueFile("prologue.js"));
	  
	    //TODO: support HTML file
	    for (File jsFile : jsFiles) {
	    	jsFileNames.add(Atom.findOrCreateAsciiAtom(jsFile.getName()));
			URL script = jsFile.toURI().toURL();
			scope.addToScope(scope.getJavaScriptLoader(), new SourceURLModule(script));
		}
	    return scope;
  }
  
  private Map<Atom, ClassLoaderReference> jsLoaderMap;
  
  public Collection<ClassLoaderReference> getJavaScriptLoaders(){
	  return jsLoaderMap.values();
  }
  
  public ClassLoaderReference getJavaScriptLoader() {
    return getLoader(JavaScriptTypes.jsLoaderName);
  }
  
  public Set<Atom> getJavaScriptNames(){
	  return jsFileNames;
  }
}
