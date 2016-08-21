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
package kr.ac.kaist.hybridroid.pointer;

import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.strings.Atom;

public final class MockupClass implements IClass {
	private static Set<MockupClass> mSet;
	private IMethod method;
	
	static{
		mSet = new HashSet<MockupClass>();
	}
	
	public static MockupClass findOrCreateMockup(IMethod method){
		MockupClass res = null;
		for(MockupClass mc : mSet){
			if(mc.isMockupOf(method))
				res = mc;
		}
		
		if(res == null){
			res = new MockupClass(method);
			mSet.add(res);
		}
		
		return res;
	}
	
	private MockupClass(IMethod method){
		this.method = method;
	}
	
	public IMethod getMethod(){
		return method;
	}
	
	public boolean isMockupOf(IMethod method){
		return this.method.equals(method);
	}
	
	@Override
	public int hashCode(){
		return method.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		// instanceof is possible because this class is final.
		if(o instanceof MockupClass){
			if(((MockupClass)o).isMockupOf(method))
				return true;
		}
		return false;
	}
	
	@Override
	public IClassHierarchy getClassHierarchy() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public IClassLoader getClassLoader() {
		// TODO Auto-generated method stub
//		throw new UnsupportedOperationException();
		return method.getClassHierarchy().getLoader(ClassLoaderReference.Primordial);
	}

	@Override
	public boolean isInterface() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAbstract() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPublic() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPrivate() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public int getModifiers() throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public IClass getSuperclass() {
		// TODO Auto-generated method stub
//		throw new UnsupportedOperationException();
		return method.getClassHierarchy().getRootClass();
	}

	@Override
	public Collection<? extends IClass> getDirectInterfaces() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<IClass> getAllImplementedInterfaces() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public IMethod getMethod(Selector selector) {
		// TODO Auto-generated method stub
//		throw new UnsupportedOperationException();
		return (method.getSelector().equals(selector))? method : null;
	}

	@Override
	public IField getField(Atom name) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public IField getField(Atom name, TypeName type) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public TypeReference getReference() {
		// TODO Auto-generated method stub
//		throw new UnsupportedOperationException();
		return TypeReference.JavaLangObject;
	}

	@Override
	public String getSourceFileName() throws NoSuchElementException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Reader getSource() throws NoSuchElementException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public IMethod getClassInitializer() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isArrayClass() {
		// TODO Auto-generated method stub
//		throw new UnsupportedOperationException();
		return false;
	}

	@Override
	public Collection<IMethod> getDeclaredMethods() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<IField> getAllInstanceFields() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<IField> getAllStaticFields() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<IField> getAllFields() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<IMethod> getAllMethods() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<IField> getDeclaredInstanceFields() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<IField> getDeclaredStaticFields() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public TypeName getName() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isReferenceType() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Annotation> getAnnotations() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
