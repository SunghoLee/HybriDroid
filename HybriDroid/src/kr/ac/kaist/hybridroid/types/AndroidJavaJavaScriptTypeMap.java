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
	
	private static TypeReference JAVA_BOOLEAN = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/Boolean");
	private static IClass JAVA_CLASS_BOOLEAN;		
			
	private static TypeReference JAVA_INTEGER = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/Integer");
	private static IClass JAVA_CLASS_INTEGER;
	
	private static TypeReference JAVA_FLOAT = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/Float");
	private static IClass JAVA_CLASS_FLOAT;
	
	private static TypeReference JAVA_DOUBLE = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/Double");
	private static IClass JAVA_CLASS_DOUBLE;
	
	private static TypeReference JAVA_STRING = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/String");
	private static IClass JAVA_CLASS_STRING;
	
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
	
	private static Map<IClass, IClass> java2js_typemap = new HashMap<IClass, IClass>();
	private static Map<IClass, Set<IClass>> js2java_typemap = new HashMap<IClass, Set<IClass>>();
	
	public static void initialize(IClassHierarchy cha){
		JAVA_CLASS_BOOLEAN = cha.lookupClass(JAVA_BOOLEAN);
		JAVA_CLASS_INTEGER = cha.lookupClass(JAVA_INTEGER);
		JAVA_CLASS_FLOAT = cha.lookupClass(JAVA_FLOAT);
		JAVA_CLASS_DOUBLE = cha.lookupClass(JAVA_DOUBLE);
		JAVA_CLASS_STRING = cha.lookupClass(JAVA_STRING);
		
		JS_CLASS_BOOLEAN = cha.lookupClass(JS_BOOLEAN);
		JS_CLASS_NUMBER = cha.lookupClass(JS_NUMBER);
		JS_CLASS_STRING = cha.lookupClass(JS_STRING);
		JS_CLASS_OBJECT = cha.lookupClass(JS_OBJECT);
		JS_CLASS_ARRAY = cha.lookupClass(JS_ARRAY);
		
		java2js_typemap.put(JAVA_CLASS_BOOLEAN, JS_CLASS_BOOLEAN);
		java2js_typemap.put(JAVA_CLASS_INTEGER, JS_CLASS_NUMBER);
		java2js_typemap.put(JAVA_CLASS_FLOAT, JS_CLASS_NUMBER);
		java2js_typemap.put(JAVA_CLASS_DOUBLE, JS_CLASS_NUMBER);
		java2js_typemap.put(JAVA_CLASS_STRING, JS_CLASS_STRING);
		
		Set<IClass> nTypeSet = new HashSet<IClass>();
		nTypeSet.add(JAVA_CLASS_INTEGER);
		nTypeSet.add(JAVA_CLASS_FLOAT);
		nTypeSet.add(JAVA_CLASS_DOUBLE);
		js2java_typemap.put(JS_CLASS_NUMBER, nTypeSet);
		
		Set<IClass> sTypeSet = new HashSet<IClass>();
		sTypeSet.add(JAVA_CLASS_STRING);
		js2java_typemap.put(JS_CLASS_STRING, sTypeSet);
		
		Set<IClass> bTypeSet = new HashSet<IClass>();
		bTypeSet.add(JAVA_CLASS_BOOLEAN);
		js2java_typemap.put(JS_CLASS_BOOLEAN, bTypeSet);
	}
	
	public static boolean isJava2JSConvertable(IClass c){
		return java2js_typemap.containsKey(c);
	}
	
	public static IClass java2JSTypeConvert(IClass c){
		return java2js_typemap.get(c);
	}
	
	public static InstanceKey js2JavaTypeConvert(InstanceKey ik, IClass targetClass){
		IClass argType = ik.getConcreteType();
		if(js2java_typemap.containsKey(argType)){
			Set<IClass> convertableTypes = js2java_typemap.get(argType);
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
}
