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
package kr.ac.kaist.wala.hybridroid.analysis.string.model;

import java.util.HashSet;
import java.util.Set;

import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.UriCodecDecodeOpNode;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.VarBox;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.Selector;

public class UriCodecClassModel extends AbstractClassModel{
	private static UriCodecClassModel instance;
	
	public static UriCodecClassModel getInstance(){
		if(instance == null)
			instance = new UriCodecClassModel();
		return instance;
	}
		
	protected void init(){
		methodMap.put(Selector.make("decode(Ljava/lang/String;ZLjava/nio/charset/Charset;Z)Ljava/lang/String;"), new Decode());
	}
		
	//decode(Ljava/lang/String;ZLjava/nio/charset/Charset;Z)Ljava/lang/String;
	class Decode implements IMethodModel<Set<IBox>>{

		@Override
		public Set<IBox> draw(ConstraintGraph graph, IBox def, CGNode caller,
				SSAInvokeInstruction invokeInst) {
			Set<IBox> boxSet = new HashSet<IBox>();
			int stringVar = invokeInst.getUse(0);
			int convertPlusVar = invokeInst.getUse(1);
			int charsetVar = invokeInst.getUse(2);
			int throwVar = invokeInst.getUse(3);
			
			IBox stringBox = new VarBox(caller, invokeInst.iindex, stringVar);
			IBox convertPlusBox = new VarBox(caller, invokeInst.iindex, convertPlusVar);
			IBox charsetBox = new VarBox(caller, invokeInst.iindex, charsetVar);
			IBox throwBox = new VarBox(caller, invokeInst.iindex, throwVar);
			
			if(graph.addEdge(new UriCodecDecodeOpNode(), def, stringBox, convertPlusBox, charsetBox, throwBox)){
					boxSet.add(stringBox);
					boxSet.add(convertPlusBox);
					boxSet.add(charsetBox);
					boxSet.add(throwBox);
			}
			return boxSet;
		}
	
		@Override
		public String toString(){
			return "Constraint Graph Method Model: UriCodec.decode(Ljava/lang/String;ZLjava/nio/charset/Charset;Z)Ljava/lang/String;";
		}
		
	}
}

