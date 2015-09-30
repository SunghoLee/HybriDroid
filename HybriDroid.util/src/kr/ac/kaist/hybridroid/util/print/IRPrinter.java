package kr.ac.kaist.hybridroid.util.print;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;

public class IRPrinter {
	public static void printInstructions(CGNode node){
		IR ir = node.getIR();
		System.err.println("---------------------");
		System.err.println("Node: " + node);
		if(ir != null){
			for(SSAInstruction inst : ir.getInstructions()){
				if(inst != null){
					System.err.println("(" + inst.iindex + ") " + inst);
				}
			}
		}
		System.err.println("---------------------");
	}
	
	public static String getPrintableInstructios(CGNode node){
		String msg = "";
		
		IR ir = node.getIR();
		msg += "---------------------\n";
		msg += "Node: " + node + "\n";
		if(ir != null){
			for(SSAInstruction inst : ir.getInstructions()){
				if(inst != null){
					msg += "(" + inst.iindex + ") " + inst + "\n";
				}
			}
		}
		msg += "---------------------\n";
		return msg;
	}
}
