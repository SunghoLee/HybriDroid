package kr.ac.kaist.hybridroid.print;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInstruction;

public class NodePrinter {
	public static void printInsts(CGNode node){
		int index = 1;
		System.out.println("----");
		System.out.println("=> " + node );
		System.out.println("----");
		for(SSAInstruction inst : node.getIR().getInstructions()){
			System.out.println("(" + (index++) +") " + inst);
		}
		System.out.println("----");
	}
}
