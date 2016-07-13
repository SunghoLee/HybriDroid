/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package kr.ac.kaist.hybridroid.checker;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import kr.ac.kaist.hybridroid.types.AndroidJavaJavaScriptTypeMap;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.strings.Atom;

/**
 * Check which API-misuses can occur at call sites at which JS call statement invokes Java bride method.
 * This is implemented as singleton object, because type warnings just would be collected for all communications.
 *  
 * @author leesh
 */
public class HybridAPIMisusesChecker {
	private static HybridAPIMisusesChecker instance;
	private Set<Warning> warningSet;
	
	/**
	 * get singleton instance.
	 * @return type checker instance.
	 */
	public static HybridAPIMisusesChecker getInstance(){
		if(instance == null)
			instance = new HybridAPIMisusesChecker();
		return instance;
	}
	
	private HybridAPIMisusesChecker(){
		warningSet = new HashSet<Warning>();
	}
	
	private void addWarning(Warning w){
		warningSet.add(w);
	}
		
	/**
	 * API-misuses detection for Java bridge method type overloading.
	 * this is implementation for 'Method overloading using parameter type' of the document.
	 * @param objClass Java bridge class
	 * @param methods Java bridge methods of the objClass
	 * @return true if there is no type overloading in the Java bridge methods, otherwise false.
	 */
	public boolean typeOverloadingCheck(IClass objClass, Collection<IMethod> methods){
		final Set<IMethod> possibleMethods = new HashSet<IMethod>();
		
		for(IMethod t : methods){
			if(t.getAnnotations() != null)
				for (Annotation ann : t.getAnnotations()) {
					Atom className = ann.getType().getName().getClassName();
					if (className.equals(Atom.findOrCreateAsciiAtom("JavascriptInterface")))
						possibleMethods.add(t);
				}
		}

		//only for java 8
//		methods.forEach(new Consumer<IMethod>(){
//			@Override
//			public void accept(IMethod t) {
//				// TODO Auto-generated method stub
//				if(t.getAnnotations() != null)
//					for (Annotation ann : t.getAnnotations()) {
//						Atom className = ann.getType().getName().getClassName();
//						if (className.equals(Atom.findOrCreateAsciiAtom("JavascriptInterface")))
//							possibleMethods.add(t);
//					}
//			}
//		});
		
		Map<Pair<String, Integer>, Set<IMethod>> methodMap = new HashMap<Pair<String, Integer>, Set<IMethod>>();
		for(IMethod m : possibleMethods){
			String mName = m.getName().toString();
			int pNum = m.getNumberOfParameters();
			Pair<String, Integer> p = Pair.make(mName, pNum);
			
			if(methodMap.containsKey(p) == false)
				methodMap.put(p, new HashSet<IMethod>());
			
			methodMap.get(p).add(m);
		}
		
		for(Pair<String, Integer> p : methodMap.keySet()){
			Set<IMethod> mSet = methodMap.get(p);
			if(mSet.size() > 1){ //error!
				addWarning(new MethodTypeOverloadingWarning(objClass, p.fst, p.snd, mSet));
			}else
				mSet.clear();
		}
		
		methodMap.clear();
		possibleMethods.clear();
		
		return true;
	}
	
	/**
	 * API-misuses detection for function calls about return assignment.
	 * 1) void return assignment check.
	 * 2) array return type check.
	 * 3) Java to JS type conversion check.
	 * @param caller caller method
	 * @param call call statement
	 * @param target target method
	 * @return true if the type is safe, otherwise false.
	 */
	public boolean returnTypeCheck(CGNode caller, SSAAbstractInvokeInstruction call, IMethod target){
		if(call.hasDef()){
			TypeReference typeRef = target.getReturnType();
			if(voidReturnAssignmentCheck(caller, call, typeRef) == false){
				addWarning(new ReturnVoidTypeWarning(caller, call, target, typeRef));
				return false;
			}else if(arrayReturnCheck(typeRef) == false){
				addWarning(new ArrayReturnWarning(caller, call, target, typeRef));
				return false;
			}else if(java2JsReturnTypeCheck(typeRef) == false){
				addWarning(new ReturnTypeWarning(caller, call, target, typeRef));
				return false;
			}
		}
		return true;
	}
	
	/**
	 * array return Java bridge method check.
	 * @param typeRef return type reference of the Java bridge method.
	 * @return true if the return type is not an array type, otherwise false.
	 */
	private boolean arrayReturnCheck(TypeReference typeRef){
		if(typeRef.isArrayType())
			return false;
		return true;
	}
	
