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

import com.ibm.wala.properties.WalaProperties;
import kr.ac.kaist.wala.hybridroid.appinfo.properties.Property;
import kr.ac.kaist.wala.hybridroid.shell.Shell;
import kr.ac.kaist.wala.hybridroid.util.debug.Debug;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.StringTokenizer;

public class XMLManifestReader {

	public final static String APK_TOOL = "apktool.jar";
	public final static String TMP_FOLDER = "tmp";
	public final static String MANIFEST_FILE = "AndroidManifest.xml";
	
	public final static String ACTION_TAG = "action";
	public final static String ACTIVITY_TAG = "activity";
	public final static String ACTIVITY_ALIAS_TAG = "activity_alias";
	public final static String APPLICATION_TAG = "application";
	public final static String CATEGORY_TAG = "category";
	public final static String COMPATIBLE_SCREENS_TAG = "compatible-screens";
	public final static String DATA_TAG = "data";
	public final static String GRANT_URI_PERMISSION_TAG = "grant-uri-permission";
	public final static String INSTRUMENTATION_TAG = "instrumentation";
	public final static String INTENT_FILTER_TAG = "intent-filter";
	public final static String MANIFEST_TAG = "manifest";
	public final static String META_DATA_TAG = "meta-data";
	public final static String PATH_PERMISSION_TAG = "path-permission";
	public final static String PERMISSION_TAG = "permission";
	public final static String PERMISSION_GROUP_TAG = "permission-group";
	public final static String PERMISSION_TREE_TAG = "permission-tree";
	public final static String PROVIDER_TAG = "provider";
	public final static String RECEIVER_TAG = "receiver";
	public final static String SERVICE_TAG = "service";
	public final static String SUPPORTS_GL_TEXTURE_TAG = "supports-gl-texture";
	public final static String SUPPORTS_SCREENS_TAG = "supports-screens";
	public final static String USES_CONFIGURATION_TAG = "uses-configuration";
	public final static String USES_FEATURE_TAG = "uses-feature";
	public final static String USES_LIBRARY_TAG = "uses-library";
	public final static String USES_PERMISSION_TAG = "uses-permission";
	public final static String USES_SDK_TAG = "uses-sdk";
	public final static String SCREEN_TAG = "screen";
	
	private Property rootProp;
	private Property pProp;
	private Property cProp;
	
	public XMLManifestReader(String apkFile) {
		if (apkFile == null) {
			throw new IllegalArgumentException("null akpFile");
		}
		try {
			InputStream xmlFile = new FileInputStream(unpackApk(apkFile) + File.separator + MANIFEST_FILE);
			readXML(xmlFile);
			removeDir(TMP_FOLDER);
		} catch (Exception e) {
			System.err.println("Error!");
		}
	}
	
