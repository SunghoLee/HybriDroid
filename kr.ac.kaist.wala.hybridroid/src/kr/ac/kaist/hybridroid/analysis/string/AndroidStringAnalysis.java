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
package kr.ac.kaist.hybridroid.analysis.string;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.JavaClass;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.dalvik.classLoader.DexFileModule;
import com.ibm.wala.dalvik.classLoader.DexIRFactory;
import com.ibm.wala.dalvik.ipa.callgraph.impl.AndroidEntryPoint;
import com.ibm.wala.dalvik.util.AndroidEntryPointLocator;
import com.ibm.wala.dalvik.util.AndroidEntryPointLocator.LocatorFlags;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisOptions.ReflectionOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyClassTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.IRFactory;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.intset.OrdinalSet;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.Atom;

import kr.ac.kaist.hybridroid.analysis.FieldDefAnalysis;
import kr.ac.kaist.hybridroid.analysis.resource.AndroidResourceAnalysis;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintGraph;
import kr.ac.kaist.hybridroid.analysis.string.constraint.ConstraintVisitor;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.IConstraintNode;
import kr.ac.kaist.hybridroid.analysis.string.constraint.VarBox;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.ForwardSetSolver;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IStringValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringBotValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringTopValue;
import kr.ac.kaist.hybridroid.analysis.string.model.StringModel;
import kr.ac.kaist.hybridroid.callgraph.AndroidMethodTargetSelector;
import kr.ac.kaist.hybridroid.callgraph.ResourceCallGraphBuilder;
import kr.ac.kaist.hybridroid.util.data.Pair;

/**
 * 
 * @author Sungho Lee
 */
public class AndroidStringAnalysis implements StringAnalysis{
	public static boolean DEBUG = false;
	private AnalysisScope scope;
	private WorkList worklist;
	private List<Hotspot> hotspots;
	private Set<IBox> spotBoxSet;
	private Map<IConstraintNode, Set<String>> result;
	private Map<Hotspot, Set<HotspotDescriptor>> descMap;
	private BridgeInfo bi;
	private AndroidResourceAnalysis ara;
	
	public AndroidStringAnalysis(){
		scopeInit();
		worklist = new WorkList();
		result = new HashMap<IConstraintNode, Set<String>>();
		descMap = new HashMap<Hotspot, Set<HotspotDescriptor>>();
		bi = new BridgeInfo();
	}
	
	public AndroidStringAnalysis(AndroidResourceAnalysis ra){
		this();
		this.ara = ra;
		StringModel.setResourceAnalysis(ra, null);
	}
	
