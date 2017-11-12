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
package kr.ac.kaist.wala.hybridroid.test;

import com.ibm.wala.cast.ir.ssa.*;
import com.ibm.wala.cast.js.ssa.*;
import com.ibm.wala.cast.js.types.JavaScriptTypes;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.util.SourceBuffer;
import com.ibm.wala.classLoader.*;
import com.ibm.wala.dataflow.IFDS.*;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;
import com.ibm.wala.ssa.SSAInstruction.IVisitor;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.*;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.EmptyIntSet;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.SparseIntSet;
import kr.ac.kaist.wala.hybridroid.models.AndroidHybridAppModel;
import kr.ac.kaist.wala.hybridroid.util.graph.visualize.Visualizer;
import kr.ac.kaist.wala.hybridroid.util.graph.visualize.Visualizer.GraphType;

import java.io.IOException;
import java.util.*;

public class PrivateLeakageDetector {
	private final ICFGSupergraph supergraph;
	private final CallGraph cg;
	private final PointerAnalysis<InstanceKey> pa;
	private final IClassHierarchy cha;

	private final IClass activityClass;
	private final IClass wvClass;
	private final IClass contextClass;
	private final IClass mapClass;
	private final IClass bitmapClass;
	
	private final TypeReference activityTR = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/app/Activity");
	private final TypeReference wvTR = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/webkit/WebView");
	private final TypeReference contextTR = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/content/Context");
	private final TypeReference mapTR = TypeReference.find(ClassLoaderReference.Application, "Ljava/util/Map");
	private final TypeReference bitmapTR = TypeReference.find(ClassLoaderReference.Application, "Landroid/graphics/Bitmap");
	
	//Application, Ljava/util/Map
	private final Selector oncreateSelector = Selector.make("onCreate(Landroid/os/Bundle;)V");
	private final Selector startActivitySelector = Selector.make("startActivity(Landroid/content/Intent;)V");
	private final Selector startActivityForResultSelector = Selector.make("startActivityForResult(Landroid/content/Intent;I)V");
	private final Selector onActivityResultSelector = Selector.make("onActivityResult(IILandroid/content/Intent;)V");
	private final Selector requestPermissionSelector = Selector.make("requestPermissions([Ljava/lang/String;I)V");
	private final Selector onRequestPermissionResultSelector = Selector.make("onRequestPermissionsResult(I[Ljava/lang/String;[I)V");
	private final Selector loadUrlSelector = Selector.make("loadUrl(Ljava/lang/String;)V");
	
