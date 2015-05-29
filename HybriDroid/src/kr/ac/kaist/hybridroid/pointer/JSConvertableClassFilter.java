package kr.ac.kaist.hybridroid.pointer;

import kr.ac.kaist.hybridroid.types.AndroidJavaJavaScriptTypeMap;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.propagation.FilteredPointerKey.MultipleClassesFilter;
import com.ibm.wala.ipa.callgraph.propagation.PointsToSetVariable;
import com.ibm.wala.ipa.callgraph.propagation.PropagationSystem;
import com.ibm.wala.util.debug.Assertions;

public class JSConvertableClassFilter extends MultipleClassesFilter {

	public static JSConvertableClassFilter make(IClass concreteType){
		if(!AndroidJavaJavaScriptTypeMap.isJava2JSConvertable(concreteType))
			Assertions.UNREACHABLE("cannot convert this type to a JS type : " + concreteType);
		
		IClass[] cTypes = new IClass[2];
		cTypes[0] = concreteType;
		cTypes[1] = AndroidJavaJavaScriptTypeMap.java2JSTypeConvert(concreteType);
		
		return new JSConvertableClassFilter(cTypes);
	}
	
	private JSConvertableClassFilter(IClass[] concreteTypes){
		super(concreteTypes);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

	@Override
	public IClass[] getConcreteTypes() {
		// TODO Auto-generated method stub
		return super.getConcreteTypes();
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return super.equals(o);
	}

	@Override
	public boolean addFiltered(PropagationSystem system, PointsToSetVariable L,
			PointsToSetVariable R) {
		// TODO Auto-generated method stub
		return super.addFiltered(system, L, R);
	}

	@Override
	public boolean addInverseFiltered(PropagationSystem system,
			PointsToSetVariable L, PointsToSetVariable R) {
		// TODO Auto-generated method stub
		return super.addInverseFiltered(system, L, R);
	}

	@Override
	public boolean isRootFilter() {
		// TODO Auto-generated method stub
		return false;
	}

	
}
