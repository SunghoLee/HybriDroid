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
package kr.ac.kaist.hybridroid.print;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Printer {

	private BufferedWriter writer;
	
	public Printer(String output) throws Exception{
		if(output == null){
			throw new Exception(){
				/**
				 * 
				 */
				private static final long serialVersionUID = -2117251529449831113L;

				@Override
				public String getMessage() {
					// TODO Auto-generated method stub
					return " output file cannot be null";
				}
			};

		}else{
			File outfile = new File(output);
			if(outfile.exists()){
				System.out.print(outfile.getCanonicalPath() + " exist. overwrite it (y/n)? ");
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				
				String s;
				
				while((s = br.readLine()) != null){
					if(s.equalsIgnoreCase("y") || s.equalsIgnoreCase("yes")){
						outfile.delete();
						outfile.createNewFile();
						break;
					}else if(s.equalsIgnoreCase("n") || s.equalsIgnoreCase("no"))
						System.exit(0);
					else
						System.out.print(outfile.getCanonicalPath() + " exist. overwrite it (y/n)? ");
				}
			}else{
				outfile.createNewFile();
			}
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile)));
		}
	}
	
	public Printer(){
		writer = new BufferedWriter(new OutputStreamWriter(System.out));
	}
	
	public void println(String msg){
		try {
			writer.write(msg + "\n");
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void print(String msg){
		try {
			writer.write(msg);
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void finalize(){
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

