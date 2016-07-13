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
package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IDomain;

public interface IValue {
	public IValue clone();
	public IValue weakUpdate(IValue v);
	public IDomain getDomain();
}
