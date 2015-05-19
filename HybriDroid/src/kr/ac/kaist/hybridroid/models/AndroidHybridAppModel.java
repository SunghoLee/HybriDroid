package kr.ac.kaist.hybridroid.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.ac.kaist.hybridroid.callgraph.AndroidHybridAnalysisScope;
import kr.ac.kaist.hybridroid.util.data.None;
import kr.ac.kaist.hybridroid.util.data.Option;
import kr.ac.kaist.hybridroid.util.data.Some;

import com.ibm.wala.cast.js.ipa.callgraph.JavaScriptEntryPoints;
import com.ibm.wala.dalvik.ipa.callgraph.impl.AndroidEntryPoint;
import com.ibm.wala.dalvik.util.AndroidEntryPointLocator;
import com.ibm.wala.dalvik.util.AndroidEntryPointLocator.LocatorFlags;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.ComposedEntrypoints;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.collections.HashSetFactory;

public class AndroidHybridAppModel {
	
	static private Map<String, InstanceKey> interfMap;
	static{
		interfMap = new HashMap<String, InstanceKey>();
	}
	
	static public void addJSInterface(String name, InstanceKey objKey){
		interfMap.put(name, objKey);
	}
	
	static public Option<InstanceKey> findJSInterface(String name){
		Option<InstanceKey> opInstKey = (interfMap.containsKey(name))? 
				new Some<InstanceKey>(interfMap.get(name)) : new None<InstanceKey>();
		return opInstKey;
	}
	
	static public void checkJSInterfaces(){
		Set<String> sSet = interfMap.keySet();
		for(String s : sSet)
			System.out.println("# name: "+s+"\n# obj: " + interfMap.get(s));
	}
	
	static public Collection<InstanceKey> getJSInterfaces(){
		return interfMap.values();
	}
	
	static public int numberOfJSInterface(){
		return interfMap.size();
	}
	
	static public ComposedEntrypoints getEntrypoints(IClassHierarchy cha, AndroidHybridAnalysisScope scope){
		Iterable<Entrypoint> jsRoots = new JavaScriptEntryPoints(cha,
				cha.getLoader(scope.getJavaScriptLoader()));

		Set<LocatorFlags> flags = HashSetFactory.make();
		flags.add(LocatorFlags.INCLUDE_CALLBACKS);
		flags.add(LocatorFlags.EP_HEURISTIC);
		flags.add(LocatorFlags.CB_HEURISTIC);
		AndroidEntryPointLocator eps = new AndroidEntryPointLocator(flags);
		List<AndroidEntryPoint> es = eps.getEntryPoints(cha);
		final List<Entrypoint> entries = new ArrayList<Entrypoint>();
		for (AndroidEntryPoint e : es) {
			entries.add(e);
		}

		Iterable<Entrypoint> entrypoints = new Iterable<Entrypoint>() {
			@Override
			public Iterator<Entrypoint> iterator() {
				return entries.iterator();
			}
		};

		//is the order of entrypoints important?
		return new ComposedEntrypoints(jsRoots, entrypoints);
//		return new ComposedEntrypoints(entrypoints, jsRoots);
	}
}
