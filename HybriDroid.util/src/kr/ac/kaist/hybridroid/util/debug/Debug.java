package kr.ac.kaist.hybridroid.util.debug;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.types.ClassLoaderReference;

public class Debug {
	public static boolean TDEBUG = false;
	public static boolean DEBUG_ALL = false;
	public static Map<String, Boolean> debugMap = new HashMap<String, Boolean>();
	public static int ALIGN_MAX = 120;
	private static void printMsg(StackTraceElement _element, String _msg){
        if(_element != null){
        	String className = _element.getClassName();
        	String methodName = _element.getMethodName();
        	int lineNum = _element.getLineNumber();
        	if(DEBUG_ALL || (debugMap.containsKey(className) && debugMap.get(className))){
        		System.out.println(alignMsg(_msg)+" \t\t[[at]"+className+"::"+methodName+"(line "+lineNum+")]");
        	}
        }
	}
	
	private static String alignMsg(String _msg){
		int realLength = 0;;
		for(int i=0; i<_msg.length(); i++){
			if(_msg.charAt(i) == '\t')
				realLength += 8;
			else
				realLength += 1;
		}
		if(realLength >= ALIGN_MAX)
			_msg += '\t';
		else
			for(;realLength < ALIGN_MAX; realLength++)
				_msg += ' ';
		return _msg;
	}
	
	public static void printMsg(String _msg){
		Throwable e = new Throwable();
        e.fillInStackTrace();
        StackTraceElement[] elements = e.getStackTrace();
        StackTraceElement beforeElement = (elements.length > 1) ? elements[1] : null;
        printMsg(beforeElement, _msg);
	}
	
	public static void printMsg(){
		System.out.println();
	}
	
	public static void printMsg(Object _object){
		Throwable e = new Throwable();
        e.fillInStackTrace();
        StackTraceElement[] elements = e.getStackTrace();
        StackTraceElement beforeElement = (elements.length > 1) ? elements[1] : null;
        
		if(_object == null)
			printMsg(beforeElement, "null");
		else
			printMsg(beforeElement, _object.toString());
	}
	
	public static void setDebuggable(Object _object, boolean _isDebuggable){
		debugMap.put(_object.getClass().getName(), _isDebuggable);
	}
	
	public static void temporaryStop(){
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static boolean fromLoader(IClass declClass, ClassLoaderReference clr) {
        ClassLoaderReference nodeClRef =
                declClass.getClassLoader().getReference();
        return nodeClRef.equals(clr);
    }
		
	public static void printMsgInContext(String str){
//		if(fromLoader(sNode.getMethod().getDeclaringClass(), ClassLoaderReference.Application) || fromLoader(sNode.getMethod().getDeclaringClass(), ClassLoaderReference.Primordial))
			System.out.println(str);
	}
}
