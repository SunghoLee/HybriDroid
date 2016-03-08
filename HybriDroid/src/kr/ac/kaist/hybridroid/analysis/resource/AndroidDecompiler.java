package kr.ac.kaist.hybridroid.analysis.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import com.ibm.wala.properties.WalaProperties;

import kr.ac.kaist.hybridroid.shell.Shell;

public class AndroidDecompiler {
	public static String decompile(String apk){
		if(!apk.endsWith(".apk"))
			throw new InternalError("only support apk file : " + apk);
		File apkFile = new File(apk);
		if(!apkFile.exists())
			throw new InternalError("the file does not exist : " + apk);

		try{
			String path = apkFile.getCanonicalPath();
			String toPath = path.substring(0, path.length()-4);
			String[] cmds = {"-f", "d", path, "-o", toPath};
//			String apktool = Shell.walaProperties.getProperty(WalaProperties.ANDROID_APK_TOOL);
//			File f = new File(apktool);
//			if(!f.exists() || !f.isFile()){
//				throw new InternalError("Cannot find APK tool: " + apktool);
//			}
//			String[] cmd = {"java", "-jar", apktool, "-f", "d", path, "-o", toPath};
//			ProcessBuilder pb = new ProcessBuilder();
//			pb.command(cmd);
////			System.out.println(pb.command().toString());
//			Process p = pb.start();
//			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
//			BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//			
//			String r = null;
//			while((r = br.readLine()) != null){
//				System.out.println(r);
//			}
//			
//			while((r = bre.readLine()) != null){
//				System.err.println(r);
//			}
//			
//			int res = p.waitFor();
//			if(res != 0){
//				throw new InternalError("failed to decompile: " + path);
//			}
			brut.apktool.Main.main(cmds);
			return toPath;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
