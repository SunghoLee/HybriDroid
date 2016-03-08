package kr.ac.kaist.hybridroid.analysis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.cast.ipa.callgraph.StandardFunctionTargetSelector;
import com.ibm.wala.cast.ipa.cha.CrossLanguageClassHierarchy;
import com.ibm.wala.cast.js.ipa.callgraph.JSCallGraphUtil;
import com.ibm.wala.cast.js.ipa.callgraph.JavaScriptConstructTargetSelector;
import com.ibm.wala.cast.js.loader.JavaScriptLoader;
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisOptions.ReflectionOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.MethodTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.ComposedEntrypoints;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.IRFactory;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.strings.Atom;

import kr.ac.kaist.hybridroid.analysis.string.AndroidStringAnalysis;
import kr.ac.kaist.hybridroid.callgraph.AndroidHybridAnalysisScope;
import kr.ac.kaist.hybridroid.callgraph.AndroidHybridCallGraphBuilder;
import kr.ac.kaist.hybridroid.callgraph.AndroidHybridMethodTargetSelector;
import kr.ac.kaist.hybridroid.callgraph.HybridClassLoaderFactory;
import kr.ac.kaist.hybridroid.callgraph.HybridIRFactory;
import kr.ac.kaist.hybridroid.checker.HybridAPIMisusesChecker;
import kr.ac.kaist.hybridroid.checker.HybridAPIMisusesChecker.Warning;
import kr.ac.kaist.hybridroid.models.AndroidHybridAppModel;
import kr.ac.kaist.hybridroid.test.TaintAnalysisForHybrid;
import kr.ac.kaist.hybridroid.utils.VisualizeCGTest;

/**
 * Build Control-flow graph for the target Android hybrid application. Now, it
 * supports 'addJavascriptInterface' API only to deal with communication between
 * Android Java and JavaScript. This analysis has two step. 1) Pre string
 * analysis for some string value. This phase is needed to get argument values
 * for 'loadUrl' and 'addJavascriptInterface'. 2) Build Control-flow graph. Now,
 * the string analysis can deal with constant values only. We must improve this
 * analysis for accurate Control-flow graph. We analyzed the communication
 * behaviors between Android Java and JavaScript in Android_5.0.2_r3 for this
 * implementation.
 * 
 * @author Sungho Lee
 */
public class HybridCFGAnalysis {
	
	public HybridCFGAnalysis() {

	}

	/**
	 * Set MethodTargetSelector to the AnalysisOptions.
	 * @param options
	 * @param scope
	 * @param cha
	 */
	public static void addHybridDispatchLogic(AnalysisOptions options,
			AnalysisScope scope, IClassHierarchy cha) {
		com.ibm.wala.ipa.callgraph.impl.Util.addDefaultSelectors(options, cha);

		Map<Atom, MethodTargetSelector> methodTargetSelectors = HashMapFactory
				.make();
		methodTargetSelectors.put(
				JavaScriptLoader.JS.getName(),
				new JavaScriptConstructTargetSelector(cha,
						new StandardFunctionTargetSelector(cha, options
								.getMethodTargetSelector())));
		methodTargetSelectors.put(Language.JAVA.getName(),
				options.getMethodTargetSelector());

		options.setSelector(new AndroidHybridMethodTargetSelector(
				methodTargetSelectors));
		options.setUseConstantSpecificKeys(true);
	}

	/**
	 * For debugging.
	 * @param cg
	 * @param pa
	 */
	public static void check(CallGraph cg, PointerAnalysis<InstanceKey> pa) {
		boolean find = false;
		for (CGNode node : cg) {

			if (node.toString()
					.contains(
							"< Application, Lcom/example/hellohybrid/JavascriptBridge, sendName(Ljava/lang/String;)V > Context: Everywhere")) {
				System.out.println("===========================>");
				find = true;
				for (int i = 1; i < 4; i++) {
					PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(
							node, i);
					Iterator<InstanceKey> ikIter = pa.getPointsToSet(pk)
							.iterator();
					System.out.println("#" + i + ": "
							+ pa.getPointsToSet(pk).size());
					while (ikIter.hasNext()) {
						InstanceKey ik = ikIter.next();
						System.out.println("### v" + i + ": " + ik);
						System.out.println("\t" + ik.getClass().getName());
					}
				}
				System.out.println("<===========================");
			}
		}
		if (find == false) {
			System.out.println(" Incomplete Call Graph!");
		}
	}

