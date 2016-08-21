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
package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.LongBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.LongTopValue;

public interface ILongDomain extends IDomain {
	public static LongTopValue TOP = LongTopValue.getInstance();
	public static LongBotValue BOT = LongBotValue.getInstance();
}
