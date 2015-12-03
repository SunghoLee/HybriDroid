package kr.ac.kaist.hybridroid.analysis.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SmaliParser {
	private static final String RES_TYPE = ".field public static final ";
	public SmaliParser(){
		
	}
	
	public Map<String, Integer> parseResource(File smali){
		Map<String, Integer> resourceMap = new HashMap<String, Integer>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(smali));
			String line;
			
			while((line = br.readLine()) != null){
				if(line.startsWith(RES_TYPE)){
					line = line.substring(RES_TYPE.length(), line.length());
					String nameWithType = line.substring(0, line.lastIndexOf("=")).replace(" ", "");
					String value = line.substring(line.lastIndexOf("=") + 1, line.length()).replace(" ", "");
					String name = nameWithType.substring(0, nameWithType.lastIndexOf(":"));
					int intValue = Integer.parseInt(value.replace("0x", ""), 16);
					resourceMap.put(name, intValue);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new InternalError("Failed to parse resource info : " + smali);
		}
		
		return resourceMap;
	}
}
