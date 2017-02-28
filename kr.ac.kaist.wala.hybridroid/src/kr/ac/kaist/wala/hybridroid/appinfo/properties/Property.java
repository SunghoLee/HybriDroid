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
package kr.ac.kaist.wala.hybridroid.appinfo.properties;

import kr.ac.kaist.wala.hybridroid.appinfo.XMLManifestReader;

import java.util.*;

public class Property {
	
	public final static int ACTION = 1;
	public final static int ACTIVITY = 2;
	public final static int ACTIVITY_ALIAS = 3;
	public final static int APPLICATION = 4;
	public final static int CATEGORY = 5;
	public final static int COMPATIBLE_SCREENS = 6;
	public final static int DATA = 7;
	public final static int GRANT_URI_PERMISSION = 8;
	public final static int INSTRUMENTATION = 9;
	public final static int INTENT_FILTER = 10;
	public final static int MANIFEST = 11;
	public final static int META_DATA = 12;
	public final static int PATH_PERMISSION = 13;
	public final static int PERMISSION = 14;
	public final static int PERMISSION_GROUP = 15;
	public final static int PERMISSION_TREE = 16;
	public final static int PROVIDER = 17;
	public final static int RECEIVER = 18;
	public final static int SERVICE = 19;
	public final static int SUPPORTS_GL_TEXTURE = 20;
	public final static int SUPPORTS_SCREENS = 21;
	public final static int USES_CONFIGURATION = 22;
	public final static int USES_FEATURE = 23;
	public final static int USES_LIBRARY = 24;
	public final static int USES_PERMISSION = 25;
	public final static int USES_SDK = 26;
	public final static int SCREEN = 27;
	
	private final static int INDENT_INC = 2;
	
	private Map<String, String> attributes;
	private Set<Property> children;
	private Property parent;
	private int tag;
	
	public Property(int tag, Property parent){
		this.tag = tag;
		this.parent = parent;
		attributes = new HashMap<String, String>();
		children = new HashSet<Property>();
	}

	public int getTag(){
		return tag;
	}

	public void addAttribute(String name, String value){
		if(attributes.containsKey(name)){
//			Assertions.UNREACHABLE(" " + name + " attribute already exists.");
		}else{
			attributes.put(name, value);
		}
	}
	
	public Map<String, String> getAttributes() {
		// TODO Auto-generated method stub
		return attributes;
	}

	public String getAttributeValue(String attrName) {
		// TODO Auto-generated method stub
		if(attributes.containsKey(attrName))
			return attributes.get(attrName);
//		Assertions.UNREACHABLE(" " + attrName + " does not exists in " + tagToString(tag));
		return "";
	}

	public void addChild(Property child) {
		// TODO Auto-generated method stub
		children.add(child);
	}

	public Set<Property> getChildren() {
		// TODO Auto-generated method stub
		return children;
	}

	public Iterator<Property> iterateChildren() {
		// TODO Auto-generated method stub
		return children.iterator();
	}

	public Property getParent() {
		// TODO Auto-generated method stub
		return parent;
	}
	
	public static String tagToString(int tag){
		switch(tag){
		case ACTION : return XMLManifestReader.ACTION_TAG;
		case ACTIVITY : return XMLManifestReader.ACTIVITY_TAG;
		case ACTIVITY_ALIAS : return XMLManifestReader.ACTIVITY_ALIAS_TAG;
		case APPLICATION : return XMLManifestReader.APPLICATION_TAG;
		case CATEGORY : return XMLManifestReader.CATEGORY_TAG;
		case COMPATIBLE_SCREENS : return XMLManifestReader.COMPATIBLE_SCREENS_TAG;
		case DATA : return XMLManifestReader.DATA_TAG; 
		case GRANT_URI_PERMISSION : return XMLManifestReader.GRANT_URI_PERMISSION_TAG; 
		case INSTRUMENTATION : return XMLManifestReader.INSTRUMENTATION_TAG; 
		case INTENT_FILTER : return XMLManifestReader.INTENT_FILTER_TAG; 
		case MANIFEST : return XMLManifestReader.MANIFEST_TAG; 
		case META_DATA : return XMLManifestReader.META_DATA_TAG; 
		case PATH_PERMISSION : return XMLManifestReader.PATH_PERMISSION_TAG; 
		case PERMISSION : return XMLManifestReader.PERMISSION_TAG; 
		case PERMISSION_GROUP : return XMLManifestReader.PERMISSION_GROUP_TAG; 
		case PERMISSION_TREE : return XMLManifestReader.PERMISSION_TREE_TAG; 
		case PROVIDER : return XMLManifestReader.PROVIDER_TAG; 
		case RECEIVER : return XMLManifestReader.RECEIVER_TAG; 
		case SERVICE : return XMLManifestReader.SERVICE_TAG; 
		case SUPPORTS_GL_TEXTURE : return XMLManifestReader.SUPPORTS_GL_TEXTURE_TAG; 
		case SUPPORTS_SCREENS : return XMLManifestReader.SUPPORTS_SCREENS_TAG; 
		case USES_CONFIGURATION : return XMLManifestReader.USES_CONFIGURATION_TAG; 
		case USES_FEATURE : return XMLManifestReader.USES_FEATURE_TAG; 
		case USES_LIBRARY : return XMLManifestReader.USES_LIBRARY_TAG; 
		case USES_PERMISSION : return XMLManifestReader.USES_PERMISSION_TAG; 
		case USES_SDK : return XMLManifestReader.USES_SDK_TAG; 
		case SCREEN : return XMLManifestReader.SCREEN_TAG; 
		default:
//			Assertions.UNREACHABLE(" Invalid Tag number: " + tag);
			return "error";
		}
	}
	
