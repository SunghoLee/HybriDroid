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
package kr.ac.kaist.wala.hybridroid.analysis.resource;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class XMLStringResourceReader {	
	public final static String STRING_TAG = "string";
	public final static String RESOURCE_TAG = "resources";
	public final static String NAME_PROP = "name";
	private String fileName;
	
	public XMLStringResourceReader() {
	}
	
	public Map<String, String> parseResource(File xml){
		try {
			this.fileName = xml.getCanonicalPath();
			return readXML(new FileInputStream(xml));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			try {
				System.err.println("cannot parse the string resource file: " + xml.getCanonicalPath());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return Collections.emptyMap();
	}
	
	private Map<String, String> readXML(InputStream xml) throws SAXException, IOException, ParserConfigurationException {
		final Map<String, String> resourceMap = new HashMap<String, String>();
	    assert xml != null : "Null xml stream";
	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    factory.newSAXParser().parse(new InputSource(xml), new DefaultHandler(){
			private String resName;
			private String resValue;

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
			
			@Override
			public void startElement(String uri, String localName, String qName,
					Attributes attributes) throws SAXException {
				// TODO Auto-generated method stub
				switch(qName){
				case STRING_TAG :
					setAttributes(attributes);
					break;
				case RESOURCE_TAG:
					break;
				default:
					resValue += "<" + qName; 
					for(int i=0; i<attributes.getLength(); i++){
						String attrName = attributes.getQName(i);
						String attrValue = attributes.getValue(i);
						resValue += " " + attrName + "=\"" + attrValue + "\"";
					}
					resValue += ">";
				}
			}

			private void setAttributes(Attributes attr){
				for(int i=0; i<attr.getLength(); i++){
					String attrName = attr.getQName(i);
					String attrValue = attr.getValue(i);
					if(attrName.equals(NAME_PROP))
						resName = attrValue;
				}
			}
			
			@Override
			public void endElement(String uri, String localName, String qName)
					throws SAXException {
				// TODO Auto-generated method stub
				switch(qName){
				case STRING_TAG:
					if(resName == null || resValue == null || resValue.equals(""))
						throw new InternalError("string resource must have name and value.");
					//remove white space before real string value
					resValue = resValue.replace("\n", "");
					resValue = resValue.substring(4, resValue.length());
					
					resourceMap.put(resName, resValue);
					resName = null;
					resValue = "";
					break;
				case RESOURCE_TAG:
					break;
				default:
					resValue += "</" + qName + ">";
				}
			}

			@Override
			public void characters(char[] ch, int start, int length)
					throws SAXException {
				// TODO Auto-generated method stub
				String v = new String(ch).substring(start, start + length);
				resValue += v;
			}
	    });
	    
	    return resourceMap;
	  }
}
