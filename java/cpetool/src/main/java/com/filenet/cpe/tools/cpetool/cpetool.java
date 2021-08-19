package com.filenet.cpe.tools.cpetool;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.filenet.cpe.tools.cpetool.managers.ImagingManager;
import com.filenet.cpe.tools.cpetool.managers.WorkflowManager;

@Component
public class cpetool implements CommandLineRunner {

	private List<String> arguments;
	
	private static Logger log = LoggerFactory.getLogger(cpetool.class);
	
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
		this.arguments = arguments;
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
		if (args != null && args.length > 0) {
			for (String arg : args) {
				//Check arguments and ignore spring.output argument
				if (!arg.contains("spring.output.ansi.enabled")) {
					//Add Argument to the List
					this.arguments.add(arg);
				}
			}
		}
		
		//Parameters
		//1. Type: Imaging or Workflow
		//2. Method: i.e. GetWorkflows -> getFnWorkflowList
		//3. Method Parameters: i.e. Step Name, Step Property, Step Value
		
		//Check the 1st argument for Imaging or Workflow
		if (arguments.get(0).equals("Imaging")) {
			log.info("Imaging Method");
			//Check the 2nd argument for Method
			if (arguments.get(1).equals("ConnectionTest")) {
				log.info("Connection Test");
				//Test Connection to FileNet P8 Imaging Manager
				im.connectionTest();
			} else if (arguments.get(1).equals("ConnectionTest")){
				
			} else {
				log.info("Invalid Imaging Method argument specified");
			}
		} else if (arguments.get(0).equals("Workflow")) {
			log.info("Workflow Method");
			//Check the 2nd argument for Method
			if (arguments.get(1).equals("ConnectionTest")) {
				log.info("Connection Test");
				//Test Connection to FileNet P8 Workflow Manager
				wm.connectionTest();
			} else if (arguments.get(1).equals("GetWorkflows")){
				log.info("GetWorkflows");
				//Check for proper parameters
				//Roster or Queue
				if (arguments.get(2) != null && !arguments.get(2).equals("")) {
					if (arguments.get(2).equals("Roster")) {
						log.info("Roster");
						//Argument 3 must be Roster Name
						//Argument 4 must be Parameter Name
						//Argument 5 must be Parameter Value
						//Roster Name, Queue Name, Parameter Name, Parameter Value
						//Perform Roster Query
						wm.getWorkflowInfo(arguments.get(3), "", "", arguments.get(4), arguments.get(5));
					} else if (arguments.get(2).equals("Queue")) {
						log.info("Queue");
						//Argument 3 must be Queue Name
						//Argument 4 must be Parameter Name
						//Argument 5 must be Parameter Value
						//Roster Name, Queue Name, Parameter Name, Parameter Value
						//Perform Queue Query
						//wm.getWorkflowInfo("", arguments.get(3), "", arguments.get(4), arguments.get(5));
						
						wm.getWorkflowList("", arguments.get(3), "", arguments.get(4), arguments.get(5), "");
						
						
					} else {
						log.info("Invalid argument - can only be Roster or Queue");
					}
				} else {
					log.info("Invalid argument - cannot be a null or empty value, must be Roster or Queue");
				}
			} else {
				log.info("Invalid Workflow Method argument specified");
			}
			
			//wm.getWorkflowInfo("GBER", "AGBR", "cpeadmin-dv", "plan_code", "ERS");
			//wm.getWorkflowInfo("", "", "cpeadmin-dv", "plan_code", "ERS");

		} else {
			log.info("Invalid argument - must be Imaging or Workflow");
		}
		
		
		
		//Added to force this run method to only run one time!
		System.exit(0);
	}

	
}
