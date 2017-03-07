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
package kr.ac.kaist.wala.hybridroid.callgraph;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXCFABuilder;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;
import com.ibm.wala.ssa.SSAInstruction.IVisitor;
import com.ibm.wala.types.*;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.debug.UnimplementedError;
import kr.ac.kaist.wala.hybridroid.analysis.resource.AndroidResourceAnalysis;
import kr.ac.kaist.wala.hybridroid.analysis.resource.AndroidResourceAnalysis.ResourceInfo;
import kr.ac.kaist.wala.hybridroid.pointer.ResourceInstanceKey;
import kr.ac.kaist.wala.hybridroid.types.HybriDroidTypes;
import kr.ac.kaist.wala.hybridroid.utils.Wrapper;

import java.util.HashMap;
import java.util.Map;

public class ResourceCallGraphBuilder extends ZeroXCFABuilder {
	public static boolean DEBUG = false;
	private AndroidResourceAnalysis ara;
	
	public static ResourceCallGraphBuilder make(IClassHierarchy cha, AnalysisOptions options, AnalysisCache cache,
		      ContextSelector appContextSelector, SSAContextInterpreter appContextInterpreter, int instancePolicy, AndroidResourceAnalysis ara) throws IllegalArgumentException {
		    if (options == null) {
		      throw new IllegalArgumentException("options == null");
		    }
		    return new ResourceCallGraphBuilder(cha, options, cache, appContextSelector, appContextInterpreter, instancePolicy, ara);
		  }
	
