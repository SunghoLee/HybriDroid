package com.ibm.wala.cast.ipa.callgraph;

import com.ibm.wala.cast.ipa.callgraph.ScopeMappingInstanceKeys.ScopeMappingInstanceKey;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextItem;
import com.ibm.wala.ipa.callgraph.ContextKey;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

public class ScopeMappingKeysContextSelector implements ContextSelector {
  
  public static final ContextKey scopeKey = new ContextKey() {
    public String toString() { return "SCOPE KEY"; }
  };
  
  public static class ScopeMappingContext implements Context {
    private final Context base;
    private final ScopeMappingInstanceKey key;
    
    private ScopeMappingContext(Context base, ScopeMappingInstanceKey key) {
      this.base = base;
      this.key = key;
    }

    public ContextItem get(ContextKey name) {
      if (scopeKey.equals(name)) {
        return key;
      } else {
        return base.get(name);
      }
    }
 
    public int hashCode() {
      return base.hashCode()*key.hashCode();
    }
    
    public String toString() {
      return "context for " + key;
    }
    
    public boolean equals(Object o) {
      return (o instanceof ScopeMappingContext) &&
             key.equals(((ScopeMappingContext)o).key) &&
             base.equals(((ScopeMappingContext)o).base);
    }
  }

  private final ContextSelector base;
  
  public ScopeMappingKeysContextSelector(ContextSelector base) {
    this.base = base;
  }
  
  public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey receiver) {
    Context bc = base.getCalleeTarget(caller, site, callee, receiver);
    if (receiver instanceof ScopeMappingInstanceKey) {
      return new ScopeMappingContext(bc, (ScopeMappingInstanceKey) receiver);
    } else {
      return bc;
    }
  }
}