	/**
	 * Build Control-flow graph for the AnalysisScope.
	 * @param scope the scope that includes all target files.
	 * @throws IOException
	 * @throws ClassHierarchyException
	 * @throws IllegalArgumentException
	 * @throws CancelException
	 */
	public void main(AndroidHybridAnalysisScope scope, AndroidStringAnalysis asa, Map<String, String> pathMap) throws IOException,
			ClassHierarchyException, IllegalArgumentException, CancelException {
		
		JSCallGraphUtil.setTranslatorFactory(new CAstRhinoTranslatorFactory());
		
		HybridClassLoaderFactory loaders = new HybridClassLoaderFactory();

		AnalysisOptions options = new AnalysisOptions(scope, null);

		IClassHierarchy cha = CrossLanguageClassHierarchy.make(scope, loaders);

		IRFactory<IMethod> factory = new HybridIRFactory();

		AnalysisCache cache = new AnalysisCache(factory);

		ComposedEntrypoints roots = AndroidHybridAppModel.getEntrypoints(cha,
				scope, options, cache);

		options.setEntrypoints(roots);

		options.setReflectionOptions(ReflectionOptions.NONE);

		addHybridDispatchLogic(options, scope, cha);

		AndroidHybridCallGraphBuilder b = new AndroidHybridCallGraphBuilder(
				cha, options, cache, HybridAPIMisusesChecker.getInstance(), asa, pathMap);

		CallGraph cg = b.makeCallGraph(options);

		System.out.println("Done");

//		VisualizeCGTest.visualizeCallGraph(cg, "cg_dex", true);

		printTypeWarning(b.getWarnings());

		// printNodeInsts(cg, null, "send");

		// taintTest(cg);
	}

	private static void taintTest(CallGraph cg) {
		TaintAnalysisForHybrid tAnalyzer = new TaintAnalysisForHybrid(cg);
		tAnalyzer.analyze();
	}

	private static Set<CGNode> printNodeInsts(CallGraph cg, String nodeStr) {
		Set<CGNode> nodes = new HashSet<CGNode>();
		// System.out.println(cg);
		for (CGNode node : cg) {
			if (node.toString().contains(nodeStr)) {
				nodes.add(node);
				System.out.println("=======");
				System.out.println("\t#Node: " + node);
				IR ir = node.getIR();
				if (ir != null) {
					int index = 1;
					for (SSAInstruction inst : ir.getInstructions()) {
						System.out.println("\t\t(" + (index++) + ") " + inst);
					}
				}
				System.out.println("=======");
				System.out.println();
			}
		}
		if (nodes.size() == 0)
			System.out.println("Not found the Method: " + nodeStr);

		return nodes;
	}

	private static Set<CGNode> printNodeInsts(CallGraph cg, String className,
			String methodName) {
		Set<CGNode> nodes = new HashSet<CGNode>();
		// System.out.println(cg);
		for (CGNode node : cg) {
			if (((methodName != null) ? node.getMethod().getName().toString()
					.contains(methodName) : true)
					&& ((className != null) ? node.getMethod()
							.getDeclaringClass().getName().getClassName()
							.toString().contains(className) : true)) {
				nodes.add(node);
				System.out.println("=======");
				System.out.println("\t#Node: " + node);
				IR ir = node.getIR();
				if (ir != null) {
					int index = 1;
					for (SSAInstruction inst : ir.getInstructions()) {
						System.out.println("\t\t(" + (index++) + ") " + inst);
					}
				}
				System.out.println("=======");
				System.out.println();
			}
		}
		if (nodes.size() == 0)
			System.out.println("Not found the Method: " + methodName + "( "
					+ className + " )");

		return nodes;
	}

	private static void printTypeWarning(Set<Warning> warnings) {
		// TODO Auto-generated method stub
		System.out.println("=== Type mismatch warnings ===");
		for (Warning tw : warnings) {
			System.out.println(tw);
		}
		System.out.println("==============================");
	}
}
