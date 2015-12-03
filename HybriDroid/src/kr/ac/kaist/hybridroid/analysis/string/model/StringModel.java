package kr.ac.kaist.hybridroid.analysis.string.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.resource.AndroidResourceAnalysis;
import kr.ac.kaist.hybridroid.analysis.string.resource.model.ContextClassModel;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.MethodReference;

public class StringModel {
	
	private static Map<String, IClassModel> classMap;
	private static Set<String> warnings;
	
	static{
		warnings = new HashSet<String>();
		classMap = new HashMap<String, IClassModel>();
		init();
	}
	
	public static void setResourceAnalysis(AndroidResourceAnalysis ra, String region){
		classMap.put("Context", ContextClassModel.getInstance(ra, region));
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
	
	private static void init(){
		classMap.put("String", StringClassModel.getInstance());
		classMap.put("StringBuilder", StringBuilderClassModel.getInstance());
		classMap.put("StringBuffer", StringBufferClassModel.getInstance());
		classMap.put("Context", ContextClassModel.getInstance());
		classMap.put("UriCodec", UriCodecClassModel.getInstance());
		classMap.put("Uri", UriClassModel.getInstance());
		classMap.put("Object", ObjectClassModel.getInstance());
	}
	
	public static boolean isTargetClassModeled(SSAInvokeInstruction invokeInst){
		MethodReference targetMR = invokeInst.getDeclaredTarget();
		String className = targetMR.getDeclaringClass().getName().getClassName().toString();
		return classMap.containsKey(className);
	}
	
	public static boolean isClassModeled(String className){
		return classMap.containsKey(className);
	}
	
	public static IClassModel getModeledTargetClass(SSAInvokeInstruction invokeInst){
		MethodReference targetMR = invokeInst.getDeclaredTarget();
		String className = targetMR.getDeclaringClass().getName().getClassName().toString();
		
		return getModeledClass(className);
	}
	
	public static IClassModel getModeledClass(String className){
		if(isClassModeled(className))
			return classMap.get(className);
		return null;
	}
	
	public static IMethodModel getTargetMethod(String cn, String mn){
		String className = cn;
		String methodName = mn;
		IClassModel mClass = getModeledClass(className);
		if(mClass != null)
			return mClass.getMethod(methodName);
		return null;
	}
	
	public static IMethodModel getTargetMethod(CGNode target){
		MethodReference targetMR = target.getMethod().getReference();
		String className = targetMR.getDeclaringClass().getName().getClassName().toString();
		String methodName = targetMR.getName().toString();
		IClassModel mClass = getModeledClass(className);
		if(mClass != null)
			return mClass.getMethod(methodName);
		return null;
	}
}
