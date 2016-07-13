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
package kr.ac.kaist.hybridroid.callgraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.jf.dexlib.Util.FileUtils;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.strings.Atom;

import kr.ac.kaist.hybridroid.util.debug.Debug;
import kr.ac.kaist.hybridroid.utils.LoaderUtils;

public class JsAnalysisScopeReader {
	private File file;
	private static String EXTENSION_HTML = "html";
	private static String EXTENSION_JS = "js";
	private static String BASE_DIR = "tmp";
	private List<String> htmlList;
	private List<String> jsList;
	
	public JsAnalysisScopeReader(File _file) throws IOException{
		//enable this class to print debug messages
		Debug.setDebuggable(this, true);
		
		file = _file;
		
		htmlList = new ArrayList<String>();
		jsList = new ArrayList<String>();
		File baseDirectory = new File(BASE_DIR);
		if(baseDirectory.exists())
			deleteFilesAtFolder(baseDirectory);
		baseDirectory.mkdir();
		
		byte[] magic = FileUtils.readFile(file,0,8);
		ZipFile zipFile = null;
		
		if(magic[0] == 0x50 && magic[1] == 0x4B){ // is it a zip file?
			zipFile = new ZipFile(file);
			ZipInputStream zInputStream = new ZipInputStream(new FileInputStream(_file));
			ZipEntry entry = zInputStream.getNextEntry();
			while(entry != null){
				String fileName = entry.getName();
				if(fileName.endsWith(EXTENSION_HTML)){
					Debug.printMsg("\t( HTML ) "+entry.getName());
					extractFiles(entry, zInputStream);
					htmlList.add(BASE_DIR+"/"+entry.getName());
				}
				else if(fileName.endsWith(EXTENSION_JS)){
					Debug.printMsg("\t( JS ) "+entry.getName());
					extractFiles(entry, zInputStream);
					jsList.add(BASE_DIR+"/"+entry.getName());
				}
				entry = zInputStream.getNextEntry();
			}
			zInputStream.closeEntry();
			zInputStream.close();
			zipFile.close();
		}
	}
	
	public boolean isHybridApp(){
		if(htmlList.size() == 0 && jsList.size() == 0)
			return false;
		return true;
	}
	
	private void extractFiles(ZipEntry _entry, ZipInputStream _inStream) throws IOException{
		String addr = _entry.getName();
		byte[] buffer = new byte[1024];
		int seperateIndex = addr.indexOf("/");
		String prefixDirName;
		String directoryPath = BASE_DIR;
		
		while(seperateIndex != -1){
			prefixDirName = addr.substring(0, seperateIndex);
			directoryPath += "/"+prefixDirName;
			addr = addr.substring(seperateIndex+1, addr.length());
			File dir = new File(directoryPath);
			dir.mkdir();
			seperateIndex = addr.indexOf("/");
		}
		
		String filePath = directoryPath+"/"+addr;
		Debug.printMsg("@EXTRACTING "+filePath+"...");
		File file = new File(filePath);
		FileOutputStream outStream = new FileOutputStream(file);
		int len = -1;
		while((len = _inStream.read(buffer))> 0){
			outStream.write(buffer, 0, len);
		}
		Debug.printMsg("\tDone: "+filePath+".");
		outStream.close();
	}
	
	private void deleteFilesAtFolder(File _dir){
		File[] listFiles = _dir.listFiles();
		for(File file : listFiles){
			if(file.isDirectory()){
				deleteFilesAtFolder(file);
			}
			file.delete();
		}
	}
	
	public List<String> getJSList(){
		return jsList;
	}
	
	public List<String> getHTMLList(){
		return htmlList;
	}
	
	public List<IMethod> getJavascriptInterfaceMethods(IClassHierarchy cha){
		List<IMethod> methods = new ArrayList<IMethod>();
		Atom interfaceName = Atom.findOrCreateAsciiAtom("JavascriptInterface");
		for (IClass ic:cha) {
			if (!LoaderUtils.fromLoader(ic, ClassLoaderReference.Application)) {
				continue;
			}
			for (IMethod im:ic.getAllMethods()) {
				Collection<Annotation> annSet = im.getAnnotations();
				for(Annotation ann : annSet){
					Atom className = ann.getType().getName().getClassName();
					if(className.equals(interfaceName)){
						methods.add(im);
					}
				}
			}
		}
		return methods;
	}
}
