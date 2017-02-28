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
package kr.ac.kaist.wala.hybridroid.utils;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.types.ClassLoaderReference;

public class LoaderUtils {
    
    public static boolean fromLoader(CGNode node, ClassLoaderReference clr) {
        IClass declClass = node.getMethod().getDeclaringClass();

        ClassLoaderReference nodeClRef =
                declClass.getClassLoader().getReference();

        return nodeClRef.equals(clr);
    }
    
    public static boolean fromLoader(IMethod method, ClassLoaderReference clr) {
        IClass declClass = method.getDeclaringClass();

        ClassLoaderReference nodeClRef =
                declClass.getClassLoader().getReference();

        return nodeClRef.equals(clr);
    }
    
    public static boolean fromLoader(IClass declClass, ClassLoaderReference clr) {
        ClassLoaderReference nodeClRef =
                declClass.getClassLoader().getReference();

        return nodeClRef.equals(clr);
    }
}
