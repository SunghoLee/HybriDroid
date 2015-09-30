package kr.ac.kaist.hybridroid.appinfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kr.ac.kaist.hybridroid.appinfo.properties.Property;


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
