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
package kr.ac.kaist.wala.hybridroid.analysis.string.constraint;

public enum ConstType {
	STRING,
	INT,
	BOOL,
	DOUBLE,
	LONG,
	LONG_TOP,
	LONG_BOT,
	STRING_TOP,
	STRING_BOT,
	INT_TOP,
	BOOL_TOP,
	DOUBLE_TOP,
	UNKNOWN;
	
	private String name;
	
	static{
		STRING.name = "String";
		INT.name = "int";
		BOOL.name = "bool";
		DOUBLE.name = "double";
		STRING_TOP.name = "StringTop";
		INT_TOP.name = "IntTop";
		BOOL_TOP.name = "BoolTop";
		DOUBLE_TOP.name = "DoubleTop";
		UNKNOWN.name = "unknown";
	}
	
	public String toString(){
		return name;
	}
}
