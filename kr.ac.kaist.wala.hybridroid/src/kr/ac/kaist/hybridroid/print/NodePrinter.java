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

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInstruction;

public class NodePrinter {
	public static void printInsts(CGNode node){
		int index = 1;
		System.out.println("----");
		System.out.println("=> " + node );
		System.out.println("----");
		for(SSAInstruction inst : node.getIR().getInstructions()){
			System.out.println("(" + (index++) +") " + inst);
		}
		System.out.println("----");
	}
}
