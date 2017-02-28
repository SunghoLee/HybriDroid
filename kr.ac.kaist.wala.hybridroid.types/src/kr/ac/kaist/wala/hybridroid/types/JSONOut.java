/**
 * 
 */
package kr.ac.kaist.wala.hybridroid.types;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kr.ac.kaist.hybridroid.util.data.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import kr.ac.kaist.wala.hybridroid.types.bridge.BridgeInfo;
import kr.ac.kaist.wala.hybridroid.types.bridge.ClassInfo;
import kr.ac.kaist.wala.hybridroid.types.bridge.MethodInfo;

/**
 * @author Sungho Lee
 *
 */
public class JSONOut {
	private Map<File, Set<BridgeInfo>> m;
	
	public JSONOut(Map<File, Set<BridgeInfo>> m){
		this.m = m;
//		System.out.println(m);
	}
	
	@SuppressWarnings("unchecked")
	public String toJSONString() throws IOException{
		JSONObject outer = new JSONObject();
		JSONObject js2bridge = new JSONObject();
		JSONObject klass = new JSONObject();
		
		Map<String, Set<Pair<String, String>>> jsBridgeMap = new HashMap<String, Set<Pair<String, String>>>();
		Map<String, ClassInfo> classMap = new HashMap<String, ClassInfo>();
		
		for(File f : m.keySet()){
			Set<BridgeInfo> bis = m.get(f);
			String jsPath = f.getCanonicalPath();
			if(bis != null && !bis.isEmpty()){
				for(BridgeInfo bi : bis){
					String cn = bi.getClassInfo().getClassName();
					if(!jsBridgeMap.containsKey(jsPath))
						jsBridgeMap.put(jsPath, new HashSet<Pair<String, String>>());
					jsBridgeMap.get(jsPath).add(Pair.make(bi.getName(), cn));
					classMap.put(cn, bi.getClassInfo());
				}
			}
		}
		
		for(String n : jsBridgeMap.keySet()){
			JSONObject bridge = new JSONObject();

			Set<Pair<String, String>> bridgeMap = jsBridgeMap.get(n);

			for(Pair<String, String> p : bridgeMap){
				String b = p.fst();
				String o = p.snd();
				if(!bridge.containsKey(b))
					bridge.put(b, new JSONArray());
				((JSONArray)(bridge.get(b))).add(o);
			}
			js2bridge.put(n, bridge);
		}
		
		outer.put("js2bridge", js2bridge);
		
		for(String n : classMap.keySet()){
			ClassInfo c = classMap.get(n);
			JSONArray methods = new JSONArray();
			
			for(MethodInfo mi : c.getAllAccessibleMethods()){
				JSONObject method = new JSONObject();
				JSONArray params = new JSONArray();
				method.put("name", mi.getName().toString());
				for(int i = (mi.isStatic()? 0 : 1); i < mi.getNumberOfParameters(); i++){
					String ptn = mi.getParameterType(i).getName().getClassName().toString();
					params.add(ptn);
				}
				method.put("params", params);
				method.put("result", mi.getReturnType().getName().getClassName().toString());
				methods.add(method);
			}
			klass.put(c.getClassName(), methods);
		}
		outer.put("classes", klass);
		return outer.toJSONString();
	}
}
