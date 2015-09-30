package kr.ac.kaist.hybridroid.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.ac.kaist.hybridroid.util.debug.Debug;

import com.ibm.wala.cast.ir.ssa.AssignInstruction;
import com.ibm.wala.cast.ir.ssa.AstAssertInstruction;
import com.ibm.wala.cast.ir.ssa.AstEchoInstruction;
import com.ibm.wala.cast.ir.ssa.AstGlobalRead;
import com.ibm.wala.cast.ir.ssa.AstGlobalWrite;
import com.ibm.wala.cast.ir.ssa.AstIsDefinedInstruction;
import com.ibm.wala.cast.ir.ssa.AstLexicalRead;
import com.ibm.wala.cast.ir.ssa.AstLexicalWrite;
import com.ibm.wala.cast.ir.ssa.EachElementGetInstruction;
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
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.dataflow.IFDS.IFlowFunction;
import com.ibm.wala.dataflow.IFDS.IMergeFunction;
import com.ibm.wala.dataflow.IFDS.IPartiallyBalancedFlowFunctions;
import com.ibm.wala.dataflow.IFDS.ISupergraph;
import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.dataflow.IFDS.IdentityFlowFunction;
import com.ibm.wala.dataflow.IFDS.PartiallyBalancedTabulationProblem;
import com.ibm.wala.dataflow.IFDS.PartiallyBalancedTabulationSolver;
import com.ibm.wala.dataflow.IFDS.PathEdge;
import com.ibm.wala.dataflow.IFDS.TabulationDomain;
import com.ibm.wala.dataflow.IFDS.TabulationResult;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAAddressOfInstruction;
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
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSALoadIndirectInstruction;
import com.ibm.wala.ssa.SSALoadMetadataInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPiInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSAStoreIndirectInstruction;
import com.ibm.wala.ssa.SSASwitchInstruction;
import com.ibm.wala.ssa.SSAThrowInstruction;
import com.ibm.wala.ssa.SSAUnaryOpInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.MutableMapping;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.strings.Atom;

public class TaintAnalysisForHybrid {
	private final ISupergraph<BasicBlockInContext<IExplodedBasicBlock>, CGNode> supergraph;
	private final TaintDomain domain = new TaintDomain();
	private CallGraph cg;
	private boolean isTainted = false;
	//pending assign for return value at call black is needed, because there is no return block in supergraph.
	
	private Map<BasicBlockInContext<IExplodedBasicBlock>, Set<Pair<CGNode,Integer>>> pendingAssignForReturn;
	
	private static Atom[][] source={
		{Atom.findOrCreateAsciiAtom("LocationManager"),Atom.findOrCreateAsciiAtom("getLastKnownLocation")}
	};
	
	private static Atom[][] sink={
		{Atom.findOrCreateAsciiAtom("XMLHttpRequest"),Atom.findOrCreateAsciiAtom("send")}
	};
	
	final private boolean isSourceTarget(CGNode caller, SSAAbstractInvokeInstruction inst){
		Debug.setDebuggable(this, true);
		//TODO: after perfectly modeling of Activity, use this code commented out.
//		for(CGNode target : cg.getPossibleTargets(caller, inst.getCallSite())){
//			Atom className = target.getMethod().getDeclaringClass().getName().getClassName();
//			Atom methodName = target.getMethod().getName();
//			Debug.printMsg("#Class: "+className);
//			Debug.printMsg("#Method: "+methodName);
//			for(int i=0; i<source.length; i++){
//				if(className.equals(source[i][0]) && methodName.equals(source[i][1]))
//					return true;
//			}
//		}
		
		Atom className = inst.getDeclaredTarget().getDeclaringClass().getName().getClassName();
		Atom methodName = inst.getDeclaredTarget().getName();
		
		for(int i=0; i<source.length; i++){
			if(className.equals(source[i][0]) && methodName.equals(source[i][1]))
				return true;
		}
		return false;
	}
	
	final private boolean isSinkTarget(CGNode target){
		Atom className = target.getMethod().getDeclaringClass().getName().getClassName();
		Atom methodName = target.getMethod().getName();
		for(int i=0; i<sink.length; i++){
			if(className.equals(sink[i][0]) && methodName.equals(sink[i][1]))
				return true;
//			if(methodName.equals(sink[i][1]))
//				return true;
		}
		return false;
	}
	
