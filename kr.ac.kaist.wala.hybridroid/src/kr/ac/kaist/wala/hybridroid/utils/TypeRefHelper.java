package kr.ac.kaist.wala.hybridroid.utils;

import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;

/**
 * Created by leesh on 08/11/2017.
 */
public class TypeRefHelper {
    public static TypeReference findTypeReferenceByName(TypeName tn, ClassLoaderReference clr, boolean isCLRFixed){
        TypeReference tr = TypeReference.find(clr, tn);

        if(tr == null && !isCLRFixed)
            tr = TypeReference.find((clr.equals(ClassLoaderReference.Primordial))? ClassLoaderReference.Application : ClassLoaderReference.Primordial, tn);

        return tr;
    }
}
