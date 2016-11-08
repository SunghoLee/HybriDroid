/**
 * 
 */
package kr.ac.kaist.wala.hybridroid.types;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import kr.ac.kaist.wala.hybridroid.types.bridge.BridgeInfo;

/**
 * @author Sungho Lee
 *
 */
public class Shell {
	
	static public void main(String[] args){
		Driver d = new Driver();
		
		Map<File, Set<BridgeInfo>> m = d.analyzeBridgeMapping(args[0], args[1]);
		JSONOut out = new JSONOut(m);
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(args[2]));
			bw.write(out.toJSONString());
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
