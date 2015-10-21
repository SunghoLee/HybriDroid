package kr.ac.kaist.hybridroid.analysis.string.model;

import java.util.HashMap;
import java.util.Map;

import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.MethodReference;

public class StringModel {
	
	private static Map<String, ClassModel> classMap;
	
	static{
		classMap = new HashMap<String, ClassModel>();
		init();
	}
	
	private static void init(){
		classMap.put("String", StringClassModel.getInstance());
		classMap.put("StringBuilder", StringBuilderClassModel.getInstance());
		classMap.put("StringBuffer", StringBufferClassModel.getInstance());
	}
	
	public static boolean isTargetClassModeled(SSAInvokeInstruction invokeInst){
		MethodReference targetMR = invokeInst.getDeclaredTarget();
		String className = targetMR.getDeclaringClass().getName().getClassName().toString();
		return classMap.containsKey(className);
	}
	
	public static boolean isClassModeled(String className){
		return classMap.containsKey(className);
	}
	
	public static ClassModel getModeledTargetClass(SSAInvokeInstruction invokeInst){
		MethodReference targetMR = invokeInst.getDeclaredTarget();
		String className = targetMR.getDeclaringClass().getName().getClassName().toString();
		
		return getModeledClass(className);
	}
	
	public static ClassModel getModeledClass(String className){
		if(isClassModeled(className))
			return classMap.get(className);
		return null;
	}
	
	public static MethodModel getTargetMethod(SSAInvokeInstruction invokeInst){
		MethodReference targetMR = invokeInst.getDeclaredTarget();
		String className = targetMR.getDeclaringClass().getName().getClassName().toString();
		String methodName = targetMR.getName().toString();
		
		ClassModel mClass = getModeledClass(className);
		if(mClass != null)
			return mClass.getMethod(methodName);
		return null;
	}
}
