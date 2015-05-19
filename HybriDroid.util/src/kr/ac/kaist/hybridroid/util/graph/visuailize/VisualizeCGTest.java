package kr.ac.kaist.hybridroid.util.graph.visuailize;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import com.ibm.wala.cast.ipa.callgraph.AstCallGraph.AstCGNode;
import com.ibm.wala.cast.js.types.JavaScriptTypes;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.types.ClassLoaderReference;

public class VisualizeCGTest {
	private static String DOT_PATH;
	
	static{
		if (System.getProperty("os.name").matches(".*Mac.*"))
			DOT_PATH = "dot";
		else if (System.getProperty("os.name").contains("Windows"))
			DOT_PATH = "\"C:\\Program Files (x86)\\Graphviz2.38\\bin\\dot.exe\"";
	}
	
	private static boolean fromLoader(IClass declClass, ClassLoaderReference clr) {
		ClassLoaderReference nodeClRef = declClass.getClassLoader()
				.getReference();

		return nodeClRef.equals(clr);
    }
	
	private static boolean isVisualizable(CGNode _node){
		if (!_node.toString().contains("preamble.js")
				&& !_node.toString().contains("prologue.js")) {
//			if(fromLoader(_node.getMethod().getDeclaringClass(), ClassLoaderReference.Primordial))
//				return false;
//			if(fromLoader(_node.getMethod().getDeclaringClass(), ClassLoaderReference.Extension))
//				return false;
//			else
//				return true;
			if(fromLoader(_node.getMethod().getDeclaringClass(), JavaScriptTypes.jsLoader))
				return true;
//			if(fromLoader(_node.getMethod().getDeclaringClass(), ClassLoaderReference.Primordial))
//				return false;
//			if(fromLoader(_node.getMethod().getDeclaringClass(), ClassLoaderReference.Extension))
//				return false;
			else
				return false;
		}
		return false;
		// return true;
	}
	
	private static boolean isContentPrintable(CGNode _node){
		if (!_node.toString().contains("preamble.js")
				&& !_node.toString().contains("prologue.js")) {
			if(fromLoader(_node.getMethod().getDeclaringClass(), JavaScriptTypes.jsLoader))
				return true;
//			if(fromLoader(_node.getMethod().getDeclaringClass(), ClassLoaderReference.Primordial))
//				return false;
//			if(fromLoader(_node.getMethod().getDeclaringClass(), ClassLoaderReference.Extension))
//				return false;
			else
				return false;

		}
		return false;
	}
	
	private static void assignNodeName(CallGraph cg, FileOutputStream outStream) throws IOException{
		for (CGNode node : cg) {
			if (isVisualizable(node)) {
				String irs = "";
				if (node.getIR() != null)
					irs = node.getIR().toString();
				int num = 0;
				String before = irs;
				while (irs.indexOf("\n\n") > 0) {
					irs = irs.replace("\n\n", "\n");
					num++;
				}
				irs = irs.replace("\n", "\\l");
				irs += "\n\n";
				irs += "<SuccessNodes: " + cg.getSuccNodeCount(node) + ">\\l";
				Iterator<CGNode> iSuccNode = cg.getSuccNodes(node);
				while (iSuccNode.hasNext()) {
					CGNode succNode = iSuccNode.next();
					irs += succNode.toString();
					if (iSuccNode.hasNext())
						irs += "\\l";
				}
				// irs = irs.replace("@", "(at)");
				if (isContentPrintable(node))
					outStream.write((cg.getNumber(node) + " [label=\"" + node
							+ "\n" + irs + "\" shape=box]\n").getBytes());
				else
					outStream
							.write((cg.getNumber(node) + " [label=\"" + node + "\" shape=box]\n")
									.getBytes());
			}
		}
	}
	
	public static void visualizeCallGraph(CallGraph cg, String _outputFileName, boolean convert){
		File visOut = new File(_outputFileName + ".dot");
		System.out.println("#making " + _outputFileName + ".dot...");
		try {
			FileOutputStream outStream = new FileOutputStream(visOut);
			outStream.write(("digraph callgraph{\n").getBytes());

			assignNodeName(cg, outStream);

			for (CGNode node : cg) {
				if (isVisualizable(node))
					for (Iterator<CGNode> succI = cg.getSuccNodes(node); succI
							.hasNext();) {
						CGNode succNode = succI.next();
						if (isVisualizable(succNode)) {
							outStream
									.write((cg.getNumber(node) + " -> "
											+ cg.getNumber(succNode) + "\n")
											.getBytes());
						}
					}
			}
			outStream.write(("}").getBytes());
			outStream.close();
			System.out.println("\tdone.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (convert) {
			Runtime runtime = Runtime.getRuntime();
			System.out.println("#translating " + _outputFileName + ".dot to "
					+ _outputFileName + ".svg...");
			try {
				Process tr = runtime.exec(DOT_PATH + " -Tsvg -o "
						+ _outputFileName + ".svg -v " + _outputFileName
						+ ".dot");
				tr.waitFor();
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("\tdone.");
		}
	}
}
