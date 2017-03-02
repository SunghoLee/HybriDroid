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

import kr.ac.kaist.wala.hybridroid.util.print.AsyncPrinter;

import java.io.*;

public class FilePrinter {

	public static void print(File f, OutputStream os) throws InternalError{
		if(!f.exists()){
			try {
				throw new InternalError("the file does not exsit: " + f.getCanonicalPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			bw = new BufferedWriter(new OutputStreamWriter(os));

			String s = null;

			while ((s = br.readLine()) != null) {
				bw.write(s + "\n");
			}
			bw.flush();
		} catch (IOException e) {

		} finally {
			try {
				if (br != null)
					br.close();
				if (bw != null)
					bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void printUsingShell(File f, String path){
		if(!f.exists()){
			try {
				throw new InternalError("the file does not exsit: " + f.getCanonicalPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Runtime r = Runtime.getRuntime();
		try {
			System.out.println("cp " + f.getCanonicalPath() + " " + path);
			Process p = r.exec("cp " + f.getCanonicalPath() + " " + path);
			AsyncPrinter errPinter = new AsyncPrinter(p.getErrorStream(), AsyncPrinter.PRINT_ERR);
			AsyncPrinter outPinter = new AsyncPrinter(p.getInputStream(), AsyncPrinter.PRINT_OUT);
			errPinter.run();
			outPinter.run();
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
