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
package kr.ac.kaist.hybridroid.analysis.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SmaliParser {
	private static final String RES_TYPE = ".field public static final ";
	private static final String CLASS_TYPE = ".class ";
	private static final String SUPER_TYPE = ".super ";
	private static final String SOURCE_TYPE = ".source ";
	private static final String COMMENT_TYPE = "#";
	public SmaliParser(){
		
	}
	
	public Map<String, Integer> parseResource(File smali){
		Map<String, Integer> resourceMap = new HashMap<String, Integer>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(smali));
			String line;
			
			while((line = br.readLine()) != null){
				if(line.startsWith(RES_TYPE) && line.contains(" = ")){
					line = line.substring(RES_TYPE.length(), line.length());
					String nameWithType = line.substring(0, line.lastIndexOf("=")).replace(" ", "");
					String value = line.substring(line.lastIndexOf("=") + 1, line.length()).replace(" ", "");
					String name = nameWithType.substring(0, nameWithType.lastIndexOf(":"));
					try{
						int intValue = Integer.parseInt(value.replace("0x", ""), 16);
						resourceMap.put(name, intValue);
					}catch(NumberFormatException nfe){
						continue;
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new InternalError("Failed to parse resource info : " + smali);
		}finally{
			if(br != null)
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		return resourceMap;
	}
	
	public boolean isResource(File smali){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(smali));
			String line;
			
			while((line = br.readLine()) != null){
				if(line.startsWith(CLASS_TYPE))
					continue;
				else if(line.startsWith(SUPER_TYPE))
					continue;
				else if(line.startsWith(SOURCE_TYPE))
					continue;
				else if(line.startsWith(COMMENT_TYPE))
					continue;
				else if(!line.startsWith(RES_TYPE) || !line.contains(" = ") || !line.contains(":I") || !line.equals("")){
					return false;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new InternalError("Failed to parse resource info : " + smali);
		}finally{
			if(br != null)
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		return true;
	}
}
