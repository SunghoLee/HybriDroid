package kr.ac.kaist.hybridroid.js.merger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.ibm.wala.cast.ir.translator.TranslatorToCAst.Error;
import com.ibm.wala.cast.js.html.IHtmlCallback;
import com.ibm.wala.cast.js.html.ITag;
import com.ibm.wala.cast.js.html.jericho.JerichoHtmlParser;
import com.ibm.wala.cast.js.html.jericho.JerichoTag;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.debug.Assertions;

import kr.ac.kaist.hybridroid.util.file.OnlineFileReader;


/**
 * Merge several javascript codes in a html into a file of javascript. The codes
 * may contains codes of javascript files or inlining javascript codes.
 * 
 * @author Sungho Lee
 */
public class JSScriptMerger {
	public JSScriptMerger(){}
	
	private File getHtmlFile(String htmlPath) throws InternalError{
		if(!htmlPath.endsWith(".html") && !htmlPath.endsWith(".htm"))
			throw new InternalError("the file is not html file: " + htmlPath);
		
		File html = new File(htmlPath);
		
		if(!html.exists() || !html.isFile())
			throw new InternalError("the html file does not exist: " + htmlPath);
		
		return html;
	}
	
	/**
	 * Check which the html file is a local file or not 
	 * @param htmlPath path of a html file
	 * @return true if the html file is a local file, or false
	 */
	public boolean isLocalHtml(String htmlPath){
		if(htmlPath.startsWith("file:///") && htmlPath.endsWith(".html"))
			return true;
		return false;
	}
	
	/**
	 * Check which the html file is a online file or not 
	 * @param htmlPath path of a html file
	 * @return true if the html file is a online file, or false
	 */
	public boolean isOnlineHtml(String htmlPath){
		if(htmlPath.startsWith("http://") || htmlPath.startsWith("https://"))
			return true;
		return false;
	}
	
	/**
	 * Merge the javascript codes in the html file to a file
	 * @param dirPath path of decompiled project directory
	 * @param htmlPath path of the html file
	 * @return a file object contains all javascript codes
	 */
	public File merge(File html){
		String script = "";
		String path = null;
		
		try {
			path = html.getCanonicalPath();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// parse the html file and get javascript codes related with the html
		// file.
		JerichoHtmlParser parser = new JerichoHtmlParser();
		try {
			ScriptCallback scb = new ScriptCallback(path.substring(0, path.lastIndexOf(File.separator)));
			parser.parse(null, new FileReader(html), scb, "");
			script = scb.getScript();
		} catch (Error | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// make a new javascript file which contains the javascript codes.
		File newJS = new File(path.substring(0, path.lastIndexOf(".")) + "_merge.js");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(newJS));
			bw.write(script);
			bw.flush();
			bw.close();
			System.out.println(" merged to " + newJS.getCanonicalPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newJS;
	}
	
	/**
	 * Merge the javascript codes in the html file to a file
	 * @param dirPath path of decompiled project directory
	 * @param htmlPath path of the html file
	 * @return a file object contains all javascript codes
	 */
	public File merge(String dir, String path){
		String script = "";
		
		// parse the html file and get javascript codes related with the html
		// file.
		JerichoHtmlParser parser = new JerichoHtmlParser();
		try {
			ScriptCallback scb = new ScriptCallback(dir);
			if(isLocalHtml(path))
				parser.parse(null, new FileReader(new File(path)), scb, "");
			else if(isOnlineHtml(path))
				parser.parse(null, new InputStreamReader((new URL(path)).openStream()), scb, "");
			else
				Assertions.UNREACHABLE("html file must be a local or online file: " + path);
			script = scb.getScript();
		} catch (Error | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// make a new javascript file which contains the javascript codes.
		File newJS = new File(dir + File.separator + path.substring(path.lastIndexOf(File.separator) + 1, ((path.lastIndexOf(".") > path.lastIndexOf(File.separator))? path.lastIndexOf(".") : path.length())) + "_merge.js");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(newJS));
			bw.write(script);
			bw.flush();
			bw.close();
			System.out.println(" merged to " + newJS.getCanonicalPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newJS;
	}
	
	/**
	 * Script callback class for parsing html. Because this class is only used for this
	 * purpose, so it is private inner class.
	 * 
	 * @author Sungho Lee
	 */
	private class ScriptCallback implements IHtmlCallback{
		
		private String script;
		private String path;
		
		public ScriptCallback(String path){
			this.path = path;
			script = "";
		}
		
		@Override
		public void handleEndTag(ITag arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void handleStartTag(ITag arg0) {
			// TODO Auto-generated method stub
			JerichoTag tag = ((JerichoTag) arg0);
			
			//if the tag is 'script', then the 'src' attribute shows the js file, or the inner texts are js codes.
			if(tag.getName().equals("script")){
				Pair<String, Position> p = tag.getAttributeByName("src");
				
				if(p == null){
					script += tag.getBodyText().snd + "\n";
				}else{
					String txt = p.fst;
					txt = txt.replace(" ", "");
					if(txt.startsWith("http")){
						OnlineFileReader ofr = new OnlineFileReader(txt);
						try {
							script += ofr.readData() + "\n";
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return;
					}else if(txt.startsWith("//")){
						
					}
					
					try {
						if(txt.startsWith("./")) //remove current directory descriptor
							txt = txt.substring(2);
						
						File f = new File(path + File.separator + txt);
						if(!f.exists()){
							System.err.println("[Warning] missing JavaScript file: " + path + File.separator + txt);
						}
						
						BufferedReader br = new BufferedReader(new FileReader(f));
						String s;
						while((s = br.readLine()) != null){
							script += s + "\n";
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}else if(tag.getName().equals("body")){
				
			}
		}
		
		@Override
		public void handleText(Position arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}
		
		public String getScript(){
			return script;
		}
	}
}
