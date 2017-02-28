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
package kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.model;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.IDomain;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.value.BotValue;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.value.StringBotValue;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.value.StringTopValue;
import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.value.TopValue;

public class UriGetQueryParameterOpSetModel implements IOperationModel{

	private static UriGetQueryParameterOpSetModel instance;
	
	public static UriGetQueryParameterOpSetModel getInstance(){
		if(instance == null)
			instance = new UriGetQueryParameterOpSetModel();
		return instance;
	}

	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		if(args.length != 2)
			throw new InternalError("UriGetQueryParameter must have two in-edges.");
		
		IValue absUri = (IValue)args[0].clone();
		IValue absKey = (IValue)args[1].clone();
		
		if(absUri instanceof TopValue || absKey instanceof TopValue)
			return StringTopValue.getInstance();
		else if(absUri instanceof BotValue || absKey instanceof BotValue)
			return StringBotValue.getInstance();

		//this part is domain specific!
		IDomain domain = absUri.getDomain();
		Set<String> uri = (Set<String>)domain.getOperator().gamma(absUri);
		Set<String> res = new HashSet<String>();
		
		/*
		 * if the 'key' argument is Top, we croll all values of the uri.
		 * this method can analyze more precise string value than just getting Top.
		 */
		if(absKey instanceof StringTopValue){
			for(String u : uri){
				int qi = u.indexOf("?");
				if(qi == -1){
					/*
					* if there is no question mark, this method must return null.
					* now we model it to just return 'null' string value.
					* we must refine this!
					*/
					res.add("null");
					continue;
				}
				
				StringTokenizer st = new StringTokenizer(u.substring(0, qi), "&");
				String v = null;
				while(st.hasMoreTokens()){
					String token = st.nextToken();
					int ei = token.indexOf("=");
					if(ei == -1)
						continue;
					String value = token.substring(ei + 1);
					
					res.add(value);
				}
				
				if(v == null){
					/*
					* if there is no value in uri, this method must return null.
					* now we model it to just return 'null' string value.
					* we must refine this!
					*/
					res.add("null");
				}
			}
		}else{
			Set<String> k = (Set<String>)domain.getOperator().gamma(absKey);
			for(String u : uri){
				int qi = u.indexOf("?");
				if(qi == -1){
					/*
					* if there is no question mark, this method must return null.
					* now we model it to just return 'null' string value.
					* we must refine this!
					*/
					res.add("null");
					continue;
				}
				
				StringTokenizer st = new StringTokenizer(u.substring(0, qi), "&");
				String v = null;
				while(st.hasMoreTokens()){
					String token = st.nextToken();
					int ei = token.indexOf("=");
					if(ei == -1)
						continue;
					String key = token.substring(0,ei);
					if(k.contains(key)){
						String value = token.substring(ei + 1);
						res.add(value);
					}
				}
				
				if(v == null){
					/*
					* if there is no value assigned at key corresponding to 'key' argument, this method must return null.
					* now we model it to just return 'null' string value.
					* we must refine this!
					*/
					res.add("null");
				}
			}
		}
			
		return domain.getOperator().alpha(res);
	}
}
