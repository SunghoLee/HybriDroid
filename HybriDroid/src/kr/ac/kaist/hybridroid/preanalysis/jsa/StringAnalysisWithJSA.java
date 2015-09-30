package kr.ac.kaist.hybridroid.preanalysis.jsa;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import kr.ac.kaist.hybridroid.soot.SootBridge;
import soot.ValueBox;

import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.debug.Assertions;

import dk.brics.string.StringAnalysis;

public class StringAnalysisWithJSA {
	
	private SootBridge bridge;
	
	public StringAnalysisWithJSA(){
		bridge = new SootBridge();
	}
	
	public void addAnalysisScope(String path) throws IOException{
		File file = new File(path);
		if(!file.exists())
			Assertions.UNREACHABLE("File is not exist: " + path);
		
		if(file.isDirectory())
			bridge.addDirScope(path);
		else
			if(path.endsWith(".dex"))
				bridge.addDexScope(path);
			else if(path.endsWith(".jar"))
				bridge.addJarScope(path);
			else
				Assertions.UNREACHABLE("The file format can not be readable: " + file.getName());			
	}
	
	public void addHotspot(MethodReference mr){
		
	}
	
	public void addHotspots(Set<MethodReference> mrSet){
		
	}
	
	public void analyze(){
		StringAnalysis analzer = new StringAnalysis();
	}
	
//	private Collection<ValueBox> makeHotspots(){
//		
//		
//	}
}
