/*******************************************************************************
* Copyright (c) 2016 IBM Corporation and KAIST.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* KAIST - initial API and implementation
*******************************************************************************/
package kr.ac.kaist.wala.hybridroid.command;

import org.apache.commons.cli.*;

public class CommandArguments {
	static private Options options;
	static final public String PROP_ARG = "p";
	static final public String PROP_LONG_ARG = "property";
	static final public String MODEL_ARG = "m";
	static final public String MODEL_LONG_ARG = "model";
	static final public String TARGET_ARG = "t";
	static final public String TARGET_LONG_ARG = "target";
	static final public String DROIDEL_ARG = "droidel";
	static final public String MANIFEST_ARG = "manifest";
	static final public String CFG_ARG = "cfg";
	static final public String CFG_LONG_ARG = "cfg";
	static final public String PRE_STRING_ARG = "prestr";
	static final public String ONLY_JS_ARG = "j";
	static final public String ONLY_JS_LONG_ARG = "jsonly";
	
	static{
		options = new Options();

		Option targetOp = new Option(TARGET_ARG, TARGET_LONG_ARG,true, "target apk file of analysis");
		targetOp.setRequired(true);
		options.addOption(targetOp);

		Option propOp = new Option(PROP_ARG, PROP_LONG_ARG, true, "set the wala property file");
		propOp.setRequired(true);
		options.addOption(propOp);

		Option modelOp = new Option(MODEL_ARG, MODEL_LONG_ARG, true, "set the wala property file");
		options.addOption(modelOp);

		Option jsOp = new Option(ONLY_JS_ARG, ONLY_JS_LONG_ARG, false, "set the wala property file");
		options.addOption(jsOp);

//		OptionGroup functions = new OptionGroup();
		Option cfgOp = new Option(CFG_ARG, CFG_LONG_ARG, false, "construct cfg for the android application");
		options.addOption(cfgOp);
//		options.addOptionGroup(functions);
////		options.addOption(new Option(PRE_STRING_ARG, false, "pre-analysis for "), hasArg, description)
//		options.addOption(new Option(DROIDEL_ARG, false, "enable pre transforming using DROIDEL"));
//		options.addOption(new Option(MANIFEST_ARG, false, "enable the manifest analysis"));
	}
	
	private CommandLine cmd;
	
	public void usage(){
		HelpFormatter help = new HelpFormatter();
		help.printHelp("command line options", options);
	}
	
	public CommandArguments(String[] args){
		CommandLineParser parser = new BasicParser();
		try {
			cmd = parser.parse(options, args, false);
			
			if(!computeDependency()){
				System.out.println("124555");
				usage();
				System.exit(-1);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			System.out.println("124");
			usage();
			System.exit(-1);
		}
	}
	
	private boolean computeDependency(){
		if(!cmd.hasOption(CFG_ARG) || !cmd.hasOption(PROP_ARG) || !cmd.hasOption(TARGET_ARG)){
			return false;
		}else
			return true;
	}
	
	public boolean has(String op){
		return cmd.hasOption(op);
	}
	
	public String get(String op){
		return cmd.getOptionValue(op);
	}
}
