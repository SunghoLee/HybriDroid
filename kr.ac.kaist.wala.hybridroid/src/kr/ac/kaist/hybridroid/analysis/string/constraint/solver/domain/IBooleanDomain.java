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

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BooleanBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.BooleanTopValue;

public interface IBooleanDomain extends IDomain {
	public static BooleanTopValue TOP = BooleanTopValue.getInstance();
	public static BooleanBotValue BOT = BooleanBotValue.getInstance();
}
