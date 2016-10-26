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
package kr.ac.kaist.wala.hybridroid.types.bridge;

import java.util.Collection;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.strings.Atom;

/**
 * Data structure contains type information of parameters and a return value of a method.
 * @author Sungho Lee
 *
 */
public class MethodInfo implements IMethod{
	private IMethod m;
	private boolean isAccessible;
	
	public MethodInfo(IMethod m, boolean isAccessible){
		this.m = m;
		this.isAccessible = isAccessible;
	}
	
	/**
	 * Is this method accessible from JavaScript?
	 * @return true if it is, otherwise false.
	 */
	public boolean isAccessible(){
		return this.isAccessible;
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMember#getDeclaringClass()
	 */
	@Override
	public IClass getDeclaringClass() {
		// TODO Auto-generated method stub
		return m.getDeclaringClass();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMember#getName()
	 */
	@Override
	public Atom getName() {
		// TODO Auto-generated method stub
		return m.getName();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMember#isStatic()
	 */
	@Override
	public boolean isStatic() {
		// TODO Auto-generated method stub
		return m.isStatic();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMember#getAnnotations()
	 */
	@Override
	public Collection<Annotation> getAnnotations() {
		// TODO Auto-generated method stub
		return m.getAnnotations();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.ipa.cha.IClassHierarchyDweller#getClassHierarchy()
	 */
	@Override
	public IClassHierarchy getClassHierarchy() {
		// TODO Auto-generated method stub
		return m.getClassHierarchy();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#isSynchronized()
	 */
	@Override
	public boolean isSynchronized() {
		// TODO Auto-generated method stub
		return m.isSynchronized();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#isClinit()
	 */
	@Override
	public boolean isClinit() {
		// TODO Auto-generated method stub
		return m.isClinit();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#isInit()
	 */
	@Override
	public boolean isInit() {
		// TODO Auto-generated method stub
		return m.isInit();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#isNative()
	 */
	@Override
	public boolean isNative() {
		// TODO Auto-generated method stub
		return m.isNative();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#isSynthetic()
	 */
	@Override
	public boolean isSynthetic() {
		// TODO Auto-generated method stub
		return m.isSynthetic();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#isAbstract()
	 */
	@Override
	public boolean isAbstract() {
		// TODO Auto-generated method stub
		return m.isAbstract();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#isPrivate()
	 */
	@Override
	public boolean isPrivate() {
		// TODO Auto-generated method stub
		return m.isPrivate();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#isProtected()
	 */
	@Override
	public boolean isProtected() {
		// TODO Auto-generated method stub
		return m.isProtected();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#isPublic()
	 */
	@Override
	public boolean isPublic() {
		// TODO Auto-generated method stub
		return m.isPublic();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#isFinal()
	 */
	@Override
	public boolean isFinal() {
		// TODO Auto-generated method stub
		return m.isFinal();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#isBridge()
	 */
	@Override
	public boolean isBridge() {
		// TODO Auto-generated method stub
		return m.isBridge();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#getReference()
	 */
	@Override
	public MethodReference getReference() {
		// TODO Auto-generated method stub
		return m.getReference();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#hasExceptionHandler()
	 */
	@Override
	public boolean hasExceptionHandler() {
		// TODO Auto-generated method stub
		return m.hasExceptionHandler();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#getParameterType(int)
	 */
	@Override
	public TypeReference getParameterType(int i) {
		// TODO Auto-generated method stub
		return m.getParameterType(i);
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#getReturnType()
	 */
	@Override
	public TypeReference getReturnType() {
		// TODO Auto-generated method stub
		return m.getReturnType();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#getNumberOfParameters()
	 */
	@Override
	public int getNumberOfParameters() {
		// TODO Auto-generated method stub
		return m.getNumberOfParameters();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#getDeclaredExceptions()
	 */
	@Override
	public TypeReference[] getDeclaredExceptions() throws InvalidClassFileException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		return m.getDeclaredExceptions();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#getLineNumber(int)
	 */
	@Override
	public int getLineNumber(int bcIndex) {
		// TODO Auto-generated method stub
		return m.getLineNumber(bcIndex);
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#getSourcePosition(int)
	 */
	@Override
	public SourcePosition getSourcePosition(int instructionIndex) throws InvalidClassFileException{
		// TODO Auto-generated method stub
		return m.getSourcePosition(instructionIndex);
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#getParameterSourcePosition(int)
	 */
	@Override
	public SourcePosition getParameterSourcePosition(int paramNum) throws InvalidClassFileException{
		// TODO Auto-generated method stub
		return m.getParameterSourcePosition(paramNum);
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#getLocalVariableName(int, int)
	 */
	@Override
	public String getLocalVariableName(int bcIndex, int localNumber) {
		// TODO Auto-generated method stub
		return m.getLocalVariableName(bcIndex, localNumber);
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#getSignature()
	 */
	@Override
	public String getSignature() {
		// TODO Auto-generated method stub
		return m.getSignature();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#getSelector()
	 */
	@Override
	public Selector getSelector() {
		// TODO Auto-generated method stub
		return m.getSelector();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#getDescriptor()
	 */
	@Override
	public Descriptor getDescriptor() {
		// TODO Auto-generated method stub
		return m.getDescriptor();
	}

	/* (non-Javadoc)
	 * @see com.ibm.wala.classLoader.IMethod#hasLocalVariableTable()
	 */
	@Override
	public boolean hasLocalVariableTable() {
		// TODO Auto-generated method stub
		return m.hasLocalVariableTable();
	}	
	
	public String toString(){
		String res = "";
		res += m + "(" + isAccessible + ")";
		return res;
	}
}