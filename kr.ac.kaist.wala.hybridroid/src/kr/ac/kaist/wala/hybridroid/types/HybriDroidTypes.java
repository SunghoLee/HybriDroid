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
package kr.ac.kaist.wala.hybridroid.types;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.types.annotations.Annotation;

/**
 * Created by leesh on 05/01/2017.
 */
public class HybriDroidTypes {
    public static TypeReference WEBVIEW_APP_TYPEREFERENCE = TypeReference.findOrCreate(ClassLoaderReference.Application, "Landroid/webkit/WebView");
    public static TypeReference WEBVIEW_PRI_TYPEREFERENCE = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/webkit/WebView");
    public static TypeReference JAVASCRIPTINTERFACE_ANNOTATION_PRI_TYPEREFERENCE = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/webkit/JavascriptInterface");
    public static TypeReference JAVASCRIPTINTERFACE_ANNOTATION_APP_TYPEREFERENCE = TypeReference.findOrCreate(ClassLoaderReference.Application, "Landroid/webkit/JavascriptInterface");

    public static Selector ADDJAVASCRIPTINTERFACE_SELECTOR = Selector.make("addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V");

    public static MethodReference ADDJAVASCRIPTINTERFACE_APP_METHODREFERENCE = (WEBVIEW_APP_TYPEREFERENCE != null)? MethodReference.findOrCreate(WEBVIEW_APP_TYPEREFERENCE, ADDJAVASCRIPTINTERFACE_SELECTOR) : null;
    public static MethodReference ADDJAVASCRIPTINTERFACE_PRI_METHODREFERENCE = (WEBVIEW_PRI_TYPEREFERENCE != null)? MethodReference.findOrCreate(WEBVIEW_PRI_TYPEREFERENCE, ADDJAVASCRIPTINTERFACE_SELECTOR) : null;

    public static Selector SETWEBVIEWCLIENT_SELECTOR = Selector.make("setWebViewClient(Landroid/webkit/WebViewClient;)V");
    public static TypeReference WEBVIEW_APP_CLASS = TypeReference.findOrCreate(ClassLoaderReference.Application, "Landroid/webkit/WebView");
    public static TypeReference WEBVIEW_PRI_CLASS = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/webkit/WebView");

    /**
     * Check which a method has the 'JavascriptInterface' annotation or not.
     * @param m a target method
     * @return true if the method has the 'JavascriptInterface' annotation, otherwise false.
     */
    public static boolean hasJavascriptInterfaceAnnotation(IMethod m) {
        if(m.getAnnotations() != null){
            for (Annotation ann : m.getAnnotations()) {
                TypeReference annTr= ann.getType();
                if (((HybriDroidTypes.JAVASCRIPTINTERFACE_ANNOTATION_PRI_TYPEREFERENCE == null)? false : HybriDroidTypes.JAVASCRIPTINTERFACE_ANNOTATION_PRI_TYPEREFERENCE.equals(annTr)) ||
                        (HybriDroidTypes.JAVASCRIPTINTERFACE_ANNOTATION_APP_TYPEREFERENCE == null)? false: HybriDroidTypes.JAVASCRIPTINTERFACE_ANNOTATION_APP_TYPEREFERENCE.equals(annTr))
                    return true;
            }
            return false;
        }else
            return true;
    }
}
