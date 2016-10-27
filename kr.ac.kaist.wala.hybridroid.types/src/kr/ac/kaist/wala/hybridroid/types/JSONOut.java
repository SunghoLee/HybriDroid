/**
 * 
 */
package kr.ac.kaist.wala.hybridroid.types;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
	}
	
	@SuppressWarnings("unchecked")
	public String toJSONString(){
		JSONObject outer = new JSONObject();
		JSONObject bridge = new JSONObject();
		JSONObject klass = new JSONObject();
		
		Map<String, String> bridgeMap = new HashMap<String, String>();
		Map<String, ClassInfo> classMap = new HashMap<String, ClassInfo>();
		
		for(Set<BridgeInfo> bis : m.values()){
			for(BridgeInfo bi : bis){
				String cn = bi.getClassInfo().getClassName();
				bridgeMap.put(bi.getName(), cn);
				classMap.put(cn, bi.getClassInfo());
			}
		}
		
		for(String n : bridgeMap.keySet()){
			bridge.put(n, bridgeMap.get(n));
		}
		
		outer.put("bridge", bridge);
		
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
