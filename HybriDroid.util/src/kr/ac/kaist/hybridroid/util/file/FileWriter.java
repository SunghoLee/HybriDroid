package kr.ac.kaist.hybridroid.util.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileWriter {
	public static boolean DEBUG = false;
	
	public static File makeFile(String dir, String filename, String content){
		File fdir = new File(dir);
		if(!fdir.exists())
			fdir.mkdirs();
		
		String fpath = dir + File.separator + filename;
		
		int sepIndex = filename.lastIndexOf(File.separator);
		
		if(sepIndex > -1){
			File dirs = new File(dir + File.separator + filename.substring(0, sepIndex));
			dirs.mkdirs();
		}
		
		BufferedWriter bw = null;
		File output = new File(fpath);
		
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
			bw.write(content);
			bw.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(bw != null)
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		if(DEBUG){
			try {
				System.out.println(content + " => " + output.getCanonicalPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return output; 
	}
	
	public static File makeHtmlFile(String dir, String filename, String content){
		content = "<html><head><script>" + content +"</script></head><body></body></html>";
		File fdir = new File(dir);
		if(!fdir.exists())
			fdir.mkdirs();
		
		String fpath = dir + File.separator + filename;
		
		int sepIndex = filename.lastIndexOf(File.separator);
		
		if(sepIndex > -1){
			File dirs = new File(dir + File.separator + filename.substring(0, sepIndex));
			dirs.mkdirs();
		}
		
		BufferedWriter bw = null;
		File output = new File(fpath);
		
		if(output.exists())
			output.delete();
		
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
			bw.write(content);
			bw.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(bw != null)
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		if(DEBUG){
			try {
				System.out.println(content + " => " + output.getCanonicalPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return output; 
	}
}