	private void scopeInit(){
		scope = AnalysisScope.createJavaAnalysisScope();
		scope.setLoaderImpl(ClassLoaderReference.Primordial, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");
		scope.setLoaderImpl(ClassLoaderReference.Application, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");
	}

	public void setExclusion(String exclusions){
		File exclusionsFile = new File(exclusions);
		try {
			InputStream fs = exclusionsFile.exists() ? new FileInputStream(exclusionsFile) : FileProvider.class.getClassLoader()
			    .getResourceAsStream(exclusionsFile.getName());
			scope.setExclusions(new FileOfClasses(fs));
			fs.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void addAnalysisScope(String path){
		// TODO Auto-generated method stub
		if(path.endsWith(".apk")){
			try {
				scope.addToScope(ClassLoaderReference.Application, DexFileModule.make(new File(path)));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			throw new InternalError("Support only apk format as target file");	
		}
	}

	public void setupAndroidLibs(String... libs){
		try{
			for(String lib : libs){
				if(lib.endsWith(".dex"))
					scope.addToScope(ClassLoaderReference.Primordial, DexFileModule.make(new File(lib)));
				else if(lib.endsWith(".jar"))
					scope.addToScope(ClassLoaderReference.Primordial, new JarFileModule(new JarFile(new File(lib))));
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void solve(ConstraintGraph cg){
		ForwardSetSolver fss = new ForwardSetSolver();
		Map<IConstraintNode, IValue> res = fss.solve(cg);
//		Map<IConstraintNode, IValue> res = Collections.emptyMap();
		for(IBox n : spotBoxSet){
			IValue v = res.get(n);
			if(v instanceof IStringValue && (v instanceof StringTopValue) == false && (v instanceof StringBotValue) == false){
				result.put(n, (Set<String>)v.getDomain().getOperator().gamma(v));
			}
		}
	}
	
	@Override
	public void analyze(List<Hotspot> hotspots) throws ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException {
		// TODO Auto-generated method stub
		this.hotspots = hotspots;
		Pair<CallGraph, PointerAnalysis<InstanceKey>> p = buildCG();
		CallGraph cg = p.fst();
		test(cg, p.snd());
		PointerAnalysis<InstanceKey> pa = p.snd();
//		WalaCGVisualizer vis = new WalaCGVisualizer();
//		vis.visualize(cg, "cfg_test.dot");
//		vis.printLabel("label.txt");
		Set<IBox> boxSet = findHotspots(cg, pa, hotspots);
		this.spotBoxSet = boxSet;
		IBox[] boxes = boxSet.toArray(new IBox[0]);
//		for(IBox box : boxes){
//			System.err.println("Spot: " + box);
//		}
		System.err.println("Field Def analysis...");
		FieldDefAnalysis fda = new FieldDefAnalysis(cg, pa);
		System.err.println("Build Constraint Graph...");
		
//		int targetN = 6;
//		IBox[] targets = new IBox[]{boxes[targetN]};
		IBox[] targets = boxes;
//		Box[] targets = boxes;
		ConstraintGraph graph = buildConstraintGraph(cg, fda, targets);
//		System.err.println("Print Constraint Graph...");
//		ConstraintGraphVisualizer cgvis = new ConstraintGraphVisualizer();
//		cgvis.visualize(graph, "const0.dot", targets);
		System.err.println("Optimize Constraint Graph...");
		graph.optimize();
//		cgvis.visualize(graph, "const_op"+targetN+".dot", targets);
		
//		System.out.println("--- String modeling warning ---");
//		for(String warning : StringModel.getWarnings()){
//			System.out.println("[Warning] " + warning);
//		}
//		System.out.println("------------");
		System.err.println("Solving the constraints...");
		solve(graph);
		makeDescriptors();
		collectBridgeInfo(cg, pa);
	}
	
	private void test(CallGraph cg, PointerAnalysis<InstanceKey> pa){
//		for(CGNode n : cg){
//			if(n.toString().contains("Node: < Application, Ltk/likeberry/modeltest/JSBridge, snd(Ljava/lang/String;Landroid/content/Context;II)Landroid/graphics/Bitmap; > Context: Everywhere")){// && (n.toString().contains("startActivity") || n.toString().contains("onActivityResult") || n.toString().contains("onRequestPermissionsResult") || n.toString().contains("requestPermissions") || n.toString().contains("onCreate"))){
//				int index = 1;
//				for(Iterator<SSAInstruction> iinst = n.getIR().iterateAllInstructions(); iinst.hasNext(); ){
//					System.out.println("("+(index++)+") " + iinst.next());
//				}
//////			if(n.getMethod().getDeclaringClass().toString().contains("Lmp/a/a/a")){
////				NodePrinter.printInsts(n);
//////				PointerKey v8 = pa.getHeapModel().getPointerKeyForLocal(n, 8);
//////				PointerKey v21 = pa.getHeapModel().getPointerKeyForLocal(n, 21);
//////				
//////				for(InstanceKey ik : pa.getPointsToSet(v8))
//////					System.out.println("v8 => " + ik);
//////				for(InstanceKey ik : pa.getPointsToSet(v21))
//////					System.out.println("v21 => " + ik);
//			}
//		}
//		System.exit(-1);
	}
	
	private void collectBridgeInfo(CallGraph cg, PointerAnalysis<InstanceKey> pa){
		final TypeReference wvType = TypeReference.find(ClassLoaderReference.Application, "Landroid/webkit/WebView");
		final Selector addJSSelector = Selector.make("addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V");
		IClassHierarchy cha = cg.getClassHierarchy();
		IClass wvClass = cha.lookupClass(wvType);
		for(CGNode n : cg){
			IR ir = n.getIR();
			if(ir == null)
				continue;
			
			for(Iterator<CallSiteReference> icsr = ir.iterateCallSites(); icsr.hasNext();){
				for(SSAAbstractInvokeInstruction callInst : ir.getCalls(icsr.next())){
					MethodReference mr = callInst.getDeclaredTarget();
					IClass receiver = cha.lookupClass(mr.getDeclaringClass());
					if((receiver != null && (receiver.equals(wvClass) || cha.isSubclassOf(receiver, wvClass))) && mr.getSelector().equals(addJSSelector)){
						int bridge = callInst.getUse(1);
						PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(n, bridge);
						for(InstanceKey ik : pa.getPointsToSet(pk)){
							TypeReference tr = ik.getConcreteType().getReference();
							bi.addBridge(n, bridge, tr);
//							for(Iterator<com.ibm.wala.util.collections.Pair<CGNode, NewSiteReference>> ip = ik.getCreationSites(cg); ip.hasNext(); ){
//								com.ibm.wala.util.collections.Pair<CGNode, NewSiteReference> p = ip.next();
//								bi.addBridge(n, bridge, tr, p.fst, p.snd);
//							}
						}
					}
				}
			}
		}
	}
	
	private void makeDescriptors(){
		Set<HotspotDescriptor> descSet = getAllDescriptors();
		
		for(IConstraintNode n : result.keySet()){
			for(HotspotDescriptor desc : descSet){
				if(desc.getConstNode().equals(n)){
					desc.setValues(result.get(n));
				}
			}
		}
	}
	
	public List<Hotspot> getSpots(){
		return hotspots;
	}
	
	public Map<IBox, Set<String>> getSpotString(){
		Map<IBox, Set<String>> res = new HashMap<IBox, Set<String>>();
		for(IBox b : spotBoxSet){
			VarBox var = (VarBox) b;
			Object v = getValue(var.getNode(), var.getVar());
			if(v != null){
				if(v instanceof String){
					Set<String> ss = new HashSet<String>();
					ss.add((String)v);
					res.put(b, ss);
				}else
					res.put(b, (Set<String>)v);
			}
		}
		return res;
	}
	
	public boolean isSolvedString(CGNode node, int var){
		IR ir = node.getIR();
		if(ir != null && ir.getSymbolTable().isConstant(var))
			return true;
		
		VarBox box = new VarBox(node, -1, var);
		if(result.containsKey(box))
			return true;
		
		return false;
	}
	
	public Object getValue(CGNode node, int var){
		IR ir = node.getIR();
		if(ir != null && ir.getSymbolTable().isConstant(var))
			return ir.getSymbolTable().getConstantValue(var);
		
		VarBox box = new VarBox(node, -1, var);
		if(result.containsKey(box))
			return result.get(box);
		
		return null;
	}
	
	private Pair<CallGraph, PointerAnalysis<InstanceKey>> buildCG() throws ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException{
		IClassHierarchy cha = ClassHierarchy.make(scope);
		//test
//		IClass klass = cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/util/Log"));
//		for(IMethod m : klass.getAllMethods())
//			System.out.println(m);
//		System.exit(-1);
		//test-end
		AnalysisOptions options = new AnalysisOptions();
		IRFactory<IMethod> irFactory = new DexIRFactory();
		AnalysisCache cache = new AnalysisCache(irFactory);
		options.setReflectionOptions(ReflectionOptions.NONE);
		options.setAnalysisScope(scope);
		options.setEntrypoints(getEntrypoints(cha, scope, options, cache));
		options.setSelector(new ClassHierarchyClassTargetSelector(cha));
//		options.setSelector(new ClassHierarchyMethodTargetSelector(cha));
		options.setSelector(new AndroidMethodTargetSelector(cha));
//		CallGraphBuilder cgb = new nCFABuilder(0, cha, options, cache, null, null);
//		CallGraphBuilder cgb = ZeroXCFABuilder.make(cha, options, cache, null, null, 0);
		CallGraphBuilder cgb = ResourceCallGraphBuilder.make(cha, options, cache, null, null, 0, ara);
		return Pair.make(cgb.makeCallGraph(options, null), cgb.getPointerAnalysis());
	}
	
	private Iterable<Entrypoint> getEntrypoints(final IClassHierarchy cha, AnalysisScope scope, AnalysisOptions option, AnalysisCache cache){
		Iterable<Entrypoint> entrypoints = null;
		
		if(cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Lgeneratedharness/GeneratedAndroidHarness")) == null){
			Set<LocatorFlags> flags = HashSetFactory.make();
			flags.add(LocatorFlags.INCLUDE_CALLBACKS);
			flags.add(LocatorFlags.EP_HEURISTIC);
			flags.add(LocatorFlags.CB_HEURISTIC);
			AndroidEntryPointLocator eps = new AndroidEntryPointLocator(flags);
			List<AndroidEntryPoint> es = eps.getEntryPoints(cha);
					
			final List<Entrypoint> entries = new ArrayList<Entrypoint>();
			for (AndroidEntryPoint e : es) {
				entries.add(e);
			}
	
			entrypoints = new Iterable<Entrypoint>() {
				@Override
				public Iterator<Entrypoint> iterator() {
					return entries.iterator();
				}
			};
		}else{
			IClass root = cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, "Lgeneratedharness/GeneratedAndroidHarness"));
			IMethod rootMethod = root.getMethod(new Selector(Atom.findOrCreateAsciiAtom("androidMain"), Descriptor.findOrCreate(null, TypeName.findOrCreate("V"))));
			Entrypoint droidelEntryPoint = new DefaultEntrypoint(rootMethod, cha);
			
			final List<Entrypoint> entry = new ArrayList<Entrypoint>();
			entry.add(droidelEntryPoint);
			
			entrypoints = new Iterable<Entrypoint>(){
				@Override
				public Iterator<Entrypoint> iterator(){
					return entry.iterator();
				}
			};
		}
		return entrypoints;
	}
	
	private Set<IBox> findHotspots(CallGraph cg, PointerAnalysis<InstanceKey> pa, List<Hotspot> hotspots){
		Set<IBox> boxes = new HashSet<IBox>();
		for(CGNode node : cg){
			IR ir = node.getIR();
			
			if(ir == null)
				continue;

			SSAInstruction[] insts = ir.getInstructions();
			for(int i=0; i<insts.length; i++){
				SSAInstruction inst = insts[i];
				
				if(inst == null)
					continue;
				
				for(Hotspot hotspot : hotspots){
					if(isHotspot(inst, hotspot)){

						int useVar = inst.getUse(hotspot.index() + 1);
						IBox nBox = new VarBox(node, i, useVar);
						boxes.add(nBox);
						int receiverVar = inst.getUse(0);
						LocalPointerKey usePK = (LocalPointerKey)pa.getHeapModel().getPointerKeyForLocal(node, useVar);
						LocalPointerKey receiverPK = (LocalPointerKey)pa.getHeapModel().getPointerKeyForLocal(node, receiverVar);
						InstanceKey[] uses = getInstanceKeys(pa, usePK);
						InstanceKey[] receivers = getInstanceKeys(pa, receiverPK);
						
						HeapGraph<InstanceKey> hg = pa.getHeapGraph();
						Set<Pointing> rpSet = new HashSet<Pointing>();
						rpSet.add(new Pointing(receiverPK.getNode(), receiverPK.getValueNumber()));
						
						for(InstanceKey receiver : receivers){
							for(Iterator<Object> ipk = hg.getPredNodes(receiver); ipk.hasNext();){
								Object o = ipk.next();
								if(o instanceof LocalPointerKey){
									LocalPointerKey rpk = (LocalPointerKey) o;
									rpSet.add(new Pointing(rpk.getNode(), rpk.getValueNumber()));
								}
							}
						}
						
						Set<Pointing> upSet = new HashSet<Pointing>();
						upSet.add(new Pointing(usePK.getNode(), usePK.getValueNumber()));
						
						for(InstanceKey use : uses){
							for(Iterator<Object> ipk = hg.getPredNodes(use); ipk.hasNext();){
								Object o = ipk.next();
								if(o instanceof LocalPointerKey){
									LocalPointerKey upk = (LocalPointerKey) o;
									rpSet.add(new Pointing(upk.getNode(), upk.getValueNumber()));
								}
							}
						}
						putHotspotDesciptor(hotspot, new HotspotDescriptor(node, inst.iindex, nBox, rpSet, upSet));
					}
				}
			}
		}
		return boxes;
	}
	
	private void putHotspotDesciptor(Hotspot h, HotspotDescriptor desc){
		if(!descMap.containsKey(h)){
			descMap.put(h, new HashSet<HotspotDescriptor>());
		}
		descMap.get(h).add(desc);
	}
	
	private InstanceKey[] getInstanceKeys(PointerAnalysis<InstanceKey> pa, PointerKey pk){
		OrdinalSet<InstanceKey> ikSet = pa.getPointsToSet(pk);
		InstanceKey[] iks = new InstanceKey[ikSet.size()];
		
		int index = 0;
		for(InstanceKey ik : ikSet){
			iks[index++] = ik;
		}
		return iks;
	}
	
	private boolean isHotspot(SSAInstruction inst, Hotspot hotspot){
		if(hotspot instanceof ArgumentHotspot){
			ArgumentHotspot argHotspot = (ArgumentHotspot) hotspot;
			if(inst instanceof SSAAbstractInvokeInstruction){
				SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) inst;
				MethodReference targetMr = invokeInst.getDeclaredTarget();
				TypeReference cTRef = targetMr.getDeclaringClass();
				Selector mSelector = targetMr.getSelector();
				if(cTRef.equals(argHotspot.getClassDescriptor()) && mSelector.equals(argHotspot.getMethodDescriptor()))
					return true;
			}
		}

		return false;
	}
	
	private ConstraintGraph buildConstraintGraph(CallGraph cg, FieldDefAnalysis fda, IBox... initials){
//		for(IBox box : initials){
//			System.err.println("TargetSpot: " + box);
//		}
		
		StringModel.init(cg.getClassHierarchy());
		ConstraintGraph graph = new ConstraintGraph();
//		ConstraintVisitor v = new ConstraintVisitor(cg, fda, graph, new InteractionConstraintMonitor(cg, InteractionConstraintMonitor.CLASSTYPE_ALL, InteractionConstraintMonitor.NODETYPE_NONE));
		ConstraintVisitor v = new ConstraintVisitor(cg, fda, graph, null);//new GraphicalDebugMornitor(initials));
		
		for(IBox initial : initials)
			worklist.add(initial);
		
		int iter = 1;
		while(!worklist.isEmpty()){
			IBox box = worklist.pop();
//			System.out.println("#Iter(" + (iter++) + ", size: " + worklist.size() + ") " + box);
			Set<IBox> res = box.visit(v);
			
			for(IBox next : res)
				worklist.add(next);
		}
		
		if(DEBUG){
			System.out.println("--- constraint visitor warning ---");
			for(String str : v.getWarnings()){
				System.out.println("[Warning] " + str);
			}
			System.out.println("----------");
		}
		return graph;
	}

	class WorkList{
		private List<IBox> list;
		private Set<IBox> visited;
		public WorkList(){
			list = new ArrayList<IBox>();
			visited = new HashSet<IBox>();
		}
		
		public void add(IBox box){
			if(!visited.contains(box)){
				list.add(box);
				visited.add(box);
			}
		}
		
		public IBox pop(){
			IBox box = list.get(0);
			list.remove(0);
			return box;
		}
		
		public boolean isEmpty(){
			return list.isEmpty();
		}
		
		public int size(){
			return list.size();
		}
	}
	
	
	public Set<HotspotDescriptor> getDescriptors(Hotspot h){
		return descMap.get(h);
	}
	
	public Set<HotspotDescriptor> getAllDescriptors(){
		Set<HotspotDescriptor> res = new HashSet<HotspotDescriptor>();
		for(Set<HotspotDescriptor> desc : descMap.values()){
			res.addAll(desc);
		}
		return res;
	}
	
	public class HotspotDescriptor{
		private CGNode node;
		private int iindex;
		private Set<Pointing> receiverAlias;
		private Set<Pointing> argAlias;
		private Set<String> values;
		private IConstraintNode constNode;
		
		private HotspotDescriptor(CGNode node, int iindex, IConstraintNode constNode, Set<Pointing> receiver, Set<Pointing> arg){
			this.node = node;
			this.iindex = iindex;
			this.constNode = constNode;
			this.receiverAlias = receiver;
			this.argAlias = arg;
		}
		
		private void setValues(Set<String> values){
			this.values = values;
		}
		
		private IConstraintNode getConstNode(){
			return constNode;
		}
		
		public Set<Pointing> getReceiverAlias(){
			return receiverAlias;
		}
		
		public CGNode getNode(){
			return node;
		}
		
		public SSAInstruction getInstruction(){
			return node.getIR().getInstructions()[iindex];
		}
		
		public Set<Pointing> getSpotAlias(){
			return argAlias;
		}
		
		public Set<String> getValues(){
			if(values != null)
				return values;
			else
				return Collections.emptySet();
		}
		
		@Override
		public String toString(){
			String res = "[HD]\n";
			res += "\tNode: " + constNode + "\n";
			res += "\tValue: " + values;
			return res;
		}
	}
	
	public static class Pointing{
		private CGNode node;
		private int varNum;
		
		public static Pointing make(CGNode node, int varNum){
			return new Pointing(node, varNum);
		}
		
		private Pointing(CGNode node, int varNum){
			this.node = node;
			this.varNum = varNum;
		}
		
		@Override
		public int hashCode(){
			return node.hashCode() + varNum;
		}
		
		@Override
		public boolean equals(Object o){
			if(o instanceof Pointing){
				Pointing p = (Pointing) o;
				if(p.node.getMethod().equals(node.getMethod()) && p.node.getContext().equals(node.getContext()) && p.varNum == varNum)
					return true;
			}
			return false;
		}
		
		@Override
		public String toString(){
			return "[" + varNum + "] in " + node;
		}
	}
	
	public static class BridgeInfo{
		private Map<Pair<Atom, Integer>, Set<BridgeDescription>> bridgeDescMap;
		
		private BridgeInfo(){
			bridgeDescMap = new HashMap<Pair<Atom, Integer>, Set<BridgeDescription>>();
		}

		private void addBridge(CGNode node, int var, TypeReference tr){
//			System.out.println("#FindBridge: " + node + " (var: " + var + ")");
			Pair<Atom, Integer> p = Pair.make(Atom.findOrCreateAsciiAtom(node.toString()), var);
			if(!bridgeDescMap.containsKey(p)){
				bridgeDescMap.put(p, new HashSet<BridgeDescription>());
			}
			bridgeDescMap.get(p).add(new BridgeDescription(tr));
		}
		
		public Set<BridgeDescription> getDescriptionsOfBridge(CGNode node, int var){
//			System.out.println("#FindingBridge: " + node + " (var: " + var + ")");
			Pair<Atom, Integer> p = Pair.make(Atom.findOrCreateAsciiAtom(node.toString()), var);
			if(bridgeDescMap.containsKey(p))
				return bridgeDescMap.get(p);
			return Collections.emptySet();
		}
				
		public static class BridgeDescription{
			private final TypeReference tr;

			private BridgeDescription(TypeReference tr){
				this.tr = tr;
			}
			
			public TypeReference getTypeReference(){
				return tr;
			}
		}
	}
	
	public BridgeInfo getBridgeInfo(){
		return bi;
	}
}
