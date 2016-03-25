package kr.ac.kaist.hybridroid.util.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class FilePrinter {

	public static void print(File f, OutputStream os) throws InternalError{
		if(!f.exists()){
			try {
				throw new InternalError("the file does not exsit: " + f.getCanonicalPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		BufferedReader br = null;
		BufferedWriter bw = null;
		try{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			bw = new BufferedWriter(new OutputStreamWriter(os));
			
			String s = null;
			
			while((s = br.readLine()) != null){
				bw.write(s + "\n");
			}
			bw.flush();
		}catch(IOException e){
			
		}finally{
			try {
				if(br != null)
					br.close();
				if(bw != null)
					bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
