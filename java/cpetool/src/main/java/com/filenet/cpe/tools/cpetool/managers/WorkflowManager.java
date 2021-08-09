package com.filenet.cpe.tools.cpetool.managers;

//import org.apache.sling.commons.json.JSONObject;
//import org.apache.sling.commons.json.JSONArray;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.filenet.cpe.tools.cpetool.configuration.AppConfiguration;

//import org.apache.commons.codec.binary.Base64;

import com.filenet.wcm.api.ObjectFactory;
import com.filenet.wcm.api.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//import com.microsoft.sqlserver.jdbc.SQLServerConnection;
//import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import filenet.vw.api.IVWtoXML;
import filenet.vw.api.VWAttachment;
import filenet.vw.api.VWAttachmentType;
import filenet.vw.api.VWException;
import filenet.vw.api.VWFetchType;
import filenet.vw.api.VWFieldType;
import filenet.vw.api.VWLibraryType;
import filenet.vw.api.VWModeType;
import filenet.vw.api.VWOperationDefinition;
import filenet.vw.api.VWParameter;
import filenet.vw.api.VWParticipant;
import filenet.vw.api.VWQueue;
import filenet.vw.api.VWQueueDefinition;
import filenet.vw.api.VWQueueElement;
import filenet.vw.api.VWQueueQuery;
import filenet.vw.api.VWRoster;
import filenet.vw.api.VWRosterElement;
import filenet.vw.api.VWRosterQuery;
import filenet.vw.api.VWSession;
import filenet.vw.api.VWStepElement;
import filenet.vw.api.VWSystemAdministration;
import filenet.vw.api.VWTransferResult;
import filenet.vw.api.VWWorkObject;
import filenet.vw.api.VWWorkObjectNumber;
import filenet.vw.api.VWWorkflowDefinition;
import filenet.vw.api.VWXMLConfiguration;

@Service("wm")
public class WorkflowManager {

	@Autowired
	private ImagingManager imagingManager;
	
	@Autowired
	private AppConfiguration appConfig;
	
	private static Logger log = LoggerFactory.getLogger(WorkflowManager.class);
	
	//******************************************************
	//Public Main Methods
	//******************************************************
	public String connectionTest()
	{
		String result = "";
		//Get a VWSession Object
		VWSession vwSession = new VWSession();

		try
		{
			log.info("Entered WorkflowManager -> connectionTest()");

			//Login to the Workflow Server
			vwSession = loginWorkflow();

			if (vwSession != null)
			{
				log.info("Logged in successfully to the Workflow Server");
				result = "Workflow Login SUCCESSFUL";
				//Logoff the Workflow Server
				log.info("Logging off the Workflow Server");
				vwSession.logoff();
				//Release the VWSession
				vwSession = null;
				log.info("Logged off");
			}
			else
			{
				log.info("Failed to login to the Workflow Server");
				result = "Workflow Login FAILED";
				//Release the VWSession
				vwSession = null;
				log.info("Logged off");
			}
		}
		catch(VWException e)
		{
			log.error("Error Message", e.getMessage());
			if (vwSession != null)
			{
				//Set vwSession to null to kill any connections
				vwSession = null;
			}
		}
		log.info("Leaving WorkflowManager -> connectionTest()");
		log.info("===========================================================");
		return result;
	}

