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
package kr.ac.kaist.wala.hybridroid.util.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YMLParser {
	private File f;
	
	public YMLParser(File f){
		this.f = f;
	}
	
	public YMLData parse() throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		List<YMLData> children = parseData(br);
		br.close();
		return new YMLData("Root", children);
	}
	
	private List<YMLData> parseData(BufferedReader br) throws IOException{
		List<YMLData> list = new ArrayList<YMLData>();
		
		String s = null;
		boolean isFirst = true;
		boolean isChild = false;
		
		while((s = br.readLine()) != null && s.contains(":")){
			if(isFirst){
				isFirst = false;
				if(s.startsWith("  "))
					isChild = true;
			}
			
			if(isChild && !s.startsWith("  ")){
				br.reset();
				return list;
			}
			
			s = removeWS(s);
			String name = s.substring(0, s.indexOf(":"));
			
			if(!s.endsWith(":")){
				String value = s.substring(s.indexOf(":") + 1, s.length());
				list.add(new YMLData(name, value));
			}else{
				br.mark(0);
				s = br.readLine();
				if(!s.contains(":")){
					s = removeWS(s);
					list.add(new YMLData(name, s));
				}else{
					br.reset();
					list.add(new YMLData(name, parseData(br)));
				}
			}
			br.mark(0);
		}
		return list;
	}
	
	
	private String removeWS(String s){
		return s.replace(" ", "").replace("\t", "");
	}
	
	public static class YMLData{
		private final String name;
		private String value;
		private List<YMLData> children;
		
		private YMLData(String name){
			this.name = name;
		}
		
		private YMLData(String name, String value){
			this(name);
			this.value = value;
			children = Collections.emptyList();
		}
		
		private YMLData(String name, List<YMLData> children){
			this(name);
			this.children = children;
		}
		
		private YMLData(String name, String value, List<YMLData> children){
			this(name, value);
			this.children = children;
		}
		
		public String getName(){
			return name;
		}
		
		public String getValue(){
			return value;
		}
		
		public List<YMLData> getChildren(){
			return children;
		}
		@Override
		public String toString(){			
			return toString("", (name.equals("Root"))? true : false);
		}
		
		private String toString(String indent, boolean root){
			String res = "";
			
			if(!root)
				res = name + ": " + ((value != null)? value : "");
			
			for(YMLData child : children){
				res += "\n" + (child.toString(indent + "  ", false));
			}
			return indent + res;
		}
	}

}
