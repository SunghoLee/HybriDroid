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
package kr.ac.kaist.wala.hybridroid.frontend.bridge;

/**
 * Data structure denotes bridge information used for access from JavaScript to Java in hybrid applications.
 * @author Sungho Lee
 *
 */
public class BridgeInfo {
	private String name;
	private ClassInfo klass;
	
	public BridgeInfo(String name, ClassInfo klass){
		this.name = name;
		this.klass = klass;
	}
	
	/**
	 * Get the name of this bridge.
	 * @return the name of this bridge.
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * Get the class information of a Java object referred by this bridge.
	 * @return the Java class information for this bridge.
	 */
	public ClassInfo getClassInfo(){
		return this.klass;
	}
	
	public String toString(){
		String res = "";
		res = name + " -> " + klass;
		return res;
	}
}
