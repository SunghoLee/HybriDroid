package kr.ac.kaist.hybridroid.command;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandArguments {
	static private Options options;
	static final public String PROP_ARG = "p";
	static final public String MODEL_ARG = "m";
	static final public String TARGET_ARG = "t";
	static final public String DROIDEL_ARG = "droidel";
	static final public String MANIFEST_ARG = "manifest";
	static final public String CFG_ARG = "cfg";
	static final public String PRE_STRING_ARG = "prestr";
	static final public String ONLY_JS_ARG = "jsonly";
	
	static{
		options = new Options();
		
		Option targetOp = new Option(TARGET_ARG, true, "target apk file of analysis");
		targetOp.setRequired(true);
		options.addOption(targetOp);
		
		Option propOp = new Option(PROP_ARG, true, "set the wala property file");
		propOp.setRequired(true);
		options.addOption(propOp);
		
		Option modelOp = new Option(MODEL_ARG, true, "set the wala property file");
		options.addOption(modelOp);
		
		Option jsOp = new Option(ONLY_JS_ARG, false, "set the wala property file");
		options.addOption(jsOp);
		
		OptionGroup functions = new OptionGroup();
		functions.addOption(new Option(CFG_ARG, false, "construct cfg for the android application"));
		options.addOptionGroup(functions);
//		options.addOption(new Option(PRE_STRING_ARG, false, "pre-analysis for "), hasArg, description)
		options.addOption(new Option(DROIDEL_ARG, false, "enable pre transforming using DROIDEL"));
		options.addOption(new Option(MANIFEST_ARG, false, "enable the manifest analysis"));
	}
	
	private CommandLine cmd;
	
	public void usage(){
		HelpFormatter help = new HelpFormatter();
		help.printHelp("command line options", options);
	}
	
	public CommandArguments(String[] args){
		CommandLineParser parser = new BasicParser();
		try {
			cmd = parser.parse(options, args);
			
			if(!computeDependency()){
				usage();
				System.exit(-1);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			usage();
			System.exit(-1);
		}
	}
	
	private boolean computeDependency(){
		if(cmd.hasOption(DROIDEL_ARG) || cmd.hasOption(MANIFEST_ARG)){
			if(!cmd.hasOption(CFG_ARG)){
				return false;
			}else
				return true;
		}
		return true;
	}
	
	public boolean has(String op){
		return cmd.hasOption(op);
	}
	
	public String get(String op){
		return cmd.getOptionValue(op);
	}
}