	public static int nameToTag(String name){
		switch(name){
		case XMLManifestReader.ACTION_TAG : return ACTION;
		case XMLManifestReader.ACTIVITY_TAG : return ACTIVITY;
		case XMLManifestReader.ACTIVITY_ALIAS_TAG : return ACTIVITY_ALIAS;
		case XMLManifestReader.APPLICATION_TAG : return APPLICATION;
		case XMLManifestReader.CATEGORY_TAG : return CATEGORY;
		case XMLManifestReader.COMPATIBLE_SCREENS_TAG : return COMPATIBLE_SCREENS;
		case XMLManifestReader.DATA_TAG : return DATA; 
		case XMLManifestReader.GRANT_URI_PERMISSION_TAG : return GRANT_URI_PERMISSION; 
		case XMLManifestReader.INSTRUMENTATION_TAG : return INSTRUMENTATION; 
		case XMLManifestReader.INTENT_FILTER_TAG : return INTENT_FILTER; 
		case XMLManifestReader.MANIFEST_TAG : return MANIFEST; 
		case XMLManifestReader.META_DATA_TAG : return META_DATA; 
		case XMLManifestReader.PATH_PERMISSION_TAG : return PATH_PERMISSION; 
		case XMLManifestReader.PERMISSION_TAG : return PERMISSION; 
		case XMLManifestReader.PERMISSION_GROUP_TAG : return PERMISSION_GROUP; 
		case XMLManifestReader.PERMISSION_TREE_TAG : return PERMISSION_TREE; 
		case XMLManifestReader.PROVIDER_TAG : return PROVIDER; 
		case XMLManifestReader.RECEIVER_TAG : return RECEIVER; 
		case XMLManifestReader.SERVICE_TAG : return SERVICE; 
		case XMLManifestReader.SUPPORTS_GL_TEXTURE_TAG : return SUPPORTS_GL_TEXTURE; 
		case XMLManifestReader.SUPPORTS_SCREENS_TAG : return SUPPORTS_SCREENS; 
		case XMLManifestReader.USES_CONFIGURATION_TAG : return USES_CONFIGURATION; 
		case XMLManifestReader.USES_FEATURE_TAG : return USES_FEATURE; 
		case XMLManifestReader.USES_LIBRARY_TAG : return USES_LIBRARY; 
		case XMLManifestReader.USES_PERMISSION_TAG : return USES_PERMISSION; 
		case XMLManifestReader.USES_SDK_TAG : return USES_SDK; 
		case XMLManifestReader.SCREEN_TAG : return SCREEN; 
		default:
//			Assertions.UNREACHABLE(" Invalid Tag name: " + name);
			return -1;
		}
	}
	
	@Override
	public String toString(){
		return toString(0);
	}
	
	public boolean isDefinedAttribute(String name){
		return attributes.containsKey(name);
	}
	
	public String toString(int indentNum){
		String indent = (indentNum > 0)? new String(new char[indentNum]).replace('\0', ' ') : "";
		String attrIndent = indent + " ";
		
		String res = indent + "<" + tagToString(tag) + ((attributes.size() != 0)? "\n" : "");
		
		for(String key : attributes.keySet()){
			String attrValue = attributes.get(key);

			res += attrIndent + key + " = ";
			res += "\"" + attrValue + "\"\n";
		}
		
		res += ((attributes.size() != 0)? indent : "") + ">\n";
		
		Iterator<Property> childIter = children.iterator();
		
		while(childIter.hasNext()){
			Property child = childIter.next();
			res += child.toString(indentNum + INDENT_INC);
		}
		
		res += indent + "</" + tagToString(tag) + ">\n";
		return res;
	}
}
