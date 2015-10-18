package kr.ac.kaist.hybridroid.soot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.ArgumentHotspot;
import kr.ac.kaist.hybridroid.soot.phantom.PhantomClassManager;
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
	
	static private String JAVA_ENV = "/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/jre/lib/jce.jar";
	private boolean isLocked = false;
	private Set<String> includedPacks;
	
	static private String[] excludeAppPacks = {
		"com.inmobi",
		"com.google",
		"com.mopub",
		"com.nineoldandroids",
		"com.millennialmedia",
		"roboguice",
		"com.actionbarsherlock",
		"com.fasterxml",
		"com.facebook",
		"android",
		"com.bolts",
		"com.flurry",
		"com.viewpagerindicator",
		"com.jakewharton",
		"bolts",
		"com.ctrlplusz",
		"org.slf4j",
		"org.apache",
		"com.makeramen",
		"se.emilsjolander",
		"com.sothree",
		"com.crittercism",
		"com.tonicartos",
		"com.amazon",
		"com.android",
		"crittercism",
		
		
	};
	
	public SootBridge(){
		includedPacks = new HashSet<String>();
		initJava();
		Options.v().set_keep_line_number(true);
		Options.v().set_whole_program(true);
        // LWG
//		Options.v().setPhaseOption("jb", "use-original-names:true");
//		Options.v().setPhaseOption("cg", "verbose:false");
//		Options.v().setPhaseOption("cg", "trim-clinit:true");
		 //soot.options.Options.v().setPhaseOption("jb.tr", "ignore-wrong-staticness:true");
		 				 		
		 // don't optimize the program 
//		Options.v().setPhaseOption("wjop", "enabled:false");
		 // allow for the absence of some classes
		Options.v().set_allow_phantom_refs(true);
		 
//		Options.v().set_ignore_resolution_errors(true);

		Options.v().set_src_prec(Options.src_prec_apk);
//		Options.v().set_verbose(true);
//		Options.v().set_app(true);
//		PhaseOptions.v().setPhaseOption("cg.cha", "verbose");
	}
	
	private void initJava(){
		Options.v().set_soot_classpath(JAVA_ENV);
	}
	
	public void addDirScope(String dir) throws IOException{
		checkLocking(false, "SootBridge is already locked.");
		
		File dirFile = new File(dir);
		
		if(!dirFile.exists())
			Assertions.UNREACHABLE("The file does not exist.");
		
		if(!dirFile.isDirectory())
			Assertions.UNREACHABLE("The file is not a directory.");
		
		List<String> dirList = Options.v().process_dir();
		if(dirList.isEmpty()){
			dirList = new ArrayList<String>();
			Options.v().set_process_dir(dirList);
		}
			
		dirList.add(dirFile.getCanonicalPath());
	}
	
	public void setAndroidJar(String jar) throws IOException{
		checkLocking(false, "SootBridge is already locked.");
		
		File jarFile = new File(jar);
		if(!jar.endsWith(".jar"))
			Assertions.UNREACHABLE("The file is not 'Jar' format.");
		
		if(!jarFile.exists())
			Assertions.UNREACHABLE("The file does not exist.");
		
		Options.v().set_android_jars(jar);
		
		System.err.println("@AND_JAR: " + jarFile.getCanonicalPath());
	}
	
	public void setTargetApk(String apk) throws IOException{
		checkLocking(false, "SootBridge is already locked.");
		
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
		
		System.err.println("@APK_TARGET: " + apkFile.getCanonicalPath());
	}
	
	public void lock(){
		if(isLocked == false){
			System.err.println("CLASSPATH: " + Scene.v().getSootClassPath());
			isLocked = true;
			Scene.v().loadNecessaryClasses();
			exclude();
			PhantomClassManager.getInstance(Scene.v().getPhantomClasses()).setCommonSuperClass(Scene.v().getSootClass("java.lang.Object"));
		}
	}
	
	private void exclude(){
		List<SootClass> appClasses = new ArrayList<SootClass>();
		
		for(SootClass cls : Scene.v().getApplicationClasses()){
			boolean isExcluded = false;
			for(String excludePack : excludeAppPacks)
				if(cls.getPackageName().startsWith(excludePack))
					isExcluded = true;
			if(!isExcluded){
				appClasses.add(cls);
				String packName = cls.getPackageName();
				if(!includedPacks.contains(packName))
					includedPacks.add(packName);
			}
		}
		
		Scene.v().getApplicationClasses().clear();
		for(SootClass cls : appClasses)
			Scene.v().getApplicationClasses().add(cls);
		
		for(String packName : includedPacks){
			System.out.println("#Include: " + packName);
		}
	}
	
	public List<ValueBox> getArgumentHotspots(ArgumentHotspot argHotspot){
		checkLocking(true, "SootBridge must be locked before analysis.");
		
		String methodName = argHotspot.getMethodName();
		int paramNum = argHotspot.getParamNum();
		int argIndex = argHotspot.getArgIndex();
		
		if(paramNum < argIndex + 1)
			Assertions.UNREACHABLE("Parameter number must be bigger than argument index + 1.");
		
		List<ValueBox> hotspots = new ArrayList<ValueBox>();
				
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
								hotspots.add(hotspot);
							}
						}
					}
				}
			}
		}
		return hotspots;
	}
	
	public Chain<SootClass> getAllClasses(){
		checkLocking(true, "SootBridge must be locked before analysis.");
		return Scene.v().getClasses();
	}
	
	public List<SootMethod> getAllMethod(SootClass c){
		checkLocking(true, "SootBridge must be locked before analysis.");
		return c.getMethods();
	}
	
	public Body getMethodBody(SootMethod m){
		checkLocking(true, "SootBridge must be locked before analysis.");
		return m.retrieveActiveBody();
	}
	
	public PatchingChain<Unit> getStmts(Body b){
		checkLocking(true, "SootBridge must be locked before analysis.");
		return b.getUnits();
	}
	
	public PatchingChain<Unit> getStmts(SootMethod m){
		return getMethodBody(m).getUnits();
	}
	
	private void checkLocking(boolean check, String errMsg){
		if(isLocked != check)
			Assertions.UNREACHABLE(errMsg);
	}
}
