package kr.ac.kaist.hybridroid.util.debug;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.ibm.wala.util.debug.Assertions;


public class FileLogger{
	private File log;
	private BufferedWriter writer;
	
	public final static int INFO_LEVEL = 1;
	public final static int WARNING_LEVEL = 2;
	public final static int ERROR_LEVEL = 3;
	
	public FileLogger(String path) throws IOException{
		log = new File(path);
		if(log.exists()){
			log.delete();
			log.createNewFile();
		}
		writer = new BufferedWriter(new FileWriter(log));
	}
	
	private String levelToString(int level){
		String res = "";
		switch(level){
		case INFO_LEVEL:
			res = "[INFO]";
			break;
		case WARNING_LEVEL:
			res = "[WARNING]";
			break;
		case ERROR_LEVEL:
			res = "[ERROR]";
			break;
			default:
				Assertions.UNREACHABLE("undefined log level: " + level);
		}
		return res;
	}
	
	public void logging(String msg){
		try {
			writer.write(msg+"\n");
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void logging(String msg, int level){
		logging(levelToString(level) + " " + msg);
	}
	
	public void logging(String msg, int level, boolean console){
		logging(levelToString(level) + " " + msg, console);
	}
	
	public void logging(String msg, boolean console){
		logging(msg);
		if(console)
			System.err.println(msg);
	}
	
	public void close() throws IOException{
		writer.close();
	}
	
	@Override
	public void finalize() throws IOException{
		writer.close();
	}
}
