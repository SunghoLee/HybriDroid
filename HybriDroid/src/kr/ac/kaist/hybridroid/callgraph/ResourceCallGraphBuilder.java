package kr.ac.kaist.hybridroid.callgraph;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXCFABuilder;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSACheckCastInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;

import kr.ac.kaist.hybridroid.pointer.ResourceInstanceKey;

public class ResourceCallGraphBuilder extends ZeroXCFABuilder {

	
	public static ResourceCallGraphBuilder make(IClassHierarchy cha, AnalysisOptions options, AnalysisCache cache,
		      ContextSelector appContextSelector, SSAContextInterpreter appContextInterpreter, int instancePolicy) throws IllegalArgumentException {
		    if (options == null) {
		      throw new IllegalArgumentException("options == null");
		    }
		    return new ResourceCallGraphBuilder(cha, options, cache, appContextSelector, appContextInterpreter, instancePolicy);
		  }
	
	public ResourceCallGraphBuilder(IClassHierarchy cha, AnalysisOptions options, AnalysisCache cache,
			ContextSelector appContextSelector, SSAContextInterpreter appContextInterpreter, int instancePolicy) {
		super(cha, options, cache, appContextSelector, appContextInterpreter, instancePolicy);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ConstraintVisitor makeVisitor(CGNode node) {
		// TODO Auto-generated method stub
		return new ResourceVisitor(this, node);
	}
	
	public static class ResourceVisitor extends ConstraintVisitor{

		private final IClass activityClass;
		private final Selector fvbiSelector;
		private final IClassHierarchy cha;
		
		public ResourceVisitor(SSAPropagationCallGraphBuilder builder, CGNode node) {
			super(builder, node);
			// TODO Auto-generated constructor stub
			cha = builder.cha;
			
			TypeReference activityTR = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/app/Activity");
			activityClass = cha.lookupClass(activityTR);
			
			fvbiSelector = Selector.make("findViewById(I)Landroid/view/View;");
		}

		@Override
		public void visitInvoke(SSAInvokeInstruction instruction) {
			// TODO Auto-generated method stub
			MethodReference target = instruction.getDeclaredTarget();
			IClass mainClass = cha.lookupClass(target.getDeclaringClass());
			
			if(fvbiSelector.equals(target.getSelector()) && mainClass != null && cha.isSubclassOf(mainClass, activityClass)){
				SymbolTable symTab = node.getIR().getSymbolTable();
				int paramVar = instruction.getUse(1);
				
				if(symTab.isIntegerConstant(paramVar)){
					int v = symTab.getIntValue(paramVar);
					int defVar = instruction.getDef();
					SSAInstruction nextInst = null;
					
					int index = instruction.iindex;
					do{
						index++;
						nextInst = node.getIR().getInstructions()[index];
					}while(nextInst == null);
					
					if(nextInst instanceof SSACheckCastInstruction){
						SSACheckCastInstruction ccInst = (SSACheckCastInstruction) nextInst;
						// at this point, instancekey may be exploit when the result is not converted to concrete type.  
						for(TypeReference tr : ccInst.getDeclaredResultTypes()){
							ResourceInstanceKey rik = new ResourceInstanceKey(node, cha.lookupClass(tr), instruction.iindex, v);
							system.newConstraint(builder.getPointerKeyForLocal(node, defVar), rik);
						}
					}else{
//						Assertions.UNREACHABLE("the return value of Activity.findViewById must be converted to any concrete type: " + nextInst);
						//currently, if the return of findViewById is not casted, just deal with it as View.
						ResourceInstanceKey rik = new ResourceInstanceKey(node, cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/view/View")), instruction.iindex, v);
						system.newConstraint(builder.getPointerKeyForLocal(node, defVar), rik);
					}
				}else{
					System.err.println("Warning: resource descriptor is not int constant." + instruction);
				}
			}
			super.visitInvoke(instruction);
		}
	}
}
