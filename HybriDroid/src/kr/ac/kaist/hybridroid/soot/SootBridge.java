package kr.ac.kaist.hybridroid.soot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;
import soot.util.Chain;

import com.ibm.wala.util.debug.Assertions;

public class SootBridge {
	private List<String> args;		
	public SootBridge(){
		args = new ArrayList<String>();
		Options.v().set_src_prec(Options.src_prec_java);
		args.add("-w");
		args.add("-android-jars");
		args.add("../../sdk");
//		Options.v().set_whole_program(true);
	}
	
	public void addDirScope(String dir) throws IOException{
		File dirFile = new File(dir);
		
		if(!dirFile.exists())
			Assertions.UNREACHABLE("The file does not exist.");
		
		if(!dirFile.isDirectory())
			Assertions.UNREACHABLE("The file is not a directory.");
		
		@SuppressWarnings("unchecked")
		List<String> dirList = Options.v().process_dir();
		if(dirList.isEmpty()){
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
		List<String> includeList = (List<String>) Options.v().include();
		if(includeList.isEmpty()){
			includeList = new ArrayList<String>();
			Options.v().set_include(includeList);
		}
		args.add("-src-prec format ");
		args.add("apk");
		args.add("-process-dir");
		args.add(jarFile.getCanonicalPath());
		includeList.add(jarFile.getCanonicalPath());
	}
	
	public void addDexScope(String apk) throws IOException{
		File apkFile = new File(apk);
		
		if(!apk.endsWith(".apk"))
			Assertions.UNREACHABLE("The file is not 'dex' format.");
		
		if(!apkFile.exists())
			Assertions.UNREACHABLE("The file does not exist: " + apkFile.getCanonicalPath());
		
		@SuppressWarnings("unchecked")
		List<String> includeList = Options.v().include();
		if(includeList.isEmpty()){
			includeList = new ArrayList<String>();
			Options.v().set_include(includeList);
		}
		
		includeList.add(apkFile.getCanonicalPath());
	}
	
	public CallGraph getCallGraph(){
		PackManager.v().getPack("cg").add(
			      new Transform("cg.myTransform", new SceneTransformer() {
			        protected void internalTransform(String phaseName,
			            Map options) {
			          System.err.println(Scene.v().getCallGraph());
			        }
			      }));
		soot.Main.main(convert2StrArray(args));
		return Scene.v().getCallGraph();
	}
	
	private String[] convert2StrArray(List<String> list){
		String[] strArray = new String[list.size()];
		for(int i=0; i<list.size(); i++)
			strArray[i] = list.get(i);
		return strArray;
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
