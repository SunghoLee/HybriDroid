/**
 * 
 */
package kr.ac.kaist.wala.hybridroid.types;

/**
 * @author Sungho Lee
 *
 */
public class Shell {
	
	static public void main(String[] args){
		Driver d = new Driver();
		System.out.println("TT: " + d.analyzeBridgeMapping(args[0], args[1]));
	}
}
