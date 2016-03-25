package kr.ac.kaist.hybridroid.callgraph;

import java.util.HashMap;
import java.util.Map;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXCFABuilder;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAArrayLengthInstruction;
import com.ibm.wala.ssa.SSAArrayLoadInstruction;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSACheckCastInstruction;
import com.ibm.wala.ssa.SSAComparisonInstruction;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAConversionInstruction;
import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAGotoInstruction;
import com.ibm.wala.ssa.SSAInstanceofInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInstruction.IVisitor;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSALoadMetadataInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPiInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSASwitchInstruction;
import com.ibm.wala.ssa.SSAThrowInstruction;
import com.ibm.wala.ssa.SSAUnaryOpInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.UnimplementedError;

import kr.ac.kaist.hybridroid.analysis.resource.AndroidResourceAnalysis;
import kr.ac.kaist.hybridroid.analysis.resource.AndroidResourceAnalysis.ResourceInfo;
import kr.ac.kaist.hybridroid.pointer.ResourceInstanceKey;
import kr.ac.kaist.hybridroid.utils.Wrapper;

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

			activityClass = cha.lookupClass(activityTR);
			viewClass = cha.lookupClass(viewTR);
			dialogClass = cha.lookupClass(dialogTR);
			windowClass = cha.lookupClass(windowTR);
			
			fvbiSelector = Selector.make("findViewById(I)Landroid/view/View;");
			setidSelector = Selector.make("setId(I)V");
		}

		@Override
		public void visitInvoke(SSAInvokeInstruction instruction) {
			// TODO Auto-generated method stub
			MethodReference target = instruction.getDeclaredTarget();
			IClass mainClass = cha.lookupClass(target.getDeclaringClass());
			
			if (fvbiSelector.equals(target.getSelector()) && mainClass != null
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
							ResourceInstanceKey rik = new ResourceInstanceKey(node, cha.lookupClass(tr), instruction.iindex, v);
							system.newConstraint(builder.getPointerKeyForLocal(node, defVar), rik);
						}
						return;
					}else{
//						Assertions.UNREACHABLE("the return value of Activity.findViewById must be converted to any concrete type: " + nextInst);
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
//						if(instruction.toString().contains("invokevirtual < Application, Landroid/webkit/WebView, addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V > 10,15,16 @26 exception:17")){
//							NodePrinter.printInsts(node);
//							System.out.println("I: " + instruction);
//						}
//						NodePrinter.printInsts(node);
					}else{
						int v = resId;
						if(ccInst != null){
							for(TypeReference tr : ccInst.getDeclaredResultTypes()){
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
			}else if(setidSelector.equals(target.getSelector()) && (mainClass.equals(viewClass) || cha.isSubclassOf(mainClass, viewClass))){
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
						System.err.println("Warning: resource descriptor is not int constant." + instruction + "(" + symTab.isConstant(paramVar) + ") in " + node);
				}else{
					if(symTab.isParameter(receiverVar)){
						TypeReference tr = node.getMethod().getParameterType(receiverVar);
						ResourceInstanceKey rik = new ResourceInstanceKey(node, cha.lookupClass(tr), instruction.iindex, v);
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
							ResourceInstanceKey rik = new ResourceInstanceKey(node, cha.lookupClass(wtr.getObject()), instruction.iindex, v);
							
							try{
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
		 * @param insts
		 * @param startIndex
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
			
			return info.getResourceValue(fieldName);
		}
	}
}
