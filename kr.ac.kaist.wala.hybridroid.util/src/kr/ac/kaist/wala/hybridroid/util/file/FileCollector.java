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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class FileCollector {
	public static Set<File> collectFiles(File dir, String... exts){
		Set<File> res = new HashSet<File>();
		for(File f : dir.listFiles()){
			if(f.isDirectory())
				res.addAll(collectFiles(f, exts));
			else{
				for(String ext : exts)
					if(f.getName().endsWith(ext))
						res.add(f);
			} 
		}
		return res;
	}
}
