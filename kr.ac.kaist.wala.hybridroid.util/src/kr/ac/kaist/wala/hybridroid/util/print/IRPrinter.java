package kr.ac.kaist.wala.hybridroid.util.print;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import com.ibm.wala.dalvik.classLoader.DexIRFactory;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAOptions;

/**
 * Created by leesh on 07/03/2017.
 */
public class IRPrinter {

    public static void printIR(CallGraph cg, String out, Filter f){
        if(f == null)
            f = defaultFilter();

        File outFile = new File(out);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
            for(CGNode n : cg) {
                if(!f.filter(n))
                    continue;
                bw.write("N" + n.toString() + "\n");
                bw.write("=======================================\n");
                IR ir = makeIR(n);
                if(ir != null) {
                    SSAInstruction[] insts = ir.getInstructions();
                    int index = 1;
                    for (int i = 0; i < insts.length; i++) {
                        SSAInstruction inst = insts[i];
                        if (inst != null) {
                            bw.write("( " + (i) + " ) " + inst + "\n");
                        }
                    }
                    bw.write("[Succ]=================================\n");
                    Iterator<CGNode> iSucc = cg.getSuccNodes(n);
                    while (iSucc.hasNext()) {
                        CGNode succ = iSucc.next();
                        bw.write("\t" + succ + "\n");
                    }
                }
                bw.newLine();
                bw.newLine();
            }

            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static IR makeIR(CGNode n){
        IR ir = n.getIR();

        if(ir == null){
            DexIRFactory irFactory = new DexIRFactory();
            try {
                ir = irFactory.makeIR(n.getMethod(), Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
            }catch(NullPointerException e){
                return null;
            }
        }
        return ir;
    }

    public interface Filter{
        boolean filter(CGNode n);
    }
    
    static public Filter defaultFilter(){
        return new Filter() {
			@Override
			public boolean filter(CGNode n) {
				return true;
			}
		};
    }

}
