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
package kr.ac.kaist.hybridroid.util.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class OnlineFileReader {
	private String path;
	
	public OnlineFileReader(String path){
		this.path = path;
	}
	
	public String readData() throws IOException{
		String res = "";
		
		URL url = new URL(path);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		
		String s = null;
		
		boolean isFirstLine = true;
		
		while((s = br.readLine()) != null){
			if(!isFirstLine)
				res += "\n";
			
			res += s;
			
			if(isFirstLine)
				isFirstLine = false;
		}
		br.close();
		
		return res;
	}
}
