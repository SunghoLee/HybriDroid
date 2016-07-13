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
package kr.ac.kaist.hybridroid.analysis.string.constraint;

import com.ibm.wala.ipa.callgraph.CGNode;

public interface IBox extends IConstraintNode {
	public <T> T visit(IBoxVisitor<T> v);
	public CGNode getNode();
}
