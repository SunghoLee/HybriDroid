package kr.ac.kaist.hybridroid.util.print;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Print messages from input stream asynchronously. This supports two type of
 * message; Standard-out and Standard-error. 
 * 
 * @author Sungho Lee
 */
public class AsyncPrinter extends Thread {
	static public final int PRINT_OUT = 1;
	static public final int PRINT_ERR = 2;

	private BufferedReader reader;
	private PrintStream printer;
	
	/**
	 * Unique constructor for AsyncPrinter.
	 * @param reader the input stream for printing. 
	 * @param type the type of printing. now, only support standard-out and standard-error.
	 */
	public AsyncPrinter(InputStream reader, int type){
		this.reader = new BufferedReader(new InputStreamReader(reader));
		setPrinter(type);
	}
	
	/**
	 * Set printing type for AsyncPrinter. 
	 * @param type the type of printing. now, only support standard-out and standard-error.
	 */
	private void setPrinter(int type){
		switch(type){
		case PRINT_OUT:
			printer = System.out;
			break;
		case PRINT_ERR:
			printer = System.err;
			break;
		default:
				
		}
	}
	
	/**
	 * Start printing messages from the input stream. Must stop this operation
	 * using stop method when no more need to print messages for preventing
	 * the resource waste.
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		String s;

		try {
			while ((s = reader.readLine()) != null)
				printer.println(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
