/**
 * 
 */
package kr.ac.kaist.wala.hybridroid.apifinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

import kr.ac.kaist.wala.hybridroid.types.bridge.BridgeInfo;

/**
 * @author Sungho Lee
 *
 */
public class Shell {
	
	static public void main(String[] args){
		APIFinder d = new APIFinder();
		Properties p;
		File propFile = new File(args[1]);
		p = new Properties();
		try {
			p.load(new FileInputStream(propFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Set<BridgeInfo> res;
		try {
			res = d.findAccessibleApis(args[0], p);
			System.out.println("BREF: " + res);
		} catch (ClassHierarchyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
