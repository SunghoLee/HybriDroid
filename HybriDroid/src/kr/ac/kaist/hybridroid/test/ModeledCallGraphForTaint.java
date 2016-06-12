package kr.ac.kaist.hybridroid.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import com.ibm.wala.cast.js.types.JavaScriptTypes;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.impl.ExplicitCallGraph;
import com.ibm.wala.ipa.callgraph.impl.ExplicitCallGraph.ExplicitNode;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.IRFactory;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSet;

import kr.ac.kaist.hybridroid.callgraph.HybridIRFactory;
import kr.ac.kaist.hybridroid.print.NodePrinter;

public class ModeledCallGraphForTaint implements CallGraph {

	public static boolean MODEL_APPLICATION_ONLY = true;
	private static int MAX_NODE_NUM;
	private CallGraph delegate;
	private static IClassHierarchy cha;
	private IClass activityClass;
	private IClass webViewClass;
	private IClass contextClass;
	private IClass handlerClass;
	private static IRFactory irFactory;
	private Map<CGNode, Set<CGNode>> predMap;
	private Map<CGNode, Set<CGNode>> succMap;
	private Map<Pair<CGNode, CallSiteReference>, Set<CGNode>> possibleTargetMap;
	private Map<Pair<CGNode, CGNode>, Set<CallSiteReference>> possibleSiteMap;
	private Map<Integer, CGNode> mNodes;
	
	public ModeledCallGraphForTaint(CallGraph delegate){
		this.delegate = delegate;
		this.cha = delegate.getClassHierarchy();
		
		predMap = new HashMap<CGNode, Set<CGNode>>();
		succMap = new HashMap<CGNode, Set<CGNode>>();
		possibleTargetMap = new HashMap<Pair<CGNode, CallSiteReference>, Set<CGNode>>();
		possibleSiteMap = new HashMap<Pair<CGNode, CGNode>, Set<CallSiteReference>>();
		
		TypeReference activityTR = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/app/Activity");
		TypeReference wvTR = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/webkit/WebView");
		TypeReference contextTR = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/content/Context");
		TypeReference handlerTR = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Handler");
	
		activityClass = cha.lookupClass(activityTR);
		webViewClass = cha.lookupClass(wvTR);
		contextClass = cha.lookupClass(contextTR);
		handlerClass = cha.lookupClass(handlerTR);
		MAX_NODE_NUM = delegate.getMaxNumber();
		irFactory = new HybridIRFactory();
		mNodes = new HashMap<Integer, CGNode>();
		
		init_model();
		init_unreachable();

//		modelUnreachableNodes();
	}
	
	private SSANewInstruction findNew(IR ir, int def){
		for(Iterator<NewSiteReference> inew = ir.iterateNewSites(); inew.hasNext(); ){
			NewSiteReference nsr = inew.next();
			SSANewInstruction newInst = ir.getNew(nsr);
			if(newInst.getDef() == def)
				return newInst;
		}
		return null;
	}
	
	private boolean init_handler(){
		Selector hs = Selector.make("post(Ljava/lang/Runnable;)Z");
		TypeReference runnableTR = TypeReference.find(ClassLoaderReference.Application, "Ljava/lang/Runnable");
		IClass runnableClass = cha.lookupClass(runnableTR);
		Set<Pair<Integer, CGNode>> createNodes = new HashSet<Pair<Integer, CGNode>>();
		
		for(CGNode n : this){
			if(!n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application) && isAndroidLibrary(n))
				continue;
			IR ir = n.getIR();
			if(ir != null){
				for(Iterator<CallSiteReference> icsr = ir.iterateCallSites(); icsr.hasNext(); ){
					CallSiteReference csr = icsr.next();
					MethodReference targetMR = csr.getDeclaredTarget();
					TypeReference targetClassTR = targetMR.getDeclaringClass();
					IClass targetClass = cha.lookupClass(targetClassTR);

					
					if(targetClass != null && (targetClass.equals(handlerClass) && targetMR.getSelector().equals(hs) && targetMR.getNumberOfParameters() > 0)){
						TypeReference pTR =  targetMR.getParameterType(0);
						IClass pClass = cha.lookupClass(pTR);
						
						for(SSAAbstractInvokeInstruction callInst : ir.getCalls(csr)){
							int use = callInst.getUse(1);
							
							SSANewInstruction newInst = findNew(ir, use);
							if(newInst != null){
								TypeReference tr = newInst.getNewSite().getDeclaredType();
								IClass klass = cha.lookupClass(tr);
								
								if(cha.implementsInterface(klass, runnableClass)){
									MethodReference mr = MethodReference.findOrCreate(tr, Selector.make("run()V"));
									IMethod m = cha.resolveMethod(mr);
									if(m != null){
										CGNode run = new UnreachableCGNode(m);

										updatePredMap(run, n);
										updateSuccMap(n, run);
										updatePossibleTargetMap(Pair.make(n, csr), run);
										updatePossibleSiteMap(Pair.make(n, run), csr);
										createNodes.add(Pair.make(MAX_NODE_NUM, run));
									}
								}
							}
						}
					}
				}
			}
		}
		
