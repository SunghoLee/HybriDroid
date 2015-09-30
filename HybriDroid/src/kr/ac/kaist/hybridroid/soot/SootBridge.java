package kr.ac.kaist.hybridroid.soot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;
import soot.util.Chain;

import com.ibm.wala.util.debug.Assertions;

public class SootBridge {
			
	public SootBridge(){
		Options.v().set_whole_program(true);
	}
	
	public void addDirScope(String dir) throws IOException{
		File dirFile = new File(dir);
		
		if(!dirFile.exists())
			Assertions.UNREACHABLE("The file does not exist.");
		
		if(!dirFile.isDirectory())
			Assertions.UNREACHABLE("The file is not a directory.");
		
		@SuppressWarnings("unchecked")
		List<String> dirList = Options.v().process_dir();
		if(dirList == null){
			dirList = new ArrayList<String>();
			Options.v().set_process_dir(dirList);
		}
			
		dirList.add(dirFile.getCanonicalPath());
	}
	
	public void addJarScope(String jar) throws IOException{
		File jarFile = new File(jar);
		
		if(!jar.endsWith(".jar"))
			Assertions.UNREACHABLE("The file is not 'Jar' format.");
		
		if(!jarFile.exists())
			Assertions.UNREACHABLE("The file does not exist.");
		
		@SuppressWarnings("unchecked")
		List<String> includeList = Options.v().include();
		if(includeList == null){
			includeList = new ArrayList<String>();
			Options.v().set_include(includeList);
		}
		
		includeList.add(jarFile.getCanonicalPath());
	}
	
	public void addDexScope(String dex) throws IOException{
		File dexFile = new File(dex);
		
		if(!dex.endsWith(".dex"))
			Assertions.UNREACHABLE("The file is not 'dex' format.");
		
		if(!dexFile.exists())
			Assertions.UNREACHABLE("The file does not exist.");
		
		@SuppressWarnings("unchecked")
		List<String> includeList = Options.v().include();
		if(includeList == null){
			includeList = new ArrayList<String>();
			Options.v().set_include(includeList);
		}
		
		includeList.add(dexFile.getCanonicalPath());
	}
	
	public CallGraph getCallGraph(){
		return Scene.v().getCallGraph();
	}
	
	public Chain<SootClass> getAllClasses(){
		return Scene.v().getClasses();
	}
	
	public List<SootMethod> getAllMethod(SootClass c){
		return c.getMethods();
	}
	
	public Body getMethodBody(SootMethod m){
		return m.retrieveActiveBody();
	}
	
	public PatchingChain<Unit> getStmts(Body b){
		return b.getUnits();
	}
	
	public PatchingChain<Unit> getStmts(SootMethod m){
		return getMethodBody(m).getUnits();
	}
}
