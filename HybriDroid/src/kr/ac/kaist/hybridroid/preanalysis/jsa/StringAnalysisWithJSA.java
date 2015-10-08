package kr.ac.kaist.hybridroid.preanalysis.jsa;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.ac.kaist.hybridroid.preanalysis.spots.ArgumentHotspot;
import kr.ac.kaist.hybridroid.preanalysis.spots.Hotspot;
import kr.ac.kaist.hybridroid.soot.SootBridge;
import soot.ValueBox;

import com.ibm.wala.util.debug.Assertions;

import dk.brics.automaton.Automaton;
import dk.brics.string.StringAnalysis;

public class StringAnalysisWithJSA {
	
	private SootBridge bridge;
	private StringAnalysis analyzer;
	private Map<Hotspot, List<ValueBox>> spotMap;
	public StringAnalysisWithJSA(){
		bridge = new SootBridge();
		spotMap = new HashMap<Hotspot, List<ValueBox>>();
	}
	
	public void addAnalysisScope(String path) throws IOException{
		File file = new File(path);
		if(!file.exists())
			Assertions.UNREACHABLE("File is not exist: " + path);
		
		if(file.isDirectory())
				bridge.addDirScope(path);
		else
			if(path.endsWith(".dex") || path.endsWith(".apk"))
				bridge.setTargetApk(path);
			else if(path.endsWith(".jar"))
				bridge.setAndroidJar(path);
			else
				Assertions.UNREACHABLE("The file format can not be readable: " + file.getName());			
	}
	
	private List<ValueBox> findHotspots(List<Hotspot> hotspots){
		List<ValueBox> sootHotspots = new ArrayList<ValueBox>();
		for(Hotspot hotspot : hotspots){
			if(hotspot instanceof ArgumentHotspot){
				List<ValueBox> boxes = bridge.getArgumentHotspots((ArgumentHotspot)hotspot);
				spotMap.put(hotspot, boxes);
				sootHotspots.addAll(boxes);
			}
		}
		return sootHotspots;
	}
	
	public void analyze(List<Hotspot> hotspots){
		bridge.lock();
		
		List<ValueBox> sootHotspots = findHotspots(hotspots);
		
		for(ValueBox box : sootHotspots){
			System.err.println("--HOTSPOT: " + box);
		}
		soot.options.Options.v().parse(new String[]{"-keep-line-number"});
		analyzer = new StringAnalysis(sootHotspots);
//		analyzer.get
	}
	
	public List<Automaton> getAutomaton(Hotspot hotspot){
		if(analyzer == null)
			throw new InternalError("must excute analyze method before getting automatons");
		if(!spotMap.containsKey(hotspot))
			throw new InternalError("the hotspot cannot be found: " + hotspot);
		
		List<ValueBox> boxes = spotMap.get(hotspot);
		List<Automaton> automatons = new ArrayList<Automaton>();
		for(ValueBox box : boxes){
			automatons.add(analyzer.getAutomaton(box));
		}
		return automatons;
	}
}
