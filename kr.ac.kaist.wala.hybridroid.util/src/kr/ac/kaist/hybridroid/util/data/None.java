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
package kr.ac.kaist.hybridroid.util.data;

public class None<T> implements Option<T> {

	@Override
	public boolean isSome() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNone() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public T get() {
		// TODO Auto-generated method stub
		throw new InternalError("'None' object can not be gotten.");
	}
}
