package kr.ac.kaist.hybridroid.pointer;

import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.ibm.wala.classLoader.FieldImpl;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dalvik.classLoader.DexIClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeBT.Constants;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.strings.Atom;

public class InterfaceClass implements IClass {

	private static Map<IClass, InterfaceClass> classes;
	private IClass targetClass;
	private Map<Atom, IField> mFields;
	
	static{
		classes = new HashMap<IClass, InterfaceClass>();
	}
	
	public static InterfaceClass wrapping(IClass targetClass){
		if(classes.keySet().contains(targetClass))
			return classes.get(targetClass);
		InterfaceClass c = new InterfaceClass((DexIClass)targetClass);
		classes.put(targetClass, c);
		return c;
	}
	
	private InterfaceClass(DexIClass targetClass) {
//		super(targetClass.getClassLoader(),targetClass.getClassHierarchy(),targetClass.getModuleEntry()); // this is dummy super call. 
		this.targetClass = targetClass;
		mFields = new HashMap<Atom, IField>();
	}

	public void addMethodAsField(IMethod method){
		FieldReference fr = FieldReference.findOrCreate(targetClass.getClassLoader().getReference(), 
				targetClass.getReference().getName().toString(), 
				method.getName().toString(), 
				method.getReference().getName().toString());
		IField f = new FieldImpl(targetClass, fr, getAccessFlagForMethod(method), (Collection)Collections.emptySet());
		mFields.put(method.getName(), f);
	}
	
	private int getAccessFlagForMethod(IMethod method){
		if(method.isPrivate()){
			return Constants.ACC_PRIVATE;
		}else if(method.isProtected()){
			return Constants.ACC_PROTECTED;
		}else if(method.isPublic()){
			return Constants.ACC_PUBLIC;
		}else{
			throw new InternalError("Access flag cannot be set: " + method);
		}
	}
	
	@Override
	public IClassHierarchy getClassHierarchy() {
		// TODO Auto-generated method stub
		return targetClass.getClassHierarchy();
	}

	@Override
	public IClassLoader getClassLoader() {
		// TODO Auto-generated method stub
		return targetClass.getClassLoader();
	}

	@Override
	public boolean isInterface() {
		// TODO Auto-generated method stub
		return targetClass.isInterface();
	}

	@Override
	public boolean isAbstract() {
		// TODO Auto-generated method stub
		return targetClass.isAbstract();
	}

	@Override
	public boolean isPublic() {
		// TODO Auto-generated method stub
		return targetClass.isPublic();
	}

	@Override
	public boolean isPrivate() {
		// TODO Auto-generated method stub
		return targetClass.isPrivate();
	}

	@Override
	public int getModifiers() throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return targetClass.getModifiers();
	}

	@Override
	public IClass getSuperclass() {
		// TODO Auto-generated method stub
		return targetClass.getSuperclass();
	}

	@Override
	public Collection<? extends IClass> getDirectInterfaces() {
		// TODO Auto-generated method stub
		return targetClass.getDirectInterfaces();
	}

	@Override
	public Collection<IClass> getAllImplementedInterfaces() {
		// TODO Auto-generated method stub
		return targetClass.getAllImplementedInterfaces();
	}

	@Override
	public IMethod getMethod(Selector selector) {
		// TODO Auto-generated method stub
		return targetClass.getMethod(selector);
	}

	@Override
	public IField getField(Atom name) {
		// TODO Auto-generated method stub
		IField f = targetClass.getField(name);
		if(f == null && mFields.containsKey(name))
			return mFields.get(name);
		return f;
	}

	@Override
	public IField getField(Atom name, TypeName type) {
		// TODO Auto-generated method stub
		IField f = targetClass.getField(name, type);
		if(f == null && mFields.containsKey(name)){
			IField mf = mFields.get(name);
			if(mf.getFieldTypeReference().getName().equals(type))
				return mf;
			else
				return null;
		}
		return f;
	}

	@Override
	public TypeReference getReference() {
		// TODO Auto-generated method stub
		return targetClass.getReference();
	}

	@Override
	public String getSourceFileName() throws NoSuchElementException {
		// TODO Auto-generated method stub
		return targetClass.getSourceFileName();
	}

	@Override
	public Reader getSource() throws NoSuchElementException {
		// TODO Auto-generated method stub
		return targetClass.getSource();
	}

	@Override
	public IMethod getClassInitializer() {
		// TODO Auto-generated method stub
		return targetClass.getClassInitializer();
	}

	@Override
	public boolean isArrayClass() {
		// TODO Auto-generated method stub
		return targetClass.isArrayClass();
	}

	@Override
	public Collection<IMethod> getDeclaredMethods() {
		// TODO Auto-generated method stub
		return targetClass.getDeclaredMethods();
	}

	@Override
	public Collection<IField> getAllInstanceFields() {
		// TODO Auto-generated method stub
		return targetClass.getAllInstanceFields();
	}

	@Override
	public Collection<IField> getAllStaticFields() {
		// TODO Auto-generated method stub
		return targetClass.getAllStaticFields();
	}

	@Override
	public Collection<IField> getAllFields() {
		// TODO Auto-generated method stub
		Collection<IField> mfset = mFields.values();
		Collection<IField> fset = targetClass.getAllFields();
		fset.addAll(mfset);
		return fset;
	}

	@Override
	public Collection<IMethod> getAllMethods() {
		// TODO Auto-generated method stub
		return targetClass.getAllMethods();
	}

	@Override
	public Collection<IField> getDeclaredInstanceFields() {
		// TODO Auto-generated method stub
		return targetClass.getDeclaredInstanceFields();
	}

	@Override
	public Collection<IField> getDeclaredStaticFields() {
		// TODO Auto-generated method stub
		return targetClass.getDeclaredStaticFields();
	}

	@Override
	public TypeName getName() {
		// TODO Auto-generated method stub
		return targetClass.getName();
	}

	@Override
	public boolean isReferenceType() {
		// TODO Auto-generated method stub
		return targetClass.isReferenceType();
	}

	@Override
	public Collection<Annotation> getAnnotations() {
		// TODO Auto-generated method stub
		return targetClass.getAnnotations();
	}

}
