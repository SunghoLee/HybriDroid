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

import com.ibm.wala.cast.js.types.JavaScriptTypes;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;


public class AndroidJavaJavaScriptTypeMap {
	
	private final static TypeReference JAVA_PRI_Z = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Z");
	private final static TypeReference JAVA_APP_Z = TypeReference.findOrCreate(ClassLoaderReference.Application, "Z");
	
	private final static TypeReference JAVA_PRI_I = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "I");
	private final static TypeReference JAVA_APP_I = TypeReference.findOrCreate(ClassLoaderReference.Application, "I");
	
	private final static TypeReference JAVA_PRI_V = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "V");
	private final static TypeReference JAVA_APP_V = TypeReference.findOrCreate(ClassLoaderReference.Application, "V");
	
	private final static TypeReference JAVA_PRI_F = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "F");
	private final static TypeReference JAVA_APP_F = TypeReference.findOrCreate(ClassLoaderReference.Application, "F");
	
	private final static TypeReference JAVA_PRI_D = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "D");
	private final static TypeReference JAVA_APP_D = TypeReference.findOrCreate(ClassLoaderReference.Application, "D");
	
	private final static TypeReference JAVA_PRI_B = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "B");
	private final static TypeReference JAVA_APP_B = TypeReference.findOrCreate(ClassLoaderReference.Application, "B");
	
	private final static TypeReference JAVA_PRI_BOOLEAN = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/Boolean");
	private final static TypeReference JAVA_APP_BOOLEAN = TypeReference.findOrCreate(ClassLoaderReference.Application, "Ljava/lang/Boolean");
	
	private final static TypeReference JAVA_PRI_VOID = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/Void");
	private final static TypeReference JAVA_APP_VOID = TypeReference.findOrCreate(ClassLoaderReference.Application, "Ljava/lang/Void");
	
	private final static TypeReference JAVA_PRI_INTEGER = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/Integer");
	private final static TypeReference JAVA_APP_INTEGER = TypeReference.findOrCreate(ClassLoaderReference.Application, "Ljava/lang/Integer");
	
	private final static TypeReference JAVA_PRI_FLOAT = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/Float");
	private final static TypeReference JAVA_APP_FLOAT = TypeReference.findOrCreate(ClassLoaderReference.Application, "Ljava/lang/Float");
	
	private final static TypeReference JAVA_PRI_DOUBLE = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/Double");
	private final static TypeReference JAVA_APP_DOUBLE = TypeReference.findOrCreate(ClassLoaderReference.Application, "Ljava/lang/Double");
	
	private final static TypeReference JAVA_PRI_STRING = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/String");
	private final static TypeReference JAVA_APP_STRING = TypeReference.findOrCreate(ClassLoaderReference.Application, "Ljava/lang/String");
	
	private final static TypeReference JS_BOOLEAN = TypeReference.findOrCreate(JavaScriptTypes.jsLoader, "LBoolean");
	
	private final static TypeReference JS_NUMBER = TypeReference.findOrCreate(JavaScriptTypes.jsLoader, "LNumber");
	
	private final static TypeReference JS_STRING = TypeReference.findOrCreate(JavaScriptTypes.jsLoader, "LString");
	
	private final static TypeReference JS_OBJECT = TypeReference.findOrCreate(JavaScriptTypes.jsLoader, "LObject");
	
	private final static TypeReference JS_ARRAY = TypeReference.findOrCreate(JavaScriptTypes.jsLoader, "LArray");
	
	public static TypeReference js2JavaTypeConvert(TypeReference jsTr, TypeReference declaredTr){
		TypeReference javaTr = null;
		
		if(jsTr.equals(JS_BOOLEAN)){
			if(declaredTr.equals(JAVA_PRI_B) || declaredTr.equals(JAVA_APP_B) || 
					declaredTr.equals(JAVA_PRI_BOOLEAN) || declaredTr.equals(JAVA_APP_BOOLEAN) || 
					declaredTr.equals(JAVA_PRI_STRING) || declaredTr.equals(JAVA_APP_STRING))
				javaTr = declaredTr;
			
		}else if(jsTr.equals(JS_NUMBER)){
			if(declaredTr.equals(JAVA_PRI_I) || declaredTr.equals(JAVA_APP_I) ||
					declaredTr.equals(JAVA_PRI_F) || declaredTr.equals(JAVA_APP_F) ||
					declaredTr.equals(JAVA_PRI_D) || declaredTr.equals(JAVA_APP_D) ||
					declaredTr.equals(JAVA_PRI_INTEGER) || declaredTr.equals(JAVA_APP_INTEGER) ||
					declaredTr.equals(JAVA_PRI_DOUBLE) || declaredTr.equals(JAVA_APP_DOUBLE) ||
					declaredTr.equals(JAVA_PRI_FLOAT) || declaredTr.equals(JAVA_APP_FLOAT) ||
					declaredTr.equals(JAVA_PRI_STRING) || declaredTr.equals(JAVA_APP_STRING))
				javaTr = declaredTr;
			
		}else if(jsTr.equals(JS_STRING)){
			if(declaredTr.equals(JAVA_PRI_STRING) || declaredTr.equals(JAVA_APP_STRING))
				javaTr = declaredTr;
		}
		
		return javaTr;
	}
	
	public static TypeReference js2JavaTypeConvertNoImplcitConversion(TypeReference jsTr, TypeReference declaredTr){
		TypeReference javaTr = null;
		
		if(jsTr.equals(JS_BOOLEAN)){
			if(declaredTr.equals(JAVA_PRI_B) || declaredTr.equals(JAVA_APP_B) || 
					declaredTr.equals(JAVA_PRI_BOOLEAN) || declaredTr.equals(JAVA_APP_BOOLEAN))
				javaTr = declaredTr;
			
		}else if(jsTr.equals(JS_NUMBER)){
			if(declaredTr.equals(JAVA_PRI_I) || declaredTr.equals(JAVA_APP_I) ||
					declaredTr.equals(JAVA_PRI_F) || declaredTr.equals(JAVA_APP_F) ||
					declaredTr.equals(JAVA_PRI_D) || declaredTr.equals(JAVA_APP_D) ||
					declaredTr.equals(JAVA_PRI_INTEGER) || declaredTr.equals(JAVA_APP_INTEGER) ||
					declaredTr.equals(JAVA_PRI_DOUBLE) || declaredTr.equals(JAVA_APP_DOUBLE) ||
					declaredTr.equals(JAVA_PRI_FLOAT) || declaredTr.equals(JAVA_APP_FLOAT))
				javaTr = declaredTr;
			
		}else if(jsTr.equals(JS_STRING)){
			if(declaredTr.equals(JAVA_PRI_STRING) || declaredTr.equals(JAVA_APP_STRING))
				javaTr = declaredTr;
		}else if(jsTr.equals(declaredTr))
			javaTr = declaredTr;
		
		return javaTr;
	}
	
	public static TypeReference java2JsTypeConvert(TypeReference javaTr){
		TypeReference jsTr = null;
		
		if(javaTr.equals(JAVA_PRI_I) || javaTr.equals(JAVA_APP_I) ||
				javaTr.equals(JAVA_PRI_INTEGER) || javaTr.equals(JAVA_APP_INTEGER) ||
				javaTr.equals(JAVA_PRI_F) || javaTr.equals(JAVA_APP_F) ||
				javaTr.equals(JAVA_PRI_FLOAT) || javaTr.equals(JAVA_APP_FLOAT) ||
				javaTr.equals(JAVA_PRI_D) || javaTr.equals(JAVA_APP_D) ||
				javaTr.equals(JAVA_PRI_DOUBLE) || javaTr.equals(JAVA_APP_DOUBLE))
			jsTr = JS_NUMBER;
		
		else if(javaTr.equals(JAVA_PRI_B) || javaTr.equals(JAVA_APP_B) ||
				javaTr.equals(JAVA_PRI_BOOLEAN) || javaTr.equals(JAVA_APP_BOOLEAN) ||
				javaTr.equals(JAVA_PRI_Z) || javaTr.equals(JAVA_APP_Z))
			jsTr = JS_BOOLEAN;
		
		else if(javaTr.equals(JAVA_PRI_STRING) || javaTr.equals(JAVA_APP_STRING))
			jsTr = JS_STRING;
		
		return jsTr;
	}
		
	public static boolean isJs2JavaTypeCompatibleNoImplicitConversion(TypeReference jsType, TypeReference javaType){
		if(js2JavaTypeConvertNoImplcitConversion(jsType, javaType) != null)
			return true;
		else
			return false;
	}
	
	public static boolean isJs2JavaTypeCompatible(TypeReference jsType, TypeReference javaType){
		if(js2JavaTypeConvert(jsType, javaType) != null)
			return true;
		else
			return false;
	}

	public static boolean isJava2JsTypeCompatible(TypeReference javaType){
		if(java2JsTypeConvert(javaType) != null)
			return true;
		else
			return false;
	}
}
