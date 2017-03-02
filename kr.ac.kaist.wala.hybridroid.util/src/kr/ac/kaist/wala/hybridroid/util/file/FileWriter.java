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
package kr.ac.kaist.wala.hybridroid.util.file;

import java.io.*;

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

	public static File copyFile(File src, String dstPath){
		File n = new File(dstPath);

		try {
			BufferedReader br = new BufferedReader(new FileReader(src));
			BufferedWriter bw = new BufferedWriter(new java.io.FileWriter(n));
			String buff;
			while((buff = br.readLine()) != null){
				bw.write(buff + "\n");
			}
			bw.flush();
			bw.close();
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return n;
	}
}