	public TaintAnalysisForHybrid(CallGraph cg){
		this.cg = cg;
		pendingAssignForReturn = new HashMap<BasicBlockInContext<IExplodedBasicBlock>, Set<Pair<CGNode,Integer>>>();
		System.out.println("#building super graph...");
		this.supergraph = ICFGSupergraph.make(cg, new AnalysisCache());
		System.out.println("\tdone.");
	}
	
	final private void pendingReturnAssign(BasicBlockInContext<IExplodedBasicBlock> exitBlock, Pair<CGNode,Integer> pair){
		Set<Pair<CGNode,Integer>> pairSet;
		if(!pendingAssignForReturn.containsKey(exitBlock)){
			pendingAssignForReturn.put(exitBlock, new HashSet<Pair<CGNode,Integer>>());
		}
		pairSet = pendingAssignForReturn.get(exitBlock);
		pairSet.add(pair);
	}
	
	final private List<Integer> execReturnAssign(BasicBlockInContext<IExplodedBasicBlock> exitBlock, TabulationDomain<Pair<CGNode, Integer>, BasicBlockInContext<IExplodedBasicBlock>> domain){
		List<Integer> dvs = new ArrayList<Integer>(0); 
		if(!pendingAssignForReturn.containsKey(exitBlock))
			return dvs;
		Set<Pair<CGNode,Integer>> targetVarSet = pendingAssignForReturn.get(exitBlock);
		for(Pair<CGNode,Integer> varPair : targetVarSet){
			int d = domain.add(varPair);
			dvs.add(d);
		}
		targetVarSet.clear();
		pendingAssignForReturn.remove(exitBlock);
		
		return dvs;
	}
	
	public void analyze(){
		this.solve();
		System.out.println("#find taint: "+isTainted);
	}
	
		/** Lee
	   * controls numbering of variables for use in tabulation
	   */
	  private class TaintDomain extends MutableMapping<Pair<CGNode, Integer>> implements
	      TabulationDomain<Pair<CGNode, Integer>, BasicBlockInContext<IExplodedBasicBlock>> {

	    @Override
	    public boolean hasPriorityOver(PathEdge<BasicBlockInContext<IExplodedBasicBlock>> p1,
	        PathEdge<BasicBlockInContext<IExplodedBasicBlock>> p2) {
	      // don't worry about worklist priorities
	      return false;
	    }

	  }