	private String unpackApk(String apkFile){
		Debug.setDebuggable(this, true);
		
		StringTokenizer st = new StringTokenizer(apkFile, File.separator);
		String file = null;
		
		while(st.hasMoreTokens())
			file = st.nextToken();
		
		System.out.println("APK_TOOL_PATH: " + Shell.walaProperties.getProperty(WalaProperties.ANDROID_APK_TOOL));
		String pathToApkTool = Shell.walaProperties.getProperty(WalaProperties.ANDROID_APK_TOOL) + File.separator + APK_TOOL;
		String pathToJava = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		
		ProcessBuilder pb = new ProcessBuilder(pathToJava, "-jar", pathToApkTool, "d", apkFile, "-f", "-o", TMP_FOLDER);
		
		try {
			Process p = pb.start();
			
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			
			String s;
			while((s = stdInput.readLine()) != null)
				Debug.printMsg(s);
			
			while((s = stdError.readLine()) != null)
				Debug.printMsg(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return TMP_FOLDER;
	}
	
	public ManifestInfo getAppInfo(){
		return new ManifestInfo(rootProp);
	}
	
	private void readXML(InputStream xml) throws SAXException, IOException, ParserConfigurationException {
	    SAXHandler handler = new SAXHandler();

	    assert xml != null : "Null xml stream";
	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    factory.newSAXParser().parse(new InputSource(xml), handler);
	  }
	
	private void removeDir(String target){
		ProcessBuilder pb = new ProcessBuilder("rm", "-r", target);
		
		try {
			Process p = pb.start();
			
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			
			String s;
			while((s = stdInput.readLine()) != null)
				Debug.printMsg(s);
			
			while((s = stdError.readLine()) != null)
				Debug.printMsg(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class SAXHandler extends DefaultHandler{

		@Override
		public void startDocument() throws SAXException {
			// TODO Auto-generated method stub
			super.startDocument();
		}

		@Override
		public void endDocument() throws SAXException {
			// TODO Auto-generated method stub
			super.endDocument();
		}

		private Property newProp(int type, Property parent){
			pProp = parent;
			cProp = new Property(type, parent);
			
			if(pProp != null)
				pProp.addChild(cProp);
			
			return cProp;
		}
		
		private Property getCurrentProp(){
			return cProp;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			// TODO Auto-generated method stub
			switch(qName){
			case ACTION_TAG :
				newProp(Property.ACTION, getCurrentProp());
				break;
			case ACTIVITY_TAG :
				newProp(Property.ACTIVITY, getCurrentProp());
				break;
			case ACTIVITY_ALIAS_TAG : 
				newProp(Property.ACTIVITY_ALIAS, getCurrentProp());
				break;
			case APPLICATION_TAG : 
				newProp(Property.APPLICATION, getCurrentProp());
				break;
			case CATEGORY_TAG : 
				newProp(Property.CATEGORY, getCurrentProp());
				break;
			case COMPATIBLE_SCREENS_TAG : 
				newProp(Property.COMPATIBLE_SCREENS, getCurrentProp());
				break;
			case DATA_TAG : 
				newProp(Property.DATA, getCurrentProp());
				break;
			case GRANT_URI_PERMISSION_TAG :
				newProp(Property.GRANT_URI_PERMISSION, getCurrentProp());
				break;
			case INSTRUMENTATION_TAG : 
				newProp(Property.INSTRUMENTATION, getCurrentProp());
				break;
			case INTENT_FILTER_TAG : 
				newProp(Property.INTENT_FILTER, getCurrentProp());
				break;
			case MANIFEST_TAG : // manifest property is root property in AndroidManifest.xml file.
				newProp(Property.MANIFEST, null);
				rootProp = getCurrentProp();
				break;
			case META_DATA_TAG : 
				newProp(Property.META_DATA, getCurrentProp());
				break;
			case PATH_PERMISSION_TAG : 
				newProp(Property.PATH_PERMISSION, getCurrentProp());
				break;
			case PERMISSION_TAG : 
				newProp(Property.PERMISSION, getCurrentProp());
				break;
			case PERMISSION_GROUP_TAG : 
				newProp(Property.PERMISSION_GROUP, getCurrentProp());
				break;
			case PERMISSION_TREE_TAG : 
				newProp(Property.PERMISSION_TREE, getCurrentProp());
				break;
			case PROVIDER_TAG : 
				newProp(Property.PROVIDER, getCurrentProp());
				break;
			case RECEIVER_TAG : 
				newProp(Property.RECEIVER, getCurrentProp());
				break;
			case SERVICE_TAG : 
				newProp(Property.SERVICE, getCurrentProp());
				break;
			case SUPPORTS_GL_TEXTURE_TAG : 
				newProp(Property.SUPPORTS_GL_TEXTURE, getCurrentProp());
				break;
			case SUPPORTS_SCREENS_TAG : 
				newProp(Property.SUPPORTS_SCREENS, getCurrentProp());
				break;
			case USES_CONFIGURATION_TAG : 
				newProp(Property.USES_CONFIGURATION, getCurrentProp());
				break;
			case USES_FEATURE_TAG : 
				newProp(Property.USES_FEATURE, getCurrentProp());
				break;
			case USES_LIBRARY_TAG : 
				newProp(Property.USES_LIBRARY, getCurrentProp());
				break;
			case USES_PERMISSION_TAG : 
				newProp(Property.USES_PERMISSION, getCurrentProp());
				break;
			case USES_SDK_TAG : 
				newProp(Property.USES_SDK, getCurrentProp());
				break;
			case SCREEN_TAG:
				newProp(Property.SCREEN, getCurrentProp());
				break;
			default:
				System.err.println(" this tag can not occur in Manifest.xml: " + qName);
//				Assertions.UNREACHABLE(" this tag can not occur in Manifest.xml: " + qName);
			}
			
			setAttributes(attributes);
		}

		private void setAttributes(Attributes attr){
			for(int i=0; i<attr.getLength(); i++){
				String attrName = attr.getQName(i);
				String attrValue = attr.getValue(i);
				cProp.addAttribute(attrName, attrValue);
			}
		}
		
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			// TODO Auto-generated method stub
			cProp = pProp;
			
			if(!qName.equals(MANIFEST_TAG))
				pProp = cProp.getParent();
		}
		
	}
	
	public Property rootProperty(){
		return rootProp;
	}
}
