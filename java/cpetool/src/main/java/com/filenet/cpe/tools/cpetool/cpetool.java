package com.filenet.cpe.tools.cpetool;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.filenet.cpe.tools.cpetool.managers.ImagingManager;
import com.filenet.cpe.tools.cpetool.managers.WorkflowManager;

@Component
public class cpetool implements CommandLineRunner {

	private static List<String> arguments;
	
	//Autowireds
	@Autowired
	private ImagingManager im;
	
	@Autowired
	private WorkflowManager wm;
	
			
	public cpetool() {
		super();
		arguments = new ArrayList<String>();
	}

	//Getters and Setters
	public List<String> getArguments() {
		return arguments;
	}

	public void setArguments(List<String> arguments) {
		cpetool.arguments = arguments;
	}
	
	//Custom Methods
	/*
	 * public void loadArgument(String arg) { if (arg != null && arg.length() > 0) {
	 * //Add Argument to the List this.arguments.add(arg); } }
	 */
	
	//Used to determine which Imaging or Workflow method to run
	/*
	 * public String execute() { String result = ""; //im = new ImagingManager();
	 * im.connectionTest();
	 * 
	 * return result; }
	 */

	@Override
	public void run(String... args) throws Exception {
//		if (args != null && args.length > 0) {
//			for (String arg : args) {
//				//Add Argument to the List
//				this.arguments.add(arg);
//			}
//		}
		//Test Connection to FileNet P8 Imaging Manager
		im.connectionTest();
		//Test Connection to FileNet P8 Workflow Manager
		wm.connectionTest();
		
		
		//Added to force this run method to only run one time!
		System.exit(0);
	}

	
}