	private VWSession loginWorkflow()
	{
		//Create a VWSession Object
		VWSession vwSession = new VWSession();

		try
		{
			log.info("Entered WorkflowManager -> loginWorkflow()");
			
			//Set the Bootstrap Content Engine URI
			log.info("Setting the Bootstrap CEURI");
			vwSession.setBootstrapCEURI(appConfig.getCpeConnectionURI());
			//Login to the Workflow Server
			log.info("Logging into the Workflow Server");
			vwSession.logon(appConfig.getCpeUsername(), appConfig.getCpePassword(), appConfig.getPeConnectionPoint());
			if (vwSession.isLoggedOn())
			{
				log.info("Logged in successfully to the Workflow Server");
			}
			else
			{
				log.info("Failed to login to the Workflow Server");
				//Set vwSession to null to kill any connections
				vwSession = null;
			}
		}
		catch(VWException e)
		{
			log.info("Failed to login to the Workflow Server");
			log.error("Error Message", e.getMessage());
			if (vwSession != null)
			{
				//Set vwSession to null to kill any connections
				vwSession = null;
			}
		}
		log.info("Leaving WorkflowManager -> loginWorkflow()");
		log.info("===========================================================");
		return vwSession;
	}
	
	
	
	
	//Export the Workflow Configuration XML
//	public FnWorkflow exportConfigXML(WIISCLog wiiscLog)
//	{
//		FnWorkflow fnWorkflow = new FnWorkflow();
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> exportConfigXML()");
//
//			//Login to the Workflow Server
//			vwSession = loginWorkflow(wiiscLog);
//
//			if (vwSession != null)
//			{
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(0);
//				fnWorkflow.setErrorMessage("");
//				
//				//Add Export Configuration XML Steps
//				//Get the Workflow Configuration XML Export Path
//				String workflowConfigXMLExportPath = globalConfig.getString("workflowConfigXMLExportPath");
//				if (workflowConfigXMLExportPath.length() > 0)
//				{
//					//VWXMLConfiguration.exportConfigurationToFile(arg0, arg1)
//					IVWtoXML sysConfig;
//					StringBuffer configBuf = new StringBuffer("");
//					PrintWriter outDestination1 = null;
//					File file = new File(workflowConfigXMLExportPath);
//					
//					//Check if Export path exists or needs created
//					if (!file.getParentFile().exists())
//					{
//						file.getParentFile().mkdir();
//					}
//					
//					//Save SysConfig
//					sysConfig = vwSession.fetchSystemConfiguration();
//					//Push SysConfig to StringBuffer in XML format
//					sysConfig.toXML(configBuf);
//					//Write StringBuffer to File
//					outDestination1 = getWriter(workflowConfigXMLExportPath, wiiscLog);
//					outDestination1.println(sysConfig);
//					outDestination1.close();
//					
//					//Clear StringBuffer
//					configBuf = null;
//					//Clear SysConfig
//					sysConfig = null;
//					
//					//System.out.println("Imported Workflow Config XML Successfully with Merge Option");
//					wiiscLog.log(wiiscLog.INFO, "Exported Workflow Config XML Successfully");
//					//Update the Workflow Status
//					fnWorkflow.setFnWorkflowStatus("Export Workflow Configuration XML SUCCESSFUL");
//				}
//				else
//				{
//					//workflowConfigXMLPath is empty
//					wiiscLog.log(wiiscLog.INFO, "The Workflow Config XML Export Path was empty.");
//				}
//								
//				//Logoff the Workflow Server
//				wiiscLog.log(wiiscLog.INFO, "Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				wiiscLog.log(wiiscLog.INFO, "Logged off");
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(1);
//				fnWorkflow.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//				fnWorkflow.setFnWorkflowStatus("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Release the VWSession
//				vwSession = null;
//				wiiscLog.log(wiiscLog.INFO, "Logged off");
//			}
//		}
//		catch(VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update ErrorFlag
//			fnWorkflow.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnWorkflow.setErrorMessage(ex.getMessage());
//		}
//		catch (Exception ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update ErrorFlag
//			fnWorkflow.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnWorkflow.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> exportConfigXML()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflow;
//	}
//	
//	//Import the Workflow Configuration XML
//	public FnWorkflow importConfigXML(WIISCLog wiiscLog)
//	{
//		FnWorkflow fnWorkflow = new FnWorkflow();
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//		boolean regionInitialized = false;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> importConfigXML()");
//
//			//Login to the Workflow Server
//			vwSession = loginWorkflow(wiiscLog);
//
//			if (vwSession != null)
//			{
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(0);
//				fnWorkflow.setErrorMessage("");
//								
//				//Add Import Configuration XML Steps
//				//Get the Workflow Configuration XML Path
//				String workflowConfigXMLPath = globalConfig.getString("workflowConfigXMLPath");
//				if (workflowConfigXMLPath.length() > 0)
//				{
//					String configOutput = "";
//					String[] configOutputData = null;
//					String importOption = "";
//					
//					//Get Region status
//					regionInitialized = vwSession.isRegionInitialized();
//					//Check if the Region has been initialized by the System at some point
//					//This applies to Newly initialized and existing that were once initialized
//					if (regionInitialized)
//					{
//						//Region already initialized
//						wiiscLog.log(wiiscLog.INFO, "Region has already been initialized");
//						//Check the Import Option
//						importOption = globalConfig.getString("workflowConfigXMLImportOption");
//						if (importOption.length() > 0)
//						{
//							if (importOption.equals("Overwrite"))
//							{
//								//Overwrite Option
//								configOutput = VWXMLConfiguration.importConfigurationFromFile(vwSession, workflowConfigXMLPath, VWXMLConfiguration.IMPORT_REPLACE);
//								configOutputData = configOutput.split("\\n");
//							}
//							else
//							{
//								//Merge Option
//								configOutput = VWXMLConfiguration.importConfigurationFromFile(vwSession, workflowConfigXMLPath, VWXMLConfiguration.IMPORT_MERGE);
//								configOutputData = configOutput.split("\\n");
//							}
//						}
//						else
//						{
//							//Region Initialized - Merge Option
//							configOutput = VWXMLConfiguration.importConfigurationFromFile(vwSession, workflowConfigXMLPath, VWXMLConfiguration.IMPORT_MERGE);
//							configOutputData = configOutput.split("\\n");
//						}
//					}
//					else
//					{
//						//Region Not Initialized - Overwrite Option
//						configOutput = VWXMLConfiguration.importConfigurationFromFile(vwSession, workflowConfigXMLPath, VWXMLConfiguration.IMPORT_REPLACE);
//						configOutputData = configOutput.split("\\n");
//					}
//					//Output the Import info to the Log
//					for (String w : configOutputData)
//					{
//						wiiscLog.log(wiiscLog.INFO, w);
//					}
//					//System.out.println("Imported Workflow Config XML Successfully with Merge Option");
//					wiiscLog.log(wiiscLog.INFO, "Imported Workflow Config XML Successfully with " + importOption + " Option");
//					//Update the Workflow Status
//					fnWorkflow.setFnWorkflowStatus("Import Workflow Configuration XML SUCCESSFUL");
//				}
//				else
//				{
//					//workflowConfigXMLPath is empty
//					wiiscLog.log(wiiscLog.INFO, "The Workflow Config XML Path was empty.");
//				}
//								
//				//Logoff the Workflow Server
//				wiiscLog.log(wiiscLog.INFO, "Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				wiiscLog.log(wiiscLog.INFO, "Logged off");
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(1);
//				fnWorkflow.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//				fnWorkflow.setFnWorkflowStatus("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Release the VWSession
//				vwSession = null;
//				wiiscLog.log(wiiscLog.INFO, "Logged off");
//			}
//		}
//		catch(VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update ErrorFlag
//			fnWorkflow.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnWorkflow.setErrorMessage(ex.getMessage());
//		}
//		catch (Exception ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update ErrorFlag
//			fnWorkflow.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnWorkflow.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> importConfigXML()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflow;
//	}
//	
//	//Load the Workflow Maps from a defined directory in the Global Config Properties
//	public FnWorkflow loadMaps(WIISCLog wiiscLog)
//	{
//		FnWorkflow fnWorkflow = new FnWorkflow();
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//		boolean regionInitialized = false;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> loadMaps()");
//
//			//Login to the Workflow Server
//			vwSession = loginWorkflow(wiiscLog);
//
//			if (vwSession != null)
//			{
//
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(0);
//				fnWorkflow.setErrorMessage("");
//
//				//Get the Workflow Maps Path
//				String workflowMapsPath = globalConfig.getString("workflowMapsPath");
//				if (workflowMapsPath.length() > 0)
//				{
//					//Get Region status
//					regionInitialized = vwSession.isRegionInitialized();
//					//Check if the Region has been initialized
//					if (regionInitialized)
//					{
//						//Region already initialized
//						wiiscLog.log(wiiscLog.INFO, "Region has already been initialized");
//						//Region Initialized - Workflow Maps will be loaded
//						File mapsDir = new File(workflowMapsPath);
//						if (mapsDir.isDirectory())
//						{
//							//Get all of the Workflow Map files
//							File[] mapFiles = mapsDir.listFiles();
//							//Get the Total Count of PEP Files
//							int pepFileCount = 0;
//							//Get the Total Count of Processed PEP Files
//							int pepFileProcessedCount = 0;
//							//Process 1 Workflow Map File at a time
//							for (File mapFile : mapFiles)
//							{
//								//Verify we get a file
//								if (mapFile.isFile())
//								{
//									//Verify we get a valid PEP file
//									if (mapFile.getName().endsWith(".pep") || mapFile.getName().endsWith(".PEP"))
//									{
//										//Update the PEP File Count
//										pepFileCount++;
//										//Process the valid PEP file
//										VWWorkflowDefinition wfDefinition = null;
//										wfDefinition = VWWorkflowDefinition.readFromFile(mapFile.getAbsolutePath());
//										//Transfer the PEP file
//										VWTransferResult transferResult = vwSession.transfer(wfDefinition, null, false, false);
//										//Check the Transfer Result
//										if (transferResult.success())
//										{
//											wiiscLog.log(wiiscLog.INFO, mapFile.getName() + " Transferred Successfully");
//											//Update the Processed PEP File Count
//											pepFileProcessedCount++;
//										}
//										else
//										{
//											String[] errors = transferResult.getErrors();
//											wiiscLog.log(wiiscLog.INFO, "ERRORS Transferring Workflow Map " + mapFile.getName());
//											//wiiscLog.log(wiiscLog.INFO, errors.toString());
//											for (String oneError : errors)
//											{
//												wiiscLog.log(wiiscLog.INFO, oneError);
//											}
//										}
//									}
//								}
//							}
//							wiiscLog.log(wiiscLog.INFO, "===========================================================");
//							wiiscLog.log(wiiscLog.INFO, "Transferred " + pepFileProcessedCount + " out of " + pepFileCount + " Workflow Maps");
//							
//							//Update the Workflow Status
//							fnWorkflow.setFnWorkflowStatus("Loading of the Workflow Maps was SUCCESSFUL");
//						}
//						else
//						{
//							//Workflow Maps Path is not a valid directory
//							wiiscLog.log(wiiscLog.INFO, "Workflow Maps Path is not a valid directory");
//						}
//					}
//					else
//					{
//						//Region Not Initialized - Workflow Maps cannot be loaded
//						wiiscLog.log(wiiscLog.INFO, "Workflow Region has not been initialized");
//					}
//				}
//				else
//				{
//					//workflowMapsPath is empty
//					wiiscLog.log(wiiscLog.INFO, "The Workflow Maps Path was empty.");
//				}
//
//				//Logoff the Workflow Server
//				wiiscLog.log(wiiscLog.INFO, "Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				wiiscLog.log(wiiscLog.INFO, "Logged off");
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(1);
//				fnWorkflow.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//				fnWorkflow.setFnWorkflowStatus("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Release the VWSession
//				vwSession = null;
//				wiiscLog.log(wiiscLog.INFO, "Logged off");
//			}
//		}
//		catch(VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update ErrorFlag
//			fnWorkflow.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflow.setErrorMessage(ex.getMessage());
//		}
//		catch (IOException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update ErrorFlag
//			fnWorkflow.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflow.setErrorMessage(ex.getMessage());
//		}
//		catch (Exception ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update ErrorFlag
//			fnWorkflow.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflow.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> loadMaps()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflow;
//	}
//	
//	//Initialize the Workflow Region
//	public FnWorkflow initializeRegion(WIISCLog wiiscLog)
//	{
//		FnWorkflow fnWorkflow = new FnWorkflow();
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> initializeRegion()");
//
//			//Login to the Workflow Server
//			vwSession = loginWorkflow(wiiscLog);
//
//			if (vwSession != null)
//			{
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(0);
//				fnWorkflow.setErrorMessage("");
//
//				VWSystemAdministration vwsysadmin = null;
//				vwsysadmin = vwSession.fetchSystemAdministration();
//				vwsysadmin.initializeRegion();
//				vwsysadmin.commit();
//				wiiscLog.log(wiiscLog.INFO, "Workflow Region Initialized Successfully");
//				
//				//Update the Workflow Status
//				fnWorkflow.setFnWorkflowStatus("Initializing of the Workflow Region SUCCESSFUL");
//
//				//Logoff the Workflow Server
//				wiiscLog.log(wiiscLog.INFO, "Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				wiiscLog.log(wiiscLog.INFO, "Logged off");
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(1);
//				fnWorkflow.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//				fnWorkflow.setFnWorkflowStatus("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Release the VWSession
//				vwSession = null;
//				wiiscLog.log(wiiscLog.INFO, "Logged off");
//			}
//		}
//		catch(VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update ErrorFlag
//			fnWorkflow.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflow.setErrorMessage(ex.getMessage());
//		}
//		catch (Exception ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update ErrorFlag
//			fnWorkflow.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflow.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> initializeRegion()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflow;
//	}
//		
//	//Initiate PE Workflow with FnWorkflow Object from an XML Request
//	public FnWorkflow initiateWorkflow(FnWorkflow fnWorkflow, WIISCLog wiiscLog)
//	{
//		//Create the FnWorkflow Object to Return
//		FnWorkflow fnWorkflowResult = new FnWorkflow();
//
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> initiateWorkflow()");
//
//			//Get the Workflow Fields
//			String process = fnWorkflow.getFnWorkflowProcess();
//			String user = fnWorkflow.getFnWorkflowUser();
//
//			//Login to the Workflow Server
//			vwSession = loginWorkflow(wiiscLog);
//
//			if (vwSession != null)
//			{
//				wiiscLog.log(wiiscLog.INFO, "Logged in successfully to the Workflow Server");
//				//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//				//Update the FnWorkflow Object
//				//fnWorkflowResult.setErrorFlag(0);
//				//fnWorkflowResult.setFnWorkflowStatus("Workflow Login SUCCESSFUL");
//				//Boolean to Validate the Requested Process
//				boolean validProcess = false;
//				//Retrieve Rosters
//				String[] rosterNames = vwSession.fetchRosterNames(true);
//				//Verify the Workflow Process is Valid			
//				for (int a = 0; a < rosterNames.length; a++)
//				{
//					//Verify that the Process Parameter is a valid Roster to Launch a Workflow
//					if (rosterNames[a].equals(process))
//					{
//						if (vwSession.getRoster(process) != null)
//						{
//							//Valid Process
//							validProcess = true;
//						}
//						else
//						{
//							//Invalid Process
//							validProcess = false;
//						}
//						break;
//					}
//				}
//				//Check if a Valid Process was Found
//				if (validProcess)
//				{
//					//Valid Process, so we Launch the Workflow
//					wiiscLog.log(wiiscLog.INFO, "===========================================");
//					wiiscLog.log(wiiscLog.INFO, "Workflow Process " + process + " is valid");
//					//Create the Workflow
//					VWStepElement stepElement = vwSession.createWorkflow(process);
//
//					//Check if a Valid Process was Launched
//					if (stepElement != null)
//					{
//						//Initial Workflow Launch Step Properties
//						wiiscLog.log(wiiscLog.INFO, "Workflow Process " + process + " has launched");
//
//						wiiscLog.log(wiiscLog.INFO, "===========================================");
//						wiiscLog.log(wiiscLog.INFO, "Setting the Workflow Data Fields");
//
//						//Create the FnWorkflowPropertyList Object
//						FnWorkflowPropertyList fnWorkflowPropertyListResult = new FnWorkflowPropertyList();
//
//						//Get the VWStepElement System and User Defined Data Fields
//						VWParameter[] vwParametersData = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_AND_SYSTEM_DEFINED);
//
//						//DoStepElementDataField - Integer and String
//						for (int i = 0; i < vwParametersData.length; i++)
//						{
//							VWParameter vwParam = vwParametersData[i];
//							//Only Process Integer and String
//							if (vwParam.getFieldType() != VWFieldType.FIELD_TYPE_ATTACHMENT && 
//									vwParam.getFieldType() != VWFieldType.FIELD_TYPE_PARTICIPANT)
//							{
//								//Check FnWorkflow
//								if (fnWorkflow != null)
//								{
//									wiiscLog.log(wiiscLog.INFO, "Updating Workflow Data Field: " + vwParam.getName());
//									FnWorkflowProperty fnWorkflowPropertyResult = new FnWorkflowProperty();
//									//Call DoStepElementDataField
//									fnWorkflowPropertyResult = doStepElementDataField(stepElement, vwParam, fnWorkflow, wiiscLog);
//									if (fnWorkflowPropertyResult != null)
//									{
//										//Add the FnProperty to the FnPropertyListResult
//										fnWorkflowPropertyListResult.addFnWorkflowProperty(fnWorkflowPropertyResult);
//									}
//								}
//							}
//						}
//
//						wiiscLog.log(wiiscLog.INFO, "===========================================");
//
//						//Update the FnWorkflowResult
//						fnWorkflowResult = updateFnWorkflowInfo(stepElement, "New", wiiscLog);
//						fnWorkflowPropertyListResult = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//
//						//Get the Step Element Responses
//						String[] stepResponses = stepElement.getStepResponses();
//						if(stepResponses != null)
//						{
//							for (int j = 0; j < stepResponses.length; j++)
//							{
//								String stepResponse = stepResponses[j];
//								wiiscLog.log(wiiscLog.INFO, "Step Response: " + stepResponse);
//							}
//							String responseValue = "Ok";
//							wiiscLog.log(wiiscLog.INFO, "Applying Step Response: " + responseValue);
//							stepElement.setSelectedResponse(responseValue);
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "No Step Responses - Possible Launch Step");
//						}
//
//						//DoStepElementAction - Dispatch the Workflow
//						fnWorkflowResult = doStepElementAction(stepElement, "Dispatch", "", wiiscLog);
//
//						//Update additional custom data fields
//						//More to add later
//
//						//Dispatch the Workflow Launch Step
//						wiiscLog.log(wiiscLog.INFO, "===========================================");
//						wiiscLog.log(wiiscLog.INFO, "Workflow Process " + process + " has initiated");
//						//Update FnWorkflow
//						fnWorkflowResult.setErrorFlag(0);
//						fnWorkflowResult.setErrorMessage("");
//						fnWorkflowResult.setFnWorkflowStatus("INITIATED");
//						//Get the Queue Name
//						fnWorkflowResult.setFnWorkflowQueue(stepElement.getCurrentQueueName());
//						//Add the FnWorkflowPropertyList to the FnWorkflow
//						fnWorkflowResult.setFnWorkflowPropertyList(fnWorkflowPropertyListResult);
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "Workflow Process " + process + " failed to launch");
//						//Update FnWorkflow
//						fnWorkflowResult.setErrorFlag(1);
//						fnWorkflowResult.setErrorMessage("Process " + process + " FAILED to launch");
//						fnWorkflowResult.setFnWorkflowStatus("Process " + process + " FAILED to launch");
//					}
//				}
//				else
//				{
//					//Missing Parameters
//					wiiscLog.log(wiiscLog.INFO, "The Workflow Process " + process + " is Invalid or does not exist");
//					//Update FnWorkflow
//					fnWorkflowResult.setErrorFlag(1);
//					fnWorkflowResult.setErrorMessage("Process " + process + " is Invalid");
//					fnWorkflowResult.setFnWorkflowStatus("Process " + process + " is Invalid");
//				}
//
//				//Logoff the Workflow Server
//				wiiscLog.log(wiiscLog.INFO, "Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				wiiscLog.log(wiiscLog.INFO, "Logged off");
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflow Object
//				fnWorkflowResult.setErrorFlag(1);
//				fnWorkflowResult.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//				fnWorkflowResult.setFnWorkflowStatus("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Release the VWSession
//				vwSession = null;
//			}
//		}
//		catch(VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update the FnWorkflow Object
//			fnWorkflowResult.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowResult.setErrorMessage(ex.getMessage());
//			fnWorkflowResult.setFnWorkflowStatus("Workflow Login FAILED");
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> initiateWorkflow()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflowResult;
//	}
//
//	//Initiate PE Workflow with Document using the FnWorkflow Object from an XML Request
//	public FnWorkflow initiateWorkflowWithDocument(FnWorkflow fnWorkflow, WIISCLog wiiscLog)
//	{
//		//Create the FnWorkflow Object to Return
//		FnWorkflow fnWorkflowResult = new FnWorkflow();
//
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> initiateWorkflowWithDocument()");
//
//			//Get the Workflow Fields
//			String rosterName = fnWorkflow.getFnWorkflowRoster();
//			String queueName = fnWorkflow.getFnWorkflowQueue();
//			String process_sys_code = fnWorkflow.getFnWorkflowProcess();
//			String user = fnWorkflow.getFnWorkflowUser();
//			//List<FnWorkflowProperty> fnWorkflowPropertyListRequest = fnWorkflow.getFnWorkflowPropsList();
//
//			//Login to the Workflow Server
//			vwSession = loginWorkflow(wiiscLog);
//
//			if (vwSession != null)
//			{
//				wiiscLog.log(wiiscLog.INFO, "Logged in successfully to the Workflow Server");
//				
//				//Boolean to Validate the Requested Process
//				boolean validProcess = false;
//				//Retrieve Work Classes
//				String[] workClassNames = vwSession.fetchRosterNames(true);
//				//Verify the Workflow Process is Valid			
//				for (int a = 0; a < workClassNames.length; a++)
//				{
//					//Verify that the Process Parameter is a valid WorkClass to Launch a Workflow
//					if (workClassNames[a].equals(process_sys_code))
//					{
//						if (vwSession.getRoster(process_sys_code) != null)
//						{
//							//Valid Process
//							validProcess = true;
//						}
//						else
//						{
//							//Invalid Process
//							validProcess = false;
//						}
//						break;
//					}
//				}
//				//Check if a Valid Process was Found
//				if (validProcess)
//				{
//					//Valid Process, so we Launch the Workflow
//					wiiscLog.log(wiiscLog.INFO, "===========================================");
//					wiiscLog.log(wiiscLog.INFO, "Workflow Process " + process_sys_code + " is valid");
//					//Create the Workflow
//					VWStepElement stepElement = vwSession.createWorkflow(process_sys_code);
//
//					//Check if a Valid Process was Launched
//					if (stepElement != null)
//					{
//						//Initial Workflow Launch Step Properties
//						wiiscLog.log(wiiscLog.INFO, "Workflow Process " + process_sys_code + " has launched");
//
//						wiiscLog.log(wiiscLog.INFO, "===========================================");
//						wiiscLog.log(wiiscLog.INFO, "Setting the Workflow Data Fields");
//
//						//Create the FnWorkflowPropertyList Object
//						FnWorkflowPropertyList fnWorkflowPropertyListResult = new FnWorkflowPropertyList();
//
//						//Get the VWStepElement System and User Defined Data Fields
//						VWParameter[] vwParametersData = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_AND_SYSTEM_DEFINED);
//
//						//DoStepElementDataField - Integer and String
//						for (int i = 0; i < vwParametersData.length; i++)
//						{
//							VWParameter vwParam = vwParametersData[i];
//							//Only Process Integer and String
//							if (vwParam.getFieldType() != VWFieldType.FIELD_TYPE_ATTACHMENT && 
//									vwParam.getFieldType() != VWFieldType.FIELD_TYPE_PARTICIPANT)
//							{
//								//Check FnWorkflow
//								if (fnWorkflow != null)
//								{
//									wiiscLog.log(wiiscLog.INFO, "Updating Workflow Data Field: " + vwParam.getName());
//									FnWorkflowProperty fnWorkflowPropertyResult = new FnWorkflowProperty();
//									//Call DoStepElementDataField
//									fnWorkflowPropertyResult = doStepElementDataField(stepElement, vwParam, fnWorkflow, wiiscLog);
//									if (fnWorkflowPropertyResult != null)
//									{
//										//Add the FnProperty to the FnPropertyListResult
//										fnWorkflowPropertyListResult.addFnWorkflowProperty(fnWorkflowPropertyResult);
//									}
//								}
//							}
//							else if (vwParam.getFieldType() == VWFieldType.FIELD_TYPE_ATTACHMENT)
//							{
//								//Check FnWorkflow
//								if (fnWorkflow != null)
//								{
//									wiiscLog.log(wiiscLog.INFO, "Updating Workflow Attachment: " + vwParam.getName());
//									FnDocument fnDocument = new FnDocument();
//									//Setup GlobalConfig for imagingManager
//									imagingManager.setGlobalConfig(getGlobalConfig());
//									fnDocument = imagingManager.getDocumentInfo(fnWorkflow.getFnDocumentID(), wiiscLog);
//									//Verify the FnDocument exists with an ID
//									if (fnDocument.getFnDocumentID().length() > 0)
//									{
//										boolean attachmentAdded = false;
//										//Call DoStepElementAttachmentField
//										attachmentAdded = doStepElementAttachment(stepElement, vwParam, fnDocument, wiiscLog);
//									}
//								}
//							}
//						}
//
//						wiiscLog.log(wiiscLog.INFO, "===========================================");
//
//						//Update the FnWorkflowResult
//						fnWorkflowResult = updateFnWorkflowInfo(stepElement, "New", wiiscLog);
//
//						//fnWorkflowPropertyListResult = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//
//						//Get the Step Element Responses
//						String[] stepResponses = stepElement.getStepResponses();
//						if(stepResponses != null)
//						{
//							for (int j = 0; j < stepResponses.length; j++)
//							{
//								String stepResponse = stepResponses[j];
//								wiiscLog.log(wiiscLog.INFO, "Step Response: " + stepResponse);
//							}
//							String responseValue = "Ok";
//							wiiscLog.log(wiiscLog.INFO, "Applying Step Response: " + responseValue);
//							stepElement.setSelectedResponse(responseValue);
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "No Step Responses - Possible Launch Step");
//						}
//
//						//DoStepElementAction - Dispatch the Workflow
//						fnWorkflowResult = doStepElementAction(stepElement, "Dispatch", "", wiiscLog);
//
//						//Update additional custom data fields
//						//More to add later
//
//						//Dispatch the Workflow Launch Step
//						wiiscLog.log(wiiscLog.INFO, "===========================================");
//						wiiscLog.log(wiiscLog.INFO, "Workflow Process " + process_sys_code + " has initiated");
//						//Update FnWorkflow
//						fnWorkflowResult.setErrorFlag(0);
//						fnWorkflowResult.setErrorMessage("");
//						fnWorkflowResult.setFnWorkflowStatus("INITIATED");
//						//Get the Queue Name
//						fnWorkflowResult.setFnWorkflowQueue(stepElement.getCurrentQueueName());
//						//Add the FnWorkflowPropertyList to the FnWorkflow
//						fnWorkflowResult.setFnWorkflowPropertyList(fnWorkflowPropertyListResult);
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "Workflow Process " + process_sys_code + " failed to launch");
//						//Update FnWorkflow
//						fnWorkflowResult.setErrorFlag(1);
//						fnWorkflowResult.setErrorMessage("Process " + process_sys_code + " FAILED to launch");
//						fnWorkflowResult.setFnWorkflowStatus("Process " + process_sys_code + " FAILED to launch");
//					}
//				}
//				else
//				{
//					//Missing Parameters
//					wiiscLog.log(wiiscLog.INFO, "The Workflow Process " + process_sys_code + " is Invalid or does not exist");
//					//Update FnWorkflow
//					fnWorkflowResult.setErrorFlag(1);
//					fnWorkflowResult.setErrorMessage("Process " + process_sys_code + " is Invalid");
//					fnWorkflowResult.setFnWorkflowStatus("Process " + process_sys_code + " is Invalid");
//				}
//
//				//Logoff the Workflow Server
//				wiiscLog.log(wiiscLog.INFO, "Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				wiiscLog.log(wiiscLog.INFO, "Logged off");
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflow Object
//				fnWorkflowResult.setErrorFlag(1);
//				fnWorkflowResult.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//				fnWorkflowResult.setFnWorkflowStatus("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Release the VWSession
//				vwSession = null;
//			}
//		}
//		catch(VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update the FnWorkflow Object
//			fnWorkflowResult.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowResult.setErrorMessage(ex.getMessage());
//			fnWorkflowResult.setFnWorkflowStatus("Workflow Login FAILED");
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> initiateWorkflowWithDocument()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflowResult;
//	}
//
//	//Update PE Workflow with FnWorkflow Object from an XML Request
//	public FnWorkflow updateWorkflow(FnWorkflow fnWorkflowRequest, WIISCLog wiiscLog)
//	{
//		//Create the FnWorkflow Object to Return
//		FnWorkflow fnWorkflow = new FnWorkflow();
//
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//
//		//Get the Filter Name
//		String[] filterName = null;
//		//Get the Filter Value
//		String[] filterValue = null;
//		//Boolean to determine if we need to get the FnWorkflow and FnWorkflow Property Info
//		boolean displayFnWorkflowInfo = false;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> updateWorkflow()");
//
//			//Get the Workflow Fields
//			String process = fnWorkflowRequest.getFnWorkflowProcess();
//			String step = fnWorkflowRequest.getFnWorkflowStep();
//			String user = fnWorkflowRequest.getFnWorkflowUser();
//			String response = fnWorkflowRequest.getFnWorkflowResponse();
//			String status = fnWorkflowRequest.getFnWorkflowStatus();
//			String propName = "";
//			String propValue = "";
//			String customResponse = "";
//			String workflowObjectNumber = "";
//
//			//Determine the Workflow Search Field and get its value to use for Finding the requested Workflow
//			if (globalConfig.containsKey("workflowSearchField"))
//			{
//				if (globalConfig.getString("workflowSearchField").length() > 0)
//				{
//					propName = globalConfig.getString("workflowSearchField");
//					//Get the FnWorkflow Property Value for the Workflow Search Field
//					propValue = getFnWorkflowPropertyValue(fnWorkflowRequest, propName, wiiscLog);
//				}
//			}
//
//			//Login to the Workflow Server
//			vwSession = loginWorkflow(wiiscLog);
//
//			if (vwSession != null)
//			{
//				wiiscLog.log(wiiscLog.INFO, "Logged in successfully to the Workflow Server");
//
//				//Get the VWStepElement to be used for updating the Workflow
//				VWStepElement stepElement = getWorkflowStepElement(vwSession, process, step, user, propName, propValue, wiiscLog);
//
//				if (stepElement != null)
//				{
//					//PropertyListResult
//					//FnWorkflowPropertyList fnWorkflowPropertyListResult = new FnWorkflowPropertyList();
//					
//					//StepElement Parameters Map of Name and Value
//					Map<String, String> stepElementParamsMap = new HashMap<String, String>();
//					
//					//Get the VWStepElement System and User Defined Data Fields
//					//VWParameter[] vwParametersData = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_AND_SYSTEM_DEFINED);
//
//					//Get the Workflow Object Number
//					workflowObjectNumber = stepElement.getWorkObjectNumber();
//										
//					//******************************************************************
//					//Check for Custom Response with underscores (_)
//					//e.g. REVOKED_REFUND_REJECT, this would use REVOKED but set the
//					//current state to REVOKED_REFUND_REJECT
//					//******************************************************************
//					//Set Custom Response equal to Response
//					customResponse = response;
//					
//					if (response.contains("_"))
//					{
//						//Used for alternate Custom Responses
//						//instead of normal ones - COMPLETED, RETURNED, REVOKED, SKIPPED, CANCELLED
//						String[] customResponseData = null;
//						customResponseData = response.split("_");
//						//Set Response equal to 1st element of Custom Response with underscore
//						response = customResponseData[0];
//					}
//					
//					//**************************************************************
//					//Check the FnWorkflow Response to see which action to perform
//					//**************************************************************
//					//Reassign the Workflow to another User
//					if (response.equals("Reassign"))
//					{
//						wiiscLog.log(wiiscLog.INFO, "Reassign");
//						
//						//Update the Step Element
//						fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, null, response, user, wiiscLog);
//						
//						//Display FnWorkflow Info
//						displayFnWorkflowInfo = true;
//					}
//					//Return the Workflow to its Source
//					else if (response.equals("Return"))
//					{
//						wiiscLog.log(wiiscLog.INFO, "Return");
//						
//						//Update the Step Element
//						fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, null, response, "", wiiscLog);
//
//						//Display FnWorkflow Info
//						displayFnWorkflowInfo = true;
//					}
//					//Move the Workflow to a User's Inbox
//					else if (response.equals("Move"))
//					{
//						wiiscLog.log(wiiscLog.INFO, "Move");
//						
//						//Update the Step Element
//						fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, null, response, user, wiiscLog);
//						
//						//Display FnWorkflow Info
//						displayFnWorkflowInfo = true;
//					}
//					//Apply Workflow Property Updates and Save
//					else if (response.equals("Save"))
//					{
//						wiiscLog.log(wiiscLog.INFO, "Save");
//						
//						//Update the Step Element
//						fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, null, response, "", wiiscLog);
//
//						//Display FnWorkflow Info
//						displayFnWorkflowInfo = true;
//					}
//					else if (response.equals("Dispatch"))
//					{
//						//Update the Step Element
//						fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, null, response, "", wiiscLog);
//
//						//Display FnWorkflow Info
//						displayFnWorkflowInfo = true;
//					}
//					else
//					{
//						//Check for Clarety Workflow Actions Enabled
//						//If not, then nothing to do
//						if (globalConfig.getString("claretyWorkflowActionsEnabled").equals("true"))
//						{
//							//Setup variables used by Clarety
//							String propName1 = "";
//							String propValue1 = "";
//							String propName2 = "";
//							String propValue2 = "";
//							String propName3 = "";
//														
//							if (response.equalsIgnoreCase("IN PROGRESS"))
//							{
//								wiiscLog.log(wiiscLog.INFO, "Clarety - " + customResponse);
//								
//								//Update the Step Element
//								fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, null, "DataFields", user, wiiscLog);
//								
//								//Output the Updates
//								//fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing", wiiscLog);
//								//fnWorkflowPropertyListResult = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//								
//								//Update Current State to In Progress
//								propName1 = "current_state";
//								propValue1 = customResponse;
//								//propValue1 = "IN PROGRESS";
//								//Update Assigned User Login
//								propName2 = "assigned_user_login";
//								//propValue2 = fnWorkflowRequest.getFnWorkflowUser();
//								propValue2 = user;
//								//Check if User was passed or if it was a Workflow Property
//								if (propValue2.length() == 0)
//								{
//									propValue2 = getFnWorkflowPropertyValue(fnWorkflowRequest, propName2, wiiscLog);
//								}
//								
//								//Build the Step Element Parameters Map
//								stepElementParamsMap.put(propName1, propValue1);
//								stepElementParamsMap.put(propName2, propValue2);
//								
//								//Update the Step Element
//								fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, stepElementParamsMap, "Parameters-Save", user, wiiscLog);
//								
//								//Display FnWorkflow Info
//								displayFnWorkflowInfo = true;
//							}
//							else if (response.equalsIgnoreCase("SKIPPED"))
//							{
//								wiiscLog.log(wiiscLog.INFO, "Clarety - " + customResponse);
//								//Update Current State to Skipped
//								propName1 = "current_state";
//								propValue1 = customResponse;
//								//propValue1 = "SKIPPED";
//								//Update Assigned User Login
//								propName2 = "assigned_user_login";
//								//Update Previous User
//								propName3 = "previous_performer";
//								//Get Current assigned_user_login value
//								String currentUser = stepElement.getParameterValue(propName2).toString();
//																
//								//Update the Step Element
//								fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, stepElementParamsMap, "DataFields", user, wiiscLog);
//								
//								//Output the Updates
//								//fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing", wiiscLog);
//								//fnWorkflowPropertyListResult = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//								
//								//Build the Step Element Parameters Map
//								stepElementParamsMap.put(propName1, propValue1);
//								stepElementParamsMap.put(propName2, "");
//								stepElementParamsMap.put(propName3, currentUser);
//								
//								//Update the Step Element
//								fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, stepElementParamsMap, "Parameters-Dispatch", user, wiiscLog);
//																
//								//Display FnWorkflow Info
//								displayFnWorkflowInfo = true;
//							}
//							else if (response.equalsIgnoreCase("SUSPENDED"))
//							{
//								wiiscLog.log(wiiscLog.INFO, "Clarety - " + customResponse);
//								
//								//Update the Step Element
//								fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, stepElementParamsMap, "DataFields", user, wiiscLog);
//								
//								//Output the Updates
//								//fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing", wiiscLog);
//								//fnWorkflowPropertyListResult = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//								
//								//Update Current State to Suspended
//								propName1 = "current_state";
//								propValue1 = customResponse;
//								//propValue1 = "SUSPENDED";
//								//Build the Step Element Parameters Map
//								stepElementParamsMap.put(propName1, propValue1);
//																
//								//Update the Step Element
//								fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, stepElementParamsMap, "Parameters-Save", user, wiiscLog);
//																
//								//Display FnWorkflow Info
//								displayFnWorkflowInfo = true;
//							}
//							else if (response.equalsIgnoreCase("COMPLETED"))
//							{
//								wiiscLog.log(wiiscLog.INFO, "Clarety - " + customResponse);
//								
//								//Update Current State to Completed
//								propName1 = "current_state";
//								propValue1 = customResponse;
//								//propValue1 = "COMPLETED";
//								//Update Assigned User Login
//								propName2 = "assigned_user_login";
//								//Update Previous User
//								propName3 = "previous_performer";
//								//Get Current assigned_user_login value
//								String currentUser = stepElement.getParameterValue(propName2).toString();
//								
//								//Update the Step Element
//								fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, stepElementParamsMap, "DataFields", "", wiiscLog);
//								
//								//Output the Updates
//								//fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing", wiiscLog);
//								//fnWorkflowPropertyListResult = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//								
//								//Build the Step Element Parameters Map
//								stepElementParamsMap.put(propName1, propValue1);
//								stepElementParamsMap.put(propName2, "");
//								stepElementParamsMap.put(propName3, currentUser);
//								
//								//Update the Step Element
//								fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, stepElementParamsMap, "Parameters-Dispatch", user, wiiscLog);
//
//								//Display FnWorkflow Info
//								displayFnWorkflowInfo = true;
//							}
//							else if (response.equalsIgnoreCase("REVOKED"))
//							{
//								wiiscLog.log(wiiscLog.INFO, "Clarety - " + customResponse);
//								
//								//Update Current State to Revoked
//								propName1 = "current_state";
//								propValue1 = customResponse;
//								//propValue1 = "REVOKED";
//								//Update Assigned User Login
//								propName2 = "assigned_user_login";
//								//Update Revoking User
//								propName3 = "revoking_user";
//								//Get Current assigned_user_login value
//								String currentUser = stepElement.getParameterValue(propName2).toString();
//								
//								//Update the Step Element
//								fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, stepElementParamsMap, "DataFields", "", wiiscLog);
//								
//								//Output the Updates
//								//fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing", wiiscLog);
//								//fnWorkflowPropertyListResult = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//								
//								//Build the Step Element Parameters Map
//								stepElementParamsMap.put(propName1, propValue1);
//								stepElementParamsMap.put(propName2, "");
//								stepElementParamsMap.put(propName3, currentUser);
//								
//								//Update the Step Element
//								fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, stepElementParamsMap, "Parameters-Dispatch", user, wiiscLog);
//								
//								//Display FnWorkflow Info
//								displayFnWorkflowInfo = true;
//							}
//							else if (response.equalsIgnoreCase("RETURNED"))
//							{
//								wiiscLog.log(wiiscLog.INFO, "Clarety - " + customResponse);
//								
//								//Update Current State to Returned
//								propName1 = "current_state";
//								propValue1 = customResponse;
//								//propValue1 = "RETURNED";
//								//Update Assigned User Login
//								propName2 = "assigned_user_login";
//								
//								//***********************
//								//01/05/2016
//								//No longer doing this because if a User picks up a workflow and 
//								//returns it back to the Workpool, then there is no need to change the previous_performer
//								//since the User did not do anything to the workflow.
//								
//								//Update Revoking User
//								//propName3 = "previous_performer";
//								
//								//Get Current assigned_user_login value
//								//String currentUser = stepElement.getParameterValue(propName2).toString();
//								//***********************
//								
//								//Update the Step Element
//								fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, stepElementParamsMap, "DataFields", "", wiiscLog);
//								
//								//Output the Updates
//								//fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing", wiiscLog);
//								//fnWorkflowPropertyListResult = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//								
//								//Build the Step Element Parameters Map
//								stepElementParamsMap.put(propName1, propValue1);
//								stepElementParamsMap.put(propName2, "");
//								
//								//***********************
//								//01/05/2016
//								//stepElementParamsMap.put(propName3, currentUser);
//								//***********************
//								
//								//Update the Step Element
//								fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, stepElementParamsMap, "Parameters-Save", user, wiiscLog);
//
//								//Display FnWorkflow Info
//								displayFnWorkflowInfo = true;
//							}
//							else if (response.equalsIgnoreCase("CANCELLED"))
//							{
//								wiiscLog.log(wiiscLog.INFO, "Clarety - " + customResponse);
//								
//								//Update the Step Element
//								fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, stepElementParamsMap, "DataFields", "", wiiscLog);
//								
//								//Output the Updates
//								//fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing", wiiscLog);
//								//fnWorkflowPropertyListResult = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//								
//								//Delete the Workflow by getting the Work Object from the
//								//Step Element, then do terminate
//
//								//Update the FnWorkflow
//								fnWorkflow.setFnWorkflowRoster(stepElement.getRosterName());
//								fnWorkflow.setFnWorkflowQueue(stepElement.getCurrentQueueName());
//								fnWorkflow.setFnWorkflowID(stepElement.getWorkObjectNumber());
//
//								//Get the WorkObject from the StepElement
//								VWWorkObject workObj = stepElement.fetchWorkObject(false, false);
//
//								//Lock the WorkObject
//								workObj.doLock(true);
//
//								//Terminate the WorkObject
//								workObj.doTerminate();
//
//								//Display FnWorkflow Info
//								displayFnWorkflowInfo = false;
//							}
//							else
//							{
//								wiiscLog.log(wiiscLog.INFO, "Clarety - ABORT");
//
//								//Update the Step Element
//								fnWorkflow = updateStepElement(stepElement, fnWorkflowRequest, stepElementParamsMap, "DataFields", "", wiiscLog);
//
//								//Output the Updates
//								//fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing", wiiscLog);
//								//fnWorkflowPropertyListResult = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//
//								//Do Abort - unlock Step Element and make no changes
//								//Abort the Step Element
//								stepElement.doAbort();
//
//								//End of For Loop
//								wiiscLog.log(wiiscLog.INFO, "===========================================================");
//
//								//Display FnWorkflow Info
//								displayFnWorkflowInfo = true;								
//							}
//						}
//						else
//						{
//							//Error because Response was not valid
//							wiiscLog.log(wiiscLog.INFO, "Response " + response + " was Invalid");
//						}
//					}
//
//					//Update additional custom data fields
//					//More to add later
//
//					//Do at the End
//					if (displayFnWorkflowInfo)
//					{
//						//Get Updated Workflow
//						fnWorkflow = getWorkflow(vwSession, process, workflowObjectNumber, wiiscLog);
//						
//						//Update the FnWorkflow
//						//fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing", wiiscLog);
//						//fnWorkflowPropertyListResult = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//						
//						//Check if the Workflow has Finished
//						if (fnWorkflow.getErrorFlag() == 0 && fnWorkflow.getFnWorkflowStatus().length() > 0)
//						{
//							wiiscLog.log(wiiscLog.INFO, "===========================================================");
//							wiiscLog.log(wiiscLog.INFO, "Workflow Process " + fnWorkflow.getFnWorkflowProcess() + " has finished");
//							//Update FnWorkflow
//							fnWorkflow.setErrorFlag(0);
//							fnWorkflow.setErrorMessage("");
//							//fnWorkflow.setFnWorkflowStatus("UPDATED");
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "===========================================================");
//							wiiscLog.log(wiiscLog.INFO, "Workflow Process " + fnWorkflow.getFnWorkflowProcess() + " for Workflow Step " + fnWorkflow.getFnWorkflowStep() + " has been updated");
//							//Update FnWorkflow
//							fnWorkflow.setErrorFlag(0);
//							fnWorkflow.setErrorMessage("");
//							fnWorkflow.setFnWorkflowStatus("UPDATED");
//						}
//						
//						//Get the Queue Name
//						//fnWorkflow.setFnWorkflowQueue(stepElement.getCurrentQueueName());
//						//Add the FnWorkflowPropertyList to the FnWorkflow
//						//fnWorkflow.setFnWorkflowPropertyList(fnWorkflowPropertyListResult);
//					}
//					else
//					{
//						//Don't show FnWorkflow Info because it was deleted
//						wiiscLog.log(wiiscLog.INFO, "===========================================================");
//						wiiscLog.log(wiiscLog.INFO, "Workflow Process " + process + " for Workflow Step " + step + " has been deleted");
//						//Update FnWorkflow
//						fnWorkflow.setErrorFlag(0);
//						fnWorkflow.setErrorMessage("");
//						fnWorkflow.setFnWorkflowStatus("DELETED");
//					}
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "Failed to get a StepElement");
//					//Update the FnWorkflow Object
//					//fnWorkflow.setErrorFlag(1);
//				}
//
//				//Logoff the Workflow Server
//				wiiscLog.log(wiiscLog.INFO, "Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				wiiscLog.log(wiiscLog.INFO, "Logged off");
//			}//End If VWSession
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(1);
//				fnWorkflow.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//				fnWorkflow.setFnWorkflowStatus("Workflow Login FAILED, Workflow Server may be unavailable.");
//			}
//		}
//		catch(VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update the FnWorkflow Object
//			fnWorkflow.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflow.setErrorMessage(ex.getMessage());
//			fnWorkflow.setFnWorkflowStatus("Workflow Login FAILED");
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> updateWorkflow()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflow;
//	}
//
//
//	//Initiate PE Workflow with Parameters
//	public FnWorkflow initWorkflow(String process_sys_code, String process_id, String account_id, WIISCLog wiiscLog)
//	{
//		//Create the FnWorkflow Object
//		FnWorkflow fnWorkflow = new FnWorkflow();
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> initWorkflow()");
//
//			//Login to the Workflow Server
//			vwSession = loginWorkflow(wiiscLog);
//
//			if (vwSession != null)
//			{
//				wiiscLog.log(wiiscLog.INFO, "Logged in successfully to the Workflow Server");
//				//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(0);
//				fnWorkflow.setErrorMessage("");
//				fnWorkflow.setFnWorkflowStatus("Workflow Login SUCCESSFUL");
//				//Boolean to Validate the Requested Process
//				boolean validProcess = false;
//				//Retrieve Work Classes
//				String[] workClassNames = vwSession.fetchWorkClassNames(true);
//
//				for (int a = 0; a < workClassNames.length; a++)
//				{
//					//Verify that the Process Parameter is a valid WorkClass to Launch a Workflow
//					if (workClassNames[a].equals(process_sys_code))
//					{
//						//Valid Process
//						validProcess = true;
//						break;
//					}
//				}
//				//Check if a Valid Process was Found
//				if (validProcess)
//				{
//					//Valid Process, so we Launch the Workflow
//					wiiscLog.log(wiiscLog.INFO, "===========================================");
//					wiiscLog.log(wiiscLog.INFO, "Workflow Process " + process_sys_code + " is valid");
//					//Create the Workflow
//					VWStepElement stepElement = vwSession.createWorkflow(process_sys_code);
//
//					//Check if a Valid Process was Launched
//					if (stepElement != null)
//					{
//						//Initial Workflow Launch Step Properties
//						wiiscLog.log(wiiscLog.INFO, "Workflow Process " + process_sys_code + " has launched");
//
//						fnWorkflow = updateFnWorkflowInfo(stepElement, "New", wiiscLog);
//
//						//Get the Custom Parameters from the GlobalConfig.properties
//						String workflowParameter = "";
//						String[] workflowParameterData = null;
//						workflowParameter = globalConfig.getString("workflowParameterNames");
//						workflowParameterData = workflowParameter.split(",");
//
//						//Get the VWStepElement System and User Defined Data Fields
//						//VWParameter[] vwParametersData = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_AND_SYSTEM_DEFINED);
//
//						//Create the FnWorkflowPropertyList Object
//						FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
//
//						//Get the Step Element Responses
//						String[] stepResponses = stepElement.getStepResponses();
//						if(stepResponses != null)
//						{
//							for (int j = 0; j < stepResponses.length; j++)
//							{
//								String stepResponse = stepResponses[j];
//								wiiscLog.log(wiiscLog.INFO, "Step Response: " + stepResponse);
//							}
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "No Step Responses - Possible Launch Step");
//						}
//						wiiscLog.log(wiiscLog.INFO, "===========================================");
//						wiiscLog.log(wiiscLog.INFO, "Setting the Workflow Data Fields");
//
//						//Lock the Step Element
//						//stepElement.doLock(true);
//
//						//Get the VWStepElement System and User Defined Data Fields
//						VWParameter[] vwParametersData = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_AND_SYSTEM_DEFINED);
//
//						//Process Step Element Parameters
//						for (int i = 0; i < vwParametersData.length; i++ )
//						{
//							//FnWorkflowProperty
//							FnWorkflowProperty fnWorkflowProperty = new FnWorkflowProperty();
//
//							//Check parameter mode
//							boolean readOnly = false;
//							if (vwParametersData[i].getMode() == VWModeType.MODE_TYPE_IN)
//							{
//								readOnly = true;
//							}
//							//If the parameter is editable, switch through each data type
//							if (!readOnly)
//							{
//								// For each data type,
//								// check whether the parameter is single or an array
//								// and set the parameter value(s)
//
//								//wiiscLog.log(wiiscLog.INFO, "Updating " + vwParametersData[i].getName());
//
//								switch (vwParametersData[i].getFieldType())
//								{
//								case VWFieldType.FIELD_TYPE_INT:
//									//wiiscLog.log(wiiscLog.INFO, "Integer");
//									if (vwParametersData[i].isArray())
//									{
//										/*int[] arrParamValues = new int[] {1, 2, 3};
//										stepElement.setParameterValue(vwParametersData[i].getName(),arrParamValues,true);*/
//										break;
//									}
//									else
//									{
//										//List of Parameters from GlobalConfig.properties file
//										//wiiscLog.log(wiiscLog.INFO, "Checking to see if any Integer parameters match a Workflow parameter");
//										for (int a = 0; a < workflowParameterData.length; a++)
//										{
//											String param = workflowParameterData[a];
//											//VWParameter vwParameter = stepElement.getParameter(param);
//											//Compare the Global Properties Parameter with the StepElement Parameter
//											//If we have a match, then see if its one of the hardcoded parameters from the original request
//											if (vwParametersData[i].getName().equals(param))
//											{
//												if (param.equals("process_id"))
//												{
//													wiiscLog.log(wiiscLog.INFO, "Updating process_id");
//													stepElement.setParameterValue(vwParametersData[i].getName(), Integer.parseInt(process_id), true);
//													fnWorkflowProperty.setName(param);
//													fnWorkflowProperty.setValue(process_id);
//													wiiscLog.log(wiiscLog.INFO, "process_id: " + process_id);
//													break;
//												}
//											}
//										}
//									}
//								case VWFieldType.FIELD_TYPE_STRING:
//									//wiiscLog.log(wiiscLog.INFO, "String");
//									if (vwParametersData[i].isArray())
//									{
//										/*String[] arrParamValues =	new String[] {"Test_1", "Test_2", "Test_3"};
//										stepElement.setParameterValue(vwParametersData[i].getName(),arrParamValues,true);*/
//										break;
//									} 
//									else
//									{
//										//List of Parameters from GlobalConfig.properties file
//										//wiiscLog.log(wiiscLog.INFO, "Checking to see if any String parameters match a Workflow parameter");
//										for (int a = 0; a < workflowParameterData.length; a++)
//										{
//											String param = workflowParameterData[a];
//											//VWParameter vwParameter = stepElement.getParameter(param);
//											//Compare the Global Properties Parameter with the StepElement Parameter
//											//If we have a match, then see if its one of the hardcoded parameters from the original request
//											if (vwParametersData[i].getName().equals(param))
//											{
//												if (param.equals("process_sys_code"))
//												{
//													wiiscLog.log(wiiscLog.INFO, "Updating process_sys_code");
//													stepElement.setParameterValue(vwParametersData[i].getName(), process_sys_code, true);
//													//fnWorkflow.setFnWorkflowName(process_sys_code);
//													//fnWorkflow.setFnWorkflowRoster(process_sys_code);
//													fnWorkflowProperty.setName(param);
//													fnWorkflowProperty.setValue(process_sys_code);
//													wiiscLog.log(wiiscLog.INFO, "process_sys_code: " + process_sys_code);
//													break;
//												}
//												else if (param.equals("ID_"))
//												{
//													wiiscLog.log(wiiscLog.INFO, "Updating ID_");
//													stepElement.setParameterValue(vwParametersData[i].getName(), account_id, true);
//													//fnWorkflow.setFnWorkflowID(account_id);
//													fnWorkflowProperty.setName(param);
//													fnWorkflowProperty.setValue(account_id);
//													wiiscLog.log(wiiscLog.INFO, "ID_: " + account_id);
//													break;
//												}
//												else
//												{
//													//Do Nothing
//												}
//											}
//										}
//										//String paramValue = "Test";
//										//stepElement.setParameterValue(vwParametersData[i].getName(),paramValue,true);
//									}
//									break;
//
//								case VWFieldType.FIELD_TYPE_ATTACHMENT:
//									//wiiscLog.log(wiiscLog.INFO, "Attachment");
//									if (!vwParametersData[i].isArray())
//									{
//										// Get the value for the VWAttachment
//										VWAttachment attachment = (VWAttachment) vwParametersData[i].getValue();
//										// Set the attachment name
//										attachment.setAttachmentName("Document Title");
//										// Set the attachment description
//										attachment.setAttachmentDescription("A document added programmatically");
//										// Set the type of object (Document)
//										attachment.setType(VWAttachmentType.ATTACHMENT_TYPE_DOCUMENT);
//										// Set the library type and name (CE Object Store)
//										attachment.setLibraryType(VWLibraryType.LIBRARY_TYPE_CONTENT_ENGINE);
//										attachment.setLibraryName("ObjectStoreName");
//										// Set the document ID and version
//										attachment.setId("{BBE5AD7F-2449-4DC3-AA38-012A65EC4286}");
//										attachment.setVersion("{BBE5AD7F-2449-4DC3-AA38-012A65EC4286}");
//										// Set the parameter value
//										stepElement.setParameterValue(vwParametersData[i].getName(),attachment,true);
//									}
//									break;
//								case VWFieldType.FIELD_TYPE_PARTICIPANT:
//									//wiiscLog.log(wiiscLog.INFO, "Participant");
//									// Instantiate a new VWParticipant array
//									//VWParticipant[] participant = new VWParticipant[1];
//									// Set the participant name using username value
//									//String participantUserName = "Administrator";
//									//participant[0].setParticipantName(participantUserName);
//									// Set the parameter value
//									//stepElement.setParameterValue(vwParametersData[i].getName(),participant,true);
//									break;
//								default:
//									// Do not take action for other data types
//									break;
//								}
//
//								//Remove the Old FnWorkflowProperty and Update the FnWorkflowPropertyList with the New FnWorkflowProperty
//								List<FnWorkflowProperty> TempfnWorkflowPropertyList = new ArrayList<FnWorkflowProperty>(10);
//								TempfnWorkflowPropertyList = fnWorkflowPropertyList.getFnWorkflowPropsList();
//								for (int x = 0; x < TempfnWorkflowPropertyList.size(); x++)
//								{
//									FnWorkflowProperty fnWorkProp = new FnWorkflowProperty();
//									fnWorkProp = TempfnWorkflowPropertyList.get(x);
//									//Compare with the Current FnWorkflowProperty and an existing one
//									if (fnWorkProp.getName().equals(fnWorkflowProperty.getName()))
//									{
//										TempfnWorkflowPropertyList.remove(x);
//										fnWorkflowPropertyList.addFnWorkflowProperty(fnWorkflowProperty);
//									}
//								}
//								//fnWorkflowPropertyList.addFnWorkflowProperty(fnWorkflowProperty);
//							}
//							//End of ReadOnly If
//						}
//						//End of For Loop
//						wiiscLog.log(wiiscLog.INFO, "===========================================");
//						//Update additional custom data fields
//						//More to add later
//
//						// Set the value for the system-defined Response parameter
//						if (stepElement.getStepResponses() != null) {
//							wiiscLog.log(wiiscLog.INFO, "Step Responses");
//							String responseValue = "Ok";
//							stepElement.setSelectedResponse(responseValue);
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "No Step Responses");
//							wiiscLog.log(wiiscLog.INFO, "===========================================");
//						}
//
//						//Workflow Action Types
//						final int ACTION_TYPE_REASIGN = 1;
//						final int ACTION_TYPE_RETURN = 2;
//						final int ACTION_TYPE_ABORT = 3;
//						final int ACTION_TYPE_SAVE= 4;
//						final int ACTION_TYPE_DISPATCH = 5;
//
//						// Action to perform on the Step Element
//						int actionToPerform = 5; // Dispatch
//
//						switch (actionToPerform) {
//						case ACTION_TYPE_REASIGN:
//							wiiscLog.log(wiiscLog.INFO, "Reasign");
//							// Determine whether a step element
//							// can be reassigned and reassign it
//							if (stepElement.getCanReassign()) {
//								String participantName = "Administrator";
//								stepElement.doReassign(participantName,true,null);
//							}
//							break;
//						case ACTION_TYPE_RETURN:
//							wiiscLog.log(wiiscLog.INFO, "Return");
//							// Determine whether a step element can be returned to the
//							// queue from which the user delegated or reassigned it and
//							// return it
//							if (stepElement.getCanReturnToSource()) {
//								stepElement.doReturnToSource();
//							}
//							break;
//						case ACTION_TYPE_ABORT:
//							wiiscLog.log(wiiscLog.INFO, "Abort");
//							// Cancel the changes to the work item
//							// without advancing it in the workflow
//							stepElement.doAbort();
//							break;
//						case ACTION_TYPE_SAVE:
//							wiiscLog.log(wiiscLog.INFO, "Save");
//							// Save the changes to the work item
//							// and unlock it without advancing it in the workflow
//							stepElement.doSave(true);
//							break;
//						case ACTION_TYPE_DISPATCH:
//							wiiscLog.log(wiiscLog.INFO, "Dispatch");
//							// Save the changes to the work item
//							// and advance it in the workflow
//							stepElement.doDispatch();
//							break;
//						}
//
//						//Dispatch the Workflow Launch Step
//						wiiscLog.log(wiiscLog.INFO, "===========================================");
//						wiiscLog.log(wiiscLog.INFO, "Workflow Process " + process_sys_code + " has initiated");
//						//Update FnWorkflow
//						fnWorkflow.setErrorFlag(0);
//						fnWorkflow.setErrorMessage("");
//						fnWorkflow.setFnWorkflowStatus("INITIATED");
//						//Get the Queue Name
//						fnWorkflow.setFnWorkflowQueue(stepElement.getCurrentQueueName());
//						//Add the FnWorkflowPropertyList to the FnWorkflow
//						//fnWorkflow.setFnWorkflowProps(fnWorkflowPropertyList);
//						//Add the FnWorkflowPropertyList to the FnWorkflow
//						fnWorkflow.setFnWorkflowPropertyList(fnWorkflowPropertyList);
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "Workflow Process " + process_sys_code + " failed to launch");
//						//Update FnWorkflow
//						fnWorkflow.setErrorFlag(1);
//						fnWorkflow.setErrorMessage("Process " + process_sys_code + " FAILED to launch");
//						fnWorkflow.setFnWorkflowStatus("Process " + process_sys_code + " FAILED to launch");
//					}
//				}
//				else
//				{
//					//Missing Parameters
//					wiiscLog.log(wiiscLog.INFO, "The Workflow Process " + process_sys_code + " is Invalid or does not exist");
//					//Update FnWorkflow
//					fnWorkflow.setErrorFlag(1);
//					fnWorkflow.setErrorMessage("Process " + process_sys_code + " is Invalid");
//					fnWorkflow.setFnWorkflowStatus("Process " + process_sys_code + " is Invalid");
//				}
//
//				//Logoff the Workflow Server
//				wiiscLog.log(wiiscLog.INFO, "Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				wiiscLog.log(wiiscLog.INFO, "Logged off");
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(1);
//				fnWorkflow.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//				fnWorkflow.setFnWorkflowStatus("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Release the VWSession
//				vwSession = null;
//			}
//		}
//		catch(VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update the FnWorkflow Object
//			fnWorkflow.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflow.setErrorMessage(ex.getMessage());
//			fnWorkflow.setFnWorkflowStatus("Workflow Login FAILED");
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> initWorkflow()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflow;
//	}
//
//	//Create Workflows
//	public FnWorkflowList createWorkflows(String process_sys_code, String count, WIISCLog wiiscLog)
//	{
//		//Create the FnWorkflowList Object
//		FnWorkflowList fnWorkflowList = new FnWorkflowList();
//		//Workflow Count
//		int workflowCount = 0;
//		workflowCount = Integer.parseInt(count);
//		//Current Workflow Count
//		int currentWorkflowCount = 0;
//
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> createWorkflows()");
//
//			//Login to the Workflow Server
//			vwSession = loginWorkflow(wiiscLog);
//
//			if (vwSession != null)
//			{
//				wiiscLog.log(wiiscLog.INFO, "Logged in successfully to the Workflow Server");
//				//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//				//Update the FnWorkflowList Object
//				fnWorkflowList.setErrorFlag(0);
//				fnWorkflowList.setErrorMessage("");
//				//Get the Work Classes
//				String[] workClassNames = vwSession.fetchWorkClassNames(true);
//				List<String> validRosters = new ArrayList<String>(10);
//								
//				//Build valid Roster List
//				for (int i = 0; i < workClassNames.length; i++)
//				{
//					VWRoster vwRoster = null;
//					VWWorkflowDefinition vwWorkflowDef = null;
//					try
//					{
//						wiiscLog.log(wiiscLog.INFO, "Checking Roster: " + workClassNames[i]);
//						vwRoster = vwSession.getRoster(workClassNames[i]);
//						vwWorkflowDef = vwSession.fetchWorkflowDefinition(-1, workClassNames[i], false);
//					}
//					catch (Exception e)
//					{
//						vwRoster = null;
//						vwWorkflowDef = null;
//					}
//					//If neither were null then add them to the list
//					if (vwRoster != null && vwWorkflowDef != null)
//					{
//						wiiscLog.log(wiiscLog.INFO, "Roster and Workflow Definition exist");
//						//Add Roster to valid Rosters list
//						validRosters.add(workClassNames[i]);
//					}
//				}
//				
//
//				/**********************************************
//				 * Create Workflows until Workflow Count is 0
//				 **********************************************/
//				if (process_sys_code.length() == 0 && workflowCount > 0)
//				{
//					//ALL Workflows - no process_sys_code, no account_id, no Participant specified
//					wiiscLog.log(wiiscLog.INFO, "Create Workflows until Workflow Count is 0");
//					wiiscLog.log(wiiscLog.INFO, "Creating " + workflowCount + " Workflows");
//					//Retrieve Work Class Names
//					//String[] workClassNames = vwSession.fetchWorkClassNames(true);
//					
//					//Check workClassNames
//					if (validRosters.size() > 1)
//					{
//						//Workflow Count loop
//						while (currentWorkflowCount < workflowCount)
//						{
//							//Loop and create a Workflow for each Roster
//							for (int i = 0; i < validRosters.size(); i++)
//							{
//								//Check to make sure we aren't using DefaultRoster
//								if (!validRosters.get(i).equals("DefaultRoster"))
//								{
//									//Check the Current Workflow Count against Workflow Count
//									if (currentWorkflowCount <= workflowCount)
//									{
//										FnWorkflowList tempFnWorkflowList = new FnWorkflowList();
//										tempFnWorkflowList = createWorkflowsByRoster(vwSession, validRosters.get(i), wiiscLog);
//										//Add the TempFnWorkflowList to the Master FnWorkflowList
//										fnWorkflowList.addFnWorkflowList(tempFnWorkflowList);
//										tempFnWorkflowList.getFnWorkflowList().clear();
//										//Increment currentWorkflowCount
//										currentWorkflowCount++;
//									}
//									else
//									{
//										//Done Creating Workflows
//										break;
//									}
//								}										
//								//Check the Current Workflow Count against Workflow Count
//								if (currentWorkflowCount >= workflowCount)
//								{
//									//Done Creating Workflows
//									break;
//								}
//							}
//						}
//					}
//					else if (validRosters.size() == 1)
//					{
//						//Workflow Count loop
//						while (currentWorkflowCount < workflowCount)
//						{
//							//Check to make sure we aren't using DefaultRoster
//							if (validRosters.get(0).equals("DefaultRoster"))
//							{
//								//Done Creating Workflows
//								break;
//							}
//							else
//							{
//								wiiscLog.log(wiiscLog.INFO, "Checking Roster: " + validRosters.get(0));
//								//Check the Current Workflow Count against Workflow Count
//								if (currentWorkflowCount <= workflowCount)
//								{
//									FnWorkflowList tempFnWorkflowList = new FnWorkflowList();
//									tempFnWorkflowList = createWorkflowsByRoster(vwSession, validRosters.get(0), wiiscLog);
//									//Add the TempFnWorkflowList to the Master FnWorkflowList
//									fnWorkflowList.addFnWorkflowList(tempFnWorkflowList);
//									tempFnWorkflowList.getFnWorkflowList().clear();
//									//Increment currentWorkflowCount
//									currentWorkflowCount++;
//								}
//								else
//								{
//									//Done Creating Workflows
//									break;
//								}
//							}
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "There were No Rosters, so there are No Workflows");
//						//Update the FnWorkflowList Object
//						fnWorkflowList.setErrorFlag(1);
//						fnWorkflowList.setErrorMessage("There were No Rosters, so there are No Workflows");
//					}					
//				}
//				/********************************************************
//				 * Create Workflows By Roster until Workflow Count is 0 
//				 ********************************************************/
//				else if (process_sys_code.length() > 0 && workflowCount > 0)
//				{
//					//ALL Workflows By Roster - no account_id, no Participant specified
//					wiiscLog.log(wiiscLog.INFO, "Create Workflows By Roster until Workflow Count is 0");
//					wiiscLog.log(wiiscLog.INFO, "Creating " + workflowCount + " Workflows");
//					
//					//Boolean to Validate the Requested Process
//					boolean validProcess = false;
//										
//					//Verify the Workflow Process is Valid			
//					for (int a = 0; a < validRosters.size(); a++)
//					{
//						wiiscLog.log(wiiscLog.INFO, "Checking Roster: " + validRosters.get(a));
//						//Verify that the Process Parameter is a valid WorkClass to Launch a Workflow
//						if (validRosters.get(a).equals(process_sys_code))
//						{
//							//Valid Process
//							validProcess = true;
//							break;
//						}
//					}
//					//Check if a Valid Process was Found
//					if (validProcess)
//					{
//						//Workflow Count loop
//						while (currentWorkflowCount < workflowCount)
//						{
//							//Check the Current Workflow Count against Workflow Count
//							if (currentWorkflowCount <= workflowCount)
//							{
//								FnWorkflowList tempFnWorkflowList = new FnWorkflowList();
//								tempFnWorkflowList = createWorkflowsByRoster(vwSession, process_sys_code, wiiscLog);
//								//Add the TempFnWorkflowList to the Master FnWorkflowList
//								fnWorkflowList.addFnWorkflowList(tempFnWorkflowList);
//								tempFnWorkflowList.getFnWorkflowList().clear();
//								//Increment currentWorkflowCount
//								currentWorkflowCount++;
//							}
//							else
//							{
//								//Done Creating Workflows
//								break;
//							}
//						}
//					}
//					else
//					{
//						//Missing Parameters
//						wiiscLog.log(wiiscLog.INFO, "The Workflow Process " + process_sys_code + " is Invalid or does not exist");
//						//Update FnWorkflowList
//						fnWorkflowList.setErrorFlag(1);
//						fnWorkflowList.setErrorMessage("Process " + process_sys_code + " is Invalid");
//					}
//				}
//				else
//				{
//					//Do Nothing
//				}
//
//				//Logoff the Workflow Server
//				wiiscLog.log(wiiscLog.INFO, "Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				wiiscLog.log(wiiscLog.INFO, "Logged off");
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflowList Object
//				fnWorkflowList.setErrorFlag(1);
//				fnWorkflowList.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//			}
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update the FnWorkflowList Object
//			fnWorkflowList.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowList.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> createWorkflows()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflowList;
//	}
//	
//	//Delete Workflows
//	public FnWorkflowList deleteWorkflows(String process_sys_code, String account_id, String user, String region, WIISCLog wiiscLog)
//	{
//		//Create the FnWorkflowList Object
//		FnWorkflowList fnWorkflowList = new FnWorkflowList();
//
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> deleteWorkflows()");
//
//			//Login to the Workflow Server
//			vwSession = loginWorkflow(wiiscLog);
//
//			if (vwSession != null)
//			{
//				wiiscLog.log(wiiscLog.INFO, "Logged in successfully to the Workflow Server");
//				//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//				//Update the FnWorkflowList Object
//				fnWorkflowList.setErrorFlag(0);
//				fnWorkflowList.setErrorMessage("");
//
//				/***********************
//				 * Delete ALL Workflows
//				 ***********************/
//				if (process_sys_code.length() == 0 && account_id.length() == 0 && user.length() == 0 && region.equals("true"))
//				{
//					//ALL Workflows - no process_sys_code, no account_id, no Participant specified
//					wiiscLog.log(wiiscLog.INFO, "Delete ALL Workflows");
//
//					//Get String[] of Roster
//					String[] rosterNames = getRosters(vwSession, wiiscLog);
//					//Check rosterNames
//					if (rosterNames.length > 0)
//					{
//						//Loop and Get Workflows from each Roster
//						for (int i = 0; i < rosterNames.length; i++)
//						{
//							FnWorkflowList tempFnWorkflowList = new FnWorkflowList();
//							tempFnWorkflowList = deleteWorkflowsByRoster(vwSession, rosterNames[i], wiiscLog);
//							//Add the TempFnWorkflowList to the Master FnWorkflowList
//							fnWorkflowList.getFnWorkflowList().addAll(tempFnWorkflowList.getFnWorkflowList());
//							tempFnWorkflowList.getFnWorkflowList().clear();
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "There were No Rosters, so there are No Workflows");
//						//Update the FnWorkflowList Object
//						fnWorkflowList.setErrorFlag(1);
//						fnWorkflowList.setErrorMessage("There were No Rosters, so there are No Workflows");
//					}					
//				}
//				/*********************************
//				 * Delete ALL Workflows By Roster
//				 *********************************/
//				else if (process_sys_code.length() > 0 && account_id.length() == 0 && user.length() == 0 && !region.equals("true"))
//				{
//					//ALL Workflows By Roster - no account_id, no Participant specified
//					wiiscLog.log(wiiscLog.INFO, "Delete ALL Workflows By Roster");
//
//					FnWorkflowList tempFnWorkflowList = new FnWorkflowList();
//					tempFnWorkflowList = deleteWorkflowsByRoster(vwSession, process_sys_code, wiiscLog);
//					//Add the TempFnWorkflowList to the Master FnWorkflowList
//					fnWorkflowList.getFnWorkflowList().addAll(tempFnWorkflowList.getFnWorkflowList());
//					tempFnWorkflowList.getFnWorkflowList().clear();
//				}
//				else
//				{
//					//Do Nothing
//				}
//
//				//Logoff the Workflow Server
//				wiiscLog.log(wiiscLog.INFO, "Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				wiiscLog.log(wiiscLog.INFO, "Logged off");
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflowList Object
//				fnWorkflowList.setErrorFlag(1);
//				fnWorkflowList.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//			}
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update the FnWorkflowList Object
//			fnWorkflowList.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowList.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> deleteWorkflows()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflowList;
//	}
//	
//	private boolean checkPropertiesFileExist(String propName, WIISCLog wiiscLog)
//	{
//		boolean propertiesExist = false;
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> checkPropertiesFileExist()");
//			//ResourceBundle res = ResourceBundle.getBundle(propName);
//			ResourceBundle res = null;
//			LocalResource resConfig = getLocalResource(propName);
//			res = resConfig.getLocalBundle(resConfig.getBundlePath(), resConfig.getBundleFile());
//			
//			//Check if the ResourceBundle exists
//			if (res != null)
//			{
//				wiiscLog.log(wiiscLog.INFO, "Properties file " + propName + " exists");
//				System.out.println("Properties file " + propName + " exists");
//				propertiesExist = true;
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Properties file " + propName + " does not exist");
//				System.out.println("Properties file " + propName + " does not exist");
//				propertiesExist = false;
//			}
//		}
//		catch (MissingResourceException e)
//		{
//			//Properties file does not exist
//			wiiscLog.log(wiiscLog.INFO, "Properties file " + propName + " does not exist");
//			System.out.println("Properties file " + propName + " does not exist");
//			propertiesExist = false;
//		}
//		catch (Exception e)
//		{
//			//Properties file does not exist
//			wiiscLog.log(wiiscLog.INFO, "Properties file " + propName + " does not exist");
//			System.out.println("Properties file " + propName + " does not exist");
//			propertiesExist = false;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> checkPropertiesFileExist()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		
//		return propertiesExist;
//	}	
//
//	//Create Workflows for a Roster
//	private FnWorkflowList createWorkflowsByRoster(VWSession vwSession, String rosterName, WIISCLog wiiscLog)
//	{
//		//Create the FnWorkflowList Object
//		FnWorkflowList fnWorkflowList = new FnWorkflowList();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> createWorkflowsByRoster()");
//			
//			//Create the Workflow
//			VWStepElement stepElement = vwSession.createWorkflow(rosterName);
//			//Create a dummy FnWorkflow
//			FnWorkflow fnWorkflow = new FnWorkflow();
//			//Create a FnWorkflow Result
//			FnWorkflow fnWorkflowResult = new FnWorkflow();
//
//			//Check if a Valid Process was Launched
//			if (stepElement != null)
//			{
//				//Initial Workflow Launch Step Properties
//				wiiscLog.log(wiiscLog.INFO, "Workflow Process " + rosterName + " has launched");
//
//				//Update the FnWorkflowResult
//				fnWorkflowResult = updateFnWorkflowInfo(stepElement, "New", wiiscLog);
//
//				//Get the Step Element Responses
//				String[] stepResponses = stepElement.getStepResponses();
//				if(stepResponses != null)
//				{
//					for (int j = 0; j < stepResponses.length; j++)
//					{
//						String stepResponse = stepResponses[j];
//						wiiscLog.log(wiiscLog.INFO, "Step Response: " + stepResponse);
//					}
//					String responseValue = "Ok";
//					wiiscLog.log(wiiscLog.INFO, "Applying Step Response: " + responseValue);
//					stepElement.setSelectedResponse(responseValue);
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "No Step Responses - Possible Launch Step");
//				}
//
//				//DoStepElementAction - Dispatch the Workflow
//				fnWorkflowResult = doStepElementAction(stepElement, "Dispatch", "", wiiscLog);
//
//				//Update additional custom data fields
//				//More to add later
//
//				//Dispatch the Workflow Launch Step
//				wiiscLog.log(wiiscLog.INFO, "===========================================");
//				wiiscLog.log(wiiscLog.INFO, "Workflow Process " + rosterName + " has initiated");
//				//Update FnWorkflow
//				fnWorkflowResult.setErrorFlag(0);
//				fnWorkflowResult.setErrorMessage("");
//				fnWorkflowResult.setFnWorkflowStatus("INITIATED");
//				//Get the Queue Name
//				fnWorkflowResult.setFnWorkflowQueue(stepElement.getCurrentQueueName());
//				//Add the FnWorkflowPropertyList to the FnWorkflow
//				//fnWorkflowResult.setFnWorkflowPropertyList(fnWorkflowPropertyListResult);
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Workflow Process " + rosterName + " failed to launch");
//				//Update FnWorkflow
//				fnWorkflowResult.setErrorFlag(1);
//				fnWorkflowResult.setErrorMessage("Process " + rosterName + " FAILED to launch");
//				fnWorkflowResult.setFnWorkflowStatus("Process " + rosterName + " FAILED to launch");
//			}
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			//Update the FnWorkflowList Object
//			fnWorkflowList.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowList.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> createWorkflowsByRoster()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		
//		return fnWorkflowList;
//	}
//	
//	//Delete Workflows for a Roster
//	private FnWorkflowList deleteWorkflowsByRoster(VWSession vwSession, String rosterName, WIISCLog wiiscLog)
//	{
//		//Create the FnWorkflowList Object
//		FnWorkflowList fnWorkflowList = new FnWorkflowList();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> deleteWorkflowsByRoster()");
//			
//			VWRoster vwRoster = vwSession.getRoster(rosterName);
//			int workflowCount = vwRoster.fetchCount();
//
//			//Check to see if we need to run the Roster Query to delete Workflows more than once
//			while (workflowCount > 0)
//			{
//				VWRosterQuery query = getRosterQuery(vwRoster, null, null, wiiscLog);
//				//Check query
//				if (query != null)
//				{
//					//Process the Results
//					while(query.hasNext())
//					{
//						VWRosterElement rosterItem = (VWRosterElement) query.next();
//						VWWorkObject workObj = rosterItem.fetchWorkObject(false, false);
//						if (workObj != null)
//						{
//							//Update the FnWorkflow
//							FnWorkflow fnWorkflow = new FnWorkflow();
//							fnWorkflow.setFnWorkflowRoster(workObj.getRosterName());
//							fnWorkflow.setFnWorkflowQueue(workObj.getCurrentQueueName());
//							fnWorkflow.setFnWorkflowID(workObj.getWorkObjectNumber());
//
//							//Lock the WorkObject
//							workObj.doLock(true);
//							//Terminate the Workflow
//							workObj.doTerminate();
//
//							//Add the FnWorkflow to the FnWorkflowList
//							fnWorkflowList.addFnWorkflow(fnWorkflow);
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "Failed to get a WorkObject");
//							//Update the FnWorkflow Object
//							fnWorkflowList.setErrorFlag(1);
//							fnWorkflowList.setErrorMessage("Failed to get a WorkObject");
//						}
//					}
//				}
//
//				//Check the Workflow Count again
//				workflowCount = vwRoster.fetchCount();
//			}
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update the FnWorkflowList Object
//			fnWorkflowList.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowList.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> deleteWorkflowsByRoster()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		
//		return fnWorkflowList;
//	}
//
//	//Get PE Workflow Info
//	public FnWorkflow getWorkflowInfo(String process, String step, String user, String propName, String propValue, WIISCLog wiiscLog)
//	{
//		//Create the FnWorkflow Object
//		FnWorkflow fnWorkflow = new FnWorkflow();
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//
//		//Get the Filter Name
//		//String filterName = "";
//		String[] filterName = null;
//		//Get the Filter Value
//		//String filterValue = "";
//		String[] filterValue = null;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getWorkflowInfo()");
//			
//			wiiscLog.log(wiiscLog.INFO, "Process: " + process);
//			wiiscLog.log(wiiscLog.INFO, "Step: " + step);
//			wiiscLog.log(wiiscLog.INFO, "User: " + user);
//			wiiscLog.log(wiiscLog.INFO, "Property Name: " + propName);
//			wiiscLog.log(wiiscLog.INFO, "Property Value: " + propValue);
//			
//			//Login to the Workflow Server
//			vwSession = loginWorkflow(wiiscLog);
//
//			if (vwSession != null)
//			{
//				wiiscLog.log(wiiscLog.INFO, "Logged in successfully to the Workflow Server");
//				//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(0);
//				fnWorkflow.setErrorMessage("");
//
//				/*****************************************************************************
//				 * Get the Workflow by a Property and Property Value - workflow data field
//				 *****************************************************************************/
//				if (step.length() == 0 && propName.length() > 0 && propValue.length() > 0)
//				{
//					//By Property and Property Value only
//					wiiscLog.log(wiiscLog.INFO, "Get the Workflow by Property and Property Value");
//
//					//Initialize the Array
//					filterName = new String[1];
//					filterValue = new String[1];
//					filterName[0] = propName;
//					filterValue[0] = propValue;
//
//					//Get String[] of Queues
//					String[] queueNames = getQueues(vwSession, wiiscLog);
//					//Check queueNames
//					if (queueNames.length > 0)
//					{
//						//Loop and Get Workflows from each Queue
//						for (int i = 0; i < queueNames.length; i++)
//						{
//							//Exclude the System Queues
//							if (queueNames[i].contains("Instruction") == false &&
//									queueNames[i].contains("(") == false &&
//									queueNames[i].equals("Conductor") == false &&
//									queueNames[i].equals("WSRequest") == false &&
//									queueNames[i].equals("CE_Operations") == false)
//							{
//								//Get the Workflow
//								fnWorkflow = getFnWorkflowByQueue(vwSession, queueNames[i], filterName, filterValue, wiiscLog);
//								//Check the FnWorkflow to see if its Null
//								if (fnWorkflow != null)
//								{
//									break;
//								}
//							}
//						}
//						//Check if the FnWorkflow was still null after checking each Queue
//						if (fnWorkflow == null)
//						{
//							wiiscLog.log(wiiscLog.INFO, "There was No Workflow found for the Property " + propName + " and Property Value " + propValue);
//							//Update the Error Flag to 0 since this is not a real Error
//							fnWorkflow = new FnWorkflow();
//							fnWorkflow.setErrorFlag(0);
//							fnWorkflow.setErrorMessage("");
//							wiiscLog.log(wiiscLog.INFO, "Workflow returning with ErrorFlag of 0");
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "There were No Queues, so there are No Workflows");
//						//Update the FnWorkflow Object
//						fnWorkflow.setErrorFlag(1);
//						fnWorkflow.setErrorMessage("There were No Queues, so there are No Workflows");
//					}
//				}
//				/*********************************************************************************
//				 * Get the Workflow by a Step, Property and Property Value - workflow data field
//				 *********************************************************************************/
//				else if (step.length() > 0 && propName.length() > 0 && propValue.length() > 0)
//				{
//					//By Property and Property Value only
//					wiiscLog.log(wiiscLog.INFO, "Get the Workflow by a Step, Property and Property Value");
//
//					//Initialize the Array
//					filterName = new String[1];
//					filterValue = new String[1];
//					filterName[0] = propName;
//					filterValue[0] = propValue;
//
//					//Set the Queue
//					String queueName = step;
//					//Check queueName
//					if (queueName.length() > 0)
//					{
//						//Exclude the System Queues
//						if (!queueName.contains("Instruction") &&
//								!queueName.contains("(") &&
//								!queueName.equals("Conductor") &&
//								!queueName.equals("WSRequest") &&
//								!queueName.equals("CE_Operations"))
//						{
//							//Get the Workflow
//							fnWorkflow = getFnWorkflowByQueue(vwSession, queueName, filterName, filterValue, wiiscLog);
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "There was No Queue, so there are No Workflows");
//						//Update the FnWorkflow Object
//						fnWorkflow.setErrorFlag(1);
//						fnWorkflow.setErrorMessage("There was No Queue, so there are No Workflows");
//					}
//				}
//				else
//				{
//					//Return an Error
//					wiiscLog.log(wiiscLog.INFO, "The Parameters Process, Step, Property Name and Property Value cannot be used together, so there are No Workflows");
//					//Update the FnWorkflow Object
//					fnWorkflow.setErrorFlag(1);
//					fnWorkflow.setErrorMessage("The Parameters Process, Step, Property Name and Property Value cannot be used together, so there are No Workflows");
//				}
//
//				//Logoff the Workflow Server
//				wiiscLog.log(wiiscLog.INFO, "Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				wiiscLog.log(wiiscLog.INFO, "Logged off");
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(1);
//				fnWorkflow.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//			}
//
//		}
//		catch(VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			//Update the FnWorkflow Object
//			fnWorkflow.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflow.setErrorMessage(ex.getMessage());
//			fnWorkflow.setFnWorkflowStatus("Exception in getWorkflowInfo()");
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getWorkflowInfo()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflow;
//	}
//
//	public FnBaseXML getWorkflowList(String process, String step, String user, String propName, String propValue, String sortBy, WIISCLog wiiscLog)
//	{
//		//Create an FnBaseXML
//		FnBaseXML fnBaseXML = new FnBaseXML();
//		//Create the FnWorkflowList Object
//		FnWorkflowList fnWorkflowList = new FnWorkflowList();
//
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//		//FileNet DB Connection
//		SQLServerConnection con = null;
//		//Boolean to continue processing
//		boolean beginProcessing = false;
//
//		//Get the Filter Name
//		//String filterName = "";
//		String[] filterName = null;
//		//Get the Filter Value
//		//String filterValue = "";
//		String[] filterValue = null;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getWorkflowList()");
//			
//			wiiscLog.log(wiiscLog.INFO, "Process: " + process);
//			wiiscLog.log(wiiscLog.INFO, "Step: " + step);
//			wiiscLog.log(wiiscLog.INFO, "User: " + user);
//			wiiscLog.log(wiiscLog.INFO, "Property Name: " + propName);
//			wiiscLog.log(wiiscLog.INFO, "Property Value: " + propValue);
//			wiiscLog.log(wiiscLog.INFO, "Sort By: " + sortBy);
//
//			//Login to the Workflow Server
//			vwSession = loginWorkflow(wiiscLog);
//
//			if (vwSession != null)
//			{
//				wiiscLog.log(wiiscLog.INFO, "Logged in successfully to the Workflow Server");
//				//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//				//Update the FnWorkflowList Object
//				fnWorkflowList.setErrorFlag(0);
//				fnWorkflowList.setErrorMessage("");
//
//				/****************************************
//				 * Get ALL Workflows using Roster Search
//				 ****************************************/
//				//Add later if needed
//				/**************************************************
//				 * Get ALL Workflows using Queue Search - Workpool 
//				 **************************************************/
//				if (process.length() == 0 && step.length() == 0 && user.length() == 0 && propName.length() == 0 && propValue.length() == 0)
//				{
//					//ALL Workflows - no process_sys_code, no account_id, no Participant specified
//					wiiscLog.log(wiiscLog.INFO, "Get ALL Workflows");
//
//					if (globalConfig.containsKey("workflowSearchUserField"))
//					{
//						//Check if the Property setting has a value
//						if (globalConfig.getString("workflowSearchUserField").length() > 0)
//						{
//							//Initialize the Array
//							filterName = new String[1];
//							filterValue = new String[1];
//							filterName[0] = globalConfig.getString("workflowSearchUserField");
//							filterValue[0] = user;
//						}
//					}
//					
//					//Get String[] of Queue
//					String[] queueNames = getQueues(vwSession, wiiscLog);
//					//Check queueNames
//					if (queueNames.length > 0)
//					{
//						//Check if DB FileNet Query will be used or FileNet API
//						if (globalConfig.getString("workflowDBSearchEnabled").equals("true"))
//						{
//							con = loginWorkflowDB(wiiscLog);
//							if (con != null)
//							{
//								beginProcessing = true;
//							}
//						}
//						else
//						{
//							beginProcessing = true;
//						}
//						
//						//Verify Processing can begin
//						if (beginProcessing)
//						{
//							//Loop and Get Workflows from each Queue
//							for (int i = 0; i < queueNames.length; i++)
//							{
//								//Exclude the System Queues
//								if (!queueNames[i].contains("Instruction") &&
//										!queueNames[i].contains("(") &&
//										!queueNames[i].equals("Conductor") &&
//										!queueNames[i].equals("WSRequest") &&
//										!queueNames[i].equals("CE_Operations"))
//								{
//												
//									//Get the FnWorkflowList by Queue Name
//									fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueNames[i], filterName, filterValue, sortBy, wiiscLog);
//																	
//									//Check FnWorkflowList size
//									if (fnWorkflowList.getCount() == 0 || fnWorkflowList.getErrorFlag() == 0)
//									{
//										fnWorkflowList.setStepName(queueNames[i]);
//										//Add the List to the FnBaseXML
//										fnBaseXML.addFnWorkflowList(fnWorkflowList);
//									}
//									else
//									{
//										wiiscLog.log(wiiscLog.INFO, "Error with Queue that needs addressed");
//										//Update the FnBaseXML Object
//										fnBaseXML.setErrorFlag(1);
//										fnBaseXML.setErrorMessage("Error with Queue that needs addressed");
//									}
//								}
//							}
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "Processing cannot begin because a DB Connection could not be established");
//						}
//												
//						//Close the FileNet DB Connection
//						if (con != null)
//						{
//							try
//							{
//								wiiscLog.log(wiiscLog.INFO, "Closing the FileNet DB Connection");
//								con.close();
//							}
//							catch(Exception e)
//							{
//								wiiscLog.log(wiiscLog.INFO, "Error closing the FileNet DB Connection");
//								wiiscLog.log("ERROR", e.getMessage());
//							}
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "There were No Queues, so there are No Workflows");
//						//Update the FnBaseXML Object
//						fnBaseXML.setErrorFlag(1);
//						fnBaseXML.setErrorMessage("There were No Queues, so there are No Workflows");
//					}					
//				}
//				/**********************************
//				 * Get ALL Workflows for a Process
//				 **********************************/
//				else if (process.length() > 0 && step.length() == 0 && user.length() == 0 && propName.length() == 0 && propValue.length() == 0)
//				{
//					//By process only
//					wiiscLog.log(wiiscLog.INFO, "Get ALL Workflows for a Process");
//					
//					//Get the FnWorkflowList
//					fnWorkflowList = getFnWorkflowListByRoster(vwSession, process, filterName, filterValue, wiiscLog);
//					//Add the List to the FnBaseXML
//					fnBaseXML.addFnWorkflowList(fnWorkflowList);
//				}
//				/**********************************
//				 * Get ALL Workflows for a Step
//				 **********************************/
//				else if (process.length() == 0 && step.length() > 0 && user.length() == 0 && propName.length() == 0 && propValue.length() == 0)
//				{
//					//By step only
//					wiiscLog.log(wiiscLog.INFO, "Get ALL Workflows for a Step");
//					
//					if (globalConfig.containsKey("workflowSearchUserField"))
//					{
//						//Check if the Property setting has a value
//						if (globalConfig.getString("workflowSearchUserField").length() > 0)
//						{
//							//Initialize the Array
//							filterName = new String[1];
//							filterValue = new String[1];
//							filterName[0] = globalConfig.getString("workflowSearchUserField");
//							filterValue[0] = "";
//						}
//					}
//					
//					//Check if DB FileNet Query will be used or FileNet API
//					if (globalConfig.getString("workflowDBSearchEnabled").equals("true"))
//					{
//						con = loginWorkflowDB(wiiscLog);
//						if (con != null)
//						{
//							beginProcessing = true;
//						}
//					}
//					else
//					{
//						beginProcessing = true;
//					}
//
//					//Verify Processing can begin
//					if (beginProcessing)
//					{
//						//Get the FnWorkflowList
//						fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, step, filterName, filterValue, sortBy, wiiscLog);
//						//Add the List to the FnBaseXML
//						fnBaseXML.addFnWorkflowList(fnWorkflowList);
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "Processing cannot begin because a DB Connection could not be established");
//					}
//											
//					//Close the FileNet DB Connection
//					if (con != null)
//					{
//						try
//						{
//							wiiscLog.log(wiiscLog.INFO, "Closing the FileNet DB Connection");
//							con.close();
//						}
//						catch(Exception e)
//						{
//							wiiscLog.log(wiiscLog.INFO, "Error closing the FileNet DB Connection");
//							wiiscLog.log("ERROR", e.getMessage());
//						}
//					}
//				}
//				/*****************************************************************************
//				 * Get ALL Workflows by a Property and Property Value - workflow data field
//				 *****************************************************************************/
//				else if (process.length() == 0 && step.length() == 0 && user.length() == 0 && propName.length() > 0 && propValue.length() > 0)
//				{
//					//By Property and Property Value only
//					wiiscLog.log(wiiscLog.INFO, "Get ALL Workflows by Property and Property Value");
//
//					//Get String[] of Queues
//					String[] queueNames = getQueues(vwSession, wiiscLog);
//					//Check queueNames
//					if (queueNames.length > 0)
//					{
//						if (globalConfig.containsKey("workflowSearchUserField"))
//						{
//							//Check if the Property setting has a value
//							if (globalConfig.getString("workflowSearchUserField").length() == 0)
//							{
//								//Initialize the Array
//								filterName = new String[1];
//								filterValue = new String[1];
//								filterName[0] = propName;
//								filterValue[0] = propValue;
//								wiiscLog.log(wiiscLog.INFO, "Property Value Only");
//							}
//							else
//							{
//								//Initialize the Array
//								filterName = new String[2];
//								filterValue = new String[2];
//								filterName[0] = globalConfig.getString("workflowSearchUserField");
//								filterValue[0] = user;
//								filterName[1] = propName;
//								filterValue[1] = propValue;
//								wiiscLog.log(wiiscLog.INFO, "Property Value and User");
//							}
//						}
//												
//						//Check if DB FileNet Query will be used or FileNet API
//						if (globalConfig.getString("workflowDBSearchEnabled").equals("true"))
//						{
//							con = loginWorkflowDB(wiiscLog);
//							if (con != null)
//							{
//								beginProcessing = true;
//							}
//						}
//						else
//						{
//							beginProcessing = true;
//						}
//						
//						//Verify Processing can begin
//						if (beginProcessing)
//						{
//							//Loop and Get Workflows from each Queue
//							for (int i = 0; i < queueNames.length; i++)
//							{
//								//Exclude the System Queues
//								if (!queueNames[i].contains("Instruction") &&
//										!queueNames[i].contains("(") &&
//										!queueNames[i].equals("Conductor") &&
//										!queueNames[i].equals("WSRequest") &&
//										!queueNames[i].equals("CE_Operations"))
//								{
//									fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueNames[i], filterName, filterValue, sortBy, wiiscLog);
//									//Add the List to the FnBaseXML
//									fnBaseXML.addFnWorkflowList(fnWorkflowList);
//								}
//							}
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "Processing cannot begin because a DB Connection could not be established");
//						}
//												
//						//Close the FileNet DB Connection
//						if (con != null)
//						{
//							try
//							{
//								wiiscLog.log(wiiscLog.INFO, "Closing the FileNet DB Connection");
//								con.close();
//							}
//							catch(Exception e)
//							{
//								wiiscLog.log(wiiscLog.INFO, "Error closing the FileNet DB Connection");
//								wiiscLog.log("ERROR", e.getMessage());
//							}
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "There were No Queues, so there are No Workflows");
//						//Update the FnWorkflowList Object
//						fnWorkflowList.setErrorFlag(1);
//						fnWorkflowList.setErrorMessage("There were No Queues, so there are No Workflows");
//						//Update the FnBaseXML Object
//						fnBaseXML.setErrorFlag(1);
//						fnBaseXML.setErrorMessage("There were No Queues, so there are No Workflows");
//					}
//				}
//				/***************************************************************************************
//				 * Get ALL Workflows by User/Participant - workflow data field User/Participant - Inbox
//				 ***************************************************************************************/
//				else if (process.length() == 0 && step.length() == 0 && user.length() > 0 && propName.length() == 0 && propValue.length() == 0)
//				{
//					//By Participant Only
//					wiiscLog.log(wiiscLog.INFO, "Get ALL Workflows by User");
//
//					//Initialize the Array
//					filterName = new String[1];
//					filterValue = new String[1];
//					//Queue
//					String queueName = "";
//					//Get String[] of Queues
//					String[] queueNames = null;
//					
//					if (globalConfig.containsKey("workflowSearchUserField"))
//					{
//						//Check if the Property setting has a value
//						if (globalConfig.getString("workflowSearchUserField").length() == 0)
//						{
//							//Update the Array
//							filterName[0] = "F_BoundUser";
//							filterValue[0] = user;
//
//							//Get the Inbox(0) Queue and use this to find the Workflows for the particular User
//							queueName = "Inbox(0)";
//							
//							fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueName, filterName, filterValue, sortBy, wiiscLog);
//							//Add the List to the FnBaseXML
//							fnBaseXML.addFnWorkflowList(fnWorkflowList);
//						}
//						else
//						{
//							//String[] of Queues
//							queueNames = getQueues(vwSession, wiiscLog);
//							//Check queueNames
//							if (queueNames.length > 0)
//							{
//								//Update the Array
//								filterName[0] = globalConfig.getString("workflowSearchUserField");
//								filterValue[0] = user;
//								
//								//Loop and Get Workflows from each Queue
//								for (int i = 0; i < queueNames.length; i++)
//								{
//									//Exclude the System Queues
//									if (!queueNames[i].contains("Instruction") &&
//											!queueNames[i].contains("(") &&
//											!queueNames[i].equals("Conductor") &&
//											!queueNames[i].equals("WSRequest") &&
//											!queueNames[i].equals("CE_Operations"))
//									{
//										fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueNames[i], filterName, filterValue, sortBy, wiiscLog);
//										//Add the List to the FnBaseXML
//										fnBaseXML.addFnWorkflowList(fnWorkflowList);
//									}
//								}
//							}
//							else
//							{
//								wiiscLog.log(wiiscLog.INFO, "There were No Queues, so there are No Workflows");
//								//Update the FnBaseXML Object
//								fnBaseXML.setErrorFlag(1);
//								fnBaseXML.setErrorMessage("There were No Queues, so there are No Workflows");
//							}
//						}
//					}
//					else
//					{
//						//Update the Array
//						filterName[0] = "F_BoundUser";
//						filterValue[0] = user;
//
//						//Get the Inbox(0) Queue and use this to find the Workflows for the particular User
//						queueName = "Inbox(0)";
//						
//						fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueName, filterName, filterValue, sortBy, wiiscLog);
//						//Add the List to the FnBaseXML
//						fnBaseXML.addFnWorkflowList(fnWorkflowList);
//					}
//				}
//				/***************************************************************************************
//				 * Get ALL Workflows by User/Participant, Property and Property Value - Inbox Search
//				 ***************************************************************************************/
//				else if (process.length() == 0 && step.length() == 0 && user.length() > 0 && propName.length() > 0 && propValue.length() > 0)
//				{
//					//By Participant Only
//					wiiscLog.log(wiiscLog.INFO, "Get ALL Workflows by User, Property and Property Value");
//
//					//Initialize the Array
//					filterName = new String[2];
//					filterValue = new String[2];
//					//Queue
//					String queueName = "";
//					//Get String[] of Queues
//					String[] queueNames = null;
//					
//					if (globalConfig.containsKey("workflowSearchUserField"))
//					{
//						//Check if the Property setting has a value
//						if (globalConfig.getString("workflowSearchUserField").length() == 0)
//						{
//							//Update the Array
//							filterName[0] = "F_BoundUser";
//							filterValue[0] = user;
//							filterName[1] = propName;
//							filterValue[1] = propValue;
//
//							//Get the Inbox(0) Queue and use this to find the Workflows for the particular User
//							queueName = "Inbox(0)";
//							
//							fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueName, filterName, filterValue, sortBy, wiiscLog);
//							//Add the List to the FnBaseXML
//							fnBaseXML.addFnWorkflowList(fnWorkflowList);
//						}
//						else
//						{
//							//String[] of Queues
//							queueNames = getQueues(vwSession, wiiscLog);
//							//Check queueNames
//							if (queueNames.length > 0)
//							{
//								//Update the Array
//								filterName[0] = globalConfig.getString("workflowSearchUserField");
//								filterValue[0] = user;
//								filterName[1] = propName;
//								filterValue[1] = propValue;
//								
//								//Loop and Get Workflows from each Queue
//								for (int i = 0; i < queueNames.length; i++)
//								{
//									//Exclude the System Queues
//									if (!queueNames[i].contains("Instruction") &&
//											!queueNames[i].contains("(") &&
//											!queueNames[i].equals("Conductor") &&
//											!queueNames[i].equals("WSRequest") &&
//											!queueNames[i].equals("CE_Operations"))
//									{
//										fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueNames[i], filterName, filterValue, sortBy, wiiscLog);
//										//Add the List to the FnBaseXML
//										fnBaseXML.addFnWorkflowList(fnWorkflowList);
//									}
//								}
//							}
//							else
//							{
//								wiiscLog.log(wiiscLog.INFO, "There were No Queues, so there are No Workflows");
//								//Update the FnBaseXML Object
//								fnBaseXML.setErrorFlag(1);
//								fnBaseXML.setErrorMessage("There were No Queues, so there are No Workflows");
//							}
//						}
//					}
//					else
//					{
//						//Update the Array
//						filterName[0] = "F_BoundUser";
//						filterValue[0] = user;
//						filterName[1] = propName;
//						filterValue[1] = propValue;
//
//						//Get the Inbox(0) Queue and use this to find the Workflows for the particular User
//						queueName = "Inbox(0)";
//						
//						fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueName, filterName, filterValue, sortBy, wiiscLog);
//						//Add the List to the FnBaseXML
//						fnBaseXML.addFnWorkflowList(fnWorkflowList);
//					}
//				}
//				/*********************************************************************************
//				 * Get ALL Workflows by a Step, Property and Property Value - workflow data field
//				 *********************************************************************************/
//				else if (process.length() == 0 && step.length() > 0 && user.length() == 0 && propName.length() > 0 && propValue.length() > 0)
//				{
//					//By Property and Property Value only
//					wiiscLog.log(wiiscLog.INFO, "Get ALL Workflows by a Step, Property and Property Value");
//
//					//Initialize the Array
//					filterName = new String[1];
//					filterValue = new String[1];
//					filterName[0] = propName;
//					filterValue[0] = propValue;
//
//					//Set the Queue
//					String queueName = step;
//					//Check queueName
//					if (queueName.length() > 0)
//					{
//						//Check if DB FileNet Query will be used or FileNet API
//						if (globalConfig.getString("workflowDBSearchEnabled").equals("true"))
//						{
//							con = loginWorkflowDB(wiiscLog);
//							if (con != null)
//							{
//								beginProcessing = true;
//							}
//						}
//						else
//						{
//							beginProcessing = true;
//						}
//						
//						//Verify Processing can begin
//						if (beginProcessing)
//						{
//							//Exclude the System Queues
//							if (!queueName.contains("Instruction") &&
//									!queueName.contains("(") &&
//									!queueName.equals("Conductor") &&
//									!queueName.equals("WSRequest") &&
//									!queueName.equals("CE_Operations"))
//							{
//								fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueName, filterName, filterValue, sortBy, wiiscLog);
//								//Add the List to the FnBaseXML
//								fnBaseXML.addFnWorkflowList(fnWorkflowList);
//							}
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "Processing cannot begin because a DB Connection could not be established");
//						}
//												
//						//Close the FileNet DB Connection
//						if (con != null)
//						{
//							try
//							{
//								wiiscLog.log(wiiscLog.INFO, "Closing the FileNet DB Connection");
//								con.close();
//							}
//							catch(Exception e)
//							{
//								wiiscLog.log(wiiscLog.INFO, "Error closing the FileNet DB Connection");
//								wiiscLog.log("ERROR", e.getMessage());
//							}
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "There was No Queue, so there are No Workflows");
//						//Update the FnWorkflowList Object
//						fnWorkflowList.setErrorFlag(1);
//						fnWorkflowList.setErrorMessage("There was No Queue, so there are No Workflows");
//						//Update the FnBaseXML Object
//						fnBaseXML.setErrorFlag(1);
//						fnBaseXML.setErrorMessage("There was No Queue, so there are No Workflows");
//					}
//				}
//				/**********************************************
//				 * Get ALL Workflows by Step and User
//				 **********************************************/
//				else if (process.length() == 0 && step.length() > 0 && user.length() > 0 && propName.length() == 0 && propValue.length() == 0)
//				{
//					//By Process and Account ID
//					wiiscLog.log(wiiscLog.INFO, "Get ALL Workflows by Step and User");
//
//					//Initialize the Array
//					filterName = new String[1];
//					filterValue = new String[1];
//					
//					if (globalConfig.containsKey("workflowSearchUserField"))
//					{
//						//Check if the Property setting has a value
//						if (globalConfig.getString("workflowSearchUserField").length() == 0)
//						{
//							//Update the Array
//							filterName[0] = "F_BoundUser";
//							filterValue[0] = user;
//						}
//						else
//						{
//							//Update the Array
//							filterName[0] = globalConfig.getString("workflowSearchUserField");
//							filterValue[0] = user;
//						}
//					}
//					else
//					{
//						//Update the Array
//						filterName[0] = "F_BoundUser";
//						filterValue[0] = user;
//					}
//					
//					//Set the Queue
//					String queueName = step;
//					//Check queueName
//					if (queueName.length() > 0)
//					{
//						//Exclude the System Queues
//						if (!queueName.contains("Instruction") &&
//								!queueName.contains("(") &&
//								!queueName.equals("Conductor") &&
//								!queueName.equals("WSRequest") &&
//								!queueName.equals("CE_Operations"))
//						{
//							fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueName, filterName, filterValue, sortBy, wiiscLog);
//							//Add the List to the FnBaseXML
//							fnBaseXML.addFnWorkflowList(fnWorkflowList);
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "There were No Queues, so there are No Workflows");
//						//Update the FnWorkflowList Object
//						fnWorkflowList.setErrorFlag(1);
//						fnWorkflowList.setErrorMessage("There were No Queues, so there are No Workflows");
//						//Update the FnBaseXML Object
//						fnBaseXML.setErrorFlag(1);
//						fnBaseXML.setErrorMessage("There were No Queues, so there are No Workflows");
//					}
//				}
//				else
//				{
//					//Return an Error
//					wiiscLog.log(wiiscLog.INFO, "The Process, Step, Property Name and Property Value combination is not an allowed function, so there are No Workflows");
//					//Update the FnWorkflowList Object
//					fnWorkflowList.setErrorFlag(1);
//					fnWorkflowList.setErrorMessage("The Process, Step, Property Name and Property Value combination is not an allowed function, so there are No Workflows");
//					//Update the FnBaseXML Object
//					fnBaseXML.setErrorFlag(1);
//					fnBaseXML.setErrorMessage("The Process, Step, Property Name and Property Value combination is not an allowed function, so there are No Workflows");
//				}
//
//				//Logoff the Workflow Server
//				wiiscLog.log(wiiscLog.INFO, "Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				wiiscLog.log(wiiscLog.INFO, "Logged off");
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflowList Object
//				fnWorkflowList.setErrorFlag(1);
//				fnWorkflowList.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnBaseXML Object
//				fnBaseXML.setErrorFlag(1);
//				fnBaseXML.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//			}
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update the FnWorkflowList Object
//			fnWorkflowList.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowList.setErrorMessage(ex.getMessage());
//			//Update the FnBaseXML Object
//			fnBaseXML.setErrorFlag(1);
//			//Update ErrorMessage
//			fnBaseXML.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getWorkflowList()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnBaseXML;
//	}
//	
//	public FnBaseXML getWorkflowCountsList(String process, String step, String user, String propName, String propValue, String sortBy, WIISCLog wiiscLog)
//	{
//		//Create an FnBaseXML
//		FnBaseXML fnBaseXML = new FnBaseXML();
//		//Create the FnWorkflowList Object
//		FnWorkflowList fnWorkflowList = new FnWorkflowList();
//
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//		
//		//Get the Filter Name
//		//String filterName = "";
//		String[] filterName = null;
//		//Get the Filter Value
//		//String filterValue = "";
//		String[] filterValue = null;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getWorkflowCountsList()");
//			wiiscLog.log(wiiscLog.INFO, "Process: " + process);
//			wiiscLog.log(wiiscLog.INFO, "Step: " + step);
//			wiiscLog.log(wiiscLog.INFO, "User: " + user);
//			wiiscLog.log(wiiscLog.INFO, "Property Name: " + propName);
//			wiiscLog.log(wiiscLog.INFO, "Property Value: " + propValue);
//			wiiscLog.log(wiiscLog.INFO, "Sort By: " + sortBy);
//
//			//Login to the Workflow Server
//			vwSession = loginWorkflow(wiiscLog);
//
//			if (vwSession != null)
//			{
//				wiiscLog.log(wiiscLog.INFO, "Logged in successfully to the Workflow Server");
//				//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//				//Update the FnWorkflowList Object
//				fnWorkflowList.setErrorFlag(0);
//				fnWorkflowList.setErrorMessage("");
//
//				/****************************************
//				 * Get ALL Workflows using Roster Search
//				 ****************************************/
//				//Add later if needed
//				/**************************************************
//				 * Get ALL Workflows using Queue Search - Workpool 
//				 **************************************************/
//				if (process.length() == 0 && step.length() == 0 && user.length() == 0 && propName.length() == 0 && propValue.length() == 0)
//				{
//					//ALL Workflows - no process_sys_code, no account_id, no Participant specified
//					wiiscLog.log(wiiscLog.INFO, "Get ALL Workflows");
//
//					if (globalConfig.containsKey("workflowSearchUserField"))
//					{
//						//Check if the Property setting has a value
//						if (globalConfig.getString("workflowSearchUserField").length() > 0)
//						{
//							//Initialize the Array
//							filterName = new String[1];
//							filterValue = new String[1];
//							filterName[0] = globalConfig.getString("workflowSearchUserField");
//							filterValue[0] = user;
//						}
//					}
//					
//					//Get String[] of Queue
//					String[] queueNames = getQueues(vwSession, wiiscLog);
//					//Check queueNames
//					if (queueNames.length > 0)
//					{
//						//Loop and Get Workflows from each Queue
//						for (int i = 0; i < queueNames.length; i++)
//						{
//							//Exclude the System Queues
//							if (!queueNames[i].contains("Instruction") &&
//									!queueNames[i].contains("(") &&
//									!queueNames[i].equals("Conductor") &&
//									!queueNames[i].equals("WSRequest") &&
//									!queueNames[i].equals("CE_Operations"))
//							{
//
//								//Get the FnWorkflowList by Queue Name
//								fnWorkflowList = getFnWorkflowCountsListByQueue(vwSession, queueNames[i], filterName, filterValue, sortBy, wiiscLog);
//
//								//Check FnWorkflowList size
//								if (fnWorkflowList.getCount() == 0 || fnWorkflowList.getErrorFlag() == 0)
//								{
//									fnWorkflowList.setStepName(queueNames[i]);
//									//Add the List to the FnBaseXML
//									fnBaseXML.addFnWorkflowList(fnWorkflowList);
//								}
//								else
//								{
//									wiiscLog.log(wiiscLog.INFO, "Error with Queue that needs addressed");
//									//Update the FnBaseXML Object
//									fnBaseXML.setErrorFlag(1);
//									fnBaseXML.setErrorMessage("Error with Queue that needs addressed");
//								}
//							}
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "There were No Queues, so there are No Workflows");
//						//Update the FnBaseXML Object
//						fnBaseXML.setErrorFlag(1);
//						fnBaseXML.setErrorMessage("There were No Queues, so there are No Workflows");
//					}					
//				}
//				/**********************************
//				 * Get ALL Workflows for a Process
//				 **********************************/
//				else if (process.length() > 0 && step.length() == 0 && user.length() == 0 && propName.length() == 0 && propValue.length() == 0)
//				{
//					//By process only
//					wiiscLog.log(wiiscLog.INFO, "Get ALL Workflows for a Process");
//					
//					//Get the FnWorkflowList
//					fnWorkflowList = getFnWorkflowListByRoster(vwSession, process, filterName, filterValue, wiiscLog);
//					//Add the List to the FnBaseXML
//					fnBaseXML.addFnWorkflowList(fnWorkflowList);
//				}
//				/**********************************
//				 * Get ALL Workflows for a Step
//				 **********************************/
//				else if (process.length() == 0 && step.length() > 0 && user.length() == 0 && propName.length() == 0 && propValue.length() == 0)
//				{
//					//By step only
//					wiiscLog.log(wiiscLog.INFO, "Get ALL Workflows for a Step");
//					
//					if (globalConfig.containsKey("workflowSearchUserField"))
//					{
//						//Check if the Property setting has a value
//						if (globalConfig.getString("workflowSearchUserField").length() > 0)
//						{
//							//Initialize the Array
//							filterName = new String[1];
//							filterValue = new String[1];
//							filterName[0] = globalConfig.getString("workflowSearchUserField");
//							filterValue[0] = "";
//						}
//					}
//					
//					//Get the FnWorkflowList
//					fnWorkflowList = getFnWorkflowCountsListByQueue(vwSession, step, filterName, filterValue, sortBy, wiiscLog);
//					//Add the List to the FnBaseXML
//					fnBaseXML.addFnWorkflowList(fnWorkflowList);
//					
//				}
//				/*****************************************************************************
//				 * Get ALL Workflows by a Property and Property Value - workflow data field
//				 *****************************************************************************/
//				else if (process.length() == 0 && step.length() == 0 && user.length() == 0 && propName.length() > 0 && propValue.length() > 0)
//				{
//					//By Property and Property Value only
//					wiiscLog.log(wiiscLog.INFO, "Get ALL Workflows by Property and Property Value");
//
//					//Get String[] of Queues
//					String[] queueNames = getQueues(vwSession, wiiscLog);
//					//Check queueNames
//					if (queueNames.length > 0)
//					{
//						if (globalConfig.containsKey("workflowSearchUserField"))
//						{
//							//Check if the Property setting has a value
//							if (globalConfig.getString("workflowSearchUserField").length() == 0)
//							{
//								//Initialize the Array
//								filterName = new String[1];
//								filterValue = new String[1];
//								filterName[0] = propName;
//								filterValue[0] = propValue;
//							}
//							else
//							{
//								//Initialize the Array
//								filterName = new String[2];
//								filterValue = new String[2];
//								filterName[0] = globalConfig.getString("workflowSearchUserField");
//								filterValue[0] = user;
//								filterName[1] = propName;
//								filterValue[1] = propValue;
//							}
//						}
//						
//						//Loop and Get Workflows from each Queue
//						for (int i = 0; i < queueNames.length; i++)
//						{
//							//Exclude the System Queues
//							if (!queueNames[i].contains("Instruction") &&
//									!queueNames[i].contains("(") &&
//									!queueNames[i].equals("Conductor") &&
//									!queueNames[i].equals("WSRequest") &&
//									!queueNames[i].equals("CE_Operations"))
//							{
//								fnWorkflowList = getFnWorkflowCountsListByQueue(vwSession, queueNames[i], filterName, filterValue, sortBy, wiiscLog);
//								//Add the List to the FnBaseXML
//								fnBaseXML.addFnWorkflowList(fnWorkflowList);
//							}
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "There were No Queues, so there are No Workflows");
//						//Update the FnWorkflowList Object
//						fnWorkflowList.setErrorFlag(1);
//						fnWorkflowList.setErrorMessage("There were No Queues, so there are No Workflows");
//						//Update the FnBaseXML Object
//						fnBaseXML.setErrorFlag(1);
//						fnBaseXML.setErrorMessage("There were No Queues, so there are No Workflows");
//					}
//				}
//				/***************************************************************************************
//				 * Get ALL Workflows by User/Participant - workflow data field User/Participant - Inbox
//				 ***************************************************************************************/
//				else if (process.length() == 0 && step.length() == 0 && user.length() > 0 && propName.length() == 0 && propValue.length() == 0)
//				{
//					//By Participant Only
//					wiiscLog.log(wiiscLog.INFO, "Get ALL Workflows by User");
//
//					//Initialize the Array
//					filterName = new String[1];
//					filterValue = new String[1];
//					//Queue
//					String queueName = "";
//					//Get String[] of Queues
//					String[] queueNames = null;
//					
//					if (globalConfig.containsKey("workflowSearchUserField"))
//					{
//						//Check if the Property setting has a value
//						if (globalConfig.getString("workflowSearchUserField").length() == 0)
//						{
//							//Update the Array
//							filterName[0] = "F_BoundUser";
//							filterValue[0] = user;
//
//							//Get the Inbox(0) Queue and use this to find the Workflows for the particular User
//							queueName = "Inbox(0)";
//							
//							fnWorkflowList = getFnWorkflowCountsListByQueue(vwSession, queueName, filterName, filterValue, sortBy, wiiscLog);
//							//Add the List to the FnBaseXML
//							fnBaseXML.addFnWorkflowList(fnWorkflowList);
//						}
//						else
//						{
//							//String[] of Queues
//							queueNames = getQueues(vwSession, wiiscLog);
//							//Check queueNames
//							if (queueNames.length > 0)
//							{
//								//Update the Array
//								filterName[0] = globalConfig.getString("workflowSearchUserField");
//								filterValue[0] = user;
//								
//								//Loop and Get Workflows from each Queue
//								for (int i = 0; i < queueNames.length; i++)
//								{
//									//Exclude the System Queues
//									if (!queueNames[i].contains("Instruction") &&
//											!queueNames[i].contains("(") &&
//											!queueNames[i].equals("Conductor") &&
//											!queueNames[i].equals("WSRequest") &&
//											!queueNames[i].equals("CE_Operations"))
//									{
//										fnWorkflowList = getFnWorkflowCountsListByQueue(vwSession, queueNames[i], filterName, filterValue, sortBy, wiiscLog);
//										//Add the List to the FnBaseXML
//										fnBaseXML.addFnWorkflowList(fnWorkflowList);
//									}
//								}
//							}
//							else
//							{
//								wiiscLog.log(wiiscLog.INFO, "There were No Queues, so there are No Workflows");
//								//Update the FnBaseXML Object
//								fnBaseXML.setErrorFlag(1);
//								fnBaseXML.setErrorMessage("There were No Queues, so there are No Workflows");
//							}
//						}
//					}
//					else
//					{
//						//Update the Array
//						filterName[0] = "F_BoundUser";
//						filterValue[0] = user;
//
//						//Get the Inbox(0) Queue and use this to find the Workflows for the particular User
//						queueName = "Inbox(0)";
//						
//						fnWorkflowList = getFnWorkflowCountsListByQueue(vwSession, queueName, filterName, filterValue, sortBy, wiiscLog);
//						//Add the List to the FnBaseXML
//						fnBaseXML.addFnWorkflowList(fnWorkflowList);
//					}
//				}
//				/***************************************************************************************
//				 * Get ALL Workflows by User/Participant, Property and Property Value - Inbox Search
//				 ***************************************************************************************/
//				else if (process.length() == 0 && step.length() == 0 && user.length() > 0 && propName.length() > 0 && propValue.length() > 0)
//				{
//					//By Participant Only
//					wiiscLog.log(wiiscLog.INFO, "Get ALL Workflows by User, Property and Property Value");
//
//					//Initialize the Array
//					filterName = new String[2];
//					filterValue = new String[2];
//					//Queue
//					String queueName = "";
//					//Get String[] of Queues
//					String[] queueNames = null;
//					
//					if (globalConfig.containsKey("workflowSearchUserField"))
//					{
//						//Check if the Property setting has a value
//						if (globalConfig.getString("workflowSearchUserField").length() == 0)
//						{
//							//Update the Array
//							filterName[0] = "F_BoundUser";
//							filterValue[0] = user;
//							filterName[1] = propName;
//							filterValue[1] = propValue;
//
//							//Get the Inbox(0) Queue and use this to find the Workflows for the particular User
//							queueName = "Inbox(0)";
//							
//							fnWorkflowList = getFnWorkflowCountsListByQueue(vwSession, queueName, filterName, filterValue, sortBy, wiiscLog);
//							//Add the List to the FnBaseXML
//							fnBaseXML.addFnWorkflowList(fnWorkflowList);
//						}
//						else
//						{
//							//String[] of Queues
//							queueNames = getQueues(vwSession, wiiscLog);
//							//Check queueNames
//							if (queueNames.length > 0)
//							{
//								//Update the Array
//								filterName[0] = globalConfig.getString("workflowSearchUserField");
//								filterValue[0] = user;
//								filterName[1] = propName;
//								filterValue[1] = propValue;
//								
//								//Loop and Get Workflows from each Queue
//								for (int i = 0; i < queueNames.length; i++)
//								{
//									//Exclude the System Queues
//									if (!queueNames[i].contains("Instruction") &&
//											!queueNames[i].contains("(") &&
//											!queueNames[i].equals("Conductor") &&
//											!queueNames[i].equals("WSRequest") &&
//											!queueNames[i].equals("CE_Operations"))
//									{
//										fnWorkflowList = getFnWorkflowCountsListByQueue(vwSession, queueNames[i], filterName, filterValue, sortBy, wiiscLog);
//										//Add the List to the FnBaseXML
//										fnBaseXML.addFnWorkflowList(fnWorkflowList);
//									}
//								}
//							}
//							else
//							{
//								wiiscLog.log(wiiscLog.INFO, "There were No Queues, so there are No Workflows");
//								//Update the FnBaseXML Object
//								fnBaseXML.setErrorFlag(1);
//								fnBaseXML.setErrorMessage("There were No Queues, so there are No Workflows");
//							}
//						}
//					}
//					else
//					{
//						//Update the Array
//						filterName[0] = "F_BoundUser";
//						filterValue[0] = user;
//						filterName[1] = propName;
//						filterValue[1] = propValue;
//
//						//Get the Inbox(0) Queue and use this to find the Workflows for the particular User
//						queueName = "Inbox(0)";
//						
//						fnWorkflowList = getFnWorkflowCountsListByQueue(vwSession, queueName, filterName, filterValue, sortBy, wiiscLog);
//						//Add the List to the FnBaseXML
//						fnBaseXML.addFnWorkflowList(fnWorkflowList);
//					}
//				}
//				/*********************************************************************************
//				 * Get ALL Workflows by a Step, Property and Property Value - workflow data field
//				 *********************************************************************************/
//				else if (process.length() == 0 && step.length() > 0 && user.length() == 0 && propName.length() > 0 && propValue.length() > 0)
//				{
//					//By Property and Property Value only
//					wiiscLog.log(wiiscLog.INFO, "Get ALL Workflows by a Step, Property and Property Value");
//
//					//Initialize the Array
//					filterName = new String[1];
//					filterValue = new String[1];
//					filterName[0] = propName;
//					filterValue[0] = propValue;
//
//					//Set the Queue
//					String queueName = step;
//					//Check queueName
//					if (queueName.length() > 0)
//					{
//						//Exclude the System Queues
//						if (!queueName.contains("Instruction") &&
//								!queueName.contains("(") &&
//								!queueName.equals("Conductor") &&
//								!queueName.equals("WSRequest") &&
//								!queueName.equals("CE_Operations"))
//						{
//							fnWorkflowList = getFnWorkflowCountsListByQueue(vwSession, queueName, filterName, filterValue, sortBy, wiiscLog);
//							//Add the List to the FnBaseXML
//							fnBaseXML.addFnWorkflowList(fnWorkflowList);
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "There was No Queue, so there are No Workflows");
//						//Update the FnWorkflowList Object
//						fnWorkflowList.setErrorFlag(1);
//						fnWorkflowList.setErrorMessage("There was No Queue, so there are No Workflows");
//						//Update the FnBaseXML Object
//						fnBaseXML.setErrorFlag(1);
//						fnBaseXML.setErrorMessage("There was No Queue, so there are No Workflows");
//					}
//				}
//				/**********************************************
//				 * Get ALL Workflows by Step and User
//				 **********************************************/
//				else if (process.length() == 0 && step.length() > 0 && user.length() > 0 && propName.length() == 0 && propValue.length() == 0)
//				{
//					//By Process and Account ID
//					wiiscLog.log(wiiscLog.INFO, "Get ALL Workflows by Step and User");
//
//					//Initialize the Array
//					filterName = new String[1];
//					filterValue = new String[1];
//					
//					if (globalConfig.containsKey("workflowSearchUserField"))
//					{
//						//Check if the Property setting has a value
//						if (globalConfig.getString("workflowSearchUserField").length() == 0)
//						{
//							//Update the Array
//							filterName[0] = "F_BoundUser";
//							filterValue[0] = user;
//						}
//						else
//						{
//							//Update the Array
//							filterName[0] = globalConfig.getString("workflowSearchUserField");
//							filterValue[0] = user;
//						}
//					}
//					else
//					{
//						//Update the Array
//						filterName[0] = "F_BoundUser";
//						filterValue[0] = user;
//					}
//					
//					//Set the Queue
//					String queueName = step;
//					//Check queueName
//					if (queueName.length() > 0)
//					{
//						//Exclude the System Queues
//						if (!queueName.contains("Instruction") &&
//								!queueName.contains("(") &&
//								!queueName.equals("Conductor") &&
//								!queueName.equals("WSRequest") &&
//								!queueName.equals("CE_Operations"))
//						{
//							fnWorkflowList = getFnWorkflowCountsListByQueue(vwSession, queueName, filterName, filterValue, sortBy, wiiscLog);
//							//Add the List to the FnBaseXML
//							fnBaseXML.addFnWorkflowList(fnWorkflowList);
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "There were No Queues, so there are No Workflows");
//						//Update the FnWorkflowList Object
//						fnWorkflowList.setErrorFlag(1);
//						fnWorkflowList.setErrorMessage("There were No Queues, so there are No Workflows");
//						//Update the FnBaseXML Object
//						fnBaseXML.setErrorFlag(1);
//						fnBaseXML.setErrorMessage("There were No Queues, so there are No Workflows");
//					}
//				}
//				else
//				{
//					//Return an Error
//					wiiscLog.log(wiiscLog.INFO, "The Process, Step, Property Name and Property Value combination is not an allowed function, so there are No Workflows");
//					//Update the FnWorkflowList Object
//					fnWorkflowList.setErrorFlag(1);
//					fnWorkflowList.setErrorMessage("The Process, Step, Property Name and Property Value combination is not an allowed function, so there are No Workflows");
//					//Update the FnBaseXML Object
//					fnBaseXML.setErrorFlag(1);
//					fnBaseXML.setErrorMessage("The Process, Step, Property Name and Property Value combination is not an allowed function, so there are No Workflows");
//				}
//
//				//Logoff the Workflow Server
//				wiiscLog.log(wiiscLog.INFO, "Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				wiiscLog.log(wiiscLog.INFO, "Logged off");
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflowList Object
//				fnWorkflowList.setErrorFlag(1);
//				fnWorkflowList.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnBaseXML Object
//				fnBaseXML.setErrorFlag(1);
//				fnBaseXML.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//			}
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update the FnWorkflowList Object
//			fnWorkflowList.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowList.setErrorMessage(ex.getMessage());
//			//Update the FnBaseXML Object
//			fnBaseXML.setErrorFlag(1);
//			//Update ErrorMessage
//			fnBaseXML.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getWorkflowCountsList()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnBaseXML;
//	}
//	
//	private FnWorkflow updateFnWorkflowInfo(VWStepElement stepElement, String workflowType, WIISCLog wiiscLog)
//	{
//		//Create the FnWorkflow Object
//		FnWorkflow fnWorkflow = new FnWorkflow();
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> updateFnWorkflowInfo()");
//			wiiscLog.log(wiiscLog.INFO, "===========================================================");
//			//Workflow Name
//			wiiscLog.log(wiiscLog.INFO, "Workflow Name: " + stepElement.getWorkflowName());
//			fnWorkflow.setFnWorkflowName(stepElement.getWorkflowName());
//			//Workflow Process
//			wiiscLog.log(wiiscLog.INFO, "Workflow Process: " + stepElement.getRosterName());
//			fnWorkflow.setFnWorkflowProcess(stepElement.getRosterName());
//			//Workflow Activity/Step
//			wiiscLog.log(wiiscLog.INFO, "Workflow Activity: " + stepElement.getQueueName());
//			fnWorkflow.setFnWorkflowStep(stepElement.getQueueName());
//			//Workflow Roster
//			wiiscLog.log(wiiscLog.INFO, "Workflow Roster: " + stepElement.getRosterName());
//			fnWorkflow.setFnWorkflowRoster(stepElement.getRosterName());
//			//Workflow Queue
//			wiiscLog.log(wiiscLog.INFO, "Workflow Queue: " + stepElement.getQueueName());
//			fnWorkflow.setFnWorkflowQueue(stepElement.getQueueName());
//
//			/*//Workflow Object
//			wiiscLog.log(wiiscLog.INFO, "Workflow Object Name: " + stepElement.getWorkObjectName());
//			fnWorkflow.setFnWorkflowObject(stepElement.getWorkObjectName());*/
//
//			//Workflow ID
//			wiiscLog.log(wiiscLog.INFO, "Workflow ID: " + stepElement.getWorkObjectNumber());
//			fnWorkflow.setFnWorkflowID(stepElement.getWorkObjectNumber());
//			//Workflow Originator
//			wiiscLog.log(wiiscLog.INFO, "Workflow Originator: " + stepElement.getOriginator());
//			//Workflow Participant
//			wiiscLog.log(wiiscLog.INFO, "Workflow Participant: " + stepElement.getParticipantName());
//			fnWorkflow.setFnWorkflowUser(stepElement.getParticipantName());
//
//			//Check the WorkflowType - New or Existing
//			if (!workflowType.equals("New"))
//			{
//				wiiscLog.log(wiiscLog.INFO, "Workflow Date Received: " + stepElement.getDateReceived().toString());
//			}
//
//			wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			//Update ErrorFlag
//			fnWorkflow.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflow.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> updateFnWorkflowInfo()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflow;
//	}
//
//	private FnWorkflowPropertyList updateFnWorkflowPropertyListInfo(VWStepElement stepElement, WIISCLog wiiscLog)
//	{
//		//Create the FnWorkflowPropertyList Object
//		FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> updateFnWorkflowPropertyListInfo()");
//			//Get the VWStepElement System and User Defined Data Fields
//			//VWParameter[] vwParametersData = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_AND_SYSTEM_DEFINED);
//			
//			//Get the VWStepElement User Defined Data Fields
//			VWParameter[] vwParametersData = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_DEFINED);
//			
//			//Get the name, type, mode and value for each VWParameter
//			for (int i = 0; i < vwParametersData.length; i++)
//			{
//				//Create the FnWorkflowProperty Object
//				FnWorkflowProperty fnWorkflowProperty = new FnWorkflowProperty();
//				//Get the Parameter Name
//				String vwParameterName = vwParametersData[i].getName();
//				fnWorkflowProperty.setName(vwParameterName);
//				wiiscLog.log(wiiscLog.INFO, "VWParameterName: " + vwParameterName);
//				//Get the Parameter Value
//				String vwParameterValue = vwParametersData[i].getStringValue();
//				fnWorkflowProperty.setValue(vwParameterValue);
//				wiiscLog.log(wiiscLog.INFO, "VWParameterValue: " + vwParameterValue);
//				//Add the FnWorkflowProperty to the FnWorkflowPropertyList
//				fnWorkflowPropertyList.addFnWorkflowProperty(fnWorkflowProperty);
//			}
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			//Update ErrorFlag
//			fnWorkflowPropertyList.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowPropertyList.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> updateFnWorkflowPropertyListInfo()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflowPropertyList;
//	}
//
//	private boolean doStepElementAttachment(VWStepElement stepElement, VWParameter vwParam, FnDocument fnDocument, WIISCLog wiiscLog)
//	{
//		boolean attachmentAdded = false;
//		//Create a List from the FnWorkflowRequest variable
//		//List<FnWorkflowProperty> fnWorkflowPropertyListRequest = fnWorkflowRequest.getFnWorkflowPropsList();
//		//Create the FnWorkflowProperty Object
//		FnWorkflowProperty fnWorkflowPropertyResult = new FnWorkflowProperty();
//		//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> doStepElementAttachment()");
//			//Check parameter mode
//			boolean readOnly = false;
//			if (vwParam.getMode() == VWModeType.MODE_TYPE_IN)
//			{
//				readOnly = true;
//			}
//			//If the parameter is editable, switch through each data type
//			if (!readOnly)
//			{
//				switch (vwParam.getFieldType())
//				{
//				case VWFieldType.FIELD_TYPE_ATTACHMENT:
//					//Check if the Passed in fieldType was Attachment
//					if (!vwParam.isArray())
//					{
//						// Get the value for the VWAttachment
//						VWAttachment attachment = (VWAttachment) vwParam.getValue();
//						//Check attachment
//						if (attachment != null)
//						{
//							wiiscLog.log(wiiscLog.INFO, "1 Attachment");
//							// Set the attachment name
//							attachment.setAttachmentName(fnDocument.getFnDocumentName());
//							// Set the attachment description
//							attachment.setAttachmentDescription("Attachment for Workflow");
//							// Set the type of object (Document)
//							attachment.setType(VWAttachmentType.ATTACHMENT_TYPE_DOCUMENT);
//							// Set the library type and name (CE Object Store)
//							attachment.setLibraryType(VWLibraryType.LIBRARY_TYPE_CONTENT_ENGINE);
//							attachment.setLibraryName(globalConfig.getString("objectstoreName"));
//							// Set the document ID and version
//							attachment.setId(fnDocument.getFnDocumentID());
//							attachment.setVersion(fnDocument.getFnDocumentID());
//							// Set the parameter value
//							stepElement.setParameterValue(vwParam.getName(),attachment,true);
//							//Verify the attachment was successful
//							if (attachment.getId().length() > 0)
//							{
//								//Update the boolean
//								attachmentAdded = true;
//								wiiscLog.log(wiiscLog.INFO, "Attachment Added");
//							}
//							else
//							{
//								//Update the boolean
//								attachmentAdded = false;
//								wiiscLog.log(wiiscLog.INFO, "Attachment Failed");
//							}
//						}
//					}
//					else
//					{
//						//VWParam is an array of Attachments
//						// Get the value for the VWAttachment[]
//						VWAttachment[] attachments = (VWAttachment[]) vwParam.getValue();
//						VWAttachment[] updatedAttachments = new VWAttachment[attachments.length];
//						wiiscLog.log(wiiscLog.INFO, "Multiple Attachments " + attachments.length);
//						for (int i = 0; i < attachments.length; i++)
//						{
//							VWAttachment attachment = attachments[i];
//							// Set the attachment name
//							attachment.setAttachmentName(fnDocument.getFnDocumentName());
//							// Set the attachment description
//							attachment.setAttachmentDescription("Attachment for Workflow");
//							// Set the type of object (Document)
//							attachment.setType(VWAttachmentType.ATTACHMENT_TYPE_DOCUMENT);
//							// Set the library type and name (CE Object Store)
//							attachment.setLibraryType(VWLibraryType.LIBRARY_TYPE_CONTENT_ENGINE);
//							attachment.setLibraryName(globalConfig.getString("objectstoreName"));
//							//Check to see if fnDocument has more than 1 attachment to use
//							if (fnDocument.getFnDocumentID().contains(","))
//							{
//								wiiscLog.log(wiiscLog.INFO, "Split ID list");
//								String[] docIDs = fnDocument.getFnDocumentID().split(",");
//								//String docID = "";
//								//Set the document ID and version
//								attachment.setId(docIDs[i]);
//								attachment.setVersion(docIDs[i]);
//								//UpdatedAttachments array
//								updatedAttachments[i] = attachment;
//							}
//							else
//							{
//								// Set the document ID and version
//								attachment.setId(fnDocument.getFnDocumentID());
//								attachment.setVersion(fnDocument.getFnDocumentID());
//								//UpdatedAttachments array
//								updatedAttachments[i] = attachment;
//							}
//						}
//						// Set the parameter value
//						stepElement.setParameterValue(vwParam.getName(),updatedAttachments,true);
//						//Update the boolean
//						attachmentAdded = true;
//						wiiscLog.log(wiiscLog.INFO, "Attachment Added");
//					}
//					break;
//				default:
//					// Do not take action for other data types
//					break;
//				}
//			}//End of ReadOnly If
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			wiiscLog.log(wiiscLog.INFO, "Attachment Failed");
//			//Update the boolean
//			attachmentAdded = false;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> doStepElementAttachment()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return attachmentAdded;
//	}
//
//	private FnWorkflowProperty doStepElementParticipant(VWStepElement stepElement, VWParameter vwParam, FnWorkflow fnWorkflowRequest, WIISCLog wiiscLog)
//	{
//
//		//Create a List from the FnWorkflowRequest variable
//		//List<FnWorkflowProperty> fnWorkflowPropertyListRequest = fnWorkflowRequest.getFnWorkflowPropsList();
//		//Create the FnWorkflowProperty Object
//		FnWorkflowProperty fnWorkflowPropertyResult = new FnWorkflowProperty();
//
//		//Check parameter mode
//		boolean readOnly = false;
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> doStepElementParticipant()");
//			
//			if (vwParam.getMode() == VWModeType.MODE_TYPE_IN)
//			{
//				readOnly = true;
//			}
//			//If the parameter is editable, switch through each data type
//			if (!readOnly)
//			{
//				switch (vwParam.getFieldType())
//				{
//				case VWFieldType.FIELD_TYPE_PARTICIPANT:
//					wiiscLog.log(wiiscLog.INFO, "Participant Field Name: " + vwParam);
//					// Instantiate a new VWParticipant array
//					VWParticipant[] participant = new VWParticipant[1];
//					// Set the participant name using username value
//					//String participantUserName = "Administrator";
//					String participantUserName = fnWorkflowRequest.getFnWorkflowUser();
//					participant[0].setParticipantName(participantUserName);
//					// Set the parameter value
//					stepElement.setParameterValue(vwParam.getName(),participant,true);
//					break;
//				default:
//					// Do not take action for other data types
//					break;
//				}
//			}
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> doStepElementParticipant()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflowPropertyResult;
//	}
//
//	private FnWorkflowPropertyList doStepElementDataFields(VWStepElement stepElement, FnWorkflow fnWorkflowRequest, WIISCLog wiiscLog)
//	{
//		//Create the FnWorkflowPropertyList
//		FnWorkflowPropertyList fnWorkflowPropertyListResult = new FnWorkflowPropertyList();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> doStepElementDataFields()");
//			
//			//Get the VWStepElement System and User Defined Data Fields
//			VWParameter[] vwParametersData = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_AND_SYSTEM_DEFINED);
//
//			//Update and Save the Workflow Data Fields
//			//Process Step Element Parameters
//			for (int i = 0; i < vwParametersData.length; i++ )
//			{
//				VWParameter vwParam = vwParametersData[i];
//				//Only Process Integer and String
//				if (vwParam.getFieldType() != VWFieldType.FIELD_TYPE_ATTACHMENT && 
//						vwParam.getFieldType() != VWFieldType.FIELD_TYPE_PARTICIPANT)
//				{
//					wiiscLog.log(wiiscLog.INFO, "Updating Workflow Data Field: " + vwParam.getName());
//					FnWorkflowProperty fnWorkflowPropertyResult = new FnWorkflowProperty();
//					//Call DoStepElementDataField
//					fnWorkflowPropertyResult = doStepElementDataField(stepElement, vwParam, fnWorkflowRequest, wiiscLog);
//					//Save the StepElement
//					stepElement.doSave(false);
//					//Add the FnProperty to the FnPropertyListResult
//					fnWorkflowPropertyListResult.addFnWorkflowProperty(fnWorkflowPropertyResult);
//				}
//			}
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			//Update ErrorFlag
//			fnWorkflowPropertyListResult.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowPropertyListResult.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> doStepElementDataFields()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflowPropertyListResult;
//	}
//
//	private FnWorkflowProperty doStepElementDataField(VWStepElement stepElement, VWParameter vwParam, FnWorkflow fnWorkflowRequest, WIISCLog wiiscLog)
//	{
//		//Create a List from the FnWorkflowRequest variable
//		FnWorkflowPropertyList fnWorkflowPropertyList = fnWorkflowRequest.getFnWorkflowPropertyList();
//		List<FnWorkflowProperty> fnWorkflowPropertyListRequest = fnWorkflowPropertyList.getFnWorkflowPropsList();
//		//Create the FnWorkflowProperty Object
//		FnWorkflowProperty fnWorkflowPropertyResult = new FnWorkflowProperty();
//
//		//Check parameter mode
//		boolean readOnly = false;
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> doStepElementDataField()");
//			
//			if (vwParam.getMode() == VWModeType.MODE_TYPE_IN)
//			{
//				readOnly = true;
//			}
//			//If the parameter is editable, switch through each data type
//			if (!readOnly)
//			{
//				// For each data type,
//				// check whether the parameter is single or an array
//				// and set the parameter value(s)
//
//				//Perform Field Updates based on the Field
//
//
//				switch (vwParam.getFieldType())
//				{
//				case VWFieldType.FIELD_TYPE_INT:
//					if (vwParam.isArray())
//					{
//						/*int[] arrParamValues = new int[] {1, 2, 3};
//							stepElement.setParameterValue(vwParametersData[i].getName(),arrParamValues,true);*/
//						break;
//					}
//					else
//					{
//						for (int a = 0; a < fnWorkflowPropertyListRequest.size(); a++)
//						{
//							//FnWorkflowProperty Request
//							FnWorkflowProperty fnWorkflowPropertyRequest = new FnWorkflowProperty();
//							fnWorkflowPropertyRequest = fnWorkflowPropertyListRequest.get(a);
//							if (vwParam.getName().equals(fnWorkflowPropertyRequest.getName()))
//							{
//								//If the Property Matches - then Save it to the Workflow Data Field
//								//wiiscLog.log(wiiscLog.INFO, "Updating " + fnWorkflowPropertyRequest.getName());
//								wiiscLog.log(wiiscLog.INFO, "Updating Workflow Data Value: " + fnWorkflowPropertyRequest.getValue());
//								stepElement.setParameterValue(vwParam.getName(), Integer.parseInt(fnWorkflowPropertyRequest.getValue()), true);
//								//Update the FnPropertyResult
//								fnWorkflowPropertyResult.setName(fnWorkflowPropertyRequest.getName());
//								fnWorkflowPropertyResult.setValue(fnWorkflowPropertyRequest.getValue());
//								break;
//							}
//						}
//					}
//				case VWFieldType.FIELD_TYPE_STRING:
//					if (vwParam.isArray())
//					{
//						/*String[] arrParamValues =	new String[] {"Test_1", "Test_2", "Test_3"};
//							stepElement.setParameterValue(vwParametersData[i].getName(),arrParamValues,true);*/
//						break;
//					} 
//					else
//					{
//						for (int a = 0; a < fnWorkflowPropertyListRequest.size(); a++)
//						{
//							//FnWorkflowProperty Request
//							FnWorkflowProperty fnWorkflowPropertyRequest = new FnWorkflowProperty();
//							fnWorkflowPropertyRequest = fnWorkflowPropertyListRequest.get(a);
//							if (vwParam.getName().equals(fnWorkflowPropertyRequest.getName()))
//							{
//								//If the Property Matches - then Save it to the Workflow Data Field
//								//wiiscLog.log(wiiscLog.INFO, "Updating " + fnWorkflowPropertyRequest.getName());
//								wiiscLog.log(wiiscLog.INFO, "Updating Workflow Data Value: " + fnWorkflowPropertyRequest.getValue());
//								stepElement.setParameterValue(vwParam.getName(), fnWorkflowPropertyRequest.getValue(), true);
//								//Update the FnPropertyResult
//								fnWorkflowPropertyResult.setName(fnWorkflowPropertyRequest.getName());
//								fnWorkflowPropertyResult.setValue(fnWorkflowPropertyRequest.getValue());
//								break;
//							}
//						}
//					}
//				default:
//					// Do not take action for other data types
//					break;
//				}
//			}			
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> doStepElementDataField()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflowPropertyResult;
//	}
//
//	private FnWorkflow updateStepElement(VWStepElement stepElement, FnWorkflow fnWorkflowRequest, Map<String, String> stepElementParamsMap, String stepElementAction, String stepElementActionValue, WIISCLog wiiscLog)
//	{
//		FnWorkflow fnWorkflowResult = new FnWorkflow();
//		FnWorkflowPropertyList fnWorkflowPropertyListResult = new FnWorkflowPropertyList();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> updateStepElement()");
//			
//			//If Attachment, call doStepElementAttachment and doStepElementAction
//			if (stepElementAction.equals("Attachment"))
//			{
//				wiiscLog.log(wiiscLog.INFO, "Attachment");
//				//Not implemented yet
//			}
//			//If Participant, call doStepElementParticipant and doStepElementAction
//			else if (stepElementAction.equals("Participant"))
//			{
//				wiiscLog.log(wiiscLog.INFO, "Participant");
//				//Not implemented yet
//			}
//			//If DataFields, call doStepElementDataFields and doStepElementAction
//			else if (stepElementAction.equals("DataFields"))
//			{
//				wiiscLog.log(wiiscLog.INFO, "DataFields");
//				//Lock the Step Element
//				stepElement.doLock(true);
//
//				//Call doStepElementDataFields
//				fnWorkflowPropertyListResult =  doStepElementDataFields(stepElement, fnWorkflowRequest, wiiscLog);
//
//				//End of For Loop
//				wiiscLog.log(wiiscLog.INFO, "===========================================");
//
//				// Set the value for the system-defined Response parameter
//				if (stepElement.getStepResponses() != null) {
//					wiiscLog.log(wiiscLog.INFO, "Step Responses");
//					String responseValue = "Ok";
//					stepElement.setSelectedResponse(responseValue);
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "No Step Responses");
//					wiiscLog.log(wiiscLog.INFO, "===========================================");
//				}
//
//				//DoStepElementAction - Save & Unlock the Workflow
//				fnWorkflowResult = doStepElementAction(stepElement, "Save", "", wiiscLog);
//			}
//			//If Parameters, call doStepElementParameterFields and doStepElementAction
//			else if (stepElementAction.equals("Parameters-Save"))
//			{
//				wiiscLog.log(wiiscLog.INFO, "Parameters-Save");
//				if (stepElementParamsMap.size() > 0)
//				{
//					//Lock the Step Element
//					stepElement.doLock(true);
//
//					//Update the Step Element
//					for (Map.Entry<String, String> entry : stepElementParamsMap.entrySet()) {
//						wiiscLog.log(wiiscLog.INFO, "Entry: " + entry.getKey() + " Value: " + entry.getValue());
//						stepElement.setParameterValue(entry.getKey(), entry.getValue(), true);
//					}
//
//					wiiscLog.log(wiiscLog.INFO, "===========================================");
//
//					// Set the value for the system-defined Response parameter
//					if (stepElement.getStepResponses() != null) {
//						wiiscLog.log(wiiscLog.INFO, "Step Responses");
//						String responseValue = "Ok";
//						stepElement.setSelectedResponse(responseValue);
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "No Step Responses");
//						wiiscLog.log(wiiscLog.INFO, "===========================================");
//					}
//
//					//DoStepElementAction - Save & Unlock the Workflow
//					fnWorkflowResult = doStepElementAction(stepElement, "Save", "", wiiscLog);
//				}
//			}
//			//If Parameters, call doStepElementParameterFields and doStepElementAction
//			else if (stepElementAction.equals("Parameters-Dispatch"))
//			{
//				wiiscLog.log(wiiscLog.INFO, "Parameters-Dispatch");
//				if (stepElementParamsMap.size() > 0)
//				{
//					//Lock the Step Element
//					stepElement.doLock(true);
//
//					//Update the Step Element
//					for (Map.Entry<String, String> entry : stepElementParamsMap.entrySet()) {
//						wiiscLog.log(wiiscLog.INFO, "Entry: " + entry.getKey() + " Value: " + entry.getValue());
//						stepElement.setParameterValue(entry.getKey(), entry.getValue(), true);
//					}
//
//					wiiscLog.log(wiiscLog.INFO, "===========================================");
//
//					// Set the value for the system-defined Response parameter
//					if (stepElement.getStepResponses() != null) {
//						wiiscLog.log(wiiscLog.INFO, "Step Responses");
//						String responseValue = "Ok";
//						stepElement.setSelectedResponse(responseValue);
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "No Step Responses");
//						wiiscLog.log(wiiscLog.INFO, "===========================================");
//					}
//
//					//DoStepElementAction - Save & Unlock the Workflow & Dispatch
//					fnWorkflowResult = doStepElementAction(stepElement, "Dispatch", "", wiiscLog);
//				}
//			}
//			//If Save, call doStepElementDataFields and doStepElementAction
//			else if (stepElementAction.equals("Save"))
//			{
//				wiiscLog.log(wiiscLog.INFO, "Save");
//				//Lock the Step Element
//				stepElement.doLock(true);
//
//				//Call doStepElementDataFields
//				fnWorkflowPropertyListResult =  doStepElementDataFields(stepElement, fnWorkflowRequest, wiiscLog);
//
//				//End of For Loop
//				wiiscLog.log(wiiscLog.INFO, "===========================================");
//
//				// Set the value for the system-defined Response parameter
//				if (stepElement.getStepResponses() != null) {
//					wiiscLog.log(wiiscLog.INFO, "Step Responses");
//					String responseValue = "Ok";
//					stepElement.setSelectedResponse(responseValue);
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "No Step Responses");
//					wiiscLog.log(wiiscLog.INFO, "===========================================");
//				}
//
//				//DoStepElementAction - Save & Unlock the Workflow
//				fnWorkflowResult = doStepElementAction(stepElement, stepElementAction, stepElementActionValue, wiiscLog);
//			}
//			//If Dispatch, call doStepElementDataFields and doStepElementAction
//			else if (stepElementAction.equals("Dispatch"))
//			{
//				wiiscLog.log(wiiscLog.INFO, "Dispatch");
//				//Lock the Step Element
//				stepElement.doLock(true);
//
//				//Call doStepElementDataFields
//				fnWorkflowPropertyListResult =  doStepElementDataFields(stepElement, fnWorkflowRequest, wiiscLog);
//
//				//End of For Loop
//				wiiscLog.log(wiiscLog.INFO, "===========================================");
//
//				// Set the value for the system-defined Response parameter
//				if (stepElement.getStepResponses() != null) {
//					wiiscLog.log(wiiscLog.INFO, "Step Responses");
//					String responseValue = "Ok";
//					stepElement.setSelectedResponse(responseValue);
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "No Step Responses");
//					wiiscLog.log(wiiscLog.INFO, "===========================================");
//				}
//
//				//DoStepElementAction - Save & Unlock the Workflow & Dispatch
//				fnWorkflowResult = doStepElementAction(stepElement, stepElementAction, stepElementActionValue, wiiscLog);
//			}
//			//If any other value, call doStepElementAction
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Other");
//				//Lock the Step Element
//				stepElement.doLock(true);
//				//DoStepElementAction - Save & Unlock the Workflow
//				fnWorkflowResult = doStepElementAction(stepElement, stepElementAction, stepElementActionValue, wiiscLog);
//			}
//			
//			//Add fnWorkflowPropertyList to fnWorkflowResult
//			fnWorkflowResult.setFnWorkflowPropertyList(fnWorkflowPropertyListResult);
//
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			//Update ErrorFlag
//			fnWorkflowPropertyListResult.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowPropertyListResult.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> updateStepElement()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflowResult;
//	}
//	
//	private FnWorkflow doStepElementAction(VWStepElement stepElement, String action, String actionValue, WIISCLog wiiscLog)
//	{
//		//Create the FnWorkflow Object
//		FnWorkflow fnWorkflowResult = new FnWorkflow();
//		//Create the FnWorkflowPropertyList Object
//		//FnWorkflowPropertyList fnWorkflowPropertyListResult = new FnWorkflowPropertyList();
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> doStepElementAction()");
//			//Check the Action
//			if (action.equals("Reassign"))
//			{
//				wiiscLog.log(wiiscLog.INFO, "Reassign");
//				// Determine whether a step element
//				// can be reassigned and reassign it
//				if (stepElement.getCanReassign())
//				{
//					wiiscLog.log(wiiscLog.INFO, "Able to Reassign");
//					String participantName = actionValue;
//					stepElement.doReassign(participantName,false,null);
//				}
//			}
//			else if (action.equals("Return"))
//			{
//				wiiscLog.log(wiiscLog.INFO, "Return");
//				// Determine whether a step element can be returned to the
//				// queue from which the user delegated or reassigned it and
//				// return it
//				if (stepElement.getCanReturnToSource())
//				{
//					wiiscLog.log(wiiscLog.INFO, "Able to Return");
//					stepElement.doReturnToSource();
//				}
//			}
//			else if (action.equals("Move"))
//			{
//				wiiscLog.log(wiiscLog.INFO, "Move");
//				// Determine whether a step element
//				// can be reassigned and reassign it
//				//
//				if (stepElement.getCanReassign())
//				{
//					wiiscLog.log(wiiscLog.INFO, "Able to Move");
//					String participantName = actionValue;
//					stepElement.doReassign(participantName,false,"Inbox");
//				}
//			}
//			else if (action.equals("Abort"))
//			{
//				wiiscLog.log(wiiscLog.INFO, "Abort");
//				// Cancel the changes to the work item
//				// without advancing it in the workflow
//				stepElement.doAbort();
//			}
//			else if (action.equals("Save"))
//			{
//				wiiscLog.log(wiiscLog.INFO, "Save");
//				// Save the changes to the work item
//				// and unlock it without advancing it in the workflow
//				stepElement.doSave(true);
//			}
//			else if (action.equals("Dispatch"))
//			{
//				wiiscLog.log(wiiscLog.INFO, "Dispatch");
//				// Save the changes to the work item
//				// and advance it in the workflow
//				stepElement.doDispatch();
//			}
//			
//			/*//Get FnWorkflowInfo
//			fnWorkflowResult = updateFnWorkflowInfo(stepElement, "Existing", wiiscLog);
//			//Get FnWorkflowPropertyListInfo
//			fnWorkflowPropertyListResult = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//			//Add the FnWorkflowPropertyList to FnWorkflow
//			fnWorkflowResult.setFnWorkflowPropertyList(fnWorkflowPropertyListResult);*/
//			
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			//Update ErrorFlag
//			fnWorkflowResult.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowResult.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> doStepElementAction()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflowResult;
//	}
//
//	private FnWorkflowList getQueueDBQuery(SQLServerConnection con, VWQueue vwQueue, String queueName, String[] filterName, String[] filterValue, WIISCLog wiiscLog)
//	{
//		//DB Query to the FileNet Queue to get the results
//		//Create the FnWorkflowList
//		FnWorkflowList fnWorkflowList = new FnWorkflowList();
//		Statement stmt = null;
//		ResultSet rs = null;
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getQueueDBQuery()");
//			//Workflow Region
//			String workflowRegion = "";
//			//Workflow Search Results Limit
//			String workflowSearchLimit = "";
//			//Workflow Data Fields Properties
//			String workflowDataFieldsProperties = "";
//			//Workflow Data Fields Properties file
//			ResourceBundle workflowDataFieldsProps = null;
//			//Workflow Data Fields FnWorkflowPropertyList
//			FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
//			//SQL Query
//			String SQL = "";
//						
//			//Get some Workflow Information
//			workflowRegion = globalConfig.getString("peWorkflowRegion");
//			workflowSearchLimit = globalConfig.getString("workflowSearchLimit");
//			workflowDataFieldsProperties = globalConfig.getString("workflowDataFieldsProperties");
//			
//			wiiscLog.log(wiiscLog.INFO, "Queue: " + queueName);
//			wiiscLog.log(wiiscLog.INFO, "FilterName[0]: " + filterName[0]);
//			wiiscLog.log(wiiscLog.INFO, "FilterValue[0]: " + filterValue[0]);
//			wiiscLog.log(wiiscLog.INFO, "Workflow Region: " + workflowRegion);
//									
//			//Build the Properties to retrieve from the SQL Query
//			if (checkPropertiesFileExist(workflowDataFieldsProperties, wiiscLog))
//			{
//				LocalResource resWorkflowFields = getLocalResource(workflowDataFieldsProperties);
//				workflowDataFieldsProps = resWorkflowFields.getLocalBundle(resWorkflowFields.getBundlePath(), resWorkflowFields.getBundleFile());
//				//Integer for the Property Count to use from the Doc Class Config Properties file
//				int propCount = 1;
//				//Get Workflow Data Field Properties
//				while (workflowDataFieldsProps.containsKey("prop" + propCount))
//				{
//					FnWorkflowProperty fnWorkflowProperty = new FnWorkflowProperty();
//					fnWorkflowProperty.setName(workflowDataFieldsProps.getString("prop" + propCount));
//					fnWorkflowPropertyList.addFnWorkflowProperty(fnWorkflowProperty);
//					propCount++;
//				}
//			}
//			
//			//Output FnWorkflowPropertyList
//			//outputFnWorkflowPropertyList(fnWorkflowPropertyList, wiiscLog);
//			
//			//Check Queue Field Names
//			VWQueueDefinition vwQueueDef = vwQueue.fetchQueueDefinition();
//			List<FnWorkflowProperty> fnWorkflowProps = fnWorkflowPropertyList.getFnWorkflowPropsList();
//			FnWorkflowPropertyList validFnWorkflowPropertyList = new FnWorkflowPropertyList();
//			
//			for (int i = 0; i < fnWorkflowProps.size(); i++)
//			{
//				FnWorkflowProperty fnWorkflowProp = fnWorkflowProps.get(i);
//				//Check for the Queue Field
//				if (vwQueueDef.hasFieldName(fnWorkflowProp.getName()))
//				{
//					//Add the FnWorkflowProperty to the validFnWorkflowPropertyList
//					validFnWorkflowPropertyList.addFnWorkflowProperty(fnWorkflowProp);
//				}
//			}
//			
//			//Clear the Temp List
//			fnWorkflowProps.clear();
//			
//			//Update FnWorkflowPropertyList
//			if (validFnWorkflowPropertyList.getCount() > 0)
//			{
//				//Clear the old FnWorkflowPropertyList
//				fnWorkflowPropertyList.getFnWorkflowPropsList().clear();
//				//Add the updated FnWorkflowPropertyList
//				fnWorkflowPropertyList.addFnWorkflowPropertyList(validFnWorkflowPropertyList);
//			}
//			
//			//Output FnWorkflowPropertyList
//			//outputFnWorkflowPropertyList(fnWorkflowPropertyList, wiiscLog);
//			
//			//Build the SQL Query Statement
//			if (filterName.length > 0)
//			{
//				wiiscLog.log(wiiscLog.INFO, "FilterName is being used");
//				//Check FnWorkflowPropertyList
//				if (fnWorkflowPropertyList.getCount() > 0)
//				{
//					String params = "";
//					fnWorkflowProps = fnWorkflowPropertyList.getFnWorkflowPropsList();
//					for (int i = 0; i < fnWorkflowProps.size(); i++)
//					{
//						if (params.length() == 0)
//						{
//							params = fnWorkflowProps.get(i).getName();
//						}
//						else
//						{
//							params = params + "," + fnWorkflowProps.get(i).getName();
//						}
//					}
//					wiiscLog.log(wiiscLog.INFO, "Select Params: " + params);
//					//Update SQL Query
//					if (workflowSearchLimit.length() == 0)
//					{
//						SQL = "SELECT " + params + " FROM VWVQ" + workflowRegion + "_" + queueName + " where " + filterName[0] + " = '" + filterValue[0] + "'";
//					}
//					else
//					{
//						SQL = "SELECT TOP " + workflowSearchLimit + " " + params + " FROM VWVQ" + workflowRegion + "_" + queueName + " where " + filterName[0] + " = '" + filterValue[0] + "'";
//					}
//				}
//				else
//				{
//					//Update SQL Query
//					if (workflowSearchLimit.length() == 0)
//					{
//						SQL = "SELECT * FROM VWVQ" + workflowRegion + "_" + queueName + " where " + filterName[0] + " = '" + filterValue[0] + "'";
//					}
//					else
//					{
//						SQL = "SELECT TOP " + workflowSearchLimit + " * FROM VWVQ" + workflowRegion + "_" + queueName + " where " + filterName[0] + " = '" + filterValue[0] + "'";
//					}
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "FilterName is NOT being used");
//				//Check FnWorkflowPropertyList
//				if (fnWorkflowPropertyList.getCount() > 0)
//				{
//					String params = "";
//					fnWorkflowProps = fnWorkflowPropertyList.getFnWorkflowPropsList();
//					for (int i = 0; i < fnWorkflowProps.size(); i++)
//					{
//						if (params.length() == 0)
//						{
//							params = fnWorkflowProps.get(i).getName();
//						}
//						else
//						{
//							params = params + "," + fnWorkflowProps.get(i).getName();
//						}
//					}
//					wiiscLog.log(wiiscLog.INFO, "Select Params: " + params);
//					//Update SQL Query
//					if (workflowSearchLimit.length() == 0)
//					{
//						SQL = "SELECT " + params + " FROM VWVQ" + workflowRegion + "_" + queueName;
//					}
//					else
//					{
//						SQL = "SELECT TOP " + workflowSearchLimit + " " + params + " FROM VWVQ" + workflowRegion + "_" + queueName;
//					}
//				}
//				else
//				{
//					//Update SQL Query
//					if (workflowSearchLimit.length() == 0)
//					{
//						SQL = "SELECT * FROM VWVQ" + workflowRegion + "_" + queueName;
//					}
//					else
//					{
//						SQL = "SELECT TOP " + workflowSearchLimit + " * FROM VWVQ" + workflowRegion + "_" + queueName;
//					}
//				}
//			}
//			wiiscLog.log(wiiscLog.INFO, "SQL Query: " + SQL); 
//
//			//Execute an SQL statement that returns some data.
//			stmt = con.createStatement();
//			rs = stmt.executeQuery(SQL);
//
//			// Iterate through the data in the result set and display it.
//			while (rs.next())
//			{
//				String propName = "";
//				String propValue = "";
//				FnWorkflow fnWorkflow = new FnWorkflow();
//				FnWorkflowPropertyList fnWorkflowPropertyListResult = new FnWorkflowPropertyList();
//				
//				List<FnWorkflowProperty> fnWorkflowPropsResult = fnWorkflowPropertyList.getFnWorkflowPropsList();
//				for (int i = 0; i < fnWorkflowPropsResult.size(); i++)
//				{
//					//Get Prop Name
//					propName = fnWorkflowPropsResult.get(i).getName();
//					propValue = rs.getString(propName);
//					wiiscLog.log(wiiscLog.INFO, "Property Name: " + propName);
//					wiiscLog.log(wiiscLog.INFO, "Property Value: " + propValue);
//					//Create the FnWorkflowProperty
//					FnWorkflowProperty fnWorkflowProperty = new FnWorkflowProperty();
//					//Set the FnWorkflowProperty Name
//					fnWorkflowProperty.setName(propName);
//					//Set the FnWorkflowProperty Value from the DB
//					fnWorkflowProperty.setValue(propValue);
//					//Add the FnWorkflowProperty to the FnWorkflowPropertyListResult
//					fnWorkflowPropertyListResult.addFnWorkflowProperty(fnWorkflowProperty);
//				}
//				//Separate the Workflow Results
//				wiiscLog.log(wiiscLog.INFO, "===========================================================");
//				if (fnWorkflowPropertyListResult.getCount() > 0)
//				{
//					//Add the FnWorkflowPropertyListResult to the FnWorkflow
//					fnWorkflow.setFnWorkflowPropertyList(fnWorkflowPropertyListResult);
//					//Add the FnWorkflow to the FnWorkflowList
//					fnWorkflowList.addFnWorkflow(fnWorkflow);
//				}
//				else
//				{
//					//Add the FnWorkflow to the FnWorkflowList
//					fnWorkflowList.addFnWorkflow(fnWorkflow);
//				}
//			}
//			wiiscLog.log(wiiscLog.INFO, "FileNet Workflow List from DB Query Finished");
//		}
//
//		// Handle any errors that may have occurred.
//		catch (Exception e)
//		{
//			//e.printStackTrace();
//			wiiscLog.log("ERROR", e.getMessage());
//		}
//		finally
//		{
//			if (rs != null)
//			{
//				try
//				{
//					wiiscLog.log(wiiscLog.INFO, "Closing ResultSet");
//					rs.close();
//				}
//				catch(Exception e)
//				{
//					wiiscLog.log(wiiscLog.INFO, "Error closing ResultSet");
//					wiiscLog.log("ERROR", e.getMessage());
//				}
//			}
//			if (stmt != null)
//			{
//				try
//				{
//					wiiscLog.log(wiiscLog.INFO, "Closing Statement");
//					stmt.close();
//				}
//				catch(Exception e)
//				{
//					wiiscLog.log(wiiscLog.INFO, "Error closing Statement");
//					wiiscLog.log("ERROR", e.getMessage());
//				}
//			}
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getQueueDBQuery()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflowList;
//	}
//	
//	private VWQueueQuery getQueueQuery(VWQueue vwQueue, String[] filterName, String[] filterValue, String sortBy, WIISCLog wiiscLog)
//	{
//		//Create the VWQueueQuery Object
//		VWQueueQuery query = null;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getQueueQuery()");
//			//Fetch Count
//			int fetchCount = 0;
//			//Query Flags
//			int queryFlags = VWQueue.QUERY_NO_OPTIONS;
//			String queryFilter="";
//			//Object[] substitutionVars = null;
//			int fetchType = VWFetchType.FETCH_TYPE_QUEUE_ELEMENT;
//
//			//Update Queue Buffer Size
//			fetchCount = vwQueue.fetchCount();
//			//Make sure fetchCount is > 0, otherwise skip
//			if (fetchCount > 0)
//			{
//				//Setup the BufferSize for the Query
//				if (fetchCount > 200)
//				{
//					vwQueue.setBufferSize(200);
//					wiiscLog.log(wiiscLog.INFO, "Buffer Size set to 200");
//				}
//				else if (fetchCount > 50 && fetchCount < 200)
//				{
//					vwQueue.setBufferSize(fetchCount);
//					wiiscLog.log(wiiscLog.INFO, "Buffer Size set to " + fetchCount);
//				}
//				else
//				{
//					vwQueue.setBufferSize(50);
//					wiiscLog.log(wiiscLog.INFO, "Buffer Size set to 50");
//				}
//				
//				//Check if FilterName and FilterValue are Valid
//				if (filterName != null && filterValue != null)
//				{
//					//Setup variables for Query
//					Object[] substitutionVars = null;
//					Object[] minValues = null;
//					Object[] maxValues = null;
//					
//					//Check if Query Filter, Substitution Vars, Min and Max need updated
//					if (filterName.length == 3 && filterValue.length == 3)
//					{
//						wiiscLog.log(wiiscLog.INFO, "Filter Name and Value is Length of 3");
//						wiiscLog.log(wiiscLog.INFO, "filterName[0]: " + filterName[0]);
//						wiiscLog.log(wiiscLog.INFO, "filterValue[0]: " + filterValue[0]);
//						wiiscLog.log(wiiscLog.INFO, "filterName[1]: " + filterName[1]);
//						wiiscLog.log(wiiscLog.INFO, "filterValue[1]: " + filterValue[1]);
//						wiiscLog.log(wiiscLog.INFO, "filterName[2]: " + filterName[2]);
//						wiiscLog.log(wiiscLog.INFO, "filterValue[2]: " + filterValue[2]);
//						//Used for Filtered Query
//						//32 + 64
//						//queryFlags = VWQueue.QUERY_MIN_VALUES_INCLUSIVE + VWQueue.QUERY_MAX_VALUES_INCLUSIVE;
//						//0
//						queryFlags = VWQueue.QUERY_NO_OPTIONS;
//						
//						queryFilter = filterName[0] + "=:A AND " + filterName[1] + "=:B AND " + filterName[2] + "=:C";
//						substitutionVars = new String[3];
//						substitutionVars[0] = filterValue[0];
//						substitutionVars[1] = filterValue[1];
//						substitutionVars[2] = filterValue[2];
//						
//						minValues = new String[3];
//						minValues[0] = filterValue[0];
//						minValues[1] = filterValue[1];
//						minValues[2] = filterValue[2];
//						
//						maxValues = new String[3];
//						maxValues[0] = filterValue[0];
//						maxValues[1] = filterValue[1];
//						maxValues[2] = filterValue[2];
//
//						//Run the Queue Query
//						//query = vwQueue.createQuery(null, minValues, maxValues, queryFlags, queryFilter, substitutionVars, fetchType);
//						//query = vwQueue.createQuery("Indx_" + filterName[0], null, null, queryFlags, queryFilter, substitutionVars, fetchType);
//						//query = vwQueue.createQuery(null, null, null, queryFlags, queryFilter, substitutionVars, fetchType);					
//					}
//					else if (filterName.length == 2 && filterValue.length == 2)
//					{
//						wiiscLog.log(wiiscLog.INFO, "Filter Name and Value is Length of 2");
//						wiiscLog.log(wiiscLog.INFO, "filterName[0]: " + filterName[0]);
//						wiiscLog.log(wiiscLog.INFO, "filterValue[0]: " + filterValue[0]);
//						wiiscLog.log(wiiscLog.INFO, "filterName[1]: " + filterName[1]);
//						wiiscLog.log(wiiscLog.INFO, "filterValue[1]: " + filterValue[1]);
//						//Used for Filtered Query
//						//32 + 64
//						//queryFlags = VWQueue.QUERY_MIN_VALUES_INCLUSIVE + VWQueue.QUERY_MAX_VALUES_INCLUSIVE;
//						//0
//						queryFlags = VWQueue.QUERY_NO_OPTIONS;
//						
//						queryFilter = filterName[0] + "=:A AND " + filterName[1] + "=:B";
//						substitutionVars = new String[2];
//						substitutionVars[0] = filterValue[0];
//						substitutionVars[1] = filterValue[1];
//												
//						minValues = new String[2];
//						minValues[0] = filterValue[0];
//						minValues[1] = filterValue[1];
//												
//						maxValues = new String[2];
//						maxValues[0] = filterValue[0];
//						maxValues[1] = filterValue[1];
//												
//						//Run the Queue Query
//						//query = vwQueue.createQuery(null, minValues, maxValues, queryFlags, queryFilter, substitutionVars, fetchType);
//						//query = vwQueue.createQuery("Indx_" + filterName[0] + "+" + "Indx_" + filterName[1], null, null, queryFlags, queryFilter, substitutionVars, fetchType);
//						//query = vwQueue.createQuery(null, null, null, queryFlags, queryFilter, substitutionVars, fetchType);
//					}
//					else if (filterName.length == 1 && filterValue.length == 1)
//					{
//						wiiscLog.log(wiiscLog.INFO, "Filter Name and Value is Length of 1");
//						wiiscLog.log(wiiscLog.INFO, "filterName[0]: " + filterName[0]);
//						wiiscLog.log(wiiscLog.INFO, "filterValue[0]: " + filterValue[0]);
//						//Used for Filtered Query
//						//32 + 64
//						//queryFlags = VWQueue.QUERY_MIN_VALUES_INCLUSIVE + VWQueue.QUERY_MAX_VALUES_INCLUSIVE;
//						//0
//						queryFlags = VWQueue.QUERY_NO_OPTIONS;
//						//queryFlags = VWQueue.QUERY_GET_NO_SYSTEM_FIELDS + VWQueue.QUERY_GET_NO_TRANSLATED_SYSTEM_FIELDS;
//						
//						queryFilter = filterName[0] + " = :A";
//						substitutionVars = new String[1];
//						substitutionVars[0] = filterValue[0];
//												
//						minValues = new String[1];
//						minValues[0] = filterValue[0];
//												
//						maxValues = new String[1];
//						maxValues[0] = filterValue[0];
//												
//						//Run the Queue Query
//						//query = vwQueue.createQuery(null, minValues, maxValues, queryFlags, queryFilter, substitutionVars, fetchType);
//						//query = vwQueue.createQuery("Indx_" + filterName[0], null, null, queryFlags, queryFilter, substitutionVars, fetchType);
//						//query = vwQueue.createQuery(null, null, null, queryFlags, queryFilter, substitutionVars, fetchType);
//					}
//					else
//					{
//						//Do Nothing
//					}
//					
//					//Setup the Query and Check if sortBy is being used
//					if (sortBy.length() == 0)
//					{
//						//sortBy was empty
//						wiiscLog.log(wiiscLog.INFO, "sortBy: " + sortBy);
//						query = vwQueue.createQuery(null, null, null, queryFlags, queryFilter, substitutionVars, fetchType);
//					}
//					else
//					{
//						//sortBy was not empty
//						wiiscLog.log(wiiscLog.INFO, "sortBy: " + sortBy);
//						query = vwQueue.createQuery("Indx_" + sortBy, null, null, queryFlags, queryFilter, substitutionVars, fetchType);
//					}
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "Filter Name and Value is Length of Null");
//					
//					//Used to Return All Workflows with No Filter
//					//Setup the Query and Check if sortBy is being used
//					if (sortBy.length() == 0)
//					{
//						//sortBy was empty
//						wiiscLog.log(wiiscLog.INFO, "sortBy: " + sortBy);
//						query = vwQueue.createQuery(null, null, null, queryFlags, queryFilter, null, fetchType);
//					}
//					else
//					{
//						//sortBy not was empty
//						wiiscLog.log(wiiscLog.INFO, "sortBy: " + sortBy);
//						query = vwQueue.createQuery("Indx_" + sortBy, null, null, queryFlags, queryFilter, null, fetchType);
//					}
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Queue has No Records so a query will not be performed");
//				query = null;
//			}
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			query = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getQueueQuery()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return query;
//	}
//
//	private VWQueueQuery getQueueQueryByWobNum(VWQueue vwQueue, String wobNum, WIISCLog wiiscLog)
//	{
//		//Create the VWQueueQuery Object
//		VWQueueQuery query = null;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getQueueQueryByWobNum()");
//			int queryFlags = VWQueue.QUERY_NO_OPTIONS;
//			String queryFilter="";
//			//Object[] substitutionVars = null;
//			int fetchType = VWFetchType.FETCH_TYPE_QUEUE_ELEMENT;
//			
//			//Check if Query Filter needs updated
//			if (wobNum.length() > 0)
//			{
//				queryFilter = "F_WobNum=:A";
//				Object[] substitutionVars = {new VWWorkObjectNumber(wobNum)};
//				//Update Query Buffer Size
//				vwQueue.setBufferSize(1);
//				//Run the Queue Query
//				query = vwQueue.createQuery(null, null, null, queryFlags, queryFilter, substitutionVars, fetchType);
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "WobNum was missing from the request");
//				query = null;
//			}
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			query = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getQueueQueryByWobNum()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return query;
//	}
//
//	private VWRosterQuery getRosterQueryByWobNum(VWRoster vwRoster, String wobNum, WIISCLog wiiscLog)
//	{
//		//Create the VWRosterQuery Object
//		VWRosterQuery query = null;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getRosterQueryByWobNum()");
//			int queryFlags = VWRoster.QUERY_NO_OPTIONS;
//			String queryFilter="";
//			//Object[] substitutionVars = null;
//			int fetchType = VWFetchType.FETCH_TYPE_ROSTER_ELEMENT;
//
//			//Check if Query Filter needs updated
//			if (wobNum.length() > 0)
//			{
//				queryFilter = "F_WobNum=:A";
//				Object[] substitutionVars = {new VWWorkObjectNumber(wobNum)};
//				//Update Roster Buffer Size
//				vwRoster.setBufferSize(1);
//				//Run the Roster Query
//				query = vwRoster.createQuery(null, null, null, queryFlags, queryFilter, substitutionVars, fetchType);
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "WobNum was missing from the request");
//				query = null;
//			}
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			query = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getRosterQueryByWobNum()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return query;
//	}
//
//	private VWRosterQuery getRosterQuery(VWRoster vwRoster, String[] filterName, String[] filterValue, WIISCLog wiiscLog)
//	{
//		//Create the VWRosterQuery Object
//		VWRosterQuery query = null;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getRosterQuery()");
//			//Fetch Count
//			int fetchCount = 0;
//			//Query Flags
//			int queryFlags = VWRoster.QUERY_NO_OPTIONS;
//			String queryFilter="";
//			//Object[] substitutionVars = null;
//			int fetchType = VWFetchType.FETCH_TYPE_ROSTER_ELEMENT;
//
//			//Update Roster Buffer Size
//			fetchCount = vwRoster.fetchCount();
//			//Make sure fetchCount is > 0, otherwise skip
//			if (fetchCount > 0)
//			{
//				wiiscLog.log(wiiscLog.INFO, "Roster Count was " + fetchCount);
//				if (fetchCount > 200)
//				{
//					vwRoster.setBufferSize(200);
//				}
//				else if (fetchCount > 50 && fetchCount < 200)
//				{
//					vwRoster.setBufferSize(fetchCount);
//				}
//				else
//				{
//					vwRoster.setBufferSize(50);
//				}
//				
//				//Check if FilterName and FilterValue are Valid
//				if (filterName != null && filterValue != null)
//				{
//					//Check if Query Filter, Substitution Vars, Min and Max need updated
//					if (filterName.length == 3 && filterValue.length == 3)
//					{
//						wiiscLog.log(wiiscLog.INFO, "Filter Name and Value is Length of 3");
//						wiiscLog.log(wiiscLog.INFO, "filterName[0]: " + filterName[0]);
//						wiiscLog.log(wiiscLog.INFO, "filterValue[0]: " + filterValue[0]);
//						wiiscLog.log(wiiscLog.INFO, "filterName[1]: " + filterName[1]);
//						wiiscLog.log(wiiscLog.INFO, "filterValue[1]: " + filterValue[1]);
//						wiiscLog.log(wiiscLog.INFO, "filterName[2]: " + filterName[2]);
//						wiiscLog.log(wiiscLog.INFO, "filterValue[2]: " + filterValue[2]);
//						//Used for Filtered Query
//						//32 + 64
//						//queryFlags = VWRoster.QUERY_MIN_VALUES_INCLUSIVE + VWRoster.QUERY_MAX_VALUES_INCLUSIVE;
//						//0
//						queryFlags = VWRoster.QUERY_NO_OPTIONS;
//						
//						queryFilter = filterName[0] + "=:A AND " + filterName[1] + "=:B AND " + filterName[2] + "=:C";
//						Object[] substitutionVars = {filterValue[0], filterValue[1], filterValue[2]};
//						Object[] minValues = {filterValue[0], filterValue[1], filterValue[2]};
//						Object[] maxValues = {filterValue[0], filterValue[1], filterValue[2]};
//
//						//Run the Roster Query
//						//query = vwRoster.createQuery(null, minValues, maxValues, queryFlags, queryFilter, substitutionVars, fetchType);
//						query = vwRoster.createQuery(null, null, null, queryFlags, queryFilter, substitutionVars, fetchType);
//					}
//					else if (filterName.length == 2 && filterValue.length == 2)
//					{
//						wiiscLog.log(wiiscLog.INFO, "Filter Name and Value is Length of 2");
//						wiiscLog.log(wiiscLog.INFO, "filterName[0]: " + filterName[0]);
//						wiiscLog.log(wiiscLog.INFO, "filterValue[0]: " + filterValue[0]);
//						wiiscLog.log(wiiscLog.INFO, "filterName[1]: " + filterName[1]);
//						wiiscLog.log(wiiscLog.INFO, "filterValue[1]: " + filterValue[1]);
//						//Used for Filtered Query
//						//32 + 64
//						//queryFlags = VWRoster.QUERY_MIN_VALUES_INCLUSIVE + VWRoster.QUERY_MAX_VALUES_INCLUSIVE;
//						//0
//						queryFlags = VWRoster.QUERY_NO_OPTIONS;
//						
//						queryFilter = filterName[0] + "=:A AND " + filterName[1] + "=:B";
//						Object[] substitutionVars = {filterValue[0], filterValue[1]};
//						Object[] minValues = {filterValue[0], filterValue[1]};
//						Object[] maxValues = {filterValue[0], filterValue[1]};
//
//						//Run the Roster Query
//						//query = vwRoster.createQuery(null, minValues, maxValues, queryFlags, queryFilter, substitutionVars, fetchType);
//						query = vwRoster.createQuery(null, null, null, queryFlags, queryFilter, substitutionVars, fetchType);
//					}
//					else if (filterName.length == 1 && filterValue.length == 1)
//					{
//						wiiscLog.log(wiiscLog.INFO, "Filter Name and Value is Length of 1");
//						wiiscLog.log(wiiscLog.INFO, "filterName[0]: " + filterName[0]);
//						wiiscLog.log(wiiscLog.INFO, "filterValue[0]: " + filterValue[0]);
//						//Used for Filtered Query
//						//32 + 64
//						//queryFlags = VWRoster.QUERY_MIN_VALUES_INCLUSIVE + VWRoster.QUERY_MAX_VALUES_INCLUSIVE;
//						//0
//						queryFlags = VWRoster.QUERY_NO_OPTIONS;
//												
//						queryFilter = filterName[0] + " = :A";
//						Object[] substitutionVars = {filterValue[0]};
//						Object[] minValues = {filterValue[0]};
//						Object[] maxValues = {filterValue[0]};
//
//						//Run the Roster Query
//						//query = vwRoster.createQuery(null, minValues, maxValues, queryFlags, queryFilter, substitutionVars, fetchType);
//						query = vwRoster.createQuery(null, null, null, queryFlags, queryFilter, substitutionVars, fetchType);
//					}
//					else
//					{
//						//Do Nothing
//					}
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "Filter Name and Value is Length of Null");
//					
//					//Used to Return All Workflows with No Filter
//					//Run the Roster Query
//					query = vwRoster.createQuery(null, null, null, queryFlags, queryFilter, null, fetchType);
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Roster Count was " + fetchCount);
//				wiiscLog.log(wiiscLog.INFO, "Roster has No Records so a query will not be performed");
//				query = null;
//			}
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			query = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getRosterQuery()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return query;
//	}
//
//	private String[] getQueues(VWSession vwSession, WIISCLog wiiscLog)
//	{
//		String[] queueNames = null;
//		//VWQueue vwQueue = null;
//		int qFlags = VWSession.QUEUE_PROCESS | VWSession.QUEUE_USER_CENTRIC | VWSession.QUEUE_IGNORE_SECURITY | VWSession.QUEUE_SYSTEM;
//		int queueTotal = 0;
//		//int wfCount = 0;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getQueues()");
//			//Retrieve List of Available Queues
//			queueNames = vwSession.fetchQueueNames(qFlags);
//			//Check the length of the QueueNames
//			if (queueNames.length == 0)
//			{
//				wiiscLog.log(wiiscLog.INFO, "There are NO Queues");
//			}
//			else
//			{
//				for (int i = 0; i < queueNames.length; i++)
//				{
//					//vwQueue = vwSession.getQueue(queueNames[i]);
//					//wfCount = vwQueue.fetchCount();
//					//wiiscLog.log(wiiscLog.INFO, "Workflow Queue Count -> " + queueNames[i] + " = " + wfCount);
//					queueTotal++;
//				}
//				wiiscLog.log(wiiscLog.INFO, "There are " + queueTotal + " Queues");
//			}
//		}
//		catch(VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			queueNames = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getQueues()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return queueNames;
//	}
//
//	private String[] getRosters(VWSession vwSession, WIISCLog wiiscLog)
//	{
//		String[] rosterNames = null;
//		VWRoster vwRoster = null;
//		int rosterTotal = 0;
//		int wfCount = 0;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getRosters()");
//			//Retrieve List of Available Rosters
//			rosterNames = vwSession.fetchRosterNames(true);
//			//Check the length of the RosterNames
//			if (rosterNames.length == 0)
//			{
//				wiiscLog.log(wiiscLog.INFO, "There are NO Rosters");
//			}
//			else
//			{
//				for (int i = 0; i < rosterNames.length; i++)
//				{
//					vwRoster = vwSession.getRoster(rosterNames[i]);
//					wfCount = vwRoster.fetchCount();
//					wiiscLog.log(wiiscLog.INFO, "Workflow Roster Count -> " + rosterNames[i] + " = " + wfCount);
//					rosterTotal++;
//				}
//				wiiscLog.log(wiiscLog.INFO, "There are " + rosterTotal + " Rosters");
//			}
//		}
//		catch(VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			rosterNames = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getRosters()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return rosterNames;
//	}
//
//	private VWStepElement getWorkflowStepElement(VWSession vwSession, String process, String step, String user, String propName, String propValue, WIISCLog wiiscLog)
//	{
//		//StepElement to Return
//		VWStepElement stepElement = null;
//		//Get the Filter Name
//		String[] filterName = null;
//		//Get the Filter Value
//		String[] filterValue = null;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getWorkflowStepElement()");
//
//			//Fix any Null values
//			if (process == null)
//			{
//				process = "";				
//			}
//			if (step == null)
//			{
//				step = "";
//			}
//			if (user == null)
//			{
//				user = "";
//			}
//			//Assuming PropName and PropValue are never passed to this method with a Null Value
//			/*********************************************************************************
//			 * Find the Workflow by a Step, Property and Property Value - workflow data field
//			 *********************************************************************************/
//			if (step.length() > 0 && propName.length() > 0 && propValue.length() > 0)
//			{
//				//*********************************************************************
//				//This option allows for Process to have a Value or Not have a Value
//				//*********************************************************************
//				//By a Step, Property and Property Value
//				wiiscLog.log(wiiscLog.INFO, "Get a Workflow by a Step, Property and Property Value");
//
//				//Initialize the Array
//				filterName = new String[1];
//				filterValue = new String[1];
//				filterName[0] = propName;
//				filterValue[0] = propValue;
//
//				//Set the Queue
//				String queueName = step;
//				//Check queueName
//				if (queueName.length() > 0)
//				{
//					//Exclude the System Queues
//					if (!queueName.contains("Instruction") &&
//							!queueName.contains("(") &&
//							!queueName.equals("Conductor") &&
//							!queueName.equals("WSRequest") &&
//							!queueName.equals("CE_Operations"))
//					{
//						//Get the Workflow
//						//fnWorkflowResult = getFnWorkflowByQueue(vwSession, queueName, filterName, filterValue, wiiscLog);
//
//						wiiscLog.log(wiiscLog.INFO, "Queue: " + queueName);
//						VWQueue vwQueue = getQueue(vwSession, queueName, wiiscLog);
//						//Verify the Queue was OK
//						if (vwQueue != null)
//						{
//							//Get the Queue Elements
//							VWQueueQuery query = null;
//							if (queueName.equals("Inbox(0)"))
//							{
//								//Different Query to get a User's Inbox Items
//								query = getQueueQuery(vwQueue, null, null, "", wiiscLog);
//							}
//							else
//							{
//								query = getQueueQuery(vwQueue, filterName, filterValue, "", wiiscLog);
//							}
//							//Verify the Query was OK
//							if (query != null && query.hasNext())
//							{
//								wiiscLog.log(wiiscLog.INFO, "Query Not Null");
//								//Process the Results
//								while(query.hasNext())
//								{
//									wiiscLog.log(wiiscLog.INFO, "Query Has Next");
//									VWQueueElement queueItem = (VWQueueElement) query.next();
//									stepElement = queueItem.fetchStepElement(false, false);
//									break;
//								}//End While
//							}//End If Query
//						}//End If VWQueue
//					}//End If QueueName specifics
//				}//End If QueueName
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "There was No Queue, so there is No Workflow");
//					//Update the StepElement value
//					stepElement = null;
//				}
//			}//End If Step - Property - Value
//			/*********************************************************************************
//			 * Find the Workflow by a Property and Property Value - workflow data field
//			 *********************************************************************************/
//			else if (step.length() == 0 && propName.length() > 0 && propValue.length() > 0)
//			{
//				//By Property and Property Value only
//				wiiscLog.log(wiiscLog.INFO, "Get a Workflow by a Property and Property Value");
//
//				//Initialize the Array
//				filterName = new String[1];
//				filterValue = new String[1];
//				filterName[0] = propName;
//				filterValue[0] = propValue;
//
//				//Get String[] of Queues
//				String[] queueNames = getQueues(vwSession, wiiscLog);
//				//Check queueNames
//				if (queueNames.length > 0)
//				{
//					//Loop and Get Workflows from each Queue
//					for (int i = 0; i < queueNames.length; i++)
//					{
//						//Exclude the System Queues
//						if (!queueNames[i].contains("Instruction") &&
//								!queueNames[i].contains("(") &&
//								!queueNames[i].equals("Conductor") &&
//								!queueNames[i].equals("WSRequest") &&
//								!queueNames[i].equals("CE_Operations"))
//						{
//							wiiscLog.log(wiiscLog.INFO, "Queue: " + queueNames[i]);
//							VWQueue vwQueue = getQueue(vwSession, queueNames[i], wiiscLog);
//							//Verify the Queue was OK
//							if (vwQueue != null)
//							{
//								//Get the Queue Elements
//								VWQueueQuery query = null;
//								if (queueNames[i].equals("Inbox(0)"))
//								{
//									//Different Query to get a User's Inbox Items
//									query = getQueueQuery(vwQueue, null, null, "", wiiscLog);
//								}
//								else
//								{
//									query = getQueueQuery(vwQueue, filterName, filterValue, "", wiiscLog);
//								}
//								//Verify the Query was OK
//								if (query != null && query.hasNext())
//								{
//									wiiscLog.log(wiiscLog.INFO, "Query Not Null");
//									//Process the Results
//									while(query.hasNext())
//									{
//										wiiscLog.log(wiiscLog.INFO, "Query Has Next");
//										VWQueueElement queueItem = (VWQueueElement) query.next();
//										stepElement = queueItem.fetchStepElement(false, false);
//										break;
//									}//End While
//								}//End If Query
//							}//End If VWQueue
//						}//End If QueueName specifics
//					}//End If QueueName
//				}//End If Property - Value
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "There were No Queues, so there are No Workflows");
//					//Update the StepElement value
//					stepElement = null;
//				}
//			}
//			else
//			{
//				//Return an Error
//				wiiscLog.log(wiiscLog.INFO, "The Parameters Step, Property Name and Property Value are required to find the Workflow");
//				//Update the StepElement value
//				stepElement = null;
//			}
//		}
//		catch(VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			stepElement = null;
//		}
//
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getWorkflowStepElement()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return stepElement;
//	}
//
//	private FnWorkflow getWorkflowStep(VWSession vwSession, String step, String wobNum, WIISCLog wiiscLog)
//	{
//		//Create FnWorkflow
//		FnWorkflow fnWorkflow = new FnWorkflow();
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getWorkflow()");
//			//Update the Workflow
//			fnWorkflow.setFnWorkflowStep(step);
//			fnWorkflow.setFnWorkflowQueue(step);
//			//By step only
//			VWQueue vwQueue = getQueue(vwSession, step, wiiscLog);
//			//Verify the Queue was OK
//			if (vwQueue != null)
//			{
//				//Get the Queue Elements by WobNum
//				VWQueueQuery query = getQueueQueryByWobNum(vwQueue, wobNum, wiiscLog);
//				//Verify the Query was OK
//				if (query != null)
//				{
//					//Process the Results
//					while(query.hasNext())
//					{
//						VWQueueElement queueItem = (VWQueueElement) query.next();
//						VWStepElement stepElement = queueItem.fetchStepElement(false, false);
//						if (stepElement != null)
//						{
//							//Update the FnWorkflow
//							fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing", wiiscLog);
//
//							//Create the FnWorkflowPropertyList
//							FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
//							fnWorkflowPropertyList = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//
//							//Add the FnWorkflowPropertyList to the FnWorkflow
//							fnWorkflow.setFnWorkflowPropertyList(fnWorkflowPropertyList);
//							//Update the FnWorkflow Object
//							fnWorkflow.setErrorFlag(0);
//							fnWorkflow.setErrorMessage("");
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "Failed to get a StepElement");
//							//Update the FnWorkflow Object
//							fnWorkflow.setErrorFlag(1);
//							fnWorkflow.setErrorMessage("Failed to get a StepElement");
//							fnWorkflow.setFnWorkflowStatus("Failed to get a StepElement");
//						}
//					}
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "Failed to get a QueueQuery");
//					//Update the FnWorkflow Object
//					fnWorkflow.setErrorFlag(1);
//					fnWorkflow.setErrorMessage("Failed to get a QueueQuery");
//					fnWorkflow.setFnWorkflowStatus("Failed to get a QueueQuery");
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Failed to get a VWQueue");
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(1);
//				fnWorkflow.setErrorMessage("Failed to get a VWQueue");
//				fnWorkflow.setFnWorkflowStatus("Failed to get a VWQueue");
//			}
//		}
//		catch(VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			fnWorkflow = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getWorkflowStep()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflow;
//	}
//
//	private FnWorkflow getWorkflow(VWSession vwSession, String process, String wobNum, WIISCLog wiiscLog)
//	{
//		//Create FnWorkflow
//		FnWorkflow fnWorkflowResult = new FnWorkflow();
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getWorkflow()");
//			//Update the Workflow
//			fnWorkflowResult.setFnWorkflowProcess(process);
//			fnWorkflowResult.setFnWorkflowRoster(process);
//			//By process_sys_code only
//			VWRoster vwRoster = getRoster(vwSession, process, wiiscLog);
//			//Verify the Roster was OK
//			if (vwRoster != null)
//			{
//				//Get the Roster Elements by WobNum
//				VWRosterQuery query = getRosterQueryByWobNum(vwRoster, wobNum, wiiscLog);
//				//Verify the Query was OK
//				if (query != null)
//				{
//					//Process the Results
//					if (query.hasNext())
//					{
//						while(query.hasNext())
//						{
//							wiiscLog.log(wiiscLog.INFO, "Found Workflow");
//							VWRosterElement rosterItem = (VWRosterElement) query.next();
//							VWStepElement stepElement = rosterItem.fetchStepElement(false, false);
//							if (stepElement != null)
//							{
//								//Update the FnWorkflow
//								fnWorkflowResult = updateFnWorkflowInfo(stepElement, "Existing", wiiscLog);
//
//								//Create the FnWorkflowPropertyList
//								FnWorkflowPropertyList fnWorkflowPropertyListResult = new FnWorkflowPropertyList();
//								fnWorkflowPropertyListResult = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//
//								//Add the FnWorkflowPropertyList to the FnWorkflow
//								fnWorkflowResult.setFnWorkflowPropertyList(fnWorkflowPropertyListResult);
//								//Update the FnWorkflow Object
//								fnWorkflowResult.setErrorFlag(0);
//								fnWorkflowResult.setErrorMessage("");
//								fnWorkflowResult.setFnWorkflowStatus("");
//							}
//							else
//							{
//								wiiscLog.log(wiiscLog.INFO, "Failed to get a StepElement");
//								//Update the FnWorkflow Object
//								fnWorkflowResult.setErrorFlag(1);
//								fnWorkflowResult.setErrorMessage("Failed to get a StepElement");
//								fnWorkflowResult.setFnWorkflowStatus("Failed to get a StepElement");
//							}
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "Workflow has Finished.  No StepElement available.");
//						//Update the FnWorkflow Object
//						fnWorkflowResult.setErrorFlag(0);
//						fnWorkflowResult.setErrorMessage("Workflow Finished");
//						fnWorkflowResult.setFnWorkflowStatus("Workflow Finished");
//					}
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "Failed to get a RosterQuery");
//					//Update the FnWorkflow Object
//					fnWorkflowResult.setErrorFlag(1);
//					fnWorkflowResult.setErrorMessage("Failed to get a RosterQuery");
//					fnWorkflowResult.setFnWorkflowStatus("Failed to get a RosterQuery");
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Failed to get a VWRoster");
//				//Update the FnWorkflow Object
//				fnWorkflowResult.setErrorFlag(1);
//				fnWorkflowResult.setErrorMessage("Failed to get a VWRoster");
//				fnWorkflowResult.setFnWorkflowStatus("Failed to get a VWRoster");
//			}
//		}
//		catch(VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			fnWorkflowResult = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getWorkflow()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflowResult;
//	}
//
//	private VWQueue getQueue(VWSession vwSession, String queueName, WIISCLog wiiscLog)
//	{
//		VWQueue vwQueue = null;
//		try
//		{
//			//wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getQueue()");
//			vwQueue = vwSession.getQueue(queueName);
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			vwQueue = null;
//		}
//		//wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getQueue()");
//		return vwQueue;
//	}
//
//	private VWRoster getRoster(VWSession vwSession, String rosterName, WIISCLog wiiscLog)
//	{
//		VWRoster vwRoster = null;
//		try
//		{
//			//wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getRoster()");
//			vwRoster = vwSession.getRoster(rosterName);
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			vwRoster = null;
//		}
//		//wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getRoster()");
//		return vwRoster;
//	}
//
//	private FnWorkflowList getFnWorkflowListByRoster(VWSession vwSession, String process, String[] filterName, String[] filterValue, WIISCLog wiiscLog)
//	{
//		//Create the FnWorkflowList Object
//		FnWorkflowList fnWorkflowList = new FnWorkflowList();
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getFnWorkflowListByRoster()");
//			VWRoster vwRoster = getRoster(vwSession, process, wiiscLog);
//			//Verify the Roster was OK
//			if (vwRoster != null)
//			{
//				//Get the Roster Count
//				int rosterCount = vwRoster.fetchCount();
//				wiiscLog.log(wiiscLog.INFO, "Roster Count: " + rosterCount);
//				//Update the Workflow List
//				fnWorkflowList.setCount(rosterCount);
//				fnWorkflowList.setProcessName(process);
//				
//				//Check the Roster Count to make sure it is > 0, otherwise skip.
//				if (rosterCount > 0)
//				{
//					//Get the Roster Elements
//					VWRosterQuery query = getRosterQuery(vwRoster, filterName, filterValue, wiiscLog);
//					//Verify the Query was OK
//					if (query != null && query.hasNext())
//					{
//						wiiscLog.log(wiiscLog.INFO, "Query Not Null");
//						//Process the Results
//						while(query.hasNext())
//						{
//							wiiscLog.log(wiiscLog.INFO, "Query has next");
//							VWRosterElement rosterItem = (VWRosterElement) query.next();
//							VWStepElement stepElement = rosterItem.fetchStepElement(false, false);
//							if (stepElement != null)
//							{
//								//Update the FnWorkflow
//								FnWorkflow fnWorkflow = new FnWorkflow();
//								fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing", wiiscLog);
//
//								//Create the FnWorkflowPropertyList
//								FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
//								fnWorkflowPropertyList = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//
//								//Add the FnWorkflowPropertyList to the FnWorkflow
//								fnWorkflow.setFnWorkflowPropertyList(fnWorkflowPropertyList);
//								//Update the Workflow List for the Process
//								fnWorkflowList.setStepName(fnWorkflow.getFnWorkflowProcess());
//								
//								//Check if Clarety Workflow Queue Description Actions are used
//								//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//								if (globalConfig.containsKey("claretyWorkflowDescriptionActionsEnabled"))
//								{
//									//Check if the Property setting is true
//									if (globalConfig.getString("claretyWorkflowDescriptionActionsEnabled").equals("true"))
//									{
//										//Initialize the Queue Name - this is needed since we are doing
//										// a Roster query
//										VWQueue vwQueue = null;
//										String queue = "";
//										queue = stepElement.getQueueName();
//										//Initialize the Description
//										String description = "";
//										
//										//Check if we are using the Inbox Queue or Not
//										if (queue.equals("Inbox(0)"))
//										{
//											//Get the Workflow Queue not the Inbox
//											if (fnWorkflow.getFnWorkflowQueue() != null)
//											{
//												vwQueue = getQueue(vwSession, fnWorkflow.getFnWorkflowQueue(), wiiscLog);
//												//Get the Workflow Queue Description
//												VWQueueDefinition def = vwQueue.fetchQueueDefinition();
//												VWOperationDefinition op = def.getOperation(globalConfig.getString("claretyWorkflowDescriptionOperation"));
//												description = op.getDescription();
//											}
//										}
//										else
//										{
//											//Get the Workflow Queue
//											if (fnWorkflow.getFnWorkflowQueue() != null)
//											{
//												vwQueue = getQueue(vwSession, fnWorkflow.getFnWorkflowQueue(), wiiscLog);
//												//Get the Workflow Queue Description
//												VWQueueDefinition def = vwQueue.fetchQueueDefinition();
//												VWOperationDefinition op = def.getOperation(globalConfig.getString("claretyWorkflowDescriptionOperation"));
//												description = op.getDescription();
//											}
//										}
//										
//										//Check to make sure the Description is not empty
//										if (description.length() > 0)
//										{
//											wiiscLog.log(wiiscLog.INFO, "Workflow Description: ");
//											wiiscLog.log(wiiscLog.INFO, description);
//											fnWorkflow.setFnWorkflowResponse(description);
//										}
//									}
//								}
//															
//								//Add the FnWorkflow to the FnWorkflowList
//								fnWorkflowList.addFnWorkflow(fnWorkflow);
//							}
//							else
//							{
//								wiiscLog.log(wiiscLog.INFO, "Failed to get a StepElement because the Workflow is no longer available");
//								//Update the FnWorkflow Object
//								fnWorkflowList.setErrorFlag(1);
//								fnWorkflowList.setErrorMessage("Failed to get a StepElement because the Workflow is no longer available");
//							}
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "Failed to get a RosterQuery because " + process + " has 0 Records");
//						//Update the FnWorkflowList Object
//						fnWorkflowList.setErrorFlag(1);
//						fnWorkflowList.setErrorMessage("Failed to get a RosterQuery because " + process + " has 0 Records");
//					}
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "Roster has No Records so a query will not be performed");
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Failed to get a VWRoster because " + process + " does not exist");
//				//Update the FnWorkflowList Object
//				fnWorkflowList.setErrorFlag(1);
//				fnWorkflowList.setErrorMessage("Failed to get a VWRoster because " + process + " does not exist");
//			}
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update the FnWorkflowList Object
//			fnWorkflowList.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowList.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getFnWorkflowListByRoster()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflowList;
//	}
//
//	private String getFnWorkflowPropertyValue(FnWorkflow fnWorkflow, String propName, WIISCLog wiiscLog)
//	{
//		String propValue = "";
//		//Create the FnWorkflowPropertyList from the Request
//		FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getFnWorkflowPropertyValue()");
//			wiiscLog.log(wiiscLog.INFO, "Property Name: " + propName);
//
//			fnWorkflowPropertyList = fnWorkflow.getFnWorkflowPropertyList();
//			//Verify there was a Workflow Property
//			if (fnWorkflowPropertyList != null)
//			{
//				List<FnWorkflowProperty> fnWorkflowProps = fnWorkflowPropertyList.getFnWorkflowPropsList();
//				for (int i = 0; i < fnWorkflowProps.size(); i++)
//				{
//					FnWorkflowProperty fnWorkflowProperty = fnWorkflowProps.get(i);
//					if (fnWorkflowProperty != null)
//					{
//						//Check if the FnWorkflowProperty Name matches the propName passed in
//						if (fnWorkflowProperty.getName().equals(propName))
//						{
//							propValue = fnWorkflowProperty.getValue();
//							wiiscLog.log(wiiscLog.INFO, "Property Value: " + propValue);
//							break;
//						}
//					}
//
//				}
//			}
//		}
//		catch (Exception ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			propValue = "";
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getFnWorkflowPropertyValue()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//
//		return propValue;
//	}
//
//	private FnWorkflow getFnWorkflowByQueue(VWSession vwSession, String queue, String[] filterName, String[] filterValue, WIISCLog wiiscLog)
//	{
//		//Create the FnWorkflow Object
//		FnWorkflow fnWorkflow = new FnWorkflow();
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getFnWorkflowByQueue()");
//			wiiscLog.log(wiiscLog.INFO, "Queue: " + queue);
//			VWQueue vwQueue = getQueue(vwSession, queue, wiiscLog);
//			//Workflow Search Results Limit
//			String workflowSearchLimit = "";
//			int workflowSearchMax = -1;
//			int workflowResultCount = 0;
//			
//			//Verify the Queue was OK
//			if (vwQueue != null)
//			{
//				//Get the Workflow Search Limit
//				workflowSearchLimit = globalConfig.getString("workflowSearchLimit");
//				if (workflowSearchLimit.length() > 0)
//				{
//					workflowSearchMax = Integer.parseInt(workflowSearchLimit);
//				}
//				
//				//Get the Queue Elements
//				VWQueueQuery query = null;
//				if (queue.equals("Inbox(0)"))
//				{
//					//Different Query to get a User's Inbox Items
//					query = getQueueQuery(vwQueue, null, null, "", wiiscLog);
//				}
//				else
//				{
//					query = getQueueQuery(vwQueue, filterName, filterValue, "", wiiscLog);
//				}
//				
//				//Verify the Query was OK
//				if (query != null && query.hasNext())
//				{
//					wiiscLog.log(wiiscLog.INFO, "Query Not Null");
//					//Process the Results
//					while(query.hasNext())
//					{
//						//Check if the workflowSearchMax limit has been reached
//						if (workflowResultCount == workflowSearchMax)
//						{
//							wiiscLog.log(wiiscLog.INFO, workflowSearchMax + " Query Results reached");
//							break;
//						}
//						wiiscLog.log(wiiscLog.INFO, "Query Has Next");
//						VWQueueElement queueItem = (VWQueueElement) query.next();
//						VWStepElement stepElement = queueItem.fetchStepElement(false, false);
//						if (stepElement != null)
//						{
//							//Check for Inbox Queue
//							if (queue.equals("Inbox(0)"))
//							{
//								VWParticipant vwParticipant = stepElement.getParticipantNamePx();
//								wiiscLog.log(wiiscLog.INFO, "Participant: " + vwParticipant.getParticipantName());
//								//Compare with the User to see if this is their Workflow
//								if (filterValue[0].equals(vwParticipant.getParticipantName()))
//								{
//									//Update the FnWorkflow
//									fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing", wiiscLog);
//
//									//Create the FnWorkflowPropertyList
//									FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
//									fnWorkflowPropertyList = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//									//Add the FnWorkflowPropertyList to the FnWorkflow
//									fnWorkflow.setFnWorkflowPropertyList(fnWorkflowPropertyList);
//								}
//							}
//							else
//							{
//								//Update the FnWorkflow
//								fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing", wiiscLog);
//
//								//Create the FnWorkflowPropertyList
//								FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
//								fnWorkflowPropertyList = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//								//Add the FnWorkflowPropertyList to the FnWorkflow
//								fnWorkflow.setFnWorkflowPropertyList(fnWorkflowPropertyList);
//							}
//
//							//Check if Clarety Workflow Queue Description Actions are used
//							//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//							if (globalConfig.containsKey("claretyWorkflowDescriptionActionsEnabled"))
//							{
//								//Check if the Property setting is true
//								if (globalConfig.getString("claretyWorkflowDescriptionActionsEnabled").equals("true"))
//								{
//									//Initialize the Description
//									String description = "";
//									//Check if we are using the Inbox Queue or Not
//									if (queue.equals("Inbox(0)"))
//									{
//										//Get the Workflow Queue not the Inbox
//										if (fnWorkflow.getFnWorkflowQueue() != null)
//										{
//											vwQueue = getQueue(vwSession, fnWorkflow.getFnWorkflowQueue(), wiiscLog);
//											//Get the Workflow Queue Description
//											VWQueueDefinition def = vwQueue.fetchQueueDefinition();
//											VWOperationDefinition op = def.getOperation(globalConfig.getString("claretyWorkflowDescriptionOperation"));
//											description = op.getDescription();
//										}
//									}
//									else
//									{
//										//Get the Workflow Queue Description
//										VWQueueDefinition def = vwQueue.fetchQueueDefinition();
//										VWOperationDefinition op = def.getOperation(globalConfig.getString("claretyWorkflowDescriptionOperation"));
//										description = op.getDescription();
//									}
//									
//									//Check to make sure the Description is not empty
//									if (description.length() > 0)
//									{
//										wiiscLog.log(wiiscLog.INFO, "Workflow Description: ");
//										wiiscLog.log(wiiscLog.INFO, description);
//										fnWorkflow.setFnWorkflowResponse(description);
//									}
//								}
//							}
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "Failed to get a StepElement");
//							//Update the FnWorkflow Object
//							//fnWorkflow.setErrorFlag(1);
//						}
//						//Increment the workflowResultCount
//						workflowResultCount++;
//					}//End While
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "Failed to get a QueueQuery");
//					//Update the FnWorkflow Object
//					//fnWorkflow.setErrorFlag(1);
//					fnWorkflow = null;
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Failed to get a VWQueue");
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(1);
//				//Update ErrorMessage
//				fnWorkflow.setErrorMessage("Failed to get a VWQueue");
//			}
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			//Update the FnWorkflow Object
//			fnWorkflow.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflow.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getFnWorkflowByQueue()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflow;
//	}
//
//
//	private FnWorkflowList getFnWorkflowListByQueue(SQLServerConnection con, VWSession vwSession, String queue, String[] filterName, String[] filterValue, String sortBy, WIISCLog wiiscLog)
//	{
//		//Create the FnWorkflowList Object
//		FnWorkflowList fnWorkflowList = new FnWorkflowList();
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getFnWorkflowListByQueue()");
//			wiiscLog.log(wiiscLog.INFO, "Queue: " + queue);
//			VWQueue vwQueue = getQueue(vwSession, queue, wiiscLog);
//			//Workflow Search Results Limit
//			String workflowSearchLimit = "";
//			int workflowSearchMax = -1;
//			int workflowResultCount = 0;
//			
//			//Verify the Queue was OK
//			if (vwQueue != null)
//			{
//				//Get the Queue Count
//				int queueCount = vwQueue.fetchCount();
//				wiiscLog.log(wiiscLog.INFO, "Queue Count: " + queueCount);
//				//Update Queue Name on FnWorkflowList
//				fnWorkflowList.setStepName(queue);
//				//FnWorkflowList from DB Query
//				FnWorkflowList tempFnWorkflowList = new FnWorkflowList();
//				//Get the Workflow Search Limit
//				workflowSearchLimit = globalConfig.getString("workflowSearchLimit");
//				if (workflowSearchLimit.length() > 0)
//				{
//					workflowSearchMax = Integer.parseInt(workflowSearchLimit);
//				}
//				
//				//Check the Queue Count to make sure it is > 0, otherwise skip.
//				if (queueCount > 0)
//				{
//					//Get the Queue Elements
//					VWQueueQuery query = null;
//					if (queue.equals("Inbox(0)"))
//					{
//						if (globalConfig.getString("workflowDBSearchEnabled").equals("true"))
//						{
//							//DB FileNet Query for Workflows
//							tempFnWorkflowList = getQueueDBQuery(con, vwQueue, queue, filterName, filterValue, wiiscLog);
//						}
//						else
//						{
//							//FileNet Query
//							//Different Query to get a User's Inbox Items
//							query = getQueueQuery(vwQueue, null, null, sortBy, wiiscLog);
//						}
//					}
//					else
//					{
//						if (globalConfig.getString("workflowDBSearchEnabled").equals("true"))
//						{
//							//DB FileNet Query for Workflows
//							tempFnWorkflowList = getQueueDBQuery(con, vwQueue, queue, filterName, filterValue, wiiscLog);
//						}
//						else
//						{
//							//FileNet Query
//							query = getQueueQuery(vwQueue, filterName, filterValue, sortBy, wiiscLog);
//						}
//					}
//					
//					//Check to see if the Query or the FnWorkflowList was OK
//					if (query != null && query.hasNext())
//					{
//						wiiscLog.log(wiiscLog.INFO, "Query Not Null");
//						//Process the Results
//						while(query.hasNext())
//						{
//							//Check if the workflowSearchMax limit has been reached
//							if (workflowResultCount == workflowSearchMax)
//							{
//								wiiscLog.log(wiiscLog.INFO, workflowSearchMax + " Query Results reached");
//								break;
//							}
//							wiiscLog.log(wiiscLog.INFO, "Query has next");
//							VWQueueElement queueItem = (VWQueueElement) query.next();
//							VWStepElement stepElement = queueItem.fetchStepElement(false, false);
//							if (stepElement != null)
//							{
//								wiiscLog.log(wiiscLog.INFO, "Queue Query Total: " + query.fetchCount());
//								//Update Count for the Queue on FnWorkflowList
//								//fnWorkflowList.setCount(query.fetchCount());
//								//Update the FnWorkflow
//								FnWorkflow fnWorkflow = new FnWorkflow();
//								//Check for Inbox Queue
//								if (queue.equals("Inbox(0)"))
//								{
//									VWParticipant vwParticipant = stepElement.getParticipantNamePx();
//									wiiscLog.log(wiiscLog.INFO, "Participant: " + vwParticipant.getParticipantName());
//									//Compare with the User to see if this is their Workflow
//									if (filterValue[0].equals(vwParticipant.getParticipantName()))
//									{
//										//Update the FnWorkflow
//										//FnWorkflow fnWorkflow = new FnWorkflow();
//										fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing", wiiscLog);
//
//										//Create the FnWorkflowPropertyList
//										FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
//										fnWorkflowPropertyList = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//										//Add the FnWorkflowPropertyList to the FnWorkflow
//										fnWorkflow.setFnWorkflowPropertyList(fnWorkflowPropertyList);
//										//Update the Workflow List for the Activity/Step
//										fnWorkflowList.setStepName(fnWorkflow.getFnWorkflowStep());
//										//Add the FnWorkflow to the FnWorkflowList
//										//fnWorkflowList.addFnWorkflow(fnWorkflow);
//									}
//								}
//								else
//								{
//									//Update the FnWorkflow
//									//FnWorkflow fnWorkflow = new FnWorkflow();
//									fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing", wiiscLog);
//
//									//Create the FnWorkflowPropertyList
//									FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
//									fnWorkflowPropertyList = updateFnWorkflowPropertyListInfo(stepElement, wiiscLog);
//									//Add the FnWorkflowPropertyList to the FnWorkflow
//									fnWorkflow.setFnWorkflowPropertyList(fnWorkflowPropertyList);
//									//Update the Workflow List for the Activity/Step
//									fnWorkflowList.setStepName(fnWorkflow.getFnWorkflowStep());
//									//Add the FnWorkflow to the FnWorkflowList
//									//fnWorkflowList.addFnWorkflow(fnWorkflow);
//								}
//								
//								//Check if Clarety Workflow Queue Description Actions are used
//								//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//								if (globalConfig.containsKey("claretyWorkflowDescriptionActionsEnabled"))
//								{
//									//Check if the Property setting is true
//									if (globalConfig.getString("claretyWorkflowDescriptionActionsEnabled").equals("true"))
//									{
//										//Initialize the Description
//										String description = "";
//										//Check if we are using the Inbox Queue or Not
//										if (queue.equals("Inbox(0)"))
//										{
//											//Get the Workflow Queue not the Inbox
//											if (fnWorkflow.getFnWorkflowQueue() != null)
//											{
//												vwQueue = getQueue(vwSession, fnWorkflow.getFnWorkflowQueue(), wiiscLog);
//												//Get the Workflow Queue Description
//												VWQueueDefinition def = vwQueue.fetchQueueDefinition();
//												VWOperationDefinition op = def.getOperation(globalConfig.getString("claretyWorkflowDescriptionOperation"));
//												description = op.getDescription();
//											}
//										}
//										else
//										{
//											//Get the Workflow Queue
//											if (fnWorkflow.getFnWorkflowQueue() != null)
//											{
//												vwQueue = getQueue(vwSession, fnWorkflow.getFnWorkflowQueue(), wiiscLog);
//												//Get the Workflow Queue Description
//												VWQueueDefinition def = vwQueue.fetchQueueDefinition();
//												VWOperationDefinition op = def.getOperation(globalConfig.getString("claretyWorkflowDescriptionOperation"));
//												description = op.getDescription();
//											}
//										}
//										
//										//Check to make sure the Description is not empty
//										if (description.length() > 0)
//										{
//											wiiscLog.log(wiiscLog.INFO, "Workflow Description: ");
//											wiiscLog.log(wiiscLog.INFO, description);
//											fnWorkflow.setFnWorkflowResponse(description);
//										}
//									}
//								}
//								//Add the FnWorkflow to the FnWorkflowList
//								fnWorkflowList.addFnWorkflow(fnWorkflow);
//								wiiscLog.log(wiiscLog.INFO, "Queue Count: " + fnWorkflowList.getCount());
//							}
//							else
//							{
//								wiiscLog.log(wiiscLog.INFO, "Failed to get a StepElement because the Workflow is no longer available");
//							}
//							//Increment the workflowResultCount
//							workflowResultCount++;
//						}//End While
//					}
//					else if (tempFnWorkflowList.getCount() > 0)
//					{
//						wiiscLog.log(wiiscLog.INFO, "Queue Query Total: " + tempFnWorkflowList.getCount());
//						//Update Count for the Queue on FnWorkflowList
//						//fnWorkflowList.setCount(tempFnWorkflowList.getCount());
//						
//						//Check if Clarety Workflow Queue Description Actions are used
//						//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//						if (globalConfig.containsKey("claretyWorkflowDescriptionActionsEnabled"))
//						{
//							//Check if the Property setting is true
//							if (globalConfig.getString("claretyWorkflowDescriptionActionsEnabled").equals("true"))
//							{
//								//FnWorkflow List
//								List<FnWorkflow> tempFnWorkflowListData = tempFnWorkflowList.getFnWorkflowList();
//								
//								for (int a = 0; a < tempFnWorkflowListData.size(); a++)
//								{
//									//Update FnWorkflow
//									FnWorkflow fnWorkflow = tempFnWorkflowListData.get(a);
//									
//									//Initialize the Description
//									String description = "";
//									//Check if we are using the Inbox Queue or Not
//									if (queue.equals("Inbox(0)"))
//									{
//										//Get the Workflow Queue not the Inbox
//										if (fnWorkflow.getFnWorkflowQueue() != null)
//										{
//											vwQueue = getQueue(vwSession, fnWorkflow.getFnWorkflowQueue(), wiiscLog);
//											//Get the Workflow Queue Description
//											VWQueueDefinition def = vwQueue.fetchQueueDefinition();
//											VWOperationDefinition op = def.getOperation(globalConfig.getString("claretyWorkflowDescriptionOperation"));
//											description = op.getDescription();
//										}
//									}
//									else
//									{
//										//Get the Workflow Queue
//										if (fnWorkflow.getFnWorkflowQueue() != null)
//										{
//											vwQueue = getQueue(vwSession, fnWorkflow.getFnWorkflowQueue(), wiiscLog);
//											//Get the Workflow Queue Description
//											VWQueueDefinition def = vwQueue.fetchQueueDefinition();
//											VWOperationDefinition op = def.getOperation(globalConfig.getString("claretyWorkflowDescriptionOperation"));
//											description = op.getDescription();
//										}
//									}
//									
//									//Check to make sure the Description is not empty
//									if (description.length() > 0)
//									{
//										wiiscLog.log(wiiscLog.INFO, "Workflow Description: ");
//										wiiscLog.log(wiiscLog.INFO, description);
//										fnWorkflow.setFnWorkflowResponse(description);
//									}
//								}
//							}
//						}
//						//Add the tempFnWorkflowList to the FnWorkflowList
//						fnWorkflowList.addFnWorkflowList(tempFnWorkflowList);
//						wiiscLog.log(wiiscLog.INFO, "Queue Count: " + fnWorkflowList.getCount());
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "Failed to get a QueueQuery because " + queue + " has 0 Records");
//					}
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "Queue has No Records so a query will not be performed");
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Failed to get a VWQueue because " + queue + " does not exist");
//				//Update the FnWorkflowList Object
//				fnWorkflowList.setErrorFlag(1);
//				//Update ErrorMessage
//				fnWorkflowList.setErrorMessage("Failed to get a VWQueue because " + queue + " does not exist");
//			}
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update the FnWorkflowList Object
//			fnWorkflowList.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowList.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getFnWorkflowListByQueue()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflowList;
//	}
//
//	private FnWorkflowList getFnWorkflowCountsListByQueue(VWSession vwSession, String queue, String[] filterName, String[] filterValue, String sortBy, WIISCLog wiiscLog)
//	{
//		//Create the FnWorkflowList Object
//		FnWorkflowList fnWorkflowList = new FnWorkflowList();
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> getFnWorkflowCountsListByQueue()");
//			wiiscLog.log(wiiscLog.INFO, "Queue: " + queue);
//			VWQueue vwQueue = getQueue(vwSession, queue, wiiscLog);
//			//Verify the Queue was OK
//			if (vwQueue != null)
//			{
//				//Get the Queue Count
//				int queueCount = vwQueue.fetchCount();
//				wiiscLog.log(wiiscLog.INFO, "Queue Total: " + queueCount);
//				//Update Queue Name on FnWorkflowList
//				fnWorkflowList.setStepName(queue);
//												
//				//Check the Queue Count to make sure it is > 0, otherwise skip.
//				if (queueCount > 0)
//				{
//					//Get the Queue Elements
//					VWQueueQuery query = null;
//					if (queue.equals("Inbox(0)"))
//					{
//						//FileNet Query
//						//Different Query to get a User's Inbox Items
//						query = getQueueQuery(vwQueue, null, null, sortBy, wiiscLog);
//					}
//					else
//					{
//						//FileNet Query
//						query = getQueueQuery(vwQueue, filterName, filterValue, sortBy, wiiscLog);
//					}
//					
//					//Check to see if the Query or the FnWorkflowList was OK
//					if (query != null)
//					{
//						wiiscLog.log(wiiscLog.INFO, "Query Not Null");
//						wiiscLog.log(wiiscLog.INFO, "Queue Query Total: " + query.fetchCount());
//						//Update Count for the Queue on FnWorkflowList
//						//fnWorkflowList.setCount(query.fetchCount());
//						int queryCount = query.fetchCount();
//						for (int a = 0; a < queryCount; a++)
//						{
//							FnWorkflow fnWorkflow = new FnWorkflow();
//							//Add FnWorkflow to the List
//							fnWorkflowList.addFnWorkflow(fnWorkflow);
//						}
//					}					
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "Failed to get a QueueQuery");
//					}
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "Queue has No Records so a query will not be performed");
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Failed to get a VWQueue");
//				//Update the FnWorkflowList Object
//				fnWorkflowList.setErrorFlag(1);
//				fnWorkflowList.setErrorMessage("Failed to get a VWQueue");
//			}
//		}
//		catch (VWException ex)
//		{
//			wiiscLog.log("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			//Update the FnWorkflowList Object
//			fnWorkflowList.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowList.setErrorMessage(ex.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> getFnWorkflowCountsListByQueue()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnWorkflowList;
//	}
//	
//	private SQLServerConnection loginWorkflowDB(WIISCLog wiiscLog)
//	{
//		SQLServerConnection con = null;
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> loginWorkflowDB()");
//			//Workflow DB Server
//			String workflowDBServerName = "";
//			//Workflow DB Server Port
//			String workflowDBServerPort = "";
//			//Workflow DB Name
//			String workflowDBName = "";
//			//Workflow DB User
//			String workflowDBUser = "";
//			//Workflow DB User's Password
//			String workflowDBUserPassword = "";
//			
//			//Get all of the Workflow Information
//			workflowDBServerName = globalConfig.getString("workflowDBServerName");
//			workflowDBServerPort = globalConfig.getString("workflowDBServerPort");
//			workflowDBName = globalConfig.getString("workflowDBName");
//			workflowDBUser = globalConfig.getString("workflowDBUser");
//			workflowDBUserPassword = globalConfig.getString("workflowDBUserPassword");
//			
//			wiiscLog.log(wiiscLog.INFO, "Workflow DB Server: " + workflowDBServerName);
//			wiiscLog.log(wiiscLog.INFO, "Workflow DB Server Port: " + workflowDBServerPort);
//			wiiscLog.log(wiiscLog.INFO, "Workflow DB Name: " + workflowDBName);
//			wiiscLog.log(wiiscLog.INFO, "Workflow DB User: " + workflowDBUser);
//			
//			// Establish the connection. 
//			SQLServerDataSource ds = new SQLServerDataSource();
//			ds.setUser(workflowDBUser);
//			ds.setPassword(workflowDBUserPassword);
//			ds.setServerName(workflowDBServerName);
//			ds.setPortNumber(Integer.parseInt(workflowDBServerPort)); 
//			ds.setDatabaseName(workflowDBName);
//			con = (SQLServerConnection) ds.getConnection();
//			
//			//Verify the Connection was successful
//			if (con != null)
//			{
//				wiiscLog.log(wiiscLog.INFO, "Workflow DB Connection was Successful");
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Workflow DB Connection Failed");
//			}
//			
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log("ERROR", e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> loginWorkflowDB()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return con;
//	}
//	

//
//	//Output the FnWorkflowPropertyList
//	private void outputFnWorkflowPropertyList(FnWorkflowPropertyList fnWorkflowPropertyList, WIISCLog wiiscLog)
//	{
//		wiiscLog.log(wiiscLog.INFO, "outputFnWorkflowPropertyList");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		int propSize;
//		propSize = fnWorkflowPropertyList.getFnWorkflowPropsList().size();
//		for (int i = 0; i < propSize; i++)
//		{
//			FnWorkflowProperty fnWorkflowProperty = fnWorkflowPropertyList.getFnWorkflowPropsList().get(i);
//			wiiscLog.log(wiiscLog.INFO, "Property: " + fnWorkflowProperty.getName());
//			wiiscLog.log(wiiscLog.INFO, "Value: " + fnWorkflowProperty.getValue());
//		}
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//	}
//	
//	public String createCase(String appId, String planId, String caseId, String process, WIISCLog wiiscLog)
//	{
//		String result = "Fail";
//		String response = null;
//		String caseType = "";
//		String caseFolderId = "";
//		String caseTitle = "";
//		String caseIdentifier = "";
//		int COMMENT_TYPE_CASE = 102;
//
//		//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//
//		System.out.println("Entered WorkflowManager -> createCase()");
//
//		wiiscLog.log(wiiscLog.INFO, "Entered WorkflowManager -> createCase()");
//
//		/* create case */
//		String caseURL = "http://" + globalConfig.getString("ceServerName") + ":" + globalConfig.getString("ceApplicationPort") + "/CaseManager/CASEREST/v1/cases"; 
//		System.out.println("CASE URL: " + caseURL);
//
//		wiiscLog.log(wiiscLog.INFO, "caseURL: " + caseURL);
//
//		JSONObject createCasePayload = new JSONObject();
//		try
//		{
//			JSONArray ary = new JSONArray();
//
//			//Determine the Case Type from the passed in Process
//			String processNameToCaseType = globalConfig.getString("ProcessNameToCaseTypeMapping");
//			String[] processNameToCaseTypeData = null;
//			//Split the ProcessNameToCaseTypeMapping list to determine which one to process
//			processNameToCaseTypeData = processNameToCaseType.split(",");
//			//Check for the Process to find the correct CaseType
//			for (int a = 0; a < processNameToCaseTypeData.length; a++)
//			{
//				if (processNameToCaseTypeData[a].contains(process))
//				{
//					String[] foundData = null;
//					foundData = processNameToCaseTypeData[a].split("~");
//					for (int b = 0; b < foundData.length; b++)
//					{
//						if (!foundData[b].contains(process))
//						{
//							//Found Case Type name
//							caseType = foundData[b];
//							break;
//						}
//					}
//					break;
//				}
//			}
//
//			//Set the Case Type
//			createCasePayload.put("CaseType", caseType);
//			//Set the Object Store
//			createCasePayload.put("TargetObjectStore", globalConfig.getString("CaseManagerTargetObjectStore"));
//			//Set the Case Return Updates
//			//createCasePayload.put("ReturnUpdates", globalConfig.getString("CaseManagerReturnUpdates"));
//
//
//			/* add required properties here (if any)  
//	           for example: */
//			/*
//	        JSONObject prop = new JSONObject();
//	        prop.put("SymbolicName", yourRequiredPropertyName); 
//	        prop.put("Value", yourPropertyValue);
//	        ary.add(prop);
//	        ...
//	        ...
//			 */
//			//JSONObject for Properties
//			JSONObject prop = new JSONObject();
//			//CaseManagerSolutionPrefix
//			//Person ID
//			prop.put("SymbolicName", globalConfig.getString("CaseManagerSolutionPrefix") + "PersonID");
//			prop.put("DisplayName", "Person ID");
//			prop.put("Value", appId);      
//			ary.put(prop);
//			//Plan ID
//			prop = new JSONObject();
//			prop.put("SymbolicName", globalConfig.getString("CaseManagerSolutionPrefix") + "PlanID");
//			prop.put("DisplayName", "Plan ID");
//			prop.put("Value", planId);      
//			ary.put(prop);
//			//Case ID
//			prop = new JSONObject();
//			prop.put("SymbolicName", globalConfig.getString("CaseManagerSolutionPrefix") + "ApplicationCaseID");
//			prop.put("DisplayName", "Application Case ID");
//			prop.put("Value", caseId);      
//			ary.put(prop);
//			//Process
//			prop = new JSONObject();
//			prop.put("SymbolicName", globalConfig.getString("CaseManagerSolutionPrefix") + "ProcessCode");
//			prop.put("DisplayName", "Process Code");
//			prop.put("Value", process);      
//			ary.put(prop);
//
//			createCasePayload.put("Properties", ary);
//			response = executeCaseRESTAPI(caseURL, "POST", createCasePayload.toString());
//			System.out.println("Response Results: " + response);
//		}
//		catch (Exception e)
//		{
//			System.out.println("Application error - creating Case, terminates abnormally!");
//			e.printStackTrace();
//			result = "Fail";
//			//System.exit(1);
//		}
//
//		//Check the response content
//		if (response.length() > 0)
//		{
//			try
//			{
//				//Output to the Log the Results
//				wiiscLog.log(wiiscLog.INFO, "==========================================");
//				wiiscLog.log(wiiscLog.INFO, "appId: " + appId);
//				wiiscLog.log(wiiscLog.INFO, "planId: " + planId);
//				wiiscLog.log(wiiscLog.INFO, "caseId: " + caseId);
//				wiiscLog.log(wiiscLog.INFO, "process: " + process);
//				wiiscLog.log(wiiscLog.INFO, "==========================================");
//				wiiscLog.log(wiiscLog.INFO, "==		Case Results -> Success	    ==");
//				wiiscLog.log(wiiscLog.INFO, "==========================================");
//
//
//				//Get the Results
//				JSONObject contentObj = new JSONObject(response);
//				caseTitle = (String) contentObj.get("CaseTitle");
//				caseIdentifier = (String) contentObj.get("CaseIdentifier");
//				caseFolderId = (String) contentObj.get("CaseFolderId");
//
//				wiiscLog.log(wiiscLog.INFO, "CaseTitle: " + caseTitle);
//				wiiscLog.log(wiiscLog.INFO, "CaseIdentifier: " + caseIdentifier);
//				wiiscLog.log(wiiscLog.INFO, "CaseFolderId: " + caseFolderId);
//				wiiscLog.log(wiiscLog.INFO, "==========================================");
//
//				//Update the Result to Success for creating the Case and Workflow successfully
//				result = "Success";
//			}
//			catch (Exception e)
//			{
//				System.out.println("Application error - Checking Response Content, terminates abnormally!");
//				e.printStackTrace();
//				result = "Fail";
//				//System.exit(1);
//			}
//		}
//		else
//		{
//			//Output to the Log the Results
//			wiiscLog.log(wiiscLog.INFO, "==========================================");
//			wiiscLog.log(wiiscLog.INFO, "appId: " + appId);
//			wiiscLog.log(wiiscLog.INFO, "planId: " + planId);
//			wiiscLog.log(wiiscLog.INFO, "caseId: " + caseId);
//			wiiscLog.log(wiiscLog.INFO, "process: " + process);
//			wiiscLog.log(wiiscLog.INFO, "==========================================");
//			wiiscLog.log(wiiscLog.INFO, "==		Case Results -> Fail	    ==");
//			wiiscLog.log(wiiscLog.INFO, "==========================================");
//
//			//Response Content was empty
//			result = "Fail";
//		}
//
//
//		//Update the Result to Success for creating the Case and Workflow successfully
//		//result = "Success";
//
//		/* get case folder id for future use */
//		try 
//		{
//			JSONObject contentObj = new JSONObject(response);
//			caseFolderId = (String) contentObj.get("CaseFolderId");
//		}
//		catch (Exception e)
//		{
//			System.out.println("Application error - getting Case Folder ID, terminates abnormally!");
//			e.printStackTrace();
//			result = "Fail";
//			//System.exit(1);
//		}
//
//		//Clean up the Case Folder ID
//		caseFolderId = removeGUIDBrackets(caseFolderId);
//
//		// create comment 1 
//		String createComment1 = "http://" + globalConfig.getString("ceServerName") + ":" + globalConfig.getString("ceApplicationPort") + 
//				"/CaseManager/CASEREST/v1/case/" + caseFolderId + "/comments?TargetObjectStore=" + 
//				globalConfig.getString("CaseManagerTargetObjectStore");
//
//		JSONObject createComment1Payload = new JSONObject();
//
//		try
//		{
//			createComment1Payload.put("CommentType", "Case");
//			createComment1Payload.put("CommentContext", COMMENT_TYPE_CASE);
//			createComment1Payload.put("CommentText", "Initiated Case by DocServices Application");
//			executeCaseRESTAPI(createComment1, "POST", createComment1Payload.toString());
//		}
//		catch (Exception e)
//		{
//			System.out.println("Application error - creating Case Comment, terminates abnormally!");
//			e.printStackTrace();
//			result = "Fail";
//			//System.exit(1);
//		}
//
//		// create comment 2 
//		/*String createComment2 = "http://" + CMserverInfo + "/CaseManager/CASEREST/v1/case/" +
//	                                caseFolderId + "/comments?TargetObjectStore=" + TOS;
//	    JSONObject createComment2Payload = new JSONObject();
//
//	    try
//	    {
//	        createComment2Payload.put("CommentType", "Case");
//
//	        // Constant 102 is used when CommentType is 'Case' 
//	        createComment2Payload.put("CommentContext", 102);
//	        createComment2Payload.put("CommentText", "This is the second comment!!");
//	        executeCaseRESTAPI(createComment2, "POST", createComment2Payload.write());
//	    }
//	    catch (Exception e)
//	    {
//	        System.out.println("Application encountered error, terminates abnormally!");
//	        e.printStackTrace();
//	        //System.exit(1);
//	    }*/
//
//		// get comments 
//		String getComment = "http://" + globalConfig.getString("ceServerName") + ":" + globalConfig.getString("ceApplicationPort") + 
//				"/CaseManager/CASEREST/v1/case/" + caseFolderId + "/comments?TargetObjectStore=" + 
//				globalConfig.getString("CaseManagerTargetObjectStore") + "&CommentType=Case";
//		executeCaseRESTAPI(getComment, "GET", null);
//
//		System.out.println("Leaving WorkflowManager -> createCase()");
//		wiiscLog.log(wiiscLog.INFO, "Leaving WorkflowManager -> createCase()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return result;
//	}
//
//	private LocalResource getLocalResource(String propsFile)
//	{
//		LocalResource res = null;
//		String wiiscConfigsPath = "";
//		wiiscConfigsPath = System.getProperty("wiisc.config.properties.path");
//		if (wiiscConfigsPath == null || wiiscConfigsPath.length() == 0)
//		{
//			System.out.println("WorkflowManager -> getLocalResource() cannot find the JVM Property wiisc.config.properties.path using WIISCConfig.properties file");
//			res = LocalResource.setResource(ResourceBundle.getBundle(ConstantsUtil.WIISC_CONFIG).getString("wiiscConfigFilesPath"), "\\" + propsFile);
//		}
//		else
//		{
//			//Assumes the JVM Properties Path includes /Configs e.g. C:/WIISC/<Env>/Configs
//			System.out.println("WorkflowManager -> getLocalResource() JVM Property wiisc.config.properties.path " + wiiscConfigsPath);
//			res = LocalResource.setResource(wiiscConfigsPath, "\\" + propsFile);
//		}
//		
//		return res;
//	}
//	
//	private String getImagingSession(ResourceBundle globalConfig)
//	{
//		String sessionId = "";
//		//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//		//Create CE User Session Token for viewing documents
//		Session session = ObjectFactory.getSession("UserToken", null, globalConfig.getString("ceUserId"), globalConfig.getString("cePassword"));
//		//Generate a Token URL with Session ID
//		sessionId = session.getToken(false);
//		sessionId = URLEncoder.encode(sessionId);
//
//		return sessionId;
//	}
//
//	private String removeGUIDBrackets(String src)
//	{
//		return src.substring(1, src.length()-1);
//	}
//
//	private String executeCaseRESTAPI(String resourceURI, String requestMethod, String content)
//	{
//		HttpURLConnection   httpConn;
//		URL                 url = null;
//		OutputStream        out = null;
//		InputStream         in = null;
//		int                 responseCode = 0;
//		String              responseContent= "";
//
//		//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//
//		try 
//		{
//			System.out.println(requestMethod + ": " + resourceURI);
//			if (content != null)
//			{
//				System.out.println("Payload: " + content);  
//			}
//
//			//Create Connection to CE
//			//String sessionId = getCESession();
//
//			/* set up http connection */
//			url = new URL(resourceURI);
//			httpConn = (HttpURLConnection) url.openConnection();
//
//			if (requestMethod.equalsIgnoreCase("POST")) 
//			{
//				httpConn.setRequestMethod("POST");
//				httpConn.setDoOutput(true);
//				httpConn.setRequestProperty("Content-Type", "application/json");
//			}
//			else if (requestMethod.equalsIgnoreCase("GET"))
//			{
//				httpConn.setRequestMethod("GET");
//			}
//			else if (requestMethod.equalsIgnoreCase("PUT"))
//			{
//				httpConn.setRequestMethod("PUT");
//				httpConn.setDoOutput(true);
//				httpConn.setRequestProperty("Content-Type", "application/json");
//			}
//
//			/* the protocol is NOT allowed to use caching */
//			httpConn.setUseCaches(false);
//
//			/* basic http web server authentication */
//			//String authStr = username + ":" + password;
//			String authStr = globalConfig.getString("ceUserId") + ":" + globalConfig.getString("cePassword");
//			//String encodedAuthStr = new String(Base64.encode(authStr.getBytes("UTF-8")));
//			String encodedAuthStr = new String(Base64.encodeBase64(authStr.getBytes("UTF-8")));
//			httpConn.setRequestProperty("Authorization", "Basic " + encodedAuthStr);
//
//			// Re-use Session
//			//httpConn.setRequestProperty("Cookie", "JSESSIONID="+sessionId);
//
//			/* caller has payload, write it to outstream */     
//			if (content != null)
//			{
//				out = httpConn.getOutputStream();
//				out.write(content.getBytes());
//				out.close();
//			}
//
//			/* response code from the server */
//			responseCode = httpConn.getResponseCode();
//			in = httpConn.getInputStream();
//
//			/* returned data (response content) from the server in JSON format */
//			responseContent = getResponse(in);
//			httpConn.disconnect();
//
//			if ((responseCode == 200) || (responseCode == 201))
//			{
//				System.out.println("RESPONSE: " + responseCode + " " + responseContent);
//			}
//			else
//			{
//				System.out.println("ERROR: " + responseCode + " " + responseContent);
//				System.out.println("Application error - bad response code, terminates abnormally!");
//				//System.exit(1);
//			}
//		}
//		catch (Exception e)
//		{
//			System.out.println("Application error - executeCaseRESTAPI " + responseCode
//					+ ", terminates abnormally!");
//
//			e.printStackTrace();
//			//System.exit(1);
//		}
//
//		return responseContent;
//	}
//
//	private PrintWriter getWriter(String name, WIISCLog wiiscLog)
//	{
//		PrintWriter out = null;
//		try
//		{
//			File file = new File(name);
//			out = new PrintWriter(
//					new BufferedWriter(
//							new FileWriter(file,false)));
//		}
//		catch (IOException e)
//		{
//			wiiscLog.log(wiiscLog.INFO, "I/O Error with the file " + name);
//		}
//		
//		return out;
//	}
//	
//	private String getResponse(InputStream in) 
//	{
//		if (in != null)
//		{
//			BufferedReader br = new BufferedReader(new InputStreamReader(in));
//			StringBuffer responseBuf = new StringBuffer();
//			String response = null;
//
//			try 
//			{
//				while ((response = br.readLine()) != null)
//				{
//					responseBuf.append(response);
//					responseBuf.append("\n");
//				}
//
//				br.close();
//			}
//			catch (IOException e)
//			{
//				e.printStackTrace();
//			}
//
//			return (responseBuf.length() > 0 ? responseBuf.toString() : null);
//		}
//		else
//		{
//			return null;
//		}
//	}

}