		for(Pair<Integer, CGNode> p : createNodes){
			mNodes.put(p.fst, p.snd);
		}
		
//		System.exit(-1);
		return !createNodes.isEmpty();
	}
	
	private boolean isApplicationCall(CallSiteReference csr){
		MethodReference targetMethodReference = csr.getDeclaredTarget();
		TypeReference targetClassReference = targetMethodReference.getDeclaringClass();
		
		IClass targetClass = cha.lookupClass(targetClassReference);
		
		if(targetClass != null && targetClass.getClassLoader().getReference().equals(ClassLoaderReference.Application))
			return true;
		else if(targetClass != null && (targetClass.equals(handlerClass) || cha.isSubclassOf(targetClass, handlerClass))) // except for handler
			return true;
		return false;
	}
	
	private boolean isRecursion(CGNode n, CallSiteReference csr){
		//recursion problems
		MethodReference mr = n.getMethod().getReference();
		IClass klass = n.getMethod().getDeclaringClass();
		
		MethodReference targetMethodReference = csr.getDeclaredTarget();
		TypeReference targetClassReference = targetMethodReference.getDeclaringClass();
		
		IClass targetClass = cha.lookupClass(targetClassReference);
		
		if(targetClass != null)
			if(targetClass.equals(klass) && targetMethodReference.equals(mr))
				return true;
		return false;
	}
	
	private boolean isAndroidLibrary(CallSiteReference csr){
		MethodReference targetMethodReference = csr.getDeclaredTarget();
		TypeReference targetClassReference = targetMethodReference.getDeclaringClass();
		IClass klass = cha.lookupClass(targetClassReference);
		if(klass != null && targetClassReference.getName().getPackage() != null)
			if(targetClassReference.getName().getPackage().toString().startsWith("android/support/") || targetClassReference.getName().getPackage().toString().startsWith("com/google/"))
				return true;
		return false;
	}
	
	private boolean isAndroidLibrary(CGNode csr){
		MethodReference targetMethodReference = csr.getMethod().getReference();
		TypeReference targetClassReference = targetMethodReference.getDeclaringClass();
		
		IClass klass = cha.lookupClass(targetClassReference);
		if(klass != null && targetClassReference.getName().getPackage() != null)
			if(targetClassReference.getName().getPackage().toString().startsWith("android/support/") || targetClassReference.getName().getPackage().toString().startsWith("com/google/"))
				return true;
		
		return false;
	}
	
	private boolean modelUnreachableNodes(){
		this.iterator();
		Set<Pair<Integer, CGNode>> createNodes = new HashSet<Pair<Integer, CGNode>>();
		
		for(CGNode n : this){
			if(!n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application))
				continue;
			IR ir = n.getIR();
			if(ir != null){
				for(Iterator<CallSiteReference> icsr = ir.iterateCallSites(); icsr.hasNext(); ){
					CallSiteReference csr = icsr.next();

					if(this.getPossibleTargets(n, csr).size() == 0){
						if(isApplicationCall(csr)){
							if(isRecursion(n, csr)){
								updatePredMap(n, n);
								updateSuccMap(n, n);
								updatePossibleTargetMap(Pair.make(n, csr), n);
								updatePossibleSiteMap(Pair.make(n, n), csr);
							}else if(!isAndroidLibrary(csr)){ // we do not expand Android libraries used for backward compatibility.
								IMethod m = cha.resolveMethod(csr.getDeclaredTarget());
								if(m != null){
									CGNode target = new UnreachableCGNode(m);
									updatePredMap(target, n);
									updateSuccMap(n, target);
									updatePossibleTargetMap(Pair.make(n, csr), target);
									updatePossibleSiteMap(Pair.make(n, target), csr);
									createNodes.add(Pair.make(MAX_NODE_NUM, target));
								}
							}
						} 
					}
				}
			}
		}
		
		for(Pair<Integer, CGNode> p : createNodes){
			mNodes.put(p.fst, p.snd);
		}
		
		return !createNodes.isEmpty();
	}
	
	private void init_unreachable(){
			while(modelUnreachableNodes() == true);
			init_handler();
			while(modelUnreachableNodes() == true);
			modelOfJavaToJsBackEdges();
	}
	
	public Set<CGNode> getTargetsOfFakeEdges(){
		return predMap.keySet();
	}
	
	private void updatePredMap(CGNode src, CGNode pred){
		if(!predMap.containsKey(src))
			predMap.put(src, new HashSet<CGNode>());
		predMap.get(src).add(pred);
	}
	
	private void updateSuccMap(CGNode src, CGNode succ){
		if(!succMap.containsKey(src))
			succMap.put(src, new HashSet<CGNode>());
		succMap.get(src).add(succ);
	}
	
	private void updatePossibleTargetMap(Pair<CGNode, CallSiteReference> p, CGNode target){
		if(!possibleTargetMap.containsKey(p))
			possibleTargetMap.put(p, new HashSet<CGNode>());
		possibleTargetMap.get(p).add(target);
	}
	
	private void updatePossibleSiteMap(Pair<CGNode, CGNode> p, CallSiteReference site){
		if(!possibleSiteMap.containsKey(p))
			possibleSiteMap.put(p, new HashSet<CallSiteReference>());
		possibleSiteMap.get(p).add(site);
	}
	
	private void init_model(){
		modelOfStartActivityToOnCreate();
		modelOfStartActivityForResultToOnActivityResult();
		modelOfRequestPermissionToOnRequestPermissionsResult();
	}
	
	private void modelOfStartActivityToOnCreate(){
		Set<CGNode> onCreateNodes = findOnCreateNodes();
		Set<Pair<CGNode, CallSiteReference>> startActivityNodesAndSites = findStartActivityNodesAndSites();
		Set<Pair<CGNode, CallSiteReference>> startActivityForResultNodesAndSites = findStartActivityForResultNodesAndSites();
		
		for(Pair<CGNode, CallSiteReference> p : startActivityNodesAndSites){
			CGNode caller = p.fst;
			CallSiteReference site = p.snd;
			
			for(CGNode target : onCreateNodes){
				updatePredMap(target, caller);
				updateSuccMap(caller, target);
				updatePossibleTargetMap(p, target);
				updatePossibleSiteMap(Pair.make(caller, target), site);
			}
		}
		
		for(Pair<CGNode, CallSiteReference> p : startActivityForResultNodesAndSites){
			CGNode caller = p.fst;
			CallSiteReference site = p.snd;
			
			for(CGNode target : onCreateNodes){
				updatePredMap(target, caller);
				updateSuccMap(caller, target);
				updatePossibleTargetMap(p, target);
				updatePossibleSiteMap(Pair.make(caller, target), site);
			}
		}
	}
	
	private Set<CGNode> findOnCreateNodesCache = null;
	
	private Set<CGNode> findOnCreateNodes(){
		if(findOnCreateNodesCache != null)
			return findOnCreateNodesCache;
		
		Set<CGNode> res = new HashSet<CGNode>();
		Selector oncreateSelector = Selector.make("onCreate(Landroid/os/Bundle;)V");
		
		for(CGNode n : this){
			if(isAndroidLibrary(n))
				continue;
			IClass klass = n.getMethod().getDeclaringClass();
			Selector selector = n.getMethod().getSelector();
			if(klass != null && (klass.equals(activityClass) || cha.isSubclassOf(klass, activityClass)) && selector.equals(oncreateSelector) && ((MODEL_APPLICATION_ONLY)? isJavaNode(n) : true))
				res.add(n);
		}
		
		findOnCreateNodesCache = res;
		return res;
	}
	
	private Set<Pair<CGNode, CallSiteReference>> findStartActivityNodesAndSitesCache = null;
	
	private Set<Pair<CGNode, CallSiteReference>> findStartActivityNodesAndSites(){
		if(findStartActivityNodesAndSitesCache != null)
			return findStartActivityNodesAndSitesCache;
		
		Set<Pair<CGNode, CallSiteReference>> res = new HashSet<Pair<CGNode, CallSiteReference>>();
		Selector startActivitySelector = Selector.make("startActivity(Landroid/content/Intent;)V");
		
		for(CGNode n : this){
			if(isAndroidLibrary(n))
				continue;
			
			IClass klass = n.getMethod().getDeclaringClass();
			
			if(klass != null && ((MODEL_APPLICATION_ONLY)? isJavaNode(n) : true)){
				if(n.getIR() != null)
				for(Iterator<CallSiteReference> icsr = n.getIR().iterateCallSites(); icsr.hasNext(); ){
					CallSiteReference csr = icsr.next();
					MethodReference targetMR = csr.getDeclaredTarget();
					TypeReference targetClassTR = targetMR.getDeclaringClass();
					IClass targetClass = cha.lookupClass(targetClassTR);
					
					if(targetClass != null && (targetClass.equals(contextClass) || cha.isSubclassOf(targetClass, contextClass)) && targetMR.getSelector().equals(startActivitySelector)){
						res.add(Pair.make(n, csr));
					}
				}
			}
		}
		
		findStartActivityNodesAndSitesCache = res;
		return res;
	}
	
	private void modelOfStartActivityForResultToOnActivityResult(){
		Set<Pair<CGNode, CallSiteReference>> startActivityForResultNodesAndSites = findStartActivityForResultNodesAndSites();
		Set<CGNode> onActivityResultNodes = findOnActivityResultNodes();
			
		for(Pair<CGNode, CallSiteReference> p : startActivityForResultNodesAndSites){
			CGNode caller = p.fst;
			CallSiteReference site = p.snd;
			
			for(CGNode target : onActivityResultNodes){
				if(caller.getMethod().getDeclaringClass().equals(target.getMethod().getDeclaringClass())){
					updatePredMap(target, caller);
					updateSuccMap(caller, target);
					updatePossibleTargetMap(p, target);
					updatePossibleSiteMap(Pair.make(caller, target), site);
				}
			}
		}
	}
	
	private Set<Pair<CGNode, CallSiteReference>> findStartActivityForResultNodesAndSitesCache = null;
	private Set<Pair<CGNode, CallSiteReference>> findStartActivityForResultNodesAndSites(){
		if(findStartActivityForResultNodesAndSitesCache != null)
			return findStartActivityForResultNodesAndSitesCache;
		
		Set<Pair<CGNode, CallSiteReference>> res = new HashSet<Pair<CGNode, CallSiteReference>>();
		Selector startActivityForResultSelector = Selector.make("startActivityForResult(Landroid/content/Intent;I)V");
		
		for(CGNode n : this){
			if(isAndroidLibrary(n))
				continue;
			
			IClass klass = n.getMethod().getDeclaringClass();
			
			if(klass != null && ((MODEL_APPLICATION_ONLY)? isJavaNode(n) : true)){
				if(n.getIR() != null)
				for(Iterator<CallSiteReference> icsr = n.getIR().iterateCallSites(); icsr.hasNext(); ){
					CallSiteReference csr = icsr.next();
					MethodReference targetMR = csr.getDeclaredTarget();
					TypeReference targetClassTR = targetMR.getDeclaringClass();
					IClass targetClass = cha.lookupClass(targetClassTR);
					
					if(targetClass != null && (targetClass.equals(contextClass) || cha.isSubclassOf(targetClass, contextClass)) && targetMR.getSelector().equals(startActivityForResultSelector)){
						res.add(Pair.make(n, csr));
					}
				}
			}
		}
		findStartActivityForResultNodesAndSitesCache = res;
		return res;
	}
	
	private Set<CGNode> findOnActivityResultNodesCache = null;
	private Set<CGNode> findOnActivityResultNodes(){
		if(findOnActivityResultNodesCache != null)
			return findOnActivityResultNodesCache;
		
		Set<CGNode> res = new HashSet<CGNode>();
		Selector oncreateSelector = Selector.make("onActivityResult(IILandroid/content/Intent;)V");
		
		for(CGNode n : this){
			if(isAndroidLibrary(n) && ((MODEL_APPLICATION_ONLY)? !isJavaNode(n) : !true))
				continue;
			
			IClass klass = n.getMethod().getDeclaringClass();
			Selector selector = n.getMethod().getSelector();
			
			if(klass != null && (klass.equals(activityClass) || cha.isSubclassOf(klass, activityClass)) && selector.equals(oncreateSelector) && ((MODEL_APPLICATION_ONLY)? isJavaNode(n) : true))
				res.add(n);
		}
		
		findOnActivityResultNodesCache = res;
		return res;
	}
	
	private void modelOfRequestPermissionToOnRequestPermissionsResult(){
		Set<Pair<CGNode, CallSiteReference>> requestPermissionNodesAndSites = findRequestPermissionNodesAndSites();
		Set<CGNode> onRequestPermissionResultNodes = findOnRequestPermissionResultNodes();
		
		for(Pair<CGNode, CallSiteReference> p : requestPermissionNodesAndSites){
			CGNode caller = p.fst;
			CallSiteReference site = p.snd;
			for(CGNode target : onRequestPermissionResultNodes){
				if(caller.getMethod().getDeclaringClass().equals(target.getMethod().getDeclaringClass())){
					updatePredMap(target, caller);
					updateSuccMap(caller, target);
					updatePossibleTargetMap(p, target);
					updatePossibleSiteMap(Pair.make(caller, target), site);
				}
			}
		}
	}
	
	private Set<Pair<CGNode, CallSiteReference>> findRequestPermissionNodesAndSitesCache = null;
	private Set<Pair<CGNode, CallSiteReference>> findRequestPermissionNodesAndSites(){
		if(findRequestPermissionNodesAndSitesCache != null)
			return findRequestPermissionNodesAndSitesCache;
		
		Set<Pair<CGNode, CallSiteReference>> res = new HashSet<Pair<CGNode, CallSiteReference>>();
		Selector startActivityForResultSelector = Selector.make("requestPermissions([Ljava/lang/String;I)V");
		
		for(CGNode n : this){
			if(isAndroidLibrary(n))
				continue;
			
			IClass klass = n.getMethod().getDeclaringClass();
			
			if(klass != null && ((MODEL_APPLICATION_ONLY)? isJavaNode(n) : true)){
				if(n.getIR() != null)
				for(Iterator<CallSiteReference> icsr = n.getIR().iterateCallSites(); icsr.hasNext(); ){
					CallSiteReference csr = icsr.next();
					MethodReference targetMR = csr.getDeclaredTarget();
					TypeReference targetClassTR = targetMR.getDeclaringClass();
					IClass targetClass = cha.lookupClass(targetClassTR);
					
					if(targetClass != null && (targetClass.equals(contextClass) || cha.isSubclassOf(targetClass, contextClass)) && targetMR.getSelector().equals(startActivityForResultSelector)){
						res.add(Pair.make(n, csr));
					}
				}
			}
		}
		findRequestPermissionNodesAndSitesCache = res;
		return res;
	}
	
	private Set<CGNode> findOnRequestPermissionResultNodesCache = null;
	private Set<CGNode> findOnRequestPermissionResultNodes(){
		if(findOnRequestPermissionResultNodesCache != null)
			return findOnRequestPermissionResultNodesCache;
		
		Set<CGNode> res = new HashSet<CGNode>();
		Selector oncreateSelector = Selector.make("onRequestPermissionsResult(I[Ljava/lang/String;[I)V");
		
		for(CGNode n : this){
			if(isAndroidLibrary(n))
				continue;
			IClass klass = n.getMethod().getDeclaringClass();
			Selector selector = n.getMethod().getSelector();
			
			if(klass != null && (klass.equals(activityClass) || cha.isSubclassOf(klass, activityClass)) && selector.equals(oncreateSelector) && ((MODEL_APPLICATION_ONLY)? isJavaNode(n) : true))
				res.add(n);
		}
		
		findOnRequestPermissionResultNodesCache = res;
		return res;
	}
	
	private void modelOfJavaToJsBackEdges(){
		Set<Pair<CGNode, CallSiteReference>> loadUrlNodesAndSites = findLoadUrlNodesAndSites();
		Set<CGNode> connectedNodesFromJavaScript = findAllConnectedNodesFromJavaScript();
		Set<CGNode> javaScriptNodes = findAllJavaScriptNodes();
		
		for(Pair<CGNode, CallSiteReference> p : loadUrlNodesAndSites){
			CGNode caller = p.fst;
			CallSiteReference site = p.snd;
			if(connectedNodesFromJavaScript.contains(caller)){
				for(CGNode target : javaScriptNodes){
					updatePredMap(target, caller);
					updateSuccMap(caller, target);
					updatePossibleTargetMap(p, target);
					updatePossibleSiteMap(Pair.make(caller, target), site);
				}
			}
		}
	}
	
	private Set<Pair<CGNode, CallSiteReference>> findLoadUrlNodesAndSitesCache = null;
	private Set<Pair<CGNode, CallSiteReference>> findLoadUrlNodesAndSites(){
		if(findLoadUrlNodesAndSitesCache != null)
			return findLoadUrlNodesAndSitesCache;
		
		Set<Pair<CGNode, CallSiteReference>> res = new HashSet<Pair<CGNode, CallSiteReference>>();
		Selector startActivityForResultSelector = Selector.make("loadUrl(Ljava/lang/String;)V");
		
		for(CGNode n : this){			
			if(n.getIR() != null)
			for(Iterator<CallSiteReference> icsr = n.getIR().iterateCallSites(); icsr.hasNext(); ){
				CallSiteReference csr = icsr.next();
				MethodReference targetMR = csr.getDeclaredTarget();
				TypeReference targetClassTR = targetMR.getDeclaringClass();
				IClass targetClass = cha.lookupClass(targetClassTR);
				
				if(targetClass != null && (targetClass.equals(webViewClass) || cha.isSubclassOf(targetClass, webViewClass)) && targetMR.getSelector().equals(startActivityForResultSelector) && ((MODEL_APPLICATION_ONLY)? isJavaNode(n) : true)){
					res.add(Pair.make(n, csr));
				}
			}
		}
		findLoadUrlNodesAndSitesCache = res;
		return res;
	}
	
	private Set<CGNode> findAllConnectedNodesFromJavaScriptCache = null;
	private Set<CGNode> findAllConnectedNodesFromJavaScript(){
		if(findAllConnectedNodesFromJavaScriptCache != null)
			return findOnRequestPermissionResultNodesCache;
		
		Set<CGNode> res = new HashSet<CGNode>();
		
		Queue<CGNode> succQueue = new LinkedBlockingQueue<CGNode>();
		Set<CGNode> visited = new HashSet<CGNode>();
		
		//find directly accessible Android Java methods from JavaScript 
		for(CGNode n : this){
			if(isJSNode(n)){
				for(Iterator<CGNode> isucc = this.getSuccNodes(n); isucc.hasNext(); ){
					CGNode succ = isucc.next();
					if(isAnyJavaNode(succ)){
						visited.add(succ);
						succQueue.add(succ);
					}
				}
			}
		}

		while(!succQueue.isEmpty()){
			CGNode n = succQueue.poll();
			
			if(isJavaNode(n))
				res.add(n);
			
			for(Iterator<CGNode> isucc = this.getSuccNodes(n); isucc.hasNext(); ){
				CGNode succ = isucc.next();
				if(isAnyJavaNode(succ) && !visited.contains(succ)){
					visited.add(succ);
					succQueue.add(succ);
				}
			}
		}
		
		findAllConnectedNodesFromJavaScriptCache = res;
		return res;
	}
	
	private Set<CGNode> findAllJavaScriptNodesCache = null;
	private Set<CGNode> findAllJavaScriptNodes(){
		if(findAllJavaScriptNodesCache != null)
			return findAllJavaScriptNodesCache;
		
		Set<CGNode> res = new HashSet<CGNode>();
		
		Queue<CGNode> succQueue = new LinkedBlockingQueue<CGNode>();
		
		//find directly accessible Android Java methods from JavaScript 
		for(CGNode n : this){
			if(isJSNode(n) && !n.getMethod().isInit() && !n.getMethod().isClinit() && !n.toString().contains("<ctor for"))
				res.add(n);
			
		}
		findAllJavaScriptNodesCache = res;
		return res;
	}
	
	private boolean isJSNode(CGNode n){
		return n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(JavaScriptTypes.jsLoader);
	}
	
	private boolean isJavaNode(CGNode n){
		return n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application);
	}

	private boolean isAnyJavaNode(CGNode n){
		return n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application) || n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial)  || n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Extension);
	}
	
	private Collection<CGNode> getModeledNodes(){
		return mNodes.values();
	}
	
	@Override
	public void removeNodeAndEdges(CGNode n) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		delegate.removeNodeAndEdges(n);
	}

	@Override
	public Iterator<CGNode> iterator() {
		// TODO Auto-generated method stub
		final Iterator<CGNode> iterm = getModeledNodes().iterator();
		final Iterator<CGNode> iterd = delegate.iterator();
		return new Iterator<CGNode>(){

			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return iterd.hasNext() || iterm.hasNext();
			}

			@Override
			public CGNode next() {
				// TODO Auto-generated method stub
				if(iterd.hasNext())
					return iterd.next();
				else
					return iterm.next();
			}
		};
