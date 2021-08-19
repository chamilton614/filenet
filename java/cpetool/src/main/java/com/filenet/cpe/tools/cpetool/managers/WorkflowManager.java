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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.filenet.cpe.tools.cpetool.configuration.AppConfiguration;
import com.filenet.cpe.tools.cpetool.model.FnBase;
import com.filenet.cpe.tools.cpetool.model.FnWorkflow;
import com.filenet.cpe.tools.cpetool.model.FnWorkflowList;
import com.filenet.cpe.tools.cpetool.model.FnWorkflowProperty;
import com.filenet.cpe.tools.cpetool.model.FnWorkflowPropertyList;

//import org.apache.commons.codec.binary.Base64;

import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

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
	public void exportConfigXML()
	{
//		FnWorkflow fnWorkflow = new FnWorkflow();
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//		
//		try
//		{
//			log.info("Entered WorkflowManager -> exportConfigXML()");
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
//					log.info("Exported Workflow Config XML Successfully");
//					//Update the Workflow Status
//					fnWorkflow.setFnWorkflowStatus("Export Workflow Configuration XML SUCCESSFUL");
//				}
//				else
//				{
//					//workflowConfigXMLPath is empty
//					log.info("The Workflow Config XML Export Path was empty.");
//				}
//								
//				//Logoff the Workflow Server
//				log.info("Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				log.info("Logged off");
//			}
//			else
//			{
//				log.info("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(1);
//				fnWorkflow.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//				fnWorkflow.setFnWorkflowStatus("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Release the VWSession
//				vwSession = null;
//				log.info("Logged off");
//			}
//		}
//		catch(VWException ex)
//		{
//			log.info("ERROR", ex.getMessage());
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
//			log.info("ERROR", ex.getMessage());
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
//		log.info("Leaving WorkflowManager -> exportConfigXML()");
//		log.info("===========================================================");
//		return fnWorkflow;
	}
	
	//Import the Workflow Configuration XML
	public void importConfigXML()
	{
//		FnWorkflow fnWorkflow = new FnWorkflow();
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//		boolean regionInitialized = false;
//
//		try
//		{
//			log.info("Entered WorkflowManager -> importConfigXML()");
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
//						log.info("Region has already been initialized");
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
//						log.info(w);
//					}
//					//System.out.println("Imported Workflow Config XML Successfully with Merge Option");
//					log.info("Imported Workflow Config XML Successfully with " + importOption + " Option");
//					//Update the Workflow Status
//					fnWorkflow.setFnWorkflowStatus("Import Workflow Configuration XML SUCCESSFUL");
//				}
//				else
//				{
//					//workflowConfigXMLPath is empty
//					log.info("The Workflow Config XML Path was empty.");
//				}
//								
//				//Logoff the Workflow Server
//				log.info("Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				log.info("Logged off");
//			}
//			else
//			{
//				log.info("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(1);
//				fnWorkflow.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//				fnWorkflow.setFnWorkflowStatus("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Release the VWSession
//				vwSession = null;
//				log.info("Logged off");
//			}
//		}
//		catch(VWException ex)
//		{
//			log.info("ERROR", ex.getMessage());
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
//			log.info("ERROR", ex.getMessage());
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
//		log.info("Leaving WorkflowManager -> importConfigXML()");
//		log.info("===========================================================");
//		return fnWorkflow;
	}
	
	//Load the Workflow Maps from a defined directory in the Global Config Properties
	public void loadMaps()
	{
//		FnWorkflow fnWorkflow = new FnWorkflow();
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//		boolean regionInitialized = false;
//
//		try
//		{
//			log.info("Entered WorkflowManager -> loadMaps()");
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
//						log.info("Region has already been initialized");
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
//											log.info(mapFile.getName() + " Transferred Successfully");
//											//Update the Processed PEP File Count
//											pepFileProcessedCount++;
//										}
//										else
//										{
//											String[] errors = transferResult.getErrors();
//											log.info("ERRORS Transferring Workflow Map " + mapFile.getName());
//											//log.info(errors.toString());
//											for (String oneError : errors)
//											{
//												log.info(oneError);
//											}
//										}
//									}
//								}
//							}
//							log.info("===========================================================");
//							log.info("Transferred " + pepFileProcessedCount + " out of " + pepFileCount + " Workflow Maps");
//							
//							//Update the Workflow Status
//							fnWorkflow.setFnWorkflowStatus("Loading of the Workflow Maps was SUCCESSFUL");
//						}
//						else
//						{
//							//Workflow Maps Path is not a valid directory
//							log.info("Workflow Maps Path is not a valid directory");
//						}
//					}
//					else
//					{
//						//Region Not Initialized - Workflow Maps cannot be loaded
//						log.info("Workflow Region has not been initialized");
//					}
//				}
//				else
//				{
//					//workflowMapsPath is empty
//					log.info("The Workflow Maps Path was empty.");
//				}
//
//				//Logoff the Workflow Server
//				log.info("Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				log.info("Logged off");
//			}
//			else
//			{
//				log.info("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(1);
//				fnWorkflow.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//				fnWorkflow.setFnWorkflowStatus("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Release the VWSession
//				vwSession = null;
//				log.info("Logged off");
//			}
//		}
//		catch(VWException ex)
//		{
//			log.info("ERROR", ex.getMessage());
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
//			log.info("ERROR", ex.getMessage());
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
//			log.info("ERROR", ex.getMessage());
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
//		log.info("Leaving WorkflowManager -> loadMaps()");
//		log.info("===========================================================");
//		return fnWorkflow;
	}
	
	//Initialize the Workflow Region
	public void initializeRegion()
	{
//		FnWorkflow fnWorkflow = new FnWorkflow();
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//		
//		try
//		{
//			log.info("Entered WorkflowManager -> initializeRegion()");
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
//				log.info("Workflow Region Initialized Successfully");
//				
//				//Update the Workflow Status
//				fnWorkflow.setFnWorkflowStatus("Initializing of the Workflow Region SUCCESSFUL");
//
//				//Logoff the Workflow Server
//				log.info("Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				log.info("Logged off");
//			}
//			else
//			{
//				log.info("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(1);
//				fnWorkflow.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//				fnWorkflow.setFnWorkflowStatus("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Release the VWSession
//				vwSession = null;
//				log.info("Logged off");
//			}
//		}
//		catch(VWException ex)
//		{
//			log.info("ERROR", ex.getMessage());
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
//			log.info("ERROR", ex.getMessage());
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
//		log.info("Leaving WorkflowManager -> initializeRegion()");
//		log.info("===========================================================");
//		return fnWorkflow;
	}
		
	//Initiate PE Workflow with FnWorkflow Object from an XML Request
	public void initiateWorkflow()
	{
//		//Create the FnWorkflow Object to Return
//		FnWorkflow fnWorkflowResult = new FnWorkflow();
//
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//
//		try
//		{
//			log.info("Entered WorkflowManager -> initiateWorkflow()");
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
//				log.info("Logged in successfully to the Workflow Server");
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
//					log.info("===========================================");
//					log.info("Workflow Process " + process + " is valid");
//					//Create the Workflow
//					VWStepElement stepElement = vwSession.createWorkflow(process);
//
//					//Check if a Valid Process was Launched
//					if (stepElement != null)
//					{
//						//Initial Workflow Launch Step Properties
//						log.info("Workflow Process " + process + " has launched");
//
//						log.info("===========================================");
//						log.info("Setting the Workflow Data Fields");
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
//									log.info("Updating Workflow Data Field: " + vwParam.getName());
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
//						log.info("===========================================");
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
//								log.info("Step Response: " + stepResponse);
//							}
//							String responseValue = "Ok";
//							log.info("Applying Step Response: " + responseValue);
//							stepElement.setSelectedResponse(responseValue);
//						}
//						else
//						{
//							log.info("No Step Responses - Possible Launch Step");
//						}
//
//						//DoStepElementAction - Dispatch the Workflow
//						fnWorkflowResult = doStepElementAction(stepElement, "Dispatch", "", wiiscLog);
//
//						//Update additional custom data fields
//						//More to add later
//
//						//Dispatch the Workflow Launch Step
//						log.info("===========================================");
//						log.info("Workflow Process " + process + " has initiated");
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
//						log.info("Workflow Process " + process + " failed to launch");
//						//Update FnWorkflow
//						fnWorkflowResult.setErrorFlag(1);
//						fnWorkflowResult.setErrorMessage("Process " + process + " FAILED to launch");
//						fnWorkflowResult.setFnWorkflowStatus("Process " + process + " FAILED to launch");
//					}
//				}
//				else
//				{
//					//Missing Parameters
//					log.info("The Workflow Process " + process + " is Invalid or does not exist");
//					//Update FnWorkflow
//					fnWorkflowResult.setErrorFlag(1);
//					fnWorkflowResult.setErrorMessage("Process " + process + " is Invalid");
//					fnWorkflowResult.setFnWorkflowStatus("Process " + process + " is Invalid");
//				}
//
//				//Logoff the Workflow Server
//				log.info("Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				log.info("Logged off");
//			}
//			else
//			{
//				log.info("Workflow Login FAILED, Workflow Server may be unavailable.");
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
//			log.info("ERROR", ex.getMessage());
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
//		log.info("Leaving WorkflowManager -> initiateWorkflow()");
//		log.info("===========================================================");
//		return fnWorkflowResult;
	}

	//Initiate PE Workflow with Document using the FnWorkflow Object from an XML Request
	public void initiateWorkflowWithDocument()
	{
//		//Create the FnWorkflow Object to Return
//		FnWorkflow fnWorkflowResult = new FnWorkflow();
//
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//
//		try
//		{
//			log.info("Entered WorkflowManager -> initiateWorkflowWithDocument()");
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
//				log.info("Logged in successfully to the Workflow Server");
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
//					log.info("===========================================");
//					log.info("Workflow Process " + process_sys_code + " is valid");
//					//Create the Workflow
//					VWStepElement stepElement = vwSession.createWorkflow(process_sys_code);
//
//					//Check if a Valid Process was Launched
//					if (stepElement != null)
//					{
//						//Initial Workflow Launch Step Properties
//						log.info("Workflow Process " + process_sys_code + " has launched");
//
//						log.info("===========================================");
//						log.info("Setting the Workflow Data Fields");
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
//									log.info("Updating Workflow Data Field: " + vwParam.getName());
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
//									log.info("Updating Workflow Attachment: " + vwParam.getName());
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
//						log.info("===========================================");
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
//								log.info("Step Response: " + stepResponse);
//							}
//							String responseValue = "Ok";
//							log.info("Applying Step Response: " + responseValue);
//							stepElement.setSelectedResponse(responseValue);
//						}
//						else
//						{
//							log.info("No Step Responses - Possible Launch Step");
//						}
//
//						//DoStepElementAction - Dispatch the Workflow
//						fnWorkflowResult = doStepElementAction(stepElement, "Dispatch", "", wiiscLog);
//
//						//Update additional custom data fields
//						//More to add later
//
//						//Dispatch the Workflow Launch Step
//						log.info("===========================================");
//						log.info("Workflow Process " + process_sys_code + " has initiated");
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
//						log.info("Workflow Process " + process_sys_code + " failed to launch");
//						//Update FnWorkflow
//						fnWorkflowResult.setErrorFlag(1);
//						fnWorkflowResult.setErrorMessage("Process " + process_sys_code + " FAILED to launch");
//						fnWorkflowResult.setFnWorkflowStatus("Process " + process_sys_code + " FAILED to launch");
//					}
//				}
//				else
//				{
//					//Missing Parameters
//					log.info("The Workflow Process " + process_sys_code + " is Invalid or does not exist");
//					//Update FnWorkflow
//					fnWorkflowResult.setErrorFlag(1);
//					fnWorkflowResult.setErrorMessage("Process " + process_sys_code + " is Invalid");
//					fnWorkflowResult.setFnWorkflowStatus("Process " + process_sys_code + " is Invalid");
//				}
//
//				//Logoff the Workflow Server
//				log.info("Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				log.info("Logged off");
//			}
//			else
//			{
//				log.info("Workflow Login FAILED, Workflow Server may be unavailable.");
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
//			log.info("ERROR", ex.getMessage());
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
//		log.info("Leaving WorkflowManager -> initiateWorkflowWithDocument()");
//		log.info("===========================================================");
//		return fnWorkflowResult;
	}

	//Update PE Workflow with FnWorkflow Object from an XML Request
	public void updateWorkflow()
	{
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
//			log.info("Entered WorkflowManager -> updateWorkflow()");
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
//				log.info("Logged in successfully to the Workflow Server");
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
//						log.info("Reassign");
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
//						log.info("Return");
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
//						log.info("Move");
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
//						log.info("Save");
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
//								log.info("Clarety - " + customResponse);
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
//								log.info("Clarety - " + customResponse);
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
//								log.info("Clarety - " + customResponse);
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
//								log.info("Clarety - " + customResponse);
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
//								log.info("Clarety - " + customResponse);
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
//								log.info("Clarety - " + customResponse);
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
//								log.info("Clarety - " + customResponse);
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
//								log.info("Clarety - ABORT");
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
//								log.info("===========================================================");
//
//								//Display FnWorkflow Info
//								displayFnWorkflowInfo = true;								
//							}
//						}
//						else
//						{
//							//Error because Response was not valid
//							log.info("Response " + response + " was Invalid");
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
//							log.info("===========================================================");
//							log.info("Workflow Process " + fnWorkflow.getFnWorkflowProcess() + " has finished");
//							//Update FnWorkflow
//							fnWorkflow.setErrorFlag(0);
//							fnWorkflow.setErrorMessage("");
//							//fnWorkflow.setFnWorkflowStatus("UPDATED");
//						}
//						else
//						{
//							log.info("===========================================================");
//							log.info("Workflow Process " + fnWorkflow.getFnWorkflowProcess() + " for Workflow Step " + fnWorkflow.getFnWorkflowStep() + " has been updated");
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
//						log.info("===========================================================");
//						log.info("Workflow Process " + process + " for Workflow Step " + step + " has been deleted");
//						//Update FnWorkflow
//						fnWorkflow.setErrorFlag(0);
//						fnWorkflow.setErrorMessage("");
//						fnWorkflow.setFnWorkflowStatus("DELETED");
//					}
//				}
//				else
//				{
//					log.info("Failed to get a StepElement");
//					//Update the FnWorkflow Object
//					//fnWorkflow.setErrorFlag(1);
//				}
//
//				//Logoff the Workflow Server
//				log.info("Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				log.info("Logged off");
//			}//End If VWSession
//			else
//			{
//				log.info("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(1);
//				fnWorkflow.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//				fnWorkflow.setFnWorkflowStatus("Workflow Login FAILED, Workflow Server may be unavailable.");
//			}
//		}
//		catch(VWException ex)
//		{
//			log.info("ERROR", ex.getMessage());
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
//		log.info("Leaving WorkflowManager -> updateWorkflow()");
//		log.info("===========================================================");
//		return fnWorkflow;
	}


	//Initiate PE Workflow with Parameters
	public void initWorkflow(String process_sys_code, String process_id, String account_id)
	{
//		//Create the FnWorkflow Object
//		FnWorkflow fnWorkflow = new FnWorkflow();
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//
//		try
//		{
//			log.info("Entered WorkflowManager -> initWorkflow()");
//
//			//Login to the Workflow Server
//			vwSession = loginWorkflow(wiiscLog);
//
//			if (vwSession != null)
//			{
//				log.info("Logged in successfully to the Workflow Server");
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
//					log.info("===========================================");
//					log.info("Workflow Process " + process_sys_code + " is valid");
//					//Create the Workflow
//					VWStepElement stepElement = vwSession.createWorkflow(process_sys_code);
//
//					//Check if a Valid Process was Launched
//					if (stepElement != null)
//					{
//						//Initial Workflow Launch Step Properties
//						log.info("Workflow Process " + process_sys_code + " has launched");
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
//								log.info("Step Response: " + stepResponse);
//							}
//						}
//						else
//						{
//							log.info("No Step Responses - Possible Launch Step");
//						}
//						log.info("===========================================");
//						log.info("Setting the Workflow Data Fields");
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
//								//log.info("Updating " + vwParametersData[i].getName());
//
//								switch (vwParametersData[i].getFieldType())
//								{
//								case VWFieldType.FIELD_TYPE_INT:
//									//log.info("Integer");
//									if (vwParametersData[i].isArray())
//									{
//										/*int[] arrParamValues = new int[] {1, 2, 3};
//										stepElement.setParameterValue(vwParametersData[i].getName(),arrParamValues,true);*/
//										break;
//									}
//									else
//									{
//										//List of Parameters from GlobalConfig.properties file
//										//log.info("Checking to see if any Integer parameters match a Workflow parameter");
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
//													log.info("Updating process_id");
//													stepElement.setParameterValue(vwParametersData[i].getName(), Integer.parseInt(process_id), true);
//													fnWorkflowProperty.setName(param);
//													fnWorkflowProperty.setValue(process_id);
//													log.info("process_id: " + process_id);
//													break;
//												}
//											}
//										}
//									}
//								case VWFieldType.FIELD_TYPE_STRING:
//									//log.info("String");
//									if (vwParametersData[i].isArray())
//									{
//										/*String[] arrParamValues =	new String[] {"Test_1", "Test_2", "Test_3"};
//										stepElement.setParameterValue(vwParametersData[i].getName(),arrParamValues,true);*/
//										break;
//									} 
//									else
//									{
//										//List of Parameters from GlobalConfig.properties file
//										//log.info("Checking to see if any String parameters match a Workflow parameter");
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
//													log.info("Updating process_sys_code");
//													stepElement.setParameterValue(vwParametersData[i].getName(), process_sys_code, true);
//													//fnWorkflow.setFnWorkflowName(process_sys_code);
//													//fnWorkflow.setFnWorkflowRoster(process_sys_code);
//													fnWorkflowProperty.setName(param);
//													fnWorkflowProperty.setValue(process_sys_code);
//													log.info("process_sys_code: " + process_sys_code);
//													break;
//												}
//												else if (param.equals("ID_"))
//												{
//													log.info("Updating ID_");
//													stepElement.setParameterValue(vwParametersData[i].getName(), account_id, true);
//													//fnWorkflow.setFnWorkflowID(account_id);
//													fnWorkflowProperty.setName(param);
//													fnWorkflowProperty.setValue(account_id);
//													log.info("ID_: " + account_id);
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
//									//log.info("Attachment");
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
//									//log.info("Participant");
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
//						log.info("===========================================");
//						//Update additional custom data fields
//						//More to add later
//
//						// Set the value for the system-defined Response parameter
//						if (stepElement.getStepResponses() != null) {
//							log.info("Step Responses");
//							String responseValue = "Ok";
//							stepElement.setSelectedResponse(responseValue);
//						}
//						else
//						{
//							log.info("No Step Responses");
//							log.info("===========================================");
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
//							log.info("Reasign");
//							// Determine whether a step element
//							// can be reassigned and reassign it
//							if (stepElement.getCanReassign()) {
//								String participantName = "Administrator";
//								stepElement.doReassign(participantName,true,null);
//							}
//							break;
//						case ACTION_TYPE_RETURN:
//							log.info("Return");
//							// Determine whether a step element can be returned to the
//							// queue from which the user delegated or reassigned it and
//							// return it
//							if (stepElement.getCanReturnToSource()) {
//								stepElement.doReturnToSource();
//							}
//							break;
//						case ACTION_TYPE_ABORT:
//							log.info("Abort");
//							// Cancel the changes to the work item
//							// without advancing it in the workflow
//							stepElement.doAbort();
//							break;
//						case ACTION_TYPE_SAVE:
//							log.info("Save");
//							// Save the changes to the work item
//							// and unlock it without advancing it in the workflow
//							stepElement.doSave(true);
//							break;
//						case ACTION_TYPE_DISPATCH:
//							log.info("Dispatch");
//							// Save the changes to the work item
//							// and advance it in the workflow
//							stepElement.doDispatch();
//							break;
//						}
//
//						//Dispatch the Workflow Launch Step
//						log.info("===========================================");
//						log.info("Workflow Process " + process_sys_code + " has initiated");
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
//						log.info("Workflow Process " + process_sys_code + " failed to launch");
//						//Update FnWorkflow
//						fnWorkflow.setErrorFlag(1);
//						fnWorkflow.setErrorMessage("Process " + process_sys_code + " FAILED to launch");
//						fnWorkflow.setFnWorkflowStatus("Process " + process_sys_code + " FAILED to launch");
//					}
//				}
//				else
//				{
//					//Missing Parameters
//					log.info("The Workflow Process " + process_sys_code + " is Invalid or does not exist");
//					//Update FnWorkflow
//					fnWorkflow.setErrorFlag(1);
//					fnWorkflow.setErrorMessage("Process " + process_sys_code + " is Invalid");
//					fnWorkflow.setFnWorkflowStatus("Process " + process_sys_code + " is Invalid");
//				}
//
//				//Logoff the Workflow Server
//				log.info("Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				log.info("Logged off");
//			}
//			else
//			{
//				log.info("Workflow Login FAILED, Workflow Server may be unavailable.");
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
//			log.info("ERROR", ex.getMessage());
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
//		log.info("Leaving WorkflowManager -> initWorkflow()");
//		log.info("===========================================================");
//		return fnWorkflow;
	}

	//Create Workflows
	public void createWorkflows(String process_sys_code, String count)
	{
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
//			log.info("Entered WorkflowManager -> createWorkflows()");
//
//			//Login to the Workflow Server
//			vwSession = loginWorkflow(wiiscLog);
//
//			if (vwSession != null)
//			{
//				log.info("Logged in successfully to the Workflow Server");
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
//						log.info("Checking Roster: " + workClassNames[i]);
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
//						log.info("Roster and Workflow Definition exist");
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
//					log.info("Create Workflows until Workflow Count is 0");
//					log.info("Creating " + workflowCount + " Workflows");
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
//								log.info("Checking Roster: " + validRosters.get(0));
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
//						log.info("There were No Rosters, so there are No Workflows");
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
//					log.info("Create Workflows By Roster until Workflow Count is 0");
//					log.info("Creating " + workflowCount + " Workflows");
//					
//					//Boolean to Validate the Requested Process
//					boolean validProcess = false;
//										
//					//Verify the Workflow Process is Valid			
//					for (int a = 0; a < validRosters.size(); a++)
//					{
//						log.info("Checking Roster: " + validRosters.get(a));
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
//						log.info("The Workflow Process " + process_sys_code + " is Invalid or does not exist");
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
//				log.info("Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				log.info("Logged off");
//			}
//			else
//			{
//				log.info("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflowList Object
//				fnWorkflowList.setErrorFlag(1);
//				fnWorkflowList.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//			}
//		}
//		catch (VWException ex)
//		{
//			log.info("ERROR", ex.getMessage());
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
//		log.info("Leaving WorkflowManager -> createWorkflows()");
//		log.info("===========================================================");
//		return fnWorkflowList;
	}
	
	//Delete Workflows
	public void deleteWorkflows(String process_sys_code, String account_id, String user, String region)
	{
//		//Create the FnWorkflowList Object
//		FnWorkflowList fnWorkflowList = new FnWorkflowList();
//
//		//Get a VWSession Object
//		VWSession vwSession = new VWSession();
//
//		try
//		{
//			log.info("Entered WorkflowManager -> deleteWorkflows()");
//
//			//Login to the Workflow Server
//			vwSession = loginWorkflow(wiiscLog);
//
//			if (vwSession != null)
//			{
//				log.info("Logged in successfully to the Workflow Server");
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
//					log.info("Delete ALL Workflows");
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
//						log.info("There were No Rosters, so there are No Workflows");
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
//					log.info("Delete ALL Workflows By Roster");
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
//				log.info("Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				log.info("Logged off");
//			}
//			else
//			{
//				log.info("Workflow Login FAILED, Workflow Server may be unavailable.");
//				//Update the FnWorkflowList Object
//				fnWorkflowList.setErrorFlag(1);
//				fnWorkflowList.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
//			}
//		}
//		catch (VWException ex)
//		{
//			log.info("ERROR", ex.getMessage());
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
//		log.info("Leaving WorkflowManager -> deleteWorkflows()");
//		log.info("===========================================================");
//		return fnWorkflowList;
	}
	
	private void checkPropertiesFileExist(String propName)
	{
//		boolean propertiesExist = false;
//		
//		try
//		{
//			log.info("Entered WorkflowManager -> checkPropertiesFileExist()");
//			//ResourceBundle res = ResourceBundle.getBundle(propName);
//			ResourceBundle res = null;
//			LocalResource resConfig = getLocalResource(propName);
//			res = resConfig.getLocalBundle(resConfig.getBundlePath(), resConfig.getBundleFile());
//			
//			//Check if the ResourceBundle exists
//			if (res != null)
//			{
//				log.info("Properties file " + propName + " exists");
//				System.out.println("Properties file " + propName + " exists");
//				propertiesExist = true;
//			}
//			else
//			{
//				log.info("Properties file " + propName + " does not exist");
//				System.out.println("Properties file " + propName + " does not exist");
//				propertiesExist = false;
//			}
//		}
//		catch (MissingResourceException e)
//		{
//			//Properties file does not exist
//			log.info("Properties file " + propName + " does not exist");
//			System.out.println("Properties file " + propName + " does not exist");
//			propertiesExist = false;
//		}
//		catch (Exception e)
//		{
//			//Properties file does not exist
//			log.info("Properties file " + propName + " does not exist");
//			System.out.println("Properties file " + propName + " does not exist");
//			propertiesExist = false;
//		}
//		log.info("Leaving WorkflowManager -> checkPropertiesFileExist()");
//		log.info("===========================================================");
//		
//		return propertiesExist;
	}	

	//Create Workflows for a Roster
	private void createWorkflowsByRoster(VWSession vwSession, String rosterName)
	{
//		//Create the FnWorkflowList Object
//		FnWorkflowList fnWorkflowList = new FnWorkflowList();
//		
//		try
//		{
//			log.info("Entered WorkflowManager -> createWorkflowsByRoster()");
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
//				log.info("Workflow Process " + rosterName + " has launched");
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
//						log.info("Step Response: " + stepResponse);
//					}
//					String responseValue = "Ok";
//					log.info("Applying Step Response: " + responseValue);
//					stepElement.setSelectedResponse(responseValue);
//				}
//				else
//				{
//					log.info("No Step Responses - Possible Launch Step");
//				}
//
//				//DoStepElementAction - Dispatch the Workflow
//				fnWorkflowResult = doStepElementAction(stepElement, "Dispatch", "", wiiscLog);
//
//				//Update additional custom data fields
//				//More to add later
//
//				//Dispatch the Workflow Launch Step
//				log.info("===========================================");
//				log.info("Workflow Process " + rosterName + " has initiated");
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
//				log.info("Workflow Process " + rosterName + " failed to launch");
//				//Update FnWorkflow
//				fnWorkflowResult.setErrorFlag(1);
//				fnWorkflowResult.setErrorMessage("Process " + rosterName + " FAILED to launch");
//				fnWorkflowResult.setFnWorkflowStatus("Process " + rosterName + " FAILED to launch");
//			}
//		}
//		catch (VWException ex)
//		{
//			log.info("ERROR", ex.getMessage());
//			//Update the FnWorkflowList Object
//			fnWorkflowList.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowList.setErrorMessage(ex.getMessage());
//		}
//		log.info("Leaving WorkflowManager -> createWorkflowsByRoster()");
//		log.info("===========================================================");
//		
//		return fnWorkflowList;
	}
	
	//Delete Workflows for a Roster
	private void deleteWorkflowsByRoster(VWSession vwSession, String rosterName)
	{
//		//Create the FnWorkflowList Object
//		FnWorkflowList fnWorkflowList = new FnWorkflowList();
//		
//		try
//		{
//			log.info("Entered WorkflowManager -> deleteWorkflowsByRoster()");
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
//							log.info("Failed to get a WorkObject");
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
//			log.info("ERROR", ex.getMessage());
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
//		log.info("Leaving WorkflowManager -> deleteWorkflowsByRoster()");
//		log.info("===========================================================");
//		
//		return fnWorkflowList;
	}

	//Get PE Workflow Info
	public FnWorkflow getWorkflowInfo(String process, String step, String user, String propName, String propValue)
	{
		//Create the FnWorkflow Object
		FnWorkflow fnWorkflow = new FnWorkflow();
		
		//Get a VWSession Object
		VWSession vwSession = new VWSession();

		//Get the Filter Name
		//String filterName = "";
		String[] filterName = null;
		//Get the Filter Value
		//String filterValue = "";
		String[] filterValue = null;

		try
		{
			log.info("Entered WorkflowManager -> getWorkflowInfo()");
			
			log.info("Process: " + process);
			log.info("Step: " + step);
			log.info("User: " + user);
			log.info("Property Name: " + propName);
			log.info("Property Value: " + propValue);
			
			//Login to the Workflow Server
			vwSession = loginWorkflow();

			if (vwSession != null)
			{
				log.info("Logged in successfully to the Workflow Server");
				//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
				//Update the FnWorkflow Object
				fnWorkflow.setErrorFlag(0);
				//fnWorkflow.setErrorMessage("");

				/*****************************************************************************
				 * Get the Workflow by a Property and Property Value - workflow data field
				 *****************************************************************************/
				if (step.length() == 0 && propName.length() > 0 && propValue.length() > 0)
				{
					//By Property and Property Value only
					log.info("Get the Workflow by Property and Property Value");

					//Initialize the Array
					filterName = new String[1];
					filterValue = new String[1];
					filterName[0] = propName;
					filterValue[0] = propValue;

					//Get String[] of Queues
					String[] queueNames = getQueues(vwSession);
					//Check queueNames
					if (queueNames.length > 0)
					{
						//Loop and Get Workflows from each Queue
						for (int i = 0; i < queueNames.length; i++)
						{
							//Exclude the System Queues
							if (queueNames[i].contains("Instruction") == false &&
									queueNames[i].contains("(") == false &&
									queueNames[i].equals("Conductor") == false &&
									queueNames[i].equals("WSRequest") == false &&
									queueNames[i].equals("CE_Operations") == false)
							{
								//Get the Workflow
								fnWorkflow = getFnWorkflowByQueue(vwSession, queueNames[i], filterName, filterValue);
								//Check the FnWorkflow to see if its Null
								if (fnWorkflow != null)
								{
									break;
								}
							}
						}
						//Check if the FnWorkflow was still null after checking each Queue
						if (fnWorkflow == null)
						{
							log.info("There was No Workflow found for the Property " + propName + " and Property Value " + propValue);
							//Update the Error Flag to 0 since this is not a real Error
							fnWorkflow = new FnWorkflow();
							fnWorkflow.setErrorFlag(0);
							//fnWorkflow.setErrorMessage("");
							log.info("Workflow returning with ErrorFlag of 0");
						}
					}
					else
					{
						log.info("There were No Queues, so there are No Workflows");
						//Update the FnWorkflow Object
						fnWorkflow.setErrorFlag(1);
						//fnWorkflow.setErrorMessage("There were No Queues, so there are No Workflows");
					}
				}
				/*********************************************************************************
				 * Get the Workflow by a Step, Property and Property Value - workflow data field
				 *********************************************************************************/
				else if (step.length() > 0 && propName.length() > 0 && propValue.length() > 0)
				{
					//By Property and Property Value only
					log.info("Get the Workflow by a Step, Property and Property Value");

					//Initialize the Array
					filterName = new String[1];
					filterValue = new String[1];
					filterName[0] = propName;
					filterValue[0] = propValue;

					//Set the Queue
					String queueName = step;
					//Check queueName
					if (queueName.length() > 0)
					{
						//Exclude the System Queues
						if (!queueName.contains("Instruction") &&
								!queueName.contains("(") &&
								!queueName.equals("Conductor") &&
								!queueName.equals("WSRequest") &&
								!queueName.equals("CE_Operations"))
						{
							//Get the Workflow
							fnWorkflow = getFnWorkflowByQueue(vwSession, queueName, filterName, filterValue);
						}
					}
					else
					{
						log.info("There was No Queue, so there are No Workflows");
						//Update the FnWorkflow Object
						fnWorkflow.setErrorFlag(1);
//						fnWorkflow.setErrorMessage("There was No Queue, so there are No Workflows");
					}
				}
				else
				{
					//Return an Error
					log.info("The Parameters Process, Step, Property Name and Property Value cannot be used together, so there are No Workflows");
					//Update the FnWorkflow Object
					fnWorkflow.setErrorFlag(1);
//					fnWorkflow.setErrorMessage("The Parameters Process, Step, Property Name and Property Value cannot be used together, so there are No Workflows");
				}

				//Logoff the Workflow Server
				log.info("Logging off the Workflow Server");
				vwSession.logoff();
				//Release the VWSession
				vwSession = null;
				log.info("Logged off");
				
				//Check if Workflow Output is Enabled to write the records to a file
				if (appConfig.getEnableWorkflowOutput().equals("true")) {
					if (appConfig.getWorkflowOutputPath() != null && !appConfig.getWorkflowOutputPath().equals("")) {
						//Use the Output Path to create a file with the Workflow Info
						//Call Write FnWorkflow to File
					}
				}
			}
			else
			{
				log.info("Workflow Login FAILED, Workflow Server may be unavailable.");
				//Update the FnWorkflow Object
				fnWorkflow.setErrorFlag(1);
//				fnWorkflow.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
			}

		}
		catch(VWException ex)
		{
			log.info("ERROR", ex.getMessage());
			//Update the FnWorkflow Object
			fnWorkflow.setErrorFlag(1);
			//Update ErrorMessage
//			fnWorkflow.setErrorMessage(ex.getMessage());
//			fnWorkflow.setFnWorkflowStatus("Exception in getWorkflowInfo()");
		}
		log.info("Leaving WorkflowManager -> getWorkflowInfo()");
		log.info("===========================================================");
		return fnWorkflow;
	}

	public FnBase getWorkflowList(String process, String step, String user, String propName, String propValue, String sortBy)
	{
		//Create an FnBaseXML
		FnBase fnBase = new FnBase();
		//Create the FnWorkflowList Object
		FnWorkflowList fnWorkflowList = new FnWorkflowList();

		//Get a VWSession Object
		VWSession vwSession = new VWSession();
		//FileNet DB Connection
		SQLServerConnection con = null;
		//Boolean to continue processing
		boolean beginProcessing = false;

		//Get the Filter Name
		//String filterName = "";
		String[] filterName = null;
		//Get the Filter Value
		//String filterValue = "";
		String[] filterValue = null;

		try
		{
			log.info("Entered WorkflowManager -> getWorkflowList()");
			
			log.info("Process: " + process);
			log.info("Step: " + step);
			log.info("User: " + user);
			log.info("Property Name: " + propName);
			log.info("Property Value: " + propValue);
			log.info("Sort By: " + sortBy);

			//Login to the Workflow Server
			vwSession = loginWorkflow();

			if (vwSession != null)
			{
				log.info("Logged in successfully to the Workflow Server");
				//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
				//Update the FnWorkflowList Object
				fnWorkflowList.setErrorFlag(0);
				//fnWorkflowList.setErrorMessage("");

				/****************************************
				 * Get ALL Workflows using Roster Search
				 ****************************************/
				//Add later if needed
				/**************************************************
				 * Get ALL Workflows using Queue Search - Workpool 
				 **************************************************/
				if (process.length() == 0 && step.length() == 0 && user.length() == 0 && propName.length() == 0 && propValue.length() == 0)
				{
					//ALL Workflows - no process_sys_code, no account_id, no Participant specified
					log.info("Get ALL Workflows");

					if (appConfig.getWorkflowSearchUserField() != null && appConfig.getWorkflowSearchUserField().length() > 0)
					{
						//Initialize the Array
						filterName = new String[1];
						filterValue = new String[1];
						filterName[0] = appConfig.getWorkflowSearchUserField();
						filterValue[0] = user;
					}
					
					//Get String[] of Queue
					String[] queueNames = getQueues(vwSession);
					//Check queueNames
					if (queueNames.length > 0)
					{
						//Check if DB FileNet Query will be used or FileNet API
						if (appConfig.getWorkflowDBSearchEnabled().equals("true"))
						{
							con = loginWorkflowDB();
							if (con != null)
							{
								beginProcessing = true;
							}
						}
						else
						{
							beginProcessing = true;
						}
						
						//Verify Processing can begin
						if (beginProcessing)
						{
							//Loop and Get Workflows from each Queue
							for (int i = 0; i < queueNames.length; i++)
							{
								//Exclude the System Queues
								if (!queueNames[i].contains("Instruction") &&
										!queueNames[i].contains("(") &&
										!queueNames[i].equals("Conductor") &&
										!queueNames[i].equals("WSRequest") &&
										!queueNames[i].equals("CE_Operations"))
								{
												
									//Get the FnWorkflowList by Queue Name
									fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueNames[i], filterName, filterValue, sortBy);
																	
									//Check FnWorkflowList size
									if (fnWorkflowList.getCount() == 0 || fnWorkflowList.getErrorFlag() == 0)
									{
										fnWorkflowList.setStepName(queueNames[i]);
										//Add the List to the FnBaseXML
										fnBase.addFnWorkflowList(fnWorkflowList);
										
										//Check if Workflow Output is Enabled to write the records to a file
										if (appConfig.getEnableWorkflowOutput().equals("true")) {
											if (appConfig.getWorkflowOutputPath() != null && !appConfig.getWorkflowOutputPath().equals("")) {
												//Use the Output Path to create a file with the Workflow Info
												//Call Write FnWorkflow to File
												outputToFile(fnBase, "Workflow", appConfig.getWorkflowOutputPath());
											}
										}
									}
									else
									{
										log.info("Error with Queue that needs addressed");
										//Update the FnBaseXML Object
										fnBase.setErrorFlag(1);
										//fnBase.setErrorMessage("Error with Queue that needs addressed");
									}
								}
							}
						}
						else
						{
							log.info("Processing cannot begin because a DB Connection could not be established");
						}
												
						//Close the FileNet DB Connection
						if (con != null)
						{
							try
							{
								log.info("Closing the FileNet DB Connection");
								con.close();
							}
							catch(Exception e)
							{
								log.info("Error closing the FileNet DB Connection");
								log.info("ERROR", e.getMessage());
							}
						}
					}
					else
					{
						log.info("There were No Queues, so there are No Workflows");
						//Update the FnBaseXML Object
						fnBase.setErrorFlag(1);
						//fnBase.setErrorMessage("There were No Queues, so there are No Workflows");
					}					
				}
				/**********************************
				 * Get ALL Workflows for a Process
				 **********************************/
				else if (process.length() > 0 && step.length() == 0 && user.length() == 0 && propName.length() == 0 && propValue.length() == 0)
				{
					//By process only
					log.info("Get ALL Workflows for a Process");
					
					//Get the FnWorkflowList
					fnWorkflowList = getFnWorkflowListByRoster(vwSession, process, filterName, filterValue);
					//Add the List to the FnBaseXML
					fnBase.addFnWorkflowList(fnWorkflowList);
					
					//Check if Workflow Output is Enabled to write the records to a file
					if (appConfig.getEnableWorkflowOutput().equals("true")) {
						if (appConfig.getWorkflowOutputPath() != null && !appConfig.getWorkflowOutputPath().equals("")) {
							//Use the Output Path to create a file with the Workflow Info
							//Call Write FnWorkflow to File
							outputToFile(fnBase, "Workflow", appConfig.getWorkflowOutputPath());
						}
					}
				}
				/**********************************
				 * Get ALL Workflows for a Step
				 **********************************/
				else if (process.length() == 0 && step.length() > 0 && user.length() == 0 && propName.length() == 0 && propValue.length() == 0)
				{
					//By step only
					log.info("Get ALL Workflows for a Step");
					
					if (appConfig.getWorkflowSearchUserField() != null && appConfig.getWorkflowSearchUserField().length() > 0)
					{
						//Initialize the Array
						filterName = new String[1];
						filterValue = new String[1];
						filterName[0] = appConfig.getWorkflowSearchUserField();
						filterValue[0] = "";
					}
					
					//Check if DB FileNet Query will be used or FileNet API
					if (appConfig.getWorkflowDBSearchEnabled().equals("true"))
					{
						con = loginWorkflowDB();
						if (con != null)
						{
							beginProcessing = true;
						}
					}
					else
					{
						beginProcessing = true;
					}

					//Verify Processing can begin
					if (beginProcessing)
					{
						//Get the FnWorkflowList
						fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, step, filterName, filterValue, sortBy);
						//Add the List to the FnBaseXML
						fnBase.addFnWorkflowList(fnWorkflowList);
						
						//Check if Workflow Output is Enabled to write the records to a file
						if (appConfig.getEnableWorkflowOutput().equals("true")) {
							if (appConfig.getWorkflowOutputPath() != null && !appConfig.getWorkflowOutputPath().equals("")) {
								//Use the Output Path to create a file with the Workflow Info
								//Call Write FnWorkflow to File
								outputToFile(fnBase, "Workflow", appConfig.getWorkflowOutputPath());
							}
						}
					}
					else
					{
						log.info("Processing cannot begin because a DB Connection could not be established");
					}
											
					//Close the FileNet DB Connection
					if (con != null)
					{
						try
						{
							log.info("Closing the FileNet DB Connection");
							con.close();
						}
						catch(Exception e)
						{
							log.info("Error closing the FileNet DB Connection");
							log.info("ERROR", e.getMessage());
						}
					}
				}
				/*****************************************************************************
				 * Get ALL Workflows by a Property and Property Value - workflow data field
				 *****************************************************************************/
				else if (process.length() == 0 && step.length() == 0 && user.length() == 0 && propName.length() > 0 && propValue.length() > 0)
				{
					//By Property and Property Value only
					log.info("Get ALL Workflows by Property and Property Value");

					//Get String[] of Queues
					String[] queueNames = getQueues(vwSession);
					//Check queueNames
					if (queueNames.length > 0)
					{
						if (appConfig.getWorkflowSearchUserField() != null)
						{
							//Check if the Property setting has a value
							if (appConfig.getWorkflowSearchUserField().length() == 0)
							{
								//Initialize the Array
								filterName = new String[1];
								filterValue = new String[1];
								filterName[0] = propName;
								filterValue[0] = propValue;
								log.info("Property Value Only");
							}
							else
							{
								//Initialize the Array
								filterName = new String[2];
								filterValue = new String[2];
								filterName[0] = appConfig.getWorkflowSearchUserField();
								filterValue[0] = user;
								filterName[1] = propName;
								filterValue[1] = propValue;
								log.info("Property Value and User");
							}
						}
												
						//Check if DB FileNet Query will be used or FileNet API
						if (appConfig.getWorkflowDBSearchEnabled().equals("true"))
						{
							con = loginWorkflowDB();
							if (con != null)
							{
								beginProcessing = true;
							}
						}
						else
						{
							beginProcessing = true;
						}
						
						//Verify Processing can begin
						if (beginProcessing)
						{
							//Loop and Get Workflows from each Queue
							for (int i = 0; i < queueNames.length; i++)
							{
								//Exclude the System Queues
								if (!queueNames[i].contains("Instruction") &&
										!queueNames[i].contains("(") &&
										!queueNames[i].equals("Conductor") &&
										!queueNames[i].equals("WSRequest") &&
										!queueNames[i].equals("CE_Operations"))
								{
									fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueNames[i], filterName, filterValue, sortBy);
									//Add the List to the FnBaseXML
									fnBase.addFnWorkflowList(fnWorkflowList);
									
									//Check if Workflow Output is Enabled to write the records to a file
									if (appConfig.getEnableWorkflowOutput().equals("true")) {
										if (appConfig.getWorkflowOutputPath() != null && !appConfig.getWorkflowOutputPath().equals("")) {
											//Use the Output Path to create a file with the Workflow Info
											//Call Write FnWorkflow to File
											outputToFile(fnBase, "Workflow", appConfig.getWorkflowOutputPath());
										}
									}
								}
							}
						}
						else
						{
							log.info("Processing cannot begin because a DB Connection could not be established");
						}
												
						//Close the FileNet DB Connection
						if (con != null)
						{
							try
							{
								log.info("Closing the FileNet DB Connection");
								con.close();
							}
							catch(Exception e)
							{
								log.info("Error closing the FileNet DB Connection");
								log.info("ERROR", e.getMessage());
							}
						}
					}
					else
					{
						log.info("There were No Queues, so there are No Workflows");
						//Update the FnWorkflowList Object
						fnWorkflowList.setErrorFlag(1);
						//fnWorkflowList.setErrorMessage("There were No Queues, so there are No Workflows");
						//Update the FnBaseXML Object
						fnBase.setErrorFlag(1);
						//fnBase.setErrorMessage("There were No Queues, so there are No Workflows");
					}
				}
				/***************************************************************************************
				 * Get ALL Workflows by User/Participant - workflow data field User/Participant - Inbox
				 ***************************************************************************************/
				else if (process.length() == 0 && step.length() == 0 && user.length() > 0 && propName.length() == 0 && propValue.length() == 0)
				{
					//By Participant Only
					log.info("Get ALL Workflows by User");

					//Initialize the Array
					filterName = new String[1];
					filterValue = new String[1];
					//Queue
					String queueName = "";
					//Get String[] of Queues
					String[] queueNames = null;
					
					if (appConfig.getWorkflowSearchUserField() != null)
					{
						//Check if the Property setting has a value
						if (appConfig.getWorkflowSearchUserField().length() == 0)
						{
							//Update the Array
							filterName[0] = "F_BoundUser";
							filterValue[0] = user;

							//Get the Inbox(0) Queue and use this to find the Workflows for the particular User
							queueName = "Inbox(0)";
							
							fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueName, filterName, filterValue, sortBy);
							//Add the List to the FnBaseXML
							fnBase.addFnWorkflowList(fnWorkflowList);
							
							//Check if Workflow Output is Enabled to write the records to a file
							if (appConfig.getEnableWorkflowOutput().equals("true")) {
								if (appConfig.getWorkflowOutputPath() != null && !appConfig.getWorkflowOutputPath().equals("")) {
									//Use the Output Path to create a file with the Workflow Info
									//Call Write FnWorkflow to File
									outputToFile(fnBase, "Workflow", appConfig.getWorkflowOutputPath());
								}
							}
						}
						else
						{
							//String[] of Queues
							queueNames = getQueues(vwSession);
							//Check queueNames
							if (queueNames.length > 0)
							{
								//Update the Array
								filterName[0] = appConfig.getWorkflowSearchUserField();
								filterValue[0] = user;
								
								//Loop and Get Workflows from each Queue
								for (int i = 0; i < queueNames.length; i++)
								{
									//Exclude the System Queues
									if (!queueNames[i].contains("Instruction") &&
											!queueNames[i].contains("(") &&
											!queueNames[i].equals("Conductor") &&
											!queueNames[i].equals("WSRequest") &&
											!queueNames[i].equals("CE_Operations"))
									{
										fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueNames[i], filterName, filterValue, sortBy);
										//Add the List to the FnBaseXML
										fnBase.addFnWorkflowList(fnWorkflowList);
										
										//Check if Workflow Output is Enabled to write the records to a file
										if (appConfig.getEnableWorkflowOutput().equals("true")) {
											if (appConfig.getWorkflowOutputPath() != null && !appConfig.getWorkflowOutputPath().equals("")) {
												//Use the Output Path to create a file with the Workflow Info
												//Call Write FnWorkflow to File
												outputToFile(fnBase, "Workflow", appConfig.getWorkflowOutputPath());
											}
										}
									}
								}
							}
							else
							{
								log.info("There were No Queues, so there are No Workflows");
								//Update the FnBaseXML Object
								fnBase.setErrorFlag(1);
								//fnBase.setErrorMessage("There were No Queues, so there are No Workflows");
							}
						}
					}
					else
					{
						//Update the Array
						filterName[0] = "F_BoundUser";
						filterValue[0] = user;

						//Get the Inbox(0) Queue and use this to find the Workflows for the particular User
						queueName = "Inbox(0)";
						
						fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueName, filterName, filterValue, sortBy);
						//Add the List to the FnBaseXML
						fnBase.addFnWorkflowList(fnWorkflowList);
						
						//Check if Workflow Output is Enabled to write the records to a file
						if (appConfig.getEnableWorkflowOutput().equals("true")) {
							if (appConfig.getWorkflowOutputPath() != null && !appConfig.getWorkflowOutputPath().equals("")) {
								//Use the Output Path to create a file with the Workflow Info
								//Call Write FnWorkflow to File
								outputToFile(fnBase, "Workflow", appConfig.getWorkflowOutputPath());
							}
						}
					}
				}
				/***************************************************************************************
				 * Get ALL Workflows by User/Participant, Property and Property Value - Inbox Search
				 ***************************************************************************************/
				else if (process.length() == 0 && step.length() == 0 && user.length() > 0 && propName.length() > 0 && propValue.length() > 0)
				{
					//By Participant Only
					log.info("Get ALL Workflows by User, Property and Property Value");

					//Initialize the Array
					filterName = new String[2];
					filterValue = new String[2];
					//Queue
					String queueName = "";
					//Get String[] of Queues
					String[] queueNames = null;
					
					if (appConfig.getWorkflowSearchUserField() != null)
					{
						//Check if the Property setting has a value
						if (appConfig.getWorkflowSearchUserField().length() == 0)
						{
							//Update the Array
							filterName[0] = "F_BoundUser";
							filterValue[0] = user;
							filterName[1] = propName;
							filterValue[1] = propValue;

							//Get the Inbox(0) Queue and use this to find the Workflows for the particular User
							queueName = "Inbox(0)";
							
							fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueName, filterName, filterValue, sortBy);
							//Add the List to the FnBaseXML
							fnBase.addFnWorkflowList(fnWorkflowList);
							
							//Check if Workflow Output is Enabled to write the records to a file
							if (appConfig.getEnableWorkflowOutput().equals("true")) {
								if (appConfig.getWorkflowOutputPath() != null && !appConfig.getWorkflowOutputPath().equals("")) {
									//Use the Output Path to create a file with the Workflow Info
									//Call Write FnWorkflow to File
									outputToFile(fnBase, "Workflow", appConfig.getWorkflowOutputPath());
								}
							}
						}
						else
						{
							//String[] of Queues
							queueNames = getQueues(vwSession);
							//Check queueNames
							if (queueNames.length > 0)
							{
								//Update the Array
								filterName[0] = appConfig.getWorkflowSearchUserField();
								filterValue[0] = user;
								filterName[1] = propName;
								filterValue[1] = propValue;
								
								//Loop and Get Workflows from each Queue
								for (int i = 0; i < queueNames.length; i++)
								{
									//Exclude the System Queues
									if (!queueNames[i].contains("Instruction") &&
											!queueNames[i].contains("(") &&
											!queueNames[i].equals("Conductor") &&
											!queueNames[i].equals("WSRequest") &&
											!queueNames[i].equals("CE_Operations"))
									{
										fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueNames[i], filterName, filterValue, sortBy);
										//Add the List to the FnBaseXML
										fnBase.addFnWorkflowList(fnWorkflowList);
										
										//Check if Workflow Output is Enabled to write the records to a file
										if (appConfig.getEnableWorkflowOutput().equals("true")) {
											if (appConfig.getWorkflowOutputPath() != null && !appConfig.getWorkflowOutputPath().equals("")) {
												//Use the Output Path to create a file with the Workflow Info
												//Call Write FnWorkflow to File
												outputToFile(fnBase, "Workflow", appConfig.getWorkflowOutputPath());
											}
										}
									}
								}
							}
							else
							{
								log.info("There were No Queues, so there are No Workflows");
								//Update the FnBaseXML Object
								fnBase.setErrorFlag(1);
								//fnBase.setErrorMessage("There were No Queues, so there are No Workflows");
							}
						}
					}
					else
					{
						//Update the Array
						filterName[0] = "F_BoundUser";
						filterValue[0] = user;
						filterName[1] = propName;
						filterValue[1] = propValue;

						//Get the Inbox(0) Queue and use this to find the Workflows for the particular User
						queueName = "Inbox(0)";
						
						fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueName, filterName, filterValue, sortBy);
						//Add the List to the FnBaseXML
						fnBase.addFnWorkflowList(fnWorkflowList);
						
						//Check if Workflow Output is Enabled to write the records to a file
						if (appConfig.getEnableWorkflowOutput().equals("true")) {
							if (appConfig.getWorkflowOutputPath() != null && !appConfig.getWorkflowOutputPath().equals("")) {
								//Use the Output Path to create a file with the Workflow Info
								//Call Write FnWorkflow to File
								outputToFile(fnBase, "Workflow", appConfig.getWorkflowOutputPath());
							}
						}
					}
				}
				/*********************************************************************************
				 * Get ALL Workflows by a Step, Property and Property Value - workflow data field
				 *********************************************************************************/
				else if (process.length() == 0 && step.length() > 0 && user.length() == 0 && propName.length() > 0 && propValue.length() > 0)
				{
					//By Property and Property Value only
					log.info("Get ALL Workflows by a Step, Property and Property Value");

					//Initialize the Array
					filterName = new String[1];
					filterValue = new String[1];
					filterName[0] = propName;
					filterValue[0] = propValue;

					//Set the Queue
					String queueName = step;
					//Check queueName
					if (queueName.length() > 0)
					{
						//Check if DB FileNet Query will be used or FileNet API
						if (appConfig.getWorkflowDBSearchEnabled().equals("true"))
						{
							con = loginWorkflowDB();
							if (con != null)
							{
								beginProcessing = true;
							}
						}
						else
						{
							beginProcessing = true;
						}
						
						//Verify Processing can begin
						if (beginProcessing)
						{
							//Exclude the System Queues
							if (!queueName.contains("Instruction") &&
									!queueName.contains("(") &&
									!queueName.equals("Conductor") &&
									!queueName.equals("WSRequest") &&
									!queueName.equals("CE_Operations"))
							{
								fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueName, filterName, filterValue, sortBy);
								//Add the List to the FnBaseXML
								fnBase.addFnWorkflowList(fnWorkflowList);
								
								//Check if Workflow Output is Enabled to write the records to a file
								if (appConfig.getEnableWorkflowOutput().equals("true")) {
									if (appConfig.getWorkflowOutputPath() != null && !appConfig.getWorkflowOutputPath().equals("")) {
										//Use the Output Path to create a file with the Workflow Info
										//Call Write FnWorkflow to File
										outputToFile(fnBase, "Workflow", appConfig.getWorkflowOutputPath());
									}
								}
								
							}
						}
						else
						{
							log.info("Processing cannot begin because a DB Connection could not be established");
						}
												
						//Close the FileNet DB Connection
						if (con != null)
						{
							try
							{
								log.info("Closing the FileNet DB Connection");
								con.close();
							}
							catch(Exception e)
							{
								log.info("Error closing the FileNet DB Connection");
								log.info("ERROR", e.getMessage());
							}
						}
					}
					else
					{
						log.info("There was No Queue, so there are No Workflows");
						//Update the FnWorkflowList Object
						fnWorkflowList.setErrorFlag(1);
						//fnWorkflowList.setErrorMessage("There was No Queue, so there are No Workflows");
						//Update the FnBaseXML Object
						fnBase.setErrorFlag(1);
						//fnBase.setErrorMessage("There was No Queue, so there are No Workflows");
					}
				}
				/**********************************************
				 * Get ALL Workflows by Step and User
				 **********************************************/
				else if (process.length() == 0 && step.length() > 0 && user.length() > 0 && propName.length() == 0 && propValue.length() == 0)
				{
					//By Process and Account ID
					log.info("Get ALL Workflows by Step and User");

					//Initialize the Array
					filterName = new String[1];
					filterValue = new String[1];
					
					if (appConfig.getWorkflowSearchUserField() != null)
					{
						//Check if the Property setting has a value
						if (appConfig.getWorkflowSearchUserField().length() == 0)
						{
							//Update the Array
							filterName[0] = "F_BoundUser";
							filterValue[0] = user;
						}
						else
						{
							//Update the Array
							filterName[0] = appConfig.getWorkflowSearchUserField();
							filterValue[0] = user;
						}
					}
					else
					{
						//Update the Array
						filterName[0] = "F_BoundUser";
						filterValue[0] = user;
					}
					
					//Set the Queue
					String queueName = step;
					//Check queueName
					if (queueName.length() > 0)
					{
						//Exclude the System Queues
						if (!queueName.contains("Instruction") &&
								!queueName.contains("(") &&
								!queueName.equals("Conductor") &&
								!queueName.equals("WSRequest") &&
								!queueName.equals("CE_Operations"))
						{
							fnWorkflowList = getFnWorkflowListByQueue(con, vwSession, queueName, filterName, filterValue, sortBy);
							//Add the List to the FnBaseXML
							fnBase.addFnWorkflowList(fnWorkflowList);
							
							//Check if Workflow Output is Enabled to write the records to a file
							if (appConfig.getEnableWorkflowOutput().equals("true")) {
								if (appConfig.getWorkflowOutputPath() != null && !appConfig.getWorkflowOutputPath().equals("")) {
									//Use the Output Path to create a file with the Workflow Info
									//Call Write FnWorkflow to File
									outputToFile(fnBase, "Workflow", appConfig.getWorkflowOutputPath());
								}
							}
						}
					}
					else
					{
						log.info("There were No Queues, so there are No Workflows");
						//Update the FnWorkflowList Object
						fnWorkflowList.setErrorFlag(1);
						//fnWorkflowList.setErrorMessage("There were No Queues, so there are No Workflows");
						//Update the FnBaseXML Object
						fnBase.setErrorFlag(1);
						//fnBase.setErrorMessage("There were No Queues, so there are No Workflows");
					}
				}
				else
				{
					//Return an Error
					log.info("The Process, Step, Property Name and Property Value combination is not an allowed function, so there are No Workflows");
					//Update the FnWorkflowList Object
					fnWorkflowList.setErrorFlag(1);
					//fnWorkflowList.setErrorMessage("The Process, Step, Property Name and Property Value combination is not an allowed function, so there are No Workflows");
					//Update the FnBaseXML Object
					fnBase.setErrorFlag(1);
					//fnBase.setErrorMessage("The Process, Step, Property Name and Property Value combination is not an allowed function, so there are No Workflows");
				}

				//Logoff the Workflow Server
				log.info("Logging off the Workflow Server");
				vwSession.logoff();
				//Release the VWSession
				vwSession = null;
				log.info("Logged off");
			}
			else
			{
				log.info("Workflow Login FAILED, Workflow Server may be unavailable.");
				//Update the FnWorkflowList Object
				fnWorkflowList.setErrorFlag(1);
				//fnWorkflowList.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
				//Update the FnBaseXML Object
				fnBase.setErrorFlag(1);
				//fnBase.setErrorMessage("Workflow Login FAILED, Workflow Server may be unavailable.");
			}
		}
		catch (VWException ex)
		{
			log.info("ERROR", ex.getMessage());
			if (vwSession != null)
			{
				//Set vwSession to null to kill any connections
				vwSession = null;
			}
			//Update the FnWorkflowList Object
			fnWorkflowList.setErrorFlag(1);
			//Update ErrorMessage
			//fnWorkflowList.setErrorMessage(ex.getMessage());
			//Update the FnBaseXML Object
			fnBase.setErrorFlag(1);
			//Update ErrorMessage
			//fnBase.setErrorMessage(ex.getMessage());
		}
		log.info("Leaving WorkflowManager -> getWorkflowList()");
		log.info("===========================================================");
		return fnBase;
	}
	
	private void outputToFile(Object object, String objectType, String outputPath) {
		//Log Output File
		PrintWriter outDestination1 = null;
		//CSV Output File
		PrintWriter outDestination2 = null;
		
		try {
						
			String outputFile1 = outputPath + "/" + objectType + "_Output_" + getDateTime() + ".log";
			String outputFile2 = outputPath + "/" + objectType + "_Output_" + getDateTime() + ".csv";
			//Write to File
			outDestination1 = getWriter(outputFile1);
			outDestination2 = getWriter(outputFile2);
			
			//Check the objectType
			if (objectType.equals("Imaging")) {
				//Do Something
			} else if (objectType.equals("Workflow")) {
				
				if (object instanceof FnBase) {
					FnBase fnBase = (FnBase) object;
					
					//Header
					outDestination1.println("====================================");
					outDestination1.println(outputFile1);
					outDestination1.println("====================================");
					
					//workflowCount
					outDestination1.println("Workflow Count: " + fnBase.getWorkflowCount());
					
					//fnWorkflowList
					List<FnWorkflowList> list = fnBase.getFnWorkflowList();
					for (FnWorkflowList wflist : list) {
						outDestination1.println("====================================");
						outDestination1.println("WORKFLOW START");
						outDestination1.println("Workflow Process Name: " + wflist.getProcessName());
						outDestination1.println("Workflow Step Name: " + wflist.getStepName());
						outDestination1.println("====================================");
						List<FnWorkflow> list2 = wflist.getFnWorkflowList();
						for (FnWorkflow wf : list2) {
							outDestination1.println("Workflow ID: " + wf.getFnWorkflowID());
							outDestination1.println("Workflow Name: " + wf.getFnWorkflowName());
							outDestination1.println("Workflow Process: " + wf.getFnWorkflowProcess());
							outDestination1.println("Workflow Queue: " + wf.getFnWorkflowQueue());
							outDestination1.println("Workflow Response: " + wf.getFnWorkflowResponse());
							outDestination1.println("Workflow Roster: " + wf.getFnWorkflowRoster());
							outDestination1.println("Workflow Step: " + wf.getFnWorkflowStep());
							outDestination1.println("Workflow User: " + wf.getFnWorkflowUser());
							outDestination1.println("====================================");
							
							//Output to CSV
							outDestination2.print(wf.getFnWorkflowID() + ",");
							outDestination2.print(wf.getFnWorkflowProcess() + ",");
							outDestination2.print(wf.getFnWorkflowQueue() + ",");
							
							FnWorkflowPropertyList wfpropList = wf.getFnWorkflowPropertyList();
							List<FnWorkflowProperty> wfpropList2 = wfpropList.getFnWorkflowPropsList();
							for (FnWorkflowProperty wfprop : wfpropList2) {
								outDestination1.println("Name: " + wfprop.getName());
								outDestination1.println("Value: " + wfprop.getValue());
								outDestination1.println("====================================");
								
								//Output to CSV
								outDestination2.print(wfprop.getName() + ",");
								outDestination2.print(wfprop.getValue() + ",");
							}
						}
						outDestination1.println("WORKFLOW END");
					}
					//outDestination1.println(fnBase.getFnWorkflowList());
					
				}
				
			} else if (objectType.equals("Both")) {
				//Do Something
			} else {
				//Do Something - log error
			}
			
			//Close the Files
			outDestination1.close();
			outDestination1 = null;
			
			outDestination2.close();
			outDestination2 = null;
			
		} catch (Exception e) {
			log.info("ERROR", e.getMessage());
			
			//Close the Files
			outDestination1.close();
			outDestination1 = null;
			
			outDestination2.close();
			outDestination2 = null;
		}
		
	}

	public void getWorkflowCountsList(String process, String step, String user, String propName, String propValue, String sortBy)
	{
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
//			log.info("Entered WorkflowManager -> getWorkflowCountsList()");
//			log.info("Process: " + process);
//			log.info("Step: " + step);
//			log.info("User: " + user);
//			log.info("Property Name: " + propName);
//			log.info("Property Value: " + propValue);
//			log.info("Sort By: " + sortBy);
//
//			//Login to the Workflow Server
//			vwSession = loginWorkflow(wiiscLog);
//
//			if (vwSession != null)
//			{
//				log.info("Logged in successfully to the Workflow Server");
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
//					log.info("Get ALL Workflows");
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
//									log.info("Error with Queue that needs addressed");
//									//Update the FnBaseXML Object
//									fnBaseXML.setErrorFlag(1);
//									fnBaseXML.setErrorMessage("Error with Queue that needs addressed");
//								}
//							}
//						}
//					}
//					else
//					{
//						log.info("There were No Queues, so there are No Workflows");
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
//					log.info("Get ALL Workflows for a Process");
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
//					log.info("Get ALL Workflows for a Step");
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
//					log.info("Get ALL Workflows by Property and Property Value");
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
//						log.info("There were No Queues, so there are No Workflows");
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
//					log.info("Get ALL Workflows by User");
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
//								log.info("There were No Queues, so there are No Workflows");
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
//					log.info("Get ALL Workflows by User, Property and Property Value");
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
//								log.info("There were No Queues, so there are No Workflows");
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
//					log.info("Get ALL Workflows by a Step, Property and Property Value");
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
//						log.info("There was No Queue, so there are No Workflows");
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
//					log.info("Get ALL Workflows by Step and User");
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
//						log.info("There were No Queues, so there are No Workflows");
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
//					log.info("The Process, Step, Property Name and Property Value combination is not an allowed function, so there are No Workflows");
//					//Update the FnWorkflowList Object
//					fnWorkflowList.setErrorFlag(1);
//					fnWorkflowList.setErrorMessage("The Process, Step, Property Name and Property Value combination is not an allowed function, so there are No Workflows");
//					//Update the FnBaseXML Object
//					fnBaseXML.setErrorFlag(1);
//					fnBaseXML.setErrorMessage("The Process, Step, Property Name and Property Value combination is not an allowed function, so there are No Workflows");
//				}
//
//				//Logoff the Workflow Server
//				log.info("Logging off the Workflow Server");
//				vwSession.logoff();
//				//Release the VWSession
//				vwSession = null;
//				log.info("Logged off");
//			}
//			else
//			{
//				log.info("Workflow Login FAILED, Workflow Server may be unavailable.");
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
//			log.info("ERROR", ex.getMessage());
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
//		log.info("Leaving WorkflowManager -> getWorkflowCountsList()");
//		log.info("===========================================================");
//		return fnBaseXML;
	}
	
	private FnWorkflow updateFnWorkflowInfo(VWStepElement stepElement, String workflowType)
	{
		//Create the FnWorkflow Object
		FnWorkflow fnWorkflow = new FnWorkflow();

		try
		{
			log.info("Entered WorkflowManager -> updateFnWorkflowInfo()");
			log.info("===========================================================");
			//Workflow Name
			log.info("Workflow Name: " + stepElement.getWorkflowName());
			fnWorkflow.setFnWorkflowName(stepElement.getWorkflowName());
			//Workflow Process
			log.info("Workflow Process: " + stepElement.getRosterName());
			fnWorkflow.setFnWorkflowProcess(stepElement.getRosterName());
			//Workflow Activity/Step
			log.info("Workflow Activity: " + stepElement.getQueueName());
			fnWorkflow.setFnWorkflowStep(stepElement.getQueueName());
			//Workflow Roster
			log.info("Workflow Roster: " + stepElement.getRosterName());
			fnWorkflow.setFnWorkflowRoster(stepElement.getRosterName());
			//Workflow Queue
			log.info("Workflow Queue: " + stepElement.getQueueName());
			fnWorkflow.setFnWorkflowQueue(stepElement.getQueueName());

			/*//Workflow Object
			log.info("Workflow Object Name: " + stepElement.getWorkObjectName());
			fnWorkflow.setFnWorkflowObject(stepElement.getWorkObjectName());*/

			//Workflow ID
			log.info("Workflow ID: " + stepElement.getWorkObjectNumber());
			fnWorkflow.setFnWorkflowID(stepElement.getWorkObjectNumber());
			//Workflow Originator
			log.info("Workflow Originator: " + stepElement.getOriginator());
			//Workflow Participant
			log.info("Workflow Participant: " + stepElement.getParticipantName());
			fnWorkflow.setFnWorkflowUser(stepElement.getParticipantName());

			//Check the WorkflowType - New or Existing
			if (!workflowType.equals("New"))
			{
				log.info("Workflow Date Received: " + stepElement.getDateReceived().toString());
			}

			log.info("===========================================================");
		}
		catch (VWException ex)
		{
			log.info("ERROR", ex.getMessage());
			//Update ErrorFlag
			fnWorkflow.setErrorFlag(1);
			//Update ErrorMessage
			//fnWorkflow.setErrorMessage(ex.getMessage());
		}
		log.info("Leaving WorkflowManager -> updateFnWorkflowInfo()");
		log.info("===========================================================");
		return fnWorkflow;
	}

	private FnWorkflowPropertyList updateFnWorkflowPropertyListInfo(VWStepElement stepElement)
	{
		//Create the FnWorkflowPropertyList Object
		FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();

		try
		{
			log.info("Entered WorkflowManager -> updateFnWorkflowPropertyListInfo()");
			//Get the VWStepElement System and User Defined Data Fields
			//VWParameter[] vwParametersData = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_AND_SYSTEM_DEFINED);
			
			//Get the VWStepElement User Defined Data Fields
			VWParameter[] vwParametersData = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_DEFINED);
			
			//Get the name, type, mode and value for each VWParameter
			for (int i = 0; i < vwParametersData.length; i++)
			{
				//Create the FnWorkflowProperty Object
				FnWorkflowProperty fnWorkflowProperty = new FnWorkflowProperty();
				//Get the Parameter Name
				String vwParameterName = vwParametersData[i].getName();
				fnWorkflowProperty.setName(vwParameterName);
				log.info("VWParameterName: " + vwParameterName);
				//Get the Parameter Value
				String vwParameterValue = vwParametersData[i].getStringValue();
				fnWorkflowProperty.setValue(vwParameterValue);
				log.info("VWParameterValue: " + vwParameterValue);
				//Add the FnWorkflowProperty to the FnWorkflowPropertyList
				fnWorkflowPropertyList.addFnWorkflowProperty(fnWorkflowProperty);
			}
		}
		catch (VWException ex)
		{
			log.info("ERROR", ex.getMessage());
			//Update ErrorFlag
			fnWorkflowPropertyList.setErrorFlag(1);
			//Update ErrorMessage
			//fnWorkflowPropertyList.setErrorMessage(ex.getMessage());
		}
		log.info("Leaving WorkflowManager -> updateFnWorkflowPropertyListInfo()");
		log.info("===========================================================");
		return fnWorkflowPropertyList;
	}

	private void doStepElementAttachment(VWStepElement stepElement, VWParameter vwParam, String fnDocument)
	{
//		boolean attachmentAdded = false;
//		//Create a List from the FnWorkflowRequest variable
//		//List<FnWorkflowProperty> fnWorkflowPropertyListRequest = fnWorkflowRequest.getFnWorkflowPropsList();
//		//Create the FnWorkflowProperty Object
//		FnWorkflowProperty fnWorkflowPropertyResult = new FnWorkflowProperty();
//		//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//
//		try
//		{
//			log.info("Entered WorkflowManager -> doStepElementAttachment()");
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
//							log.info("1 Attachment");
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
//								log.info("Attachment Added");
//							}
//							else
//							{
//								//Update the boolean
//								attachmentAdded = false;
//								log.info("Attachment Failed");
//							}
//						}
//					}
//					else
//					{
//						//VWParam is an array of Attachments
//						// Get the value for the VWAttachment[]
//						VWAttachment[] attachments = (VWAttachment[]) vwParam.getValue();
//						VWAttachment[] updatedAttachments = new VWAttachment[attachments.length];
//						log.info("Multiple Attachments " + attachments.length);
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
//								log.info("Split ID list");
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
//						log.info("Attachment Added");
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
//			log.info("ERROR", ex.getMessage());
//			log.info("Attachment Failed");
//			//Update the boolean
//			attachmentAdded = false;
//		}
//		log.info("Leaving WorkflowManager -> doStepElementAttachment()");
//		log.info("===========================================================");
//		return attachmentAdded;
	}

	private void doStepElementParticipant(VWStepElement stepElement, VWParameter vwParam, String fnWorkflowRequest)
	{
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
//			log.info("Entered WorkflowManager -> doStepElementParticipant()");
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
//					log.info("Participant Field Name: " + vwParam);
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
//			log.info("ERROR", ex.getMessage());
//		}
//		log.info("Leaving WorkflowManager -> doStepElementParticipant()");
//		log.info("===========================================================");
//		return fnWorkflowPropertyResult;
	}

	private void doStepElementDataFields(VWStepElement stepElement, String fnWorkflowRequest)
	{
//		//Create the FnWorkflowPropertyList
//		FnWorkflowPropertyList fnWorkflowPropertyListResult = new FnWorkflowPropertyList();
//		
//		try
//		{
//			log.info("Entered WorkflowManager -> doStepElementDataFields()");
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
//					log.info("Updating Workflow Data Field: " + vwParam.getName());
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
//			log.info("ERROR", ex.getMessage());
//			//Update ErrorFlag
//			fnWorkflowPropertyListResult.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowPropertyListResult.setErrorMessage(ex.getMessage());
//		}
//		log.info("Leaving WorkflowManager -> doStepElementDataFields()");
//		log.info("===========================================================");
//		return fnWorkflowPropertyListResult;
	}

	private void doStepElementDataField(VWStepElement stepElement, VWParameter vwParam, String fnWorkflowRequest)
	{
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
//			log.info("Entered WorkflowManager -> doStepElementDataField()");
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
//								//log.info("Updating " + fnWorkflowPropertyRequest.getName());
//								log.info("Updating Workflow Data Value: " + fnWorkflowPropertyRequest.getValue());
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
//								//log.info("Updating " + fnWorkflowPropertyRequest.getName());
//								log.info("Updating Workflow Data Value: " + fnWorkflowPropertyRequest.getValue());
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
//			log.info("ERROR", ex.getMessage());
//		}
//		log.info("Leaving WorkflowManager -> doStepElementDataField()");
//		log.info("===========================================================");
//		return fnWorkflowPropertyResult;
	}

	private void updateStepElement(VWStepElement stepElement, String fnWorkflowRequest, Map<String, String> stepElementParamsMap, String stepElementAction, String stepElementActionValue)
	{
//		FnWorkflow fnWorkflowResult = new FnWorkflow();
//		FnWorkflowPropertyList fnWorkflowPropertyListResult = new FnWorkflowPropertyList();
//		
//		try
//		{
//			log.info("Entered WorkflowManager -> updateStepElement()");
//			
//			//If Attachment, call doStepElementAttachment and doStepElementAction
//			if (stepElementAction.equals("Attachment"))
//			{
//				log.info("Attachment");
//				//Not implemented yet
//			}
//			//If Participant, call doStepElementParticipant and doStepElementAction
//			else if (stepElementAction.equals("Participant"))
//			{
//				log.info("Participant");
//				//Not implemented yet
//			}
//			//If DataFields, call doStepElementDataFields and doStepElementAction
//			else if (stepElementAction.equals("DataFields"))
//			{
//				log.info("DataFields");
//				//Lock the Step Element
//				stepElement.doLock(true);
//
//				//Call doStepElementDataFields
//				fnWorkflowPropertyListResult =  doStepElementDataFields(stepElement, fnWorkflowRequest, wiiscLog);
//
//				//End of For Loop
//				log.info("===========================================");
//
//				// Set the value for the system-defined Response parameter
//				if (stepElement.getStepResponses() != null) {
//					log.info("Step Responses");
//					String responseValue = "Ok";
//					stepElement.setSelectedResponse(responseValue);
//				}
//				else
//				{
//					log.info("No Step Responses");
//					log.info("===========================================");
//				}
//
//				//DoStepElementAction - Save & Unlock the Workflow
//				fnWorkflowResult = doStepElementAction(stepElement, "Save", "", wiiscLog);
//			}
//			//If Parameters, call doStepElementParameterFields and doStepElementAction
//			else if (stepElementAction.equals("Parameters-Save"))
//			{
//				log.info("Parameters-Save");
//				if (stepElementParamsMap.size() > 0)
//				{
//					//Lock the Step Element
//					stepElement.doLock(true);
//
//					//Update the Step Element
//					for (Map.Entry<String, String> entry : stepElementParamsMap.entrySet()) {
//						log.info("Entry: " + entry.getKey() + " Value: " + entry.getValue());
//						stepElement.setParameterValue(entry.getKey(), entry.getValue(), true);
//					}
//
//					log.info("===========================================");
//
//					// Set the value for the system-defined Response parameter
//					if (stepElement.getStepResponses() != null) {
//						log.info("Step Responses");
//						String responseValue = "Ok";
//						stepElement.setSelectedResponse(responseValue);
//					}
//					else
//					{
//						log.info("No Step Responses");
//						log.info("===========================================");
//					}
//
//					//DoStepElementAction - Save & Unlock the Workflow
//					fnWorkflowResult = doStepElementAction(stepElement, "Save", "", wiiscLog);
//				}
//			}
//			//If Parameters, call doStepElementParameterFields and doStepElementAction
//			else if (stepElementAction.equals("Parameters-Dispatch"))
//			{
//				log.info("Parameters-Dispatch");
//				if (stepElementParamsMap.size() > 0)
//				{
//					//Lock the Step Element
//					stepElement.doLock(true);
//
//					//Update the Step Element
//					for (Map.Entry<String, String> entry : stepElementParamsMap.entrySet()) {
//						log.info("Entry: " + entry.getKey() + " Value: " + entry.getValue());
//						stepElement.setParameterValue(entry.getKey(), entry.getValue(), true);
//					}
//
//					log.info("===========================================");
//
//					// Set the value for the system-defined Response parameter
//					if (stepElement.getStepResponses() != null) {
//						log.info("Step Responses");
//						String responseValue = "Ok";
//						stepElement.setSelectedResponse(responseValue);
//					}
//					else
//					{
//						log.info("No Step Responses");
//						log.info("===========================================");
//					}
//
//					//DoStepElementAction - Save & Unlock the Workflow & Dispatch
//					fnWorkflowResult = doStepElementAction(stepElement, "Dispatch", "", wiiscLog);
//				}
//			}
//			//If Save, call doStepElementDataFields and doStepElementAction
//			else if (stepElementAction.equals("Save"))
//			{
//				log.info("Save");
//				//Lock the Step Element
//				stepElement.doLock(true);
//
//				//Call doStepElementDataFields
//				fnWorkflowPropertyListResult =  doStepElementDataFields(stepElement, fnWorkflowRequest, wiiscLog);
//
//				//End of For Loop
//				log.info("===========================================");
//
//				// Set the value for the system-defined Response parameter
//				if (stepElement.getStepResponses() != null) {
//					log.info("Step Responses");
//					String responseValue = "Ok";
//					stepElement.setSelectedResponse(responseValue);
//				}
//				else
//				{
//					log.info("No Step Responses");
//					log.info("===========================================");
//				}
//
//				//DoStepElementAction - Save & Unlock the Workflow
//				fnWorkflowResult = doStepElementAction(stepElement, stepElementAction, stepElementActionValue, wiiscLog);
//			}
//			//If Dispatch, call doStepElementDataFields and doStepElementAction
//			else if (stepElementAction.equals("Dispatch"))
//			{
//				log.info("Dispatch");
//				//Lock the Step Element
//				stepElement.doLock(true);
//
//				//Call doStepElementDataFields
//				fnWorkflowPropertyListResult =  doStepElementDataFields(stepElement, fnWorkflowRequest, wiiscLog);
//
//				//End of For Loop
//				log.info("===========================================");
//
//				// Set the value for the system-defined Response parameter
//				if (stepElement.getStepResponses() != null) {
//					log.info("Step Responses");
//					String responseValue = "Ok";
//					stepElement.setSelectedResponse(responseValue);
//				}
//				else
//				{
//					log.info("No Step Responses");
//					log.info("===========================================");
//				}
//
//				//DoStepElementAction - Save & Unlock the Workflow & Dispatch
//				fnWorkflowResult = doStepElementAction(stepElement, stepElementAction, stepElementActionValue, wiiscLog);
//			}
//			//If any other value, call doStepElementAction
//			else
//			{
//				log.info("Other");
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
//			log.info("ERROR", ex.getMessage());
//			//Update ErrorFlag
//			fnWorkflowPropertyListResult.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowPropertyListResult.setErrorMessage(ex.getMessage());
//		}
//		log.info("Leaving WorkflowManager -> updateStepElement()");
//		log.info("===========================================================");
//		return fnWorkflowResult;
	}
	
	private void doStepElementAction(VWStepElement stepElement, String action, String actionValue)
	{
//		//Create the FnWorkflow Object
//		FnWorkflow fnWorkflowResult = new FnWorkflow();
//		//Create the FnWorkflowPropertyList Object
//		//FnWorkflowPropertyList fnWorkflowPropertyListResult = new FnWorkflowPropertyList();
//
//		try
//		{
//			log.info("Entered WorkflowManager -> doStepElementAction()");
//			//Check the Action
//			if (action.equals("Reassign"))
//			{
//				log.info("Reassign");
//				// Determine whether a step element
//				// can be reassigned and reassign it
//				if (stepElement.getCanReassign())
//				{
//					log.info("Able to Reassign");
//					String participantName = actionValue;
//					stepElement.doReassign(participantName,false,null);
//				}
//			}
//			else if (action.equals("Return"))
//			{
//				log.info("Return");
//				// Determine whether a step element can be returned to the
//				// queue from which the user delegated or reassigned it and
//				// return it
//				if (stepElement.getCanReturnToSource())
//				{
//					log.info("Able to Return");
//					stepElement.doReturnToSource();
//				}
//			}
//			else if (action.equals("Move"))
//			{
//				log.info("Move");
//				// Determine whether a step element
//				// can be reassigned and reassign it
//				//
//				if (stepElement.getCanReassign())
//				{
//					log.info("Able to Move");
//					String participantName = actionValue;
//					stepElement.doReassign(participantName,false,"Inbox");
//				}
//			}
//			else if (action.equals("Abort"))
//			{
//				log.info("Abort");
//				// Cancel the changes to the work item
//				// without advancing it in the workflow
//				stepElement.doAbort();
//			}
//			else if (action.equals("Save"))
//			{
//				log.info("Save");
//				// Save the changes to the work item
//				// and unlock it without advancing it in the workflow
//				stepElement.doSave(true);
//			}
//			else if (action.equals("Dispatch"))
//			{
//				log.info("Dispatch");
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
//			log.info("ERROR", ex.getMessage());
//			//Update ErrorFlag
//			fnWorkflowResult.setErrorFlag(1);
//			//Update ErrorMessage
//			fnWorkflowResult.setErrorMessage(ex.getMessage());
//		}
//		log.info("Leaving WorkflowManager -> doStepElementAction()");
//		log.info("===========================================================");
//		return fnWorkflowResult;
	}

	private FnWorkflowList getQueueDBQuery(SQLServerConnection con, VWQueue vwQueue, String queueName, String[] filterName, String[] filterValue)
	{
		//DB Query to the FileNet Queue to get the results
		//Create the FnWorkflowList
		FnWorkflowList fnWorkflowList = new FnWorkflowList();
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			log.info("Entered WorkflowManager -> getQueueDBQuery()");
			//Workflow Region
			String workflowRegion = "";
			//Workflow Search Results Limit
			String workflowSearchLimit = "";
			//Workflow Data Fields
			List<String> workflowDataFields = new ArrayList<String>();
			//Workflow Data Fields FnWorkflowPropertyList
			FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
			//SQL Query
			String SQL = "";
						
			//Get some Workflow Information
			workflowRegion = appConfig.getPeWorkflowRegion();
			workflowSearchLimit = appConfig.getWorkflowSearchLimit();
			
			workflowDataFields = appConfig.getWorkflowDataFields();
			
			log.info("Queue: " + queueName);
			log.info("FilterName[0]: " + filterName[0]);
			log.info("FilterValue[0]: " + filterValue[0]);
			log.info("Workflow Region: " + workflowRegion);
									
			//Build the Properties to retrieve from the SQL Query
			if (workflowDataFields != null && workflowDataFields.size() > 0)
			{
				for(String dataField : workflowDataFields) {
					FnWorkflowProperty fnWorkflowProperty = new FnWorkflowProperty();
					fnWorkflowProperty.setName(dataField);
					fnWorkflowPropertyList.addFnWorkflowProperty(fnWorkflowProperty);
				}
			}
			
			//Output FnWorkflowPropertyList
			//outputFnWorkflowPropertyList(fnWorkflowPropertyList);
			
			//Check Queue Field Names
			VWQueueDefinition vwQueueDef = vwQueue.fetchQueueDefinition();
			List<FnWorkflowProperty> fnWorkflowProps = fnWorkflowPropertyList.getFnWorkflowPropsList();
			FnWorkflowPropertyList validFnWorkflowPropertyList = new FnWorkflowPropertyList();
			
			for (int i = 0; i < fnWorkflowProps.size(); i++)
			{
				FnWorkflowProperty fnWorkflowProp = fnWorkflowProps.get(i);
				//Check for the Queue Field
				if (vwQueueDef.hasFieldName(fnWorkflowProp.getName()))
				{
					//Add the FnWorkflowProperty to the validFnWorkflowPropertyList
					validFnWorkflowPropertyList.addFnWorkflowProperty(fnWorkflowProp);
				}
			}
			
			//Clear the Temp List
			fnWorkflowProps.clear();
			
			//Update FnWorkflowPropertyList
			if (validFnWorkflowPropertyList.getCount() > 0)
			{
				//Clear the old FnWorkflowPropertyList
				fnWorkflowPropertyList.getFnWorkflowPropsList().clear();
				//Add the updated FnWorkflowPropertyList
				fnWorkflowPropertyList.addFnWorkflowPropertyList(validFnWorkflowPropertyList);
			}
			
			//Output FnWorkflowPropertyList
			//outputFnWorkflowPropertyList(fnWorkflowPropertyList, wiiscLog);
			
			//Build the SQL Query Statement
			if (filterName.length > 0)
			{
				log.info("FilterName is being used");
				//Check FnWorkflowPropertyList
				if (fnWorkflowPropertyList.getCount() > 0)
				{
					String params = "";
					fnWorkflowProps = fnWorkflowPropertyList.getFnWorkflowPropsList();
					for (int i = 0; i < fnWorkflowProps.size(); i++)
					{
						if (params.length() == 0)
						{
							params = fnWorkflowProps.get(i).getName();
						}
						else
						{
							params = params + "," + fnWorkflowProps.get(i).getName();
						}
					}
					log.info("Select Params: " + params);
					//Update SQL Query
					if (workflowSearchLimit.length() == 0)
					{
						SQL = "SELECT " + params + " FROM VWVQ" + workflowRegion + "_" + queueName + " where " + filterName[0] + " = '" + filterValue[0] + "'";
					}
					else
					{
						SQL = "SELECT TOP " + workflowSearchLimit + " " + params + " FROM VWVQ" + workflowRegion + "_" + queueName + " where " + filterName[0] + " = '" + filterValue[0] + "'";
					}
				}
				else
				{
					//Update SQL Query
					if (workflowSearchLimit.length() == 0)
					{
						SQL = "SELECT * FROM VWVQ" + workflowRegion + "_" + queueName + " where " + filterName[0] + " = '" + filterValue[0] + "'";
					}
					else
					{
						SQL = "SELECT TOP " + workflowSearchLimit + " * FROM VWVQ" + workflowRegion + "_" + queueName + " where " + filterName[0] + " = '" + filterValue[0] + "'";
					}
				}
			}
			else
			{
				log.info("FilterName is NOT being used");
				//Check FnWorkflowPropertyList
				if (fnWorkflowPropertyList.getCount() > 0)
				{
					String params = "";
					fnWorkflowProps = fnWorkflowPropertyList.getFnWorkflowPropsList();
					for (int i = 0; i < fnWorkflowProps.size(); i++)
					{
						if (params.length() == 0)
						{
							params = fnWorkflowProps.get(i).getName();
						}
						else
						{
							params = params + "," + fnWorkflowProps.get(i).getName();
						}
					}
					log.info("Select Params: " + params);
					//Update SQL Query
					if (workflowSearchLimit.length() == 0)
					{
						SQL = "SELECT " + params + " FROM VWVQ" + workflowRegion + "_" + queueName;
					}
					else
					{
						SQL = "SELECT TOP " + workflowSearchLimit + " " + params + " FROM VWVQ" + workflowRegion + "_" + queueName;
					}
				}
				else
				{
					//Update SQL Query
					if (workflowSearchLimit.length() == 0)
					{
						SQL = "SELECT * FROM VWVQ" + workflowRegion + "_" + queueName;
					}
					else
					{
						SQL = "SELECT TOP " + workflowSearchLimit + " * FROM VWVQ" + workflowRegion + "_" + queueName;
					}
				}
			}
			log.info("SQL Query: " + SQL); 

			//Execute an SQL statement that returns some data.
			stmt = con.createStatement();
			rs = stmt.executeQuery(SQL);

			// Iterate through the data in the result set and display it.
			while (rs.next())
			{
				String propName = "";
				String propValue = "";
				FnWorkflow fnWorkflow = new FnWorkflow();
				FnWorkflowPropertyList fnWorkflowPropertyListResult = new FnWorkflowPropertyList();
				
				List<FnWorkflowProperty> fnWorkflowPropsResult = fnWorkflowPropertyList.getFnWorkflowPropsList();
				for (int i = 0; i < fnWorkflowPropsResult.size(); i++)
				{
					//Get Prop Name
					propName = fnWorkflowPropsResult.get(i).getName();
					propValue = rs.getString(propName);
					log.info("Property Name: " + propName);
					log.info("Property Value: " + propValue);
					//Create the FnWorkflowProperty
					FnWorkflowProperty fnWorkflowProperty = new FnWorkflowProperty();
					//Set the FnWorkflowProperty Name
					fnWorkflowProperty.setName(propName);
					//Set the FnWorkflowProperty Value from the DB
					fnWorkflowProperty.setValue(propValue);
					//Add the FnWorkflowProperty to the FnWorkflowPropertyListResult
					fnWorkflowPropertyListResult.addFnWorkflowProperty(fnWorkflowProperty);
				}
				//Separate the Workflow Results
				log.info("===========================================================");
				if (fnWorkflowPropertyListResult.getCount() > 0)
				{
					//Add the FnWorkflowPropertyListResult to the FnWorkflow
					fnWorkflow.setFnWorkflowPropertyList(fnWorkflowPropertyListResult);
					//Add the FnWorkflow to the FnWorkflowList
					fnWorkflowList.addFnWorkflow(fnWorkflow);
				}
				else
				{
					//Add the FnWorkflow to the FnWorkflowList
					fnWorkflowList.addFnWorkflow(fnWorkflow);
				}
			}
			log.info("FileNet Workflow List from DB Query Finished");
		}

		// Handle any errors that may have occurred.
		catch (Exception e)
		{
			//e.printStackTrace();
			log.info("ERROR", e.getMessage());
		}
		finally
		{
			if (rs != null)
			{
				try
				{
					log.info("Closing ResultSet");
					rs.close();
				}
				catch(Exception e)
				{
					log.info("Error closing ResultSet");
					log.info("ERROR", e.getMessage());
				}
			}
			if (stmt != null)
			{
				try
				{
					log.info("Closing Statement");
					stmt.close();
				}
				catch(Exception e)
				{
					log.info("Error closing Statement");
					log.info("ERROR", e.getMessage());
				}
			}
		}
		log.info("Leaving WorkflowManager -> getQueueDBQuery()");
		log.info("===========================================================");
		return fnWorkflowList;
	}
	
	private VWQueueQuery getQueueQuery(VWQueue vwQueue, String[] filterName, String[] filterValue, String sortBy)
	{
		//Create the VWQueueQuery Object
		VWQueueQuery query = null;

		try
		{
			log.info("Entered WorkflowManager -> getQueueQuery()");
			//Fetch Count
			int fetchCount = 0;
			//Query Flags
			int queryFlags = VWQueue.QUERY_NO_OPTIONS;
			String queryFilter="";
			//Object[] substitutionVars = null;
			int fetchType = VWFetchType.FETCH_TYPE_QUEUE_ELEMENT;

			//Update Queue Buffer Size
			fetchCount = vwQueue.fetchCount();
			//Make sure fetchCount is > 0, otherwise skip
			if (fetchCount > 0)
			{
				//Setup the BufferSize for the Query
				if (fetchCount > 200)
				{
					vwQueue.setBufferSize(200);
					log.info("Buffer Size set to 200");
				}
				else if (fetchCount > 50 && fetchCount < 200)
				{
					vwQueue.setBufferSize(fetchCount);
					log.info("Buffer Size set to " + fetchCount);
				}
				else
				{
					vwQueue.setBufferSize(50);
					log.info("Buffer Size set to 50");
				}
				
				//Check if FilterName and FilterValue are Valid
				if (filterName != null && filterValue != null)
				{
					//Setup variables for Query
					Object[] substitutionVars = null;
					Object[] minValues = null;
					Object[] maxValues = null;
					
					//Check if Query Filter, Substitution Vars, Min and Max need updated
					if (filterName.length == 3 && filterValue.length == 3)
					{
						log.info("Filter Name and Value is Length of 3");
						log.info("filterName[0]: " + filterName[0]);
						log.info("filterValue[0]: " + filterValue[0]);
						log.info("filterName[1]: " + filterName[1]);
						log.info("filterValue[1]: " + filterValue[1]);
						log.info("filterName[2]: " + filterName[2]);
						log.info("filterValue[2]: " + filterValue[2]);
						//Used for Filtered Query
						//32 + 64
						//queryFlags = VWQueue.QUERY_MIN_VALUES_INCLUSIVE + VWQueue.QUERY_MAX_VALUES_INCLUSIVE;
						//0
						queryFlags = VWQueue.QUERY_NO_OPTIONS;
						
						queryFilter = filterName[0] + "=:A AND " + filterName[1] + "=:B AND " + filterName[2] + "=:C";
						substitutionVars = new String[3];
						substitutionVars[0] = filterValue[0];
						substitutionVars[1] = filterValue[1];
						substitutionVars[2] = filterValue[2];
						
						minValues = new String[3];
						minValues[0] = filterValue[0];
						minValues[1] = filterValue[1];
						minValues[2] = filterValue[2];
						
						maxValues = new String[3];
						maxValues[0] = filterValue[0];
						maxValues[1] = filterValue[1];
						maxValues[2] = filterValue[2];

						//Run the Queue Query
						//query = vwQueue.createQuery(null, minValues, maxValues, queryFlags, queryFilter, substitutionVars, fetchType);
						//query = vwQueue.createQuery("Indx_" + filterName[0], null, null, queryFlags, queryFilter, substitutionVars, fetchType);
						//query = vwQueue.createQuery(null, null, null, queryFlags, queryFilter, substitutionVars, fetchType);					
					}
					else if (filterName.length == 2 && filterValue.length == 2)
					{
						log.info("Filter Name and Value is Length of 2");
						log.info("filterName[0]: " + filterName[0]);
						log.info("filterValue[0]: " + filterValue[0]);
						log.info("filterName[1]: " + filterName[1]);
						log.info("filterValue[1]: " + filterValue[1]);
						//Used for Filtered Query
						//32 + 64
						//queryFlags = VWQueue.QUERY_MIN_VALUES_INCLUSIVE + VWQueue.QUERY_MAX_VALUES_INCLUSIVE;
						//0
						queryFlags = VWQueue.QUERY_NO_OPTIONS;
						
						queryFilter = filterName[0] + "=:A AND " + filterName[1] + "=:B";
						substitutionVars = new String[2];
						substitutionVars[0] = filterValue[0];
						substitutionVars[1] = filterValue[1];
												
						minValues = new String[2];
						minValues[0] = filterValue[0];
						minValues[1] = filterValue[1];
												
						maxValues = new String[2];
						maxValues[0] = filterValue[0];
						maxValues[1] = filterValue[1];
												
						//Run the Queue Query
						//query = vwQueue.createQuery(null, minValues, maxValues, queryFlags, queryFilter, substitutionVars, fetchType);
						//query = vwQueue.createQuery("Indx_" + filterName[0] + "+" + "Indx_" + filterName[1], null, null, queryFlags, queryFilter, substitutionVars, fetchType);
						//query = vwQueue.createQuery(null, null, null, queryFlags, queryFilter, substitutionVars, fetchType);
					}
					else if (filterName.length == 1 && filterValue.length == 1)
					{
						log.info("Filter Name and Value is Length of 1");
						log.info("filterName[0]: " + filterName[0]);
						log.info("filterValue[0]: " + filterValue[0]);
						//Used for Filtered Query
						//32 + 64
						//queryFlags = VWQueue.QUERY_MIN_VALUES_INCLUSIVE + VWQueue.QUERY_MAX_VALUES_INCLUSIVE;
						//0
						queryFlags = VWQueue.QUERY_NO_OPTIONS;
						//queryFlags = VWQueue.QUERY_GET_NO_SYSTEM_FIELDS + VWQueue.QUERY_GET_NO_TRANSLATED_SYSTEM_FIELDS;
						
						queryFilter = filterName[0] + " = :A";
						substitutionVars = new String[1];
						substitutionVars[0] = filterValue[0];
												
						minValues = new String[1];
						minValues[0] = filterValue[0];
												
						maxValues = new String[1];
						maxValues[0] = filterValue[0];
												
						//Run the Queue Query
						//query = vwQueue.createQuery(null, minValues, maxValues, queryFlags, queryFilter, substitutionVars, fetchType);
						//query = vwQueue.createQuery("Indx_" + filterName[0], null, null, queryFlags, queryFilter, substitutionVars, fetchType);
						//query = vwQueue.createQuery(null, null, null, queryFlags, queryFilter, substitutionVars, fetchType);
					}
					else
					{
						//Do Nothing
					}
					
					//Setup the Query and Check if sortBy is being used
					if (sortBy.length() == 0)
					{
						//sortBy was empty
						log.info("sortBy: " + sortBy);
						query = vwQueue.createQuery(null, null, null, queryFlags, queryFilter, substitutionVars, fetchType);
					}
					else
					{
						//sortBy was not empty
						log.info("sortBy: " + sortBy);
						query = vwQueue.createQuery("Indx_" + sortBy, null, null, queryFlags, queryFilter, substitutionVars, fetchType);
					}
				}
				else
				{
					log.info("Filter Name and Value is Length of Null");
					
					//Used to Return All Workflows with No Filter
					//Setup the Query and Check if sortBy is being used
					if (sortBy.length() == 0)
					{
						//sortBy was empty
						log.info("sortBy: " + sortBy);
						query = vwQueue.createQuery(null, null, null, queryFlags, queryFilter, null, fetchType);
					}
					else
					{
						//sortBy not was empty
						log.info("sortBy: " + sortBy);
						query = vwQueue.createQuery("Indx_" + sortBy, null, null, queryFlags, queryFilter, null, fetchType);
					}
				}
			}
			else
			{
				log.info("Queue has No Records so a query will not be performed");
				query = null;
			}
		}
		catch (VWException ex)
		{
			log.info("ERROR", ex.getMessage());
			query = null;
		}
		log.info("Leaving WorkflowManager -> getQueueQuery()");
		log.info("===========================================================");
		return query;
	}

	private void getQueueQueryByWobNum(VWQueue vwQueue, String wobNum)
	{
//		//Create the VWQueueQuery Object
//		VWQueueQuery query = null;
//
//		try
//		{
//			log.info("Entered WorkflowManager -> getQueueQueryByWobNum()");
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
//				log.info("WobNum was missing from the request");
//				query = null;
//			}
//		}
//		catch (VWException ex)
//		{
//			log.info("ERROR", ex.getMessage());
//			query = null;
//		}
//		log.info("Leaving WorkflowManager -> getQueueQueryByWobNum()");
//		log.info("===========================================================");
//		return query;
	}

	private void getRosterQueryByWobNum(VWRoster vwRoster, String wobNum)
	{
//		//Create the VWRosterQuery Object
//		VWRosterQuery query = null;
//
//		try
//		{
//			log.info("Entered WorkflowManager -> getRosterQueryByWobNum()");
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
//				log.info("WobNum was missing from the request");
//				query = null;
//			}
//		}
//		catch (VWException ex)
//		{
//			log.info("ERROR", ex.getMessage());
//			query = null;
//		}
//		log.info("Leaving WorkflowManager -> getRosterQueryByWobNum()");
//		log.info("===========================================================");
//		return query;
	}

	private VWRosterQuery getRosterQuery(VWRoster vwRoster, String[] filterName, String[] filterValue)
	{
		//Create the VWRosterQuery Object
		VWRosterQuery query = null;

		try
		{
			log.info("Entered WorkflowManager -> getRosterQuery()");
			//Fetch Count
			int fetchCount = 0;
			//Query Flags
			int queryFlags = VWRoster.QUERY_NO_OPTIONS;
			String queryFilter="";
			//Object[] substitutionVars = null;
			int fetchType = VWFetchType.FETCH_TYPE_ROSTER_ELEMENT;

			//Update Roster Buffer Size
			fetchCount = vwRoster.fetchCount();
			//Make sure fetchCount is > 0, otherwise skip
			if (fetchCount > 0)
			{
				log.info("Roster Count was " + fetchCount);
				if (fetchCount > 200)
				{
					vwRoster.setBufferSize(200);
				}
				else if (fetchCount > 50 && fetchCount < 200)
				{
					vwRoster.setBufferSize(fetchCount);
				}
				else
				{
					vwRoster.setBufferSize(50);
				}
				
				//Check if FilterName and FilterValue are Valid
				if (filterName != null && filterValue != null)
				{
					//Check if Query Filter, Substitution Vars, Min and Max need updated
					if (filterName.length == 3 && filterValue.length == 3)
					{
						log.info("Filter Name and Value is Length of 3");
						log.info("filterName[0]: " + filterName[0]);
						log.info("filterValue[0]: " + filterValue[0]);
						log.info("filterName[1]: " + filterName[1]);
						log.info("filterValue[1]: " + filterValue[1]);
						log.info("filterName[2]: " + filterName[2]);
						log.info("filterValue[2]: " + filterValue[2]);
						//Used for Filtered Query
						//32 + 64
						//queryFlags = VWRoster.QUERY_MIN_VALUES_INCLUSIVE + VWRoster.QUERY_MAX_VALUES_INCLUSIVE;
						//0
						queryFlags = VWRoster.QUERY_NO_OPTIONS;
						
						queryFilter = filterName[0] + "=:A AND " + filterName[1] + "=:B AND " + filterName[2] + "=:C";
						Object[] substitutionVars = {filterValue[0], filterValue[1], filterValue[2]};
						Object[] minValues = {filterValue[0], filterValue[1], filterValue[2]};
						Object[] maxValues = {filterValue[0], filterValue[1], filterValue[2]};

						//Run the Roster Query
						//query = vwRoster.createQuery(null, minValues, maxValues, queryFlags, queryFilter, substitutionVars, fetchType);
						query = vwRoster.createQuery(null, null, null, queryFlags, queryFilter, substitutionVars, fetchType);
					}
					else if (filterName.length == 2 && filterValue.length == 2)
					{
						log.info("Filter Name and Value is Length of 2");
						log.info("filterName[0]: " + filterName[0]);
						log.info("filterValue[0]: " + filterValue[0]);
						log.info("filterName[1]: " + filterName[1]);
						log.info("filterValue[1]: " + filterValue[1]);
						//Used for Filtered Query
						//32 + 64
						//queryFlags = VWRoster.QUERY_MIN_VALUES_INCLUSIVE + VWRoster.QUERY_MAX_VALUES_INCLUSIVE;
						//0
						queryFlags = VWRoster.QUERY_NO_OPTIONS;
						
						queryFilter = filterName[0] + "=:A AND " + filterName[1] + "=:B";
						Object[] substitutionVars = {filterValue[0], filterValue[1]};
						Object[] minValues = {filterValue[0], filterValue[1]};
						Object[] maxValues = {filterValue[0], filterValue[1]};

						//Run the Roster Query
						//query = vwRoster.createQuery(null, minValues, maxValues, queryFlags, queryFilter, substitutionVars, fetchType);
						query = vwRoster.createQuery(null, null, null, queryFlags, queryFilter, substitutionVars, fetchType);
					}
					else if (filterName.length == 1 && filterValue.length == 1)
					{
						log.info("Filter Name and Value is Length of 1");
						log.info("filterName[0]: " + filterName[0]);
						log.info("filterValue[0]: " + filterValue[0]);
						//Used for Filtered Query
						//32 + 64
						//queryFlags = VWRoster.QUERY_MIN_VALUES_INCLUSIVE + VWRoster.QUERY_MAX_VALUES_INCLUSIVE;
						//0
						queryFlags = VWRoster.QUERY_NO_OPTIONS;
												
						queryFilter = filterName[0] + " = :A";
						Object[] substitutionVars = {filterValue[0]};
						Object[] minValues = {filterValue[0]};
						Object[] maxValues = {filterValue[0]};

						//Run the Roster Query
						//query = vwRoster.createQuery(null, minValues, maxValues, queryFlags, queryFilter, substitutionVars, fetchType);
						query = vwRoster.createQuery(null, null, null, queryFlags, queryFilter, substitutionVars, fetchType);
					}
					else
					{
						//Do Nothing
					}
				}
				else
				{
					log.info("Filter Name and Value is Length of Null");
					
					//Used to Return All Workflows with No Filter
					//Run the Roster Query
					query = vwRoster.createQuery(null, null, null, queryFlags, queryFilter, null, fetchType);
				}
			}
			else
			{
				log.info("Roster Count was " + fetchCount);
				log.info("Roster has No Records so a query will not be performed");
				query = null;
			}
		}
		catch (VWException ex)
		{
			log.info("ERROR", ex.getMessage());
			query = null;
		}
		log.info("Leaving WorkflowManager -> getRosterQuery()");
		log.info("===========================================================");
		return query;
	}

	private String[] getQueues(VWSession vwSession)
	{
		String[] queueNames = null;
		//VWQueue vwQueue = null;
		int qFlags = VWSession.QUEUE_PROCESS | VWSession.QUEUE_USER_CENTRIC | VWSession.QUEUE_IGNORE_SECURITY | VWSession.QUEUE_SYSTEM;
		int queueTotal = 0;
		//int wfCount = 0;

		try
		{
			log.info("Entered WorkflowManager -> getQueues()");
			//Retrieve List of Available Queues
			queueNames = vwSession.fetchQueueNames(qFlags);
			//Check the length of the QueueNames
			if (queueNames.length == 0)
			{
				log.info("There are NO Queues");
			}
			else
			{
				for (int i = 0; i < queueNames.length; i++)
				{
					//vwQueue = vwSession.getQueue(queueNames[i]);
					//wfCount = vwQueue.fetchCount();
					//log.info("Workflow Queue Count -> " + queueNames[i] + " = " + wfCount);
					queueTotal++;
				}
				log.info("There are " + queueTotal + " Queues");
			}
		}
		catch(VWException ex)
		{
			log.info("ERROR", ex.getMessage());
			if (vwSession != null)
			{
				//Set vwSession to null to kill any connections
				vwSession = null;
			}
			queueNames = null;
		}
		log.info("Leaving WorkflowManager -> getQueues()");
		log.info("===========================================================");
		return queueNames;
	}

	private void getRosters(VWSession vwSession)
	{
//		String[] rosterNames = null;
//		VWRoster vwRoster = null;
//		int rosterTotal = 0;
//		int wfCount = 0;
//
//		try
//		{
//			log.info("Entered WorkflowManager -> getRosters()");
//			//Retrieve List of Available Rosters
//			rosterNames = vwSession.fetchRosterNames(true);
//			//Check the length of the RosterNames
//			if (rosterNames.length == 0)
//			{
//				log.info("There are NO Rosters");
//			}
//			else
//			{
//				for (int i = 0; i < rosterNames.length; i++)
//				{
//					vwRoster = vwSession.getRoster(rosterNames[i]);
//					wfCount = vwRoster.fetchCount();
//					log.info("Workflow Roster Count -> " + rosterNames[i] + " = " + wfCount);
//					rosterTotal++;
//				}
//				log.info("There are " + rosterTotal + " Rosters");
//			}
//		}
//		catch(VWException ex)
//		{
//			log.info("ERROR", ex.getMessage());
//			if (vwSession != null)
//			{
//				//Set vwSession to null to kill any connections
//				vwSession = null;
//			}
//			rosterNames = null;
//		}
//		log.info("Leaving WorkflowManager -> getRosters()");
//		log.info("===========================================================");
//		return rosterNames;
	}

	private void getWorkflowStepElement(VWSession vwSession, String process, String step, String user, String propName, String propValue)
	{
//		//StepElement to Return
//		VWStepElement stepElement = null;
//		//Get the Filter Name
//		String[] filterName = null;
//		//Get the Filter Value
//		String[] filterValue = null;
//
//		try
//		{
//			log.info("Entered WorkflowManager -> getWorkflowStepElement()");
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
//				log.info("Get a Workflow by a Step, Property and Property Value");
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
//						log.info("Queue: " + queueName);
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
//								log.info("Query Not Null");
//								//Process the Results
//								while(query.hasNext())
//								{
//									log.info("Query Has Next");
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
//					log.info("There was No Queue, so there is No Workflow");
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
//				log.info("Get a Workflow by a Property and Property Value");
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
//							log.info("Queue: " + queueNames[i]);
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
//									log.info("Query Not Null");
//									//Process the Results
//									while(query.hasNext())
//									{
//										log.info("Query Has Next");
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
//					log.info("There were No Queues, so there are No Workflows");
//					//Update the StepElement value
//					stepElement = null;
//				}
//			}
//			else
//			{
//				//Return an Error
//				log.info("The Parameters Step, Property Name and Property Value are required to find the Workflow");
//				//Update the StepElement value
//				stepElement = null;
//			}
//		}
//		catch(VWException ex)
//		{
//			log.info("ERROR", ex.getMessage());
//			stepElement = null;
//		}
//
//		log.info("Leaving WorkflowManager -> getWorkflowStepElement()");
//		log.info("===========================================================");
//		return stepElement;
	}

	private void getWorkflowStep(VWSession vwSession, String step, String wobNum)
	{
//		//Create FnWorkflow
//		FnWorkflow fnWorkflow = new FnWorkflow();
//
//		try
//		{
//			log.info("Entered WorkflowManager -> getWorkflow()");
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
//							log.info("Failed to get a StepElement");
//							//Update the FnWorkflow Object
//							fnWorkflow.setErrorFlag(1);
//							fnWorkflow.setErrorMessage("Failed to get a StepElement");
//							fnWorkflow.setFnWorkflowStatus("Failed to get a StepElement");
//						}
//					}
//				}
//				else
//				{
//					log.info("Failed to get a QueueQuery");
//					//Update the FnWorkflow Object
//					fnWorkflow.setErrorFlag(1);
//					fnWorkflow.setErrorMessage("Failed to get a QueueQuery");
//					fnWorkflow.setFnWorkflowStatus("Failed to get a QueueQuery");
//				}
//			}
//			else
//			{
//				log.info("Failed to get a VWQueue");
//				//Update the FnWorkflow Object
//				fnWorkflow.setErrorFlag(1);
//				fnWorkflow.setErrorMessage("Failed to get a VWQueue");
//				fnWorkflow.setFnWorkflowStatus("Failed to get a VWQueue");
//			}
//		}
//		catch(VWException ex)
//		{
//			log.info("ERROR", ex.getMessage());
//			fnWorkflow = null;
//		}
//		log.info("Leaving WorkflowManager -> getWorkflowStep()");
//		log.info("===========================================================");
//		return fnWorkflow;
	}

	private void getWorkflow(VWSession vwSession, String process, String wobNum)
	{
//		//Create FnWorkflow
//		FnWorkflow fnWorkflowResult = new FnWorkflow();
//
//		try
//		{
//			log.info("Entered WorkflowManager -> getWorkflow()");
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
//							log.info("Found Workflow");
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
//								log.info("Failed to get a StepElement");
//								//Update the FnWorkflow Object
//								fnWorkflowResult.setErrorFlag(1);
//								fnWorkflowResult.setErrorMessage("Failed to get a StepElement");
//								fnWorkflowResult.setFnWorkflowStatus("Failed to get a StepElement");
//							}
//						}
//					}
//					else
//					{
//						log.info("Workflow has Finished.  No StepElement available.");
//						//Update the FnWorkflow Object
//						fnWorkflowResult.setErrorFlag(0);
//						fnWorkflowResult.setErrorMessage("Workflow Finished");
//						fnWorkflowResult.setFnWorkflowStatus("Workflow Finished");
//					}
//				}
//				else
//				{
//					log.info("Failed to get a RosterQuery");
//					//Update the FnWorkflow Object
//					fnWorkflowResult.setErrorFlag(1);
//					fnWorkflowResult.setErrorMessage("Failed to get a RosterQuery");
//					fnWorkflowResult.setFnWorkflowStatus("Failed to get a RosterQuery");
//				}
//			}
//			else
//			{
//				log.info("Failed to get a VWRoster");
//				//Update the FnWorkflow Object
//				fnWorkflowResult.setErrorFlag(1);
//				fnWorkflowResult.setErrorMessage("Failed to get a VWRoster");
//				fnWorkflowResult.setFnWorkflowStatus("Failed to get a VWRoster");
//			}
//		}
//		catch(VWException ex)
//		{
//			log.info("ERROR", ex.getMessage());
//			fnWorkflowResult = null;
//		}
//		log.info("Leaving WorkflowManager -> getWorkflow()");
//		log.info("===========================================================");
//		return fnWorkflowResult;
	}

	private VWQueue getQueue(VWSession vwSession, String queueName)
	{
		VWQueue vwQueue = null;
		try
		{
			log.info("Entered WorkflowManager -> getQueue()");
			vwQueue = vwSession.getQueue(queueName);
		}
		catch (VWException ex)
		{
			log.info("ERROR", ex.getMessage());
			if (vwSession != null)
			{
				//Set vwSession to null to kill any connections
				vwSession = null;
			}
			vwQueue = null;
		}
		log.info("Leaving WorkflowManager -> getQueue()");
		return vwQueue;
	}

	private VWRoster getRoster(VWSession vwSession, String rosterName)
	{
		VWRoster vwRoster = null;
		try
		{
			//log.info("Entered WorkflowManager -> getRoster()");
			vwRoster = vwSession.getRoster(rosterName);
		}
		catch (VWException ex)
		{
			log.info("ERROR", ex.getMessage());
			if (vwSession != null)
			{
				//Set vwSession to null to kill any connections
				vwSession = null;
			}
			vwRoster = null;
		}
		//log.info("Leaving WorkflowManager -> getRoster()");
		return vwRoster;
	}

	private FnWorkflowList getFnWorkflowListByRoster(VWSession vwSession, String process, String[] filterName, String[] filterValue)
	{
		//Create the FnWorkflowList Object
		FnWorkflowList fnWorkflowList = new FnWorkflowList();

		try
		{
			log.info("Entered WorkflowManager -> getFnWorkflowListByRoster()");
			VWRoster vwRoster = getRoster(vwSession, process);
			//Verify the Roster was OK
			if (vwRoster != null)
			{
				//Get the Roster Count
				int rosterCount = vwRoster.fetchCount();
				log.info("Roster Count: " + rosterCount);
				//Update the Workflow List
				fnWorkflowList.setCount(rosterCount);
				fnWorkflowList.setProcessName(process);
				
				//Check the Roster Count to make sure it is > 0, otherwise skip.
				if (rosterCount > 0)
				{
					//Get the Roster Elements
					VWRosterQuery query = getRosterQuery(vwRoster, filterName, filterValue);
					//Verify the Query was OK
					if (query != null && query.hasNext())
					{
						log.info("Query Not Null");
						//Process the Results
						while(query.hasNext())
						{
							log.info("Query has next");
							VWRosterElement rosterItem = (VWRosterElement) query.next();
							VWStepElement stepElement = rosterItem.fetchStepElement(false, false);
							if (stepElement != null)
							{
								//Update the FnWorkflow
								FnWorkflow fnWorkflow = new FnWorkflow();
								fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing");

								//Create the FnWorkflowPropertyList
								FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
								fnWorkflowPropertyList = updateFnWorkflowPropertyListInfo(stepElement);

								//Add the FnWorkflowPropertyList to the FnWorkflow
								fnWorkflow.setFnWorkflowPropertyList(fnWorkflowPropertyList);
								//Update the Workflow List for the Process
								fnWorkflowList.setStepName(fnWorkflow.getFnWorkflowProcess());
												
								//Add the FnWorkflow to the FnWorkflowList
								fnWorkflowList.addFnWorkflow(fnWorkflow);
							}
							else
							{
								log.info("Failed to get a StepElement because the Workflow is no longer available");
								//Update the FnWorkflow Object
								fnWorkflowList.setErrorFlag(1);
								//fnWorkflowList.setErrorMessage("Failed to get a StepElement because the Workflow is no longer available");
							}
						}
					}
					else
					{
						log.info("Failed to get a RosterQuery because " + process + " has 0 Records");
						//Update the FnWorkflowList Object
						fnWorkflowList.setErrorFlag(1);
						//fnWorkflowList.setErrorMessage("Failed to get a RosterQuery because " + process + " has 0 Records");
					}
				}
				else
				{
					log.info("Roster has No Records so a query will not be performed");
				}
			}
			else
			{
				log.info("Failed to get a VWRoster because " + process + " does not exist");
				//Update the FnWorkflowList Object
				fnWorkflowList.setErrorFlag(1);
				//fnWorkflowList.setErrorMessage("Failed to get a VWRoster because " + process + " does not exist");
			}
		}
		catch (VWException ex)
		{
			log.info("ERROR", ex.getMessage());
			if (vwSession != null)
			{
				//Set vwSession to null to kill any connections
				vwSession = null;
			}
			//Update the FnWorkflowList Object
			fnWorkflowList.setErrorFlag(1);
			//Update ErrorMessage
			//fnWorkflowList.setErrorMessage(ex.getMessage());
		}
		log.info("Leaving WorkflowManager -> getFnWorkflowListByRoster()");
		log.info("===========================================================");
		return fnWorkflowList;
	}

	private void getFnWorkflowPropertyValue(String fnWorkflow, String propName)
	{
//		String propValue = "";
//		//Create the FnWorkflowPropertyList from the Request
//		FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
//
//		try
//		{
//			log.info("Entered WorkflowManager -> getFnWorkflowPropertyValue()");
//			log.info("Property Name: " + propName);
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
//							log.info("Property Value: " + propValue);
//							break;
//						}
//					}
//
//				}
//			}
//		}
//		catch (Exception ex)
//		{
//			log.info("ERROR", ex.getMessage());
//			propValue = "";
//		}
//		log.info("Leaving WorkflowManager -> getFnWorkflowPropertyValue()");
//		log.info("===========================================================");
//
//		return propValue;
	}

	private FnWorkflow getFnWorkflowByQueue(VWSession vwSession, String queue, String[] filterName, String[] filterValue)
	{
		//Create the FnWorkflow Object
		FnWorkflow fnWorkflow = new FnWorkflow();
		
		try
		{
			log.info("Entered WorkflowManager -> getFnWorkflowByQueue()");
			log.info("Queue: " + queue);
			VWQueue vwQueue = getQueue(vwSession, queue);
			//Workflow Search Results Limit
			String workflowSearchLimit = "";
			int workflowSearchMax = -1;
			int workflowResultCount = 0;
			
			//Verify the Queue was OK
			if (vwQueue != null)
			{
				//Get the Workflow Search Limit
				workflowSearchLimit = appConfig.getWorkflowSearchLimit();
				if (workflowSearchLimit.length() > 0)
				{
					workflowSearchMax = Integer.parseInt(workflowSearchLimit);
				}
				
				//Get the Queue Elements
				VWQueueQuery query = null;
				if (queue.equals("Inbox(0)"))
				{
					//Different Query to get a User's Inbox Items
					query = getQueueQuery(vwQueue, null, null, "");
				}
				else
				{
					query = getQueueQuery(vwQueue, filterName, filterValue, "");
				}
				
				//Verify the Query was OK
				if (query != null && query.hasNext())
				{
					log.info("Query Not Null");
					//Process the Results
					while(query.hasNext())
					{
						//Check if the workflowSearchMax limit has been reached
						if (workflowResultCount == workflowSearchMax)
						{
							log.info(workflowSearchMax + " Query Results reached");
							break;
						}
						log.info("Query Has Next");
						VWQueueElement queueItem = (VWQueueElement) query.next();
						VWStepElement stepElement = queueItem.fetchStepElement(false, false);
						if (stepElement != null)
						{
							//Check for Inbox Queue
							if (queue.equals("Inbox(0)"))
							{
								VWParticipant vwParticipant = stepElement.getParticipantNamePx();
								log.info("Participant: " + vwParticipant.getParticipantName());
								//Compare with the User to see if this is their Workflow
								if (filterValue[0].equals(vwParticipant.getParticipantName()))
								{
									//Update the FnWorkflow
									fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing");

									//Create the FnWorkflowPropertyList
									FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
									fnWorkflowPropertyList = updateFnWorkflowPropertyListInfo(stepElement);
									//Add the FnWorkflowPropertyList to the FnWorkflow
									fnWorkflow.setFnWorkflowPropertyList(fnWorkflowPropertyList);
								}
							}
							else
							{
								//Update the FnWorkflow
								fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing");

								//Create the FnWorkflowPropertyList
								FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
								fnWorkflowPropertyList = updateFnWorkflowPropertyListInfo(stepElement);
								//Add the FnWorkflowPropertyList to the FnWorkflow
								fnWorkflow.setFnWorkflowPropertyList(fnWorkflowPropertyList);
							}

							//Check if Clarety Workflow Queue Description Actions are used
							//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
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
//										log.info("Workflow Description: ");
//										log.info(description);
//										fnWorkflow.setFnWorkflowResponse(description);
//									}
//								}
//							}
						}
						else
						{
							log.info("Failed to get a StepElement");
							//Update the FnWorkflow Object
							fnWorkflow.setErrorFlag(1);
						}
						//Increment the workflowResultCount
						workflowResultCount++;
					}//End While
				}
				else
				{
					log.info("Failed to get a QueueQuery");
					//Update the FnWorkflow Object
					fnWorkflow.setErrorFlag(1);
					fnWorkflow = null;
				}
			}
			else
			{
				log.info("Failed to get a VWQueue");
				//Update the FnWorkflow Object
				fnWorkflow.setErrorFlag(1);
				//Update ErrorMessage
				//fnWorkflow.setErrorMessage("Failed to get a VWQueue");
			}
		}
		catch (VWException ex)
		{
			log.info("ERROR", ex.getMessage());
			//Update the FnWorkflow Object
			fnWorkflow.setErrorFlag(1);
			//Update ErrorMessage
			//fnWorkflow.setErrorMessage(ex.getMessage());
		}
		log.info("Leaving WorkflowManager -> getFnWorkflowByQueue()");
		log.info("===========================================================");
		return fnWorkflow;
	}


	private FnWorkflowList getFnWorkflowListByQueue(SQLServerConnection con, VWSession vwSession, String queue, String[] filterName, String[] filterValue, String sortBy)
	{
		//Create the FnWorkflowList Object
		FnWorkflowList fnWorkflowList = new FnWorkflowList();

		try
		{
			log.info("Entered WorkflowManager -> getFnWorkflowListByQueue()");
			log.info("Queue: " + queue);
			VWQueue vwQueue = getQueue(vwSession, queue);
			//Workflow Search Results Limit
			String workflowSearchLimit = "";
			int workflowSearchMax = -1;
			int workflowResultCount = 0;
			
			//Verify the Queue was OK
			if (vwQueue != null)
			{
				//Get the Queue Count
				int queueCount = vwQueue.fetchCount();
				log.info("Queue Count: " + queueCount);
				//Update Queue Name on FnWorkflowList
				fnWorkflowList.setStepName(queue);
				//FnWorkflowList from DB Query
				FnWorkflowList tempFnWorkflowList = new FnWorkflowList();
				//Get the Workflow Search Limit
				workflowSearchLimit = appConfig.getWorkflowSearchLimit();
				if (workflowSearchLimit.length() > 0)
				{
					workflowSearchMax = Integer.parseInt(workflowSearchLimit);
				}
				
				//Check the Queue Count to make sure it is > 0, otherwise skip.
				if (queueCount > 0)
				{
					//Get the Queue Elements
					VWQueueQuery query = null;
					if (queue.equals("Inbox(0)"))
					{
						if (appConfig.getWorkflowDBSearchEnabled().equals("true"))
						{
							//DB FileNet Query for Workflows
							tempFnWorkflowList = getQueueDBQuery(con, vwQueue, queue, filterName, filterValue);
						}
						else
						{
							//FileNet Query
							//Different Query to get a User's Inbox Items
							query = getQueueQuery(vwQueue, null, null, sortBy);
						}
					}
					else
					{
						if (appConfig.getWorkflowDBSearchEnabled().equals("true"))
						{
							//DB FileNet Query for Workflows
							tempFnWorkflowList = getQueueDBQuery(con, vwQueue, queue, filterName, filterValue);
						}
						else
						{
							//FileNet Query
							query = getQueueQuery(vwQueue, filterName, filterValue, sortBy);
						}
					}
					
					//Check to see if the Query or the FnWorkflowList was OK
					if (query != null && query.hasNext())
					{
						log.info("Query Not Null");
						//Process the Results
						while(query.hasNext())
						{
							//Check if the workflowSearchMax limit has been reached
							if (workflowResultCount == workflowSearchMax)
							{
								log.info(workflowSearchMax + " Query Results reached");
								break;
							}
							log.info("Query has next");
							VWQueueElement queueItem = (VWQueueElement) query.next();
							VWStepElement stepElement = queueItem.fetchStepElement(false, false);
							if (stepElement != null)
							{
								log.info("Queue Query Total: " + query.fetchCount());
								//Update Count for the Queue on FnWorkflowList
								//fnWorkflowList.setCount(query.fetchCount());
								//Update the FnWorkflow
								FnWorkflow fnWorkflow = new FnWorkflow();
								//Check for Inbox Queue
								if (queue.equals("Inbox(0)"))
								{
									VWParticipant vwParticipant = stepElement.getParticipantNamePx();
									log.info("Participant: " + vwParticipant.getParticipantName());
									//Compare with the User to see if this is their Workflow
									if (filterValue[0].equals(vwParticipant.getParticipantName()))
									{
										//Update the FnWorkflow
										//FnWorkflow fnWorkflow = new FnWorkflow();
										fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing");

										//Create the FnWorkflowPropertyList
										FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
										fnWorkflowPropertyList = updateFnWorkflowPropertyListInfo(stepElement);
										//Add the FnWorkflowPropertyList to the FnWorkflow
										fnWorkflow.setFnWorkflowPropertyList(fnWorkflowPropertyList);
										//Update the Workflow List for the Activity/Step
										fnWorkflowList.setStepName(fnWorkflow.getFnWorkflowStep());
										//Add the FnWorkflow to the FnWorkflowList
										//fnWorkflowList.addFnWorkflow(fnWorkflow);
									}
								}
								else
								{
									//Update the FnWorkflow
									//FnWorkflow fnWorkflow = new FnWorkflow();
									fnWorkflow = updateFnWorkflowInfo(stepElement, "Existing");

									//Create the FnWorkflowPropertyList
									FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
									fnWorkflowPropertyList = updateFnWorkflowPropertyListInfo(stepElement);
									//Add the FnWorkflowPropertyList to the FnWorkflow
									fnWorkflow.setFnWorkflowPropertyList(fnWorkflowPropertyList);
									//Update the Workflow List for the Activity/Step
									fnWorkflowList.setStepName(fnWorkflow.getFnWorkflowStep());
									//Add the FnWorkflow to the FnWorkflowList
									//fnWorkflowList.addFnWorkflow(fnWorkflow);
								}
								
								//Add the FnWorkflow to the FnWorkflowList
								fnWorkflowList.addFnWorkflow(fnWorkflow);
								log.info("Queue Count: " + fnWorkflowList.getCount());
							}
							else
							{
								log.info("Failed to get a StepElement because the Workflow is no longer available");
							}
							//Increment the workflowResultCount
							workflowResultCount++;
						}//End While
					}
					else if (tempFnWorkflowList.getCount() > 0)
					{
						log.info("Queue Query Total: " + tempFnWorkflowList.getCount());
						//Update Count for the Queue on FnWorkflowList
						//fnWorkflowList.setCount(tempFnWorkflowList.getCount());
												
						//Add the tempFnWorkflowList to the FnWorkflowList
						fnWorkflowList.addFnWorkflowList(tempFnWorkflowList);
						log.info("Queue Count: " + fnWorkflowList.getCount());
					}
					else
					{
						log.info("Failed to get a QueueQuery because " + queue + " has 0 Records");
					}
				}
				else
				{
					log.info("Queue has No Records so a query will not be performed");
				}
			}
			else
			{
				log.info("Failed to get a VWQueue because " + queue + " does not exist");
				//Update the FnWorkflowList Object
				fnWorkflowList.setErrorFlag(1);
				//Update ErrorMessage
				//fnWorkflowList.setErrorMessage("Failed to get a VWQueue because " + queue + " does not exist");
			}
		}
		catch (VWException ex)
		{
			log.info("ERROR", ex.getMessage());
			if (vwSession != null)
			{
				//Set vwSession to null to kill any connections
				vwSession = null;
			}
			//Update the FnWorkflowList Object
			fnWorkflowList.setErrorFlag(1);
			//Update ErrorMessage
			//fnWorkflowList.setErrorMessage(ex.getMessage());
		}
		log.info("Leaving WorkflowManager -> getFnWorkflowListByQueue()");
		log.info("===========================================================");
		return fnWorkflowList;
	}

	private void getFnWorkflowCountsListByQueue(VWSession vwSession, String queue, String[] filterName, String[] filterValue, String sortBy)
	{
//		//Create the FnWorkflowList Object
//		FnWorkflowList fnWorkflowList = new FnWorkflowList();
//
//		try
//		{
//			log.info("Entered WorkflowManager -> getFnWorkflowCountsListByQueue()");
//			log.info("Queue: " + queue);
//			VWQueue vwQueue = getQueue(vwSession, queue, wiiscLog);
//			//Verify the Queue was OK
//			if (vwQueue != null)
//			{
//				//Get the Queue Count
//				int queueCount = vwQueue.fetchCount();
//				log.info("Queue Total: " + queueCount);
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
//						log.info("Query Not Null");
//						log.info("Queue Query Total: " + query.fetchCount());
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
//						log.info("Failed to get a QueueQuery");
//					}
//				}
//				else
//				{
//					log.info("Queue has No Records so a query will not be performed");
//				}
//			}
//			else
//			{
//				log.info("Failed to get a VWQueue");
//				//Update the FnWorkflowList Object
//				fnWorkflowList.setErrorFlag(1);
//				fnWorkflowList.setErrorMessage("Failed to get a VWQueue");
//			}
//		}
//		catch (VWException ex)
//		{
//			log.info("ERROR", ex.getMessage());
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
//		log.info("Leaving WorkflowManager -> getFnWorkflowCountsListByQueue()");
//		log.info("===========================================================");
//		return fnWorkflowList;
	}
	
	private SQLServerConnection loginWorkflowDB()
	{
		SQLServerConnection con = null;
		
		try
		{
			log.info("Entered WorkflowManager -> loginWorkflowDB()");
			//Workflow DB Server
			String workflowDBServerName = "";
			//Workflow DB Server Port
			String workflowDBServerPort = "";
			//Workflow DB Name
			String workflowDBName = "";
			//Workflow DB User
			String workflowDBUser = "";
			//Workflow DB User's Password
			String workflowDBUserPassword = "";
			
			//Get all of the Workflow Information
			workflowDBServerName = appConfig.getWorkflowDBServerName();
			workflowDBServerPort = appConfig.getWorkflowDBServerPort();
			workflowDBName = appConfig.getWorkflowDBName();
			workflowDBUser = appConfig.getWorkflowDBUser();
			workflowDBUserPassword = appConfig.getWorkflowDBUserPassword();
			
			log.info("Workflow DB Server: " + workflowDBServerName);
			log.info("Workflow DB Server Port: " + workflowDBServerPort);
			log.info("Workflow DB Name: " + workflowDBName);
			log.info("Workflow DB User: " + workflowDBUser);
			
			// Establish the connection. 
			SQLServerDataSource ds = new SQLServerDataSource();
			ds.setUser(workflowDBUser);
			ds.setPassword(workflowDBUserPassword);
			ds.setServerName(workflowDBServerName);
			ds.setPortNumber(Integer.parseInt(workflowDBServerPort)); 
			ds.setDatabaseName(workflowDBName);
			con = (SQLServerConnection) ds.getConnection();
			
			//Verify the Connection was successful
			if (con != null)
			{
				log.info("Workflow DB Connection was Successful");
			}
			else
			{
				log.info("Workflow DB Connection Failed");
			}
			
		}
		catch (Exception e)
		{
			log.info("ERROR", e.getMessage());
		}
		log.info("Leaving WorkflowManager -> loginWorkflowDB()");
		log.info("===========================================================");
		return con;
	}
	


	//Output the FnWorkflowPropertyList
