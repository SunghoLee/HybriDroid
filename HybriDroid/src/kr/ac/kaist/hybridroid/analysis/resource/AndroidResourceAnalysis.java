package kr.ac.kaist.hybridroid.analysis.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.util.debug.Assertions;


public class AndroidResourceAnalysis {
	
	// the order is important!! because it is compared using 'startWith'. so longest prefix first.
	private static String[] cpPrefix = {
			".class public final ",
			".class private final ",
			".class protected final ",
			".class public ",
			".class private ",
			".class protected ",
			".class synthetic ", 
			".class "
	};
	
	private String apk;
	private String decompPath;
	private Map<String, Map<Integer, String>> strRes;
	private String dirPath;
	private Map<String, ResourceInfo> infoMap;
	
	public AndroidResourceAnalysis(String apk){
		this.apk = apk;
		strRes = new HashMap<String, Map<Integer, String>>();
		infoMap = new HashMap<String, ResourceInfo>();
		extractResources();
	}
	
	private void extractResources(){
		decompPath = AndroidDecompiler.decompile(apk);
		if(decompPath == null)
			throw new InternalError("cannot decompile : " + apk);
		File dir = new File(decompPath);
		if(!dir.exists() && !dir.isDirectory())
			throw new InternalError("decompile path is wrong: " + decompPath);
		
		try {
			dirPath = dir.getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File smaliDir = null;
		Set<File> valueDirs = new HashSet<File>();
		
		for(File subF : dir.listFiles()){
			if(subF.isDirectory() && subF.getName().equals("smali"))
				smaliDir = subF;
			
			if(subF.isDirectory() && subF.getName().equals("res")){
				for(File subsubF : subF.listFiles()){
					if(subsubF.isDirectory() && subsubF.getName().startsWith("values")){
						valueDirs.add(subsubF);
					}
				}
			}
		}
		
		if(smaliDir == null || valueDirs.isEmpty()){
			throw new InternalError("there is no smali dir or string resource dir.");
		}
		
		Set<File> rSet = getFiles(smaliDir, "R$string.smali", "R.java");
		if(rSet.isEmpty())
			throw new InternalError("there is no R$string.smali file.");
//		
		
		for(File f : rSet){
			String classpath = getClassPath(f);
			infoMap.put(classpath, new ResourceInfo(f, classpath));
		}
		
		Map<String, Integer> rRes = new HashMap<String, Integer>();
		SmaliParser sp = new SmaliParser();
		for(File r : rSet){
			rRes.putAll(sp.parseResource(r));
		}
		
		XMLStringResourceReader xsrr = new XMLStringResourceReader();
		
		for(File f : valueDirs){
			Set<File> stringXmlFiles = new HashSet<File>();
			String region;
			
			switch(f.getName()){
			case "values":
				region = "common";
				break;
			default:
				String fname = f.getName();
				region = fname.substring(fname.indexOf("-") + 1, fname.length());
				break;
			}
			stringXmlFiles.addAll(getFiles(f, "strings.xml", null));
			Map<String, String> xmlRes = new HashMap<String, String>();
			for(File stringXml : stringXmlFiles){
				xmlRes.putAll(xsrr.parseResource(stringXml));
			}
			if(!xmlRes.isEmpty())
				strRes.put(region, matchResource(rRes, xmlRes));
		}
	}
	
	public boolean isResourceClass(String path){
		return infoMap.containsKey(path);
	}
	
	public ResourceInfo getInfo(String path){
		if(infoMap.containsKey(path))
			return infoMap.get(path);
		else{
			File f = getFile(new File(dirPath), path);
			if (f == null)
				return null;
			
			infoMap.put(path, new ResourceInfo(f, path));
			return infoMap.get(path);
		}
	}
	
	public String getDir(){
		return dirPath;
	}
	
	public void rm(File f){
		if(f.isDirectory()){
			for(File subF : f.listFiles())
				rm(subF);
			f.delete();
		}else
			f.delete();
	}
	
	public String getCommonString(int addr){
		return strRes.get("common").get(addr);
	}
	
	public String getRegionString(int addr, String region){
		return strRes.get(region).get(addr);
	}
	
	private Map<Integer, String> matchResource(Map<String, Integer> rRes, Map<String, String> vRes){
		Map<Integer, String> res = new HashMap<Integer, String>();
		
		for(String key : rRes.keySet()){
			Integer addr = rRes.get(key);
			String strValue = vRes.get(key);
			res.put(addr, strValue);
		}
		return res;
	}
	
	private Set<File> getFiles(File f, String name, String source){
		Set<File> rSet = new HashSet<File>();
		if(f.isDirectory()){
			File[] flist = f.listFiles();
			if(flist == null)
				try {
					System.out.println("can't read: " + f.getCanonicalPath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else
				for(File subF : flist)
					rSet.addAll(getFiles(subF, name, source));
		}else if(f.getName().equals(name)){
			rSet.add(f);
		}else if(source != null && isCompiledFrom(f, source)){
			rSet.add(f);
		}else if((new SmaliParser()).isResource(f))
			rSet.add(f);
		return rSet;
	}
	
	private File getFile(File f, String classPath){
		if (f.isDirectory()) {
			File[] flist = f.listFiles();
			if (flist == null)
				try {
					System.out.println("can't read: " + f.getCanonicalPath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else
				for (File subF : flist) {
					File res = getFile(subF, classPath);
					if (res != null)
						return res;
				}
		}else if (f.isFile()) {
			String path = getClassPath(f);
			if(path != null && path.equals(classPath))
				return f;
		}
		return null;
	}
	
	private boolean isCompiledFrom(File f, String source){
		String sourceClass = ".source \"" + source + "\"";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			int nline = 0;
			String s;
			while((s = br.readLine()) != null){
				nline++;
				if(nline == 3){
					if(s.equals(sourceClass))
						return true;
					else 
						return false;
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(br != null)
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		return false;
	}
	
	private String getClassPath(File f){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String s = null;

			s = br.readLine();
			
			if(s == null)
				return null;
			
			for(String prefix : cpPrefix){
				if(s.startsWith(prefix)){
					return s.substring(prefix.length(), s.length()-1);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(br != null)
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		return null;
	}
	
	public static class ResourceInfo{
		private String classpath;
		private Map<String, Integer> vMap;
		
		private ResourceInfo(File f, String classpath){
			if(classpath == null)
				try {
					Assertions.UNREACHABLE("the resource file " + f.getCanonicalPath() + " does not have class path.");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			this.classpath = classpath;
			vMap = (new SmaliParser()).parseResource(f);
		}
		
		public String getClassPath(){
			return classpath;
		}
		
		public int getResourceValue(String fieldName){
			if(!vMap.containsKey(fieldName))
				Assertions.UNREACHABLE("the " + fieldName + " is not a member of " + classpath);
			
			return vMap.get(fieldName);
		}
		
		public boolean isDeclaredResorce(String fieldName){
			return vMap.containsKey(fieldName);
		}
	}
}
