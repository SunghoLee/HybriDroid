package kr.ac.kaist.hybridroid.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.cast.js.types.JavaScriptTypes;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.propagation.ConcreteTypeKey;
import com.ibm.wala.ipa.callgraph.propagation.ConstantKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;


public class AndroidJavaJavaScriptTypeMap {
	
	private static TypeReference JAVA_Z = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Z");
	private static IClass JAVA_CLASS_Z;		
	private static TypeReference JAVA_I = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "I");
	private static IClass JAVA_CLASS_I;		
	
	private static TypeReference JAVA_BOOLEAN = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/Boolean");
	private static IClass JAVA_CLASS_BOOLEAN;		
	
	private static TypeReference JAVA_VOID = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/Void");
	private static IClass JAVA_CLASS_VOID;		
	
	private static TypeReference JAVA_INTEGER = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/Integer");
	private static IClass JAVA_CLASS_INTEGER;
	
	private static TypeReference JAVA_FLOAT = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/Float");
	private static IClass JAVA_CLASS_FLOAT;
	
	private static TypeReference JAVA_DOUBLE = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/Double");
	private static IClass JAVA_CLASS_DOUBLE;
	
	private static TypeReference JAVA_STRING = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/String");
	private static IClass JAVA_CLASS_STRING;
	
	private static TypeReference JAVA_APP_STRING = TypeReference.findOrCreate(ClassLoaderReference.Application, "Ljava/lang/String");
	private static IClass JAVA_CLASS_APP_STRING;
	
	private static TypeReference JS_BOOLEAN = TypeReference.findOrCreate(JavaScriptTypes.jsLoader, "LBoolean");
	private static IClass JS_CLASS_BOOLEAN;
	
	private static TypeReference JS_NUMBER = TypeReference.findOrCreate(JavaScriptTypes.jsLoader, "LNumber");
	private static IClass JS_CLASS_NUMBER;
	
	private static TypeReference JS_STRING = TypeReference.findOrCreate(JavaScriptTypes.jsLoader, "LString");
	private static IClass JS_CLASS_STRING;
	
	private static TypeReference JS_OBJECT = TypeReference.findOrCreate(JavaScriptTypes.jsLoader, "LObject");
	private static IClass JS_CLASS_OBJECT;
	
	private static TypeReference JS_ARRAY = TypeReference.findOrCreate(JavaScriptTypes.jsLoader, "LArray");
	private static IClass JS_CLASS_ARRAY;
	
	private static Map<IClass, IClass> java2js_classmap = new HashMap<IClass, IClass>();
	private static Map<IClass, Set<IClass>> js2java_classmap = new HashMap<IClass, Set<IClass>>();
	
	private static Map<TypeReference, TypeReference> java2js_typemap = new HashMap<TypeReference, TypeReference>();
	private static Map<TypeReference, Set<TypeReference>> js2java_typemap = new HashMap<TypeReference, Set<TypeReference>>();
	
	public static void initialize(IClassHierarchy cha){
		JAVA_CLASS_Z = cha.lookupClass(JAVA_Z);
		JAVA_CLASS_I = cha.lookupClass(JAVA_I);
		
		JAVA_CLASS_BOOLEAN = cha.lookupClass(JAVA_BOOLEAN);
		JAVA_CLASS_INTEGER = cha.lookupClass(JAVA_INTEGER);
		JAVA_CLASS_FLOAT = cha.lookupClass(JAVA_FLOAT);
		JAVA_CLASS_DOUBLE = cha.lookupClass(JAVA_DOUBLE);
		JAVA_CLASS_STRING = cha.lookupClass(JAVA_STRING);
		JAVA_CLASS_APP_STRING = cha.lookupClass(JAVA_APP_STRING);
		
		JS_CLASS_BOOLEAN = cha.lookupClass(JS_BOOLEAN);
		JS_CLASS_NUMBER = cha.lookupClass(JS_NUMBER);
		JS_CLASS_STRING = cha.lookupClass(JS_STRING);
		JS_CLASS_OBJECT = cha.lookupClass(JS_OBJECT);
		JS_CLASS_ARRAY = cha.lookupClass(JS_ARRAY);
		
		java2js_classmap.put(JAVA_CLASS_Z, JS_CLASS_BOOLEAN);
		java2js_classmap.put(JAVA_CLASS_I, JS_CLASS_NUMBER);
		
		java2js_classmap.put(JAVA_CLASS_BOOLEAN, JS_CLASS_BOOLEAN);
		java2js_classmap.put(JAVA_CLASS_INTEGER, JS_CLASS_NUMBER);
		java2js_classmap.put(JAVA_CLASS_FLOAT, JS_CLASS_NUMBER);
		java2js_classmap.put(JAVA_CLASS_DOUBLE, JS_CLASS_NUMBER);
		java2js_classmap.put(JAVA_CLASS_STRING, JS_CLASS_STRING);
		java2js_classmap.put(JAVA_CLASS_APP_STRING, JS_CLASS_STRING);
		
		java2js_typemap.put(JAVA_Z, JS_BOOLEAN);
		java2js_typemap.put(JAVA_I, JS_NUMBER);
		java2js_typemap.put(JAVA_BOOLEAN, JS_BOOLEAN);
		java2js_typemap.put(JAVA_INTEGER, JS_NUMBER);
		java2js_typemap.put(JAVA_FLOAT, JS_NUMBER);
		java2js_typemap.put(JAVA_DOUBLE, JS_NUMBER);
		java2js_typemap.put(JAVA_STRING, JS_STRING);
		java2js_typemap.put(JAVA_APP_STRING, JS_STRING);
		
		Set<IClass> nClassSet = new HashSet<IClass>();
		nClassSet.add(JAVA_CLASS_INTEGER);
		nClassSet.add(JAVA_CLASS_FLOAT);
		nClassSet.add(JAVA_CLASS_DOUBLE);
		nClassSet.add(JAVA_CLASS_I);
		js2java_classmap.put(JS_CLASS_NUMBER, nClassSet);
		
		Set<TypeReference> nTypeSet = new HashSet<TypeReference>();
		nTypeSet.add(JAVA_INTEGER);
		nTypeSet.add(JAVA_FLOAT);
		nTypeSet.add(JAVA_DOUBLE);
		nTypeSet.add(JAVA_I);
		js2java_typemap.put(JS_NUMBER, nTypeSet);
		
		Set<IClass> sClassSet = new HashSet<IClass>();
		sClassSet.add(JAVA_CLASS_STRING);
		js2java_classmap.put(JS_CLASS_STRING, sClassSet);
		
		Set<TypeReference> sTypeSet = new HashSet<TypeReference>();
		sTypeSet.add(JAVA_STRING);
		js2java_typemap.put(JS_STRING, sTypeSet);
		
		Set<IClass> bClassSet = new HashSet<IClass>();
		bClassSet.add(JAVA_CLASS_BOOLEAN);
		bClassSet.add(JAVA_CLASS_Z);
		js2java_classmap.put(JS_CLASS_BOOLEAN, bClassSet);
		
		Set<TypeReference> bTypeSet = new HashSet<TypeReference>();
		bTypeSet.add(JAVA_BOOLEAN);
		bTypeSet.add(JAVA_Z);
		js2java_typemap.put(JS_BOOLEAN, bTypeSet);
	}
	
	public static boolean isJava2JSConvertable(TypeReference tr){
		return java2js_typemap.containsKey(tr);
	}
	
	public static boolean isJava2JSConvertable(IClass c){
		return java2js_classmap.containsKey(c);
	}
	
	public static IClass java2JSTypeConvert(IClass c){
		return java2js_classmap.get(c);
	}
	
	public static InstanceKey js2JavaTypeConvert(InstanceKey ik, IClass targetClass){
		IClass argType = ik.getConcreteType();
		if(js2java_classmap.containsKey(argType)){
			Set<IClass> convertableTypes = js2java_classmap.get(argType);
			if(convertableTypes.contains(targetClass)){
				if(ik instanceof ConcreteTypeKey){
					return new ConcreteTypeKey(targetClass);	
				}else if(ik instanceof ConstantKey){
					ConstantKey cik = (ConstantKey) ik;
					return new ConstantKey(cik.getValue(), targetClass);
				}
			}
		}
		
		return ik;
	}
	
	public static boolean isJs2JavaTypeCompatible(IClass jsClass, IClass javaClass){
		if(js2java_classmap.containsKey(jsClass)){
			Set<IClass> convertableTypes = js2java_classmap.get(jsClass);
			if(convertableTypes.contains(javaClass)){
				return true;
			}
		}		
		return false;
	}
	
	public static boolean isJs2JavaTypeCompatible(TypeReference jsType, TypeReference javaType){
		if(js2java_typemap.containsKey(jsType)){
			Set<TypeReference> convertableTypes = js2java_typemap.get(jsType);
			if(convertableTypes.contains(javaType)){
				return true;
			}
		}		
		return false;
	}

}
