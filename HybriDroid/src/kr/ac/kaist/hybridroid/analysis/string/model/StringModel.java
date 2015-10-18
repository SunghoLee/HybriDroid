package kr.ac.kaist.hybridroid.analysis.string.model;

import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.MethodReference;

public interface StringModel {
	
	static String[][] models = {
		{"StringBuffer", "toString"}, 
	};
	
	public static boolean isModeledTarget(SSAInvokeInstruction invokeInst){
		MethodReference targetMR = invokeInst.getDeclaredTarget();
		String className = targetMR.getDeclaringClass().getName().getClassName().toString();
		String methodName = targetMR.getName().toString();
		
		for(String[] model : models){
			String mClassName = model[0];
			String mMethodName = model[1];
			
			if(className.equals(mClassName) && methodName.equals(mMethodName))
				return true;
		}
		return false;
	}
	
	public static StringModel getModeledClass(SSAInvokeInstruction invokeInst){
		MethodReference targetMR = invokeInst.getDeclaredTarget();
		String className = targetMR.getDeclaringClass().getName().getClassName().toString();
		String methodName = targetMR.getName().toString();
		
		for(String[] model : models){
			String mClassName = model[0];
			String mMethodName = model[1];
			
			if(className.equals(mClassName) && methodName.equals(mMethodName)){
				switch(className){
				case "StringBuffer":
					return StringBufferClassModel.getInstance();
				default:
					return null;
				}
			}
		}
		return null;
	}
}
