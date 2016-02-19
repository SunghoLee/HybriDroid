package kr.ac.kaist.hybridroid.callgraph;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.cast.ipa.callgraph.AstSSAPropagationCallGraphBuilder;
import com.ibm.wala.cast.ipa.callgraph.AstSSAPropagationCallGraphBuilder.AstPointerAnalysisImpl;
import com.ibm.wala.cast.ipa.callgraph.AstSSAPropagationCallGraphBuilder.AstPointerAnalysisImpl.AstImplicitPointsToSetVisitor;
import com.ibm.wala.cast.ipa.callgraph.CrossLanguageSSAPropagationCallGraphBuilder;
import com.ibm.wala.cast.ipa.callgraph.GlobalObjectKey;
import com.ibm.wala.cast.ipa.callgraph.ReflectedFieldPointerKey;
import com.ibm.wala.cast.ir.ssa.AstGlobalRead;
import com.ibm.wala.cast.ir.ssa.AstGlobalWrite;
import com.ibm.wala.cast.js.ipa.callgraph.JSSSAPropagationCallGraphBuilder;
import com.ibm.wala.cast.js.ipa.callgraph.JSSSAPropagationCallGraphBuilder.JSConstraintVisitor;
import com.ibm.wala.cast.js.ipa.callgraph.JSSSAPropagationCallGraphBuilder.JSPointerAnalysisImpl;
import com.ibm.wala.cast.js.ipa.callgraph.JSSSAPropagationCallGraphBuilder.JSPointerAnalysisImpl.JSImplicitPointsToSetVisitor;
import com.ibm.wala.cast.js.loader.JavaScriptLoader;
import com.ibm.wala.cast.js.ssa.JavaScriptCheckReference;
import com.ibm.wala.cast.js.ssa.JavaScriptInstanceOf;
import com.ibm.wala.cast.js.ssa.JavaScriptInvoke;
import com.ibm.wala.cast.js.ssa.JavaScriptPropertyRead;
import com.ibm.wala.cast.js.ssa.JavaScriptPropertyWrite;
import com.ibm.wala.cast.js.ssa.JavaScriptTypeOfInstruction;
import com.ibm.wala.cast.js.ssa.JavaScriptWithRegion;
import com.ibm.wala.cast.js.ssa.PrototypeLookup;
import com.ibm.wala.cast.js.ssa.SetPrototype;
import com.ibm.wala.cast.js.types.JavaScriptTypes;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.util.TargetLanguageSelector;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.ContextKey;
import com.ibm.wala.ipa.callgraph.propagation.AbstractFieldPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSite;
import com.ibm.wala.ipa.callgraph.propagation.ConcreteTypeKey;
import com.ibm.wala.ipa.callgraph.propagation.FilteredPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKeyFactory;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKeyFactory;
import com.ibm.wala.ipa.callgraph.propagation.PointsToMap;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.PropagationSystem;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.MutableMapping;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.OrdinalSet;
import com.ibm.wala.util.strings.Atom;

import kr.ac.kaist.hybridroid.analysis.string.AndroidStringAnalysis;
import kr.ac.kaist.hybridroid.checker.HybridAPIMisusesChecker;
import kr.ac.kaist.hybridroid.checker.HybridAPIMisusesChecker.Warning;
import kr.ac.kaist.hybridroid.models.AndroidHybridAppModel;
import kr.ac.kaist.hybridroid.pointer.InterfaceClass;
import kr.ac.kaist.hybridroid.pointer.JSCompatibleClassFilter;
import kr.ac.kaist.hybridroid.pointer.JavaCompatibleClassFilter;
import kr.ac.kaist.hybridroid.pointer.MockupInstanceKey;
import kr.ac.kaist.hybridroid.types.AndroidJavaJavaScriptTypeMap;

/**
 * Specialized pointer analysis constraint generation for Hybrid Android
 * applications. Now, this support only 'addJavascriptInterface' Android Java
 * method among several communication methods between Android Java and
 * JavaScript.
 * 
 * We need to support some callback communication methods, but not now.
 */
