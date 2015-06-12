package kr.ac.kaist.hybridroid.checker;

import java.util.HashSet;
import java.util.Set;

import kr.ac.kaist.hybridroid.types.AndroidJavaJavaScriptTypeMap;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;

public class Js2JavaCompatibleTypeChecker {
	private static Js2JavaCompatibleTypeChecker instance;
	private Set<TypeWarning> warningSet;
	
	public static Js2JavaCompatibleTypeChecker getInstance(){
		if(instance == null)
			instance = new Js2JavaCompatibleTypeChecker();
		return instance;
	}
	
	private Js2JavaCompatibleTypeChecker(){
		warningSet = new HashSet<TypeWarning>();
	}
	
	private void addWarning(TypeWarning w){
		warningSet.add(w);
	}
		
	public boolean returnTypeCheck(CGNode caller, SSAAbstractInvokeInstruction call, IMethod target){
		if(call.hasDef()){
			TypeReference typeRef = target.getReturnType();
			
			if(typeRef.equals(TypeReference.findOrCreate(ClassLoaderReference.Primordial, "V"))){
				int def = call.getDef();
				String sourceDefName = caller.getMethod().getLocalVariableName(-1, def);
				int numUse = caller.getDU().getNumberOfUses(def);
				
				// stupid return assignment. ex> var x = getName(); // getName is 'void' function.
				if(sourceDefName != null || numUse != 0){
					addWarning(new ReturnVoidTypeWarning(caller, call, target, typeRef));
					return false;
				}
			}else if(AndroidJavaJavaScriptTypeMap.isJava2JSConvertable(typeRef) == false){
				addWarning(new ReturnTypeWarning(caller, call, target, typeRef));
				return false;
			}
		}
		return true;
	}
	
	public boolean argTypeCheck(CGNode caller, SSAAbstractInvokeInstruction call, int argNum, IMethod target, TypeReference paramType, TypeReference argType){
		if(!AndroidJavaJavaScriptTypeMap.isJs2JavaTypeCompatible(argType, paramType)){
			addWarning(new ParamTypeWarning(caller, call, target, argNum, argType, paramType));
			return false;
		}
		return true;
	}
		
	public boolean argNumCheck(CGNode caller, SSAAbstractInvokeInstruction call, IMethod target, int paramNum, int argNum){
		if(paramNum != argNum){
			addWarning(new ArgsMismatchedWarning(caller, call, target, paramNum, argNum));
			return false;
		}
		return true;
	}
	
	public Set<TypeWarning> getWarnings(){
		return warningSet;
	}
	
	public interface TypeWarning{
		public CGNode getNode();
		public SSAAbstractInvokeInstruction getCallInst();
		public IMethod getTarget();
	}
	
	class ArgsMismatchedWarning implements TypeWarning{
		private CGNode caller;
		private SSAAbstractInvokeInstruction call;
		private IMethod target;
		private int paramNum;
		private int argNum;
		
		public ArgsMismatchedWarning(CGNode caller, SSAAbstractInvokeInstruction call, IMethod target, int paramNum, int argNum){
			this.caller = caller;
			this.call = call;
			this.target = target;
			this.paramNum = paramNum;
			this.argNum = argNum;
		}
		
		@Override
		public CGNode getNode() {
			// TODO Auto-generated method stub
			return caller;
		}

		@Override
		public SSAAbstractInvokeInstruction getCallInst() {
			// TODO Auto-generated method stub
			return call;
		}

		@Override
		public IMethod getTarget() {
			// TODO Auto-generated method stub
			return target;
		}
		
		public int getParamNum(){
			return paramNum;
		}
		
		public int getArgNum(){
			return argNum;
		}
		
		@Override
		public int hashCode(){
			return getNode().hashCode() + getCallInst().hashCode() + getTarget().hashCode() + paramNum + argNum;
		}
		
		@Override
		public boolean equals(Object o){
			if(o instanceof ArgsMismatchedWarning){
				ArgsMismatchedWarning amw = (ArgsMismatchedWarning) o;
				if(amw.getNode().equals(this.getNode()) && 
						amw.getCallInst().iindex == this.getCallInst().iindex &&
						amw.getTarget().equals(this.getTarget()) && 
						amw.getParamNum() == this.getParamNum() &&
						amw.getArgNum() == this.getArgNum())
					return true;
					
			}
			return false;
		}
		
		@Override
		public String toString(){
			String msg = "*Warning* Mismatched the number of arguments: ";
			msg += "\n\t caller: " + getNode();
			msg += "\n\t instruction: " + getCallInst();
			msg += "\n\t target: " + getTarget();
			msg += "\n\t the # of parameters is '" + getParamNum() + "' but the # of arguments is '" + getArgNum() +"'";
			return msg;
		}
	}
	
	class ReturnTypeWarning implements TypeWarning{
		private CGNode node;
		private SSAAbstractInvokeInstruction call;
		private IMethod target;
		private TypeReference retType; 
		