	  /**
	   * if taint propagation occurs, return newly tainted variable, otherwise return -1
	   * @param inst target instruction.
	   * @param var tainted variable can be propagated.
	   * @return the newly tainted variable. if there is no that, return -1 
	   */
	  final private int calcPropagatedVariable(SSAInstruction inst, int var){
		  if(!inst.hasDef())
			  return -1;
		  
		  if(inst instanceof JavaScriptCheckReference){
			  
	        }else if(inst instanceof SSAGetInstruction){
	        	
	        }
	        else if(inst instanceof JavaScriptInstanceOf){
	        	
	        }
	        else if(inst instanceof JavaScriptInvoke){
	        	
	        }
	        else if(inst instanceof JavaScriptPropertyRead){
	        	JavaScriptPropertyRead propertyReadInst = (JavaScriptPropertyRead)inst;
	        	if(propertyReadInst.getUse(0) == var)
	        		return propertyReadInst.getDef();
	        }
	        else if(inst instanceof JavaScriptPropertyWrite){
	        	JavaScriptPropertyWrite propertyWriteInst = (JavaScriptPropertyWrite)inst;
	        	if(propertyWriteInst.getUse(2) == var)
	        		return propertyWriteInst.getUse(0);
	        }
	        else if(inst instanceof JavaScriptTypeOfInstruction ){
	        	
	        }
	        else if(inst instanceof JavaScriptWithRegion){
	        	
	        }
	        else if(inst instanceof AstAssertInstruction){
	        	
	        }
	        else if(inst instanceof com.ibm.wala.cast.ir.ssa.AssignInstruction){
	        	AssignInstruction assignInst = (AssignInstruction)inst;
	        	if(assignInst.getUse(0) == var)
	        		return assignInst.getDef();
	        }
	        else if(inst instanceof com.ibm.wala.cast.ir.ssa.EachElementGetInstruction){
	        	EachElementGetInstruction eachGetInst = (EachElementGetInstruction)inst;
	        	if(eachGetInst.getUse(0) == var)
	        		return eachGetInst.getDef();
	        }
	        else if(inst instanceof com.ibm.wala.cast.ir.ssa.EachElementHasNextInstruction){
	        	
	        }
	        else if(inst instanceof AstEchoInstruction){
	        	
	        }
	        else if(inst instanceof AstGlobalRead){
	        	
	        }
	        else if(inst instanceof AstGlobalWrite){
	        	
	        }
	        else if(inst instanceof AstIsDefinedInstruction){
	        	
	        }
	        else if(inst instanceof AstLexicalRead){
	        	
	        }
	        else if(inst instanceof AstLexicalWrite){
	        	
	        }
	        else if(inst instanceof SSAArrayLengthInstruction){
	        	
	        }
	        else if(inst instanceof SSAArrayLoadInstruction){
	        	SSAArrayLoadInstruction arrayLoadInst = (SSAArrayLoadInstruction)inst;
	        	if(arrayLoadInst.getArrayRef() == var)
	        		return arrayLoadInst.getDef();
	        }
	        else if(inst instanceof SSAArrayStoreInstruction){
	        	SSAArrayStoreInstruction arrayStoreInst = (SSAArrayStoreInstruction)inst;
	        	if(arrayStoreInst.getUse(0) == var)
	        		return arrayStoreInst.getArrayRef();
	        }
	        else if(inst instanceof SSABinaryOpInstruction){
	        	SSABinaryOpInstruction binaryOpInst = (SSABinaryOpInstruction)inst;
	        	if(binaryOpInst.getUse(0) == var || binaryOpInst.getUse(1) == var)
	        		return binaryOpInst.getDef();
	        }
	        else if(inst instanceof SSACheckCastInstruction){
	        	
	        }else if(inst instanceof SSAComparisonInstruction){
	        	
	        }else if(inst instanceof SSAConditionalBranchInstruction){
	        	
	        }else if(inst instanceof SSAConversionInstruction){
	        	SSAConversionInstruction convInst = (SSAConversionInstruction)inst;
	        	if(convInst.getUse(0) == var)
	        		return convInst.getDef();
	        }else if(inst instanceof SSAGetCaughtExceptionInstruction){
	        	
	        }else if(inst instanceof SSAGetInstruction){
	        	SSAGetInstruction getInst = (SSAGetInstruction)inst;
	        	if(getInst.getUse(0) == var)
	        		return getInst.getDef();
	        }else if(inst instanceof SSAGotoInstruction){
	        	
	        }else if(inst instanceof SSAInstanceofInstruction){
	        	
	        }else if(inst instanceof SSAInvokeInstruction){
	        	
	        }else if(inst instanceof SSALoadMetadataInstruction){
	        	
	        }else if(inst instanceof SSANewInstruction){
	        	
	        }else if(inst instanceof SSAPhiInstruction){
	        	SSAPhiInstruction phiInst = (SSAPhiInstruction)inst;
	        	for(int i=0; i<phiInst.getNumberOfUses(); i++){
	        		int usedVar = phiInst.getUse(i);
	        		if(usedVar == var)
	        			return phiInst.getDef();
	        	}
	        }else if(inst instanceof SSAPiInstruction){
	        	
	        }else if(inst instanceof SSAPutInstruction){
	        	SSAPutInstruction putInst = (SSAPutInstruction)inst;
	        	if(putInst.getUse(1) == var)
	        		return putInst.getUse(0);
	        }else if(inst instanceof SSAReturnInstruction){
	        	
	        }else if(inst instanceof SSASwitchInstruction){
	        	
	        }else if(inst instanceof SSAThrowInstruction){
	        	
	        }else if(inst instanceof SSAUnaryOpInstruction){
	        	SSAUnaryOpInstruction unaryOpInst = (SSAUnaryOpInstruction)inst;
	        	if(unaryOpInst.getUse(0) == var)
	        		return unaryOpInst.getDef();
	        }else if(inst instanceof SSAAddressOfInstruction){
	        	
	        }else if(inst instanceof SSALoadIndirectInstruction){
	        	
	        }else if(inst instanceof SSAStoreIndirectInstruction){
	        	
	        }else if(inst instanceof PrototypeLookup){
	        	PrototypeLookup protoLookupInst = (PrototypeLookup)inst;
	        	if(protoLookupInst.getUse(0) == var)
	        		return protoLookupInst.getDef();
	        }else if(inst instanceof SetPrototype){
	        	
	        }else{
	        	Debug.printMsg("\tDidn't find!");
	        }
		  return -1;
	  }
	  