public class AndroidHybridCallGraphBuilder extends
		JavaJavaScriptHybridCallGraphBuilder {

	private static final Atom webviewClassName = Atom.findOrCreateAsciiAtom("WebView");
	private static final Atom addInterfaceMethodName = Atom
			.findOrCreateAsciiAtom("addJavascriptInterface");
	private static final Atom interfAnnotationName = Atom
			.findOrCreateAsciiAtom("JavascriptInterface");
	private HybridAPIMisusesChecker typeChecker;
	private AndroidStringAnalysis asa;
	
	public AndroidHybridCallGraphBuilder(IClassHierarchy cha,
			AnalysisOptions options, AnalysisCache cache, HybridAPIMisusesChecker typeChecker, AndroidStringAnalysis asa) {
		super(cha, options, cache);

		if((options.getAnalysisScope() instanceof AndroidHybridAnalysisScope) == false)
			Assertions.UNREACHABLE("AndroidHybridCallGraphBuilder must receive AndroidHybridAnalysisScope as a scope. current: " + options.getAnalysisScope().getClass().getName());
		
		this.typeChecker = typeChecker;
		this.asa = asa;
		jsGlobalMap = new HashMap<Atom, GlobalObjectKey>();
		globalInit(((AndroidHybridAnalysisScope)options.getAnalysisScope()).getJavaScriptNames());
	}

	private void globalInit(Set<Atom> files){
		for(Atom file : files)
			jsGlobalMap.put(file, new GlobalObjectKey(cha.lookupClass(JavaScriptTypes.Root)));
	}
	
	private FilteredPointerKey getFilteredPointerKeyForInterfParams(CGNode caller, SSAAbstractInvokeInstruction inst, CGNode node, int valueNumber, IClass filter){
		if(AndroidJavaJavaScriptTypeMap.isJava2JsTypeCompatible(filter.getReference())){	
			return getFilteredPointerKeyForLocal(node, valueNumber, JSCompatibleClassFilter.make(caller, inst, node, valueNumber, typeChecker, filter));
		}else{
			// incompatible type with JS type
			return super.getFilteredPointerKeyForLocal(node, valueNumber, filter);
		}
	}
	
	private IClass getReceiverClass(IMethod method) {
	    TypeReference formalType = method.getParameterType(0);
	    IClass C = getClassHierarchy().lookupClass(formalType);
	    if (method.isStatic()) {
	      Assertions.UNREACHABLE("asked for receiver of static method " + method);
	    }
	    if (C == null) {
	      Assertions.UNREACHABLE("no class found for " + formalType + " recv of " + method);
	    }
	    return C;
	  }
	
	public PointerKey getTargetPointerKey(CGNode caller, SSAAbstractInvokeInstruction inst, CGNode target, int index) {
		int vn;
		if (target.getIR() != null) {
			vn = target.getIR().getSymbolTable().getParameter(index);
		} else {
			vn = index + 1;
		}

		FilteredPointerKey.TypeFilter filter = (FilteredPointerKey.TypeFilter) target
				.getContext().get(ContextKey.PARAMETERS[index]);
		if (filter != null && !filter.isRootFilter()) {
			return getFilteredPointerKeyForLocal(target, vn, filter);

		} else {
			// the context does not select a particular concrete type for the
			// receiver, so use the type of the method
			IClass C;
			if (index == 0 && !target.getMethod().isStatic()) {
				C = getReceiverClass(target.getMethod());
			} else {
				C = cha.lookupClass(target.getMethod().getParameterType(index));
			}

			if (C == null || C.getClassHierarchy().getRootClass().equals(C)) {
				return getPointerKeyForLocal(target, vn);
			} else {
				if (this.hasJavascriptInterfaceAnnotation(target.getMethod())
						&& vn <= target.getMethod().getNumberOfParameters())
					return getFilteredPointerKeyForInterfParams(caller, inst, target, vn, C);
				else
					return getFilteredPointerKeyForLocal(target, vn,
							new FilteredPointerKey.SingleClassFilter(C));
			}
		}
	}
	  
	private void processHybridCallingConstraints(CGNode caller, SSAAbstractInvokeInstruction instruction, CGNode target, InstanceKey[][] constParams, PointerKey uniqueCatchKey){

	    int paramCount = target.getMethod().getNumberOfParameters();
	    int argCount = instruction.getNumberOfParameters() - 1;
	    
	    // pass actual arguments to formals in the normal way
	    for (int i = 0; i < Math.min(paramCount, argCount); i++) {
	    	
	    	// check the numbers between Android Java parameters and JavaScript arguments.
	    	if(typeChecker != null)
	    		typeChecker.argNumCheck(caller, instruction, target.getMethod(), paramCount, argCount);
	    	
	      final PointerKey F = this.getTargetPointerKey(caller, instruction, target, i);
	      
	      if (constParams != null && constParams[i+1] != null) {
	        for (int j = 0; j < constParams[i+1].length; j++) {
	        	this.getSystem().newConstraint(F, constParams[i+1][j]);
	        }
	      } else { // do implicit type conversion between JavaScript and Android Java
	        PointerKey A = this.getPointerKeyForLocal(caller, instruction.getUse(i+1));
    		this.getSystem().newConstraint(F, (F instanceof FilteredPointerKey)? this.filterOperator : assignOperator, A);
	      }
	    }

	    // return values
	    if (instruction.getDef(0) != -1) {
	    	//return type checking
	    	if(typeChecker != null)
	    		typeChecker.returnTypeCheck(caller, instruction, target.getMethod());
	    	
	      PointerKey RF = this.getPointerKeyForReturnValue(target);
//	      PointerKey RA = this.getPointerKeyForLocal(caller, instruction.getDef(0));
	      PointerKey RA = this.getFilteredPointerKeyForLocal(caller, instruction.getDef(0), JavaCompatibleClassFilter.make(caller, instruction, target, typeChecker));
		  
//	      this.getSystem().newConstraint(RA, assignOperator, RF);
	      this.getSystem().newConstraint(RA, this.filterOperator, RF);
	    }

	    PointerKey EF = this.getPointerKeyForExceptionalReturnValue(target);
	    if (SHORT_CIRCUIT_SINGLE_USES && uniqueCatchKey != null) {
	      // e has exactly one use. so, represent e implicitly
	    	this.getSystem().newConstraint(uniqueCatchKey, assignOperator, EF);
	    } else {
	      PointerKey EA = this.getPointerKeyForLocal(caller, instruction.getDef(1));
	      this.getSystem().newConstraint(EA, assignOperator, EF);
	    }  
	}

	@Override
	protected void processCallingConstraints(CGNode caller,
			SSAAbstractInvokeInstruction instruction, CGNode target,
			InstanceKey[][] constParams, PointerKey uniqueCatchKey) {
		// TODO Auto-generated method stub
		if (JavaScriptLoader.JS.equals(caller.getMethod().getDeclaringClass()
				.getClassLoader().getLanguage())) {

			if (constParams != null && constParams[0] != null
					&& constParams[0][0] instanceof MockupInstanceKey) {
				
				processHybridCallingConstraints(caller, instruction, target,
						constParams, uniqueCatchKey);
			} else 
//				JSSSAPropagationCallGraphBuilder.processCallingConstraintsInternal(this, caller, instruction, target, constParams, uniqueCatchKey);
				super.processCallingConstraints(caller, instruction, target,
						constParams, uniqueCatchKey);
		} else {
			super.processCallingConstraints(caller, instruction, target,
					constParams, uniqueCatchKey);
		}
	}

	public class HybridJavaConstraintVisitor extends ConstraintVisitor {
		public HybridJavaConstraintVisitor(
				SSAPropagationCallGraphBuilder builder, CGNode node) {
			super(builder, node);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void visitInvoke(SSAInvokeInstruction instruction) {
			// TODO Auto-generated method stub
			CGNode caller = node;
			CallSiteReference site = instruction.getCallSite();
			Atom classname = instruction.getDeclaredTarget()
					.getDeclaringClass().getName().getClassName();
			Atom methodname = instruction.getDeclaredTarget().getName();
			PropagationSystem system = builder.getPropagationSystem();

			// checking invoked method is 'addJavascriptInterface' of WebView
			// class
			if (classname.equals(webviewClassName)
					&& methodname.equals(addInterfaceMethodName)) {
				IR ir = caller.getIR();
				SSAAbstractInvokeInstruction[] invokes = ir.getCalls(site);
				SymbolTable symTab = ir.getSymbolTable();

				// addJavascriptInterface(this, object, name);
				for (SSAAbstractInvokeInstruction invoke : invokes) {
					if (invoke.getNumberOfUses() == 3) {
						int objUse = invoke.getUse(1);
						int nameUse = invoke.getUse(2);

						// to find the object creation site. only support intra,
						// not inter-method.
						SSAInstruction defInst = caller.getDU().getDef(objUse);

						// only support constant name, not composition name.
						if (symTab.isConstant(nameUse)) {
							String name = (String) symTab
									.getConstantValue(nameUse);
							
							NewSiteReference newSite = getLocalCreationSite(ir.getInstructions(), caller.getDU(), objUse);
							
							if(newSite == null){
								try {
									throw new NotLocalCreationException(caller, invoke, objUse);
								} catch (NotLocalCreationException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
							TypeReference tr = newSite.getDeclaredType();
							
							IClass objClass = cha.lookupClass(tr);
							InterfaceClass wClass = wrappingClass(objClass);
							cha.addClass(wClass);
							
							Collection<IMethod> methods = wClass.getAllMethods();
							
							
							InstanceKey objKey = new AllocationSite(
									caller.getMethod(), newSite, wClass);
							AndroidHybridAppModel.addJSInterface(name, objKey);
							
//							System.err.println("#InterfaceName: " + name);
//							System.err.println("#InterfaceClass: "
//									+ wClass.getName().getClassName());

							/*
							 * make mock-up object for Android Java methods of
							 * the interface object. this process is needed,
							 * because a function is an object in JavaScript,
							 * but not in Java. this mock-up is used in
							 * MethodTargetSelector, to find the target Java
							 * method at a JavaScript call site.
							 */
							TypeReference jsString = TypeReference.find(
									JavaScriptTypes.jsLoader, "LString");
							IClass jsStringClass = cha.lookupClass(jsString);

							AbstractFieldPointerKey typePK = ReflectedFieldPointerKey
									.mapped(new ConcreteTypeKey(jsStringClass),
											objKey);
							
							for (IMethod method : methods) {
								if (hasJavascriptInterfaceAnnotation(method)) {
//									System.err.println("\t#method: " + method);
									wClass.addMethodAsField(method);
									String mname = method.getName().toString();
									IField f = wClass.getField(Atom.findOrCreateAsciiAtom(mname));
									
									PointerKey constantPK = builder.getPointerKeyForInstanceField(objKey, f);
//									System.err.println("\t#CK: " + constantPK);
									InstanceKey ik = makeMockupInstanceKey(method);
									
									system.findOrCreateIndexForInstanceKey(ik);
									
									system.newConstraint(constantPK, ik);
									system.newConstraint(typePK, ik);
								}
							}

							
							/*
							 * there could be multiple global objects, because a
							 * webview has a global ojbect repectively. the
							 * global objects are seperated by javascript file
							 * name.
							 */
//							AndroidStringAnalysis.SpotSpec spec = asa.getSpotSpec();
//							asa.get
//							node.getMethod().getReference().toString()
							
							/*
							 * attach the Android Java interface object to
							 * JavaScript global object. the attached object is
							 * used when finding the receiver at a JavaScript
							 * call site.
							 */
//							InstanceKey globalObj = ((AstSSAPropagationCallGraphBuilder) builder)
//									.getGlobalObject(JavaScriptTypes.jsName);
//							system.findOrCreateIndexForInstanceKey(globalObj);

							Collection<GlobalObjectKey> globalObjs = getGlobalObjects(JavaScriptTypes.jsName);
							
							FieldReference field = FieldReference.findOrCreate(
									JavaScriptTypes.jsLoader, "LRoot",
									"global " + name, "LRoot");
							String nonGlobalFieldName = field.getName()
									.toString().substring(7);
							field = FieldReference
									.findOrCreate(
											JavaScriptTypes.Root,
											Atom.findOrCreateUnicodeAtom(nonGlobalFieldName),
											JavaScriptTypes.Root);

							IField f = getClassHierarchy().resolveField(field);

//							PointerKey fieldPtr = builder
//									.getPointerKeyForInstanceField(globalObj, f);
//
//							system.newConstraint(fieldPtr, objKey);
							
							for(InstanceKey globalObj : globalObjs){
								PointerKey fieldPtr = builder
										.getPointerKeyForInstanceField(globalObj, f);
	
								system.newConstraint(fieldPtr, objKey);							
							}
						} else {
							// TODO: Support non-constant string value
							System.err.println("-------");
							System.err.println("# caller: " + node);
							System.err
									.println("# method: addJavascriptInterface");
							System.err.println("# name paramter: " + nameUse);
							System.err.println("\tis constant? "
									+ symTab.isConstant(nameUse));
							System.err.println("-------");
							Assertions
									.UNREACHABLE("now, only support constant value of 'addJavascriptInterface' name parameter.");
						}
					}
				}
			}
			super.visitInvoke(instruction);
		}
	}

	@Override
	protected TargetLanguageSelector<ConstraintVisitor, CGNode> makeMainVisitorSelector() {
		// TODO Auto-generated method stub
		return new TargetLanguageSelector<ConstraintVisitor, CGNode>() {
			@Override
			public ConstraintVisitor get(Atom language, CGNode construct) {
				if (JavaScriptTypes.jsName.equals(language)) {
					return new AndroidHybridJSConstraintVisitor(
							AndroidHybridCallGraphBuilder.this, construct);
				} else {
					return new HybridJavaConstraintVisitor(
							AndroidHybridCallGraphBuilder.this, construct);
				}
			}
		};
	}
	
	private NewSiteReference getLocalCreationSite(SSAInstruction[] insts, DefUse du, int objUse){
		SSAInstruction defInst = du.getDef(objUse);
		
		if(defInst instanceof SSANewInstruction){
			return ((SSANewInstruction)defInst).getNewSite();
		}else if(defInst instanceof SSAGetInstruction){
			SSAGetInstruction getInst = (SSAGetInstruction)defInst;
			FieldReference F = getInst.getDeclaredField();
			int objectF = getInst.getUse(0);
			for(int index = defInst.iindex-1; index > -1; index--){
				SSAInstruction targetInst = insts[index];
				if(targetInst != null && targetInst instanceof SSAPutInstruction){
					SSAPutInstruction putInst = (SSAPutInstruction)targetInst;
					if(putInst.getDeclaredField().equals(F) && putInst.getUse(0) == objectF)
						return getLocalCreationSite(insts, du, putInst.getUse(1));
				}
			}
		}
		// Fail to find the object creation site in the method. 
		return null;
	}
	
	
	/**
	 * making a mock-up instance key for the method which can be called from a
	 * JavaScript call site.
	 * 
	 * @param method
	 *            the target method used for making the mock-up instance.
	 * @param obj
	 *            the object instance key in which the method is declared.
	 * @return
	 * @throws CancelException
	 */
	private InstanceKey makeMockupInstanceKey(IMethod method) {
		return new MockupInstanceKey(method);
	}

	/**
	 * checking the method has '@JavascriptInterface' annotation. the annotation
	 * shows that the method can be called at a JavaScript call site.
	 * 
	 * @param m
	 *            target method
	 * @return true if the method has '@JavascriptInterface' annotation, false
	 *         otherwise.
	 */
	private boolean hasJavascriptInterfaceAnnotation(IMethod m) {
		if(m.getAnnotations() != null)
			for (Annotation ann : m.getAnnotations()) {
				Atom className = ann.getType().getName().getClassName();
				if (className.equals(interfAnnotationName))
					return true;
			}
		return false;
	}
		
	private InterfaceClass wrappingClass(IClass objClass){
		return InterfaceClass.wrapping(objClass, cha.lookupClass(JavaScriptTypes.Root));
	}
	
	public HybridAPIMisusesChecker getTypeChecker(){
		return typeChecker;
	}
	
	public Set<Warning> getWarnings(){
		return typeChecker.getWarnings();
	}
	
	class NotLocalCreationException extends Exception{
		
		private static final long serialVersionUID = 1477197976866993695L;
		private CGNode node;
		private SSAInstruction inst;
		private int obj;
		
		public NotLocalCreationException(CGNode node, SSAInstruction inst, int obj){
			this.node = node;
			this.inst = inst;
			this.obj = obj;
		}
		
		@Override
		public String getMessage() {
			// TODO Auto-generated method stub
			
			String msg = "-------\n";
			msg += "# caller: " + node +"\n";
			msg += "# instruction: " + "(" + inst.iindex + ") " + inst +"\n";
			msg += "# targetMethod: addJavascriptInterface\n";
			msg += "# object paramter: " + obj + "\n";
			msg += "\tlocal creation? no\n";
			msg += "-------\n";
				
//			msg += IRPrinter.getPrintableInstructios(node);

			return msg;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return getMessage();
		}
		
	}
//	
//	@Override
//	  public JSConstraintVisitor makeVisitor(CGNode node) {
//	    return new AndroidHybridJSConstraintVisitor(this, node);
//	  }
//	
	private Map<Atom, GlobalObjectKey> jsGlobalMap;
	
	public Collection<GlobalObjectKey> getGlobalObjects(Atom language){
		assert language.equals(JavaScriptTypes.jsName);
		
		return jsGlobalMap.values();
	}
	
	public GlobalObjectKey getGlobalObject(Atom language, Atom file){
		assert language.equals(JavaScriptTypes.jsName);
		
		if(!jsGlobalMap.containsKey(file))
			Assertions.UNREACHABLE("the file is not in the scope: " + file);
		
		System.out.println("#Map: " + jsGlobalMap);
		System.out.println("#File: " + file);
		System.out.println("#Global: " + jsGlobalMap.get(file));
		
		return jsGlobalMap.get(file);
	}
	
	private static FieldReference makeNonGlobalFieldReference(FieldReference field) {
	    String nonGlobalFieldName = field.getName().toString().substring(7);
	    field = FieldReference.findOrCreate(JavaScriptTypes.Root, Atom.findOrCreateUnicodeAtom(nonGlobalFieldName),
	        JavaScriptTypes.Root);
	    return field;
	  }
	
	private static boolean directGlobalObjectRef(FieldReference field) {
	    return field.getName().toString().endsWith(JSSSAPropagationCallGraphBuilder.GLOBAL_OBJ_VAR_NAME);
	  }
	
	@Override
	  protected PropagationSystem makeSystem(AnalysisOptions options) {
	    return new PropagationSystem(callGraph, pointerKeyFactory, instanceKeyFactory) {
	      @Override
	      public PointerAnalysis<InstanceKey> makePointerAnalysis(PropagationCallGraphBuilder builder) {
	        assert builder == AndroidHybridCallGraphBuilder.this;
	        return new AndroidHybridPointerAnalysisImple(AndroidHybridCallGraphBuilder.this, cg, pointsToMap,
	            instanceKeys, pointerKeyFactory, instanceKeyFactory);
	      }
	    };
	  }
	
	protected class AndroidHybridPointerAnalysisImple extends CrossLanguagePointerAnalysisImpl{
		protected AndroidHybridPointerAnalysisImple(CrossLanguageSSAPropagationCallGraphBuilder builder, CallGraph cg,
				PointsToMap pointsToMap, MutableMapping<InstanceKey> instanceKeys, PointerKeyFactory pointerKeys,
				InstanceKeyFactory iKeyFactory) {
			super(builder, cg, pointsToMap, instanceKeys, pointerKeys, iKeyFactory);
			// TODO Auto-generated constructor stub
		}
	}
	
	@Override
	  protected TargetLanguageSelector<AstImplicitPointsToSetVisitor, LocalPointerKey> makeImplicitVisitorSelector(
	      CrossLanguagePointerAnalysisImpl analysis) {
	    return new TargetLanguageSelector<AstImplicitPointsToSetVisitor, LocalPointerKey>() {
	      @Override
	      public AstImplicitPointsToSetVisitor get(Atom language, LocalPointerKey construct) {
	        if (JavaScriptTypes.jsName.equals(language)) {
	          return new JSImplicitPointsToSetVisitor((AstPointerAnalysisImpl) getPointerAnalysis(), construct);
	        } else {
	          return new AstImplicitPointsToSetVisitor((AstPointerAnalysisImpl) getPointerAnalysis(), construct);
	        }
	      }
	    };
	  }
	
	class AndroidHybridJSConstraintVisitor extends JSConstraintVisitor{
		public AndroidHybridJSConstraintVisitor(AstSSAPropagationCallGraphBuilder builder, CGNode node) {
			super(builder, node);
		}

		@Override
		public void visitAstGlobalRead(AstGlobalRead instruction) {
			// TODO Auto-generated method stub
			int lval = instruction.getDef();
			FieldReference field = makeNonGlobalFieldReference(instruction.getDeclaredField());
			PointerKey def = getPointerKeyForLocal(lval);
			assert def != null;
			IField f = getClassHierarchy().resolveField(field);
			assert f != null : "could not resolve referenced global " + field;
			if (hasNoInterestingUses(lval)) {
				system.recordImplicitPointsToSet(def);
			} else {
				AndroidHybridCallGraphBuilder builder = (AndroidHybridCallGraphBuilder) getBuilder();
				IMethod method = node.getMethod();
				if ((method instanceof AstMethod) == false)
					Assertions.UNREACHABLE("Global read must be invoked in AstMethod: " + method.getClass().getName());
				String fn = ((AstMethod) method).getSourcePosition().getURL().getFile();

				if (fn.endsWith("preamble.js") || fn.endsWith("prologue.js")) {
					for (InstanceKey globalObj : builder.getGlobalObjects(JavaScriptTypes.jsName)) {
						if (directGlobalObjectRef(field)) {
							// points-to set is just the global object
							system.newConstraint(def, globalObj);
						} else {
							system.findOrCreateIndexForInstanceKey(globalObj);
							PointerKey p = getPointerKeyForInstanceField(globalObj, f);
							system.newConstraint(def, assignOperator, p);
						}
					}
				} else {
					Atom afn = Atom.findOrCreateAsciiAtom(fn.substring(fn.lastIndexOf(File.separator) + 1));
					InstanceKey globalObj = builder.getGlobalObject(JavaScriptTypes.jsName, afn);
					if (directGlobalObjectRef(field)) {
						// points-to set is just the global object
						system.newConstraint(def, globalObj);
					} else {
						system.findOrCreateIndexForInstanceKey(globalObj);
						PointerKey p = getPointerKeyForInstanceField(globalObj, f);
						system.newConstraint(def, assignOperator, p);
					}
				}
			}
		}

		@Override
		public void visitAstGlobalWrite(AstGlobalWrite instruction) {
			// TODO Auto-generated method stub
			int rval = instruction.getVal();
			FieldReference field = makeNonGlobalFieldReference(instruction.getDeclaredField());
			IField f = getClassHierarchy().resolveField(field);
			assert f != null : "could not resolve referenced global " + field;
			assert !f.getFieldTypeReference().isPrimitiveType();

			AndroidHybridCallGraphBuilder builder = (AndroidHybridCallGraphBuilder) getBuilder();
			IMethod method = node.getMethod();
			if ((method instanceof AstMethod) == false)
				Assertions.UNREACHABLE("Global read must be invoked in AstMethod: " + method.getClass().getName());
			String fn = ((AstMethod) method).getSourcePosition().getURL().getFile();

			if (fn.endsWith("preamble.js") || fn.endsWith("prologue.js")) {
				for (InstanceKey globalObj : builder.getGlobalObjects(JavaScriptTypes.jsName)) {
					system.findOrCreateIndexForInstanceKey(globalObj);
					PointerKey p = getPointerKeyForInstanceField(globalObj, f);

					PointerKey rvalKey = getPointerKeyForLocal(rval);
					if (contentsAreInvariant(symbolTable, du, rval)) {
						system.recordImplicitPointsToSet(rvalKey);
						InstanceKey[] ik = getInvariantContents(rval);
						for (int i = 0; i < ik.length; i++) {
							system.newConstraint(p, ik[i]);
						}
					} else {
						system.newConstraint(p, assignOperator, rvalKey);
					}
				}
			} else {
				Atom afn = Atom.findOrCreateAsciiAtom(fn.substring(fn.lastIndexOf(File.separator) + 1));
				InstanceKey globalObj = builder.getGlobalObject(JavaScriptTypes.jsName, afn);
				system.findOrCreateIndexForInstanceKey(globalObj);
				PointerKey p = getPointerKeyForInstanceField(globalObj, f);

				PointerKey rvalKey = getPointerKeyForLocal(rval);
				if (contentsAreInvariant(symbolTable, du, rval)) {
					system.recordImplicitPointsToSet(rvalKey);
					InstanceKey[] ik = getInvariantContents(rval);
					for (int i = 0; i < ik.length; i++) {
						system.newConstraint(p, ik[i]);
					}
				} else {
					system.newConstraint(p, assignOperator, rvalKey);
				}
			}
		}
	}
	
	public class AndroidHybridJSImplicitPointsToSetVisitor extends JSImplicitPointsToSetVisitor {

		public AndroidHybridJSImplicitPointsToSetVisitor(AstPointerAnalysisImpl analysis, LocalPointerKey lpk) {
			super(analysis, lpk);
		}

		@Override
		public void visitJavaScriptInvoke(JavaScriptInvoke instruction) {

		}

		@Override
		public void visitTypeOf(JavaScriptTypeOfInstruction instruction) {

		}

		@Override
		public void visitJavaScriptPropertyRead(JavaScriptPropertyRead instruction) {

		}

		@Override
		public void visitJavaScriptPropertyWrite(JavaScriptPropertyWrite instruction) {

		}

		@Override
		public void visitJavaScriptInstanceOf(JavaScriptInstanceOf instruction) {

		}

		@Override
		public void visitCheckRef(JavaScriptCheckReference instruction) {

		}

		@Override
		public void visitWithRegion(JavaScriptWithRegion instruction) {

		}

		@Override
		public void visitAstGlobalRead(AstGlobalRead instruction) {
			FieldReference field = makeNonGlobalFieldReference(instruction.getDeclaredField());
			assert !directGlobalObjectRef(field);
			IField f = getClassHierarchy().resolveField(field);
			assert f != null;
			MutableSparseIntSet S = MutableSparseIntSet.makeEmpty();

			IMethod method = node.getMethod();
			if ((method instanceof AstMethod) == false)
				Assertions.UNREACHABLE("Global read must be invoked in AstMethod: " + method.getClass().getName());
			String fn = ((AstMethod) method).getSourcePosition().getURL().getFile();

			if (fn.endsWith("preamble.js") || fn.endsWith("prologue.js")) {
				for (InstanceKey globalObj : getGlobalObjects(JavaScriptTypes.jsName)) {
					PointerKey fkey = analysis.getHeapModel().getPointerKeyForInstanceField(globalObj, f);
					if (fkey != null) {
						OrdinalSet pointees = analysis.getPointsToSet(fkey);
						IntSet set = pointees.getBackingSet();
						if (set != null) {
							S.addAll(set);
						}
					}
					pointsToSet = new OrdinalSet<InstanceKey>(S, analysis.getInstanceKeyMapping());
				}
			}else{
				Atom afn = Atom.findOrCreateAsciiAtom(fn.substring(fn.lastIndexOf(File.separator) + 1));
		          InstanceKey globalObj = getGlobalObject(JavaScriptTypes.jsName, afn);
		          PointerKey fkey = analysis.getHeapModel().getPointerKeyForInstanceField(globalObj, f);
					if (fkey != null) {
						OrdinalSet pointees = analysis.getPointsToSet(fkey);
						IntSet set = pointees.getBackingSet();
						if (set != null) {
							S.addAll(set);
						}
					}
					pointsToSet = new OrdinalSet<InstanceKey>(S, analysis.getInstanceKeyMapping());
			}
		}

		@Override
		public void visitSetPrototype(SetPrototype instruction) {
		}

		@Override
		public void visitPrototypeLookup(PrototypeLookup instruction) {
		}
	};
}
