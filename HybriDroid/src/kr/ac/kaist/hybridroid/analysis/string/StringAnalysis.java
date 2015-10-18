package kr.ac.kaist.hybridroid.analysis.string;

import java.util.List;

public interface StringAnalysis {
	
	public void addAnalysisScope(String path);
	public void analyze(List<Hotspot> hotspots) throws Exception;
}
