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
