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
package kr.ac.kaist.hybridroid.types;

import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;

/**
 * @author Sungho Lee
 *
 */
public class HybriDroidTypes {
	//ClassTypes
	public static TypeReference JAVASCRIPT_INTERFACE_ANNOTATION = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/webkit/JavascriptInterface");
	public static TypeReference WEBVIEW_APP_CLASS = TypeReference.findOrCreate(ClassLoaderReference.Application, "Landroid/webkit/WebView");
	public static TypeReference WEBVIEW_PRI_CLASS = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/webkit/WebView");
	
	//MethodSelector
	public static Selector ADDJAVASCRIPTINTERFACE_SELECTOR = Selector.make("addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V");
	public static Selector LOADURL1_SELECTOR = Selector.make("addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V");
	public static Selector SETWEBVIEWCLIENT_SELECTOR = Selector.make("setWebViewClient(Landroid/webkit/WebViewClient;)V");
	//setWebViewClient(Landroid/webkit/WebViewClient;)V
	
	//MethodTypes
	public static MethodReference ADDJAVASCRIPTINTERFACE_OF_WEBVIEW = MethodReference.findOrCreate(WEBVIEW_APP_CLASS, ADDJAVASCRIPTINTERFACE_SELECTOR);
	public static MethodReference LOADURL1_OF_WEBVIEW = MethodReference.findOrCreate(WEBVIEW_APP_CLASS, LOADURL1_SELECTOR);
}
