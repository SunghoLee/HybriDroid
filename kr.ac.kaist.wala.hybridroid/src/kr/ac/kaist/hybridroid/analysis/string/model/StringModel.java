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
package kr.ac.kaist.hybridroid.analysis.string.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;

import kr.ac.kaist.hybridroid.analysis.resource.AndroidResourceAnalysis;
import kr.ac.kaist.hybridroid.analysis.string.resource.model.ContextClassModel;

public class StringModel {
	
	private static Map<IClass, IClassModel> classMap;
	private static Set<String> warnings;
	private static AndroidResourceAnalysis ra;
	private static String region;
	
	static{
		warnings = new HashSet<String>();
		classMap = new HashMap<IClass, IClassModel>();
	}
	
	public static void setResourceAnalysis(AndroidResourceAnalysis ra, String region){
		StringModel.ra = ra;
		StringModel.region = region;
	}
	
	public static void setWarning(String msg, boolean print){
		warnings.add(msg);
		if(print){
//			System.out.println("[Warning] " + msg);
		}
	}
	
	public static Set<String> getWarnings(){
		return warnings;
	}
	
	public static void init(IClassHierarchy cha){
		classMap.put(cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, "Ljava/lang/String")), StringClassModel.getInstance());
		classMap.put(cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, "Ljava/lang/StringBuilder")), StringBuilderClassModel.getInstance());
		classMap.put(cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, "Ljava/lang/StringBuffer")), StringBufferClassModel.getInstance());
		classMap.put(cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/content/Context")), ContextClassModel.getInstance());
		
		if(ra != null)
			classMap.put(cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/content/Context")), ContextClassModel.getInstance(ra, region));
		else
			classMap.put(cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/content/Context")), ContextClassModel.getInstance());
		
//		classMap.put(cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, "Llibcore/net/UriCodec")), UriCodecClassModel.getInstance());
		classMap.put(cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, "Ljava/net/URI")), UriClassModel.getInstance());
		classMap.put(cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, "Ljava/lang/Object")), ObjectClassModel.getInstance());
	}
	
	public static boolean isClassModeled(IClass tClass){
		return classMap.containsKey(tClass);
	}

	public static IClassModel getModeledClass(IClass tClass){
		if(isClassModeled(tClass)){
			return classMap.get(tClass);
		}
		return null;
	}
	
	public static IMethodModel getTargetMethod(IClass tClass, Selector tMethodSelector){
		IClassModel mClass = getModeledClass(tClass);
		if(mClass != null){
			IMethodModel mm= mClass.getMethod(tMethodSelector);
//			if(mm == null)
//				System.err.println("#class: " + tClass);
			return mm;
		}
		return null;
	}
}
