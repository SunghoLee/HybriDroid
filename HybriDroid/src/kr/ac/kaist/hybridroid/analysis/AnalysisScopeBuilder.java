package kr.ac.kaist.hybridroid.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import kr.ac.kaist.hybridroid.callgraph.AndroidHybridAnalysisScope;
import kr.ac.kaist.hybridroid.shell.Shell;
import kr.ac.kaist.hybridroid.util.files.LocalFileReader;
import kr.ac.kaist.hybridroid.util.print.AsyncPrinter;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.properties.WalaProperties;

public class AnalysisScopeBuilder {
	private File target;
	private boolean flag;
	
	static public AnalysisScopeBuilder build(File target, boolean droidelFlag){
		return ((droidelFlag)? buildDroidelAnalysisScopeBuilder(target) : new AnalysisScopeBuilder(target));
	}
	
	private AnalysisScopeBuilder(File target){
		this.target = target;
	}
	
	private AnalysisScopeBuilder(File target, boolean flag){
		this.target = target;
		this.flag = flag;
	}
	
	private static void removeDestinationFolder(String path){
		File folder = new File(path);
		if(folder.exists() && folder.isDirectory()){
			System.err.println(path + " exists. Try to delete the folder.");
			String[] command = {"rm", "-r", folder.getAbsolutePath()};
			ProcessBuilder pb = new ProcessBuilder(command);
			int result = -1;
			try {
				Process p = pb.start();
				AsyncPrinter inputPrinter = new AsyncPrinter(p.getInputStream(), AsyncPrinter.PRINT_OUT);
				AsyncPrinter errorPrinter = new AsyncPrinter(p.getErrorStream(), AsyncPrinter.PRINT_ERR);
				
				inputPrinter.start();
				errorPrinter.start();
				
				result = p.waitFor();
				System.err.println("result: " + result);
				
				inputPrinter.interrupt();
				errorPrinter.interrupt();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(result == 0)
				System.err.println("Deletion is succeed.");
		}
	}
	
	private static AnalysisScopeBuilder buildDroidelAnalysisScopeBuilder(File target){
		System.err.println("[DROIDEL] transforming " + target.getName());
		String droidel_path = Shell.walaProperties.getProperty(WalaProperties.DROIDEL_TOOL);
		System.err.println("#DROIDEL path: " + droidel_path);
		
		removeDestinationFolder(target.getAbsolutePath().substring(0, target.getAbsolutePath().length()-4));
		String[] command = {"sh", "droidel.sh", "-app", target.getAbsolutePath(), "-android_jar", LocalFileReader.droidelAndroidLib(Shell.walaProperties).getAbsolutePath()};
		ProcessBuilder pb = new ProcessBuilder(command);
		pb = pb.directory(new File(droidel_path));
		Map<String,String> envMap = pb.environment();
		envMap.put("PATH", envMap.get("PATH") + ":" + "/opt/local/bin/");
		
		try {
			final Process p = pb.start();
			int result = -1;
			
			AsyncPrinter inputPrinter = new AsyncPrinter(p.getInputStream(), AsyncPrinter.PRINT_OUT);
			AsyncPrinter errorPrinter = new AsyncPrinter(p.getErrorStream(), AsyncPrinter.PRINT_ERR);
			
			inputPrinter.start();
			errorPrinter.start();
			
			result = p.waitFor();
			System.err.println("result: " + result);
			
			inputPrinter.interrupt();
			errorPrinter.interrupt();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println("[DROIDEL] done.");
		
		return new AnalysisScopeBuilder(target);
	}
		
	public AndroidHybridAnalysisScope makeScope() throws IOException{
		if(flag){
			String targetpath = target.getCanonicalPath();
			return AndroidHybridAnalysisScope.setUpDroidelAnalysisScope(target.toURI(),
					CallGraphTestUtil.REGRESSION_EXCLUSIONS, targetpath.substring(0, targetpath.lastIndexOf("/")), LocalFileReader.androidDexLibs(Shell.walaProperties));
		}else{
			return AndroidHybridAnalysisScope.setUpAndroidAnalysisScope(target.toURI(),
					CallGraphTestUtil.REGRESSION_EXCLUSIONS, LocalFileReader.androidDexLibs(Shell.walaProperties));
		}
	}
}