//		return delegate.iterator();
	}

	@Override
	public int getNumberOfNodes() {
		// TODO Auto-generated method stub
		return delegate.getNumberOfNodes() + mNodes.size();
	}

	@Override
	public void addNode(CGNode n) {
		// TODO Auto-generated method stub
		delegate.addNode(n);
	}

	@Override
	public void removeNode(CGNode n) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		delegate.removeNode(n);
	}

	@Override
	public boolean containsNode(CGNode n) {
		// TODO Auto-generated method stub
		return delegate.containsNode(n) || mNodes.values().contains(n);
	}

	@Override
	public Iterator<CGNode> getPredNodes(CGNode n) {
		// TODO Auto-generated method stub
		Set<CGNode> res = new HashSet<CGNode>();
		for(Iterator<CGNode> ipred = delegate.getPredNodes(n); ipred.hasNext();){
			res.add(ipred.next());
		}
		
		if(predMap.containsKey(n))
			res.addAll(predMap.get(n));
		
		return res.iterator();
	}

	@Override
	public int getPredNodeCount(CGNode n) {
		// TODO Auto-generated method stub
		int num = delegate.getPredNodeCount(n);
		
		if(predMap.containsKey(n))
			num += predMap.get(n).size();
		
		return num;
	}

	@Override
	public Iterator<CGNode> getSuccNodes(CGNode n) {
		// TODO Auto-generated method stub
		Set<CGNode> res = new HashSet<CGNode>();
		for(Iterator<CGNode> isucc = delegate.getSuccNodes(n); isucc.hasNext();){
			res.add(isucc.next());
		}
		
		if(succMap.containsKey(n))
			res.addAll(succMap.get(n));
		
		return res.iterator();
	}

	@Override
	public int getSuccNodeCount(CGNode n) {
		// TODO Auto-generated method stub
		int num = delegate.getSuccNodeCount(n);
		
		if(succMap.containsKey(n))
			num += succMap.get(n).size();
		
		return num;
	}

	@Override
	public void addEdge(CGNode src, CGNode dst) {
		// TODO Auto-generated method stub
		delegate.addEdge(src, dst);
	}

	@Override
	public void removeEdge(CGNode src, CGNode dst) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		delegate.removeEdge(src, dst);
	}

	@Override
	public void removeAllIncidentEdges(CGNode node) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		delegate.removeAllIncidentEdges(node);
	}

	@Override
	public void removeIncomingEdges(CGNode node) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		delegate.removeIncomingEdges(node);
	}

	@Override
	public void removeOutgoingEdges(CGNode node) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		delegate.removeOutgoingEdges(node);
	}

	@Override
	public boolean hasEdge(CGNode src, CGNode dst) {
		// TODO Auto-generated method stub
		return delegate.hasEdge(src, dst) || ((predMap.containsKey(dst))? predMap.get(dst).contains(src) : false);
	}

	@Override
	public int getNumber(CGNode N) {
		// TODO Auto-generated method stub
		if(N instanceof UnreachableCGNode)
			return ((UnreachableCGNode)N).number; 
		return delegate.getNumber(N);
	}

	@Override
	public CGNode getNode(int number) {
		// TODO Auto-generated method stub
		if(mNodes.containsKey(number))
			return mNodes.get(number);
		return delegate.getNode(number);
	}

	@Override
	public int getMaxNumber() {
		// TODO Auto-generated method stub
		return delegate.getMaxNumber() + this.MAX_NODE_NUM;
	}

	@Override
	public Iterator<CGNode> iterateNodes(IntSet s) {
		// TODO Auto-generated method stub

		Set<CGNode> nodes = new HashSet<CGNode>();
		
		for(IntIterator ii = s.intIterator() ; ii.hasNext(); ){
			int i = ii.next();
			if(mNodes.containsKey(i))
				nodes.add(mNodes.get(i));
			else{
				CGNode n = delegate.getNode(i);
				if(n != null)
					nodes.add(n);
			}
		}
		
		return nodes.iterator();
//		return delegate.iterateNodes(s);
	}

	@Override
	public IntSet getSuccNodeNumbers(CGNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntSet getPredNodeNumbers(CGNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CGNode getFakeRootNode() {
		// TODO Auto-generated method stub
		return delegate.getFakeRootNode();
	}

	@Override
	public Collection<CGNode> getEntrypointNodes() {
		// TODO Auto-generated method stub
		return delegate.getEntrypointNodes();
	}

	@Override
	public CGNode getNode(IMethod method, Context C) {
		// TODO Auto-generated method stub
		return delegate.getNode(method, C);
	}

	@Override
	public Set<CGNode> getNodes(MethodReference m) {
		// TODO Auto-generated method stub
		return delegate.getNodes(m);
	}

	@Override
	public IClassHierarchy getClassHierarchy() {
		// TODO Auto-generated method stub
		return delegate.getClassHierarchy();
	}

	@Override
	public Set<CGNode> getPossibleTargets(CGNode node, CallSiteReference site) {
		// TODO Auto-generated method stub
		Set<CGNode> res = new HashSet<CGNode>();
		if(delegate.containsNode(node))
			res.addAll(delegate.getPossibleTargets(node, site));
		
		Pair<CGNode, CallSiteReference> p = Pair.make(node, site);
		if(possibleTargetMap.containsKey(p))
			res.addAll(possibleTargetMap.get(p));
		
		return res;
	}

	@Override
	public int getNumberOfTargets(CGNode node, CallSiteReference site) {
		// TODO Auto-generated method stub
		int num = 0;
		
		if(delegate.containsNode(node))
			num += delegate.getNumberOfTargets(node, site);
		
		Pair<CGNode, CallSiteReference> p = Pair.make(node, site);
		if(possibleTargetMap.containsKey(p))
			num += possibleTargetMap.get(p).size();
		
		return num;
	}

	@Override
	public Iterator<CallSiteReference> getPossibleSites(CGNode src, CGNode target) {
		// TODO Auto-generated method stub
		Set<CallSiteReference> res = new HashSet<CallSiteReference>();
		
		if(delegate.containsNode(src) && delegate.containsNode(target))
			for(Iterator<CallSiteReference> icsr = delegate.getPossibleSites(src, target); icsr.hasNext(); ){
				res.add(icsr.next());
			}
		
		Pair<CGNode, CGNode> p = Pair.make(src, target);
		
		if(possibleSiteMap.containsKey(p))
			res.addAll(possibleSiteMap.get(p));
		
		return res.iterator();
	}

	
	class UnreachableCGNode extends ExplicitNode{
		
		private int number = ++MAX_NODE_NUM;
		private IR ir;
		private IMethod m;
		
		public UnreachableCGNode(IMethod m){
			((ExplicitCallGraph)delegate).super(m, Everywhere.EVERYWHERE);
			
			this.m = m;
			try{
				ir = irFactory.makeIR(m, Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
			}catch(Exception e){
				ir = null;
			}
		}
		
		@Override
		public int getGraphNodeId() {
			// TODO Auto-generated method stub
			return number;
		}

		@Override
		public void setGraphNodeId(int number) {
			// TODO Auto-generated method stub
			Assertions.UNREACHABLE();
		}

		@Override
		public IClassHierarchy getClassHierarchy() {
			// TODO Auto-generated method stub
			return cha;
		}

		@Override
		public IMethod getMethod() {
			// TODO Auto-generated method stub
			return m;
		}

		@Override
		public Context getContext() {
			// TODO Auto-generated method stub
			return Everywhere.EVERYWHERE;
		}

		@Override
		public boolean addTarget(CallSiteReference site, CGNode target) {
			// TODO Auto-generated method stub
			Assertions.UNREACHABLE();
			return false;
		}

		@Override
		public IR getIR() {
			// TODO Auto-generated method stub
			return ir;
		}

		@Override
		public DefUse getDU() {
			// TODO Auto-generated method stub
			return new DefUse(ir);
		}

		@Override
		public Iterator<NewSiteReference> iterateNewSites() {
			// TODO Auto-generated method stub
			return ir.iterateNewSites();
		}

		@Override
		public Iterator<CallSiteReference> iterateCallSites() {
			// TODO Auto-generated method stub
			return ir.iterateCallSites();
		}
		
		@Override
		public String toString(){
			return m + ", " + Everywhere.EVERYWHERE;
		}
	}
}
