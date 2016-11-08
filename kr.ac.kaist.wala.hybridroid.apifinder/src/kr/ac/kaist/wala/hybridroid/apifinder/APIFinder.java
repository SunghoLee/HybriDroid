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
package kr.ac.kaist.wala.hybridroid.apifinder;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.dalvik.classLoader.DexFileModule;
import com.ibm.wala.dalvik.classLoader.DexIRFactory;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.io.FileProvider;
import kr.ac.kaist.hybridroid.types.HybriDroidTypes;
import kr.ac.kaist.hybridroid.utils.LocalFileReader;
import kr.ac.kaist.wala.hybridroid.types.bridge.BridgeInfo;
import kr.ac.kaist.wala.hybridroid.types.bridge.ClassInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * APIFinder is used for finding possible Android Java APIs that are accessible
 * from JavaScript. Also, it finds bridge object names to reveal which bridge
 * object could be accessed from JavaScript to access which Android Java APIs.
 * All these works are done syntactically, so the results could be
 * over-approximated or under-approximated. If the results are
 * under-approximated, APIFinder gives some error messages for you.
 *
 * @author Sungho Lee
 */
public class APIFinder {
    public APIFinder() {

    }

    private IClassHierarchy makeClassHierarchy(String apkPath, String libPath, String exclusions) throws IOException, ClassHierarchyException {
        AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();
        File exclusionsFile = new File(exclusions);
        InputStream fs = exclusionsFile.exists() ? new FileInputStream(exclusionsFile)
                : FileProvider.class.getClassLoader().getResourceAsStream(exclusionsFile.getName());
        scope.setExclusions(new FileOfClasses(fs));
        scope.setLoaderImpl(ClassLoaderReference.Primordial, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");

        URI al = new File(libPath).toURI();
        if (al.getPath().endsWith(".dex"))
            scope.addToScope(ClassLoaderReference.Primordial, DexFileModule.make(new File(al)));
        else if (al.getPath().endsWith(".jar"))
            scope.addToScope(ClassLoaderReference.Primordial, new JarFileModule(new JarFile(new File(al))));
        else
            throw new InternalError("Android library must be either dex or jar file: " + al.getPath());

        scope.setLoaderImpl(ClassLoaderReference.Application, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");
        if (apkPath.endsWith(".apk"))
            scope.addToScope(ClassLoaderReference.Application, DexFileModule.make(new File(apkPath)));
        else if (apkPath.endsWith(".zip") || apkPath.endsWith(".jar"))
            scope.addToScope(ClassLoaderReference.Application, new JarFileModule(new JarFile(apkPath)));
        IClassHierarchy cha = ClassHierarchy.make(scope);

        return cha;
    }

    public Set<BridgeInfo> findAccessibleApis(String apkPath, Properties property) throws IOException, ClassHierarchyException {
        Set<BridgeInfo> bridgeSet = new HashSet<BridgeInfo>();
        IClassHierarchy cha = makeClassHierarchy(apkPath, LocalFileReader.androidJar(property).getPath(), CallGraphTestUtil.REGRESSION_EXCLUSIONS);
        DexIRFactory irFactory = new DexIRFactory();
        SSACache irCache = new SSACache(irFactory);

        for (Iterator<IClass> ic = cha.iterator(); ic.hasNext(); ) {
            IClass c = ic.next();
//			System.out.println("C: " + c);
            for (IMethod m : c.getAllMethods()) {
                IR ir = irCache.findOrCreateIR(m, Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
//				System.out.println("\tM: " + m);
                if (ir != null) {
                    for (SSAInstruction inst : ir.getInstructions()) {
                        if (inst != null && inst instanceof SSAAbstractInvokeInstruction) {
                            SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) inst;

                            if (invokeInst.getDeclaredTarget().getSelector().equals(HybriDroidTypes.ADDJAVASCRIPTINTERFACE_SELECTOR)) {
                                TypeReference declClassType = invokeInst.getDeclaredTarget().getDeclaringClass();
                                IClass declClass = cha.lookupClass(declClassType);
                                IMethod targetMethod = declClass.getMethod(HybriDroidTypes.ADDJAVASCRIPTINTERFACE_SELECTOR);
                                if (targetMethod.getDeclaringClass().getReference().equals(HybriDroidTypes.WEBVIEW_PRI_CLASS) || targetMethod.getDeclaringClass().getReference().equals(HybriDroidTypes.WEBVIEW_APP_CLASS)) {
                                    SymbolTable symTab = ir.getSymbolTable();
                                    int objvar = invokeInst.getUse(1);
                                    int strvar = invokeInst.getUse(2);

                                    String bridgeName = "UNKNOWN";
                                    if (symTab.isStringConstant(strvar))
                                        bridgeName = symTab.getStringValue(strvar);

                                    TypeReference type = findLocalType(new DefUse(ir), ir.getBasicBlockForInstruction(invokeInst), objvar);

                                    IClass klass = cha.lookupClass(type);
                                    bridgeSet.add(new BridgeInfo(bridgeName, new ClassInfo(klass)));

                                }
                            }
                        }
                    }
                }
            }
        }

        return bridgeSet;
    }


//	private boolean hasMethod(IClass c, Selector s){
//		
//	}

    private TypeReference findLocalType(DefUse du, ISSABasicBlock seed, int usevar) {
        IMethod m = seed.getMethod();
        if (m.isStatic()) {
            if (m.getNumberOfParameters() >= usevar)
                return m.getParameterType(usevar);
        } else {
            if ((m.getNumberOfParameters() + 1) >= usevar)
                return m.getParameterType(usevar);
        }

        SSAInstruction inst = du.getDef(usevar);

        if (inst instanceof SSANewInstruction) {
            SSANewInstruction newInst = (SSANewInstruction) inst;
            return newInst.getConcreteType();
        } else if (inst instanceof SSAGetInstruction) {
            SSAGetInstruction getInst = (SSAGetInstruction) inst;
            return getInst.getDeclaredFieldType();
        }

        return null;
    }
}
