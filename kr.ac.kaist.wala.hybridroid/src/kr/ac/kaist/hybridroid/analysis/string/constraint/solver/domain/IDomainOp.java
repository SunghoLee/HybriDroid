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
package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain;

import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;

public interface IDomainOp<S, T> {
	public IValue alpha(S cv);
	public IValue alpha(Set<S> cv);
	public T gamma(IValue v);
}