	private final Selector putSelector = Selector.make("put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
	private final Selector getSelector = Selector.make("get(Ljava/lang/Object;)Ljava/lang/Object;");
	private final Selector compressSelector = Selector.make("compress(Landroid/graphics/Bitmap$CompressFormat;ILjava/io/OutputStream;)Z");
	
	private static boolean TEST = false;
	public static boolean DEBUG = false;
	private final PointerKey dummy = new PointerKey(){};
	
	private final Map<PointerKey, Set<PointerKey>> toFromMap = new HashMap<PointerKey, Set<PointerKey>>();
	
	private final Set<LeakWarning> warn = new HashSet<LeakWarning>();
	
	private void fromTo(PointerKey from, PointerKey to){
		if(!toFromMap.containsKey(to)){
			toFromMap.put(to, new HashSet<PointerKey>());
		}
		
		toFromMap.get(to).add(from);
	}
	
	private Set<List<PointerKey>> calcPath(PointerKey pk){
		if(pk instanceof SeedKey){
			Set<List<PointerKey>> sl = new HashSet<List<PointerKey>>();
			List<PointerKey> l = new ArrayList<PointerKey>();
			l.add(pk);
			sl.add(l);
			return sl;
		}
		
		Set<List<PointerKey>> sl = new HashSet<List<PointerKey>>();
		List<PointerKey> l = new ArrayList<PointerKey>();
		for(PointerKey pred : toFromMap.get(pk)){
			sl.addAll(calcPath(pred));
		}
		for(List<PointerKey> lpk : sl){
			lpk.add(pk);
		}
		return sl;
	}
	
	private static MethodReference[] mSourceRefs = {
			//for wifi information
			MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Application, "Landroid/telephony/TelephonyManager"), Selector.make("getLine1Number()Ljava/lang/String;")),
//			MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/net/wifi/WifiManager"), Selector.make("getWifiApState()I")),
//			MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/net/wifi/WifiManager"), Selector.make("getConnectionInfo()Landroid/net/wifi/WifiInfo;")),
	};
	/*
< Primordial, Ljava/net/URLConnection, setRequestProperty(Ljava/lang/String;Ljava/lang/String;)V

	 */
	private static MethodReference[] mSinkRefs = {
			MethodReference.findOrCreate(TypeReference.find(JavaScriptTypes.jsLoader, "Lpreamble.js/XMLHttpRequest/xhr_send"), Selector.make("do()LRoot")),
			//for http connetion
//			MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Primordial, "Ljava/net/URLConnection"), Selector.make("setRequestProperty(Ljava/lang/String;Ljava/lang/String;)V")),
//			//for write something 
//			MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Primordial, "Ljava/io/Writer"), Selector.make("write(Ljava/lang/String;)V")),
//			MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Primordial, "Ljava/io/Writer"), Selector.make("write(Ljava/lang/String;II)V")),
//			MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Primordial, "Ljava/io/Writer"), Selector.make("write([C)V")),
//			MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Primordial, "Ljava/io/Writer"), Selector.make("write([CII)V")),
//			//for Log
//			MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/util/Log"), Selector.make("d(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I")),
//			MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/util/Log"), Selector.make("d(Ljava/lang/String;Ljava/lang/String;)I")),
//			MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/util/Log"), Selector.make("e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I")),
//			MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/util/Log"), Selector.make("e(Ljava/lang/String;Ljava/lang/String;)I")),
//			MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/util/Log"), Selector.make("i(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I")),
//			MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/util/Log"), Selector.make("i(Ljava/lang/String;Ljava/lang/String;)I")),
	};
	
	private static FieldReference[] fSourceRefs = {
			
	};
	
	private static FieldReference[] fSinkRefs = {
			
	};
	
	public boolean isSourceMethod(MethodReference mr){
		for(MethodReference sr : mSourceRefs){
			if(sr.equals(mr)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isSinkMethod(MethodReference mr){
		for(MethodReference sr : mSinkRefs){
			if(sr.equals(mr)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isSourceField(FieldReference fr){
		for(FieldReference sr : fSourceRefs){
			if(sr.equals(fr))
				return true;
		}
		return false;
	}
	
	public boolean isSinkField(FieldReference fr){
		for(FieldReference sr : fSinkRefs){
			if(sr.equals(fr))
				return true;
		}
		return false;
	}
	
	public boolean isSource(MemberReference mr){
		if(mr instanceof MethodReference){
			return isSourceMethod((MethodReference)mr);
		}else if(mr instanceof FieldReference){
			return isSourceField((FieldReference)mr);
		}
		return false;
	}
	
	public boolean isSink(MemberReference mr){
//		if(mr instanceof MethodReference){
//			return isSinkMethod((MethodReference)mr);
//		}else if(mr instanceof FieldReference){
//			return isSinkField((FieldReference)mr);
//		}
		return false;
	}
	
	public PrivateLeakageDetector(CallGraph cg, PointerAnalysis pa){
		IClassHierarchy cha = cg.getClassHierarchy();
		this.cg = cg;
		this.pa = pa;
		this.supergraph = ICFGSupergraph.make(cg);
		this.cha = cg.getClassHierarchy();
		activityClass = cha.lookupClass(activityTR);
		wvClass = cha.lookupClass(wvTR);
		contextClass = cha.lookupClass(contextTR);
		mapClass = cha.lookupClass(mapTR);
		bitmapClass = cha.lookupClass(bitmapTR);
	}
	
	private class TaintDomain implements TabulationDomain<PointerKey, BasicBlockInContext<IExplodedBasicBlock>>{
		private final Map<PointerKey, Integer> indexMap;
		private final Map<Integer, PointerKey> fastSearchMap;
		private int maxIndex = 1;
		
		private int getNewIndex(){
			return maxIndex++;
		}
		
		public TaintDomain(){
			indexMap = new HashMap<PointerKey, Integer>();
			fastSearchMap = new HashMap<Integer, PointerKey>();
			this.add(dummy);
		}
		
		@Override
		public PointerKey getMappedObject(int n) throws NoSuchElementException {
			// TODO Auto-generated method stub
			PointerKey pk = fastSearchMap.get(n);
			return pk;
		}

		@Override
		public int getMappedIndex(Object o) {
			// TODO Auto-generated method stub
			return indexMap.get(o);
		}

		@Override
		public boolean hasMappedIndex(PointerKey o) {
			// TODO Auto-generated method stub
			return indexMap.containsKey(o);
		}

		@Override
		public int getMaximumIndex() {
			// TODO Auto-generated method stub
			return maxIndex;
		}

		@Override
		public int getSize() {
			// TODO Auto-generated method stub
			return indexMap.size();
		}

		@Override
		public int add(PointerKey o) {
			// TODO Auto-generated method stub
			if(hasMappedIndex(o))
				return getMappedIndex(o);
			
			int newIndex = getNewIndex();
			indexMap.put(o, newIndex);
			fastSearchMap.put(newIndex, o);
			
			return newIndex;
		}

		@Override
		public Iterator<PointerKey> iterator() {
			// TODO Auto-generated method stub
			return indexMap.keySet().iterator();
		}

		@Override
		public boolean hasPriorityOver(PathEdge<BasicBlockInContext<IExplodedBasicBlock>> p1,
				PathEdge<BasicBlockInContext<IExplodedBasicBlock>> p2) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	private boolean isAndroidLibrary(CGNode n){
		MethodReference targetMethodReference =n.getMethod().getReference();
		TypeReference targetClassReference = targetMethodReference.getDeclaringClass();
		
		IClass klass = cha.lookupClass(targetClassReference);
		if(klass != null && targetClassReference.getName().getPackage() != null)
			if(targetClassReference.getName().getPackage().toString().startsWith("android/support/") || targetClassReference.getName().getPackage().toString().startsWith("com/google/"))
				return true;
		return false;
	}
	
	private boolean isAndroidLibrary(IClass klass){
		if(klass != null && klass.getReference().getName().getPackage() != null)
			if(klass.getReference().getName().getPackage().toString().startsWith("android/support/") || klass.getReference().getName().getPackage().toString().startsWith("com/google/"))
				return true;
		return false;
	}
	
	private boolean isApplication(CGNode n){
		MethodReference targetMethodReference = n.getMethod().getReference();
		TypeReference targetClassReference = targetMethodReference.getDeclaringClass();
		
		IClass targetClass = cha.lookupClass(targetClassReference);
		if(targetClass != null)
			if(targetClass != null && targetClass.getClassLoader().getReference().equals(ClassLoaderReference.Application))
				return true;
		return false;
	}
	
	private class TaintProblem implements
    PartiallyBalancedTabulationProblem<BasicBlockInContext<IExplodedBasicBlock>, CGNode, PointerKey> {
		private final TaintDomain domain;
		
		public TaintProblem(TaintDomain d){
			domain = d;
		}
		
		@Override
		public ISupergraph<BasicBlockInContext<IExplodedBasicBlock>, CGNode> getSupergraph() {
			// TODO Auto-generated method stub
			return supergraph;
		}

		@Override
		public TabulationDomain<PointerKey, BasicBlockInContext<IExplodedBasicBlock>> getDomain() {
			// TODO Auto-generated method stub
			return domain;
		}

		private Set<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> getTestSeeds(){
			Set<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> seeds = new HashSet<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>>();
			for(InstanceKey ik : AndroidHybridAppModel.getJSInterfaces()){
				ConcreteTypeKey ctk = (ConcreteTypeKey)ik;
				for(IMethod m : ctk.getConcreteType().getDeclaredMethods()){
					for(CGNode n : cg.getNodes(m.getReference())){
						for(BasicBlockInContext<IExplodedBasicBlock> eb : supergraph.getEntriesForProcedure(n)){
							for(Iterator<BasicBlockInContext<IExplodedBasicBlock>> icall = supergraph.getPredNodes(eb); icall.hasNext(); ){
								BasicBlockInContext<IExplodedBasicBlock> call = icall.next();
								if(supergraph.isCall(call) && call.getNode().getMethod().getDeclaringClass().getReference().getClassLoader().getLanguage().equals(JavaScriptTypes.jsName)){
									for(int i = 2; i <= n.getMethod().getNumberOfParameters(); i++){
										int d = domain.add(pa.getHeapModel().getPointerKeyForLocal(n, i));
										seeds.add(PathEdge.createPathEdge(call, d, eb, d));
									}
								}
							}
						}
					}
				}
			}
			return seeds;
		}
		
		private TypeReference tr = TypeReference.find(ClassLoaderReference.Application, "Landroid/content/Intent");
		
		private SSAAbstractInvokeInstruction findNearestInitInst(IR ir, SSANewInstruction newInst){
			SSAInstruction[] insts = ir.getInstructions();
			for(int i = newInst.iindex+1; i < insts.length; i++){
				SSAInstruction inst = insts[i];
				if(inst == null)
					continue;
				
				if(inst instanceof SSAAbstractInvokeInstruction){
					SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) inst;
					if(invokeInst.getDeclaredTarget().isInit() && invokeInst.getDeclaredTarget().getDeclaringClass().equals(tr) && invokeInst.getNumberOfUses() > 1){
						int actionUse = invokeInst.getUse(1);
						SymbolTable symTab = ir.getSymbolTable();
						if(symTab.isStringConstant(actionUse)){
							String v = symTab.getStringValue(actionUse);
							if(v.equals("android.intent.action.PICK")){
								return invokeInst;
							}
						}
					}
				}
			}
			return null;
		}
		
		private Set<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> getTaintSeeds(){
			Set<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> seeds = new HashSet<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>>();

			for(CGNode n : cg){
				if(!isApplication(n) || isAndroidLibrary(n))
					continue;
				IR ir = n.getIR();
				if(ir != null)
					for(Iterator<NewSiteReference> inew = n.getIR().iterateNewSites(); inew.hasNext(); ){
						NewSiteReference newSite = inew.next();
						if(newSite.getDeclaredType().equals(tr)){
							SSANewInstruction newInst = ir.getNew(newSite);
							SSAAbstractInvokeInstruction initInst = findNearestInitInst(ir, newInst);
							if(initInst != null){
								int def = newInst.getDef();
								int d = domain.add(pa.getHeapModel().getPointerKeyForLocal(n, def));
								fromTo(new SeedKey(), pa.getHeapModel().getPointerKeyForLocal(n, def));
								
								for(BasicBlockInContext<IExplodedBasicBlock> entry : supergraph.getEntriesForProcedure(n)){
									for(Iterator<BasicBlockInContext<IExplodedBasicBlock>> isucc = supergraph.getSuccNodes(entry); isucc.hasNext(); ){
										BasicBlockInContext<IExplodedBasicBlock> succ = isucc.next();
//										if(!succ.isEntryBlock()){
											seeds.add(PathEdge.createPathEdge(entry, d, succ, d));
//										}
									}
								}
//								Queue<BasicBlockInContext<IExplodedBasicBlock>> bQueue = new LinkedBlockingQueue<BasicBlockInContext<IExplodedBasicBlock>>();
//								Set<BasicBlockInContext<IExplodedBasicBlock>> visited = new HashSet<BasicBlockInContext<IExplodedBasicBlock>>();
//								
//								for(BasicBlockInContext<IExplodedBasicBlock> entry : supergraph.getEntriesForProcedure(n)){
//									bQueue.add(entry);
//									visited.add(entry);
//								}
//								
//								while(!bQueue.isEmpty()){
//									BasicBlockInContext<IExplodedBasicBlock> bb = bQueue.poll();
//									if(!bb.getNode().equals(n))
//										continue;
//									
//									SSAInstruction inst = bb.getLastInstruction();
//									if(inst != null && inst.iindex == initInst.iindex){
//										for(Iterator<BasicBlockInContext<IExplodedBasicBlock>> isucc = supergraph.getSuccNodes(bb); isucc.hasNext(); ){
//											BasicBlockInContext<IExplodedBasicBlock> succ = isucc.next();
//											if(!succ.isEntryBlock()){
//												seeds.add(PathEdge.createPathEdge(bb, d, succ, d));
//											}
//										}
//									}
//									
//									for(Iterator<BasicBlockInContext<IExplodedBasicBlock>> isucc = supergraph.getSuccNodes(bb); isucc.hasNext(); ){
//										BasicBlockInContext<IExplodedBasicBlock> succ = isucc.next();
//										if(!visited.contains(succ)){
//											visited.add(succ);
//											bQueue.add(succ);
//										}
//									}
//								}								
							}
						}
					}
			}
			return seeds;
		}
		
		private BasicBlockInContext<IExplodedBasicBlock> getBlockForCall(CGNode n, SSAAbstractInvokeInstruction callInst){
			int callIndex = callInst.iindex;
			
			for(BasicBlockInContext<IExplodedBasicBlock> bb : supergraph){
				if(bb.getNode().equals(n) && bb.getLastInstruction() != null && bb.getLastInstruction().iindex == callIndex)
					return bb;
			}
			
//			int nBlocks = supergraph.getNumberOfBlocks(n);
//			for(int i=0; i<nBlocks; i++){
//				BasicBlockInContext<IExplodedBasicBlock> bb = supergraph.getLocalBlock(n, i);
//				SSAInstruction inst = bb.getLastInstruction();
//				if(inst != null && inst.iindex == callIndex)
//					return bb;
//			}
			return null;
		}
		
		@Override
		public Collection<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> initialSeeds() {
			// TODO Auto-generated method stub
			Set<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> seeds = new HashSet<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>>();
			
			if(TEST){
				seeds = getTaintSeeds();
			}else
			for(MethodReference mr : mSourceRefs){
				if(cg.getNodes(mr).isEmpty()){
					for(CGNode n : cg){
						IR ir = n.getIR();
						if(ir != null){
							Iterator<CallSiteReference> icsr = ir.iterateCallSites();
							for(;icsr.hasNext();){
								CallSiteReference csr = icsr.next();
								if(cg.getPossibleTargets(n, csr).isEmpty()){
									for(SSAAbstractInvokeInstruction callInst : ir.getCalls(csr)){
										if(mr.equals(callInst.getDeclaredTarget())){
											BasicBlockInContext<IExplodedBasicBlock> callBlock = getBlockForCall(n, callInst);
											int defVar = callInst.getDef();
											fromTo(new SeedKey(), pa.getHeapModel().getPointerKeyForLocal(n, defVar));
											int d = domain.add(pa.getHeapModel().getPointerKeyForLocal(n, defVar));
											
//											for(BasicBlockInContext<IExplodedBasicBlock> entrybb : supergraph.getEntriesForProcedure(cg.getFakeRootNode())){
												for(Iterator<BasicBlockInContext<IExplodedBasicBlock>> isucc = supergraph.getSuccNodes(callBlock); isucc.hasNext(); ){
													BasicBlockInContext<IExplodedBasicBlock> succ = isucc.next();
													seeds.add(PathEdge.createPathEdge(callBlock, d, succ, d));
												}	
//											}
										}
									}
								}
							}
						}
					}
				}
				else
					for(CGNode n : cg.getNodes(mr)){
						BasicBlockInContext<IExplodedBasicBlock>[] entries = supergraph.getEntriesForProcedure(n);
						for(BasicBlockInContext<IExplodedBasicBlock> entry : entries){
							for(Iterator<BasicBlockInContext<IExplodedBasicBlock>> iCall = supergraph.getPredNodes(entry); iCall.hasNext();){
								BasicBlockInContext<IExplodedBasicBlock> caller = iCall.next();
								
								if(!supergraph.isCall(caller))
									continue;
								
								SSAInstruction callInst = caller.getLastInstruction();
								
								if(!callInst.hasDef())
									continue;
								System.out.println("Caller: " + caller);
								int defVar = callInst.getDef();
								int d = domain.add(pa.getHeapModel().getPointerKeyForLocal(caller.getNode(), defVar));
								
								for(Iterator<? extends BasicBlockInContext<IExplodedBasicBlock>> iRet = supergraph.getReturnSites(caller, n); iRet.hasNext();){
									BasicBlockInContext<IExplodedBasicBlock> ret = iRet.next();
									System.out.println("Ret: " + ret);
								}
							}
						}
					}
			}
			return seeds;
		}

		@Override
		public IMergeFunction getMergeFunction() {
			// TODO Auto-generated method stub
			return new IMergeFunction(){

				@Override
				public int merge(IntSet x, int j) {
					// TODO Auto-generated method stub
					x.union(SparseIntSet.singleton(j));
					return j;
				}
			};
		}

		@Override
		public IPartiallyBalancedFlowFunctions<BasicBlockInContext<IExplodedBasicBlock>> getFunctionMap() {
			// TODO Auto-generated method stub
			return new IPartiallyBalancedFlowFunctions<BasicBlockInContext<IExplodedBasicBlock>>(){

				@Override
				public IUnaryFlowFunction getNormalFlowFunction(final BasicBlockInContext<IExplodedBasicBlock> src,
						BasicBlockInContext<IExplodedBasicBlock> dest) {
					// TODO Auto-generated method stub
					if(DEBUG){
						System.out.println("--Normal--" );
						System.out.println("src: " + src.getNode());
						System.out.println("\tinst: " + src.getLastInstruction());
						System.out.println("dest: " + dest.getNode());
						System.out.println("\tinst: " + dest.getLastInstruction());
					}
					if(src.getLastInstruction() != null){
						final SSAInstruction inst = src.getLastInstruction();
						final CGNode node = src.getNode();
						return new IUnaryFlowFunction(){

							@Override
							public IntSet getTargets(int d1) {
								// TODO Auto-generated method stub
								final PointerKey pk = domain.getMappedObject(d1);
								SparseIntSet res = SparseIntSet.singleton(d1);
								final Set<Integer> ds = new HashSet<Integer>();
								
								inst.visit((IVisitor)new JSInstructionVisitor(){
									@Override
									public void visitGoto(SSAGotoInstruction instruction) {
										// TODO Auto-generated method stub
										for(Iterator<? extends SSAInstruction> iphi = node.getIR().iteratePhis(); iphi.hasNext(); ){
											SSAInstruction inst = iphi.next();
											SSAPhiInstruction phiInst = (SSAPhiInstruction) inst;
											int def = phiInst.getDef();
											PointerKey defPK = pa.getHeapModel().getPointerKeyForLocal(node, def);
											for(int i=0; i < phiInst.getNumberOfUses(); i++){
												int use = phiInst.getUse(i);
												if(use == -1){
													fromTo(pk, defPK);
													int d2 = domain.add(defPK);
													ds.add(d2);
												}else{
													PointerKey usePK = pa.getHeapModel().getPointerKeyForLocal(node, use);
													if(pk.equals(usePK)){
														fromTo(pk, defPK);
														int d2 = domain.add(defPK);
														ds.add(d2);
													}
												}
											}
										}
									}

									@Override
									public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
										// TODO Auto-generated method stub
										PointerKey arrPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), instruction.getUse(0));
										if(pk.equals(arrPK)){
											PointerKey defPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), instruction.getDef());
											fromTo(pk, defPK);
											ds.add(domain.add(defPK));
										}
									}

									@Override
									public void visitArrayStore(SSAArrayStoreInstruction instruction) {
										// TODO Auto-generated method stub
										PointerKey valuePK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), instruction.getUse(2));
										if(pk.equals(valuePK)){
											PointerKey arrPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), instruction.getUse(0));
											fromTo(pk, arrPK);
											ds.add(domain.add(arrPK));
										}
									}

									@Override
									public void visitBinaryOp(SSABinaryOpInstruction instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitUnaryOp(SSAUnaryOpInstruction instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitConversion(SSAConversionInstruction instruction) {
										// TODO Auto-generated method stub
										PointerKey usePK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), instruction.getUse(0));
										if(pk.equals(usePK)){
											PointerKey defPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), instruction.getDef());
											fromTo(pk, defPK);
											ds.add(domain.add(defPK));
										}
									}

									@Override
									public void visitComparison(SSAComparisonInstruction instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitConditionalBranch(SSAConditionalBranchInstruction instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitSwitch(SSASwitchInstruction instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitReturn(SSAReturnInstruction instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitGet(SSAGetInstruction instruction) {
										// TODO Auto-generated method stub
										
										if(instruction.isStatic()){
											IField sField = cg.getClassHierarchy().resolveField(instruction.getDeclaredField());
											PointerKey sfPK = pa.getHeapModel().getPointerKeyForStaticField(sField);
											
											if(sField != null)
												if(pk.equals(sfPK)){
													PointerKey defPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), instruction.getDef());
													fromTo(pk, defPK);
													ds.add(domain.add(defPK));
												}
										}else{
											IField iField = cg.getClassHierarchy().resolveField(instruction.getDeclaredField());
											GlobalFieldKey gfk = new GlobalFieldKey(iField);
											if(pk.equals(gfk)){
												int def = instruction.getDef();
												PointerKey defPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), def);
												ds.add(domain.add(defPK));
											}
										}
									}

									@Override
									public void visitPut(SSAPutInstruction instruction) {
										// TODO Auto-generated method stub										
										if(instruction.isStatic()){
											PointerKey usePK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), instruction.getUse(0));
											if(pk.equals(usePK)){
												IField sField = cg.getClassHierarchy().resolveField(instruction.getDeclaredField());
												if(sField != null){
													PointerKey sfPK = pa.getHeapModel().getPointerKeyForStaticField(sField);
													fromTo(pk, sfPK);
													ds.add(domain.add(sfPK));
												}
											}
										}else{
											PointerKey usePK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), instruction.getUse(1));
											if(pk.equals(usePK)){
												IField iField = cg.getClassHierarchy().resolveField(instruction.getDeclaredField());
												if(iField != null){
													GlobalFieldKey gfk = new GlobalFieldKey(iField);
													fromTo(pk, gfk);
													ds.add(domain.add(gfk));
												}
											}
										}
									}

									@Override
									public void visitInvoke(SSAInvokeInstruction instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitNew(SSANewInstruction instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitArrayLength(SSAArrayLengthInstruction instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitThrow(SSAThrowInstruction instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitMonitor(SSAMonitorInstruction instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitCheckCast(SSACheckCastInstruction instruction) {
										// TODO Auto-generated method stub
										int use = instruction.getUse(0);
										PointerKey usePK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), use);
										if(pk.equals(usePK)){
											int def = instruction.getDef();
											PointerKey defPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), def);
											fromTo(pk, defPK);
											int d2 = domain.add(defPK);
											ds.add(d2);
										}
									}

									@Override
									public void visitInstanceof(SSAInstanceofInstruction instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitPhi(SSAPhiInstruction instruction) {
										// TODO Auto-generated method stub
										for(int i=0; i<instruction.getNumberOfUses(); i++){
											PointerKey usePK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), instruction.getUse(i));
											if(pk.equals(usePK)){
												PointerKey defPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), instruction.getDef());
												fromTo(pk, defPK);
												ds.add(domain.add(defPK));
												break;
											}
										}
									}

									@Override
									public void visitPi(SSAPiInstruction instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitLoadMetadata(SSALoadMetadataInstruction instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitAstLexicalRead(AstLexicalRead instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitAstLexicalWrite(AstLexicalWrite instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitAstGlobalRead(AstGlobalRead instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitAstGlobalWrite(AstGlobalWrite instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitAssert(AstAssertInstruction instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitEachElementGet(EachElementGetInstruction inst) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitEachElementHasNext(EachElementHasNextInstruction inst) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitIsDefined(AstIsDefinedInstruction inst) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitEcho(AstEchoInstruction inst) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitJavaScriptInvoke(JavaScriptInvoke instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitTypeOf(JavaScriptTypeOfInstruction instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitJavaScriptPropertyRead(JavaScriptPropertyRead instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitJavaScriptPropertyWrite(JavaScriptPropertyWrite instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitJavaScriptInstanceOf(JavaScriptInstanceOf instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitWithRegion(JavaScriptWithRegion instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitCheckRef(JavaScriptCheckReference instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitSetPrototype(SetPrototype instruction) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void visitPrototypeLookup(PrototypeLookup instruction) {
										// TODO Auto-generated method stub
										
									}
									
								});
								
								if(ds.size() > 1){
									res = MutableSparseIntSet.make(res);
									for(int d : ds){
										((MutableSparseIntSet) res).add(d);
									}
								}else{
									for(int d : ds){
										res = SparseIntSet.add(res, d);
									}
								}
								return res;
							}
							
						};
					}else
						return IdentityFlowFunction.identity(); 
				}

				@Override
				public IUnaryFlowFunction getCallFlowFunction(final BasicBlockInContext<IExplodedBasicBlock> src,
						final BasicBlockInContext<IExplodedBasicBlock> dest, BasicBlockInContext<IExplodedBasicBlock> ret) {
					// TODO Auto-generated method stub
					if(DEBUG){
						System.out.println("--callflow--" );
						System.out.println("src: " + src.getNode());
						System.out.println("\tinst: " + src.getLastInstruction());
						System.out.println("dest: " + dest.getNode());
						System.out.println("\tinst: " + dest.getLastInstruction());
						System.out.println("ret: " + ret);
						if(ret != null)
							System.out.println("\tinst: " + ret.getLastInstruction());
					}
					final SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) src.getLastInstruction();
					final IClass declTargetClass = cha.lookupClass(invokeInst.getDeclaredTarget().getDeclaringClass());
					final Selector declTargetSelector = invokeInst.getDeclaredTarget().getSelector();
					if(isAndroidLibrary(dest.getNode())){
						return KillEverything.singleton();
					}
					if(isSinkMethod(dest.getNode().getMethod().getReference()) || (src.getNode().getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application) && dest.getNode().getMethod().getDeclaringClass().getClassLoader().getReference().equals(JavaScriptTypes.jsLoader))){
						return new IUnaryFlowFunction(){

							@Override
							public IntSet getTargets(int d1) {
								// TODO Auto-generated method stub
								PointerKey tpk = domain.getMappedObject(d1);
								for(int i=0; i<src.getLastInstruction().getNumberOfUses(); i++)
									if(pa.getHeapModel().getPointerKeyForLocal(src.getNode(), src.getLastInstruction().getUse(i)).equals(tpk)){
										for(List<PointerKey> l : calcPath(tpk)){
											warn.add(new LeakWarning(l, src.getLastInstruction()));
										}
									}	
								return EmptyIntSet.instance;
							}
						};
					}
//					else if((declTargetClass.equals(mapClass) || cha.isSubclassOf(declTargetClass, mapClass)) && (
//							declTargetSelector.equals(putSelector) || declTargetSelector.equals(getSelector)
//							)){
//						if(declTargetSelector.equals(putSelector)){
//							return new IUnaryFlowFunction(){
//
//								@Override
//								public IntSet getTargets(int d1) {
//									// TODO Auto-generated method stub
//									SparseIntSet s = SparseIntSet.singleton(d1);
//									int useUse = invokeInst.getUse(2);
//									PointerKey usePK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), useUse);
//									if(domain.getMappedObject(d1).equals(usePK)){
//										PointerKey refPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), invokeInst.getUse(0));
//										for(InstanceKey ik : pa.getPointsToSet(refPK)){
//											for(Iterator<Object> iobj = pa.getHeapGraph().getPredNodes(ik); iobj.hasNext(); ){
//												PointerKey pk = (PointerKey)iobj.next();
//												if(pk instanceof LocalPointerKey){
//													
//												}else{
//													System.out.println("====> add!: " + pk);
//													s = s.add(s, domain.add(pk));
//												}
//											}
//										}
//									}
//									return s;
//								}
//							};
//						}else if(declTargetSelector.equals(getSelector)){
//							return new IUnaryFlowFunction(){
//
//								@Override
//								public IntSet getTargets(int d1) {
//									// TODO Auto-generated method stub
//									SparseIntSet s = SparseIntSet.singleton(d1);
//									if(!invokeInst.isStatic() && invokeInst.hasDef()){
//										int refUse = invokeInst.getUse(0);
//										PointerKey refPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), refUse);
//										if(domain.getMappedObject(d1).equals(refPK)){
//											int d2 = domain.add(refPK);
//											s = s.add(s, d2);
//										}
//									}
//									return s;
//								}
//							};
//						}
//					}
//					else if(dest.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial)){
//							return KillEverything.singleton();
////						return IdentityFlowFunction.identity();
//					}
					else{
						return new IUnaryFlowFunction(){

							@Override
							public IntSet getTargets(int d1) {
								// TODO Auto-generated method stub
								PointerKey tpk = domain.getMappedObject(d1);
								Set<Integer> args = new HashSet<Integer>();
								CGNode destNode = dest.getNode();
								IClass destClass = destNode.getMethod().getDeclaringClass();
								Selector destSelector = destNode.getMethod().getSelector();
								SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) src.getLastInstruction();
								IClass declTargetClass = cha.lookupClass(invokeInst.getDeclaredTarget().getDeclaringClass());
								/*
								 * Context
								private final Selector oncreateSelector = Selector.make("onCreate(Landroid/os/Bundle;)V");
									private final Selector startActivitySelector = Selector.make("startActivity(Landroid/content/Intent;)V");
									private final Selector startActivityForResultSelector = Selector.make("startActivityForResult(Landroid/content/Intent;I)V");
									private final Selector onActivityResultSelector = Selector.make("onActivityResult(IILandroid/content/Intent;)V");
									private final Selector requestPermissionSelector = Selector.make("requestPermissions([Ljava/lang/String;I)V");
									private final Selector onRequestPermissionResultSelector = Selector.make("onRequestPermissionsResult(I[Ljava/lang/String;[I)V");
									WebView
									private final Selector loadUrlSelector = Selector.make("loadUrl(Ljava/lang/String;)V");
												 */
								
								if((destClass.equals(contextClass) || cha.isSubclassOf(destClass, contextClass)) && (
										destSelector.equals(oncreateSelector) ||
										destSelector.equals(onRequestPermissionResultSelector) ||
										destSelector.equals(onActivityResultSelector)
										)){
									if(destSelector.equals(oncreateSelector))
										if(tpk instanceof LocalPointerKey)
											return null;
										else
											return SparseIntSet.singleton(d1);
									else if(destSelector.equals(onRequestPermissionResultSelector))
										if(tpk instanceof LocalPointerKey)
											return null;
										else
											return SparseIntSet.singleton(d1);
									else if(destSelector.equals(onActivityResultSelector)){
										if(tpk instanceof LocalPointerKey){
											fromTo(tpk, pa.getHeapModel().getPointerKeyForLocal(destNode, 4));
											return SparseIntSet.singleton(domain.add(pa.getHeapModel().getPointerKeyForLocal(destNode, 4)));
										}else{
											SparseIntSet s =SparseIntSet.singleton(d1);
											fromTo(tpk, pa.getHeapModel().getPointerKeyForLocal(destNode, 4));
											return s.add(s, domain.add(pa.getHeapModel().getPointerKeyForLocal(destNode, 4)));
										}
									}
								}else if(declTargetClass != null && (declTargetClass.equals(wvClass) || cha.isSubclassOf(declTargetClass, wvClass)) &&	(
										invokeInst.getDeclaredTarget().getSelector().equals(loadUrlSelector) && destClass.getClassLoader().getReference().equals(JavaScriptTypes.jsLoader)
												)){
									return null;
								}else{
									for(int i=0; i<src.getLastInstruction().getNumberOfUses(); i++)
										if(pa.getHeapModel().getPointerKeyForLocal(src.getNode(), src.getLastInstruction().getUse(i)).equals(tpk)){
											args.add(i);
										}
									
									if(args.isEmpty())
										if(tpk instanceof LocalPointerKey)
											return null;
										else
											return SparseIntSet.singleton(d1);
									else{
										Set<PointerKey> pks = new HashSet<PointerKey>();
										
										for(int arg : args)
											pks.add(pa.getHeapModel().getPointerKeyForLocal(dest.getNode(), arg+1));
										
										SparseIntSet res = null;
										
										if(tpk instanceof LocalPointerKey)
											res = new SparseIntSet();
										else
											res = SparseIntSet.singleton(d1);
										
										for(PointerKey pk : pks){
											int d = domain.add(pk);
											res = SparseIntSet.add(res, d);
										}
										return res;
									}
								}
								return null;
							}							
						};
					}
				}

				private Set<SSAReturnInstruction> findRets(BasicBlockInContext<IExplodedBasicBlock> bb){
					Set<SSAReturnInstruction> retInsts = new HashSet<SSAReturnInstruction>();
					CGNode n = bb.getNode();
					IR ir = n.getIR();
					if(ir != null)
						for(SSAInstruction inst : ir.getInstructions()){
							if(inst != null && inst instanceof SSAReturnInstruction && inst.getNumberOfUses() > 0)
								retInsts.add((SSAReturnInstruction)inst);
						}
					
					return retInsts;
				}
				
				@Override
				public IFlowFunction getReturnFlowFunction(final BasicBlockInContext<IExplodedBasicBlock> call,
						final BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest) {
					// TODO Auto-generated method stub
					if(DEBUG){
						System.out.println("--returnflow--" );
						System.out.println("src: " + src.getNode());
						System.out.println("\tinst: " + src.getLastInstruction());
						System.out.println("dest: " + dest.getNode());
						System.out.println("\tinst: " + dest.getLastInstruction());
						System.out.println("call: " + call.getNode());
						System.out.println("\tinst: " + call.getLastInstruction());
					}
					
					final Set<PointerKey> pkSet = new HashSet<PointerKey>();
					
					for(SSAReturnInstruction inst : findRets(src)){
						int use = inst.getUse(0);
						PointerKey usePK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), use);
						pkSet.add(usePK);
					}
					
					return new IUnaryFlowFunction(){
						@Override
						public IntSet getTargets(int d1) {
							// TODO Auto-generated method stub
							
							PointerKey pk = domain.getMappedObject(d1);
							
							if(pkSet.contains(pk)){
								fromTo(domain.getMappedObject(d1), pa.getHeapModel().getPointerKeyForLocal(call.getNode(), call.getLastInstruction().getDef()));
								int d = domain.add(pa.getHeapModel().getPointerKeyForLocal(call.getNode(), call.getLastInstruction().getDef()));
								if(pk instanceof LocalPointerKey){
									return SparseIntSet.singleton(d);
								}else{
									SparseIntSet s = SparseIntSet.singleton(d);
									return s.add(s, d1);
								}
							}else{
								if(pk instanceof LocalPointerKey){
									return null;
								}else{
									return SparseIntSet.singleton(d1);
								}	
							}
						}
					};
				}

				@Override
				public IUnaryFlowFunction getCallToReturnFlowFunction(final BasicBlockInContext<IExplodedBasicBlock> src,
						final BasicBlockInContext<IExplodedBasicBlock> dest) {
					// TODO Auto-generated method stub
					if(DEBUG){
						System.out.println("--calltoreturn--" );
						System.out.println("src: " + src.getNode());
						System.out.println("\tinst: " + src.getLastInstruction());
						System.out.println("dest: " + dest.getNode());
						System.out.println("\tinst: " + dest.getLastInstruction());
					}
					final SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction)src.getLastInstruction();
					
					return new IUnaryFlowFunction(){

						@Override
						public IntSet getTargets(int d1) {
							// TODO Auto-generated method stub
							IClass declTargetClass = cha.lookupClass(invokeInst.getDeclaredTarget().getDeclaringClass());
							Selector declTargetSelector = invokeInst.getDeclaredTarget().getSelector();
							if(declTargetClass != null && (declTargetClass.equals(mapClass) || cha.isSubclassOf(declTargetClass, mapClass)) && (
									declTargetSelector.equals(putSelector) || declTargetSelector.equals(getSelector)
									)){
								if(declTargetSelector.equals(putSelector)){
									
									int useUse = invokeInst.getUse(2);
									PointerKey usePK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), useUse);
									if(domain.getMappedObject(d1).equals(usePK)){
										PointerKey refPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), invokeInst.getUse(0));
										SparseIntSet s = SparseIntSet.singleton(d1);
										for(InstanceKey ik : pa.getPointsToSet(refPK)){
											for(Iterator<Object> iobj = pa.getHeapGraph().getPredNodes(ik); iobj.hasNext(); ){
												PointerKey pk = (PointerKey)iobj.next();
												
												if(pk instanceof LocalPointerKey){
													
												}else if(pk instanceof StaticFieldKey){
													StaticFieldKey stpk = (StaticFieldKey) pk;
													if(stpk.getField().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application) && !isAndroidLibrary(stpk.getField().getDeclaringClass())){
														fromTo(domain.getMappedObject(d1), pk);
														int d = domain.add(pk);
														s = s.add(s, d);
													}
												}else if(pk instanceof InstanceFieldKey){
													InstanceFieldKey itpk = (InstanceFieldKey) pk;
													if(itpk.getField().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application) && !isAndroidLibrary(itpk.getField().getDeclaringClass())){
														fromTo(domain.getMappedObject(d1), pk);
														int d = domain.add(pk);
														s = s.add(s, d);
													}
												}
											}
										}
										return s;
									}
								}else if(declTargetSelector.equals(getSelector)){
									if(!invokeInst.isStatic() && invokeInst.hasDef()){
										int refUse = invokeInst.getUse(0);
										PointerKey refPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), refUse);
										if(domain.getMappedObject(d1).equals(refPK)){
											int def = invokeInst.getDef();
											PointerKey defPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), def);
											int d2 = domain.add(defPK);
											SparseIntSet s = SparseIntSet.singleton(d1);
											return s.add(s, d2);
										}
									}
								}
							}else if(declTargetClass != null && (declTargetClass.equals(bitmapClass) || cha.isSubclassOf(declTargetClass, bitmapClass)) && (
									declTargetSelector.equals(compressSelector)
									)){
									int refUse = invokeInst.getUse(0);
									PointerKey refPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), refUse);
									if(domain.getMappedObject(d1).equals(refPK)){
										if(invokeInst.hasDef()){
											int def = invokeInst.getDef();
											PointerKey defPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), def);
											int d2 = domain.add(defPK);
											SparseIntSet s = SparseIntSet.singleton(d1);
											s = s.add(s, d2);
										}
										int io = invokeInst.getUse(3);
										PointerKey ioPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), io);
										int d3 = domain.add(ioPK);
										SparseIntSet s = SparseIntSet.singleton(d1);
										return s.add(s, d3);
									}
							}else if(declTargetClass != null && declTargetClass.getClassLoader().getReference().equals(ClassLoaderReference.Primordial)){
//								if(!invokeInst.isStatic() && invokeInst.hasDef()){
								if(invokeInst.hasDef()){
									SparseIntSet s = SparseIntSet.singleton(d1);
//									int refUse = invokeInst.getUse(0);
//									PointerKey refPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), refUse);

//									if(domain.getMappedObject(d1).equals(refPK)){
//										int def = invokeInst.getDef();
//										PointerKey defPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), def);
//										int d2 = domain.add(defPK);
//										SparseIntSet s = SparseIntSet.singleton(d1);
//										return s.add(s, d2);
//									}
									for(int i = 0; i < invokeInst.getNumberOfUses(); i++){
										int use = invokeInst.getUse(i);
										PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), use);
										if(domain.getMappedObject(d1).equals(pk)){
											int def = invokeInst.getDef();
											PointerKey defPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), def);
											int d2 = domain.add(defPK);
											return s.add(s, d2);
										}
									}
								}else if(invokeInst.getDeclaredTarget().isInit()){
									SparseIntSet s = SparseIntSet.singleton(d1);
									for(int i = 1; i < invokeInst.getNumberOfUses(); i++){
										int use = invokeInst.getUse(i);
										PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), use);
										if(domain.getMappedObject(d1).equals(pk)){
											int def = invokeInst.getUse(0);
											PointerKey defPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), def);
											int d2 = domain.add(defPK);
											return s.add(s, d2);
										}
									}
								}
							}else {
//								if(!invokeInst.isStatic() && invokeInst.hasDef()){
//									int refUse = invokeInst.getUse(0);
//									PointerKey refPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), refUse);
//									if(domain.getMappedObject(d1).equals(refPK)){
//										int def = invokeInst.getDef();
//										PointerKey defPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), def);
//										int d2 = domain.add(defPK);
//										SparseIntSet s = SparseIntSet.singleton(d1);
//										return s.add(s, d2);
//									}
//								}
							}
							return SparseIntSet.singleton(d1);
						}
					};
				}

				@Override
				public IUnaryFlowFunction getCallNoneToReturnFlowFunction(final BasicBlockInContext<IExplodedBasicBlock> src,
						BasicBlockInContext<IExplodedBasicBlock> dest) {
					// TODO Auto-generated method stub
					if(DEBUG){
						System.out.println("--callnonetoreturn--" );
						System.out.println("src: " + src.getNode());
						System.out.println("\tinst: " + src.getLastInstruction());
						System.out.println("dest: " + dest.getNode());
						System.out.println("\tinst: " + dest.getLastInstruction());
					}
					return new IUnaryFlowFunction(){

						@Override
						public IntSet getTargets(int d1) {
							// TODO Auto-generated method stub
							SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) src.getLastInstruction();
							MutableSparseIntSet s = MutableSparseIntSet.makeEmpty();
							s.add(d1);
//							if(invokeInst.hasDef()){
////								int refUse = invokeInst.getUse(0);
////								PointerKey refPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), refUse);
//
////								if(domain.getMappedObject(d1).equals(refPK)){
////									int def = invokeInst.getDef();
////									PointerKey defPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), def);
////									int d2 = domain.add(defPK);
////									SparseIntSet s = SparseIntSet.singleton(d1);
////									return s.add(s, d2);
////								}
//								for(int i = 0; i < invokeInst.getNumberOfUses(); i++){
//									int use = invokeInst.getUse(i);
//									PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), use);
//									if(domain.getMappedObject(d1).equals(pk)){
//										int def = invokeInst.getDef();
//										PointerKey defPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), def);
//										int d2 = domain.add(defPK);
//										return s.add(s, d2);
//									}
//								}
//							}
							boolean is = false;
							for(int i = 0; i < invokeInst.getNumberOfUses(); i++){
								int use = invokeInst.getUse(i);
								PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), use);
								if(domain.getMappedObject(d1).equals(pk)){
									is = true;
									break;
								}
							}
							
							if(is){
								for(int i = 0; i < invokeInst.getNumberOfUses(); i++){
									int use = invokeInst.getUse(i);
									PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), use);
									fromTo(domain.getMappedObject(d1), pk);
									int d3 = domain.add(pk);
									s.add(d3);
								}
								
								if(invokeInst.hasDef()){
									int def = invokeInst.getDef();
									PointerKey defPK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), def);
									fromTo(domain.getMappedObject(d1), defPK);
									int d2 = domain.add(defPK);
									s.add(d2);
								}
							}
							return s;
						}
						
					};
				}

				@Override
				public IFlowFunction getUnbalancedReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
						BasicBlockInContext<IExplodedBasicBlock> dest) {
					// TODO Auto-generated method stub
					if(DEBUG){
						System.out.println("--unbalancedreturnflow--" );
						System.out.println("src: " + src.getNode());
						System.out.println("\tinst: " + src.getLastInstruction());
						System.out.println("dest: " + dest.getNode());
						System.out.println("\tinst: " + dest.getLastInstruction());
					}
//					return IdentityFlowFunction.identity();
//					System.out.println("dest: " + dest);
					
//					return KillEverything.singleton();
//					return KillLocals.singleton(domain);

					final Set<PointerKey> pkSet = new HashSet<PointerKey>();
					
					for(SSAReturnInstruction inst : findRets(src)){
						int use = inst.getUse(0);
						PointerKey usePK = pa.getHeapModel().getPointerKeyForLocal(src.getNode(), use);
						pkSet.add(usePK);
					}
					
					final BasicBlockInContext<IExplodedBasicBlock> call = getPairedCallBlock(dest);
					
					return new IUnaryFlowFunction(){
						@Override
						public IntSet getTargets(int d1) {
							// TODO Auto-generated method stub
							
							PointerKey pk = domain.getMappedObject(d1);
							
							if(pkSet.contains(pk)){
								fromTo(pk, pa.getHeapModel().getPointerKeyForLocal(call.getNode(), call.getLastInstruction().getDef()));
								int d = domain.add(pa.getHeapModel().getPointerKeyForLocal(call.getNode(), call.getLastInstruction().getDef()));
								if(pk instanceof LocalPointerKey){
									return SparseIntSet.singleton(d);
								}else{
									SparseIntSet s = SparseIntSet.singleton(d);
									return s.add(s, d1);
								}
							}else{
								if(pk instanceof LocalPointerKey){
									return null;
								}else{
									return SparseIntSet.singleton(d1);
								}	
							}
						}
					};
				}
			};
		}

		private BasicBlockInContext<IExplodedBasicBlock> getPairedCallBlock(BasicBlockInContext<IExplodedBasicBlock> ret){
			for(Iterator<BasicBlockInContext<IExplodedBasicBlock>> ipred = supergraph.getPredNodes(ret); ipred.hasNext(); ){
				BasicBlockInContext<IExplodedBasicBlock> pred = ipred.next();
				if(supergraph.isCall(pred))
					return pred;
			}
			Assertions.UNREACHABLE("paired call block does not exist: " + ret);
			return null;
		}
		
		@Override
		public BasicBlockInContext<IExplodedBasicBlock> getFakeEntry(BasicBlockInContext<IExplodedBasicBlock> n) {
			// TODO Auto-generated method stub
			BasicBlockInContext<IExplodedBasicBlock>[] entries = supergraph.getEntriesForProcedure(n.getNode());
			if(entries.length > 0)
				return entries[0];
			else
				return null;
		}
	}
	
	public TabulationResult<BasicBlockInContext<IExplodedBasicBlock>, CGNode, PointerKey> analyze() {
		PartiallyBalancedTabulationSolver<BasicBlockInContext<IExplodedBasicBlock>, CGNode, PointerKey> solver = PartiallyBalancedTabulationSolver
		        .createPartiallyBalancedTabulationSolver(new TaintProblem(new TaintDomain()), null);
		    TabulationResult<BasicBlockInContext<IExplodedBasicBlock>, CGNode, PointerKey> result = null;
		    try {
		      result = solver.solve();
		    } catch (CancelException e) {
		      // this shouldn't happen 
		      Assertions.UNREACHABLE();
		    }
		    return result;
	}
	
	static class KillLocals implements IUnaryFlowFunction{
		private static KillLocals instance;
		private TaintDomain domain;
		
		public static KillLocals singleton(TaintDomain domain){
			if(instance == null)
				instance = new KillLocals(domain);
			return instance;
		}
		
		
		private KillLocals(TaintDomain domain){
			this.domain = domain;
		}
		
		@Override
		public IntSet getTargets(int d1) {
			// TODO Auto-generated method stub
			PointerKey pk = domain.getMappedObject(d1);

			if(pk instanceof LocalPointerKey){
				return null;
			}else{
				return SparseIntSet.singleton(d1);
			}
		}
		
	}
	
	class GlobalFieldKey implements PointerKey{
		private IField f; 
		
		private GlobalFieldKey(IField f){
			this.f = f;
		}
		
		@Override
		public String toString(){
			String res = "[ " + f + " ]";
			return res;
		}
		
		@Override
		public int hashCode(){
			return f.hashCode()*3;
		}
		
		@Override
		public boolean equals(Object o){
			if(o instanceof GlobalFieldKey){
				GlobalFieldKey g = (GlobalFieldKey)o;
				if(g.f.equals(((GlobalFieldKey) o).f))
					return true;
			}
			return false;
		}
	}
	
	public Set<LeakWarning> getWarnings(){
		return warn;
	}
	
	class SeedKey implements PointerKey{
		
		@Override
		public String toString(){
			return "SEED";
		}
	}
	
	public class LeakWarning{
		private List<PointerKey> path;
		private SSAInstruction sinkPoint;
		private List<Pair<CGNode, Pair<String, String>>> pathFlow; 
		
		public LeakWarning(List<PointerKey> path, SSAInstruction sinkPoint){
			pathFlow = new ArrayList<Pair<CGNode, Pair<String, String>>>();
			this.path = path;
			this.sinkPoint = sinkPoint;
			buildPathFlow();
		}
		
		private void buildPathFlow(){
			String res = "[Warning] private leakage detected.";
			for(int i=0; i < path.size(); i++){
				if(i != 0)
					res += "\n\t\t=>";
				PointerKey pk = path.get(i);
				if(pk instanceof LocalPointerKey){
					
					LocalPointerKey lpk = (LocalPointerKey) pk;
					CGNode n = lpk.getNode();
					int var = lpk.getValueNumber();
					SSAInstruction defInst = n.getDU().getDef(var);
					if(defInst instanceof SSAAbstractInvokeInstruction && defInst.getDef() == var && path.get(i - 1) instanceof LocalPointerKey && !((LocalPointerKey) path.get(i - 1)).getNode().equals(n)){
						PointerKey ppk = path.get(i - 1);
						LocalPointerKey lppk = (LocalPointerKey) ppk;
						for(Iterator<SSAInstruction> ipinst = lppk.getNode().getDU().getUses(lppk.getValueNumber()); ipinst.hasNext(); ){
							SSAInstruction pinst = ipinst.next();
							if(pinst instanceof SSAReturnInstruction){
								pathFlow.add(Pair.make(lppk.getNode(), Pair.make(getInstString(lppk.getNode(), pinst), getVarString(lppk.getNode(), pinst, lppk.getValueNumber()))));
								pathFlow.add(Pair.make(n, Pair.make(getInstString(n, defInst), getVarString(n, defInst, var))));
							}
						}
					}else{
						pathFlow.add(Pair.make(n, Pair.make(getInstString(n, defInst), getVarString(n, defInst, var))));
					}
				}else if(pk instanceof SeedKey){
					res += "\n\tSeed";
				}else{
					Assertions.UNREACHABLE("Please implement string output method");
				}
			}
			
			LocalPointerKey lastPK = (LocalPointerKey)path.get(path.size()-1);
			CGNode n = lastPK.getNode();
			int var = lastPK.getValueNumber();
			
			pathFlow.add(Pair.make(n, Pair.make(getInstString(n, sinkPoint), getVarString(n, sinkPoint, var))));
		}
		
		private String getVarString(CGNode n, SSAInstruction inst, int var){
			SymbolTable symTab = n.getIR().getSymbolTable();
			String[] names = n.getIR().getLocalNames(inst.iindex, var);
			if(names == null || names.length == 0)
				return var + "";
			
			String res = "";
			for(String name : names){
				res += name + " ";
			}
			return res;
		}
		
		private String getInstString(CGNode n, SSAInstruction inst){
			IMethod m = n.getMethod();
			int instIndex = inst.iindex;
			int i = instIndex + 1;
			for(; i < n.getIR().getInstructions().length; i++){
				if(n.getIR().getInstructions()[i] == null)
					continue;
				break;
			}
			
			instIndex = i - 1;
			
			if(m instanceof AstMethod){
				
				com.ibm.wala.cast.tree.CAstSourcePositionMap.Position p = ((AstMethod)m).getSourcePosition(instIndex);
				SourceBuffer buf;
				try {
					buf = new SourceBuffer(p);
					return buf.toString();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				return inst.toString(n.getIR().getSymbolTable());
			}
			return inst.toString();
		}
		
		public String toString(){
			String res = "[Warning] private leakage detected.";
			for(int i=0; i < path.size(); i++){
				if(i != 0)
					res += "\n\t\t=>";
				PointerKey pk = path.get(i);
				if(pk instanceof LocalPointerKey){
					
					LocalPointerKey lpk = (LocalPointerKey) pk;
					CGNode n = lpk.getNode();
					int var = lpk.getValueNumber();
					SSAInstruction defInst = n.getDU().getDef(var);
					if(defInst instanceof SSAAbstractInvokeInstruction && defInst.getDef() == var && path.get(i - 1) instanceof LocalPointerKey && !((LocalPointerKey) path.get(i - 1)).getNode().equals(n)){
						PointerKey ppk = path.get(i - 1);
						LocalPointerKey lppk = (LocalPointerKey) ppk;
						for(Iterator<SSAInstruction> ipinst = lppk.getNode().getDU().getUses(lppk.getValueNumber()); ipinst.hasNext(); ){
							SSAInstruction pinst = ipinst.next();
							if(pinst instanceof SSAReturnInstruction){
								res += "\n\tNode: " + lppk.getNode();
								res += "\n\tVar: " + getVarString(lppk.getNode(), pinst, lppk.getValueNumber());
								res += "\n\tInst: " + getInstString(lppk.getNode(), pinst);
								
								res += "\n\t\t=>";
								
								res += "\n\tNode: " + n;
								res += "\n\tVar: " + getVarString(n, defInst, var);
								res += "\n\tInst: " + getInstString(n, defInst);
							}
						}
					}else{
						res += "\n\tNode: " + n;
						res += "\n\tVar: " + getVarString(n, defInst, var);
						res += "\n\tInst: " + getInstString(n, defInst);
					}
				}else if(pk instanceof SeedKey){
					res += "\n\tSeed";
				}else{
					Assertions.UNREACHABLE("Please implement string output method");
				}
			}
			
			LocalPointerKey lastPK = (LocalPointerKey)path.get(path.size()-1);
			CGNode n = lastPK.getNode();
			int var = lastPK.getValueNumber();
			res += "\n\t\t=>";
			res += "\n\tNode: " + n;
			res += "\n\tVar: " + getVarString(n, sinkPoint, var);
			res += "\n\tInst: " + getInstString(n, sinkPoint);
			
			return res;
		}
		
		@Override
		public int hashCode(){
			return path.hashCode();
		}
		
		@Override
		public boolean equals(Object o){
			if(o instanceof LeakWarning)
				return path.equals(((LeakWarning) o).path);
			return false;
		}
		
		public void printPathFlow(String out){
			Visualizer vis = Visualizer.getInstance();
			vis.setType(GraphType.Digraph);
			
			for(int i=0; i < pathFlow.size()-1; i++){
				Pair<CGNode, Pair<String, String>> pp = pathFlow.get(i);
				CGNode n = pp.fst;
				String inst = pp.snd.fst;
				String var = pp.snd.snd;
				
				Pair<CGNode, Pair<String, String>> npp = pathFlow.get(i+1);
				CGNode nn = npp.fst;
				String ninst = npp.snd.fst;
				String nvar = npp.snd.snd;
				
				String s = "";
				if(n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(JavaScriptTypes.jsLoader)){
					
					String nname = n.toString();
					String fstname = nname.substring(0, nname.indexOf("/")-2);
					String lastname = nname.substring(nname.lastIndexOf("/")+1, nname.length());
					String middlename = nname.substring(nname.indexOf("/")+1, nname.lastIndexOf("/"));
					
//					System.out.println("fst: " + fstname);
//					System.out.println("midd: " + middlename.substring(middlename.lastIndexOf("/")+1, middlename.length()));
//					System.out.println("last: " + lastname);
					s = nname + fstname + middlename.substring(middlename.lastIndexOf("/")+1, middlename.length()) + lastname; 
				}else
					s = n.toString();
				System.out.println("s: "+s);
				s += "\nInst: " + inst;
				s += "\nVariable: " + var;
				
				String ns = "";
				if(nn.getMethod().getDeclaringClass().getClassLoader().getReference().equals(JavaScriptTypes.jsLoader)){
					String nname = nn.toString();
					String fstname = nname.substring(0, nname.indexOf("/")-2);
					String lastname = nname.substring(nname.lastIndexOf("/")+1, nname.length());
					String middlename = nname.substring(nname.indexOf("/")+1, nname.lastIndexOf("/"));
					
					ns = nname + fstname + middlename.substring(middlename.lastIndexOf("/")+1, middlename.length()) + lastname;
				}else
					ns = nn.toString();
				
				ns += "\nInst: " + ninst;
				ns += "\nVariable: " + nvar;
				
				vis.fromAtoB(s, ns);
			}
			vis.printGraph(out);
		}
	}
}


