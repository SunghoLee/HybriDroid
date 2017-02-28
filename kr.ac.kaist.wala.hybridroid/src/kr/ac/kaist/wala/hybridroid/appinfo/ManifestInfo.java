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
package kr.ac.kaist.wala.hybridroid.appinfo;

import kr.ac.kaist.wala.hybridroid.appinfo.properties.Property;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ManifestInfo {
	private Property root;
	
	public ManifestInfo(Property root){
		this.root = root;
	}		
	
	public String getPackageName(){
		return root.getAttributeValue("package");
	}
	
	public Set<String> getActivityNames(){
		Set<String> names = new HashSet<String>();
		Set<Property> activityProps = getProperties(Property.ACTIVITY);
		for(Property activityProp : activityProps){
			names.add(Property.tagToString(activityProp.getTag()));
		}
		return names;
	}
	
	public Set<Property> getProperties(String name){
		return getProperties(Property.nameToTag(name));
	}
	
	public Set<Property> getProperties(int tag){
		Set<Property> result = new HashSet<Property>();
		List<Property> bfs = new ArrayList<Property>();
		bfs.add(root);
		
		while(!bfs.isEmpty()){
			Property prop = bfs.get(0);
			bfs.remove(0);
			if(prop.getTag() == tag)
				result.add(prop);
			bfs.addAll(prop.getChildren());
		}
		return result;
	}
	
	public String getAttribute(Property p, String name){
		if(p.isDefinedAttribute(name))
			return p.getAttributeValue(name);
		else
			return null;
	}
}