		public ReturnTypeWarning(CGNode node, SSAAbstractInvokeInstruction call, IMethod target, TypeReference retType){
			this.node = node;
			this.call = call;
			this.target = target;
			this.retType = retType;
		}
		
		@Override
		public CGNode getNode() {
			// TODO Auto-generated method stub
			return node;
		}

		@Override
		public SSAAbstractInvokeInstruction getCallInst() {
			// TODO Auto-generated method stub
			return call;
		}

		@Override
		public IMethod getTarget() {
			// TODO Auto-generated method stub
			return target;
		}
		
		public TypeReference returnType(){
			return retType;
		}
		
		@Override
		public String toString(){
			String msg = "*Warning* Return type mismatched: ";
			msg += "\n\t caller: " + getNode();
			msg += "\n\t instruction: " + getCallInst();
			msg += "\n\t target: " + getTarget();
			msg += "\n\t '" + returnType() + "' is not compatible for JavaScript";
			return msg;
		}
		
		@Override
		public int hashCode(){
			return this.getNode().hashCode() + this.getCallInst().hashCode() + this.getTarget().hashCode();
		}
		
		@Override
		public boolean equals(Object o){
			if(o instanceof ReturnTypeWarning){
				ReturnTypeWarning rvtw = (ReturnTypeWarning) o;
				if(rvtw.getCallInst().iindex == this.getCallInst().iindex &&
						rvtw.getNode().equals(this.getNode()) &&
						rvtw.getTarget().equals(this.getTarget()) &&
						rvtw.toString().equals(this.toString()))
					return true;
			}
			return false;
		}
	}
	
	class ReturnVoidTypeWarning extends ReturnTypeWarning{

		public ReturnVoidTypeWarning(CGNode node,
				SSAAbstractInvokeInstruction call, IMethod target,
				TypeReference retType) {
			super(node, call, target, retType);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public String toString(){
			String msg = "*Warning* Return assignment for 'void' function: ";
			msg += "\n\t caller: " + getNode();
			msg += "\n\t instruction: " + getCallInst();
			msg += "\n\t target: " + getTarget();
			msg += "\n\t return type: " + returnType();
			return msg;
		}
		
		@Override
		public int hashCode(){
			return this.getNode().hashCode() + this.getCallInst().hashCode() + this.getTarget().hashCode();
		}
		
		@Override
		public boolean equals(Object o){
			if(o instanceof ReturnVoidTypeWarning){
				ReturnVoidTypeWarning rvtw = (ReturnVoidTypeWarning) o;
				if(rvtw.getCallInst().iindex == this.getCallInst().iindex &&
						rvtw.getNode().equals(this.getNode()) &&
						rvtw.getTarget().equals(this.getTarget()))
					return true;
			}
			return false;
		}
	}
	
	class ParamTypeWarning implements TypeWarning{
		private CGNode node;
		private SSAAbstractInvokeInstruction inst;
		private IMethod target;
		private int argNum;
		private TypeReference argType;
		private TypeReference paramType;
		
		public ParamTypeWarning(CGNode node, SSAAbstractInvokeInstruction inst, IMethod target, int argNum, TypeReference argType, TypeReference paramType){
			this.node = node;
			this.inst = inst;
			this.target = target;
			this.argNum = argNum;
			this.argType = argType;
			this.paramType = paramType;
		}

		public CGNode getNode() {
			return node;
		}

		public SSAAbstractInvokeInstruction getCallInst() {
			return inst;
		}

		public IMethod getTarget() {
			return target;
		}

		public int getArgNum(){
			return argNum;
		}
		
		public TypeReference getArgType() {
			return argType;
		}

		public TypeReference getParamType() {
			return paramType;
		}
		
		@Override
		public String toString(){
			String msg = "*Warning* Parameter type mismatched: ";
			msg += "\n\t caller: " + getNode();
			msg += "\n\t instruction: " + getCallInst();
			msg += "\n\t target: " + getTarget();
			msg += "\n\t #arg: " + getArgNum();
			msg += "\n\t paramType: " + getParamType();
			msg += "\n\t argType: " + getArgType();
			return msg;
		}
		
		@Override
		public int hashCode(){
			return getNode().hashCode() + getCallInst().hashCode() + getArgNum();
		}
		
		@Override
		public boolean equals(Object o){
			if(o instanceof ParamTypeWarning){
				ParamTypeWarning ptw = (ParamTypeWarning)o;
				if(ptw.getArgNum() == this.getArgNum() &&
						ptw.getCallInst().iindex == this.getCallInst().iindex &&
						ptw.getNode().equals(this.getNode()) &&
						ptw.getParamType().equals(this.getParamType()) &&
						ptw.getArgType().equals(this.getArgType()))
					return true;
			}
			return false;
		}
	}
	
	public void printTypeWarning() {
		// TODO Auto-generated method stub		
		System.out.println("=== Type mismatch warnings ===");
		for(TypeWarning tw : this.getWarnings()){
			System.out.println(tw);
		}
		System.out.println("==============================");
	}
}
