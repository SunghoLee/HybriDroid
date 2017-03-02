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
package kr.ac.kaist.wala.hybridroid.util.data;

public class Pair<X,Y> {
	private X fst;
	private Y snd;
	
	static public <X,Y> Pair<X,Y> make(X fst, Y snd){
		return new Pair<X, Y>(fst, snd);
	}
	
	private Pair(X fst, Y snd){
		this.fst = fst;
		this.snd = snd;
	}
	
	public X fst(){
		return fst;
	}
	
	public Y snd(){
		return snd;
	}
	
	@Override
	public int hashCode(){
		return fst.hashCode() + snd.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Pair){
			if(fst.equals(((Pair) o).fst()) && snd.equals(((Pair) o).snd()))
				return true;
		}
			
		return false;
	}
	
	@Override
	public String toString(){
		return "[ " + fst + ", " + snd + "]";
	}
}
