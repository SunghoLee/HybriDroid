package kr.ac.kaist.hybridroid.js.merger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.ibm.wala.cast.ir.translator.TranslatorToCAst.Error;
import com.ibm.wala.cast.js.html.IHtmlCallback;
import com.ibm.wala.cast.js.html.ITag;
import com.ibm.wala.cast.js.html.jericho.JerichoHtmlParser;
import com.ibm.wala.cast.js.html.jericho.JerichoTag;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.util.collections.Pair;


/**
 * Merge several javascript codes in a html into a file of javascript. The codes
 * may contains codes of javascript files or inlining javascript codes.
 * 
 * @author Sungho Lee
 */
public class JSScriptMerger {
	public JSScriptMerger(){}
	
	private File getHtmlFile(String htmlPath){
		File html = new File(htmlPath);
		if(!htmlPath.endsWith(".html") && !htmlPath.endsWith(".htm") || !html.exists() || !html.isFile())
			throw new InternalError("the file is not html file: " + htmlPath);
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
	 * Merge the javascript codes in the html file to a file
	 * @param dirPath path of decompiled project directory
	 * @param htmlPath path of the html file
	 * @return a file object contains all javascript codes
	 */
	public Pair<String,File> merge(String dirPath, String htmlPath){
		System.out.println("#HTML: " + htmlPath);		
		String path = dirPath + File.separator + htmlPath.replace("file:///", "").replace("android_asset", "assets");
		System.out.println("\tpath: " + path);
		
		File html = getHtmlFile(path);
		
		String script = "";
		
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
		return Pair.make(htmlPath, newJS);
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
					try {
						BufferedReader br = new BufferedReader(new FileReader(new File(path + File.separator + txt)));
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