	  final private BasicBlockInContext<IExplodedBasicBlock> findLastCallBlock(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest){
		  BasicBlockInContext<IExplodedBasicBlock> callBlock = null;
		  Iterator<BasicBlockInContext<IExplodedBasicBlock>> iPredBB = supergraph.getPredNodes(dest);
		  
		  while(iPredBB.hasNext()){
			  BasicBlockInContext<IExplodedBasicBlock> predBB = iPredBB.next();
			  SSAInstruction inst = predBB.getDelegate().getInstruction();
			  if(inst != null && inst instanceof SSAAbstractInvokeInstruction){
				  SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction)inst;
				  if(cg.getPossibleTargets(dest.getNode(), invokeInst.getCallSite()).contains(src.getNode()))
					  return predBB;
			  }
		  }	
		  
		  return callBlock;
	  }
	  
	  final private List<BasicBlockInContext<IExplodedBasicBlock>> findReturnBlocks(BasicBlockInContext<IExplodedBasicBlock> exit){
		  List<BasicBlockInContext<IExplodedBasicBlock>> returnBlocks = new ArrayList<BasicBlockInContext<IExplodedBasicBlock>>();
		  Iterator<BasicBlockInContext<IExplodedBasicBlock>> iPredBB = supergraph.getPredNodes(exit);
		  
		  while(iPredBB.hasNext()){
			  BasicBlockInContext<IExplodedBasicBlock> predBB = iPredBB.next();
			  SSAInstruction inst = predBB.getDelegate().getInstruction();
			  if(inst != null && inst instanceof SSAReturnInstruction){
				  returnBlocks.add(predBB);
			  }
		  }
		  return returnBlocks;
	  }
	  
	  private class TaintFlowFunctions implements IPartiallyBalancedFlowFunctions<BasicBlockInContext<IExplodedBasicBlock>> {

	    private final TaintDomain domain;

	    protected TaintFlowFunctions(TaintDomain domain) {
	      this.domain = domain;
	    }

	    /**
	     * the flow function for flow from a callee to caller where there was no flow from caller to callee; just the identity function
	     * 
	     * @see ReachingDefsProblem
	     */
	    @Override
	    public IFlowFunction getUnbalancedReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
	        BasicBlockInContext<IExplodedBasicBlock> dest) {
	    	final BasicBlockInContext<IExplodedBasicBlock> srcBB = src;
	    	final SSAInstruction inst = src.getLastInstruction();
	        final BasicBlockInContext<IExplodedBasicBlock> destBB = dest;
//	        Debug.setDebuggable(this, true);
//	        if(src.toString().contains("getLocation")){
//		        Debug.printMsg("#Unbalanced!");
//		        Debug.printMsg("#src: "+src);
//		        Debug.printMsg("#dest: "+dest);
//		        Debug.printMsg("");
//	    	}
//	        Debug.setDebuggable(this, true);
//	        Debug.printMsg("#Unbalanced!");
//	        Debug.printMsg("#src: "+src);
//	        Debug.printMsg("#dest: "+dest);
//	        Debug.printMsg("");
//	        Debug.temporaryStop();
	      return IdentityFlowFunction.identity();
	     /* 
		      return new IUnaryFlowFunction(){

					@Override
					public IntSet getTargets(int d1) {
						// TODO Auto-generated method stub
						MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();
						
						Pair<CGNode,Integer> pair = domain.getMappedObject(d1);
						CGNode node = pair.fst;
						int var = pair.snd;
						
						//if this is ssa form, there is no deletion of taint info.
						result.add(d1);
						
						Debug.setDebuggable(this, true);
						
						if(node.equals(srcBB.getNode())){
						List<BasicBlockInContext<IExplodedBasicBlock>> retBBs = findReturnBlocks(srcBB);
						
						boolean retTainted = false;

//						if(srcBB.getNode().toString().contains("getLocation>")){
//							Debug.printMsg("#Node: "+node);
//							Debug.printMsg("#BB: "+srcBB);
//						}
						
						for(BasicBlockInContext<IExplodedBasicBlock> retBB : retBBs){
							SSAInstruction inst = retBB.getDelegate().getInstruction();
							if(inst != null && inst instanceof SSAReturnInstruction){
//								if(srcBB.getNode().toString().contains("getLocation>")){
//									Debug.printMsg("\t"+inst);
//									Debug.printMsg("\tvar: "+var);
//								}
								if(inst.getUse(0) == var){
									retTainted = true;
									break;
								}
							}
						}
						
						if(retTainted){
							BasicBlockInContext<IExplodedBasicBlock> callBlock = findLastCallBlock(srcBB,destBB);
							SSAInstruction inst = callBlock.getDelegate().getInstruction();
							if(inst != null && inst.hasDef()){
//								if(srcBB.getNode().toString().contains("getLocation>")){
//									Debug.printMsg("#Returned: "+destBB.getNode());
//									Debug.printMsg("#BB: "+destBB);
//									Debug.printMsg("\t: "+inst);
//									Debug.printMsg("\tvar: "+inst.getDef());
//								}
								int d2 = domain.add(Pair.make(destBB.getNode(), inst.getDef()));
								result.add(d2);
//								System.out.println("#7CallInst: "+inst +"\t set: "+inst.getDef());
							}
						}
						}
						return result;
					}
		    		
		    	}; 
		    	*/
	    }

	    /**
	     * flow function from caller to callee; just the identity function
	     */
	    @Override
	    public IUnaryFlowFunction getCallFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
	        BasicBlockInContext<IExplodedBasicBlock> dest, BasicBlockInContext<IExplodedBasicBlock> ret) {
	    	final IExplodedBasicBlock srcEbb= src.getDelegate();
	    	SSAInstruction srcInst = srcEbb.getInstruction();
	    	
	    	if(srcInst instanceof SSAAbstractInvokeInstruction){
	    		final BasicBlockInContext<IExplodedBasicBlock> srcBB = src;
	    		final CGNode srcNode = src.getNode();
	    		final CGNode target = dest.getNode();
	    		final SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction)srcInst;
		      return new IUnaryFlowFunction(){
				@Override
				public IntSet getTargets(int d1) {
					// TODO Auto-generated method stub
					MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();
					
					Pair<CGNode,Integer> pair = domain.getMappedObject(d1);
					CGNode node = pair.fst;
					int var = pair.snd;
					result.add(d1);
					
					if(node.equals(srcNode)){
						Debug.setDebuggable(this, true);
//						if(invokeInst.hasDef()){
//							for(BasicBlockInContext<IExplodedBasicBlock> exitBB : supergraph.getExitsForProcedure(target)){
//								pendingReturnAssign(exitBB,Pair.make(srcNode, invokeInst.getDef()));
//							}
//						}
						
//						if(srcBB.getNode().toString().contains("getLocation")){
//							Debug.printMsg("#Node: "+node);
//							Debug.printMsg("#BB: "+srcBB);
//							Debug.printMsg("\t"+invokeInst);
//						}
							
						if(invokeInst.isStatic()){
							for(int i=0; i < invokeInst.getNumberOfUses(); i++){
								int useVar = invokeInst.getUse(i);
								if(var == useVar){
									int d2 = domain.add(Pair.make(target, i+1));
//									System.out.println("#1CallInst: "+invokeInst + "\tset: "+(i+1));
									result.add(d2);
									
									if(invokeInst.hasDef() && invokeInst.getDeclaredTarget().getName().toString().contains("valueOf")){
										System.out.println("\t#Q: " + invokeInst.getDeclaredTarget().getName().toString());
										int d3 = domain.add(Pair.make(srcNode, invokeInst.getDef()));
										result.add(d3);
									}
								}
							}
						}else{
							if(isSinkTarget(target)){
								for(int i=0; i < invokeInst.getNumberOfUses(); i++){
									int useVar = invokeInst.getUse(i);
									if(var == useVar){
										Debug.setDebuggable(this, true);
										Debug.printMsg(" private data is leaked : the param("+useVar+") is tainted.");
										isTainted = true;
										Debug.temporaryStop();
									}
								}
							}else{
								for(int i=0; i < invokeInst.getNumberOfUses(); i++){
									int useVar = invokeInst.getUse(i);
									
									if(srcBB.getNode().toString().contains("sendMessage(Ljava/lang/String;)V >")){
										Debug.printMsg("\t"+invokeInst);
										Debug.printMsg("\tvar: "+var);
									}
									
									if(var == useVar){
										int d2 = 0;
										if(srcNode.getMethod().getDeclaringClass().getClassLoader().getLanguage().equals(JavaScriptLoader.JS) 
												&& target.getMethod().getDeclaringClass().getClassLoader().getLanguage().equals(Language.JAVA))
											if(invokeInst.isStatic()){
												d2 = domain.add(Pair.make(target, i-1));
//												System.out.println("#2CallInst: "+invokeInst + "\tset: "+(i-1));
											}
											else{
												d2 = domain.add(Pair.make(target, i));
//												System.out.println("#3CallInst: "+invokeInst + "\tset: "+(i));
											}
										else{
											d2 = domain.add(Pair.make(target, i+1));
//											System.out.println("#4CallInst: "+invokeInst + "\tset: " + (i+1));
										}
										result.add(d2);
										if(srcBB.getNode().toString().contains("sendMessage(Ljava/lang/String;)V >")){
											Debug.printMsg("#target: "+target);
											Debug.printMsg("\tvar: "+domain.getMappedObject(d2).snd);
										}
									}
								}
							}
						}
					}
					return result;
				}
		    	  
		      };
	    	}
	    	return IdentityFlowFunction.identity();
	    }

	    /**
	     * flow function from call node to return node when there are no targets for the call site; not a case we are expecting
	     */
	    @Override
	    public IUnaryFlowFunction getCallNoneToReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
	        BasicBlockInContext<IExplodedBasicBlock> dest) {
	      // if we're missing callees, just keep what information we have
	    	final IExplodedBasicBlock srcEbb= src.getDelegate();
	    	SSAInstruction srcInst = srcEbb.getInstruction();
	    	
	    	if(srcInst instanceof SSAAbstractInvokeInstruction){
	    		final CGNode srcNode = src.getNode();
	    		final SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction)srcInst;
		      return new IUnaryFlowFunction(){
				@Override
				public IntSet getTargets(int d1) {
					// TODO Auto-generated method stub
					MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();
					
					Pair<CGNode,Integer> pair = domain.getMappedObject(d1);
					CGNode node = pair.fst;
					int var = pair.snd;
					
					result.add(d1);
					
					if(node.equals(srcNode)){
						if(!invokeInst.isStatic() && invokeInst.getNumberOfUses() > 0 && invokeInst.hasDef() && invokeInst.getUse(0) == var){
							int defVar = invokeInst.getDef();
							int d2 = domain.add(Pair.make(srcNode, defVar));
//							System.out.println("#4CallInst: "+invokeInst + "\tset: " + defVar);
							result.add(d2);
						}
					}
					return result;
				}
		      };
	    	}
	    	return IdentityFlowFunction.identity();
	    }

	    /**
	     * flow function from call node to return node at a call site when callees exist. We kill everything; surviving facts should
	     * flow out of the callee
	     */
	    @Override
	    public IUnaryFlowFunction getCallToReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
	        BasicBlockInContext<IExplodedBasicBlock> dest) {
	    	
	    	final IExplodedBasicBlock srcEbb= src.getDelegate();
	    	SSAInstruction srcInst = srcEbb.getInstruction();
	    	if(srcInst instanceof SSAAbstractInvokeInstruction){
	    		final CGNode srcNode = src.getNode();
	    		final SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction)srcInst;
		      return new IUnaryFlowFunction(){
				@Override
				public IntSet getTargets(int d1) {
					// TODO Auto-generated method stub
					MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();
					
					Pair<CGNode,Integer> pair = domain.getMappedObject(d1);
					CGNode node = pair.fst;
					int var = pair.snd;
					
					result.add(d1);
					
					if(node.equals(srcNode)){
						if(!invokeInst.isStatic() && invokeInst.getNumberOfUses() > 0 && invokeInst.hasDef() && invokeInst.getUse(0) == var){
							int defVar = invokeInst.getDef();
							int d2 = domain.add(Pair.make(srcNode, defVar));
//							System.out.println("#5CallInst: "+invokeInst + "\tset: "+defVar);
							result.add(d2);
						}
					}
					return result;
				}
		      };
	    	}
	    	return IdentityFlowFunction.identity();
	    }

	    /**
	     * flow function for normal intraprocedural edges
	     */
	    @Override
	    public IUnaryFlowFunction getNormalFlowFunction(final BasicBlockInContext<IExplodedBasicBlock> src,
	        BasicBlockInContext<IExplodedBasicBlock> dest) {
	      final IExplodedBasicBlock ebb = src.getDelegate();
	      final CGNode srcNode = src.getNode();
	      final SSAInstruction instruction = ebb.getInstruction();

	      return new IUnaryFlowFunction(){

			@Override
			public IntSet getTargets(int d1) {
				// TODO Auto-generated method stub
				
				MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();
				
				Pair<CGNode,Integer> pair = domain.getMappedObject(d1);
				CGNode node = pair.fst;
				int var = pair.snd;
				
				//if this is ssa form, there is no deletion of taint info.
				result.add(d1);
				Debug.setDebuggable(this, true);
				if(node.equals(srcNode) && instruction != null){
					int defVar = calcPropagatedVariable(instruction,var);
					if(defVar != -1){
						int d2 = domain.add(Pair.make(srcNode, defVar));
//						System.out.println("#6CallInst: "+instruction + "\tset: "+defVar);
						result.add(d2);
					}
				}
				return result;
			}
	    	  
	      };
	    }

	    /**
	     * standard flow function from callee to caller; just identity
	     */
	    @Override
	    public IFlowFunction getReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> call,
	        BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest) {
	    	final SSAAbstractInvokeInstruction callInst = (SSAAbstractInvokeInstruction)call.getDelegate().getInstruction();
	    	final BasicBlockInContext<IExplodedBasicBlock> srcBlock = src;
	    	final BasicBlockInContext<IExplodedBasicBlock> callBlock = call;
	    	Debug.setDebuggable(this, true);
//	    	if(src.toString().contains("sendMessage") || src.toString().contains("getLocation")){
//	    	if(src.toString().contains("getLocation")){
//		        Debug.printMsg("#ReturnFlow!");
//		        Debug.printMsg("#src: "+src);
//		        Debug.printMsg("#dest: "+dest);
//		        Debug.printMsg("");
//	    	}
//	        Debug.temporaryStop();
	    	return new IUnaryFlowFunction(){

				@Override
				public IntSet getTargets(int d1) {
					// TODO Auto-generated method stub
					MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();
					
					Pair<CGNode,Integer> pair = domain.getMappedObject(d1);
					CGNode node = pair.fst;
					int var = pair.snd;
					
					//if this is ssa form, there is no deletion of taint info.
					result.add(d1);
					
					if(srcBlock.getNode().toString().contains("getLocation")){
//						System.out.println("#QQQ: " + node);
//						System.out.println("#NNN: " + srcBlock.getNode());
//						System.out.println("#TTT: " + callInst);
//						System.out.println("#V: " + var);
					}
					
					if(node.equals(srcBlock.getNode())){
						if(var == 17){
//							System.out.println(1);
						}
						
						if(callInst.hasDef()){
							if(var == 17){
//								System.out.println(2);
							}
							Iterator<BasicBlockInContext<IExplodedBasicBlock>> iPredBB = supergraph.getPredNodes(srcBlock);
//							System.out.println("\t#ReturnInst: "+callInst + "(" + var + ")");
							while(iPredBB.hasNext()){
								BasicBlockInContext<IExplodedBasicBlock> predBB = iPredBB.next();
								SSAInstruction inst = predBB.getDelegate().getInstruction();
								if(inst != null && inst instanceof SSAReturnInstruction){
									if(inst.getUse(0) == var){
										int d2 = domain.add(Pair.make(callBlock.getNode(), callInst.getDef()));
										result.add(d2);
									}
								}
							}
						}
					}
					return result;
				}
	    		
	    	};
	    }

	  }

	  /**
	   * Definition of the reaching definitions tabulation problem. Note that we choose to make the problem a <em>partially</em>
	   * balanced tabulation problem, where the solver is seeded with the putstatic instructions themselves. The problem is partially
	   * balanced since a definition in a callee used as a seed for the analysis may then reach a caller, yielding a "return" without a
	   * corresponding "call." An alternative to this approach, used in the Reps-Horwitz-Sagiv POPL95 paper, would be to "lift" the
	   * domain of putstatic instructions with a 0 (bottom) element, have a 0->0 transition in all transfer functions, and then seed the
	   * analysis with the path edge (main_entry, 0) -> (main_entry, 0). We choose the partially-balanced approach to avoid pollution of
	   * the flow functions.
	   * 
	   */
	  private class TaintProblem implements
	      PartiallyBalancedTabulationProblem<BasicBlockInContext<IExplodedBasicBlock>, CGNode, Pair<CGNode, Integer>> {

	    private TaintFlowFunctions flowFunctions = new TaintFlowFunctions(domain);

	    /**
	     * path edges corresponding to all putstatic instructions, used as seeds for the analysis
	     */
	    private Collection<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> initialSeeds = collectInitialSeeds();

	    /**
	     * we use the entry block of the CGNode as the fake entry when propagating from callee to caller with unbalanced parens
	     */
	    @Override
	    public BasicBlockInContext<IExplodedBasicBlock> getFakeEntry(BasicBlockInContext<IExplodedBasicBlock> node) {
	      final CGNode cgNode = node.getNode();
	      return getFakeEntry(cgNode);
	    }

	    /**
	     * we use the entry block of the CGNode as the "fake" entry when propagating from callee to caller with unbalanced parens
	     */
	    private BasicBlockInContext<IExplodedBasicBlock> getFakeEntry(final CGNode cgNode) {
	      BasicBlockInContext<IExplodedBasicBlock>[] entriesForProcedure = supergraph.getEntriesForProcedure(cgNode);
	      assert entriesForProcedure.length == 1;
	      return entriesForProcedure[0];
	    }

	    /**
	     * collect the putstatic instructions in the call graph as {@link PathEdge} seeds for the analysis
	     */
	    private Collection<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> collectInitialSeeds() {
	      Collection<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> result = HashSetFactory.make();
	      for (BasicBlockInContext<IExplodedBasicBlock> bb : supergraph) {
	        IExplodedBasicBlock ebb = bb.getDelegate();
	        SSAInstruction instruction = ebb.getInstruction();
	        if (instruction instanceof SSAAbstractInvokeInstruction) {
	        	SSAAbstractInvokeInstruction invokeInstr = (SSAAbstractInvokeInstruction) instruction;
	        	final CGNode cgNode = bb.getNode();
	          if (!invokeInstr.isStatic() && invokeInstr.hasDef() && isSourceTarget(cgNode,invokeInstr)) {
	            Pair<CGNode, Integer> fact = Pair.make(cgNode, invokeInstr.getDef());
	            int factNum = domain.add(fact);
	            BasicBlockInContext<IExplodedBasicBlock> fakeEntry = getFakeEntry(cgNode);
	            // note that the fact number used for the source of this path edge doesn't really matter
//	            result.add(PathEdge.createPathEdge(fakeEntry, factNum, bb, factNum));
	            /* new approach */
	            for(CGNode node : cg.getEntrypointNodes()){
	            	for(BasicBlockInContext<IExplodedBasicBlock> bBlock : supergraph.getEntriesForProcedure(node)){
	            		Iterator<BasicBlockInContext<IExplodedBasicBlock>> iBlock = supergraph.getSuccNodes(bBlock);
	            		while(iBlock.hasNext()){
	            			BasicBlockInContext<IExplodedBasicBlock> bbb = iBlock.next();
	            			result.add(PathEdge.createPathEdge(bBlock, factNum, bbb, factNum));
	            		}
	            	}
	            }
	            /* new approach */
	          }
	        }
	      }
	      Debug.setDebuggable(this, true);
	      Debug.printMsg("#Seed size: "+result.size());
	      return result;
	    }

	    @Override
	    public IPartiallyBalancedFlowFunctions<BasicBlockInContext<IExplodedBasicBlock>> getFunctionMap() {
	      return flowFunctions;
	    }

	    @Override
	    public TabulationDomain<Pair<CGNode, Integer>, BasicBlockInContext<IExplodedBasicBlock>> getDomain() {
	      return domain;
	    }

	    /**
	     * we don't need a merge function; the default unioning of tabulation works fine
	     */
	    @Override
	    public IMergeFunction getMergeFunction() {
	      return null;
	    }

	    @Override
	    public ISupergraph<BasicBlockInContext<IExplodedBasicBlock>, CGNode> getSupergraph() {
	      return supergraph;
	    }

	    @Override
	    public Collection<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> initialSeeds() {
	      return initialSeeds;
	    }

	  }

	  /**
	   * perform the tabulation analysis and return the {@link TabulationResult}
	   */
	  public TabulationResult<BasicBlockInContext<IExplodedBasicBlock>, CGNode, Pair<CGNode, Integer>> solve() {
	    PartiallyBalancedTabulationSolver<BasicBlockInContext<IExplodedBasicBlock>, CGNode, Pair<CGNode, Integer>> solver = PartiallyBalancedTabulationSolver
	        .createPartiallyBalancedTabulationSolver(new TaintProblem(), null);
	    TabulationResult<BasicBlockInContext<IExplodedBasicBlock>, CGNode, Pair<CGNode, Integer>> result = null;
	    try {
	      result = solver.solve();
	    } catch (CancelException e) {
	      // this shouldn't happen 
	      assert false;
	    }
//	    Debug.setDebuggable(this, false);
//	    for(BasicBlockInContext<IExplodedBasicBlock> reachedBB : result.getSupergraphNodesReached()){
//	    	Debug.printMsg("#BB: "+reachedBB);
//	    	Debug.printMsg("\t"+reachedBB.getDelegate().getInstruction());
//	    	Debug.printMsg();
//	    }
	    return result;

	  }

	  public ISupergraph<BasicBlockInContext<IExplodedBasicBlock>, CGNode> getSupergraph() {
	    return supergraph;
	  }

	  public TabulationDomain<Pair<CGNode, Integer>, BasicBlockInContext<IExplodedBasicBlock>> getDomain() {
	    return domain;
	  }
}