//	private void outputFnWorkflowPropertyList(String fnWorkflowPropertyList)
//	{
//		log.info("outputFnWorkflowPropertyList");
//		log.info("===========================================================");
//		int propSize;
//		propSize = fnWorkflowPropertyList.getFnWorkflowPropsList().size();
//		for (int i = 0; i < propSize; i++)
//		{
//			FnWorkflowProperty fnWorkflowProperty = fnWorkflowPropertyList.getFnWorkflowPropsList().get(i);
//			log.info("Property: " + fnWorkflowProperty.getName());
//			log.info("Value: " + fnWorkflowProperty.getValue());
//		}
//		log.info("===========================================================");
//	}
	
	public void createCase(String appId, String planId, String caseId, String process)
	{
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
//		log.info("Entered WorkflowManager -> createCase()");
//
//		/* create case */
//		String caseURL = "http://" + globalConfig.getString("ceServerName") + ":" + globalConfig.getString("ceApplicationPort") + "/CaseManager/CASEREST/v1/cases"; 
//		System.out.println("CASE URL: " + caseURL);
//
//		log.info("caseURL: " + caseURL);
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
//				log.info("==========================================");
//				log.info("appId: " + appId);
//				log.info("planId: " + planId);
//				log.info("caseId: " + caseId);
//				log.info("process: " + process);
//				log.info("==========================================");
//				log.info("==		Case Results -> Success	    ==");
//				log.info("==========================================");
//
//
//				//Get the Results
//				JSONObject contentObj = new JSONObject(response);
//				caseTitle = (String) contentObj.get("CaseTitle");
//				caseIdentifier = (String) contentObj.get("CaseIdentifier");
//				caseFolderId = (String) contentObj.get("CaseFolderId");
//
//				log.info("CaseTitle: " + caseTitle);
//				log.info("CaseIdentifier: " + caseIdentifier);
//				log.info("CaseFolderId: " + caseFolderId);
//				log.info("==========================================");
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
//			log.info("==========================================");
//			log.info("appId: " + appId);
//			log.info("planId: " + planId);
//			log.info("caseId: " + caseId);
//			log.info("process: " + process);
//			log.info("==========================================");
//			log.info("==		Case Results -> Fail	    ==");
//			log.info("==========================================");
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
//		log.info("Leaving WorkflowManager -> createCase()");
//		log.info("===========================================================");
//		return result;
	}

	private void getLocalResource(String propsFile)
	{
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
	}
	
	private void getImagingSession(String globalConfig)
	{
//		String sessionId = "";
//		//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//		//Create CE User Session Token for viewing documents
//		Session session = ObjectFactory.getSession("UserToken", null, globalConfig.getString("ceUserId"), globalConfig.getString("cePassword"));
//		//Generate a Token URL with Session ID
//		sessionId = session.getToken(false);
//		sessionId = URLEncoder.encode(sessionId);
//
//		return sessionId;
	}

	private void removeGUIDBrackets(String src)
	{
//		return src.substring(1, src.length()-1);
	}
//
	private void executeCaseRESTAPI(String resourceURI, String requestMethod, String content)
	{
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
	}

	private String getDateTime()
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmssSSS");
	    Date date = new Date();
	    return dateFormat.format(date);
	}
	
	
	private PrintWriter getWriter(String name)
	{
		PrintWriter out = null;
		try
		{
			File file = new File(name);
			out = new PrintWriter(
					new BufferedWriter(
							new FileWriter(file,false)));
		}
		catch (IOException e)
		{
			log.info("I/O Error with the file " + name);
		}
		
		return out;
	}
	
	private void getResponse(InputStream in) 
	{
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
	}

}



