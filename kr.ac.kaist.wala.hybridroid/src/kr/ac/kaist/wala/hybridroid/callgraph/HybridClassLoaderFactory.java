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
package kr.ac.kaist.wala.hybridroid.callgraph;

import com.ibm.wala.cast.js.loader.JavaScriptLoader;
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory;
import com.ibm.wala.cast.js.translator.JavaScriptTranslatorFactory;
import com.ibm.wala.cast.js.types.JavaScriptTypes;
import com.ibm.wala.classLoader.ClassLoaderFactoryImpl;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.SetOfClasses;

import java.io.IOException;

public class HybridClassLoaderFactory extends ClassLoaderFactoryImpl {

  private final JavaScriptTranslatorFactory jsTranslatorFactory;

  public HybridClassLoaderFactory(
	  JavaScriptTranslatorFactory jsTranslatorFactory,
	  SetOfClasses exclusions)
  {
    super(exclusions);
    this.jsTranslatorFactory = jsTranslatorFactory;
  }

  public HybridClassLoaderFactory() {
	  this(new CAstRhinoTranslatorFactory(), null);
  }

  @Override
  protected IClassLoader 
    makeNewClassLoader(ClassLoaderReference classLoaderReference, 
		       IClassHierarchy cha,
		       IClassLoader parent,
		       AnalysisScope scope) 
      throws IOException 
  {
      if (classLoaderReference.equals(JavaScriptTypes.jsLoader)) {	
	JavaScriptLoader L = new JavaScriptLoader(cha, jsTranslatorFactory);
	L.init(scope.getModules(classLoaderReference));
	return L;

      } else {
	return super.makeNewClassLoader(classLoaderReference, cha, parent, scope);
      }
  }

}
