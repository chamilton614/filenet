package com.filenet.cpe.tools;

//Import.
import com.filenet.api.constants.*;
import com.filenet.api.core.*;
import com.filenet.api.exception.*;
import com.filenet.api.security.AccessPermission;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;
import com.filenet.api.collection.DocumentSet;
import com.filenet.api.collection.FolderSet;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.collection.PageIterator;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.ReferentialContainmentRelationshipSet;
import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.query.*;

import com.filenet.api.property.FilterElement;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.property.PropertyFilter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

//import java.util.Properties;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.security.auth.Subject;

import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class CE_Importer {

	/**
	 * @param args
	 */
	//Boolean for Done.txt files in an Image folder
	private static boolean DoneFound = false;
	//P8 Domain
	private static Domain p8Dom = null;
	//P8 Object Store
	private static ObjectStore p8OS = null;
	//Import file
	private static String importFile = "";
	//Index file
	private static String indexFile = "";
	//Doc Class
	private static String docClass = "";
	//Index Values
	private static String[] indexProps = null;
	//Folder Path
	private static String folderNamingPath = "";
	//Doc Containment
	private static String DocContainerName = "";
	
	public static void setDoneFound(boolean done)
	{
		DoneFound = done;
	}
	
	public boolean getDoneFound()
	{
		return DoneFound;
	}
	
	public void setP8Domain(Domain p8dom)
	{
		p8Dom = p8dom;
	}
	
	public static Domain getP8Domain()
	{
		return p8Dom;
	}
	
	public void setP8ObjectStore(ObjectStore os)
	{
		p8OS = os;
	}
	
	public ObjectStore getP8ObjectStore()
	{
		return p8OS;
	}
	
	public static void setImportFile(File f)
	{
		importFile = f.getName();
	}
	
	public static String getImportFile()
	{
		return importFile;
	}
	
	public static void setIndexFile(File f)
	{
		indexFile = f.getName();
	}
	
	public String getIndexFile()
	{
		return indexFile;
	}
	
	public static void setDocClass(String dc)
	{
		docClass = dc;
	}
	
	public static String getDocClass()
	{
		return docClass;
	}
	
	public static void setIndexProps(String[] props)
	{
		indexProps = props;
	}
	
	public static String[] getIndexProps()
	{
		return indexProps;
	}
	
	public static void setFolderPath(String folderpath)
	{
		folderNamingPath = folderpath;
	}
	
	public String getFolderPath()
	{
		return folderNamingPath;
	}
	
	public static void setDocContainer(String docContainer)
	{
		DocContainerName = docContainer;
	}
	
	public String getDocContainer()
	{
		return DocContainerName;
	}
	
	//Used for the Import Log
	private static final class Import
	{
		//Output Print Writer
		static PrintWriter output = null;
	}
	
	//Used for the Debug Log
	private static final class Debugger
	{
		//Debug Print Writer
		static PrintWriter debug = null;
	}
	
	private static final class ConfigInfo
	{
		//Connection Type EJB or WSI
		static String connType = "";
		//URI for Connecting
		static String uri = "";
		//Stanza for JAAS
		static String p8Stanza = "";
		//Username for Connecting
		static String username = "";
		//Password for Connecting
		static String password = "";
		//P8Domain for Connecting
		//static String p8Domain = "";
		//ObjectStore for Folders and Documents
		static String objectstore = "";
		//Use ObjectStore Folder or Folders File
		static String useObjectStoreFolder = "";
		//ObjectStore Folder
		static String osFolder = "";
		//ObjectStore Folders File
		static String osFoldersFile = "";
		//Import Path
		static String importPath = "";
		//Use Import Batches by File
		static String useImportBatchesByFile = "";
		//Import Batches by File
		static String importBatchesFile = "";
		//Batches Per Connection
		static String batchesPerConnection = "";
		//Batch Name Prefix
		static String batchNamePrefix = "";
		//Batches Start Number
		static String batchesStartNumber = "";
		//Batches Count
		static String batchesCount = "";
		//Files per Batch Count
		static String filesPerBatchCount = "";
		//Files per Batch Total
		static String filesPerBatchTotal = "";
		//Doc Class Default Name - ceimporter.docclass.default.name
		static String docClassDefaultName = "";
		//Use Doc Class Name from Properties File - ceimporter.docclass.name.from.properties.file
		static String useDocClassNameFromPropsFile = "";
		//Doc Class Name from Properties File position - ceimporter.docclass.name.by.properties.file.position
		static String docClassNameFromPropsFilePosition = "";
		//Doc Class Properties Count
		static String docClassPropCount = "";
		//Doc Class Properties Skip Count
		static String docClassPropSkipCount = "";
		//Doc Class Properties File
		static String docClassPropFile = "";
		//Doc Class Properties Index File
		static String docClassPropIndexFile = "";
		//Doc Class Properties Index Values Rearrange - ceimporter.docclass.properties.index.values.rearrange
		static String useDocClassPropIndexValuesRearrange = "";
		//Doc Class Properties Index Values Rearrange Order - ceimporter.docclass.properties.index.values.rearrange.order
		static String docClassPropIndexValuesRearrangeOrder = "";
		//Store Batch_Name Property for Record Keeping
		static String batchNameSave = "";
		//CE Importer Log File
		static String CEImportLog = "";
		//Use Folder Naming by Doc Property
		static String useFolderNamingByDocProperty = "";
		//Folder Naming by Doc Property Count - Folder Depth
		static String folderNamingByDocPropertyCount = "";
		//Doc Class Properties Folder Naming
		static String docClassPropsFolderNaming = "";
		//Use Folder Naming by Doc Property by Julian Date
		static String useFolderNamingByDocPropertyByJulianDate = "";
		//Doc Class Property for Folder Naming by Julian Date
		static String docClassPropFolderNamingByJulianDate = "";
		//Doc Class Property for Folder Naming Split
		static String useFolderNamingByDocPropertySplit = "";
		//Doc Class Property for Folder Naming Split Types
		static String folderNamingByDocPropertySplitTypes = "";
		//Used to turn ON or OFF Debugging Log
		static String useDebugLog = "";
		//Debug Log
		static String debugLog = "";
		//Delete Docs by Doc List
		static String deleteDocsByList = "";
		//Delete Docs by Doc List File
		static String deleteDocsByListFile = "";
		//Delete Folder Structure - Used ONLY during Debug Testing - default value is 0
		static String deleteFolders = "";
		//Catalog Docs in a Folder Structure - default value is 0
		static String catalogDocsInFolders = "";
		//Delete Pending Docs - Used ONLY during Debug Testing - default value is 0
		static String deletePendingDocs = "";
		//Process Batches - This is the main purpose of the Application - default value is 1. Use 0 to turn Off
		static String processBatches = "";
		//Use Update Doc Security - default value is 1. Use 0 to turn Off
		static String useUpdateDocSecurity = "";
		//Update Doc Security Permissions - Full, Modify, View and comma separated options
		static String updateDocSecurityPerms = "";
		//Update Doc Security Principals to use for updating each Doc
		static String updateDocSecurityPrincipals = "";
		//Use Get Doc Security - default value is 1. Use 0 to turn Off
		static String useGetDocSecurity = "";
	}
	
	public static String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    Date date = new Date();
	    return dateFormat.format(date);
	}
	
	public static void getConfigInfo()
	{
		try
		{
			//CE_Importer Properties Object
			java.util.Properties properties= new java.util.Properties();
			
			//Reader for Properties file
			BufferedReader propsFile = getReader("CE_Importer.properties");
			
	    	try {
	    		//Load the propsFile object into properties
		    	properties.load(propsFile);
		   	} 
	    	catch (FileNotFoundException fnfe)
	    	{
	    		fnfe.printStackTrace();
	    	}
	    	catch (IOException ioe)
	    	{
	    		ioe.printStackTrace();
	    	}
		    	
	    	//Connection Type EJB or WSI
	    	ConfigInfo.connType = properties.getProperty("ceimporter.connection.type");

	    	//URI
	    	if (ConfigInfo.connType.equals("EJB")==true) //EJB Connection
	    	{
	    		ConfigInfo.uri = "iiop://" + properties.getProperty("ceimporter.server") + ":" + properties.getProperty("ceimporter.server.port") + "/FileNet/Engine";
	    	}
	    	else //WSI Connection
	    	{
	    		ConfigInfo.uri = "http://" + properties.getProperty("ceimporter.server") + ":" + properties.getProperty("ceimporter.server.port") + "/wsi/FNCEWS40MTOM/";
	    	}

	    	//P8Stanza for JAAS Connection
	    	ConfigInfo.p8Stanza = properties.getProperty("ceimporter.stanza");
	    	//Username
	    	ConfigInfo.username = properties.getProperty("ceimporter.username");
	    	//Password
	    	ConfigInfo.password = properties.getProperty("ceimporter.username.password");
	    	//P8Domain
	    	//p8Domain = properties.getProperty("ceimporter.p8domain");
	    	//Object Store
	    	ConfigInfo.objectstore = properties.getProperty("ceimporter.objectstore");
	    	//Use Object Store Folder or Folders File
	    	ConfigInfo.useObjectStoreFolder = properties.getProperty("ceimporter.objectstore.usefolder");
	    	//Object Store Folder
	    	ConfigInfo.osFolder = properties.getProperty("ceimporter.objectstore.folder");
	    	//ObjectStore Folders File
	    	ConfigInfo.osFoldersFile = properties.getProperty("ceimporter.objectstore.folders.file");
	    	//Import Path
	    	ConfigInfo.importPath = properties.getProperty("ceimporter.import.batches.path");
	    	//Use Import Batches by File
	    	ConfigInfo.useImportBatchesByFile = properties.getProperty("ceimporter.import.batches.by.file");
	    	//Import File
	    	ConfigInfo.importBatchesFile = properties.getProperty("ceimporter.import.batches.file");
	    	//Batches Per Connection
			ConfigInfo.batchesPerConnection = properties.getProperty("ceimporter.import.batches.per.connection");
	    	//Batch Name Prefix
			ConfigInfo.batchNamePrefix = properties.getProperty("ceimporter.import.batches.name.prefix");
			//Batches Start Number
			ConfigInfo.batchesStartNumber = properties.getProperty("ceimporter.import.batches.start.number");
			//Batches Count
			ConfigInfo.batchesCount = properties.getProperty("ceimporter.import.batches.count");
			//Files per Batch Count
			ConfigInfo.filesPerBatchCount = properties.getProperty("ceimporter.import.batches.files.per.batch.count");
			//Files per Batch Total
			ConfigInfo.filesPerBatchTotal = properties.getProperty("ceimporter.import.batches.files.per.batch.total");
			//Doc Class Default Name - ceimporter.docclass.default.name
			ConfigInfo.docClassDefaultName = properties.getProperty("ceimporter.docclass.default.name");
			//Use Doc Class Name from Properties File - ceimporter.docclass.name.from.properties.file
			ConfigInfo.useDocClassNameFromPropsFile = properties.getProperty("ceimporter.docclass.name.from.properties.file");
			//Doc Class Name from Properties File position - ceimporter.docclass.name.by.properties.file.position
			ConfigInfo.docClassNameFromPropsFilePosition = properties.getProperty("ceimporter.docclass.name.by.properties.file.position");
			//Doc Class Properties Count
			ConfigInfo.docClassPropCount = properties.getProperty("ceimporter.docclass.properties.count");
			//Doc Class Properties Skip Count
			ConfigInfo.docClassPropSkipCount = properties.getProperty("ceimporter.docclass.properties.skip.count");
			//Doc Class Properties File
			ConfigInfo.docClassPropFile = properties.getProperty("ceimporter.docclass.properties.file");
			//Doc Class Properties Index File
			ConfigInfo.docClassPropIndexFile = properties.getProperty("ceimporter.docclass.properties.index.file");
			//Doc Class Properties Index Values Rearrange - ceimporter.docclass.properties.index.values.rearrange
			ConfigInfo.useDocClassPropIndexValuesRearrange = properties.getProperty("ceimporter.docclass.properties.index.values.rearrange");
			//Doc Class Properties Index Values Rearrange Order - ceimporter.docclass.properties.index.values.rearrange.order
			ConfigInfo.docClassPropIndexValuesRearrangeOrder = properties.getProperty("ceimporter.docclass.properties.index.values.rearrange.order");
			//Store Batch_Name Property for Record Keeping
			ConfigInfo.batchNameSave = properties.getProperty("ceimporter.docclass.property.batchname.save");
			//CE Importer Log File
			ConfigInfo.CEImportLog = properties.getProperty("ceimporter.log.file");
			//Doc Property Folder Naming
			//ceimporter.objectstore.folder.naming.by.property
			ConfigInfo.useFolderNamingByDocProperty = properties.getProperty("ceimporter.objectstore.folder.naming.by.docproperty");
			//ceimporter.objectstore.folder.naming.by.property.count
			ConfigInfo.folderNamingByDocPropertyCount = properties.getProperty("ceimporter.objectstore.folder.naming.by.docproperty.count");
			//ceimporter.docclass.properties.folder.naming
			ConfigInfo.docClassPropsFolderNaming = properties.getProperty("ceimporter.docclass.properties.folder.naming");
			//Use Folder Naming by Doc Property by Julian Date
			ConfigInfo.useFolderNamingByDocPropertyByJulianDate = properties.getProperty("ceimporter.docclass.properties.folder.naming.julian");
			//Doc Class Property for Folder Naming by Julian Date
			ConfigInfo.docClassPropFolderNamingByJulianDate = properties.getProperty("ceimporter.docclass.properties.folder.naming.julian.property");
			//Doc Class Property for Folder Naming Split
			ConfigInfo.useFolderNamingByDocPropertySplit = properties.getProperty("ceimporter.docclass.properties.folder.naming.split");
			//Doc Class Property for Folder Naming Split Types
			ConfigInfo.folderNamingByDocPropertySplitTypes = properties.getProperty("ceimporter.docclass.properties.folder.naming.split.types");
			//Used to turn ON or OFF Debugging Log
			ConfigInfo.useDebugLog = properties.getProperty("ceimporter.import.debugging");
			//Debug Log
			ConfigInfo.debugLog = properties.getProperty("ceimporter.import.debug.log.file");
			//Delete Docs by Doc List
			ConfigInfo.deleteDocsByList = properties.getProperty("ceimporter.delete.docs.by.list");
			//Delete Docs by Doc List File
			ConfigInfo.deleteDocsByListFile = properties.getProperty("ceimporter.delete.docs.by.list.file");
			//Delete Folders
			ConfigInfo.deleteFolders = properties.getProperty("ceimporter.delete.folders");
			//Catalog Docs in a Folder Structure
			ConfigInfo.catalogDocsInFolders = properties.getProperty("ceimporter.catalog.docs.in.folders");
			//Delete Pending Docs
			ConfigInfo.deletePendingDocs = properties.getProperty("ceimporter.delete.pending.docs");
			//Process Batches - main application
			ConfigInfo.processBatches = properties.getProperty("ceimporter.process.batches");
			//Use Update Doc Security - default value is 1. Use 0 to turn Off
			ConfigInfo.useUpdateDocSecurity = properties.getProperty("ceimporter.update.doc.security");
			//Update Doc Security Option - Add or Remove
			ConfigInfo.updateDocSecurityPerms = properties.getProperty("ceimporter.update.doc.security.perms");
			//Update Doc Security Principals to use for updating each Doc
			ConfigInfo.updateDocSecurityPrincipals = properties.getProperty("ceimporter.update.doc.security.principals");
			//Use Get Doc Security - default value is 1. Use 0 to turn Off
			ConfigInfo.useGetDocSecurity = properties.getProperty("ceimporter.get.doc.security");
			
			//Close Reader
			propsFile.close();
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static PrintWriter getImportLog(String name)
	{
		//PrintWriter for Output file
		PrintWriter output1 = null;
		try
		{
			//Setup the Output Log File
			String OutputFile = ConfigInfo.CEImportLog;
			//Extract Date Time to use for Log File Name
			String tempDateTime = getDateTime();
			//String[] tempDateTime1 = tempDateTime.split(" ");
			tempDateTime = tempDateTime.replaceAll("/", "-");
			tempDateTime = tempDateTime.replaceAll(":", "-");
			tempDateTime = tempDateTime.replaceAll(" ", "_");
			//tempDateTime = tempDateTime1[0].replaceAll("/", "-");
			if (name.equals("") == true)
			{
				OutputFile = OutputFile.replaceFirst("_Log", "_" + tempDateTime + "_Log");
			}
			else
			{
				OutputFile = OutputFile.replaceFirst("_Log", "_" + tempDateTime + "_" + name + "_Log");
			}
			
			//Create Log File Object
			output1 = getWriter(OutputFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return output1;
	}
	
	public static PrintWriter getDebugLog(String name)
	{
		//PrintWriter for Debug file
		PrintWriter debug1 = null;
		try
		{
			//Setup the debug Log File
			String DebugFile = ConfigInfo.debugLog;
			//Extract Date Time to use for Log File Name
			String tempDateTime = getDateTime();
			String[] tempDateTime1 = tempDateTime.split(" ");
			tempDateTime = tempDateTime1[0].replaceAll("/", "-");
			if (name.equals("") == true)
			{
				DebugFile = DebugFile.replaceFirst("_Log", "_" + tempDateTime + "_Log");
			}
			else
			{
				DebugFile = DebugFile.replaceFirst("_Log", "_" + tempDateTime + "_" + name + "_Log");
			}
			
			//Create Log File Object
			debug1 = getWriter(DebugFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return debug1;
	}
	
	public CE_Importer()
	{
		/*this.folderNamingPath = "";
		this.DocContainerName = "";
		this.docClass = "";
		this.DoneFound = false;
		this.importFile = "";
		this.indexFile = "";
		this.indexProps = null;*/
		
		//Initialize
		getConfigInfo();
		
		/*//Use Debug Log
		//Check if Debugging is turned ON
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			//Initialize the Debug Log
			Debugger.debug = null;
			Debugger.debug = getDebugLog();
		}
		//Use Import Log
		Import.output = null;
		Import.output = getImportLog();*/
	}
	
	public static void main(String[] args)
	{
	    //Create an instance of CE_Importer
		CE_Importer ceimport = new CE_Importer();
		
		//Batches Per Connection
		//int batchesPerConnectionCount = Integer.parseInt(ConfigInfo.batchesPerConnection);
		
		//CE Connection
		//Connection p8Connection = null;
		
		//Use Debug Log
		//Check if Debugging is turned ON
		/*if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			//Initialize the Debug Log
			Debugger.debug = null;
			Debugger.debug = getDebugLog("Login");
		}*/
					
		try
	    {
	       	//Set System Properties
	    	//System.setProperty("java.naming.factory.initial","com.ibm.websphere.naming.WsnInitialContextFactory"); 
	    	//System.setProperty("com.ibm.CORBA.ConfigURL","C:\\Files\\CE_Importer\\Config\\sas.client.props"); 
	    	//System.setProperty("java.security.auth.login.config","C:\\Files\\CE_Importer\\Config\\jaas.conf.WebSphere"); 
	    	
	    	//Check to see if the Object Store Folder was populated in the properties file
	    	//if (ConfigInfo.osFolder.equals("")==true)
	    	//{
	    	//	ConfigInfo.osFolder = "Test";
	    	//}
			
			/*if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
			{
				Debugger.debug.println("====================================");
				Debugger.debug.println("Entered main Method");
			}*/
	    	
			//Start the Import
			System.out.println("================");
			System.out.println("CE Importer");
			System.out.println("================");
			System.out.println(getDateTime());
			
	    	//Output Connection Info
			//System.out.println("");
			System.out.println("=============================================================");
	    	System.out.println("URI: " + ConfigInfo.uri);
	    	System.out.println("P8Stanza: " + ConfigInfo.p8Stanza);
			System.out.println("Username: " + ConfigInfo.username);
			System.out.println("Password: " + ConfigInfo.password);
			//System.out.println("P8Domain:				" + ConfigInfo.p8Domain);
			System.out.println("ObjectStore: " + ConfigInfo.objectstore);
			System.out.println("Object Store Folder: " + ConfigInfo.osFolder);
			System.out.println("Import Path: " + ConfigInfo.importPath);
			System.out.println("=============================================================");
			System.out.println("");
			
	    	//Get the Connection
			//p8Connection = CEConnection(ConfigInfo.username, ConfigInfo.password, ConfigInfo.p8Stanza, ConfigInfo.uri);
			/*if (p8Connection != null)
			{
				System.out.println("=============================================================");
				System.out.println(getDateTime() + " Connection to Content Engine successful");
				if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
				{
					Debugger.debug.println("====================================");
					Debugger.debug.println("Connection to CE Successful");
					Debugger.debug.close();
					//Debugger.debug = null;
				}
				
				//Start the CE_Importer
				ceimport.run(p8Connection);
			}*/	
			
			//Start the CE_Importer
		    //ceimport.run(p8Connection);
		    ceimport.run();
	    }
	    catch (Exception e)
	    {
	       e.printStackTrace();
	       System.exit(0);
	    }
	}
	
	//Run Method
	public Object run()
	{
		/*if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("====================================");
			Debugger.debug.println("Entered the run Method");
		}*/
		
		int useDeleteFolders = Integer.parseInt(ConfigInfo.deleteFolders);
		int useDeleteDocsByList = Integer.parseInt(ConfigInfo.deleteDocsByList);
		int useCatalogDocsInFolders = Integer.parseInt(ConfigInfo.catalogDocsInFolders);
		int useDeletePendingDocs = Integer.parseInt(ConfigInfo.deletePendingDocs);
		int useProcessBatches = Integer.parseInt(ConfigInfo.processBatches);
		int useImportBatchesByFile = Integer.parseInt(ConfigInfo.useImportBatchesByFile);
		int useUpdateDocSecurity = Integer.parseInt(ConfigInfo.useUpdateDocSecurity);
		int useGetDocSecurity = Integer.parseInt(ConfigInfo.useGetDocSecurity);
		//P8 Domain
		//Domain p8Dom = null;
		
		//P8 Object Store
		//ObjectStore p8OS = null;
		
		//Folders
		//String[] OSFolders = null;
		
		//Use Debug Log
		//Check if Debugging is turned ON
		/*if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			//Initialize the Debug Log
			Debugger.debug = null;
			Debugger.debug = getDebugLog("Connection");
		}*/
		
		//Get the P8 Domain
		//p8Dom = getDomain(conn, null);
		//setP8Domain(p8Dom);
		
		//Get the Object Store
		//p8OS = getObjectStore(p8Dom, ConfigInfo.objectstore);
		//setP8ObjectStore(p8OS);
		
		//Check if Debugging is turned OFF
		/*if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			//Close the Debug Log
			Debugger.debug.close();
			//Debugger.debug = null;
		}*/
		
		//System.out.println("Object store: " + p8ObjectStore.get_Name());
		
		//Check if Delete Docs By List is Turned On
		if (useDeleteDocsByList == 3695)
		{
			//Use Debug Log
			//Check if Debugging is turned ON
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				//Initialize the Debug Log
				Debugger.debug = null;
				Debugger.debug = getDebugLog("DeleteDocs");
			}
			
			//Delete Documents By List
			deleteDocumentsByList(ConfigInfo.deleteDocsByListFile);
			
			//Check if Debugging is turned OFF
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				//Close the Debug Log
				Debugger.debug.close();
				//Debugger.debug = null;
			}
		}	
		
		//Check if Delete Folders is Turned On
		if (useDeleteFolders == 3695)
		{
			//Use Debug Log
			//Check if Debugging is turned ON
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				//Initialize the Debug Log
				Debugger.debug = null;
				Debugger.debug = getDebugLog("Delete");
			}
			
			//Delete Folder Contents - used for testing only
			deleteFolderContents(ConfigInfo.osFolder);
			
			//Check if Debugging is turned OFF
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				//Close the Debug Log
				Debugger.debug.close();
				//Debugger.debug = null;
			}
		}
		
		//Check if Delete Pending Docs is Turned On
		if (useDeletePendingDocs == 1)
		{
			//Use Debug Log
			//Check if Debugging is turned ON
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				//Initialize the Debug Log
				Debugger.debug = null;
				Debugger.debug = getDebugLog("DeletePending");
			}
			
			//Delete Pending Documents - used for testing only
			deletePendingDocuments();
			
			//Check if Debugging is turned OFF
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				//Close the Debug Log
				Debugger.debug.close();
				//Debugger.debug = null;
			}
		}
		
		//Check if Catalog Docs In Folders is Turned On
		if (useCatalogDocsInFolders == 1)
		{
			//Use Debug Log
			//Check if Debugging is turned ON
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				//Initialize the Debug Log
				Debugger.debug = null;
				Debugger.debug = getDebugLog("Catalog");
			}
			
			//Catalog Docs In Folder - used for testing only
			catalogFolderContents(ConfigInfo.osFolder);
			
			//Check if Debugging is turned OFF
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				//Close the Debug Log
				Debugger.debug.close();
				//Debugger.debug = null;
			}
		}
		
		//Check if Update Doc Security is Turned On
		if (useUpdateDocSecurity == 1)
		{
			//Use Debug Log
			//Check if Debugging is turned ON
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				//Initialize the Debug Log
				Debugger.debug = null;
				Debugger.debug = getDebugLog("UpdateDocSecurity");
			}
			
			//Update Doc Security - used for testing only
			updateDocSecurity(ConfigInfo.osFolder);
			
			//Check if Debugging is turned OFF
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				//Close the Debug Log
				Debugger.debug.close();
				//Debugger.debug = null;
			}
		}
		
		//Check if Catalog Doc Security is Turned On
		if (useGetDocSecurity == 1)
		{
			//Use Debug Log
			//Check if Debugging is turned ON
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				//Initialize the Debug Log
				Debugger.debug = null;
				Debugger.debug = getDebugLog("GetDocSecurity");
			}
			
			//Catalog Doc Security - used for testing only
			catalogDocSecurity(ConfigInfo.osFolder);
			
			//Check if Debugging is turned OFF
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				//Close the Debug Log
				Debugger.debug.close();
				//Debugger.debug = null;
			}
		}
		
		//Check if Process Batches is Turned On
		if (useProcessBatches == 1)
		{
			//Use Debug Log
			//Check if Debugging is turned ON
			/*if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				//Initialize the Debug Log
				Debugger.debug = null;
				Debugger.debug = getDebugLog("Catalog");
			}
			*/
				
			//Check if Processing Batches by File
			if (useImportBatchesByFile == 1)
			{
				//File List for Importing
				File importFile = new File (ConfigInfo.importBatchesFile);
				//Process Batches By File - Loop
				processBatchesByFile(importFile);
			}
			else
			{
				//Folder for Importing
				File importFolder = new File (ConfigInfo.importPath);
				
				//Process Batches - Loop
				processBatches(importFolder);
			}
						
			/*//Check if Debugging is turned OFF
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				//Close the Debug Log
				Debugger.debug.close();
				//Debugger.debug = null;
			}
			*/
		}
			
		System.out.println("");
		System.out.println("Done");
		return null;
	}
	
	public static void processDoneFiles(String doneList)
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("Entered processDoneFiles Method");
		}
		
		//Done List Data
		String[] doneListData = null;
		
		//Split the Done List to determine the Count and the locations of
		//where to create the Done.txt files
		doneListData = doneList.split(",");
		
		//Get PrintWriter object for creating the Done.txt files
		PrintWriter outputDone = null;
		
		//Create Done.txt in the processed image folders
		for (int i = 0; i < doneListData.length; i++)
		{
			//System.out.println(ConfigInfo.importPath + "/" + ConfigInfo.batchNamePrefix + batchNumber + "/" + (doneStartCount + i) + "/" + "Done.txt");
			//outputDone = getWriter(ConfigInfo.importPath + "/" + ConfigInfo.batchNamePrefix + batchNumber + "/" + (doneStartCount + i) + "/" + "Done.txt");
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				Debugger.debug.println("doneListData " + doneListData[i]);
				Debugger.debug.println("====================================");
			}
			outputDone = getWriter(doneListData[i] + "/" + "Done.txt");
			outputDone.println("Image Processed");
			//outputDone.flush();
			outputDone.close();
			//outputDone = null;
		}
	}
	
	public String[] getIndexProps(String indexFile)
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("Entered getIndexProps Method");
		}
		String[] indexValues = null;
		
		indexValues = indexFile.split(",");
		
		return indexValues;
	}
	
	public static String getIndexFile(File f)
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("Entered getIndexFile Method");
		}
		String indexFile = "";
		
		indexFile = f.getName();
		
		return indexFile;
	}
	
	public static ReferentialContainmentRelationship processDocumentRCR(int batchNumber, int docNumber, String docClass, Document doc)
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("Entered processDocumentRCR Method");
		}
				
		//BufferedReader for Index file
		BufferedReader inputFile1 = null;
		
		//Doc Container Name for a Document Name
		//String DocContainerName = "";
		
		//Line of input from Index file
		String oneLineInputFile1 = "";
		
		//Line elements from Index file
		//String[] indexElements = null;
		
		//Get the Properties into the API Cache
		Properties docProps = doc.getProperties();
		
		//Folder Naming by Doc Prop Path
		//String folderNamingPath = "";
		
		//Root OS Folder Name
		String rootFolderPath = ConfigInfo.osFolder;
		
		//ReferentialContainmentRelationship for Filing the Document
		ReferentialContainmentRelationship rcr = null;
		
		//Get indexElements
		//indexElements = getIndexProps();
		
		//Determine if we are using the defined Object Store Folder or a Folders File
		if (ConfigInfo.useObjectStoreFolder.equals("0") == true)
		{
			//Read the Object Store Folders File and file the New Doc in the proper Folder
			inputFile1 = getReader(ConfigInfo.osFoldersFile);
			try
			{
				//Set the Doc Container Name to be Unique by Batch Number and Image Number within the Batch
				//DocContainerName = "B_" + batchNumber + "_" + docNumber + "_";
				if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
				{
					Debugger.debug.println("DocContainerName: " + DocContainerName);
				}
				DocContainerName = "B-" + batchNumber + "-" + docNumber + "-" + DocContainerName;
				if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
				{
					Debugger.debug.println("DocContainerName: " + DocContainerName);
				}
				
				while ((oneLineInputFile1 = inputFile1.readLine()) != null)
				{
					if (oneLineInputFile1.trim().endsWith(indexProps[2].trim()) == true)
					{
						//Import.output.println(getDateTime() + "," + docProps.getIdValue("ID") + "," + ConfigInfo.batchNamePrefix + batchNumber + "-" + docNumber + "," + importFile + "," + docClass + "," + oneLineInputFile1.trim());
						//Import.output.println(ConfigInfo.batchNamePrefix + a + "-" + b + "," + importFile + "," + lineFile1);
						//Reset folderNamingPath
						folderNamingPath = oneLineInputFile1.trim();
						//File the Document
						rcr = fileDocument(p8OS, doc, DocContainerName + importFile.trim(), folderNamingPath);
						//Output to the Log
						Import.output.println(getDateTime() + "," + docProps.getIdValue("ID") + "," + ConfigInfo.batchNamePrefix + batchNumber + "-" + docNumber + "," + importFile + "," + docClass + "," + folderNamingPath);
						//Import.output.flush();
					}
					//Reset lineFile1
					oneLineInputFile1 = "";
				}
				//Close the Index File
				inputFile1.close();
				//Reset lineFile1
				oneLineInputFile1 = "";
			}
			catch (IOException e)
			{
				System.out.println(getDateTime());
				System.out.println("I/O Error has occurred during the reading of the Object Store Folders File.");
				System.exit(0);
			}
		}
		else //Using a defined starting Object Store Folders location that already exists
		{
			//Update Folder Path
			//osFolderPath = osFolderPath + "/" + folderNamingPath;
			
			if (DocContainerName.equals("") == true)
			{
				DocContainerName = "B-" + batchNumber + "-" + docNumber;
			}
			else
			{
				DocContainerName = "B-" + batchNumber + "-" + docNumber + "-" + DocContainerName;
			}
			if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
			{
				Debugger.debug.println("DocContainerName: " + DocContainerName);
			}
			
			//Remove trailing slash from folderNamingPath if one exists
			if (folderNamingPath.endsWith("/") == true)
			{
				folderNamingPath = folderNamingPath.substring(0, folderNamingPath.length() - 1);
			}
			
			//System.out.println("RootFolderPath: " + rootFolderPath);
			//System.out.println("FolderNamingPath: " + folderNamingPath);
			
			//Used for testing the folders
			String folderFullPath = "";
			String folderTest = "";
			String[] folderTestData = null;
			
			//Get Folder Depth
			folderFullPath = rootFolderPath + folderNamingPath;
			//folderFullPath = folderNamingPath;
			
			//Check to see if rootFolderPath is equal to folderNamingPath
			//and if use Folder Naming is enabled
			//If so, this means that a Doc Property was missing a value
			//The Doc will be filed in a ToBeReviewed Folder
			if (rootFolderPath.equals(folderFullPath) == true && 
					Integer.parseInt(ConfigInfo.useFolderNamingByDocProperty) == 1)
			{
				//Set a New folderFullPath
				folderNamingPath = "/_TO_BE_REVIEWED";
				folderFullPath = rootFolderPath + folderNamingPath;
				if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
				{
					Debugger.debug.println("ToBeReviewed: " + folderFullPath);
				}
				if (docProps.isPropertyPresent("DocumentTitle") == true)
				{
					String fileName1 = "";
					if (importFile.contains("{") == true)
					{
						String[] fileNameData = null;
						String tempDateTime = getDateTime();
						String[] tempDateTime1 = tempDateTime.split(" ");
						tempDateTime = tempDateTime1[0].replaceAll("/", "-");
						fileNameData = importFile.split("\\.");
						//System.out.println("FileName length " + fileNameData.length);
						if (fileNameData.length > 1)
						{
							fileName1 = ConfigInfo.batchNamePrefix + batchNumber + "-" + docNumber + "-" + tempDateTime + "." + fileNameData[1];
							docProps.putValue("DocumentTitle", fileName1.trim());
						}
						else
						{
							fileName1 = ConfigInfo.batchNamePrefix + batchNumber + "-" + docNumber + "-" + tempDateTime + importFile;
							docProps.putValue("DocumentTitle", fileName1.trim());
						}
					}
					else
					{
						fileName1 = importFile;
						docProps.putValue("DocumentTitle", fileName1.trim());
					}
				}
			}
			
			if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
			{
				Debugger.debug.println("folderFullPath " + folderFullPath);
			}
			
			folderTestData = folderNamingPath.split("/");
												
			//Check if the original folder path exists in the Object Store
			//System.out.println("Test Original Folders: " + folderFullPath);
			folderTest = checkFolderInCE(p8OS, folderFullPath);
			
			//Check which folders exist and don't exist
			if (folderTest.equals("") == true)
			{
				if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
				{
					Debugger.debug.println("folderFullPath " + folderFullPath + " does not exist.");
				}
				
				//System.out.println("Original Folder Path does NOT exist");
				if (folderTestData.length > 1)
				{
					String folderTemp1 = "";
					String folderTemp2 = "";
					folderTemp1 = rootFolderPath;
					folderTemp2 = "";
					for (int x = 0; x < folderTestData.length; x++)
					{
						folderTemp2 = folderTemp1 + folderTestData[x] + "/";
						if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
						{
							Debugger.debug.println("folderTemp1 " + folderTemp1);
							Debugger.debug.println("folderTemp2 " + folderTemp2);
						}
						//Check if the original folder path exists in the Object Store
						//System.out.println("Test Each Folder: " + folderTemp2);
						folderTest = checkFolderInCE(p8OS, folderTemp2);
						if (folderTest.equals("") == true)
						{
							//Create the folder - rootFolderPath/NewFolder
							createFolder(p8OS, folderTemp1, folderTestData[x]);
						}
						//Save the previous folderTemp2 value
						folderTemp1 = folderTemp2;
					}
				}
				else
				{
					//Create the folder - rootFolderPath/NewFolder
					createFolder(p8OS, rootFolderPath, folderNamingPath);
				}
			}
												
			//Import.output.println(getDateTime() + "," + docProps.getIdValue("ID") + "," + ConfigInfo.batchNamePrefix + batchNumber + "-" + docNumber + "," + importFile + "," + docClass + "," + folderFullPath.trim());
			//Import.output.println(ConfigInfo.batchNamePrefix + batchNumber + "-" + docNumber + "," + importFile + "," + lineFile1);
			
			//Reset folderNamingPath
			folderNamingPath = folderFullPath.trim();
			
			if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
			{
				Debugger.debug.println("Before Filing DocContainerName: " + DocContainerName);
				Debugger.debug.println("Before Filing folderNamingPath: " + folderNamingPath);
			}
			
			//File the Document
			rcr = fileDocument(p8OS, doc, DocContainerName, folderNamingPath);
			//Output to the Log
			Import.output.println(getDateTime() + "," + docProps.getIdValue("ID") + "," + ConfigInfo.batchNamePrefix + batchNumber + "-" + docNumber + "," + importFile + "," + docClass + "," + folderNamingPath);
			//Import.output.flush();
		}
		
		return rcr;
	}
	
	public static Document processDocument(int batchNumber, int docNumber)
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("Entered processDocument Method");
		}
		
		//Reset Folder Naming Path
		setFolderPath("");
		
		//Reset Doc Container Name
		setDocContainer("");
		
		//Reset Done Found
		setDoneFound(false);
		
		//BufferedReader for Index file
		BufferedReader inputFile1 = null;
		
		//Line of input from Index file
		String oneLineInputFile1 = "";
		
		//Document 
		Document doc = null;
		
		//Document Title Found used to Automatically set the Document Title if it is not already in the Index file
		boolean docTitleFound = false;
		
		//Create Date Found used to Automatically set the Create Date if it is not already in the Index file
		boolean createDateFound = false;
		
		//Image File
		File imageFile = null;
		
		//Lines Read
		int linesRead = 0;
		
		//Total Properties Skip Count
		int totalPropsSkipCount = Integer.parseInt(ConfigInfo.docClassPropSkipCount);
		
		//Use Folder Naming by Doc Properties
		int useFolderNamingByDocProp = Integer.parseInt(ConfigInfo.useFolderNamingByDocProperty);
		
		String tempDateTime = getDateTime();
		String[] tempDateTime1 = tempDateTime.split(" ");
		tempDateTime = tempDateTime1[0].replaceAll("/", "-");
		
		//Image Folder - should be 1 folder at a time
		//Example: Batches/Batch1/1
		File imageFolder = new File (ConfigInfo.importPath + "/" + ConfigInfo.batchNamePrefix + batchNumber + "/" + docNumber);
		if (imageFolder.isDirectory() == true)
		{
			//Get list of Files - should only be 1 to process
			File[] listOfFiles = imageFolder.listFiles();
			for (File f : listOfFiles)
			{
				if ((f.getName().contains(ConfigInfo.docClassPropIndexFile) == false) && (f.getName().contains("Done.txt") == false))
				{
					//Import File
					imageFile = f;
					setImportFile(f);
					importFile = getImportFile();
				}
				else if (f.getName().contains(ConfigInfo.docClassPropIndexFile) == true)
				{
					//Index File
					setIndexFile(f);
					indexFile = getIndexFile(f);
				}
				else if (f.getName().contains("Done.txt") == true)
				{
					//Done File
					setDoneFound(true);
				}
				else
				{
					//Should Never Be Here
				}
			}
			
			//Read Index File if one exists and create the Document
			if ((indexFile.equals("") == false) && (DoneFound == false))
			{
				try
				{
					//Assign the Index File to an Input Reader
					inputFile1 = getReader(ConfigInfo.importPath + "/" + ConfigInfo.batchNamePrefix + batchNumber + "/" + docNumber + "/" + indexFile);
					
					//Boolean to determine if index values need split after the index file has been read
					//This is for the situations where an index file has 1 index value per line
					boolean indexPropsNeedSplit = false;
					String indexPropsList = "";
					
					while ((oneLineInputFile1 = inputFile1.readLine()) != null)
					{
						if (oneLineInputFile1.length() > 0)
						{
							if (oneLineInputFile1.contains(",") == true)
							{
								indexPropsNeedSplit = false;
								
								String[] indexValuesOrder1 = null;
								
								//Determine if the Index Values need rearranged for Folder Naming or any other reason
								if (Integer.parseInt(ConfigInfo.useDocClassPropIndexValuesRearrange) == 1)
								{
									if (ConfigInfo.docClassPropIndexValuesRearrangeOrder.equals("") == false)
									{
										String indexValuesOrder = "";
										String[] tempPropsList = null;
										//Get the correct Index values order from the Properties file
										indexValuesOrder = ConfigInfo.docClassPropIndexValuesRearrangeOrder;
										//Split the correct Index values order into an array
										indexValuesOrder1 = indexValuesOrder.split(",");
										//Split the current Index values into an array
										tempPropsList = oneLineInputFile1.split(",");
										//Reset Index Values Order to be used to hold the correct order of the Index Values
										indexValuesOrder = "";
										for (int i = 0; i < indexValuesOrder1.length; i++)
										{
											int getVal = 0;
											getVal = Integer.parseInt(indexValuesOrder1[i]);
											
											if (indexValuesOrder.equals("") == true)
											{
												indexValuesOrder = tempPropsList[getVal - 1];
											}
											else
											{
												indexValuesOrder = indexValuesOrder + "," + tempPropsList[getVal - 1];
											}
										}
										//Set the Index Properties to the right order
										setIndexProps(indexValuesOrder.split(","));
										indexProps = getIndexProps();
									}
								}
								else
								{
									setIndexProps(oneLineInputFile1.split(","));
									indexProps = getIndexProps();
								}
							}
							else //Used for Index Property files that are 1 value per line
							{
								indexPropsNeedSplit = true;
								if (indexPropsList.equals("") == true)
								{
									indexPropsList = oneLineInputFile1;
								}
								else
								{
									indexPropsList = indexPropsList + "," + oneLineInputFile1;
								}
							}
						}
						//Reset oneLineInputFile1
						oneLineInputFile1 = "";
					}
					//Close the Index File
					inputFile1.close();
					//Reset lineFile1
					oneLineInputFile1 = "";
					
					//Split the indexPropsList if this was populated
					if (indexPropsNeedSplit == true)
					{
						String[] indexValuesOrder1 = null;
	
						//Determine if the Index Values need rearranged for Folder Naming or any other reason
						if (Integer.parseInt(ConfigInfo.useDocClassPropIndexValuesRearrange) == 1)
						{
							if (ConfigInfo.docClassPropIndexValuesRearrangeOrder.equals("") == false)
							{
								String indexValuesOrder = "";
								String[] tempPropsList = null;
								//Get the correct Index values order from the Properties file
								indexValuesOrder = ConfigInfo.docClassPropIndexValuesRearrangeOrder;
								//Split the correct Index values order into an array
								indexValuesOrder1 = indexValuesOrder.split(",");
								//Split the current Index values into an array
								tempPropsList = indexPropsList.split(",");
								//Reset Index Values Order to be used to hold the correct order of the Index Values
								indexValuesOrder = "";
								for (int i = 0; i < indexValuesOrder1.length; i++)
								{
									int getVal = 0;
									getVal = Integer.parseInt(indexValuesOrder1[i]);
									
									if (indexValuesOrder.equals("") == true)
									{
										indexValuesOrder = tempPropsList[getVal - 1];
									}
									else
									{
										indexValuesOrder = indexValuesOrder + "," + tempPropsList[getVal - 1];
									}
								}
								//Set the Index Properties to the right order
								setIndexProps(indexValuesOrder.split(","));
								indexProps = getIndexProps();
							}
						}
						else
						{
							setIndexProps(indexPropsList.split(","));
							indexProps = getIndexProps();
						}
					}
				}
				catch (IOException e)
				{
					System.out.println(getDateTime());
					System.out.println("I/O Error has occurred during the reading of the Index File.");
					System.exit(0);
				}
				
				//Set the Doc Class
				if (Integer.parseInt(ConfigInfo.useDocClassNameFromPropsFile) == 1)
				{
					Integer pos = Integer.parseInt(ConfigInfo.docClassNameFromPropsFilePosition);
					setDocClass(indexProps[pos].trim());
					docClass = getDocClass();
				}
				else
				{
					if (ConfigInfo.docClassDefaultName.equals("") == false)
					{
						setDocClass(ConfigInfo.docClassDefaultName.trim());
						docClass = getDocClass();
					}
					else //Default to Standard Document Class 
					{
						setDocClass("Document");
						docClass = getDocClass();
					}
				}
								
				//Create the Document
				doc = createDocument(p8OS, docClass, new File (ConfigInfo.importPath + "/" + ConfigInfo.batchNamePrefix + batchNumber + "/" + docNumber + "/" + importFile));
				
				//Get the Properties into the API Cache
				Properties docProps = doc.getProperties();
				
				//Folder Naming by Doc Prop Path
				//String folderNamingPath = "";
				
				//Get the Comma separated list of DocProperties from the properties file
				String folderNamingDocPropList = ConfigInfo.docClassPropsFolderNaming;
												
				//Read the Doc Properties File and apply the properties to the New Doc
				inputFile1 = getReader(ConfigInfo.docClassPropFile);
				try
				{
					//Applying Index Values
					if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
					{
						Debugger.debug.println("Applying Index Values:");
					}
					
					while ((oneLineInputFile1 = inputFile1.readLine()) != null)
					{
						if ((linesRead >= totalPropsSkipCount) && (linesRead < indexProps.length))
						{
							//Update Document Properties
							//System.out.println("Setting " + lineFile1);
							if (indexProps[linesRead].length() > 0)
							{
								if ((docProps.isPropertyPresent(oneLineInputFile1.trim()) == true) && (indexProps[linesRead].trim() != null))
								{
									if (oneLineInputFile1.equals("DocumentTitle") == false)
									{
										if (oneLineInputFile1.equals("Mime_Type") == true)
										{
											//Add the Doc Property - (Property Name, Property Value)
											doc.set_MimeType(indexProps[linesRead].trim());
										}
										if (oneLineInputFile1.equals("Create_Date") == true)
										{
											//Set the Create Date Found
											createDateFound = true;
										}
										//Add the Doc Property - (Property Name, Property Value)
										docProps.putValue(oneLineInputFile1.trim(), indexProps[linesRead].trim());
										//Applying Index Value
										if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
										{
											Debugger.debug.println("================================");
											Debugger.debug.println("Non-DocTitle");
											Debugger.debug.println("Index: " + oneLineInputFile1.trim());
											Debugger.debug.println("Index Value: " + indexProps[linesRead].trim());
										}
									}
																		
									if (useFolderNamingByDocProp == 1)
									{
										if (folderNamingDocPropList.contains(oneLineInputFile1) == true)
										{
											//Set DocContainerName
											//DocContainerName = "B-" + batchNumber + "-" + docNumber + "-";
											/*if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
											{
												Debugger.debug.println("DocContainerName: " + DocContainerName);
											}*/
											//Call getFolderNaming()
											getFolderNaming(oneLineInputFile1, linesRead);
										}
									}
								}
							}
						}
						if (oneLineInputFile1.equals("DocumentTitle") == true)
						{
							//Set the Doc Title Found
							docTitleFound = true;
							
							//Add the Doc Property - (Property Name, Property Value)
							//System.out.println("FileName " + importFile);
							String fileName1 = "";
							if (importFile.contains("{") == true)
							{
								String[] fileNameData = null;
								fileNameData = importFile.split("\\.");
								//System.out.println("FileName length " + fileNameData.length);
								if (fileNameData.length > 1)
								{
									//Set the Filename for the Doc Title. Example: Batch<Number>-<Doc>-<TempDateTime>-.<Extension>
									fileName1 = ConfigInfo.batchNamePrefix + batchNumber + "-" + docNumber + "-" + tempDateTime + "." + fileNameData[1];
									//Add the Filename value to Doc Title
									docProps.putValue(oneLineInputFile1.trim(), fileName1.trim());
									//Applying Index Value
									if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
									{
										Debugger.debug.println("================================");
										Debugger.debug.println("Inside If {");
										Debugger.debug.println("Index: " + oneLineInputFile1.trim());
										Debugger.debug.println("Index Value: " + fileName1.trim());
									}
								}
								else
								{
									fileName1 = ConfigInfo.batchNamePrefix + batchNumber + "-" + docNumber + "-" + tempDateTime + importFile;
									docProps.putValue(oneLineInputFile1.trim(), fileName1.trim());
									//Applying Index Value
									if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
									{
										Debugger.debug.println("================================");
										Debugger.debug.println("Inside Else {");
										Debugger.debug.println("Index: " + oneLineInputFile1.trim());
										Debugger.debug.println("Index Value: " + fileName1.trim());
									}
								}
							}
							else
							{
								fileName1 = importFile;
								docProps.putValue(oneLineInputFile1.trim(), fileName1.trim());
								//Applying Index Value
								if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
								{
									Debugger.debug.println("================================");
									Debugger.debug.println("No {");
									Debugger.debug.println("Index: " + oneLineInputFile1.trim());
									Debugger.debug.println("Index Value: " + fileName1.trim());
								}
							}
						}
						//Increment Lines Read until we reach the Starting point
						linesRead++;
						//Reset lineFile1
						//oneLineInputFile1 = "";
					}
					if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
					{
						Debugger.debug.println("================================");
					}
					
					//Close the Index File
					inputFile1.close();
					//Reset variables
					oneLineInputFile1 = "";
					linesRead = 0;
				}
				catch (IOException e)
				{
					System.out.println(getDateTime());
					System.out.println("I/O Error has occurred during the reading of the Doc Props File.");
					System.exit(0);
				}
												
				//Update the Batch_Name Property for Record Keeping
				if (ConfigInfo.batchNameSave.equals("1") == true)
				{
					docProps.putValue("Batch_Name", ConfigInfo.batchNamePrefix + batchNumber + "-" + docNumber);
				}
				
				//Update the Document Title Property if not already set
				if (docTitleFound == false)
				{
					String fileName1 = "";
					String[] fileNameData = null;
					fileNameData = importFile.split("\\.");
					//System.out.println("FileName length " + fileNameData.length);
					if (fileNameData.length > 1)
					{
						//Set the Filename for the Doc Title. Example: Batch<Number>-<Doc>-<TempDateTime>-.<Extension>
						fileName1 = ConfigInfo.batchNamePrefix + batchNumber + "-" + docNumber + "-" + tempDateTime + "." + fileNameData[1];
						//Add the Filename value to Doc Title
						docProps.putValue("DocumentTitle", fileName1.trim());
						//Applying Index Value
						if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
						{
							Debugger.debug.println("================================");
							Debugger.debug.println("In DocTitle Not Found");
							Debugger.debug.println("Index: Document Title");
							Debugger.debug.println("Index Value: " + fileName1.trim());
						}
					}
				}
				
				//Update the Create_Date Property if not already set
				if (createDateFound == false)
				{
					long dateTime = imageFile.lastModified();
					Date d = new Date(dateTime);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					String dateString = sdf.format(d);
					//Add the Date value to the Create_Date property for Record Keeping
					docProps.putValue("Create_Date", dateString.trim());
					if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
					{
						Debugger.debug.println("================================");
						Debugger.debug.println("In Create_Date Not Found");
						Debugger.debug.println("Index: Create_Date");
						Debugger.debug.println("Index Value: " + dateString.trim());
					}
				}
								
				// Save and update property cache
				doc.save(RefreshMode.REFRESH);
				//Check In Document
				//System.out.println("Checking in Document");
				checkinDoc(doc);
			}
			else
			{
				//Should Never Be Here
			}																	
		} //End For - Image Folder
		return doc;
	}
	
	public static void getFolderNaming(String oneLineInput, int linesRead)
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("Entered getFolderNaming Method");
		}
		//Get the Number of DocProperties to use for Folder Naming
		int folderNamingCount = Integer.parseInt(ConfigInfo.folderNamingByDocPropertyCount);
		//Get the Comma separated list of DocProperties from the properties file
		String folderNamingDocPropList = ConfigInfo.docClassPropsFolderNaming;
		//Get the DocProperty from the properties file that contains the Julian Date
		String folderNamingDocPropJulian = ConfigInfo.docClassPropFolderNamingByJulianDate;
		//Use Folder Naming by Doc Property by Julian Date
		int useFolderNamingByDocPropByJulian = Integer.parseInt(ConfigInfo.useFolderNamingByDocPropertyByJulianDate);
		//Use Folder Naming by Doc Property Split
		int useFolderNamingByDocPropSplit = Integer.parseInt(ConfigInfo.useFolderNamingByDocPropertySplit);
		//String array to hold the list of DocProperties
		String[] folderNamingDocPropListData = null;
		//String for Julian Path
		String julianPath = "";
		
		//Reset Folder Naming Path
		//setFolderPath("");
		
		//Root OS Folder Name
		//String rootFolderPath = ConfigInfo.osFolder;
		
		//Set New Folder Naming Path
		//setFolderPath(rootFolderPath);
		
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("oneLineInput " + oneLineInput);
			Debugger.debug.println("linesRead " + linesRead);
		}
		
		//Check if more than 1 Folder is used
		if (folderNamingCount > 1)
		{
			//Split the values from the properties file to determine 
			//with DocProperties to use for Folder Naming
			folderNamingDocPropListData = folderNamingDocPropList.split(",");
			for (int z = 0; z < folderNamingCount; z++)
			{
				if (oneLineInput.equals(folderNamingDocPropListData[z]) == true)
				{
					if (useFolderNamingByDocPropByJulian == 1)
					{
						if (folderNamingDocPropListData[z].equals(folderNamingDocPropJulian) == true)
						{
							//Julian Date - ICN Process
							
							int julianYear = 0;
							int currentYear = 0;
							String[] currentYearData = null;
							currentYearData = getDateTime().split("/");
							//Get the Current Year
							currentYear = Integer.parseInt(currentYearData[0]);
							
							//Get Julian Date
							String tempJulian = indexProps[linesRead];
							//Set the Doc Container Name to be Unique
							DocContainerName = tempJulian;
							//String[] tempDocContainer = null;
							//tempDocContainer = getDateTime().split(" ");
														
							//Test First Character of the Julian Date
							char testFirstChar = tempJulian.charAt(0);														
							//System.out.println("First Char: " + testFirstChar);
							
							try
							{
								Date d = null;
								if (Character.isDigit(testFirstChar) == true)
								{
									//Test if Julian Year is Good
									julianYear = Integer.parseInt(tempJulian.substring(0,4));
									if (julianYear <= currentYear)
									{	
										//System.out.println("Julian Good");
										d = getDateFromJulian7(tempJulian.substring(0, 7));
									}
									else
									{
										//Julian Year is Greater and this means the Julian Date is Bad
										//Reset DocContainerName and folderNamingPath
										//This document will be filed in /TO_BE_REVIEWED
										DocContainerName = "";
										folderNamingPath = "";
										if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
										{
											Debugger.debug.println("Julian Year Bad " + julianYear);
										}
									}
								}
								else
								{
									//Test if Julian Year is Good
									julianYear = Integer.parseInt(tempJulian.substring(1,5));
									if (julianYear <= currentYear)
									{	
										//System.out.println("Julian Good");
										d = getDateFromJulian7(tempJulian.substring(1, 8));
									}
									else
									{
										//Julian Year is Greater and this means the Julian Date is Bad
										//Reset DocContainerName and folderNamingPath
										//This document will be filed in /TO_BE_REVIEWED
										DocContainerName = "";
										folderNamingPath = "";
										if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
										{
											Debugger.debug.println("Julian Year Bad " + julianYear);
										}
									}
								}
								if (d != null)
								{	
									String[] julianData = d.toString().split(" ");
									//folderNamingPath = folderNamingPath + julianData[5] + "/" + julianData[1] + "/" + julianData[2] + "/";
									julianPath = julianData[5] + "/" + julianData[1] + "/" + julianData[2];
									//System.out.println("FolderNaming: " + folderNamingPath);
								}
							}
							catch (Exception e)
							{
								System.out.println("Unable to determine Julian Date from " + tempJulian);
							}
						}
						else
						{
							//folderNamingPath = folderNamingPath + indexProps[linesRead] + "/";
							folderNamingPath = "/" + indexProps[linesRead];
							if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
							{
								Debugger.debug.println("Else Equals: " + folderNamingPath);
							}
							//System.out.println("FolderNaming: " + folderNamingPath);
						}
					} //End FolderNaming by Doc Prop Julian
					else
					{
						//folderNamingPath = folderNamingPath + indexProps[linesRead] + "/";
						folderNamingPath = "/" + indexProps[linesRead];
						if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
						{
							Debugger.debug.println("Else No Use Julian: " + folderNamingPath);
						}
						//System.out.println("FolderNaming: " + folderNamingPath);
					}
				}
			}
		}//End Folder Naming Count > 1
		else
		{
			if (oneLineInput.equals(folderNamingDocPropList) == true)
			{
				if (useFolderNamingByDocPropByJulian == 1)
				{
					if (folderNamingDocPropList.equals(folderNamingDocPropJulian) == true)
					{
						//Julian Date - ICN Process
						
						int julianYear = 0;
						int currentYear = 0;
						String[] currentYearData = null;
						currentYearData = getDateTime().split("/");
						//Get the Current Year
						currentYear = Integer.parseInt(currentYearData[0]);
						
						//Get Julian Date
						//String tempJulian = folderNamingDocPropList;
						//Set the Doc Container Name to be Unique
						//DocContainerName = tempJulian;
						
						//Get Julian Date
						String tempJulian = indexProps[linesRead];
						//Set the Doc Container Name to be Unique
						DocContainerName = tempJulian;
						
						//String[] tempDocContainer = null;
						//tempDocContainer = getDateTime().split(" ");
						
						//Test First Character of the Julian Date
						char testFirstChar = tempJulian.charAt(0);														
						//System.out.println("First Char: " + testFirstChar);
																								
						try
						{
							Date d = null;
							if (Character.isDigit(testFirstChar) == true)
							{
								//Test if Julian Year is Good
								julianYear = Integer.parseInt(tempJulian.substring(0,4));
								if (julianYear <= currentYear)
								{	
									//System.out.println("Julian Good");
									d = getDateFromJulian7(tempJulian.substring(0, 7));
								}
								else
								{
									//Julian Year is Greater and this means the Julian Date is Bad
									//Reset DocContainerName and folderNamingPath
									//This document will be filed in /TO_BE_REVIEWED
									DocContainerName = "";
									folderNamingPath = "";
									if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
									{
										Debugger.debug.println("Julian Year Bad " + julianYear);
									}
								}
							}
							else
							{
								//Test if Julian Year is Good
								julianYear = Integer.parseInt(tempJulian.substring(1,5));
								if (julianYear <= currentYear)
								{	
									//System.out.println("Julian Good");
									d = getDateFromJulian7(tempJulian.substring(1, 8));
								}
								else
								{
									//Julian Year is Greater and this means the Julian Date is Bad
									//Reset DocContainerName and folderNamingPath
									//This document will be filed in /TO_BE_REVIEWED
									DocContainerName = "";
									folderNamingPath = "";
									if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
									{
										Debugger.debug.println("Julian Year Bad " + julianYear);
									}
								}
							}
							if (d != null)
							{
								String[] julianData = d.toString().split(" ");
								julianPath = julianData[5] + "/" + julianData[1] + "/" + julianData[2];
								//System.out.println("FolderNaming: " + folderNamingPath);
							}
						}
						catch (Exception e)
						{
							System.out.println("Unable to determine Julian Date from " + tempJulian);
						}
					}
					else
					{
						folderNamingPath = "/" + folderNamingDocPropList;
						if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
						{
							Debugger.debug.println("Else 1 Folder Naming: " + folderNamingPath);
						}
						//System.out.println("FolderNaming: " + folderNamingPath);
					}
				}
				else //Non-Julian
				{
					if (useFolderNamingByDocPropSplit == 1)
					{
						if (ConfigInfo.folderNamingByDocPropertySplitTypes.equals("") == false)
						{
							//Get the Split Types - Comma Space Tilde Hyphen
							String folderNamingByDocPropertySplitTypes = ConfigInfo.folderNamingByDocPropertySplitTypes;
							
							//Store the separated Split Types in this Array
							String[] splitTypesData1 = null;
							splitTypesData1 = folderNamingByDocPropertySplitTypes.split(",");
							
							//Get the Value to be used by Split Types
							String tempValue = indexProps[linesRead];
							//Used to hold the Final Value
							String tempFinal = "";
							//Used to hold the Final Path Value
							String tempFinalPath = "";
							
							if (splitTypesData1.length > 0)
							{
								int loopCount = 0;
								for (int a = 0; a < splitTypesData1.length; a++)
								{
									String[] tempValue2 = null;
									tempValue2 = getSplitData(splitTypesData1[a],tempValue);
									loopCount++;
									if (loopCount == splitTypesData1.length)
									{
										for (int b = 0; b < tempValue2.length; b++)
										{
											tempFinalPath = tempFinalPath + "/" + tempValue2[b];
											tempFinal = tempFinal + tempValue2[b];
										}
										break;
									}
									else
									{
										tempValue = tempValue2[0];
									}
								}
							}
							
							//Set the Doc Container Name to be Unique
							DocContainerName = tempFinal;
							
							//Check if the Property is a Data Property we should use a Month name instead of a number
							if (folderNamingDocPropList.contains("Date") == true)
							{
								String tempTest = "";
								String[] tempTestData = null;
								tempFinalPath = tempFinalPath.substring(1);
								tempTest = getDateWithMonthName(tempFinalPath);
								//System.out.println("Folder Path:" + tempTest);
								tempTestData = tempTest.split(" ");
								//Update Folder Naming Path
								folderNamingPath = "/" + tempTestData[5] + "/" + tempTestData[1] + "/" + tempTestData[2];
								//Reset Variables
								tempTest = "";
								tempTestData = null;
							}
							else
							{
								//Update Folder Naming Path
								folderNamingPath = tempFinalPath;
							}
							
							//folderNamingPath = tempFinalPath;
							//System.out.println("FolderNaming: " + folderNamingPath);
						}
						else
						{
							folderNamingPath = "/" + folderNamingDocPropList;
							if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
							{
								Debugger.debug.println("Else 1 Folder Naming No Use Julian: " + folderNamingPath);
							}
							//System.out.println("FolderNaming: " + folderNamingPath);
						}
					}
					else
					{
						folderNamingPath = "/" + folderNamingDocPropList;
						if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
						{
							Debugger.debug.println("Else 1 Folder Naming No Use Julian: " + folderNamingPath);
						}
						//System.out.println("FolderNaming: " + folderNamingPath);
					}
				}//End Use Folder Name by Doc Prop Julian
			}//End oneLineInput vs. folderNameDocPropList
		}//End Folder Naming Count = 1
		
		//Set Folder Naming Path
		if (julianPath.equals("") == false)
		{
			folderNamingPath = folderNamingPath + "/" + julianPath;
		}
		
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("Final folderNamingPath: " + folderNamingPath);
		}
	}
	
	public static int processBatch(int batchNumber)
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("Entered processBatch Method");
		}
		
		//filesPerBatchTotal
		int filesPerBatchTotal = Integer.parseInt(ConfigInfo.filesPerBatchTotal);
		
		//FilesProcessed
		int FilesProcessed = 0;
		
		//Document 
		Document doc = null;
		
		//Referential Containment Relationship
		ReferentialContainmentRelationship rcr = null;
		
		//Updating Batch
		UpdatingBatch ub = null;
		
		//Updating Batch
		//System.out.println("Update Batch");
		p8Dom = getP8Domain();
		ub = UpdatingBatch.createUpdatingBatchInstance(p8Dom, RefreshMode.REFRESH);
		
		//filesPerBatchCount
		int filesPerBatchCount = Integer.parseInt(ConfigInfo.filesPerBatchCount);
		
		//Done End Count
		//int doneEndCount = 0;
		
		//Done List
		String doneList = "";
		
		//Test Image Folder exists
		File imageFolder = null;
		
		//Batch Result
		int batchResult = 0;
				
		//Batch Name Folder
		//Example: Batches/Batch1
		File batchName = new File (ConfigInfo.importPath + "/" + ConfigInfo.batchNamePrefix + batchNumber);
		if (batchName.isDirectory() == true)
		{
			System.out.println(getDateTime() + " Processing " + ConfigInfo.batchNamePrefix + batchNumber);
			System.out.println("=============================================================");
			for (int b = 1; b < filesPerBatchTotal + 1; b++)
			{
				imageFolder = new File (ConfigInfo.importPath + "/" + ConfigInfo.batchNamePrefix + batchNumber + "/" + b);
				if (imageFolder.exists() == true)
				{
					//Call processDocument
					doc = processDocument(batchNumber,b);
					
					if (DoneFound == true)
					{
						//Reset Done Found
						setDoneFound(false);
						
						//Output Done Files Found
						//Import.output.println(getDateTime() + " Done.txt FOUND for " + ConfigInfo.batchNamePrefix + batchNumber + "-" + b);
						if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
						{
							Debugger.debug.println("Done.txt FOUND for " + ConfigInfo.batchNamePrefix + batchNumber + "-" + b);
						}
												
						//Update Batch Result Status
						//batchResult = batchResult + 0;
						
						//Increment Files Processed
						//FilesProcessed++;
					}
					else
					{
						if (doc != null)
						{
							//Call processDocumentRCR
							rcr = processDocumentRCR(batchNumber,b,docClass,doc);
						}
						else
						{
							//System.out.println("Problem creating the Doc object after Process Document");
							if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
							{
								Debugger.debug.println("Problem creating the Doc object after Process Document");
							}
						}
					}
				
					if (doc != null && rcr != null)
					{
						if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
						{
							Debugger.debug.println("==================");
							Debugger.debug.println("Add Doc to Batch");
							Debugger.debug.println("==================");
						}
						//Add Doc to the Batch
						ub.add(doc, null);
						//Add the RCR to the Batch
						ub.add(rcr, null);
						//Increment Files Processed
						FilesProcessed++;
						//Done Start Count
						//doneStartCount = b;
						doneList = doneList + ConfigInfo.importPath + "/" + ConfigInfo.batchNamePrefix + batchNumber + "/" + b + ",";
					}
					else
					{
						//System.out.println("Problem creating the Doc object after RCR");
						if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
						{
							Debugger.debug.println("Done Found or problem with Doc and RCR");
						}
						//Increment Files Processed
						FilesProcessed++;
					}
				
					//Reset DocNumber
					//docNumber = b;
				
					//Check Files Processed
					//FilesProcessed equals FilesPerBatch, ex. 1000 files processed = 1000 per batch, then upload to CE
					if (FilesProcessed == filesPerBatchCount)
					{
						if (ub.hasPendingExecute() == true)
						{
							if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
							{
								Debugger.debug.println("====================================");
								Debugger.debug.println("Uploading Documents");
								//Debugger.debug.println("doneStartCount: " + doneStartCount);
								//Debugger.debug.println("doneEndCount: " + doneEndCount);
								Debugger.debug.println("FilesProcessed: " + FilesProcessed);
								//Debugger.debug.println("b: " + b);
								//Debugger.debug.println("docNumber: " + docNumber);
							}
						
							//System.out.println("UpdatingBatch to CE - 5000 Files");
							Import.output.println("============================================");
							Import.output.println(getDateTime() + " Upload to CE...");
						
							//Send Docs to CE
							ub.updateBatch();
						
							if (ub.hasPendingExecute() == false)
							{
								//doneEndCount = FilesProcessed;
								//Reset Files Processed
								FilesProcessed = 0;
								//Output the Number of Batches Processed
								//Import.output.println("============================================");
								Import.output.println(getDateTime() + " Files Processed: " + filesPerBatchCount);
								Import.output.println("============================================");
							
								//Call processDoneFiles
								processDoneFiles(doneList);
							
								//Reset Done List									
								doneList = "";
								
								//Update Batch Result Status
								batchResult = 1;
							}
							else
							{
								//Output the pending Upload statement
								Import.output.println("============================================");
								Import.output.println(getDateTime() + " Upload to CE pending for " + ConfigInfo.batchNamePrefix + batchNumber);
								Import.output.println("============================================");
								//Update Batch Result Status
								batchResult = batchResult + 0;
							}
						}
						else
						{
							//Reset Files Processed
							FilesProcessed = 0;
							
							Import.output.println(getDateTime() + " Files Processed: " + filesPerBatchCount);
							Import.output.println("============================================");
							
							//Output the pending Upload statement
							Import.output.println("============================================");
							Import.output.println(getDateTime() + " No Docs in the Batch to Process for " + ConfigInfo.batchNamePrefix + batchNumber);
							Import.output.println("============================================");
							//Update Batch Result Status
							batchResult = batchResult + 0;
						}
					}
					else if (FilesProcessed == filesPerBatchTotal)
					{
						if (ub.hasPendingExecute() == true)
						{
							if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
							{
								Debugger.debug.println("====================================");
								Debugger.debug.println("Uploading Documents");
								//Debugger.debug.println("doneStartCount: " + doneStartCount);
								//Debugger.debug.println("doneEndCount: " + doneEndCount);
								Debugger.debug.println("FilesProcessed: " + FilesProcessed);
								//Debugger.debug.println("b: " + b);
								//Debugger.debug.println("docNumber: " + docNumber);
							}
						
							//System.out.println("UpdatingBatch to CE - 5000 Files");
							Import.output.println("============================================");
							Import.output.println(getDateTime() + " Upload to CE...");
						
							//Send Docs to CE
							ub.updateBatch();
						
							if (ub.hasPendingExecute() == false)
							{
								//doneEndCount = FilesProcessed;
								FilesProcessed = 0;
								//Output the Number of Batches Processed
								//Import.output.println("============================================");
								Import.output.println(getDateTime() + " Files Processed: " + filesPerBatchCount);
								Import.output.println("============================================");
							
								//Call processDoneFiles
								processDoneFiles(doneList);
							
								//Reset Done List									
								doneList = "";
								
								//Update Batch Result Status
								batchResult = 1;
							}
							else
							{
								//Output the pending Upload statement
								Import.output.println("============================================");
								Import.output.println(getDateTime() + " Upload to CE pending for " + ConfigInfo.batchNamePrefix + batchNumber);
								Import.output.println("============================================");
								//Update Batch Result Status
								batchResult = batchResult + 0;
							}
						}
						else
						{
							//Reset Files Processed
							FilesProcessed = 0;
							
							Import.output.println(getDateTime() + " Files Processed: " + filesPerBatchCount);
							Import.output.println("============================================");
							
							//Output the pending Upload statement
							Import.output.println("============================================");
							Import.output.println(getDateTime() + " No Docs in the Batch to Process for " + ConfigInfo.batchNamePrefix + batchNumber);
							Import.output.println("============================================");
							//Update Batch Result Status
							batchResult = batchResult + 0;
						}
					}
					else
					{
						//Should only get here when doing a partial 5000 batch
					}
				}
				else
				{
					//Output the Image Folder does not exist
					Import.output.println("============================================");
					Import.output.println(getDateTime() + " Image Folder: " + ConfigInfo.batchNamePrefix + batchNumber + "/" + b + " does not exist.");
					Import.output.println("============================================");
				}
				//Reset Image Folder
				imageFolder = null;
				//Reset doc
				doc = null;
				//Reset rcr
				rcr = null;
			} //End For - Batch Name Folder
		}
		else
		{
			//Output the Batch Folder does not exist
			Import.output.println("============================================");
			Import.output.println(getDateTime() + " Batch Folder: " + ConfigInfo.batchNamePrefix + batchNumber + " does not exist.");
			Import.output.println("============================================");
		}
		//Reset Batch Folder
		batchName = null;
		
		return batchResult;
	}
	
	public static void processBatchesByFile(File importFile)
	{
		//TotalBatchesProcessed
		int TotalBatchesProcessed = 0;
		
		if (importFile.isFile() == true)
		{
			//CE Connection
			Connection p8Connection = null;
			
			//One Doc Folder Test
			File oneDocFolderTest = null;
					
			//BufferedReader for Index file
			BufferedReader inputFile1 = null;
			
			//Line of input from Index file
			String oneLineInputFile1 = "";
			
			//Assign the Index File to an Input Reader
			inputFile1 = getReader(ConfigInfo.importBatchesFile);
			
			//filesPerBatchTotal
			int filesPerBatchTotal = Integer.parseInt(ConfigInfo.filesPerBatchTotal);
			
			//FilesProcessed
			int FilesProcessed = 0;
			
			//Updating Batch
			UpdatingBatch ub = null;
			
			//filesPerBatchCount
			int filesPerBatchCount = Integer.parseInt(ConfigInfo.filesPerBatchCount);
				
			//Done List
			String doneList = "";
			
			try
			{
				//Get Connection
				//Get the Connection
				p8Connection = CEConnection(ConfigInfo.username, ConfigInfo.password, ConfigInfo.p8Stanza, ConfigInfo.uri);
				//p8Connection = CEConnection(ConfigInfo.username, ConfigInfo.password, null, ConfigInfo.uri);
				if (p8Connection != null)
				{
					//Use Debug Log
					//Check if Debugging is turned ON
					if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
					{
						//Initialize the Debug Log
						Debugger.debug = null;
						Debugger.debug = getDebugLog("FileImport");
						Debugger.debug.println("=============================================================");
						Debugger.debug.println("Debugger - File Import");
						Debugger.debug.println("=============================================================");
						Debugger.debug.println(getDateTime());
					}

					System.out.println("=============================================================");
					System.out.println(getDateTime() + " processBatchesByFile");
					System.out.println(getDateTime() + " Connection to Content Engine successful");
					if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
					{
						Debugger.debug.println("Connection to CE Successful");
						Debugger.debug.println("=============================================================");
						//Debugger.debug.close();
						//Debugger.debug = null;
					}	

					//Get the P8 Domain
					p8Dom = getDomain(p8Connection, null);
					//setP8Domain(p8Dom);

					System.out.println(getDateTime() + " Connection to the Domain successful");
					//System.out.println("=============================================================");
					if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
					{
						Debugger.debug.println("Connection to the Domain Successful");
						Debugger.debug.println("=============================================================");
						//Debugger.debug.close();
						//Debugger.debug = null;
					}

					//Get the Object Store
					p8OS = getObjectStore(p8Dom, ConfigInfo.objectstore);
					//setP8ObjectStore(p8OS);

					System.out.println(getDateTime() + " Connection to the Object Store successful");
					//System.out.println("=============================================================");
					if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
					{
						Debugger.debug.println("Connection to the Object Store Successful");
						Debugger.debug.println("=============================================================");
						//Debugger.debug.close();
						//Debugger.debug = null;
					}
					
					//Use Import Log
					Import.output = null;
					Import.output = getImportLog("FileImport");
					
					//Start the Import Log
					Import.output.println("=============================================================");
					Import.output.println("CE Importer - File Import");
					Import.output.println("=============================================================");
					Import.output.println(getDateTime());
					
					//Updating Batch
					ub = UpdatingBatch.createUpdatingBatchInstance(p8Dom, RefreshMode.REFRESH);
					
					//Start Processing Batches
					while ((oneLineInputFile1 = inputFile1.readLine()) != null)
					{
						if (oneLineInputFile1.length() > 0)
						{
							if (oneLineInputFile1.contains(",") == true)
							{
								String[] oneLineData = oneLineInputFile1.split(",");
								String batchName = oneLineData[0];
								String docName = oneLineData[1];
								String docPath = oneLineData[2];
								
								//Verify the Doc Folder exists
								oneDocFolderTest = new File (docPath);
								
								if (oneDocFolderTest.isDirectory() == true)
								{
									//Document 
									Document doc = null;
									
									//Referential Containment Relationship
									ReferentialContainmentRelationship rcr = null;
									
									//Batch Number
									String batch = batchName.substring(5);
									int batchNumber = Integer.parseInt(batch);
									
									//Doc Number
									int docNumber = Integer.parseInt(docName);
									
									//Process Document
									//Call processDocument
									doc = processDocument(batchNumber,docNumber);
									
									if (DoneFound == true)
									{
										//Reset Done Found
										setDoneFound(false);
										
										//Output Done Files Found
										//Import.output.println(getDateTime() + " Done.txt FOUND for " + ConfigInfo.batchNamePrefix + batchNumber + "-" + b);
										if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
										{
											Debugger.debug.println("Done.txt FOUND for " + ConfigInfo.batchNamePrefix + batchNumber + "-" + docNumber);
										}							
									}
									else
									{
										if (doc != null)
										{
											//Call processDocumentRCR
											rcr = processDocumentRCR(batchNumber,docNumber,docClass,doc);
										}
										else
										{
											//System.out.println("Problem creating the Doc object after Process Document");
											if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
											{
												Debugger.debug.println("Problem creating the Doc object after Process Document");
											}
										}
									}
								
									if (doc != null && rcr != null)
									{
										if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
										{
											Debugger.debug.println("==================");
											Debugger.debug.println("Add Doc to Batch");
											Debugger.debug.println("==================");
										}
										//Add Doc to the Batch
										ub.add(doc, null);
										//Add the RCR to the Batch
										ub.add(rcr, null);
										//Increment Files Processed
										FilesProcessed++;
										//Done Start Count
										//doneStartCount = b;
										doneList = doneList + ConfigInfo.importPath + "/" + ConfigInfo.batchNamePrefix + batchNumber + "/" + docNumber + ",";
									}
									else
									{
										//System.out.println("Problem creating the Doc object after RCR");
										if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
										{
											Debugger.debug.println("Done Found or problem with Doc and RCR");
										}
										//Increment Files Processed
										FilesProcessed++;
									}
									
									if (FilesProcessed == filesPerBatchCount)
									{
										if (ub.hasPendingExecute() == true)
										{
											if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
											{
												Debugger.debug.println("====================================");
												Debugger.debug.println("Uploading Documents");
												Debugger.debug.println("FilesProcessed: " + FilesProcessed);
											}
										
											//System.out.println("UpdatingBatch to CE - 5000 Files");
											Import.output.println("============================================");
											Import.output.println(getDateTime() + " Upload to CE...");
										
											//Send Docs to CE
											ub.updateBatch();
										
											if (ub.hasPendingExecute() == false)
											{
												//Reset Files Processed
												FilesProcessed = 0;
												//Output the Number of Batches Processed
												Import.output.println(getDateTime() + " Files Processed: " + filesPerBatchCount);
												Import.output.println("============================================");
											
												//Call processDoneFiles
												processDoneFiles(doneList);
											
												//Reset Done List									
												doneList = "";
											}
											else
											{
												//Output the pending Upload statement
												Import.output.println("============================================");
												Import.output.println(getDateTime() + " Upload to CE pending for " + ConfigInfo.batchNamePrefix + batchNumber);
												Import.output.println("============================================");
											}
										}
										else
										{
											//Reset Files Processed
											FilesProcessed = 0;
											
											//Increment Total Batches Processed
											TotalBatchesProcessed++;
											
											Import.output.println(getDateTime() + " Files Processed: " + filesPerBatchCount);
											Import.output.println("============================================");
											
											//Output the pending Upload statement
											Import.output.println("============================================");
											Import.output.println(getDateTime() + " No Docs in the Batch to Process for " + ConfigInfo.batchNamePrefix + batchNumber);
											Import.output.println("============================================");
										}
									}
									else if (FilesProcessed == filesPerBatchTotal)
									{
										if (ub.hasPendingExecute() == true)
										{
											if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
											{
												Debugger.debug.println("====================================");
												Debugger.debug.println("Uploading Documents");
												Debugger.debug.println("FilesProcessed: " + FilesProcessed);
											}
										
											//System.out.println("UpdatingBatch to CE - 5000 Files");
											Import.output.println("============================================");
											Import.output.println(getDateTime() + " Upload to CE...");
										
											//Send Docs to CE
											ub.updateBatch();
										
											if (ub.hasPendingExecute() == false)
											{
												//doneEndCount = FilesProcessed;
												FilesProcessed = 0;
												//Output the Number of Batches Processed
												//Import.output.println("============================================");
												Import.output.println(getDateTime() + " Files Processed: " + filesPerBatchCount);
												Import.output.println("============================================");
											
												//Call processDoneFiles
												processDoneFiles(doneList);
											
												//Reset Done List									
												doneList = "";
											}
											else
											{
												//Output the pending Upload statement
												Import.output.println("============================================");
												Import.output.println(getDateTime() + " Upload to CE pending for " + ConfigInfo.batchNamePrefix + batchNumber);
												Import.output.println("============================================");
											}
										}
										else
										{
											//Reset Files Processed
											FilesProcessed = 0;
											
											//Increment Total Batches Processed
											TotalBatchesProcessed++;
											
											Import.output.println(getDateTime() + " Files Processed: " + filesPerBatchCount);
											Import.output.println("============================================");
											
											//Output the pending Upload statement
											Import.output.println("============================================");
											Import.output.println(getDateTime() + " No Docs in the Batch to Process for " + ConfigInfo.batchNamePrefix + batchNumber);
											Import.output.println("============================================");
										}
									}
									else
									{
										//Should only get here when doing a partial 5000 batch
									}
									
									//Reset doc
									doc = null;
									//Reset rcr
									rcr = null;
								} //End If for doc directory
								else
								{
									System.out.println(getDateTime() + " Doc Folder: " + docPath + " does not exist.");
									System.out.println("=============================================================");
									//Output the Batch Folder does not exist
									Import.output.println("============================================");
									Import.output.println(getDateTime() + " Doc Folder: " + docPath + " does not exist.");
									Import.output.println("============================================");
								}
							} //End If for comma
						} //End If for line length
					} //End While
					
					//Close Input File
					inputFile1.close();
					
					//Close the Output Log
					Import.output.close();

					//Close the Debug Log
					if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
					{
						Debugger.debug.close();
					}

					//Pop the Connection Subject
					//UserContext.get().popSubject();

					//Reset p8Connection
					//p8Connection = null;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				//System.exit(0);
			}
			finally
			{
				//Pop the Connection Subject
				UserContext.get().popSubject();

				//Reset p8Connection
				p8Connection = null;
			}
		}

		System.out.println("");
		System.out.println("=============================================================");
		System.out.println("Finished Importing Batches");
		System.out.println("Total Batches Processed: " + TotalBatchesProcessed);
		System.out.println(getDateTime());
		System.out.println("=============================================================");
	}
	
	public static void processBatches(File importFolder)
	{
				
		//Loop through Import Folder
		//Example: Batches
		if (importFolder.isDirectory() == true)
		{
			//CE Connection
			Connection p8Connection = null;
			//BatchProcessed
			int BatchProcessed = 0;
			//TotalBatchesProcessed
			int TotalBatchesProcessed = 0;
			//Start Batch Number
			int startBatchNumber = Integer.parseInt(ConfigInfo.batchesStartNumber);
			//totalBatchCount
			int totalBatchCount = Integer.parseInt(ConfigInfo.batchesCount);
			//Batch Folder Test
			File batchFolderTest = null;
			//Batch Result
			int batchResult = 0;
						
			//System.out.println("");
			//System.out.println("=============================================================");
			
			//Import.output.println("Loading:");
			for (int a = 1; a < totalBatchCount + 1; a++)
			{
				try
				{
					//Get Connection
					//Get the Connection
					p8Connection = CEConnection(ConfigInfo.username, ConfigInfo.password, ConfigInfo.p8Stanza, ConfigInfo.uri);
					//p8Connection = CEConnection(ConfigInfo.username, ConfigInfo.password, null, ConfigInfo.uri);
					if (p8Connection != null)
					{
						//Use Debug Log
						//Check if Debugging is turned ON
						if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
						{
							//Initialize the Debug Log
							Debugger.debug = null;
							Debugger.debug = getDebugLog(ConfigInfo.batchNamePrefix + startBatchNumber);
							Debugger.debug.println("=============================================================");
							Debugger.debug.println("Debugger - " + ConfigInfo.batchNamePrefix + startBatchNumber);
							Debugger.debug.println("=============================================================");
							Debugger.debug.println(getDateTime());
						}
						
						System.out.println("=============================================================");
						System.out.println(getDateTime() + " processBatches");
						System.out.println(getDateTime() + " Connection to Content Engine successful");
						if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
						{
							Debugger.debug.println("Connection to CE Successful");
							Debugger.debug.println("=============================================================");
							//Debugger.debug.close();
							//Debugger.debug = null;
						}	
						
						//Get the P8 Domain
						p8Dom = getDomain(p8Connection, null);
						//setP8Domain(p8Dom);
						
						System.out.println(getDateTime() + " Connection to the Domain successful");
						//System.out.println("=============================================================");
						if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
						{
							Debugger.debug.println("Connection to the Domain Successful");
							Debugger.debug.println("=============================================================");
							//Debugger.debug.close();
							//Debugger.debug = null;
						}
						
						//Get the Object Store
						p8OS = getObjectStore(p8Dom, ConfigInfo.objectstore);
						//setP8ObjectStore(p8OS);
						
						System.out.println(getDateTime() + " Connection to the Object Store successful");
						//System.out.println("=============================================================");
						if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
						{
							Debugger.debug.println("Connection to the Object Store Successful");
							Debugger.debug.println("=============================================================");
							//Debugger.debug.close();
							//Debugger.debug = null;
						}
						
						//Verify Start Batch Folder exists
						batchFolderTest = new File (ConfigInfo.importPath + "/" + ConfigInfo.batchNamePrefix + startBatchNumber);
						
						//Use Import Log
						Import.output = null;
						Import.output = getImportLog(ConfigInfo.batchNamePrefix + startBatchNumber);
						
						//Start the Import Log
						Import.output.println("=============================================================");
						Import.output.println("CE Importer - " + ConfigInfo.batchNamePrefix + startBatchNumber);
						Import.output.println("=============================================================");
						Import.output.println(getDateTime());
						
						if (batchFolderTest.isDirectory() == true)
						{
							//Reset BatchProcessed
							BatchProcessed = 0;
							
							//Call processBatch
							batchResult = processBatch(startBatchNumber);
							
							if (batchResult == 1)
							{
								//Set the Batch Processed
								BatchProcessed = 1;
						
								//Output the Number of Batches Processed
								Import.output.println("============================================");
								Import.output.println(getDateTime() + " Batches Processed: " + BatchProcessed);
								Import.output.println("============================================");
						
								//Increment Total Batches Processed
								TotalBatchesProcessed++;
								
								//Reset Batch Result
								batchResult = 0;
							}
							else
							{
								//Output the Number of Batches Processed
								Import.output.println("============================================");
								Import.output.println("There was a problem with " + ConfigInfo.batchNamePrefix + startBatchNumber);
								Import.output.println(getDateTime() + " Batches Processed: " + BatchProcessed);
								Import.output.println("============================================");
							}
						}
						else
						{
							//Output the Batch Folder does not exist
							//System.out.println("=============================================================");
							System.out.println(getDateTime() + " Batch Folder: " + ConfigInfo.batchNamePrefix + startBatchNumber + " does not exist.");
							System.out.println("=============================================================");
							//Output the Batch Folder does not exist
							Import.output.println("============================================");
							Import.output.println(getDateTime() + " Batch Folder: " + ConfigInfo.batchNamePrefix + startBatchNumber + " does not exist.");
							Import.output.println("============================================");
						}
						
						//Reset Batch Folder Test
						batchFolderTest = null;
						
						//Increment Start Batch Number
						startBatchNumber++;
						
						//Close the Output Log
						Import.output.close();
						
						//Close the Debug Log
						if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
						{
							Debugger.debug.close();
						}
						
						//Pop the Connection Subject
						//UserContext.get().popSubject();
						
						//Reset p8Connection
						//p8Connection = null;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					//System.exit(0);
				}
				finally
				{
					//Pop the Connection Subject
					UserContext.get().popSubject();
					
					//Reset p8Connection
					p8Connection = null;
				}
			}
			
			System.out.println("");
			System.out.println("=============================================================");
			System.out.println("Finished Importing Batches");
			System.out.println("Total Batches Processed: " + TotalBatchesProcessed);
			System.out.println("Total Batches Count: " + totalBatchCount);
			System.out.println(getDateTime());
						
		}
		
		/*//Output the Number of Batches Processed
		Import.output.println("");
		Import.output.println("============================================");
		Import.output.println("Finished Importing Batches");
		Import.output.println("Total Batches Processed: " + TotalBatchesProcessed);
		Import.output.println("Total Batches Count: " + totalBatchCount);
		Import.output.println(getDateTime());
		Import.output.println("============================================");
		
		//Close the Output Log
		Import.output.close();*/
		System.out.println("=============================================================");
	}
	
	public static Connection CEConnection(String username, String password, String stanza, String uri)
	{
		// Make connection.
		Connection conn = Factory.Connection.getConnection(uri);
		Subject subject = null;
	    if (stanza.equals("") == false)
	    {
	    	subject = UserContext.createSubject(conn, username, password, stanza);
	    }
	    else
	    {
	    	subject = UserContext.createSubject(conn, username, password, null);
	    }
	    UserContext.get().pushSubject(subject);
	    return conn;
	}
	
	public static Domain getDomain(Connection conn, String p8Domain)
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("====================================");
			Debugger.debug.println("Entered getDomain Method");
		}
		Domain domain = Factory.Domain.fetchInstance(conn, p8Domain, null);
		//Domain domain = Factory.Domain.getInstance(conn, p8Domain);
		if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			Debugger.debug.println("Domain: " + domain.get_Name());
		}
		return domain;
	}
	
	public static ObjectStore getObjectStore(Domain domain, String objectstore)
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			Debugger.debug.println("====================================");
			Debugger.debug.println("Entered getObjectStore Method");
		}
		ObjectStore store = Factory.ObjectStore.fetchInstance(domain, objectstore, null);
		//ObjectStore store = Factory.ObjectStore.getInstance(domain, objectstore);
		if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			Debugger.debug.println("Object Store: " + store.get_DisplayName());
		}
		return store;
	}
	/*public static boolean deleteSubFolders(String rootPath, Folder f)
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			Debugger.debug.println("Entered deleteSubFolders Method");
			Debugger.debug.println("Path: " + rootPath);
			Debugger.debug.println("Folder: " + f.get_Name());
		}
		String folderPathToRemove = "";
		String root = rootPath;
		Folder startFolder = f;
		boolean done = false;
				
		if (f.get_SubFolders().isEmpty() == false)
		{
			while (done == false)
			{
				folderPathToRemove = getSubFolders(root,startFolder);
				if (folderPathToRemove.equals(rootPath) == true)
				{
					done = true;
					folderPathToRemove = "";
					root = "";
					startFolder = null;
				}
				else
				{
					if (checkFolderInCE(p8OS,folderPathToRemove).equals("") == false)
					{
						Folder removeFolder = Factory.Folder.fetchInstance(p8OS, folderPathToRemove, null);
						if (Integer.parseInt(ConfigInfo.useDebugLog) > 1)
						{
							Debugger.debug.println("Before Delete: " + folderPathToRemove);
						}
						//Delete the Folder
						//deleteFolder(removeFolder);
						//Delete the Documents
						deleteDocuments(removeFolder);						
						//Reset the Folder Path to the Next Level Up
						folderPathToRemove = folderPathToRemove.substring(0, folderPathToRemove.lastIndexOf("/"));
						if (Integer.parseInt(ConfigInfo.useDebugLog) > 1)
						{
							Debugger.debug.println("After Delete: " + folderPathToRemove);
						}
					}
					//Reset Folder and Paths
					root = folderPathToRemove;
					startFolder = Factory.Folder.fetchInstance(p8OS, root, null);
					folderPathToRemove = "";
					done = false;
				}
			}
		}
		return done;
	}*/
	
	/* public static boolean deleteFolders(Folder f)
	{
		try
		{
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				Debugger.debug.println("Entered deleteDocuments Method");
				Debugger.debug.println("Folder: " + f.get_Name());
			}

			boolean done = false;
			int pageSizePreFetch = 20;
			int pageSizeFetch = 20;

			Property prop = f.fetchProperty("SubFolders", null, new Integer(pageSizePreFetch));
			IndependentObjectSet objectSet = prop.getIndependentObjectSetValue();

			//Initialize page iterator
			PageIterator it = objectSet.pageIterator();
			it.setPageSize(pageSizeFetch);

			//Document doc = null;
			while (it.nextPage() == true)
			{
				Object[] pageObjects = it.getCurrentPage();
				for (int index = 0; index < pageObjects.length; index++)
				{
					Object elementObject = pageObjects[index];
					if (elementObject instanceof Folder)
					{
						Folder elementFolder = (Folder) elementObject;
						elementFolder.refresh();
						if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
						{
							Debugger.debug.println("Folder Found");
						}
						if (elementFolder.get_ContainedDocuments().isEmpty() == false)
						{
							DocumentSet ds = elementFolder.get_ContainedDocuments();
							deleteDocuments(ds);
						}
						else if (elementFolder.get_SubFolders().isEmpty() == false)
						{
							done = deleteFolders(elementFolder);
						}
						else
						{
							deleteFolder(elementFolder);
							done = true;
						}
					}
					else
					{
						//Should Not Get Here
					}
				}
			}
			return done;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
	}
	*/
	
	public static String getSubFolders(String fPath, Folder f)
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			Debugger.debug.println("Entered getSubFolders Method");
			Debugger.debug.println("Path: " + fPath);
			Debugger.debug.println("SubFolder: " + f.get_Name());
		}
		
		FolderSet fs = f.get_SubFolders();
		String folderPath = fPath;
		
		if (fs.isEmpty() == false)
		{
			Iterator it = fs.iterator();
			Folder folder = null;
			String extraPath = "";
			while (it.hasNext())
			{
				folder = (Folder) it.next();
				if (Integer.parseInt(ConfigInfo.useDebugLog) > 1)
				{
					Debugger.debug.println("SubFolder: " + folder.get_Name());
				}
				if (folder.get_SubFolders().isEmpty() == false)
				{
					String tempPath = "";
					tempPath = folderPath + "/" + folder.get_Name();
					if (Integer.parseInt(ConfigInfo.useDebugLog) > 1)
					{
						Debugger.debug.println("extraPath - New Call: " + tempPath);
					}
					extraPath = getSubFolders(tempPath,folder);
					break;
				}
				else
				{
					if (Integer.parseInt(ConfigInfo.useDebugLog) > 1)
					{
						Debugger.debug.println("extraPath - Break: " + folderPath + "/" + folder.get_Name());
					}
					extraPath = folderPath + "/" + folder.get_Name();
					break;
				}
			}
			if (folder != null)
			{
				folderPath = extraPath;
			}
			if (Integer.parseInt(ConfigInfo.useDebugLog) > 1)
			{
				Debugger.debug.println("getSubFolders after While -> folderPath: " + folderPath);
			}
		}
		else
		{
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				Debugger.debug.println("getSubFolders else -> folderPath: " + folderPath);
			}
			//folderPath = "Done";
		}
		return folderPath;
	}
	
	public static void deleteFolder(Folder f)
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			Debugger.debug.println("Entered deleteFolder Method");
			Debugger.debug.println("Folder: " + f.get_Name());
		}
		f.refresh();
		f.delete();
		f.save(RefreshMode.NO_REFRESH);
		if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			Debugger.debug.println("Folder deleted");
		}
	}
	
	public static void setFolderSecurity(Folder folder) throws Exception
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			Debugger.debug.println("====================================");
			Debugger.debug.println("Entered setFolderSecurity Method");
			Debugger.debug.println("Folder: " + folder.get_FolderName());
		}
		
		if (folder != null)
		{
			int i = 0;
			//Get the Existing Access Permission List
			AccessPermissionList apl = folder.get_Permissions();

			if (ConfigInfo.updateDocSecurityPrincipals.contains(",") == true)
			{
				if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
				{
					Debugger.debug.println("More than 1 User or Group");
				}
				//Get the List of New Security Principals to Add
				String[] folderSecPrincipals = ConfigInfo.updateDocSecurityPrincipals.split(",");

				for (int a = 0; a < folderSecPrincipals.length; a++)
				{
					//Add New Access Permission
					AccessPermission ap = Factory.AccessPermission.createInstance();
					int Perms = 0;
					ap.set_GranteeName(folderSecPrincipals[a]);
					ap.set_AccessType(AccessType.ALLOW);

					if (ConfigInfo.updateDocSecurityPerms.contains(",") == true)
					{							
						//Get the List of New Security Permissions to Add
						String[] folderSecPerms = ConfigInfo.updateDocSecurityPerms.split(",");

						if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
						{
							Debugger.debug.println("More than 1 Perm");
						}

						for (int b = 0; b < folderSecPerms.length; b++)
						{
							int tempPerm = 0;
							tempPerm = getSecurityPermissionsValue("Folder",folderSecPerms[b]);
							Perms = Perms + tempPerm;
						}
						//Reset docSecPerms
						folderSecPerms = null;
					}
					else
					{
						Perms = getSecurityPermissionsValue("Folder",ConfigInfo.updateDocSecurityPerms);
					}
					//Update the Access Mask
					ap.set_AccessMask(Perms);

					if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
					{
						Debugger.debug.println("Grantee Name: " + folderSecPrincipals[a]);
						Debugger.debug.println("Access Type: Allow");
						Debugger.debug.println("Access Mask: " + Perms);
						Debugger.debug.println("Add Permission to the List");
						Debugger.debug.println("====================================");
					}

					//Add the Permissions to the List
					apl.add(ap);
				}
				//Reset docSecPrincipals
				folderSecPrincipals = null;
			}
			else //1 User or Group
			{
				if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
				{
					Debugger.debug.println("1 User or Group");
				}
				//Add New Access Permission
				AccessPermission ap = Factory.AccessPermission.createInstance();
				int Perms = 0;
				ap.set_GranteeName(ConfigInfo.updateDocSecurityPrincipals);
				ap.set_AccessType(AccessType.ALLOW);

				if (ConfigInfo.updateDocSecurityPerms.contains(",") == true)
				{							
					//Get the List of New Security Permissions to Add
					String[] folderSecPerms = ConfigInfo.updateDocSecurityPerms.split(",");

					if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
					{
						Debugger.debug.println("More than 1 Perm");
					}

					for (int b = 0; b < folderSecPerms.length; b++)
					{
						int tempPerm = 0;
						tempPerm = getSecurityPermissionsValue("Folder",folderSecPerms[b]);
						Perms = Perms + tempPerm;
					}
					//Reset docSecPerms
					folderSecPerms = null;
				}
				else
				{
					Perms = getSecurityPermissionsValue("Folder",ConfigInfo.updateDocSecurityPerms);
				}
				//Update the Access Mask
				ap.set_AccessMask(Perms);

				if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
				{
					Debugger.debug.println("Grantee Name: " + ConfigInfo.updateDocSecurityPrincipals);
					Debugger.debug.println("Access Type: Allow");
					Debugger.debug.println("Access Mask: " + Perms);
					Debugger.debug.println("Add Permission to the List");
					Debugger.debug.println("====================================");
				}

				//Add the Permissions to the List
				apl.add(ap);
			}

			//Add the Permissions List to the Folder
			folder.set_Permissions(apl);

			//Save the Document
			folder.save(RefreshMode.REFRESH);
			i++;


			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				Debugger.debug.println("====================================");
				Debugger.debug.println("Folder Security Applied");
				Debugger.debug.println("====================================");
			}
			
			Import.output.println(getDateTime() + " Folder Security Applied to " + folder.get_FolderName());
			
			//System.out.println("Folder Security Applied to " + i + " Folder");

			//System.out.println("====================================");
		}
	}
	
	public static void setSubFolderSecurity(FolderSet folderSet, String folderPath) throws Exception
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			Debugger.debug.println("====================================");
			Debugger.debug.println("Entered setSubFolderSecurity Method");
		}
		
		if (folderSet != null && !folderSet.isEmpty())
		{
			Iterator folderSetIterator = folderSet.iterator();
			int i = 0;
			//String docNames = "";
			while (folderSetIterator.hasNext())
			{
				//Folder oneFolder = (Folder) folderSetIterator.next();
				i = 0;
				UpdatingBatch updatingBatch = UpdatingBatch.createUpdatingBatchInstance(p8Dom, RefreshMode.REFRESH);
				boolean executeBatch = false;

				int foldersToGetPerBatch = 2000;
				//String[] docNamesData = new String[docsToGetPerBatch];
				for (int x = 0; x < foldersToGetPerBatch; x++)
				{
					Folder oneFolder = (Folder) folderSetIterator.next();
					
					if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
					{
						Debugger.debug.println("====================================");
						Debugger.debug.println("Updating Folder: " + oneFolder.get_FolderName());
					}
					
					//Get the Existing Access Permission List
					AccessPermissionList apl = oneFolder.get_Permissions();
					
					if (ConfigInfo.updateDocSecurityPrincipals.contains(",") == true)
					{
						if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
						{
							Debugger.debug.println("More than 1 User or Group");
						}
						//Get the List of New Security Principals to Add
						String[] folderSecPrincipals = ConfigInfo.updateDocSecurityPrincipals.split(",");

						for (int a = 0; a < folderSecPrincipals.length; a++)
						{
							//Add New Access Permission
							AccessPermission ap = Factory.AccessPermission.createInstance();
							int Perms = 0;
							ap.set_GranteeName(folderSecPrincipals[a]);
							ap.set_AccessType(AccessType.ALLOW);
							
							if (ConfigInfo.updateDocSecurityPerms.contains(",") == true)
							{							
								//Get the List of New Security Permissions to Add
								String[] folderSecPerms = ConfigInfo.updateDocSecurityPerms.split(",");

								if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
								{
									Debugger.debug.println("More than 1 Perm");
								}

								for (int b = 0; b < folderSecPerms.length; b++)
								{
									int tempPerm = 0;
									tempPerm = getSecurityPermissionsValue("Folder",folderSecPerms[b]);
									Perms = Perms + tempPerm;
								}
								//Reset docSecPerms
								folderSecPerms = null;
							}
							else
							{
								Perms = getSecurityPermissionsValue("Folder",ConfigInfo.updateDocSecurityPerms);
							}
							//Update the Access Mask
							ap.set_AccessMask(Perms);
							
							if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
							{
								Debugger.debug.println("Grantee Name: " + folderSecPrincipals[a]);
								Debugger.debug.println("Access Type: Allow");
								Debugger.debug.println("Access Mask: " + Perms);
								Debugger.debug.println("Add Permission to the List");
								Debugger.debug.println("====================================");
							}
							
							//Add the Permissions to the List
							apl.add(ap);
						}
						//Reset docSecPrincipals
						folderSecPrincipals = null;
					}
					else //1 User or Group
					{
						if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
						{
							Debugger.debug.println("1 User or Group");
						}
						//Add New Access Permission
						AccessPermission ap = Factory.AccessPermission.createInstance();
						int Perms = 0;
						ap.set_GranteeName(ConfigInfo.updateDocSecurityPrincipals);
						ap.set_AccessType(AccessType.ALLOW);
						
						if (ConfigInfo.updateDocSecurityPerms.contains(",") == true)
						{							
							//Get the List of New Security Permissions to Add
							String[] folderSecPerms = ConfigInfo.updateDocSecurityPerms.split(",");

							if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
							{
								Debugger.debug.println("More than 1 Perm");
							}

							for (int b = 0; b < folderSecPerms.length; b++)
							{
								int tempPerm = 0;
								tempPerm = getSecurityPermissionsValue("Folder",folderSecPerms[b]);
								Perms = Perms + tempPerm;
							}
							//Reset docSecPerms
							folderSecPerms = null;
						}
						else
						{
							Perms = getSecurityPermissionsValue("Folder",ConfigInfo.updateDocSecurityPerms);
						}
						//Update the Access Mask
						ap.set_AccessMask(Perms);
						
						if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
						{
							Debugger.debug.println("Grantee Name: " + ConfigInfo.updateDocSecurityPrincipals);
							Debugger.debug.println("Access Type: Allow");
							Debugger.debug.println("Access Mask: " + Perms);
							Debugger.debug.println("Add Permission to the List");
							Debugger.debug.println("====================================");
						}
						
						//Add the Permissions to the List
						apl.add(ap);
					}
					
					//Add the Permissions List to the Folder
					oneFolder.set_Permissions(apl);

					//Save the Document
					oneFolder.save(RefreshMode.REFRESH);

					//Add the Document to the Batch				
					updatingBatch.add(oneFolder, null);
					executeBatch = true;
					i++;
					
					//Output to Log
					Import.output.println(getDateTime() + " Folder Security Applied to " + folderNamingPath + "/" + oneFolder.get_FolderName());
					
					String prevPath = folderPath;
					
					//Check for more SubFolders
					if (oneFolder.get_SubFolders().isEmpty() == false)
					{
						//Update FolderNamingPath
						folderNamingPath = folderNamingPath + "/" + oneFolder.get_FolderName();
						FolderSet subFolderFolderSet = oneFolder.get_SubFolders();
						setSubFolderSecurity(subFolderFolderSet,folderNamingPath);
						folderNamingPath = prevPath;
					}
					//Check for Documents
					if (oneFolder.get_ContainedDocuments().isEmpty() == false)
					{
						//Update FolderNamingPath
						//folderNamingPath = folderNamingPath + "/" + oneFolder.get_FolderName();
						DocumentSet subFolderDocumentSet = oneFolder.get_ContainedDocuments();
						setDocumentSecurity(subFolderDocumentSet);
						folderNamingPath = prevPath;
					}
					
					if (!folderSetIterator.hasNext())
					{
						//Import.output.println(getDateTime() + " Entered SubFolder Break " + folderNamingPath);
						break;
					}
				} //End For Loop	
														
				if (executeBatch)
				{
					//Update Batch
					updatingBatch.updateBatch();

					if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
					{
						Debugger.debug.println("====================================");
						Debugger.debug.println("Folder Security Applied");
						Debugger.debug.println("====================================");
					}
					//System.out.println("Folder Security Applied to " + i + " Folder");
				}

				//System.out.println("====================================");
			} //End While
		}
	}
	
	public static void setDocumentSecurity(DocumentSet documentSet) throws Exception
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			Debugger.debug.println("====================================");
			Debugger.debug.println("Entered setDocumentSecurity Method");
		}
		
		if (documentSet != null && !documentSet.isEmpty())
		{
			Iterator documentSetIterator = documentSet.iterator();
			int i = 0;
			//String docNames = "";
			while (documentSetIterator.hasNext())
			{
				i = 0;
				UpdatingBatch updatingBatch = UpdatingBatch.createUpdatingBatchInstance(p8Dom, RefreshMode.REFRESH);
				boolean executeBatch = false;

				int docsToGetPerBatch = 2000;
				//String[] docNamesData = new String[docsToGetPerBatch];
				for (int x = 0; x < docsToGetPerBatch; x++)
				{
					Document document = (Document) documentSetIterator.next();
					
					if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
					{
						Debugger.debug.println("====================================");
						Debugger.debug.println("Updating Document: " + document.get_Name());
					}
					
					//Get the Existing Access Permission List
					AccessPermissionList apl = document.get_Permissions();
					
					if (ConfigInfo.updateDocSecurityPrincipals.contains(",") == true)
					{
						if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
						{
							Debugger.debug.println("More than 1 User or Group");
						}
						//Get the List of New Security Principals to Add
						String[] docSecPrincipals = ConfigInfo.updateDocSecurityPrincipals.split(",");

						for (int a = 0; a < docSecPrincipals.length; a++)
						{
							//Add New Access Permission
							AccessPermission ap = Factory.AccessPermission.createInstance();
							int Perms = 0;
							ap.set_GranteeName(docSecPrincipals[a]);
							ap.set_AccessType(AccessType.ALLOW);
							
							if (ConfigInfo.updateDocSecurityPerms.contains(",") == true)
							{							
								//Get the List of New Security Permissions to Add
								String[] docSecPerms = ConfigInfo.updateDocSecurityPerms.split(",");

								if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
								{
									Debugger.debug.println("More than 1 Perm");
								}

								for (int b = 0; b < docSecPerms.length; b++)
								{
									int tempPerm = 0;
									tempPerm = getSecurityPermissionsValue("Document",docSecPerms[b]);
									Perms = Perms + tempPerm;
								}
								//Reset docSecPerms
								docSecPerms = null;
							}
							else
							{
								Perms = getSecurityPermissionsValue("Document",ConfigInfo.updateDocSecurityPerms);
							}
							//Update the Access Mask
							ap.set_AccessMask(Perms);
							
							if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
							{
								Debugger.debug.println("Grantee Name: " + docSecPrincipals[a]);
								Debugger.debug.println("Access Type: Allow");
								Debugger.debug.println("Access Mask: " + Perms);
								Debugger.debug.println("Add Permission to the List");
								Debugger.debug.println("====================================");
							}
							
							//Add the Permissions to the List
							apl.add(ap);
						}
						//Reset docSecPrincipals
						docSecPrincipals = null;
					}
					else //1 User or Group
					{
						if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
						{
							Debugger.debug.println("1 User or Group");
						}
						//Add New Access Permission
						AccessPermission ap = Factory.AccessPermission.createInstance();
						int Perms = 0;
						ap.set_GranteeName(ConfigInfo.updateDocSecurityPrincipals);
						ap.set_AccessType(AccessType.ALLOW);
						
						if (ConfigInfo.updateDocSecurityPerms.contains(",") == true)
						{							
							//Get the List of New Security Permissions to Add
							String[] docSecPerms = ConfigInfo.updateDocSecurityPerms.split(",");

							if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
							{
								Debugger.debug.println("More than 1 Perm");
							}

							for (int b = 0; b < docSecPerms.length; b++)
							{
								int tempPerm = 0;
								tempPerm = getSecurityPermissionsValue("Document",docSecPerms[b]);
								Perms = Perms + tempPerm;
							}
							//Reset docSecPerms
							docSecPerms = null;
						}
						else
						{
							Perms = getSecurityPermissionsValue("Document",ConfigInfo.updateDocSecurityPerms);
						}
						//Update the Access Mask
						ap.set_AccessMask(Perms);
						
						if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
						{
							Debugger.debug.println("Grantee Name: " + ConfigInfo.updateDocSecurityPrincipals);
							Debugger.debug.println("Access Type: Allow");
							Debugger.debug.println("Access Mask: " + Perms);
							Debugger.debug.println("Add Permission to the List");
							Debugger.debug.println("====================================");
						}
						
						//Add the Permissions to the List
						apl.add(ap);
					}
					
					//Add the Permissions List to the Doc
					document.set_Permissions(apl);

					//Save the Document
					document.save(RefreshMode.REFRESH);

					//Add the Document to the Batch				
					updatingBatch.add(document, null);
					executeBatch = true;
					i++;
					
					//Output to Log
					Import.output.println(getDateTime() + " Document Security Applied to " + document.get_Name());
					
					if (!documentSetIterator.hasNext())
					{
						break;
					}
				}
														
				if (executeBatch)
				{
					//Update Batch
					updatingBatch.updateBatch();

					if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
					{
						Debugger.debug.println("====================================");
						Debugger.debug.println("Document Security Applied");
						Debugger.debug.println("====================================");
					}
					//System.out.println("Document Security Applied to " + i + " Documents");
				}

				//System.out.println("====================================");
			}
		}
	}
	
	public static void getDocumentSecurity(DocumentSet documentSet) throws Exception
	{
		if (documentSet != null && !documentSet.isEmpty())
		{
			Iterator documentSetIterator = documentSet.iterator();
			int i = 0;
			//String docNames = "";
			while (documentSetIterator.hasNext())
			{
				i = 0;
				RetrievingBatch retrievingBatch = RetrievingBatch.createRetrievingBatchInstance(p8Dom);
				boolean executeBatch = false;

				int docsToGetPerBatch = 2000;
				String[] docNamesData = new String[docsToGetPerBatch];
				for (int x = 0; x < docsToGetPerBatch; x++)
				{
					Document document = (Document) documentSetIterator.next();
					String tempDoc = document.get_Name();			
					docNamesData[x] = tempDoc + "~";
					//AccessPermissionList
					AccessPermissionList apl;
					//Get Access Permissions List from Folder
					apl = document.get_Permissions();
					
					Iterator docPermsIterator = apl.iterator();
					while (docPermsIterator.hasNext())
					{
						AccessPermission ap = (AccessPermission) docPermsIterator.next();
						docNamesData[x] = docNamesData[x] + ap.get_GranteeName() + "," + ap.get_AccessType().toString() + "," + ap.get_AccessMask().toString() + "~";				
					}
					//Cleanup docNamesData
					String tempDocNamesData = "";
					tempDocNamesData = docNamesData[x].substring(0, docNamesData[x].length() - 1);
					docNamesData[x] = tempDocNamesData;
					tempDocNamesData = "";
					
					retrievingBatch.add(document, null);
					executeBatch = true;
					i++;
										
					if (!documentSetIterator.hasNext())
					{
						break;
					}
				}
				
				if (executeBatch)
				{
					//Retrieve Batch
					retrievingBatch.retrieveBatch();
					
					if (docNamesData.length > 0)
					{
						//Output Doc Permissions					
						System.out.println("Total Docs Scanned for Permissions: " + docNamesData.length);
						for (int x = 0; x < docNamesData.length; x++)
						{
							if (docNamesData[x] != null)
							{
								String[] docNamesData2 = null;
								docNamesData2 = docNamesData[x].split("~");
								if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
								{
									Debugger.debug.println("====================================");
									Debugger.debug.println("Security Permissions on Document: " + docNamesData2[0]);
								}
								for (int y = 1; y < docNamesData2.length; y++)
								{
									if (docNamesData2[y].length() > 0)
									{
										String[] docNamesData3 = null;
										docNamesData3 = docNamesData2[y].split(",");
										if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
										{
											Debugger.debug.println("Grantee Name: " + docNamesData3[0]);
											Debugger.debug.println("Access Type: " + docNamesData3[1]);
											Debugger.debug.println("Access Mask: " + docNamesData3[2]);
											Debugger.debug.println("====================================");
										}
										//Reset docNamesData3
										docNamesData3 = null;
									}
								}
								//Reset docNamesData2
								docNamesData2 = null;
							}
						}
						//Reset DocNames
						docNamesData = null;
					}
				}
								
				//System.out.println("Successful Docs: " + i);
				System.out.println("======================");
			}
		}
	}
	
	public static void getDocuments(DocumentSet documentSet) throws Exception
	{
		if (documentSet != null && !documentSet.isEmpty())
		{
			Iterator documentSetIterator = documentSet.iterator();
			int i = 0;
			//String docNames = "";
			while (documentSetIterator.hasNext())
			{
				i = 0;
				RetrievingBatch retrievingBatch = RetrievingBatch.createRetrievingBatchInstance(p8Dom);
				boolean executeBatch = false;

				int docsToGetPerBatch = 2000;
				String[] docNamesData = new String[docsToGetPerBatch];
				for (int x = 0; x < docsToGetPerBatch; x++)
				{
					Document document = (Document) documentSetIterator.next();
					//docNames = document.get_Name() + "," + docNames;
					if (document.get_Name().equals("") == false)
					{
						Properties props = document.getProperties();
						docNamesData[x] = props.getStringValue("DocumentTitle");
						//System.out.println("Doc: " + document.get_Name());
						//System.out.println("DocName: " + docNames);
						retrievingBatch.add(document, null);
						executeBatch = true;
						i++;
					}
					if (!documentSetIterator.hasNext())
					{
						break;
					}
				}
				
				if (executeBatch)
				{
					//Retrieve Batch
					retrievingBatch.retrieveBatch();
					
					if (docNamesData.length > 0)
					{
						//Output DocNames
						
						//docNamesData = docNames.split(",");
						
						System.out.println("Maximum Docs: " + docNamesData.length);
						for (int x = 0; x < docNamesData.length; x++)
						{
							if (docNamesData[x] != null)
							{
								if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
								{
									Debugger.debug.println(docNamesData[x]);
								}
								//System.out.println("DocName: " + docNamesData[x]);
							}
						}
						//Reset DocNames
						docNamesData = null;
					}
				}
				
				System.out.println("Successful Docs: " + i);
				System.out.println("======================");
			}
		}
	}
	
	public static void deletePendingDocuments()
	{
		try
		{
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				Debugger.debug.println("====================================");
				Debugger.debug.println("Entered deletePendingDocuments Method");
				//Debugger.debug.println("Object Store: " + p8ObjectStore.get_DisplayName());
			}

			//CE Connection
			Connection p8Connection = null;

			//Get the Connection
			p8Connection = CEConnection(ConfigInfo.username, ConfigInfo.password, ConfigInfo.p8Stanza, ConfigInfo.uri);

			if (p8Connection != null)
			{
				System.out.println("=============================================================");
				System.out.println(getDateTime() + " deletePendingDocuments");
				System.out.println(getDateTime() + " Connection to Content Engine successful");
				
				//Get the P8 Domain
				p8Dom = getDomain(p8Connection, null);
				if (p8Dom != null)
				{
					System.out.println(getDateTime() + " Connection to the Domain successful");

					//Get the Object Store
					p8OS = getObjectStore(p8Dom, ConfigInfo.objectstore);
					if (p8OS != null)
					{
						System.out.println(getDateTime() + " Connection to the Object Store successful");
						System.out.println("=============================================================");
						
						//SearchScope
						SearchScope searchScope = new SearchScope(p8OS);
						//SearchSQL
						SearchSQL searchSQL = new SearchSQL();
						//Build the SQL Statement
						String sql = "select d.this from Document d " + "where VersionStatus = 3";
						//Set Max Records to Process to 500
						//searchSQL.setMaxRecords(500);
						//Set the SQL query
						searchSQL.setQueryString(sql);
						//Independent Object Set
						IndependentObjectSet objectSet = searchScope.fetchObjects(searchSQL, null, null, null);
						int i = 0;
						Iterator iter = objectSet.iterator();
						int docsToDeletePerBatch = 2000;
						while (iter.hasNext() == true)
						{
							UpdatingBatch updatingBatch = UpdatingBatch.createUpdatingBatchInstance(p8Dom, RefreshMode.NO_REFRESH);
							boolean executeBatch = false;

							for (int x = 0; x < docsToDeletePerBatch; x++)
							{
								Document document = (Document) iter.next();
								document.refresh();
								if (document.get_VersionStatus() == VersionStatus.RESERVATION)
								{
									document.setUpdateSequenceNumber(null);
									document.delete();
									updatingBatch.add(document, null);
									executeBatch = true;
									i++;
								}
								if (!iter.hasNext())
								{
									break;
								}
							}
							if (executeBatch)
							{
								updatingBatch.updateBatch();
							}
							System.out.println(getDateTime() + " Number of documents deleted: " + i);
							if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
							{
								Debugger.debug.println(getDateTime() + " Number of documents deleted: " + i);
							}
						}							
					}
				}
			}

			//Release the Connection
			p8Connection = null;
			p8Dom = null;
			p8OS = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void deleteDocuments(DocumentSet documentSet) throws Exception
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			Debugger.debug.println("====================================");
			Debugger.debug.println("Entered deleteDocuments Method");
		}
		
		if (documentSet != null && !documentSet.isEmpty())
		{
			Iterator documentSetIterator = documentSet.iterator();
			int i = 0;
			while (documentSetIterator.hasNext())
			{
				i = 0;
				UpdatingBatch updatingBatch = UpdatingBatch.createUpdatingBatchInstance(p8Dom, RefreshMode.NO_REFRESH);
				boolean executeBatch = false;

				int docsToDeletePerBatch = 2000;
				for (int x = 0; x < docsToDeletePerBatch; x++)
				{
					Document document = (Document) documentSetIterator.next();
					document.refresh();
					document.delete();
					updatingBatch.add(document, null);
					executeBatch = true;
					i++;
					if (!documentSetIterator.hasNext())
					{
						break;
					}
				}
				
				if (executeBatch)
				{
					updatingBatch.updateBatch();
				}
				//System.out.println(getDateTime() + " Number of documents deleted: " + i);
				if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
				{
					Debugger.debug.println(getDateTime() + " Number of documents deleted: " + i);
				}
			}
		}
	}
	
	public static void deleteDocumentsByList(String docList)
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			Debugger.debug.println("====================================");
			Debugger.debug.println("Entered deleteDocumentsByList Method");
		}
		
		if (docList.equals("") == false)
		{
			try
			{
				//CE Connection
				Connection p8Connection = null;

				//Get the Connection
				p8Connection = CEConnection(ConfigInfo.username, ConfigInfo.password, ConfigInfo.p8Stanza, ConfigInfo.uri);

				if (p8Connection != null)
				{
					System.out.println("=============================================================");
					System.out.println(getDateTime() + " deleteDocumentsByList");
					System.out.println(getDateTime() + " Connection to Content Engine successful");

					//Get the P8 Domain
					p8Dom = getDomain(p8Connection, null);
					if (p8Dom != null)
					{
						System.out.println(getDateTime() + " Connection to the Domain successful");

						//Get the Object Store
						p8OS = getObjectStore(p8Dom, ConfigInfo.objectstore);
						if (p8OS != null)
						{
							System.out.println(getDateTime() + " Connection to the Object Store successful");
							System.out.println("=============================================================");
							
							if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
							{
								Debugger.debug.println("====================================");
								Debugger.debug.println("Get Doc List File");
							}
							
							//Reader for Doc List File
							BufferedReader docListFile = getReader(docList);
							//String for one Document
							String oneDoc = "";
							//Count for Deleted Docs
							int deleteCount = 0;
							int deleteBatchCount = 0;
				
							UpdatingBatch updatingBatch = UpdatingBatch.createUpdatingBatchInstance(p8Dom, RefreshMode.NO_REFRESH);
							//boolean executeBatch = false;
							int docsToDeletePerBatch = 2000;
							
							if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
							{
								Debugger.debug.println("====================================");
								Debugger.debug.println("Before Reading Doc List File");
							}
							
							while ((oneDoc = docListFile.readLine()) != null)
							{
								if (oneDoc.length() > 0)
								{
									if (oneDoc.contains("{") == true)
									{
										if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
										{
											Debugger.debug.println("Doc ID: " + oneDoc);
										}
										
										//SearchScope
										SearchScope searchScope = new SearchScope(p8OS);
										//SearchSQL
										SearchSQL searchSQL = new SearchSQL();
										//Build the SQL Statement
										String sql = "select d.this,d.Id from Document d " + "where d.Id = " + oneDoc;
										//Set Max Records to Process to 1
										searchSQL.setMaxRecords(1);
										//Set the Time Limit to 30 seconds
										searchSQL.setTimeLimit(30);
										//Set the SQL query
										searchSQL.setQueryString(sql);
										//Independent Object Set
										IndependentObjectSet objectSet = searchScope.fetchObjects(searchSQL, null, null, null);
										Iterator iter = objectSet.iterator();
										
										if (iter.hasNext() == true)
										{
											Document doc = (Document) iter.next();
											doc.refresh();
											doc.delete();
											if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
											{
												Debugger.debug.println("Doc ID: " + oneDoc + " Deleted.");
											}
											updatingBatch.add(doc, null);
											deleteBatchCount++;
											deleteCount++;
											if (deleteBatchCount == docsToDeletePerBatch)
											{
												if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
												{
													Debugger.debug.println("Deleting " + deleteBatchCount + " Docs.");
												}
												updatingBatch.updateBatch();
												//executeBatch = false;
												deleteBatchCount = 0;
												updatingBatch = null;
											}

											//Reset doc
											doc = null;
										}
										else
										{
											if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
											{
												Debugger.debug.println("Doc ID: " + oneDoc + " does NOT exist.");
											}
										}
										
										//Reset oneDoc
										oneDoc = "";
									}
								}
							}
							//Close the file
							docListFile.close();
							
							if (updatingBatch.hasPendingExecute() == true)
							{
								if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
								{
									Debugger.debug.println("Deleting " + deleteBatchCount + " Docs.");
								}
								updatingBatch.updateBatch();
								//executeBatch = false;
								deleteBatchCount = 0;
								updatingBatch = null;
							}
																	
							System.out.println(getDateTime() + " Number of Documents Deleted: " + deleteCount);
							
							if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
							{
								Debugger.debug.println("====================================");
								Debugger.debug.println(getDateTime() + " Number of Documents Deleted: " + deleteCount);
							}
						}
					}
				}
				//Release the Connection
				p8Connection = null;
				p8Dom = null;
				p8OS = null;
			}
			catch (Exception e)
			{
				System.out.println(getDateTime());
				System.out.println("Error has occurred establishing a connection or with the reading of the Doc List File.");
				e.printStackTrace();
				System.exit(0);
			}
		}
		else
		{
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				Debugger.debug.println(getDateTime() + " Not a valid Doc List File passed in.");
			}
		}
	}
	
	public static int getSecurityPermissionsValue(String secType, String secPerm)
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			Debugger.debug.println("====================================");
			Debugger.debug.println("Entered getSecurityPermissionsValue Method");
			Debugger.debug.println("SecType: " + secType);
			Debugger.debug.println("SecPerm: " + secPerm);
		}
		
		int secPermValue = 0;
		if (secType.equals("Document") == true)
		{
			//Full Control of Document
			if (secPerm.equals("Full") == true)
			{
				secPermValue = AccessLevel.FULL_CONTROL_DOCUMENT_AS_INT;
			}
			//Modify Control of Document
			else if (secPerm.equals("Write") == true)
			{
				secPermValue = AccessLevel.WRITE_DOCUMENT_AS_INT;
			}
			//View Content of Document
			else if (secPerm.equals("Read") == true)
			{
				secPermValue = AccessLevel.VIEW_AS_INT;
			}
			//Annotate Content of Document
			else if (secPerm.equals("Link") == true)
			{
				secPermValue = AccessRight.LINK_AS_INT;
			}
			else
			{
				//Should Never Get Here
			}
		}
		else if (secType.equals("Folder") == true)
		{
			//Full Control of Folder
			if (secPerm.equals("Full") == true)
			{
				secPermValue = AccessLevel.FULL_CONTROL_FOLDER_AS_INT;
			}
			//Modify Control of Folder
			else if (secPerm.equals("Write") == true)
			{
				secPermValue = AccessLevel.WRITE_FOLDER_AS_INT;
			}
			//View Content of Folder
			else if (secPerm.equals("Read") == true)
			{
				secPermValue = AccessLevel.VIEW_AS_INT;
			}
			//Annotate Content of Folder - can't do
			else if (secPerm.equals("Link") == true)
			{
				secPermValue = 0;
			}
			else
			{
				//Should Never Get Here
			}
		}
		else
		{
			//Should Never Get Here
		}
		
		if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			Debugger.debug.println("SecPermValue: " + secPermValue);
		}		
				
		return secPermValue;
	}
	
	public static void updateDocSecurity(String fPath)
	{
		//File Counter
		int fileNum = 0;
		
		while (folderNamingPath.equals("Done") == false)
		{
			//CE Connection
			Connection p8Connection = null;
			
			//Increment File Number
			fileNum++;
			
			try
			{
				if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
				{
					Debugger.debug.println("====================================");
					Debugger.debug.println("Entered updateDocSecurity Method");
					//Debugger.debug.println("Object Store: " + p8ObjectStore.get_DisplayName());
					Debugger.debug.println("Folder: " + fPath);
				}

				//Get the Connection
				p8Connection = CEConnection(ConfigInfo.username, ConfigInfo.password, ConfigInfo.p8Stanza, ConfigInfo.uri);

				if (p8Connection != null)
				{
					System.out.println("=============================================================");
					System.out.println(getDateTime() + " updateDocSecurity");
					System.out.println(getDateTime() + " Connection to Content Engine successful");

					//Get the P8 Domain
					p8Dom = getDomain(p8Connection, null);
					if (p8Dom != null)
					{
						System.out.println(getDateTime() + " Connection to the Domain successful");

						//Get the Object Store
						p8OS = getObjectStore(p8Dom, ConfigInfo.objectstore);
						if (p8OS != null)
						{
							System.out.println(getDateTime() + " Connection to the Object Store successful");
							System.out.println("=============================================================");

							//Update FolderNamingPath
							folderNamingPath = fPath;
							
							if (checkFolderInCE(p8OS,folderNamingPath).equals("") == false)
							{
								if (Integer.parseInt(ConfigInfo.useDebugLog) > 1)
								{
									Debugger.debug.println("Folder Path exists for " + folderNamingPath);
								}

								Folder f = Factory.Folder.fetchInstance(p8OS, folderNamingPath, null);
								//Clear any pending transactions on the Folder
								f.clearPendingActions();

								if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
								{
									Debugger.debug.println("====================================");
									Debugger.debug.println("Updating Security Permissions on Folder: " + f.get_FolderName());
								}

								//Use Import Log
								Import.output = null;
								Import.output = getImportLog("UpdateDocSecurity-Root-" + fileNum);

								//Start the Import Log
								Import.output.println("=============================================================");
								Import.output.println("CE Importer - UpdateDocSecurity-Root");
								Import.output.println("=============================================================");
								Import.output.println(getDateTime());
								Import.output.println(getDateTime() + " Update Folder: " + folderNamingPath);
								
								//Update Current Folder
								setFolderSecurity(f);

								//Close the Output Log
								Import.output.close();

								if (f.get_SubFolders().isEmpty() == false)
								{	
									if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
									{
										Debugger.debug.println("====================================");
										Debugger.debug.println("Updating Security Permissions on SubFolders in: " + folderNamingPath);
									}

									//Use Import Log
									Import.output = null;
									Import.output = getImportLog("UpdateDocSecurity-SubFolders-" + fileNum);

									//Start the Import Log
									Import.output.println("=============================================================");
									Import.output.println("CE Importer - UpdateDocSecurity-SubFolders");
									Import.output.println("=============================================================");
									Import.output.println(getDateTime());

									FolderSet subFolderSet = f.get_SubFolders();

									//Update SubFolders
									setSubFolderSecurity(subFolderSet,folderNamingPath);

									//Close the Output Log
									Import.output.close();
								}
								if (f.get_ContainedDocuments().isEmpty() == false)
								{
									//Use Import Log
									Import.output = null;
									Import.output = getImportLog("UpdateDocSecurity-RootDocs-" + fileNum);

									//Start the Import Log
									Import.output.println("=============================================================");
									Import.output.println("CE Importer - UpdateDocSecurity-RootDocs");
									Import.output.println("=============================================================");
									Import.output.println(getDateTime());

									DocumentSet mainFolderDocumentSet = f.get_ContainedDocuments();
									//Set Document Security
									setDocumentSecurity(mainFolderDocumentSet);

									//Close the Output Log
									Import.output.close();
									
									//Set FolderNamingPath to Done
									folderNamingPath = "Done";
								}
								else
								{
									if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
									{
										Debugger.debug.println("No more SubFolders or Documents to process in " + fPath);
										Debugger.debug.println("====================================");
									}
									System.out.println(getDateTime() + " No more SubFolders or Documents to process in " + fPath);
									//Set FolderNamingPath to Done
									folderNamingPath = "Done";
								}
							}
							else
							{
								if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
								{
									Debugger.debug.println("Folder Path does NOT exist");
									Debugger.debug.println("====================================");
								}
								System.out.println(getDateTime() + " Folder Path " + fPath + " does NOT exist");
								//Set FolderNamingPath to Done
								folderNamingPath = "Done";
							}
						}
					}
				}

				//Release the Connection
				//p8Connection = null;
				//p8Dom = null;
				//p8OS = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Application will attempt to reconnect...");
				//Update FolderNamingPath
				folderNamingPath = fPath;
			}
			finally
			{
				//Pop the Connection Subject
				UserContext.get().popSubject();
				//Reset p8Connection
				p8Connection = null;
				p8Dom = null;
				p8OS = null;
				//Update FolderNamingPath
				//folderNamingPath = fPath;
			}
		}
	}
	
	public static void catalogDocSecurity(String fPath)
	{
		try
		{
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				Debugger.debug.println("====================================");
				Debugger.debug.println("Entered catalogDocSecurity Method");
				//Debugger.debug.println("Object Store: " + p8ObjectStore.get_DisplayName());
				Debugger.debug.println("Folder: " + fPath);
			}

			//CE Connection
			Connection p8Connection = null;

			//Get the Connection
			p8Connection = CEConnection(ConfigInfo.username, ConfigInfo.password, ConfigInfo.p8Stanza, ConfigInfo.uri);

			if (p8Connection != null)
			{
				System.out.println("=============================================================");
				System.out.println(getDateTime() + " catalogDocSecurity");
				System.out.println(getDateTime() + " Connection to Content Engine successful");

				//Get the P8 Domain
				p8Dom = getDomain(p8Connection, null);
				if (p8Dom != null)
				{
					System.out.println(getDateTime() + " Connection to the Domain successful");

					//Get the Object Store
					p8OS = getObjectStore(p8Dom, ConfigInfo.objectstore);
					if (p8OS != null)
					{
						System.out.println(getDateTime() + " Connection to the Object Store successful");
						System.out.println("=============================================================");

						if (checkFolderInCE(p8OS,fPath).equals("") == false)
						{
							if (Integer.parseInt(ConfigInfo.useDebugLog) > 1)
							{
								Debugger.debug.println("Folder Path exists for " + fPath);
							}

							Folder f = Factory.Folder.fetchInstance(p8OS, fPath, null);
							//Clear any pending transactions on the Folder
							f.clearPendingActions();
							
							if (f.get_SubFolders().isEmpty() == false)
							{	
								if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
								{
									Debugger.debug.println("====================================");
									Debugger.debug.println("Security Permissions on Folder: " + f.get_FolderName());
								}
								
								//AccessPermissionList
								AccessPermissionList apl;
								//Get Access Permissions List from Folder
								apl = f.get_Permissions();
								
								Iterator folderPermsIterator = apl.iterator();
								while (folderPermsIterator.hasNext())
								{
									AccessPermission ap = (AccessPermission) folderPermsIterator.next();
									if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
									{
										Debugger.debug.println("Grantee Name: " + ap.get_GranteeName());
										Debugger.debug.println("Access Type: " + ap.get_AccessType().toString());
										Debugger.debug.println("Access Mask: " + ap.get_AccessMask().toString());
										//Debugger.debug.println("====================================");
									}
									/*Properties props = ap.getProperties();
									Iterator propsIterator = props.iterator();
									while (propsIterator.hasNext())
									{
										Property prop = (Property) propsIterator.next();
										
										if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
										{
											Debugger.debug.println(prop.getClass().toString());
											Debugger.debug.println("====================================");
										}
									}*/
								}
								
								FolderSet subFolderSet = f.get_SubFolders();

								//Get Sub Folder Security
								getSubFolderSecurity(subFolderSet);
							}
							if (f.get_ContainedDocuments().isEmpty() == false)
							{
								DocumentSet mainFolderDocumentSet = f.get_ContainedDocuments();
								//Get Document Security
								getDocumentSecurity(mainFolderDocumentSet);
							}
							else
							{
								if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
								{
									Debugger.debug.println("SubFolders or Documents do NOT exist in " + fPath);
									Debugger.debug.println("====================================");
								}
								System.out.println(getDateTime() + " SubFolders or Documents do NOT exist in " + fPath);
							}
						}
						else
						{
							if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
							{
								Debugger.debug.println("Folder Path does NOT exist");
								Debugger.debug.println("====================================");
							}
							System.out.println(getDateTime() + " Folder Path " + fPath + " does NOT exist");
						}
					}
				}
			}

			//Release the Connection
			p8Connection = null;
			p8Dom = null;
			p8OS = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void catalogFolderContents(String fPath)
	{
		try
		{
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				Debugger.debug.println("====================================");
				Debugger.debug.println("Entered catalogFolderContents Method");
				//Debugger.debug.println("Object Store: " + p8ObjectStore.get_DisplayName());
				Debugger.debug.println("Folder: " + fPath);
			}

			//CE Connection
			Connection p8Connection = null;

			//Get the Connection
			p8Connection = CEConnection(ConfigInfo.username, ConfigInfo.password, ConfigInfo.p8Stanza, ConfigInfo.uri);

			if (p8Connection != null)
			{
				System.out.println("=============================================================");
				System.out.println(getDateTime() + " catalogFolderContents");
				System.out.println(getDateTime() + " Connection to Content Engine successful");

				//Get the P8 Domain
				p8Dom = getDomain(p8Connection, null);
				if (p8Dom != null)
				{
					System.out.println(getDateTime() + " Connection to the Domain successful");

					//Get the Object Store
					p8OS = getObjectStore(p8Dom, ConfigInfo.objectstore);
					if (p8OS != null)
					{
						System.out.println(getDateTime() + " Connection to the Object Store successful");
						System.out.println("=============================================================");

						if (checkFolderInCE(p8OS,fPath).equals("") == false)
						{
							if (Integer.parseInt(ConfigInfo.useDebugLog) > 1)
							{
								Debugger.debug.println("Folder Path exists for " + fPath);
							}

							Folder f = Factory.Folder.fetchInstance(p8OS, fPath, null);
							//Clear any pending transactions on the Folder
							f.clearPendingActions();
							
							if (f.get_SubFolders().isEmpty() == false)
							{	
								FolderSet subFolderSet = f.get_SubFolders();

								//Get Sub Folders
								getSubFolders(subFolderSet);
							}
							if (f.get_ContainedDocuments().isEmpty() == false)
							{
								DocumentSet mainFolderDocumentSet = f.get_ContainedDocuments();
								//Get Documents
								getDocuments(mainFolderDocumentSet);
							}
							else
							{
								if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
								{
									Debugger.debug.println("SubFolders or Documents do NOT exist in " + fPath);
									Debugger.debug.println("====================================");
								}
								System.out.println(getDateTime() + " SubFolders or Documents do NOT exist in " + fPath);
							}
						}
						else
						{
							if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
							{
								Debugger.debug.println("Folder Path does NOT exist");
								Debugger.debug.println("====================================");
							}
							System.out.println(getDateTime() + " Folder Path " + fPath + " does NOT exist");
						}
					}
				}
			}

			//Release the Connection
			p8Connection = null;
			p8Dom = null;
			p8OS = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void deleteFolderContents(String fPath)
	{
		try
		{
			if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
			{
				Debugger.debug.println("====================================");
				Debugger.debug.println("Entered deleteFolderContents Method");
				//Debugger.debug.println("Object Store: " + p8ObjectStore.get_DisplayName());
				Debugger.debug.println("Folder: " + fPath);
			}

			//CE Connection
			Connection p8Connection = null;

			//Get the Connection
			p8Connection = CEConnection(ConfigInfo.username, ConfigInfo.password, ConfigInfo.p8Stanza, ConfigInfo.uri);

			if (p8Connection != null)
			{
				System.out.println("=============================================================");
				System.out.println(getDateTime() + " deleteFolderContents");
				System.out.println(getDateTime() + " Connection to Content Engine successful");

				//Get the P8 Domain
				p8Dom = getDomain(p8Connection, null);
				if (p8Dom != null)
				{
					System.out.println(getDateTime() + " Connection to the Domain successful");

					//Get the Object Store
					p8OS = getObjectStore(p8Dom, ConfigInfo.objectstore);
					if (p8OS != null)
					{
						System.out.println(getDateTime() + " Connection to the Object Store successful");
						System.out.println("=============================================================");

						if (checkFolderInCE(p8OS,fPath).equals("") == false)
						{
							if (Integer.parseInt(ConfigInfo.useDebugLog) > 1)
							{
								Debugger.debug.println("Folder Path exists for " + fPath);
							}

							Folder f = Factory.Folder.fetchInstance(p8OS, fPath, null);
							//Clear any pending transactions on the Folder
							f.clearPendingActions();
							
							if (f.get_SubFolders().isEmpty() == false)
							{	
								FolderSet subFolderSet = f.get_SubFolders();

								//Delete Folders
								deleteSubFolders(subFolderSet);
							}
							if (f.get_ContainedDocuments().isEmpty() == false)
							{
								DocumentSet mainFolderDocumentSet = f.get_ContainedDocuments();
								deleteDocuments(mainFolderDocumentSet);
							}
							else
							{
								if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
								{
									Debugger.debug.println("SubFolders or Documents do NOT exist in " + fPath);
									Debugger.debug.println("====================================");
								}
								System.out.println(getDateTime() + " SubFolders or Documents do NOT exist in " + fPath);
							}
						}
						else
						{
							if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
							{
								Debugger.debug.println("Folder Path does NOT exist");
								Debugger.debug.println("====================================");
							}
							System.out.println(getDateTime() + " Folder Path " + fPath + " does NOT exist");
						}
					}
				}
			}

			//Release the Connection
			p8Connection = null;
			p8Dom = null;
			p8OS = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void getSubFolderSecurity(FolderSet subFolderSet) throws Exception
	{
		if (subFolderSet != null && !subFolderSet.isEmpty())
		{
			Iterator folderSetIterator = subFolderSet.iterator();
			while (folderSetIterator.hasNext())
			{
				Folder subFolder = (Folder) folderSetIterator.next();
				
				if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
				{
					Debugger.debug.println("====================================");
					Debugger.debug.println("Security Permissions on Folder: " + subFolder.get_FolderName());
				}
				
				//AccessPermissionList
				AccessPermissionList apl;
				//Get Access Permissions List from Folder
				apl = subFolder.get_Permissions();
				
				Iterator folderPermsIterator = apl.iterator();
				while (folderPermsIterator.hasNext())
				{
					AccessPermission ap = (AccessPermission) folderPermsIterator.next();
					if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
					{
						Debugger.debug.println("Grantee Name: " + ap.get_GranteeName());
						Debugger.debug.println("Access Type: " + ap.get_AccessType().toString());
						Debugger.debug.println("Access Mask: " + ap.get_AccessMask().toString());
						Debugger.debug.println("====================================");
					}
					/*Properties props = ap.getProperties();
					Iterator propsIterator = props.iterator();
					while (propsIterator.hasNext())
					{
						Property prop = (Property) propsIterator.next();
						if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
						{
							Debugger.debug.println(prop.getPropertyName() + ": " + prop.getStringValue());
							Debugger.debug.println("====================================");
						}
					}*/
				}
				
				//Check for more SubFolders
				if (subFolder.get_SubFolders().isEmpty() == false)
				{
					FolderSet subFolderFolderSet = subFolder.get_SubFolders();
					getSubFolderSecurity(subFolderFolderSet);
				}
				//Check for Documents
				if (subFolder.get_ContainedDocuments().isEmpty() == false)
				{
					DocumentSet subFolderDocumentSet = subFolder.get_ContainedDocuments();
					getDocumentSecurity(subFolderDocumentSet);
				}
				//Delete the Folder
				//subFolder.delete();
				//subFolder.save(RefreshMode.NO_REFRESH);
				//System.out.println("Folder deleted");
			}
		}
	}
	
	public static void getSubFolders(FolderSet subFolderSet) throws Exception
	{
		if (subFolderSet != null && !subFolderSet.isEmpty())
		{
			Iterator folderSetIterator = subFolderSet.iterator();
			while (folderSetIterator.hasNext())
			{
				Folder subFolder = (Folder) folderSetIterator.next();
				
				//Check for more SubFolders
				if (subFolder.get_SubFolders().isEmpty() == false)
				{
					FolderSet subFolderFolderSet = subFolder.get_SubFolders();
					getSubFolders(subFolderFolderSet);
				}
				//Check for Documents
				if (subFolder.get_ContainedDocuments().isEmpty() == false)
				{
					DocumentSet subFolderDocumentSet = subFolder.get_ContainedDocuments();
					getDocuments(subFolderDocumentSet);
				}
				//Delete the Folder
				//subFolder.delete();
				//subFolder.save(RefreshMode.NO_REFRESH);
				//System.out.println("Folder deleted");
			}
		}
	}
	
	public static void deleteSubFolders(FolderSet subFolderSet) throws Exception
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			Debugger.debug.println("====================================");
			Debugger.debug.println("Entered deleteSubFolders Method");
		}
		
		if (subFolderSet != null && !subFolderSet.isEmpty())
		{
			Iterator folderSetIterator = subFolderSet.iterator();
			while (folderSetIterator.hasNext())
			{
				Folder subFolder = (Folder) folderSetIterator.next();
				
				//Check for more SubFolders to Delete
				if (subFolder.get_SubFolders().isEmpty() == false)
				{
					FolderSet subFolderFolderSet = subFolder.get_SubFolders();
					deleteSubFolders(subFolderFolderSet);
				}
				//Check for Documents to Delete
				if (subFolder.get_ContainedDocuments().isEmpty() == false)
				{
					DocumentSet subFolderDocumentSet = subFolder.get_ContainedDocuments();
					deleteDocuments(subFolderDocumentSet);
				}
				//Delete the Folder
				subFolder.delete();
				subFolder.save(RefreshMode.NO_REFRESH);
				//System.out.println(getDateTime() + " Folder deleted");
			}
		}
	}
	
	public static void createFolder(ObjectStore os, String fPath, String fName)
    {
		if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			Debugger.debug.println("Entered createFolder Method");
			//Debugger.debug.println("Object Store: " + p8ObjectStore.get_DisplayName());
			Debugger.debug.println("Folder: " + fPath);
			Debugger.debug.println("Folder Name: " + fName);
		}
		Folder f = Factory.Folder.fetchInstance(os, fPath, null);
		Folder nf = f.createSubFolder(fName);
		nf.save(RefreshMode.REFRESH);
    }
	
	public static String[] getSplitData(String tempData1, String tempValue1)
	{
		String[] data1 = null;
		
		if (tempData1.equals("Comma") == true)
		{
			data1 = tempValue1.split(",");
		}
		else if (tempData1.equals("ForwardSlash") == true)
		{
			data1 = tempValue1.split("/");
		}
		else if (tempData1.equals("BackSlash") == true)
		{
			data1 = tempValue1.split("\\");
		}
		else if (tempData1.equals("Hyphen") == true)
		{
			data1 = tempValue1.split("-");
		}
		else if (tempData1.equals("Tilde") == true)
		{
			data1 = tempValue1.split("~");
		}
		else if (tempData1.equals("Space") == true)
		{
			data1 = tempValue1.split(" ");
		}
		else
		{
			//Should never get here
		}
		
		return data1;
	}
	
	public static Date getDateFromJulian7(String julianDate)
	throws ParseException
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("Entered getDateFromJulian7 Method");
		}
		return new SimpleDateFormat("yyyyD").parse(julianDate);
	}
	
	public static String getDateWithMonthName(String dateValue)
	{
		String dateWithMonthName = "";
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy/MM/dd");
		try
		{
			Date date = formatter.parse(dateValue);
			dateWithMonthName = date.toString();
		}
		catch (Exception e)
		{
			e.getMessage();
		}
		return dateWithMonthName;
	}
	
	/*
     * Reads the content from a file and stores it
     * in a byte array. The byte array will later be
     * used to create ContentTransfer object.
     */
	public static byte[] readDocContentFromFile(File f)
    {
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("Entered readDocContentFromFile Method");
		}
		FileInputStream is;
        byte[] b = null;
        int fileLength = (int)f.length();
        if(fileLength != 0)
        {
        	try
        	{
        		is = new FileInputStream(f);
        		b = new byte[fileLength];
        		is.read(b);
        		is.close();
        	}
        	catch (FileNotFoundException e)
        	{
        		e.printStackTrace();
        	}
        	catch (IOException e)
        	{
        		e.printStackTrace();
        	}
        }
        return b;
    }
	
    /*
     * Creates the ContentTransfer object from supplied file's
     * content.
     */
	public static ContentTransfer createContentTransfer(File f)
    {
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("Entered createContentTransfer Method");
		}
		ContentTransfer ctNew = null;
        if(readDocContentFromFile(f) != null)
        {
        	ctNew = Factory.ContentTransfer.createInstance();
            ByteArrayInputStream is = new ByteArrayInputStream(readDocContentFromFile(f));
            ctNew.setCaptureSource(is);
            ctNew.set_RetrievalName(f.getName());
            //TO DO
            //ctNew.set_ContentType("Some Mime Type");
        }
        return ctNew;
    }
    
    /*
     * Creates the ContentElementList from ContentTransfer object.
     */
	/*public static ContentElementList createContentElements(File f)
    {
        ContentElementList cel = null;
        if(createContentTransfer(f) != null)
        {
        	cel = Factory.ContentElement.createList();
            ContentTransfer ctNew = createContentTransfer(f);
            cel.add(ctNew);
        }
        return cel;
    }*/
	@SuppressWarnings("unchecked")
	public static ContentElementList createContentElements(ContentTransfer ct)
    {
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("Entered createContentElements Method");
		}
		ContentElementList cel = null;
        if(ct != null)
        {
        	cel = Factory.ContentElement.createList();
            //ContentTransfer ctNew = createContentTransfer(f);
            cel.add(ct);
        }
        return cel;
    }
    
    /*
     * Creates the Document Java Object
     */
	public static Document createDocument(ObjectStore os, String docClass, File f)
    {
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("Entered createDocument Method");
		}
		//FileInputStream fis = openLocalFile(fileName);
		
		//Create Content Transfer Object and Read the File
		ContentTransfer ct = createContentTransfer(f);
		
		//Create Content Element List Object
		ContentElementList cel = createContentElements(ct);
		
		//String fileName = "";
		Document doc = null;
		//String mimeType = "text/plain";
		
		if (docClass.equals("") == true)
        {
			doc = Factory.Document.createInstance(os, null);
        }
        else
        {
        	doc = Factory.Document.createInstance(os, docClass);
        }
        
		//Save the Doc - R/T
		doc.save(RefreshMode.REFRESH);
		
		//doc.getProperties().putValue("DocumentTitle", f.getName());
        //mimeType = f.getName().substring(f.getName().length()-4, f.getName().length()-1);
        //doc.set_MimeType(mimeType);
        if (cel != null)
        	doc.set_ContentElements(cel);
        return doc;
    }
	
	/*
     * Creates the Document without content.
     */
	public static Document createDocNoContent(String mimeType, ObjectStore os, String docName, String docClass)
    {
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("Entered createDocNoContent Method");
		}
		Document doc = null;
		if (docClass.equals(""))
        	doc = Factory.Document.createInstance(os, null);
        else
        	doc = Factory.Document.createInstance(os, docClass);
        doc.getProperties().putValue("DocumentTitle", docName);
        doc.set_MimeType(mimeType);
        return doc;
    }
	
	/*
     * Checks in the Document object.
     */
	public static void checkinDoc(Document doc)
    {
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("Entered checkinDoc Method");
		}
		doc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
        //doc.save(RefreshMode.NO_REFRESH);
        //doc.refresh();
    }
	
	public static ReferentialContainmentRelationship fileDocument(ObjectStore os, Document doc, String ContainerName, String folderName)
    {
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("Entered fileDocument Method");
		}
		//Folder fo = Factory.Folder.fetchInstance(os,folderPath,null);
		//System.out.println("Instantiate Folder");
		Folder fo = instantiateFolder(os,folderName);
		ReferentialContainmentRelationship rcr = null;
		//System.out.println("DRCR");
		//rcr = Factory.DynamicReferentialContainmentRelationship.createInstance(os, null, AutoUniqueName.NOT_AUTO_UNIQUE, DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
		try
		{
			rcr = (ReferentialContainmentRelationship) fo.file(doc, AutoUniqueName.NOT_AUTO_UNIQUE, ContainerName, DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
			rcr.save(RefreshMode.REFRESH);
			//return rcr;
		}
		catch (Exception e)
		{
			if (e instanceof EngineRuntimeException) 
			{
				EngineRuntimeException fnEx = (EngineRuntimeException) e;
				if (fnEx.getExceptionCode().equals(ExceptionCode.E_NOT_UNIQUE) == true) 
				{
					//System.out.println("Exception: " + e.getMessage());
					//System.out.println("The name " + ContainerName + " has already been used");
					if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
					{
						Debugger.debug.println("==============================");
						Debugger.debug.println("DUPLICATE: " + e.getMessage());
						Debugger.debug.println("==============================");
						Debugger.debug.println("The name " + ContainerName + " has already been used");
					}
					
					Folder f = null;
					
					//File in the Duplicates Folder
					//File in ObjectStoresFolder/Duplicates
					if (Integer.parseInt(ConfigInfo.useObjectStoreFolder) >= 1)
					{
						if (checkFolderInCE(os,ConfigInfo.osFolder + "/_DUPLICATES").equals("") == true)
						{
							createFolder(os,ConfigInfo.osFolder,"_DUPLICATES");
						}
						f = instantiateFolder(os,ConfigInfo.osFolder + "/_DUPLICATES");
						//Update folder path for the dupe doc to be used by the log
						folderNamingPath = ConfigInfo.osFolder + "/_DUPLICATES";
						if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
						{
							Debugger.debug.println("folderNamingPath: " + ConfigInfo.osFolder + "/_DUPLICATES");
							Debugger.debug.println("DocContainerName: " + "DUPE-" + ContainerName);
						}
					}
					else //File in /Duplicates
					{
						if (checkFolderInCE(os,"/_DUPLICATES").equals("") == true)
						{
							createFolder(os,"/","_DUPLICATES");
						}
						f = instantiateFolder(os,"/_DUPLICATES");
						//Update folder path for the dupe doc to be used by the log
						folderNamingPath = "/_DUPLICATES";
						if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
						{
							Debugger.debug.println("folderNamingPath: " + "/_DUPLICATES");
							Debugger.debug.println("DocContainerName: " + "DUPE-" + ContainerName);
						}
					}
																				
					rcr = (ReferentialContainmentRelationship) f.file(doc, AutoUniqueName.NOT_AUTO_UNIQUE, "DUPE-" + ContainerName, DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
					rcr.save(RefreshMode.REFRESH);
					//return rcr;
				}
		        else
		        {
		        	System.out.println("Exception: " + e.getMessage());		        
		        }
			}
		    else
		    {
		    	// A standard Java exception.
		        System.out.println("Exception: " + e.getMessage());
		    }
		}
		    	
    	//System.out.println("DRCR - tail");
    	//rcr.set_Tail(fo);
    	//System.out.println("DRCR - head");
    	//rcr.set_Head(doc);
    	//System.out.println("DRCR - containment name");
    	//Commented out to avoid issues with Containment
    	//rcr.set_ContainmentName(ContainerName);
    	return rcr;
    }
	
	public static Folder instantiateFolder(ObjectStore os, String folderName)
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) == 1)
		{
			Debugger.debug.println("Entered instantiateFolder Method");
			//Debugger.debug.println("OS: " + os);
			Debugger.debug.println("folderName: " + folderName);
		}
		//Folder folder = null;
		//try
		//{
			//System.out.println("Entered InstantiateFolder");
			//System.out.println("folderName: " + folderName);
			//Original
			//folder = Factory.Folder.getInstance(os, null, folderName.trim());
			Folder f = Factory.Folder.fetchInstance(os, folderName, null);
		//}
		//catch (Exception e)
		//{
		//	System.out.println("instantiateFolder error getting Folder");
		//	System.out.println(e);
		//}
		return f;
	}
	public static String checkFolderInCE(ObjectStore OS, String folderToVerify)
	{
		if (Integer.parseInt(ConfigInfo.useDebugLog) >= 1)
		{
			Debugger.debug.println("Entered checkFolderInCE Method");
			//Debugger.debug.println("Object Store: " + OS);
			Debugger.debug.println("Folder: " + folderToVerify);
		}
		try
		{
			//Check if the Folder exists in the Object Store
			Folder folder = Factory.Folder.fetchInstance(OS, folderToVerify, null);
			//Return the ID of the Folder proving that the Folder exists in the Object Store
			return folder.get_Id().toString();
		}
		catch (Exception e)
		{
			//Return Empty String for the Folder to show that it does not exist in the Object Store
			return "";
		}
	}
		
	private static FileInputStream openLocalFile(String name)
	{
		try
		{
			return new FileInputStream(name);
		}
		catch (FileNotFoundException fnfe)
		{
			throw new RuntimeException(fnfe);
		}
	}
	
	private static BufferedReader getReader(String name)
	{
		BufferedReader in = null;
		try
		{
			File file = new File(name);
			in = new BufferedReader(
					new FileReader(file));
		}
		catch (FileNotFoundException e)
		{
			System.out.println("The file does not exist for BufferedReader.");
			System.exit(0);
		}
		return in;
	}
	private static PrintWriter getWriter(String name)
	{
		PrintWriter out = null;
		try
		{
			File file = new File(name);
			out = new PrintWriter(
						new BufferedWriter(
								new FileWriter(file, true) ), true);
			return out;
		}
		catch (IOException e)
		{
			System.out.println("I/O Error trying to use PrintWriter.");
			System.exit(0);
		}
		return null;
	}

}