	private ResourceCallGraphBuilder(IClassHierarchy cha, AnalysisOptions options, AnalysisCache cache,
			ContextSelector appContextSelector, SSAContextInterpreter appContextInterpreter, int instancePolicy, AndroidResourceAnalysis ara) {
		super(cha, options, cache, appContextSelector, appContextInterpreter, instancePolicy);
		this.ara = ara;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ConstraintVisitor makeVisitor(CGNode node) {
		// TODO Auto-generated method stub
		return new ResourceVisitor(this, node, ara);
	}
	
	public static class ResourceVisitor extends ConstraintVisitor{

		private final IClass activityClass;
		private final IClass viewClass;
		private final IClass dialogClass;
		private final IClass windowClass;
		private final Selector fvbiSelector;
		private final Selector setidSelector;
		private final IClassHierarchy cha;
		private final AndroidResourceAnalysis ara;
		private final Map<Integer, ResourceInstanceKey> resKeyMap;
		private final TypeReference fakeRootTR;
		
		public ResourceVisitor(SSAPropagationCallGraphBuilder builder, CGNode node, AndroidResourceAnalysis ara) {
			super(builder, node);
			// TODO Auto-generated constructor stub
			cha = builder.cha;
			this.ara = ara;
			
			resKeyMap = new HashMap<Integer, ResourceInstanceKey>();
			
			TypeReference activityTR = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/app/Activity");
			TypeReference dialogTR = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/app/Dialog");
			TypeReference viewTR = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/view/View");
			TypeReference windowTR = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/view/Window");
			fakeRootTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Lcom/ibm/wala/FakeRootClass");
			
			activityClass = cha.lookupClass(activityTR);
			viewClass = cha.lookupClass(viewTR);
			dialogClass = cha.lookupClass(dialogTR);
			windowClass = cha.lookupClass(windowTR);
			
			fvbiSelector = Selector.make("findViewById(I)Landroid/view/View;");
			setidSelector = Selector.make("setId(I)V");
		}

		private TypeReference findLocalType(CGNode n, int usevar){
			IR ir = n.getIR();
			if(ir == null)
				return null;
			
			IMethod m = n.getMethod();
			DefUse du = new DefUse(n.getIR());
			
			if(m.getNumberOfParameters() >= usevar){
//				if(m.isStatic()){
//					return m.getParameterType(usevar);
//				}else{
					return m.getParameterType(usevar-1);
//				}
			}
			
			SSAInstruction inst = du.getDef(usevar);
			
			if(inst instanceof SSANewInstruction){
				SSANewInstruction newInst = (SSANewInstruction) inst;
				return newInst.getConcreteType();
			}else if(inst instanceof SSAGetInstruction){
				SSAGetInstruction getInst = (SSAGetInstruction) inst;
				return getInst.getDeclaredFieldType();
			}
			
			return null;
		}
		
		@Override
		public void visitInvoke(SSAInvokeInstruction instruction) {
			// TODO Auto-generated method stub
			MethodReference target = instruction.getDeclaredTarget();
			TypeReference targetTR = target.getDeclaringClass();
			
			if(targetTR.equals(fakeRootTR)){
				super.visitInvoke(instruction);
				return;
			}
			
			IClass mainClass = cha.lookupClass(targetTR);
			
//			if(mainClass == null)	
//				Assertions.UNREACHABLE(target.getDeclaringClass() + " does not exist.");
			//TODO: need to detach this part to a seperate logic
			if(target.getSelector().equals(HybriDroidTypes.SETWEBVIEWCLIENT_SELECTOR) && (mainClass.getReference().equals(HybriDroidTypes.WEBVIEW_PRI_CLASS) || mainClass.getReference().equals(HybriDroidTypes.WEBVIEW_APP_CLASS))){
				int objvar = instruction.getUse(1);
				int webviewvar = instruction.getUse(0);

				TypeReference objType = findLocalType(node, objvar);
				if(objType != null) {
					IClass objClass = cha.lookupClass(objType);

					PointerKey webviewPK = builder.getPointerKeyForLocal(node, webviewvar);
					if (!system.isImplicit(webviewPK)) {
						for (IMethod m : objClass.getAllMethods()) {
							if (m.getDeclaringClass().equals(objClass) && m.getName().toString().startsWith("on")) {
								try {
									CGNode callbackNode = builder.getCallGraph().findOrCreateNode(m, Everywhere.EVERYWHERE);
									if(!system.isImplicit(builder.getPointerKeyForLocal(callbackNode, 2))) {
										boolean n = system.newConstraint(builder.getPointerKeyForLocal(callbackNode, 2), assignOperator, webviewPK);
									}
//							System.out.println("inst : " + instruction);
//							System.out.println("\tPK : " + webviewPK);
//							System.out.println("\t" + n + " : " + callbackNode);
								} catch (CancelException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}else if (fvbiSelector.equals(target.getSelector()) && mainClass != null
					&& (cha.isSubclassOf(mainClass, activityClass) || 
						mainClass.equals(viewClass)	|| cha.isSubclassOf(mainClass, viewClass) || 
						mainClass.equals(dialogClass) || cha.isSubclassOf(mainClass, dialogClass) || 
						mainClass.equals(windowClass) || cha.isSubclassOf(mainClass, windowClass))) {
				SymbolTable symTab = node.getIR().getSymbolTable();
				int paramVar = instruction.getUse(1);
				int defVar = instruction.getDef();
				SSACheckCastInstruction ccInst = getCastingInstructionOf(node.getIR().getInstructions(), instruction.iindex, defVar);
				
				if(symTab.isIntegerConstant(paramVar)){
					int v = symTab.getIntValue(paramVar);

					if(ccInst != null){
						// at this point, instancekey may be exploit when the result is not converted to concrete type.
						for(TypeReference tr : ccInst.getDeclaredResultTypes()){
							IClass klass = cha.lookupClass(tr);
							if(klass == null) {
//								Assertions.UNREACHABLE(tr + " does not exist.");
								System.err.println("[Resource Error] " + tr + " does not exist.");
								return;
							}

							ResourceInstanceKey rik = new ResourceInstanceKey(node, klass, instruction.iindex, v);
							system.newConstraint(builder.getPointerKeyForLocal(node, defVar), rik);
						}
						return;
					}else{
						//currently, if the return of findViewById is not casted, just deal with it as View.
						ResourceInstanceKey rik = new ResourceInstanceKey(node, cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/view/View")), instruction.iindex, v);
						system.newConstraint(builder.getPointerKeyForLocal(node, defVar), rik);
						return;
					}
				}else{
					int resId = getStaticDefinedIntValue(node, paramVar);
					if(resId < 0){
						if(DEBUG)
							System.err.println("Warning: resource descriptor is not int constant." + instruction + "(" + symTab.isConstant(paramVar) + ") in " + node);
					}else{
						int v = resId;
						if(ccInst != null){
							for(TypeReference tr : ccInst.getDeclaredResultTypes()){
								IClass klass = cha.lookupClass(tr);
								if(klass == null)
									Assertions.UNREACHABLE(tr +" does not exist.");
								ResourceInstanceKey rik = new ResourceInstanceKey(node, cha.lookupClass(tr), instruction.iindex, v);
								system.newConstraint(builder.getPointerKeyForLocal(node, defVar), rik);
							}
							return;
						}else{
							ResourceInstanceKey rik = new ResourceInstanceKey(node, cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/view/View")), instruction.iindex, v);
							system.newConstraint(builder.getPointerKeyForLocal(node, defVar), rik);
							return;
						}
					}
				}
			}else if(setidSelector.equals(target.getSelector()) && mainClass != null && (viewClass.equals(mainClass) || cha.isSubclassOf(mainClass, viewClass))){
				SymbolTable symTab = node.getIR().getSymbolTable();
				int receiverVar = instruction.getUse(0);
				int paramVar = instruction.getUse(1);
				int v = -1;
				
				if(symTab.isIntegerConstant(paramVar))
					v = symTab.getIntValue(paramVar);
				else
					v = getStaticDefinedIntValue(node, paramVar);
				
				if(v < 0){
					if(DEBUG)
						System.err.println("Warning: resource descriptor is not inlst constant." + instruction + "(" + symTab.isConstant(paramVar) + ") in " + node);
				}else{
					if(symTab.isParameter(receiverVar)){
						TypeReference tr = null;

						if(receiverVar == 1)
							tr = node.getMethod().getDeclaringClass().getReference();
						else
							tr = node.getMethod().getParameterType(receiverVar);

						IClass klass = cha.lookupClass(tr);
						if(klass == null)
							Assertions.UNREACHABLE(tr +" does not exist in this application.");
						
						ResourceInstanceKey rik = new ResourceInstanceKey(node, klass, instruction.iindex, v);
						system.newConstraint(builder.getPointerKeyForLocal(node, receiverVar), rik);
					}else{
						SSAInstruction defInst = node.getDU().getDef(receiverVar);
						final Wrapper<TypeReference> wtr = new Wrapper<TypeReference>();
						
						defInst.visit(new IVisitor(){

							@Override
							public void visitGoto(SSAGotoInstruction instruction) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
								// TODO Auto-generated method stub
								wtr.setObject(instruction.getElementType());
							}

							@Override
							public void visitArrayStore(SSAArrayStoreInstruction instruction) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void visitBinaryOp(SSABinaryOpInstruction instruction) {
								// TODO Auto-generated method stub
								wtr.setObject(TypeReference.JavaLangInteger);
							}

							@Override
							public void visitUnaryOp(SSAUnaryOpInstruction instruction) {
								// TODO Auto-generated method stub
								wtr.setObject(TypeReference.JavaLangInteger);
							}

							@Override
							public void visitConversion(SSAConversionInstruction instruction) {
								// TODO Auto-generated method stub
								wtr.setObject(instruction.getToType());
							}

							@Override
							public void visitComparison(SSAComparisonInstruction instruction) {
								// TODO Auto-generated method stub
								wtr.setObject(TypeReference.JavaLangBoolean);
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
								wtr.setObject(instruction.getDeclaredFieldType());
							}

							@Override
							public void visitPut(SSAPutInstruction instruction) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void visitInvoke(SSAInvokeInstruction instruction) {
								// TODO Auto-generated method stub
								wtr.setObject(instruction.getDeclaredResultType());
							}

							@Override
							public void visitNew(SSANewInstruction instruction) {
								// TODO Auto-generated method stub
								wtr.setObject(instruction.getConcreteType());
							}

							@Override
							public void visitArrayLength(SSAArrayLengthInstruction instruction) {
								// TODO Auto-generated method stub
								wtr.setObject(TypeReference.JavaLangInteger);
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
								wtr.setObject(TypeReference.JavaLangBoolean);
							}

							@Override
							public void visitInstanceof(SSAInstanceofInstruction instruction) {
								// TODO Auto-generated method stub
								wtr.setObject(TypeReference.JavaLangBoolean);
							}

							@Override
							public void visitPhi(SSAPhiInstruction instruction) {
								// TODO Auto-generated method stub
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
								wtr.setObject(instruction.getType());
							}
						});
						
						if(wtr.has()){
							PointerKey pk = builder.getPointerKeyForLocal(node, receiverVar);
							IClass klass = cha.lookupClass(wtr.getObject());
							if(klass == null)
								Assertions.UNREACHABLE(wtr.getObject() +" does not exist.");
							
							ResourceInstanceKey rik = new ResourceInstanceKey(node, klass, instruction.iindex, v);
							
							try{
								if(!system.isImplicit(pk))
									system.newConstraint(pk, rik);
							}catch(UnimplementedError e){ // if the pointer key is implicit, then just return; work normally.
								return;
							}
						}
					}
					return;
				}
			}
			super.visitInvoke(instruction);
		}
		
		private SSACheckCastInstruction getCastingInstructionOf(SSAInstruction[] insts, int startIndex, int var){
			SSAInstruction nextInst = null;
			do{
				startIndex++;
				nextInst = insts[startIndex];
				
				if(nextInst != null && nextInst instanceof SSACheckCastInstruction && nextInst.getUse(0) == var)
					return (SSACheckCastInstruction) nextInst;
			}while(startIndex+1 < insts.length);
			return null;
		}
		
		/**
		 * Additional integer calculation for statically predefined integer variable.
		 * @param n
		 * @param var
		 * @return
		 */
		private int getStaticDefinedIntValue(CGNode n, int var){
			SSAInstruction defInst = n.getDU().getDef(var);
			
			if(defInst == null || !(defInst instanceof SSAGetInstruction))
				return -1;
			
			SSAGetInstruction getInst = (SSAGetInstruction) defInst;
						
			if(!getInst.isStatic()) // it is not the static predefined value case
				return -1;
			
			FieldReference fr = getInst.getDeclaredField();
			
			TypeReference classIT = fr.getDeclaringClass();
			String classpath = classIT.getName().toString();
			
			String fieldName = fr.getName().toString();
			
			ResourceInfo info = ara.getInfo(classpath);

			if(info == null || !info.isDeclaredResorce(fieldName))
				return -1;
			
			int v = info.getResourceValue(fieldName);
			
			return v;
		}
	}
}
