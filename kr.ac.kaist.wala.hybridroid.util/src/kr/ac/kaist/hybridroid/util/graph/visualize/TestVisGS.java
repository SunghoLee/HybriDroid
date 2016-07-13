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
package kr.ac.kaist.hybridroid.util.graph.visualize;

public class TestVisGS {
	private static String[] nodes = {
			"First", "Second", "Third", "Fourth", "Fifth", "Sixth", "Seventh", "Eighth", "Nineth", "Tenth",
	};
	
	private static int[][] edges = {
			{0,1},
			{1,2},
			{2,3},
			{3,4},
			{4,5},
			{5,6},
			{6,7},
			{7,8},
			{8,9},
	};
	
	
	public static void main(String[] args){
		VisualizerGS vis = VisualizerGS.getInstance();
		
		for(int[] edge: edges){
			vis.fromAtoB(nodes[edge[0]], nodes[edge[1]]);
		}
		vis.printGraph("print");
	}
}
