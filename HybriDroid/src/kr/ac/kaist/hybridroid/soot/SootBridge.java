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
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.util.Chain;

import com.ibm.wala.util.debug.Assertions;

public class SootBridge {
	
	private int localID = 0;
	private boolean isClassLoaded = false;
	
	public SootBridge(){
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_whole_program(true);
//		Options.v().set_verbose(true);
		Options.v().set_allow_phantom_refs(true);
//		Options.v().set_app(true);
//		PhaseOptions.v().setPhaseOption("cg.cha", "verbose");
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
	
	public void setAndroidJar(String jar){
		File jarFile = new File(jar);
		if(!jar.endsWith(".jar"))
			Assertions.UNREACHABLE("The file is not 'Jar' format.");
		
		if(!jarFile.exists())
			Assertions.UNREACHABLE("The file does not exist.");
		
		Options.v().set_android_jars(jar);
	}
	
	public void setJavaEnv(String jar){
		jar = "/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/jre/lib/jce.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/jre/lib/charsets.jar";
//		File jarFile = new File(jar);
//		
//		if(!jar.endsWith(".jar"))
//			Assertions.UNREACHABLE("The file is not 'Jar' format.");
//		
//		if(!jarFile.exists())
//			Assertions.UNREACHABLE("The file does not exist.");
		
		Options.v().set_soot_classpath(jar);
	}
	
	public void setTargetApk(String apk) throws IOException{
		File apkFile = new File(apk);
		
		if(!apk.endsWith(".apk"))
			Assertions.UNREACHABLE("The file is not 'dex' format.");
		
		if(!apkFile.exists())
			Assertions.UNREACHABLE("The file does not exist: " + apkFile.getCanonicalPath());
		
		List<String> targets = Options.v().process_dir();
		if(targets.isEmpty()){
			targets = new ArrayList<String>();
			Options.v().set_process_dir(targets);
		}
		targets.add(apk);
//		Options.v().set_soot_classpath(apkFile.getCanonicalPath());
	}
	
	public List<ValueBox> getHotspots(String methodName, int paramNum, int argIndex){
		if(paramNum < argIndex + 1)
			Assertions.UNREACHABLE("Parameter number must be bigger than argument index + 1.");
		
		List<ValueBox> hotspots = new ArrayList<ValueBox>();
		
		if(!isClassLoaded){
			isClassLoaded = true;
			Scene.v().loadNecessaryClasses();
		}
		
		for(SootClass sootclass : Scene.v().getApplicationClasses()){
			if(sootclass.getJavaPackageName().startsWith("android") || sootclass.getJavaPackageName().startsWith("google"))
				continue;
			for(SootMethod sootmethod : sootclass.getMethods()){
				if(sootmethod.isConcrete()){
					for(Unit unit : this.getStmts(sootmethod)){
						Stmt stmt = (Stmt)unit;
						if(stmt.containsInvokeExpr()){
							InvokeExpr invoke = stmt.getInvokeExpr();
							if(invoke.getMethod().getName().equals(methodName) && invoke.getMethod().getParameterCount() == paramNum){
								ValueBox hotspot = invoke.getArgBox(argIndex);
								System.out.println("hotspot: " + invoke.getMethod().getSignature());
								System.out.println("hotspot: " + hotspot);
								hotspots.add(hotspot);
							}
						}
					}
				}
			}
		}
		return hotspots;
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