	/**
	 * void return assignment check.
	 * @param caller
	 * @param call
	 * @param typeRef
	 * @return true if there is no assignment, otherwise false.
	 */
	private boolean voidReturnAssignmentCheck(CGNode caller, SSAAbstractInvokeInstruction call, TypeReference typeRef){
		if(typeRef.equals(TypeReference.findOrCreate(ClassLoaderReference.Primordial, "V"))){
			int def = call.getDef();
			String sourceDefName = caller.getMethod().getLocalVariableName(-1, def);
			int numUse = caller.getDU().getNumberOfUses(def);
			// stupid return assignment. ex> var x = getName(); // getName is 'void' function.
			if(sourceDefName != null || numUse != 0){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Java return type check.
	 * @param typeRef return type reference.
	 * @return true if the return type is comvertable to JS type, otherwise false.
	 */
	private boolean java2JsReturnTypeCheck(TypeReference typeRef){
		return AndroidJavaJavaScriptTypeMap.isJava2JsTypeCompatible(typeRef);
	}
	
	/**
	 * check an argument type which can be comvertable to Java type or not. 
	 * this is implementation for 'Method invocation with inconvertable type' of the document.
	 * @param caller caller JS method.
	 * @param call call JS statement.
	 * @param argNum position of the argument.
	 * @param target target Java bridge method.
	 * @param paramType Java parameter type corresponding the argument position.
	 * @param argType JS argument type corresponding the argument position.
	 * @return true if the type is safe, otherwise false.
	 */
	public boolean argTypeCheck(CGNode caller, SSAAbstractInvokeInstruction call, int argNum, IMethod target, TypeReference paramType, TypeReference argType){
		if(!AndroidJavaJavaScriptTypeMap.isJs2JavaTypeCompatibleNoImplicitConversion(argType, paramType)){
			addWarning(new ImplicitParamTypeWarning(caller, call, target, argNum, argType, paramType));
		}
		
		if(!AndroidJavaJavaScriptTypeMap.isJs2JavaTypeCompatible(argType, paramType)){
			addWarning(new ParamTypeWarning(caller, call, target, argNum, argType, paramType));
			return false;
		}
		return true;
	}
		
	/**
	 * check which the number of arguments are same with the number of parameters of the target method. 
	 * @param caller caller JS method.
	 * @param call call JS statement.
	 * @param target target Java bridge method.
	 * @param paramNum the number of Java parameters of the target method. 
	 * @param argNum the number of JS arguments at the call statement.
	 * @return true if the parameter number is same with the argument number, otherwise false.
	 */
	public boolean argNumCheck(CGNode caller, SSAAbstractInvokeInstruction call, IMethod target, int paramNum, int argNum){
		if(paramNum != argNum){
			addWarning(new ArgsMismatchedWarning(caller, call, target, paramNum, argNum));
			return false;
		}
		return true;
	}
	
	/**
	 * getter method for type warnings.
	 * @return type warning set.
	 */
	public Set<Warning> getWarnings(){
		return warningSet;
	}
	
	public interface Warning{
		public String toString();
	}
	
	public interface TypeWarning extends Warning{
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
	
	class ArrayReturnWarning implements TypeWarning{
		private CGNode node;
		private SSAAbstractInvokeInstruction call;
		private IMethod target;
		private TypeReference retType;
		
		public ArrayReturnWarning(CGNode node, SSAAbstractInvokeInstruction call, IMethod target, TypeReference retType){
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
		
		@Override
		public String toString(){
			String msg = "*Warning* Array return Java bridge method cannnot be invoked: ";
			msg += "\n\t caller: " + getNode();
			msg += "\n\t instruction: " + getCallInst();
			msg += "\n\t target: " + getTarget();
			msg += "\n\t '" + retType + "' is an array type.";
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
	
	class ImplicitParamTypeWarning extends ParamTypeWarning{

		public ImplicitParamTypeWarning(CGNode node, SSAAbstractInvokeInstruction inst, IMethod target, int argNum,
				TypeReference argType, TypeReference paramType) {
			super(node, inst, target, argNum, argType, paramType);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public String toString(){
			String msg = "*Warning* Implicit type conversion occured: ";
			msg += "\n\t caller: " + getNode();
			msg += "\n\t instruction: " + getCallInst();
			msg += "\n\t target: " + getTarget();
			msg += "\n\t #arg: " + getArgNum();
			msg += "\n\t paramType: " + getParamType();
			msg += "\n\t argType: " + getArgType();
			return msg;
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
	
	class MethodTypeOverloadingWarning implements Warning{
		private IClass tclass;
		private String name;
		private int paramNum;
		private Set<IMethod> mSet;
		
		public MethodTypeOverloadingWarning(IClass tclass, String name, int paramNum, Set<IMethod> mSet){
			this.tclass = tclass;
			this.name = name;
			this.paramNum = paramNum;
			this.mSet = mSet;
		}
		
		@Override
		public String toString(){
			String msg = "*Warning* Java bridge class has type-overloaded bridge methods: ";
			msg += "\n\t class: " + tclass.getName().getClassName();
			msg += "\n\t method name: " + name;
			msg += "\n\t #param: " + paramNum;
			msg += "\n\t method signatures: ";
			for(IMethod m : mSet){
				msg += "\n\t\t" + m;
			}
			return msg;
		}
	}
	
	public void printTypeWarning() {
		// TODO Auto-generated method stub		
		System.out.println("=== Type mismatch warnings ===");
		for(Warning tw : this.getWarnings()){
			System.out.println(tw);
		}
		System.out.println("==============================");
	}
}
