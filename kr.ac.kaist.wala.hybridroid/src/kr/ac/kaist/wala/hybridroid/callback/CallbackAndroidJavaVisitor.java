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
package kr.ac.kaist.wala.hybridroid.callback;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;

import kr.ac.kaist.wala.hybridroid.analysis.resource.AndroidResourceAnalysis;
import kr.ac.kaist.wala.hybridroid.callgraph.ResourceCallGraphBuilder.ResourceVisitor;

/**
 * @author Sungho Lee
 *
 */
public class CallbackAndroidJavaVisitor extends ResourceVisitor {

	/**
	 * @param builder
	 * @param node
	 * @param ara
	 */
	public CallbackAndroidJavaVisitor(SSAPropagationCallGraphBuilder builder, CGNode node,
			AndroidResourceAnalysis ara) {
		super(builder, node, ara);
		// TODO Auto-generated constructor stub
	}

	
}
