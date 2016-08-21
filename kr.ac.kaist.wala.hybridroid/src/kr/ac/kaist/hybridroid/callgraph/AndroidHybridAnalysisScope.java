/*******************************************************************************
* Copyright (c) 2016 IBM Corporation and KAIST.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* KAIST - initial API and implementation
*******************************************************************************/
package kr.ac.kaist.hybridroid.callgraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import com.ibm.wala.cast.ir.translator.TranslatorToCAst.Error;
import com.ibm.wala.cast.js.html.DefaultSourceExtractor;
import com.ibm.wala.cast.js.html.WebUtil;
import com.ibm.wala.cast.js.loader.JavaScriptLoader;
import com.ibm.wala.cast.js.test.JSCallGraphBuilderUtil;
import com.ibm.wala.cast.js.types.JavaScriptTypes;
import com.ibm.wala.cast.loader.CAstAbstractLoader;
import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.classLoader.SourceModule;
import com.ibm.wala.classLoader.SourceURLModule;
import com.ibm.wala.dalvik.classLoader.DexFileModule;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.debug.UnimplementedError;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.Atom;

import kr.ac.kaist.hybridroid.util.file.FileWriter;

/**
 * AnalysisScope for Android Hybrid application. This class supports DROIDEL Dex
 * scope or original Android platform scope as well as JavaScript scope.
 * 
 * @author Sungho Lee
 */

public class AndroidHybridAnalysisScope extends AnalysisScope {
	
	public static boolean DEBUG = false;
	
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
	 * Make AnalysisScope for Android hybrid application. If you want to use
	 * DROIDEL as front-end, use setUpDroidelAnalysisScope method instead of
	 * this.
	 * 
	 * @param classpath
	 *            the target apk file uri.
	 * @param jsFiles
	 *            JavaScript files contained in the scope.
	 * @param exclusions
	 *            the exclusion file.
	 * @param androidLib
	 *            the Android framework directory uri.
	 * @return AnalysisScope for Android hybrid application.
	 * @throws IOException
	 */
	public static AndroidHybridAnalysisScope setUpAndroidHybridAnalysisScope(String dir, URI classpath, Set<URL> htmls,
			String exclusions, URI... androidLib) throws IOException {
		AndroidHybridAnalysisScope scope;

		scope = new AndroidHybridAnalysisScope();

		File exclusionsFile = new File(exclusions);
		InputStream fs = exclusionsFile.exists() ? new FileInputStream(exclusionsFile)
				: FileProvider.class.getClassLoader().getResourceAsStream(exclusionsFile.getName());
		scope.setExclusions(new FileOfClasses(fs));

		scope.setLoaderImpl(ClassLoaderReference.Primordial, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");

		for (URI al : androidLib) {
			if (al.getPath().endsWith(".dex"))
				scope.addToScope(ClassLoaderReference.Primordial, DexFileModule.make(new File(al)));
			else if (al.getPath().endsWith(".jar"))
				scope.addToScope(ClassLoaderReference.Primordial, new JarFileModule(new JarFile(new File(al))));
			else
				throw new InternalError("Android library must be either dex or jar file: " + al.getPath());
		}

		scope.setLoaderImpl(ClassLoaderReference.Application, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");

		scope.addToScope(ClassLoaderReference.Application, DexFileModule.make(new File(classpath)));

		scope = setUpJsAnalysisScope(dir, scope, htmls);

		fs.close();

		return scope;
	}

	private static Map<Atom, Atom> nameConvertMap = new HashMap<Atom, Atom>();

	private static AndroidHybridAnalysisScope setUpJsAnalysisScope(String dir, AndroidHybridAnalysisScope scope, Set<URL> htmls)
			throws IllegalArgumentException, IOException {

		JavaScriptLoader.addBootstrapFile(WebUtil.preamble);
		scope.addToScope(scope.getJavaScriptLoader(), JSCallGraphBuilderUtil.getPrologueFile("prologue.js"));
		scope.addToScope(scope.getJavaScriptLoader(), JSCallGraphBuilderUtil.getPrologueFile("preamble.js"));
		
		for (URL url : htmls) {
			try {
				File f = WebUtil.extractScriptFromHTML(url, DefaultSourceExtractor.factory).snd;
				scope.addToScope(scope.getJavaScriptLoader(), new SourceURLModule(f.toURI().toURL()));

				String jspath = f.getCanonicalPath();
				addScopeMap(Atom.findOrCreateAsciiAtom(url.toString()), Atom.findOrCreateAsciiAtom(
						jspath.substring(jspath.lastIndexOf(File.separator) + 1, jspath.length())));
				if(DEBUG)
					System.err.println("#Loaded html: " + url.getFile());
			} catch (Error | RuntimeException e) {// | UnimplementedError |
													// Error e) {
				String path = url.getPath();
				SourceModule dummy = new SourceURLModule(FileWriter
						.makeHtmlFile(dir,
								path.substring(path.lastIndexOf(File.separator) + 1, path.length() - 1), "")
						.toURI().toURL());
				String dummypath = dummy.getName();
				if(DEBUG)
					System.err.println("make dummy: " + dummypath);
				addScopeMap(Atom.findOrCreateAsciiAtom(url.toString()), Atom.findOrCreateAsciiAtom(dummypath.substring(dummypath.lastIndexOf(File.separator) + 1)));
				scope.addToScope(scope.getJavaScriptLoader(), dummy);
			}
		}

		return scope;
	}

	private Map<Atom, ClassLoaderReference> jsLoaderMap;

	private static void addScopeMap(Atom f, Atom s){
		nameConvertMap.put(f, s);
		if(DEBUG)
			System.out.println("[scope] " + f + " => " + s);
	}
	
	public Collection<ClassLoaderReference> getJavaScriptLoaders() {
		return jsLoaderMap.values();
	}

	public ClassLoaderReference getJavaScriptLoader() {
		return getLoader(JavaScriptTypes.jsLoaderName);
	}

	public Collection<Atom> getJavaScriptNames() {
		return nameConvertMap.values();
	}

	public Atom htmlToJs(String html) {
		return nameConvertMap.get(Atom.findOrCreateAsciiAtom(html));
	}
}
