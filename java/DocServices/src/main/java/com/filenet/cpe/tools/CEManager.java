package com.filenet.cpe.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
import java.util.List;
//import java.util.Map;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Iterator;
import javax.security.auth.Subject;

//Apache Commons File Utils
import org.apache.commons.io.FileUtils;

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//FileNet Classes
import com.filenet.api.admin.ClassDefinition;
import com.filenet.api.admin.PropertyDefinition;
import com.filenet.api.constants.*;
import com.filenet.api.exception.*;
import com.filenet.api.query.*;
import com.filenet.api.core.Connection;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ReferentialContainmentRelationship;

import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.Document;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.util.UserContext;
import com.filenet.api.collection.ClassDefinitionSet;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.DocumentSet;
import com.filenet.api.collection.FolderSet;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.collection.PageIterator;
import com.filenet.api.collection.PropertyDefinitionList;

import com.filenet.wcm.api.ObjectFactory;
import com.filenet.wcm.api.Session;

//Custom Classes
import com.hp.docsolutions.filenet.p8.base.ConstantsUtil;
import com.hp.docsolutions.filenet.p8.base.DocServicesLog;
import com.hp.docsolutions.filenet.p8.base.XMLReader;
import com.hp.docsolutions.filenet.p8.docservices.model.FnDocClass;
import com.hp.docsolutions.filenet.p8.docservices.model.FnDocClassList;
import com.hp.docsolutions.filenet.p8.docservices.model.FnDocument;
import com.hp.docsolutions.filenet.p8.docservices.model.FnDocumentList;
import com.hp.docsolutions.filenet.p8.docservices.model.FnProperty;
import com.hp.docsolutions.filenet.p8.docservices.model.FnPropertyList;

public class CEManager {
	
	//******************************************************
	//Public Main Methods
	//******************************************************
	
	@SuppressWarnings("rawtypes")
	public FnDocumentList getDocuments(String docClassName, String docPropValue, DocServicesLog docServiceLog)
		throws Exception
	{
		//Define the Doc Class Properties Resource
		ResourceBundle docClassConfigProps = null;
		
		//FileNet Document List of Documents Object
		FnDocumentList fndocList = new FnDocumentList();
		
		//CE Object Store
		ObjectStore os = null;
						
		try
		{
			docServiceLog.log(docServiceLog.INFO, "Entered CEManager -> getDocuments()");
			docServiceLog.log(docServiceLog.INFO, "Get " + docClassName + " Documents with the Property Value " + docPropValue);
			
			ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
			
			String userId = globalConfig.getString(ConstantsUtil.CE_USER_ID);
			String password = globalConfig.getString(ConstantsUtil.CE_USER_PASSWORD);
			
			//Update for Document Image View Server - AE Server Name
			fndocList.setFnDocViewServer(globalConfig.getString("aeServerName"));
			//Update for Document Storage Area - Object Store Name
			fndocList.setFnDocStorageArea(globalConfig.getString("objectstoreName"));
			//Update Doc Class Type
			fndocList.setFnDocumentClass(docClassName);
			
			//Get CE Object Store
			os = getObjectStore(userId, password, docServiceLog);
			
			//Verify Object Store Connected
			if (os != null)
			{
				//Search CE for Documents Matching Criteria
				//SearchScope
				SearchScope searchScope = new SearchScope(os);
				//SearchSQL
				SearchSQL searchSQL = new SearchSQL();
							
				docClassConfigProps = ResourceBundle.getBundle(docClassName + "Config");
				docServiceLog.log(docServiceLog.INFO, docClassName + " Doc Class Config Props");
				
				//Build Query from Doc Props
				String sqlDocProps = "";
				
				//Initialize the Property Key Counter
				int propCount = 1;
				
				docServiceLog.log(docServiceLog.INFO, "Building the SQL Query from the Document Properties");
				while (docClassConfigProps.containsKey("prop" + propCount) == true)
				{
					sqlDocProps = sqlDocProps + docClassConfigProps.getString("prop" + propCount) + ", ";
					//Increment the Property Key Counter
					propCount++;
				}
								
				//Remove the last comma and space from the String
				sqlDocProps = sqlDocProps.substring(0, sqlDocProps.length()-2);
				
				//Output the SQL Doc Props Query to the Log
				docServiceLog.log(docServiceLog.INFO, "SQL Select: " + sqlDocProps);
				
				//SQL Query for Documents
				String sql = "select d.this, " + sqlDocProps + " from Document d" + 
					" where " + docClassConfigProps.getString("whereProperty") + " = '" + docPropValue + "'";
				
				//Output Full Query
				docServiceLog.log(docServiceLog.INFO, "SQL Query: " + sql);
				
				//Set Max Records to Process to 500
				//searchSQL.setMaxRecords(500);
				
				//Set the SQL query
				searchSQL.setQueryString(sql);
				
				//Independent Object Set
				IndependentObjectSet objectSet = searchScope.fetchObjects(searchSQL, null, null, null);
				
				//Get Custom Doc Props Call
				List<FnProperty> customDocPropsListArr = new ArrayList<FnProperty>();
				FnPropertyList customDocPropsList = new FnPropertyList();
				customDocPropsList = getCustomDocProps(os,docClassName,docServiceLog);
				customDocPropsListArr = customDocPropsList.getFnProps();
				//Browse List to include only specified Custom Doc Properties
				String browseListIncludeProps = docClassConfigProps.getString("browseListIncludeProps");
				//Browse List include props is not empty
				if (browseListIncludeProps.equals("") == false)
				{
					if (browseListIncludeProps.contains(",") == true)
					{
						String[] browseListIncludePropsData = null;
						browseListIncludePropsData = browseListIncludeProps.split(",");
						List<FnProperty> tempDocPropsList = new ArrayList<FnProperty>();
						for (int x = 0; x < customDocPropsListArr.size(); x++)
						{
							for (int y = 0; y < browseListIncludePropsData.length; y++)
							{
								if (customDocPropsListArr.get(x).getName().equals(browseListIncludePropsData[y]) == true)
								{
									tempDocPropsList.add(customDocPropsListArr.get(x));
									break;
								}
							}
						}
						//Empty customDocPropsListArr
						customDocPropsListArr.clear();
						//Save tempDocPropsList to customDocPropsList
						for (int z = 0; z < tempDocPropsList.size(); z++)
						{
							customDocPropsListArr.add(tempDocPropsList.get(z));
						}
						//Empty tempDocPropsList
						tempDocPropsList.clear();
						tempDocPropsList = null;
					}
					else
					{
						List<FnProperty> tempDocPropsList = new ArrayList<FnProperty>();
						for (int x = 0; x < customDocPropsListArr.size(); x++)
						{
							if (customDocPropsListArr.get(x).getName().equals(browseListIncludeProps) == true)
							{
								tempDocPropsList.add(customDocPropsListArr.get(x));
								break;
							}
						}
						//Empty customDocPropsListArr
						customDocPropsListArr.clear();
						//Save tempDocPropsList to customDocPropsListArr
						for (int z = 0; z < tempDocPropsList.size(); z++)
						{
							customDocPropsListArr.add(tempDocPropsList.get(z));
						}
						//Empty tempDocPropsList
						tempDocPropsList.clear();
						tempDocPropsList = null;
					}
				}
								
				int i = 0;
				Iterator iter = objectSet.iterator();
							
				while (iter.hasNext() == true)
				{
					//String tempDocPropList = "";
					Document document = (Document) iter.next();
					//Update Property Cache to include Custom Doc Properties
					document.refresh();
					Properties props;
					props = document.getProperties();
					
					//Initialize FnDocument
					FnDocument fnDoc = new FnDocument();
					//Set the Document Class
					fnDoc.setFnDocumentClass(docClassName);
					//Set the Document Name
					fnDoc.setFnDocumentName(document.get_Name());
					
					//List of Properties for the FnDocument
					//Map<String,String> fndocPropMap = new HashMap<String,String>();
					FnPropertyList fndocPropList = new FnPropertyList();
					
					//Initialize the Property Key Counter
					propCount = 1;
					
					docServiceLog.log(docServiceLog.INFO, "==============================================");
					docServiceLog.log(docServiceLog.INFO, "Get Document Properties");
					
					//Get Doc Class System Properties
					while (docClassConfigProps.containsKey("prop" + propCount) == true)
					{
						String propName = docClassConfigProps.getString("prop" + propCount);
						
						if (props.isPropertyPresent(propName) == true)
						{
							//Create FnProperty to store the name and value of the Property
							FnProperty fnProp = new FnProperty();
							fnProp.setName(propName);
							docServiceLog.log(docServiceLog.INFO, "Property: " + fnProp.getName());
							
							if (propName.equals("ID") == true)
							{
								//Update fndocPropMap
								fnProp.setValue(props.getIdValue(propName).toString());
								fndocPropList.addFnProp(fnProp);
								docServiceLog.log(docServiceLog.INFO, "Property Value: " + fnProp.getValue());
								docServiceLog.log(docServiceLog.INFO, "==============================================");
							}
							else if (propName.equals("VersionSeries") == true)
							{
								//Update fndocPropMap
								fnProp.setValue(props.getIdValue(propName).toString());
								fndocPropList.addFnProp(fnProp);
								//Update the fnDoc to store the VersionSeries ID for Document Viewing
								fnDoc.setFnDocumentID(props.getIdValue(propName).toString());
								docServiceLog.log(docServiceLog.INFO, "Property Value: " + fnProp.getValue());
								docServiceLog.log(docServiceLog.INFO, "==============================================");
							}
							else
							{
								//tempDocPropList = tempDocPropList + propName + " - " + props.getStringValue(propName) + " | ";
								
								//Update fndocPropMap
								//fndocPropMap.put(propName, props.getStringValue(propName));
								fnProp.setValue(props.getStringValue(propName));
								fndocPropList.addFnProp(fnProp);
								
								docServiceLog.log(docServiceLog.INFO, "Property Value: " + fnProp.getValue());
								docServiceLog.log(docServiceLog.INFO, "==============================================");
							}
						}
						
						//Increment the Property Key Counter
						propCount++;
					}
					
					//Get Doc Class Custom Properties
					if (customDocPropsListArr.size() > 0)
					{
						for (int a = 0; a < customDocPropsListArr.size(); a++)
						{
							String propName = customDocPropsListArr.get(a).getName();
							if (props.isPropertyPresent(propName) == true)
							{
								//Create FnProperty to store the name and value of the Property
								FnProperty fnProp = new FnProperty();
								fnProp.setName(propName);
								docServiceLog.log(docServiceLog.INFO, "Property: " + fnProp.getName());
								
								fnProp.setValue(props.getStringValue(propName));
								fndocPropList.addFnProp(fnProp);
								docServiceLog.log(docServiceLog.INFO, "Property Value: " + fnProp.getValue());
								docServiceLog.log(docServiceLog.INFO, "==============================================");
							}
							//Increment the Property Key Counter
							propCount++;
						}
					}
					
					//Decrement propCount since it started with an initial value of 1
					propCount--;
					
					docServiceLog.log(docServiceLog.INFO, "Total Properties Searched: " + propCount);
					docServiceLog.log(docServiceLog.INFO, "==============================================");
					
					//Update FnDocument with Property List
					//fnDoc.setFnDocProps(fndocPropMap);
					fnDoc.setFnDocProps(fndocPropList);
					
					docServiceLog.log(docServiceLog.INFO, "Updated Doc with Property List");
					
					//Add FnDocument to FnDocumentList
					fndocList.addFnDoc(fnDoc);
					docServiceLog.log(docServiceLog.INFO, "Added Doc to the Doc List");
					
					//Increment Document Counter
					i++;
					
					//Update errorFlag
					fndocList.setErrorFlag(0);
				}
				
				//Verify the Doc List is not empty
				if (fndocList.getCount() > 0)
				{
					//Output total documents stored in the Doc List
					docServiceLog.log(docServiceLog.INFO, "Total Documents in the Document List to View: " + fndocList.getCount());
					
					//Update the errorFlag to 0 to avoid it being set as an Error anywhere else.
					fndocList.setErrorFlag(0);
				}
				else
				{
					//This is for 0 documents returned and is not an Error.
					//Update the errorFlag to 0 to avoid it being set as an Error anywhere else.
					fndocList.setErrorFlag(0);
				}
				//Output total documents assumed to be stored in the Doc List			
				docServiceLog.log(docServiceLog.INFO, "Total Documents: " + i);
			}
			else
			{
				docServiceLog.log(docServiceLog.INFO, "Object Store Failed to Connect - ErrorFlag = 1");		
				//Update errorFlag
				fndocList.setErrorFlag(1);
			}
			
			//Reset Object Store variable
			os = null;	
		}
		catch (Exception e)
		{
			docServiceLog.log(docServiceLog.INFO, "Exception - ErrorFlag = 2");
			docServiceLog.log(docServiceLog.ERROR, docServiceLog.getStackTrace(e));
			//Update errorFlag
			fndocList.setErrorFlag(2);
			//Reset Object Store variable
			os = null;
		}
				
		docServiceLog.log(docServiceLog.INFO, "Leaving CEManager -> getDocuments()");
		return fndocList;
	}
	
	@SuppressWarnings("rawtypes")
	public FnDocClassList getCustomDocClasses(DocServicesLog docServiceLog)
		throws Exception
	{
		//CE Object Store
		ObjectStore os = null;
		
		//List to hold the Custom Doc Classes List
		FnDocClassList customDocClassesList = new FnDocClassList();
		
		try
		{
			docServiceLog.log(docServiceLog.INFO, "Entered CEManager -> getCustomDocClasses()");
						
			//Define the Global Properties Resource
			ResourceBundle globalProps = null;
			globalProps = ResourceBundle.getBundle("GlobalConfig");
			String userId = globalProps.getString(ConstantsUtil.CE_USER_ID);
			String password = globalProps.getString(ConstantsUtil.CE_USER_PASSWORD);
			
			//Get CE Object Store
			os = getObjectStore(userId, password, docServiceLog);
			
			//Verify Object Store Connected
			if (os != null)
			{
				//Exclude List of Doc Classes
				String docClassExcludeList = "";
				docClassExcludeList = globalProps.getString("docClassExcludeList");
				//Array to hold Doc Class Exclude List
				String[] docClassExcludeData = null;
				boolean excludeListSplit = false;
				
				//Split the list because there is more than 1 Doc Class to exclude
				if (docClassExcludeList.contains(",") == true)
				{
					//Split the list to hold in the array
					docClassExcludeData = docClassExcludeList.split(",");
					excludeListSplit = true;
				}
						
				//Fetch selected class definition from the server
				ClassDefinition docClassDef = Factory.ClassDefinition.fetchInstance(os, "Document", null);
				//Get the SubClasses
				ClassDefinitionSet subDocClassDefSet = docClassDef.get_ImmediateSubclassDefinitions();
								
				//PropertyDefinitionList propDefList = docClassDef.get_PropertyDefinitions();
				Iterator iter = subDocClassDefSet.iterator();
				ClassDefinition classDef = null;
					  
				//Loop until Class Definitions are found
				while (iter.hasNext())
				{	        				
					//Get the Class Definition
					classDef = (ClassDefinition) iter.next();
					//Output Class Name
					docServiceLog.log(docServiceLog.INFO, "Class Definition: " + classDef.get_DisplayName());
					
					//Only want the Custom Class Definitions			
					if (classDef.get_IsHidden() == false && classDef.get_IsSystemOwned() == false && classDef.get_IsPersistent() == true
							&& classDef.get_InstalledByAddOn() == null)
					{
						boolean skipClassDef = false;
						//excludeListSplit is true, then we need to check each exclude name against the classDef
						if (excludeListSplit == true)
						{
							for (int x = 0; x < docClassExcludeData.length; x++)
							{
								if (docClassExcludeData[x].equals(classDef.get_DisplayName()) == true)
								{
									skipClassDef = true;
								}
							}
						}
						else //Exclude List of 1 Doc Class
						{
							if (docClassExcludeList.equals(classDef.get_DisplayName()) == true)
							{
								skipClassDef = true;
							}
						}
						if (skipClassDef == false)
						{
							//Get the SubClasses
							ClassDefinitionSet subDocClassSet = classDef.get_ImmediateSubclassDefinitions();
							//Verify if the Class Definition has SubClasses.  We only want the last set of SubClasses
							if (subDocClassSet.isEmpty() == false)
							{
								Iterator iter2 = subDocClassSet.iterator();
								ClassDefinition subClassDef = null;
								while (iter2.hasNext())
								{
									//Get the SubClass Definition
									subClassDef = (ClassDefinition) iter2.next();
									//Output Sub Class Name
									docServiceLog.log(docServiceLog.INFO, "Sub Class Definition: " + subClassDef.get_DisplayName());
									if (subClassDef.get_IsHidden() == false && subClassDef.get_IsSystemOwned() == false && subClassDef.get_IsPersistent() == true
											&& subClassDef.get_InstalledByAddOn() == null)
									{
										//Create the Doc Class Object
										FnDocClass fnDocClass = new FnDocClass();
										//Set the Name of the Doc Class
										fnDocClass.setName(subClassDef.get_DisplayName());
										//Get the List of Properties for the Doc Class
										FnPropertyList fnPropsList = new FnPropertyList();
										fnPropsList = getCustomDocProps(os, subClassDef.get_DisplayName(), docServiceLog);
										//Add Props List to Class
										fnDocClass.setFnDocClassProps(fnPropsList);
										//Add the Doc Class to the Doc Class List
										customDocClassesList.addFnDocClass(fnDocClass);
										//importerLog.log(importerLog.INFO, "Saving Class Definition: " + subClassDef.get_DisplayName());
										//importerLog.log(importerLog.INFO, "=============================================");
									}
								}
							}
							else
							{
								//Create the Doc Class Object
								FnDocClass fnDocClass = new FnDocClass();
								//Set the Name of the Doc Class
								fnDocClass.setName(classDef.get_DisplayName());
								//Get the List of Properties for the Doc Class
								FnPropertyList fnPropsList = new FnPropertyList();
								fnPropsList = getCustomDocProps(os, classDef.get_DisplayName(), docServiceLog);
								//Add the Properties to the Doc Class
								fnDocClass.setFnDocClassProps(fnPropsList);							
								//Add the Doc Class to the Doc Class List
								customDocClassesList.addFnDocClass(fnDocClass);
								//importerLog.log(importerLog.INFO, "Saving Class Definition: " + classDef.get_DisplayName());
								//importerLog.log(importerLog.INFO, "=============================================");
							}
						}
					}
				}
				docServiceLog.log(docServiceLog.INFO, "Leaving CEManager -> getCustomDocClasses()");
				//Reset Object Store variable
				os = null;
			}
		}
		catch (Exception e)
		{
			docServiceLog.log(docServiceLog.INFO, "Exception - ErrorFlag = 2");
			docServiceLog.log(docServiceLog.ERROR, docServiceLog.getStackTrace(e));
			//Reset Object Store variable
			os = null;
		}
				
		return customDocClassesList;
	}

	@SuppressWarnings("rawtypes")
	public FnDocumentList getCustomDocTypes(DocServicesLog docServiceLog)
	{
		//CE Object Store
		ObjectStore os = null;
		//List to hold the Custom Doc Types List
		FnDocumentList docList = new FnDocumentList();
		
		try
		{
			docServiceLog.log(docServiceLog.INFO, "Entered CEManager -> getCustomDocTypes()");
						
			//Define the Global Properties Resource
			ResourceBundle globalProps = null;
			globalProps = ResourceBundle.getBundle("GlobalConfig");
			String userId = globalProps.getString(ConstantsUtil.CE_USER_ID);
			String password = globalProps.getString(ConstantsUtil.CE_USER_PASSWORD);
			
			//Get CE Object Store
			os = getObjectStore(userId, password, docServiceLog);
			
			//Verify Object Store Connected
			if (os != null)
			{
				//Get the Doc Types Location in the Object Store
				String docTypesRootLocation = globalProps.getString("docTypesRootLocation");
				//Verify the docTypesRootLocation Folder exists in the Object Store
				if (checkFolderInCE(os,docTypesRootLocation,docServiceLog).equals("") == false)
				{
					//Get the Document Class used for Doc Types
					String docTypeClass = globalProps.getString("docTypeClass");
					//Get Custom Doc Props to be used for checking against any found Doc Type Documents
					FnPropertyList fnProps = new FnPropertyList();
					fnProps = getCustomDocProps(os,docTypeClass,docServiceLog);
					List<FnProperty> fnPropsList = fnProps.getFnProps(); 
					//Get Doc Types Location Folder Object
					Folder rootFolder = Factory.Folder.fetchInstance(os, docTypesRootLocation, null);
					//Get the SubFolders for any specific class defined Doc Types
					FolderSet subFolderFolderSet = rootFolder.get_SubFolders();
					if (subFolderFolderSet != null && !subFolderFolderSet.isEmpty())
					{
						Iterator folderSetIterator = subFolderFolderSet.iterator();
						while (folderSetIterator.hasNext())
						{
							Folder subFolder = (Folder) folderSetIterator.next();
							//Check for Documents
							if (subFolder.get_ContainedDocuments().isEmpty() == false)
							{
								//Get the List of Documents
								DocumentSet subFolderDocumentSet = subFolder.get_ContainedDocuments();
								if (subFolderDocumentSet != null && !subFolderDocumentSet.isEmpty())
								{
									Iterator documentSetIterator = subFolderDocumentSet.iterator();
									while (documentSetIterator.hasNext())
									{
										Document document = (Document) documentSetIterator.next();
										//Update Property Cache to include Custom Doc Properties
										document.refresh();
										FnDocument fnDoc = new FnDocument();
										//Set the Doc Class
										fnDoc.setFnDocumentClass(subFolder.get_FolderName());
										//Set the Doc Name
										fnDoc.setFnDocumentName(document.get_Name());
										Properties props = document.getProperties();
										FnPropertyList fnPropsForDocList = new FnPropertyList();
										//Set Properties For Doc
										fnPropsForDocList = fnDoc.getFnDocProps();
										for (int a = 0; a < fnPropsList.size(); a++)
										{
											//Get 1 FnProperty Object to compare against the Document
											FnProperty fnProp = fnPropsList.get(a);
											//Verify if the fnProp is present in the Document
											if (props.isPropertyPresent(fnProp.getName()) == true)
											{
												//fnDoc.setFnDocumentClass(subFolder.get_FolderName());
												FnProperty fnPropForDoc = new FnProperty();
												fnPropForDoc.setName(fnProp.getName());
												fnPropForDoc.setValue(props.getStringValue(fnProp.getName()));
												fnPropsForDocList.addFnProp(fnPropForDoc);
											}
										}
										//Verify fnPropsForDocList 
										if (fnPropsForDocList.getCount() > 0 && fnPropsForDocList != null)
										{
											//Verify the fnDoc was setup and not null
											if (fnDoc.getFnDocumentClass().equals("") == false && fnDoc != null)
											{
												//Add FnDocument to FnDocumentList
												docList.addFnDoc(fnDoc);
											}
										}
									} //End While
								}
							}
						}
					}
					else
					{
						//Check for Documents
						if (rootFolder.get_ContainedDocuments().isEmpty() == false)
						{
							//Get the List of Documents
							DocumentSet folderDocumentSet = rootFolder.get_ContainedDocuments();
							if (folderDocumentSet != null && !folderDocumentSet.isEmpty())
							{
								Iterator documentSetIterator = folderDocumentSet.iterator();
								while (documentSetIterator.hasNext())
								{
									Document document = (Document) documentSetIterator.next();
									//Update Property Cache to include Custom Doc Properties
									document.refresh();
									FnDocument fnDoc = new FnDocument();
									//Set the Doc Class
									fnDoc.setFnDocumentClass(rootFolder.get_FolderName());
									//Set the Doc Name
									fnDoc.setFnDocumentName(document.get_Name());
									Properties props = document.getProperties();
									FnPropertyList fnPropsForDocList = new FnPropertyList();
									//Set Properties For Doc
									fnPropsForDocList = fnDoc.getFnDocProps();
									for (int a = 0; a < fnPropsList.size(); a++)
									{
										//Get 1 FnProperty Object to compare against the Document
										FnProperty fnProp = fnPropsList.get(a);
										//Verify if the fnProp is present in the Document
										if (props.isPropertyPresent(fnProp.getName()) == true)
										{
											//fnDoc.setFnDocumentClass(rootFolder.get_FolderName());
											FnProperty fnPropForDoc = new FnProperty();
											fnPropForDoc.setName(fnProp.getName());
											fnPropForDoc.setValue(props.getStringValue(fnProp.getName()));
											fnPropsForDocList.addFnProp(fnPropForDoc);
										}
									}
									//Verify fnPropsForDocList
									if (fnPropsForDocList.getCount() > 0 && fnPropsForDocList != null)
									{
										//Verify the fnDoc was setup and not null
										if (fnDoc.getFnDocumentClass().equals("") == false && fnDoc != null)
										{
											//Add FnDocument to FnDocumentList
											docList.addFnDoc(fnDoc);
										}
									}
								} //End While
							}
						}
					}
				}
				else
				{
					docServiceLog.log(docServiceLog.INFO, "DocTypesRootLocation: " + docTypesRootLocation + " does not exist in the Object Store");
				}
			}
		
		}
		catch (Exception e)
		{
			docServiceLog.log(docServiceLog.INFO, "Exception - ErrorFlag = 2");
			docServiceLog.log(docServiceLog.ERROR, docServiceLog.getStackTrace(e));
			//Reset Object Store variable
			os = null;
		}	
		
		docServiceLog.log(docServiceLog.INFO, "Leaving CEManager -> getCustomDocTypes()");
		
		return docList;
	}
	
	public String getDocumentURL(String docId, DocServicesLog docServiceLog, boolean loggingEnabled)
	{
		if (loggingEnabled == true)
		{
			docServiceLog.log(docServiceLog.INFO, "Entered CEManager -> getDocumentURL()");
			docServiceLog.log(docServiceLog.INFO, "DocId: " + docId);
		}
		
		String docURL = "";
		ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
		//Create CE User Session Token for viewing documents
		Session session = ObjectFactory.getSession("UserToken", null, globalConfig.getString("ceUserId"), globalConfig.getString("cePassword"));
		//Generate a Token URL with Session ID
		String token = session.getToken(false);
		token = URLEncoder.encode(token);
		
		//Generate the DocURL to use the FileNet Viewer
		docURL = globalConfig.getString("httpHeader") + "://" + globalConfig.getString("aeServerName") + ":" + globalConfig.getString("applicationPort") + "/Workplace/getContent?vsId=" + docId + "&objectStoreName=" + globalConfig.getString("objectstoreName") + "&objectType=document&ut=" + token;
		
		if (loggingEnabled == true)
		{
			docServiceLog.log(docServiceLog.INFO, "docURL: " + docURL);
			docServiceLog.log(docServiceLog.INFO, "Leaving CEManager -> getDocumentURL()");
		}
		
		//Return the URL
		return docURL;
	}
	
	public FnDocumentList storeDocuments(String importFolder, DocServicesLog docServiceLog)
		throws Exception
	{
		//Define Global Config Properties file
		ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
		//Define the Doc Class Properties Resource
		ResourceBundle docClassConfigProps = null;
		//XML Reader object for the Input XML
		XMLReader inputXML = new XMLReader();
		//Document List generated from the Input XML
		FnDocumentList importDocList = new FnDocumentList();
		//Document List to be used for verifying the storing of documents and the result XML
		FnDocumentList docList = new FnDocumentList();
		//CE Object Store
		ObjectStore os = null;
		//Index File Boolean
		boolean indexFileFound = false;
		//Import Status File Boolean
		boolean importStatusFileFound = false;
		//Import Status so Files and XML can be moved to the Archive Location
		boolean importSuccess = false;
		
		try
		{
			docServiceLog.log(docServiceLog.INFO, "Entered CEManager -> storeDocuments()");
			
			//Get the Import root location
			String importDocumentsRoot = globalConfig.getString("importDocumentsRoot");
			//Get the Import Success location
			String importDocumentsSuccessPath = globalConfig.getString("importDocumentsSuccessPath");
			//Get the Import Failed location
			String importDocumentsErrorPath = globalConfig.getString("importDocumentsErrorPath");
			//Get the Import Success Filename
			String importSuccessFile = globalConfig.getString("importSuccessFile");
			//Get the Import Failed Filename
			String importErrorFile = globalConfig.getString("importErrorFile");
						
			//Update the path to be proper for Java
			if (importDocumentsRoot.contains("/") == false)
			{
				String tempLocation = "";
				tempLocation = importDocumentsRoot.replaceAll("\\", "/");
				importDocumentsRoot = tempLocation;
			}
			
			//Get the Import Id to know which directory of documents and XMLs to grab
			//Path to the Documents based on the Import Id
			String importDocumentsLocation = "";
			importDocumentsLocation = importDocumentsRoot + "/" + importFolder;
			docServiceLog.log(docServiceLog.INFO, "Import Docs Location: " + importDocumentsLocation);
			
			//Get the Index File Extension from the Global Config Properties file
			String importIndexFileExtension = "";
			importIndexFileExtension = globalConfig.getString("importIndexFileExtension");
			docServiceLog.log(docServiceLog.INFO, "Import Index File Extension: " + importIndexFileExtension);
			
			//Get the Index File
			String importDocumentsXML = "";
			File importDocumentsFolder = new File(importDocumentsLocation);
			if (importDocumentsFolder.isDirectory() == true)
			{
				docServiceLog.log(docServiceLog.INFO, "Import Docs Location " + importDocumentsLocation + " is a Directory");
				//Get list of Files - should only be 1 to process
				File[] listOfFiles = importDocumentsFolder.listFiles();
				for (File f : listOfFiles)
				{
					docServiceLog.log(docServiceLog.INFO, "Checking if the File " + f.getName() + " is the Index File");
					//Filter to only the Index File Extension for Importing
					if ((f.getName().contains("." + importIndexFileExtension) == true) && (f.isFile() == true))
					{
						if ((f.getName().contains(importSuccessFile) == true) && (f.isFile() == true))
						{
							docServiceLog.log(docServiceLog.INFO, "Import Success File");
							importStatusFileFound = true;
							//indexFileFound = false;
							//break;
						}
						else if ((f.getName().contains(importErrorFile) == true) && (f.isFile() == true))
						{
							docServiceLog.log(docServiceLog.INFO, "Import Failed File");
							docServiceLog.log(docServiceLog.INFO, "If the issue has been corrected, then delete the " + f.getName() + " file and try again.");
							importStatusFileFound = true;
							//indexFileFound = false;
							//break;
						}
						else
						{
							docServiceLog.log(docServiceLog.INFO, "Index File FOUND");
							importDocumentsXML = importDocumentsLocation + "/" + f.getName();
							docServiceLog.log(docServiceLog.INFO, "Index File: " + importDocumentsXML);
							//statusXMLExists = false;
							indexFileFound = true;
						}
					}
					else
					{
						docServiceLog.log(docServiceLog.INFO, "Not an Index or Status File");
						//statusXMLExists = false;
						//indexFileFound = false;
					}
				}
			}
			
			//Verify the Index File was Found and the Import Status File was NOT Found
			if (indexFileFound == true && importStatusFileFound == false)
			{
				//Verify the Directory exists, the File Exists and the File is a File
				File xmlFile = new File(importDocumentsXML);
				if (importDocumentsFolder.isDirectory() == true && xmlFile.exists() == true && xmlFile.isFile() == true)
				{
					//Read the Import Documents XML
					inputXML.readFile(importDocumentsXML);
					
					//Build the FnDocumentList from the XML
					//Used for Simple XML
					//importDocList = getImportXMLData(importFolder,inputXML,importerLog);
					//Used for XML with Attributes
					importDocList = getImportXMLWithAttributesData(importFolder,inputXML,docServiceLog);
					
					//Verify the importDocList size
					if (importDocList.getCount() > 0)
					{
						docServiceLog.log(docServiceLog.INFO, importDocList.getCount() + " Document(s) to store in FileNet");
						
						//Prepare to get the Object Store Connection
						String userId = globalConfig.getString(ConstantsUtil.CE_USER_ID);
						String password = globalConfig.getString(ConstantsUtil.CE_USER_PASSWORD);
						
						//Get CE Object Store
						os = getObjectStore(userId, password, docServiceLog);
						
						//Verify Object Store Connected
						if (os != null)
						{
							//Get List of Available Doc Classes
							FnDocClassList docClassList = new FnDocClassList();
							docClassList = getCustomDocClassesInternal(os, docServiceLog);
							List<FnDocClass> docClasses = docClassList.getFnDocClassList();
							
							//Import Doc List split into a List of FnDocument objects for easier verification
							List<FnDocument> importDocListArr = importDocList.getFnDocList();
							
							//Process the Import Doc List
							for (int a = 0; a < importDocListArr.size(); a++)
							{
								//To verify the Document Class is valid
								boolean validDocumentClass = false;
								//To verify the Document Properties are valid
								boolean validDocumentProperties = false;
								
								//One FnDocument
								FnDocument fnDoc = importDocListArr.get(a);
								//Document Class for FnDocument
								String docClass = fnDoc.getFnDocumentClass();
								//FnPropertyList for FnDocument
								FnPropertyList fnDocProps = fnDoc.getFnDocProps();
								List<FnProperty> fnDocPropsArr = fnDocProps.getFnProps();
								
								//Verify Doc Class with importDocList
								for (int b = 0; b < docClasses.size(); b++)
								{
									//One Document Class from the List of Available Document Classes
									FnDocClass oneDocClass = docClasses.get(b);
									//Verify fnDoc Doc Class is a valid Document Class for the System
									if (docClass.equalsIgnoreCase(oneDocClass.getName()) == true)
									{
										docServiceLog.log(docServiceLog.INFO, "Document Class is valid");
										//Update validDocumentClass to True
										validDocumentClass = true;
										//FnPropertyList for oneDocClass
										FnPropertyList oneDocClassProps = oneDocClass.getFnDocClassProps();
										List<FnProperty> oneDocClassPropsArr = oneDocClassProps.getFnProps();
										//Real Properties Count
										int realPropsCount = fnDocPropsArr.size();
										//Verify Properties Count
										int verifyPropsCount = 0;
										//Loop through fnDoc Properties to verify the Properties with the Doc Class
										//Verify Doc Properties with importDocList
										for (int c = 0; c < fnDocPropsArr.size(); c++)
										{
											//one fnDoc Property to compare with the System
											FnProperty fnDocProp = fnDocPropsArr.get(c);
											for (int d = 0; d < oneDocClassPropsArr.size(); d++)
											{
												FnProperty oneDocClassProp = oneDocClassPropsArr.get(d);
												if (fnDocProp.getName().equals(oneDocClassProp.getName()) == true)
												{
													//Update verifyPropsCount
													verifyPropsCount++;
													break;
												}
											}
										}
										//Check verifyPropsCount and realPropsCount
										if (verifyPropsCount == realPropsCount)
										{
											docServiceLog.log(docServiceLog.INFO, "Document Properties are valid");
											//Update validDocumentProperties to True
											validDocumentProperties = true;
											break;
										}
									}
								}
								//if validDocumentClass is True and validDocumentProperties is True, continue to process the FnDoc
								if (validDocumentClass == true && validDocumentProperties == true)
								{
									//Create Instance FileNet Doc
									Document doc = null;
									//Import File
									File importFile = null;
									//Get list of Files - should only be 1 to process
									File[] listOfFiles = importDocumentsFolder.listFiles();
									for (File f : listOfFiles)
									{
										if (f.getName().contains("." + importIndexFileExtension) == false)
										{
											docServiceLog.log(docServiceLog.INFO, "Import File: " + f.getName());
											importFile = f;
											//break;
											//Switch this section for multiple files to import with 1 xml
										}
									}
									//Create FileNet Document with No Properties
									doc = createDocument(os, docClass, importFile, docServiceLog);
									
									//Create Doc Import Folder
									String docDestinationPath = "";
									docDestinationPath = createFolderForImport(os,docClass,docServiceLog);
									
									//Get Doc Properties from FileNet
									Properties properties = doc.getProperties();
									
									//Get the Importer Type either Desktop or Web to be used later for Document Naming
									String importerType = "";
									importerType = globalConfig.getString("importerType");
									
									//Get the Doc Class Props file
									docClassConfigProps = ResourceBundle.getBundle(docClass + "Config");
									
									//Get the Document Naming Property from the Doc Class Props file
									String importDocumentNamingByProperty = "";
									importDocumentNamingByProperty = docClassConfigProps.getString("importDocumentNamingByProperty");
									
									//Import Doc Name Property Value truncated with No Spaces
									String importDocumentNamingByPropertyValue = "";
									
									docServiceLog.log(docServiceLog.INFO, "The following properties have been applied to the Document");
									docServiceLog.log(docServiceLog.INFO, "===========================================================");
									//Add fnDocProperties to FileNet Doc
									for (int x = 0; x < fnDocPropsArr.size(); x++)
									{
										FnProperty fnDocProp = fnDocPropsArr.get(x);
										if (properties.isPropertyPresent(fnDocProp.getName()) == true)
										{
											docServiceLog.log(docServiceLog.INFO, "Property: " + fnDocProp.getName());
											//Add fnDocProp to FileNet Doc
											properties.putValue(fnDocProp.getName(), fnDocProp.getValue());
											//Compare the Import Document Naming Property Value with the Current Property Name
											if ((importDocumentNamingByProperty.equals("") == false) && (importDocumentNamingByProperty.equals(fnDocProp.getName()) == true))
											{
												//Save the Import Document Naming Property Value
												importDocumentNamingByPropertyValue = fnDocProp.getValue();
											}
										}
										else
										{
											docServiceLog.log(docServiceLog.INFO, "Property: " + fnDocProp.getName() + " NOT applied");
										}
									}
									
									//Cleanup the importDocumentNamingByPropertyValue to truncate any spaces
									if (importDocumentNamingByPropertyValue.equals("") == false)
									{
										String tempName = "";
										tempName = importDocumentNamingByPropertyValue.replaceAll("\\s+", "");
										importDocumentNamingByPropertyValue = "";
										importDocumentNamingByPropertyValue = tempName;
									}
									
									//Add Global Properties to FileNet Doc
									String importDocumentsGlobalPropsList = globalConfig.getString("importDocumentsGlobalPropsList");
									//Split the importDocumentsGlobalPropsList to add the proper values
									String[] importDocsGlobalPropsData = null;
									//Verify the split is needed
									if (importDocumentsGlobalPropsList.contains(",") == true)
									{
										importDocsGlobalPropsData = importDocumentsGlobalPropsList.split(",");
										for (int g = 0; g < importDocsGlobalPropsData.length; g++)
										{
											if (properties.isPropertyPresent(importDocsGlobalPropsData[g]) == true)
											{
												docServiceLog.log(docServiceLog.INFO, "Property: " + importDocsGlobalPropsData[g]);
												//Add Document Title to the FileNet Doc
												if (importDocsGlobalPropsData[g].equals("DocumentTitle") == true)
												{
													properties.putValue(importDocsGlobalPropsData[g], fnDoc.getFnDocumentName());
												}
												//Add Doc Location to the FileNet Doc
												else if (importDocsGlobalPropsData[g].equals("Doc_Location") == true)
												{
													properties.putValue(importDocsGlobalPropsData[g], docDestinationPath);
												}
												else
												{
													//Never Get Here
												}
											}
											else
											{
												docServiceLog.log(docServiceLog.INFO, "Property: " + importDocsGlobalPropsData[g] + " NOT applied");
											}
										}
									}
									else
									{
										if (properties.isPropertyPresent(importDocumentsGlobalPropsList) == true)
										{
											docServiceLog.log(docServiceLog.INFO, "Property: " + importDocumentsGlobalPropsList);
											//Add Document Title to the FileNet Doc
											if (importDocumentsGlobalPropsList.equals("DocumentTitle") == true)
											{
												properties.putValue(importDocumentsGlobalPropsList, fnDoc.getFnDocumentName());
											}
											//Add Doc Location to the FileNet Doc
											else if (importDocumentsGlobalPropsList.equals("Doc_Location") == true)
											{
												properties.putValue(importDocumentsGlobalPropsList, docDestinationPath);
											}
											else
											{
												//Never Get Here
											}
										}
									}
																	
									//Save the Doc
									doc.save(RefreshMode.NO_REFRESH);
									docServiceLog.log(docServiceLog.INFO, "===========================================================");
									
									//CheckIn Doc
									docServiceLog.log(docServiceLog.INFO, "Check In the Document");
									doc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
							        doc.save(RefreshMode.NO_REFRESH);
							        //File the Doc
							        ReferentialContainmentRelationship rcr = null;
							        
							        //Check the importDocumentNamingByPropertyValue to determine if the Doc Container Name uses a different value 
							        // than the ImportFolder name.  Typically, the Desktop version will need this update since it only uses
							        // a Batch Id name for the Import Folder
							        if (importDocumentNamingByPropertyValue.equalsIgnoreCase("") == false)
							        {
							        	//File with the importDocumentNamingByProperty as part of the Container Name with ImportFolder name
							        	rcr = fileDocument(os,doc,importDocumentNamingByPropertyValue + importFolder,docDestinationPath,docServiceLog);
							        }
							        else
							        {
							        	//File Normal with Import Folder Name
							        	rcr = fileDocument(os,doc,importFolder,docDestinationPath,docServiceLog);
							        }
							        
							        //Verify the ReferentialContainmentRelationship
							        if (doc != null & rcr != null)
							        {
							        	//Save FileNet Doc
								        doc.save(RefreshMode.REFRESH);
								        docServiceLog.log(docServiceLog.INFO, "Document: " + fnDoc.getFnDocumentName() + " stored successfully.");
								        docServiceLog.log(docServiceLog.INFO, "Document ID: " + doc.get_Id().toString());
										//Update FnDoc Status
								       	//Set fnDoc Status to 0
								        fnDoc.setErrorFlag(0);
								        //Set fnDoc Doc Id
								        fnDoc.setFnDocumentID(doc.get_Id().toString());
								        //Add fnDoc to docList
								        docList.addFnDoc(fnDoc);
								        //Update fnDocList Status
								        docList.setErrorFlag(0);
								        //Update the Import Status to True
								        importSuccess = true;
								        //Move the Import Folder and Files to the Success Folder
								        //importDocumentsSuccessPath
								        //Used to split the result from getDateTime which is in the format YYYY/MM/DD hh:mm:ss
								        String destCurrentDatePath = "";
										String[] currentDateData1 = null;
										//Used to split the result from currentDateData1[2] which is in the format DD hh:mm:ss to remove hh:mm:ss
										String[] currentDateData2 = null;
										//Get the YYYY/MM/DD hh:mm:ss split up
										currentDateData1 = getDateTime().split("/");
										//Get the DD hh:mm:ss split up
										currentDateData2 = currentDateData1[2].split(" ");
										//Update destPathForImportFolder
										destCurrentDatePath = currentDateData1[0] + "/" + currentDateData1[1] + "/" + currentDateData2[0];
								        
								        if (moveImportFolder(importDocumentsLocation,importDocumentsSuccessPath + "/" + destCurrentDatePath + "/" + importFolder,docServiceLog) == true)
								        {
								        	docServiceLog.log(docServiceLog.INFO, "Import Folder moved successfully to the Success Folder");
								        }
								        else
								        {
								        	docServiceLog.log(docServiceLog.INFO, "Import Folder did NOT move successfully to the Success Folder");
								        }
							        }
							        else
							        {
							        	//Document or RCR is NULL
							        	//Delete the Document
							        	doc.delete();
							        	doc.save(RefreshMode.NO_REFRESH);
							        	doc = null;
							        	docServiceLog.log(docServiceLog.INFO, "Document Object has been deleted.");
							        	//Failed to Add the Document to FileNet - possible RCR failure
							        	docServiceLog.log(docServiceLog.INFO, "Document: " + fnDoc.getFnDocumentName() + " failed to store in FileNet.");
							        	//Update FnDoc Status
								       	//Set fnDoc Status to 1
								        fnDoc.setErrorFlag(1);
								        //Update the Import Status to False
								        importSuccess = false;
								        //Move the Import Folder and Files to the Error Folder
								        //importDocumentsErrorPath
								        //Used to split the result from getDateTime which is in the format YYYY/MM/DD hh:mm:ss
								        String destCurrentDatePath = "";
										String[] currentDateData1 = null;
										//Used to split the result from currentDateData1[2] which is in the format DD hh:mm:ss to remove hh:mm:ss
										String[] currentDateData2 = null;
										//Get the YYYY/MM/DD hh:mm:ss split up
										currentDateData1 = getDateTime().split("/");
										//Get the DD hh:mm:ss split up
										currentDateData2 = currentDateData1[2].split(" ");
										//Update destPathForImportFolder
										destCurrentDatePath = currentDateData1[0] + "/" + currentDateData1[1] + "/" + currentDateData2[0];
										
								        if (moveImportFolder(importDocumentsLocation,importDocumentsErrorPath + "/" + destCurrentDatePath + "/" + importFolder,docServiceLog) == true)
								        {
								        	docServiceLog.log(docServiceLog.INFO, "Import Folder moved successfully to the Error Folder");
								        }
								        else
								        {
								        	docServiceLog.log(docServiceLog.INFO, "Import Folder did NOT move successfully to the Error Folder");
								        }
							        }
								}
								else
								{
									//Document is bad because the Doc Class is wrong and/or the Doc Properties are wrong
									docServiceLog.log(docServiceLog.INFO, "Document: " + fnDoc.getFnDocumentName() + " has an invalid Document Class and Properties");
									//Update FnDoc Status
									fnDoc.setErrorFlag(1);
									//Update the Import Status to False
							        importSuccess = false;
							        //Move the Import Folder and Files to the Error Folder
							        //importDocumentsErrorPath
							        //Used to split the result from getDateTime which is in the format YYYY/MM/DD hh:mm:ss
							        String destCurrentDatePath = "";
									String[] currentDateData1 = null;
									//Used to split the result from currentDateData1[2] which is in the format DD hh:mm:ss to remove hh:mm:ss
									String[] currentDateData2 = null;
									//Get the YYYY/MM/DD hh:mm:ss split up
									currentDateData1 = getDateTime().split("/");
									//Get the DD hh:mm:ss split up
									currentDateData2 = currentDateData1[2].split(" ");
									//Update destPathForImportFolder
									destCurrentDatePath = currentDateData1[0] + "/" + currentDateData1[1] + "/" + currentDateData2[0];
									
							        if (moveImportFolder(importDocumentsLocation,importDocumentsErrorPath + "/" + destCurrentDatePath + "/" + importFolder,docServiceLog) == true)
							        {
							        	docServiceLog.log(docServiceLog.INFO, "Import Folder moved successfully to the Error Folder");
							        }
							        else
							        {
							        	docServiceLog.log(docServiceLog.INFO, "Import Folder did NOT move successfully to the Error Folder");
							        }
								}
							}//End for Import Doc List
						}
						else
						{
							//Object Store connection is NULL
						}
					}
					else
					{
						//importDocList size is 0
					}
				}
			}
			else
			{
				//Used to split the result from getDateTime which is in the format YYYY/MM/DD hh:mm:ss
		        String destCurrentDatePath = "";
				String[] currentDateData1 = null;
				//Used to split the result from currentDateData1[2] which is in the format DD hh:mm:ss to remove hh:mm:ss
				String[] currentDateData2 = null;
				//Get the YYYY/MM/DD hh:mm:ss split up
				currentDateData1 = getDateTime().split("/");
				//Get the DD hh:mm:ss split up
				currentDateData2 = currentDateData1[2].split(" ");
				//Update destPathForImportFolder
				destCurrentDatePath = currentDateData1[0] + "/" + currentDateData1[1] + "/" + currentDateData2[0];
				
				if (importStatusFileFound == true)
				{
					//Import Location contains an Import Status File from a previous run either successful or a failure.  Processing will stop.
					docServiceLog.log(docServiceLog.INFO, "Import Location - " + importDocumentsLocation + " has a Success or Fail Import Status File.  Processing has ended.");
				}
				else
				{
					//Import Location is not a directory, Index File is missing or is not a file
					docServiceLog.log(docServiceLog.INFO, "Import Location - " + importDocumentsLocation + " and the Index File appear to be missing.  Processing has ended.");
				}
				//Move the Import Folder to the Error Folder
				if (moveImportFolder(importDocumentsLocation,importDocumentsErrorPath + "/" + destCurrentDatePath + "/" + importFolder,docServiceLog) == true)
		        {
		        	docServiceLog.log(docServiceLog.INFO, "Import Folder moved successfully to the Error Folder");
		        }
		        else
		        {
		        	docServiceLog.log(docServiceLog.INFO, "Import Folder did NOT move successfully to the Error Folder");
		        }
			}
		}
		catch (Exception e)
		{
			docServiceLog.log(docServiceLog.INFO, "Exception - ErrorFlag = 2");
			docServiceLog.log(docServiceLog.ERROR, docServiceLog.getStackTrace(e));
		}
		finally
		{
			//Pop the Connection Subject
			UserContext.get().popSubject();
			//Reset Object Store variable
			os = null;
		}
		
		docServiceLog.log(docServiceLog.INFO, "Leaving CEManager -> storeDocuments()");

		return docList;
	}
	
	//******************************************************
	//Private Helper Methods
	//******************************************************
	
	@SuppressWarnings("unused")
	private ObjectStore getObjectStore(String userId, String password, DocServicesLog docServiceLog)
		throws Exception
	{
		try
		{
			docServiceLog.log(docServiceLog.INFO, "Entered CEManager -> getObjectStore()");
			ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
			String connectionURI = globalConfig.getString(ConstantsUtil.CE_CONNECTION_URI);
			String stanzaName = globalConfig.getString(ConstantsUtil.CE_STANZA_NAME);
			String objectStoreName = globalConfig.getString(ConstantsUtil.CE_OBJECTSTORE_NAME);
			
			ObjectStore os = null;
			
			//Get a CE Connection
			Connection conn = Factory.Connection.getConnection(connectionURI);
			if (conn != null)
			{
				docServiceLog.log(docServiceLog.INFO, "CE Connection Successful");
				//Get a CE Connection Subject
				UserContext uc = UserContext.get();
				Subject subject = UserContext.createSubject(conn, userId, password, stanzaName);
				uc.pushSubject(subject);
				
				if (uc != null)
				{
					docServiceLog.log(docServiceLog.INFO, "CE Connection Subject Successful");
					//Get a CE Domain Object
					Domain dom = Factory.Domain.fetchInstance(conn, null, null);
					
					if (dom != null)
					{
						docServiceLog.log(docServiceLog.INFO, "CE Domain Connection Successful");
						//Get the Object Store
						os = Factory.ObjectStore.fetchInstance(dom, objectStoreName, null);
						
						if (os != null)
						{
							docServiceLog.log(docServiceLog.INFO, "CE Object Store Connection Successful");
						}
						else
						{
							docServiceLog.log(docServiceLog.INFO, "CE Object Store Connection Failed");
						}
					}
					else
					{
						docServiceLog.log(docServiceLog.INFO, "CE Domain Connection Failed");
					}
				}
				else
				{
					docServiceLog.log(docServiceLog.INFO, "CE Connection Subject Failed");
				}
			}
			else
			{
				docServiceLog.log(docServiceLog.INFO, "CE Connection Failed");
			}
			docServiceLog.log(docServiceLog.INFO, "Leaving CEManager -> getObjectStore()");
			return os;
		}
		catch (Exception e)
		{
			docServiceLog.log(docServiceLog.ERROR, docServiceLog.getStackTrace(e));
			throw e;
		}
	}
	
	private String checkFolderInCE(ObjectStore os, String folderToVerify, DocServicesLog docServiceLog)
	{
		//Folder value Found
		String folderValue = "";
		
		//importerLog.log(importerLog.INFO, "Entered CEManager -> checkFolderInCE()");
		//importerLog.log(importerLog.INFO, "Verifying the Folder Path: " + folderToVerify);
		
		if (folderToVerify.equals("") == false)
		{
			try
			{
				//Check if the Folder exists in the Object Store
				Folder folder = Factory.Folder.fetchInstance(os, folderToVerify, null);
				//Return the ID of the Folder proving that the Folder exists in the Object Store
				folderValue = folder.get_Id().toString();
				//importerLog.log(importerLog.INFO, "Leaving CEManager -> checkFolderInCE()");
				return folderValue;
			}
			catch (Exception e)
			{
				//importerLog.log(importerLog.INFO, "Leaving CEManager -> checkFolderInCE()");
				//Return Empty String for the Folder to show that it does not exist in the Object Store
				return "";
			}
		}
		else
		{
			//importerLog.log(importerLog.INFO, "Leaving CEManager -> checkFolderInCE()");
			//Return Empty String for the Folder to show that it does not exist in the Object Store
			return "";
		}
	}
	
	private FnDocClassList getCustomDocClassesInternal(ObjectStore os, DocServicesLog docServiceLog)
	{
		//List to hold the Custom Doc Classes List
		FnDocClassList customDocClassesList = new FnDocClassList();
		
		try
		{
			docServiceLog.log(docServiceLog.INFO, "Entered CEManager -> getCustomDocClassesInternal()");
						
			//Define the Global Properties Resource
			ResourceBundle globalProps = null;
			globalProps = ResourceBundle.getBundle("GlobalConfig");
			String userId = globalProps.getString(ConstantsUtil.CE_USER_ID);
			String password = globalProps.getString(ConstantsUtil.CE_USER_PASSWORD);
			
			//Verify Object Store Connected
			if (os != null)
			{
				//Exclude List of Doc Classes
				String docClassExcludeList = "";
				docClassExcludeList = globalProps.getString("docClassExcludeList");
				//Array to hold Doc Class Exclude List
				String[] docClassExcludeData = null;
				boolean excludeListSplit = false;
				
				//Split the list because there is more than 1 Doc Class to exclude
				if (docClassExcludeList.contains(",") == true)
				{
					//Split the list to hold in the array
					docClassExcludeData = docClassExcludeList.split(",");
					excludeListSplit = true;
				}
						
				//Fetch selected class definition from the server
				ClassDefinition docClassDef = Factory.ClassDefinition.fetchInstance(os, "Document", null);
				//Get the SubClasses
				ClassDefinitionSet subDocClassDefSet = docClassDef.get_ImmediateSubclassDefinitions();
								
				//PropertyDefinitionList propDefList = docClassDef.get_PropertyDefinitions();
				Iterator iter = subDocClassDefSet.iterator();
				ClassDefinition classDef = null;
				
				docServiceLog.log(docServiceLog.INFO, "=============================================");
				
				//Loop until Class Definitions are found
				while (iter.hasNext())
				{	        				
					//Get the Class Definition
					classDef = (ClassDefinition) iter.next();
					//Output Class Name
					//importerLog.log(importerLog.INFO, "Class: " + classDef.get_DisplayName());
					
					//Only want the Custom Class Definitions			
					if (classDef.get_IsHidden() == false && classDef.get_IsSystemOwned() == false && classDef.get_IsPersistent() == true
							&& classDef.get_InstalledByAddOn() == null)
					{
						boolean skipClassDef = false;
						//excludeListSplit is true, then we need to check each exclude name against the classDef
						if (excludeListSplit == true)
						{
							for (int x = 0; x < docClassExcludeData.length; x++)
							{
								if (docClassExcludeData[x].equals(classDef.get_DisplayName()) == true)
								{
									skipClassDef = true;
								}
							}
						}
						else //Exclude List of 1 Doc Class
						{
							if (docClassExcludeList.equals(classDef.get_DisplayName()) == true)
							{
								skipClassDef = true;
							}
						}
						if (skipClassDef == false)
						{
							//Get the SubClasses
							ClassDefinitionSet subDocClassSet = classDef.get_ImmediateSubclassDefinitions();
							//Verify if the Class Definition has SubClasses.  We only want the last set of SubClasses
							if (subDocClassSet.isEmpty() == false)
							{
								Iterator iter2 = subDocClassSet.iterator();
								ClassDefinition subClassDef = null;
								while (iter2.hasNext())
								{
									//Get the SubClass Definition
									subClassDef = (ClassDefinition) iter2.next();
									//Output Sub Class Name
									//importerLog.log(importerLog.INFO, "Sub Class Definition: " + subClassDef.get_DisplayName());
									if (subClassDef.get_IsHidden() == false && subClassDef.get_IsSystemOwned() == false && subClassDef.get_IsPersistent() == true
											&& subClassDef.get_InstalledByAddOn() == null)
									{
										//Create the Doc Class Object
										FnDocClass fnDocClass = new FnDocClass();
										//Set the Name of the Doc Class
										fnDocClass.setName(subClassDef.get_DisplayName());
										docServiceLog.log(docServiceLog.INFO, "Class: " + subClassDef.get_DisplayName());
										//Get the List of Properties for the Doc Class
										FnPropertyList fnPropsList = new FnPropertyList();
										fnPropsList = getCustomDocProps(os, subClassDef.get_DisplayName(), docServiceLog);
										//Add Props List to Class
										fnDocClass.setFnDocClassProps(fnPropsList);
										//Add the Doc Class to the Doc Class List
										customDocClassesList.addFnDocClass(fnDocClass);
										//importerLog.log(importerLog.INFO, "Saving Class Definition: " + subClassDef.get_DisplayName());
										//importerLog.log(importerLog.INFO, "=============================================");
									}
								}
							}
							else
							{
								//Create the Doc Class Object
								FnDocClass fnDocClass = new FnDocClass();
								//Set the Name of the Doc Class
								fnDocClass.setName(classDef.get_DisplayName());
								docServiceLog.log(docServiceLog.INFO, "Class: " + classDef.get_DisplayName());
								//Get the List of Properties for the Doc Class
								FnPropertyList fnPropsList = new FnPropertyList();
								fnPropsList = getCustomDocProps(os, classDef.get_DisplayName(), docServiceLog);
								//Add the Properties to the Doc Class
								fnDocClass.setFnDocClassProps(fnPropsList);							
								//Add the Doc Class to the Doc Class List
								customDocClassesList.addFnDocClass(fnDocClass);
								//importerLog.log(importerLog.INFO, "Saving Class Definition: " + classDef.get_DisplayName());
								//importerLog.log(importerLog.INFO, "=============================================");
							}
						}
					}
				}
				docServiceLog.log(docServiceLog.INFO, "=============================================");
				docServiceLog.log(docServiceLog.INFO, "Leaving CEManager -> getCustomDocClassesInternal()");
				//Reset Object Store variable
				os = null;
			}
		}
		catch (Exception e)
		{
			docServiceLog.log(docServiceLog.INFO, "Exception - ErrorFlag = 2");
			docServiceLog.log(docServiceLog.ERROR, docServiceLog.getStackTrace(e));
			//Reset Object Store variable
			os = null;
		}
				
		return customDocClassesList;
	}
	
	private FnPropertyList getCustomDocProps(ObjectStore os, String docClassName, DocServicesLog docServiceLog)
	{
		docServiceLog.log(docServiceLog.INFO, "Entered CEManager -> getCustomDocProps()");
		//FnPropertyList to hold the Properties
		FnPropertyList customDocPropsList = new FnPropertyList();
				
		//Construct property filter to ensure only Custom PropertyDefinitions are returned
		PropertyFilter pf = new PropertyFilter();
		pf.addIncludeType(0, null, Boolean.TRUE, FilteredPropertyType.ANY, null); 

		//Fetch selected class definition from the server
		ClassDefinition docClassDef = Factory.ClassDefinition.fetchInstance(os, docClassName, pf);
		PropertyDefinitionList propDefList = docClassDef.get_PropertyDefinitions();
		
		Iterator iter = propDefList.iterator();
		PropertyDefinition propDef = null;
		
		docServiceLog.log(docServiceLog.INFO, "=============================================");
		
		//Loop until property definition found
		while (iter.hasNext())
		{	        				
			//Get the Property Definition
			propDef = (PropertyDefinition) iter.next();
			
			//Only want the Custom Property Definitions			
			if (propDef.get_IsHidden() == false && propDef.get_IsSystemOwned() == false && propDef.get_CopyToReservation() == true)
			{
				//Create the Property Object
				FnProperty fnProp = new FnProperty();
				//Set the Name of the Doc Class
				fnProp.setFnClass(docClassName);
				//Set the Name of the Property
				fnProp.setName(propDef.get_DisplayName());
				//Add the Property to the List
				customDocPropsList.addFnProp(fnProp);
				docServiceLog.log(docServiceLog.INFO, "Property: " + propDef.get_DisplayName());
			}
		}
		docServiceLog.log(docServiceLog.INFO, "=============================================");
		docServiceLog.log(docServiceLog.INFO, "Leaving CEManager -> getCustomDocProps()");
		return customDocPropsList;
	}
	
	private FnDocumentList getImportXMLData(String docName, XMLReader xml, DocServicesLog docServiceLog)
	{
		//Document List to be built from the XML
		FnDocumentList importDocList = new FnDocumentList();
		
		docServiceLog.log(docServiceLog.INFO, "Entered CEManager -> getImportXMLData()");
		
		//Get the Root Element
		//<Batch>
		//</Batch>
		Element rootElement = xml.getRootElement();
		//importerLog.log(importerLog.INFO, "RootElement: " + rootElement.getNodeName());
		//Get the Document Elements
		//<Batch>
		//	<Document>
		//	</Document>
		NodeList docNodeList = xml.getElementNodesByTag(rootElement, "Document");
		//Verify the Document Elements exist
		if (docNodeList != null && docNodeList.getLength() > 0)
		{
			//Parse the Document Elements
			for (int a = 0; a < docNodeList.getLength(); a++)
			{
				//Get a Document Element
				Element docElement = (Element)docNodeList.item(a);
				//importerLog.log(importerLog.INFO, "SubElement: " + docElement.getNodeName());
				
				//Get the Document Properties Nodes
				NodeList docPropsNodeList = docElement.getChildNodes();
				//importerLog.log(importerLog.INFO, "Got Children");
				
				if (docPropsNodeList != null && docPropsNodeList.getLength() > 0)
				{
					//Valid Document so we need to store the FnDocument
					FnDocument fnDoc = new FnDocument();
					
					//Update the Doc Name
					fnDoc.setFnDocumentName(docName);
					docServiceLog.log(docServiceLog.INFO, "Document Name: " + docName);
					
					//FnPropertyList
					FnPropertyList fnPropList = new FnPropertyList();
					
					//Parse the Property Elements
					for (int b = 0; b < docPropsNodeList.getLength(); b++)
					{
						Node n = docPropsNodeList.item(b);
						if (n.getNodeType() == Node.ELEMENT_NODE)
						{
							//Get a Property Element
							Element propElement = (Element)n;
							String propName = propElement.getNodeName();
							String propValue = propElement.getTextContent();
														
							//Update the Doc Class
							if (propName.equals("Class") == true)
							{
								//Update the Doc Class
								fnDoc.setFnDocumentClass(propValue);
								docServiceLog.log(docServiceLog.INFO, "Document Class: " + propValue);
								docServiceLog.log(docServiceLog.INFO, "=====================================");
							}
							else
							{
								//Create FnProperty to store the XML property element
								FnProperty fnProp = new FnProperty();
								//Store Property Name
								fnProp.setName(propName);
								docServiceLog.log(docServiceLog.INFO, "Property Name: " + propName);
								//Store Property Value
								fnProp.setValue(propValue);
								docServiceLog.log(docServiceLog.INFO, "Property Value: " + propValue);
								//Add Property to FnPropertyList
								fnPropList.addFnProp(fnProp);
								//importerLog.log(importerLog.INFO, "Add Property to the Properties List");
								docServiceLog.log(docServiceLog.INFO, "=====================================");
							}
						}
					}
					//Add Property List to FnDoc
					fnDoc.setFnDocProps(fnPropList);
					//importerLog.log(importerLog.INFO, "Add Properties List to the Document");
					
					//Add Document to Document List
					importDocList.addFnDoc(fnDoc);
					docServiceLog.log(docServiceLog.INFO, "Document added to the Document List");
					docServiceLog.log(docServiceLog.INFO, "=====================================");
				}
			}//End For loop Document Node list
		}//End If test for Document Node list size 
		
		docServiceLog.log(docServiceLog.INFO, "Leaving CEManager -> getImportXMLData()");
		
		return importDocList;
	}
	
	private FnDocumentList getImportXMLWithAttributesData(String docName, XMLReader xml, DocServicesLog docServiceLog)
	{
		//Document List to be built from the XML
		FnDocumentList importDocList = new FnDocumentList();
		
		docServiceLog.log(docServiceLog.INFO, "Entered CEManager -> getImportXMLWithAttributesData()");
		
		//Get the Root Element
		//<Batch>
		//</Batch>
		Element rootElement = xml.getRootElement();
		docServiceLog.log(docServiceLog.INFO, "RootElement: " + rootElement.getNodeName());
		//Get the Document Elements
		//<Batch>
		//	<Document>
		//	</Document>
		NodeList docNodeList = xml.getElementNodesByTag(rootElement, "Document");
		//Verify the Document Elements exist
		if (docNodeList != null && docNodeList.getLength() > 0)
		{
			//Parse the Document Elements
			for (int a = 0; a < docNodeList.getLength(); a++)
			{
				//Get a Document Element
				Element docElement = (Element)docNodeList.item(a);
				//Get the Document Name Attribute
				String docAttrName = xml.getElementAttributeValue(docElement, "name");
				//Get the Document Value Attribute
				String docAttrValue = xml.getElementAttributeValue(docElement, "value");
												
				//Verify the name attribute is Class
				if (docAttrName.equals("Class") == true)
				{
					//Valid Document so we need to store the FnDocument
					FnDocument fnDoc = new FnDocument();
					//Update the Doc Class
					fnDoc.setFnDocumentClass(docAttrValue);
					docServiceLog.log(docServiceLog.INFO, "Document Class: " + docAttrValue);
					docServiceLog.log(docServiceLog.INFO, "=====================================");
					//Update the Doc Name
					fnDoc.setFnDocumentName(docName);
					//Get the Property Elements
					NodeList propNodeList = xml.getElementNodesByTag(docElement, "Property");

					if (propNodeList != null && propNodeList.getLength() > 0)
					{
						//Build Doc Properties List
						FnPropertyList fnPropList = new FnPropertyList();
						//Parse the Property Elements
						for (int b = 0; b < propNodeList.getLength(); b++)
						{
							//Get a Property Element
							Element propElement = (Element)propNodeList.item(b);
							//Get the Property Name Attribute
							String propName = xml.getElementAttributeValue(propElement, "name");
							//Get the Document Value Attribute
							String propValue = xml.getElementAttributeValue(propElement, "value");
							//Create FnProperty to store the XML property element
							FnProperty fnProp = new FnProperty();
							//Store Property Name
							fnProp.setName(propName);
							docServiceLog.log(docServiceLog.INFO, "Property Name: " + propName);
							//Store Property Value
							fnProp.setValue(propValue);
							docServiceLog.log(docServiceLog.INFO, "Property Value: " + propValue);
							//Add Property to FnPropertyList
							fnPropList.addFnProp(fnProp);
							docServiceLog.log(docServiceLog.INFO, "=====================================");
						}//End For loop Property Node list

						//Add Property List to FnDoc
						fnDoc.setFnDocProps(fnPropList);
						
					}//End If test for Property Node list size

					//Add Document to Document List
					importDocList.addFnDoc(fnDoc);
					docServiceLog.log(docServiceLog.INFO, "Document added to the Document List");
					docServiceLog.log(docServiceLog.INFO, "=====================================");

				}//End If test for "Class"
			}//End For loop Document Node list
		}//End If test for Document Node list size 
		
		docServiceLog.log(docServiceLog.INFO, "Leaving CEManager -> getImportXMLWithAttributesData()");
		
		return importDocList;
	}
	
	private Document createDocument(ObjectStore os, String docClass, File importFile, DocServicesLog docServiceLog)
    {
		Document doc = null;
		
		docServiceLog.log(docServiceLog.INFO, "Entered CEManager -> createDocument()");
		
		//Create Content Transfer Object and Read the File
		ContentTransfer ct = createContentTransfer(importFile,docServiceLog);
		
		//Create Content Element List Object
		ContentElementList cel = createContentElements(ct,docServiceLog);
		
		if (docClass.equals("") == true)
        {
			docServiceLog.log(docServiceLog.INFO, "Create a Document with No Doc Class");
			doc = Factory.Document.createInstance(os, null);
        }
        else
        {
        	docServiceLog.log(docServiceLog.INFO, "Create a Document with the " + docClass + " Doc Class");
        	doc = Factory.Document.createInstance(os, docClass);
        }
        
		//Save the Doc - NO R/T
		//doc.save(RefreshMode.NO_REFRESH);
		//Save the Doc - R/T
		doc.save(RefreshMode.REFRESH);
		
		//doc.getProperties().putValue("DocumentTitle", f.getName());
        //mimeType = f.getName().substring(f.getName().length()-4, f.getName().length()-1);
        //doc.set_MimeType(mimeType);
        if (cel != null)
        	doc.set_ContentElements(cel);
        
        docServiceLog.log(docServiceLog.INFO, "Leaving CEManager -> createDocument()");
        
		return doc;
    }
	
	private String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    Date date = new Date();
	    return dateFormat.format(date);
	}
	
	private String getDateWithMonthName(String dateValue)
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
	
	private ContentTransfer createContentTransfer(File f, DocServicesLog docServiceLog)
    {
		ContentTransfer ctNew = null;
        
		//importerLog.log(importerLog.INFO, "Entered CEManager -> createContentTransfer()");
		
		if(readDocContentFromFile(f,docServiceLog) != null)
        {
        	ctNew = Factory.ContentTransfer.createInstance();
            ByteArrayInputStream is = new ByteArrayInputStream(readDocContentFromFile(f,docServiceLog));
            ctNew.setCaptureSource(is);
            ctNew.set_RetrievalName(f.getName());
            //TO DO
            //ctNew.set_ContentType("Some Mime Type");
        }
		//importerLog.log(importerLog.INFO, "Leaving CEManager -> createContentTransfer()");
		return ctNew;
    }
	
	@SuppressWarnings("unchecked")
	private ContentElementList createContentElements(ContentTransfer ct, DocServicesLog docServiceLog)
    {
		ContentElementList cel = null;
        
		//importerLog.log(importerLog.INFO, "Entered CEManager -> createContentElements()");
		
		if(ct != null)
        {
        	cel = Factory.ContentElement.createList();
            cel.add(ct);
        }
		
		//importerLog.log(importerLog.INFO, "Leaving CEManager -> createContentElements()");
		return cel;
    }
	
	private byte[] readDocContentFromFile(File f, DocServicesLog docServiceLog)
    {
		FileInputStream is;
        byte[] b = null;
        
        //importerLog.log(importerLog.INFO, "Entered CEManager -> readDocContentFromFile()");
        
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
        
        //importerLog.log(importerLog.INFO, "Leaving CEManager -> readDocContentFromFile()");
        return b;
    }
	
	private ReferentialContainmentRelationship fileDocument(ObjectStore os, Document doc, String DocContainerName, String folderName, DocServicesLog docServiceLog)
	{
		ReferentialContainmentRelationship rcr = null;
		
		docServiceLog.log(docServiceLog.INFO, "Entered CEManager -> fileDocument()");
		
		Folder f = Factory.Folder.getInstance(os, null, folderName);
		
		try
		{
			docServiceLog.log(docServiceLog.INFO, "Document " + DocContainerName + " filed in " + folderName);
			rcr = (ReferentialContainmentRelationship) f.file(doc, AutoUniqueName.NOT_AUTO_UNIQUE, DocContainerName, DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
			rcr.save(RefreshMode.NO_REFRESH);
		}
		catch (Exception e)
		{
			if (e instanceof EngineRuntimeException) 
			{
				EngineRuntimeException fnEx = (EngineRuntimeException) e;
				if (fnEx.getExceptionCode().equals(ExceptionCode.E_NOT_UNIQUE) == true) 
				{			
					docServiceLog.log(docServiceLog.INFO, "Document is NOT UNIQUE. Possible Duplicate.");
					System.out.println("Document is NOT UNIQUE. Possible Duplicate.");
					System.out.println("Exception: " + e.getMessage());	
					//Update the rcr so the document is deleted
					rcr = null;
		        }
			}
		    else
		    {
		    	// A standard Java exception.
		        System.out.println("Exception: " + e.getMessage());
		    }
		}
		
		docServiceLog.log(docServiceLog.INFO, "Leaving CEManager -> fileDocument()");
		return rcr;
	}

	private void createFolder(ObjectStore os, String fPath, String fName, DocServicesLog docServiceLog)
    {
		//importerLog.log(importerLog.INFO, "Entered CEManager -> createFolder()");
		Folder f = Factory.Folder.getInstance(os, null, fPath);
		Folder nf = f.createSubFolder(fName);
		nf.save(RefreshMode.NO_REFRESH);
		//importerLog.log(importerLog.INFO, "Leaving CEManager -> createFolder()");
    }

	private String createFolderForImport(ObjectStore os, String docClass, DocServicesLog docServiceLog)
	{
		//Folder Location for Document
		String docDestinationPath = "";
		//Doc Class Config Properties file
		ResourceBundle docClassConfigProps = null;
		
		docServiceLog.log(docServiceLog.INFO, "Entered CEManager -> createFolderForImport()");
		
		docClassConfigProps = ResourceBundle.getBundle(docClass + "Config");
		//Get Folder Location to store FileNet Doc by Date
		String docClassDocsRootLocation = docClassConfigProps.getString("docClassDocsRootLocation");
		
		//Used to split the result from getDateTime which is in the format YYYY/MM/DD hh:mm:ss
		String[] currentDateData1 = null;
		//Used to split the result from currentDateData1[2] which is in the format DD hh:mm:ss to remove hh:mm:ss
		String[] currentDateData2 = null;
		//Get the YYYY/MM/DD hh:mm:ss split up
		currentDateData1 = getDateTime().split("/");
		//Get the DD hh:mm:ss split up
		currentDateData2 = currentDateData1[2].split(" ");
		//Update docDestinationPath
		docDestinationPath = docClassDocsRootLocation + "/" + currentDateData1[0] + "/" + currentDateData1[1] + "/" + currentDateData2[0];
		//Verify the Folder exists and if not, then create the folder path
		if (checkFolderInCE(os,docDestinationPath,docServiceLog).equals("") == true)
		{
			docServiceLog.log(docServiceLog.INFO, "Folder Path " + docDestinationPath + " does not exist and will be created.");
			//Create the Folder path and verify it again
			String[] docDestPathData = docDestinationPath.split("/");
			String tempDestPath = "";
			String tempDestPath2 = "";
			//Set tempDestPath to docClassDocsRootLocation
			//tempDestPath = docClassDocsRootLocation;
			boolean destinationPathExists = false;
			while (destinationPathExists == false)
			{
				for (int z = 0; z < docDestPathData.length; z++)
				{
					//importerLog.log(importerLog.INFO, "tempDestPath: " + tempDestPath);
					//importerLog.log(importerLog.INFO, "docDestPathData: " + docDestPathData[z]);
					if (tempDestPath.equals("") == true)
					{
						//First run through loop because it includes the Default Root Path
						tempDestPath = docDestPathData[z] + "/";
						//importerLog.log(importerLog.INFO, "1st Folder Path " + tempDestPath);
					}
					else if (tempDestPath.equals(docDestinationPath) == true)
					{
						docServiceLog.log(docServiceLog.INFO, "Folder Path " + tempDestPath + " exists and is ready for Document import.");
						destinationPathExists = true;
						break;
					}
					else
					{
						//Backup Previous tempDestPath
						tempDestPath2 = tempDestPath;
						//If first char is /
						if (tempDestPath.equals("/") == true)
						{
							tempDestPath = tempDestPath + docDestPathData[z] + "/";
						}
						else
						{
							tempDestPath = tempDestPath + "/" + docDestPathData[z] + "/";
						}
						//Remove the trailing /
						tempDestPath = tempDestPath.substring(0, tempDestPath.length() - 1);
						//importerLog.log(importerLog.INFO, "Folder Path " + tempDestPath);
						if (checkFolderInCE(os,tempDestPath,docServiceLog).equals("") == false)
						{
							//importerLog.log(importerLog.INFO, "Folder Path " + tempDestPath + " exists.");
							if (tempDestPath.equals(docDestinationPath) == true)
							{
								docServiceLog.log(docServiceLog.INFO, "Folder Path " + tempDestPath + " exists and is ready for Document import.");
								destinationPathExists = true;
								break;
							}
						}
						else
						{
							docServiceLog.log(docServiceLog.INFO, "Folder Path " + tempDestPath + " does not exist and will be created.");
							//Create Sub Folder
							createFolder(os,tempDestPath2,docDestPathData[z],docServiceLog);
						}
					}
				}
			}
		}
		
		docServiceLog.log(docServiceLog.INFO, "Leaving CEManager -> createFolderForImport()");
		return docDestinationPath;
	}

	private boolean moveImportFolder(String sourcePath, String destPath, DocServicesLog docServiceLog)
	{
		boolean moveSuccess = false;
		
		File source = new File(sourcePath);
		File destination = new File(destPath);
		
		docServiceLog.log(docServiceLog.INFO, "Entered CEManager -> moveImportFolder()");
		docServiceLog.log(docServiceLog.INFO, "Source Path: " + sourcePath);
		docServiceLog.log(docServiceLog.INFO, "Destination Path: " + destPath);
		
		try
		{
			if (source.isDirectory() == true && source.exists() == true)
			{
				//Move the Source to the Destination
				FileUtils.moveDirectory(source, destination);
				moveSuccess = true;
				docServiceLog.log(docServiceLog.INFO, "Import Folder moved successfully");
			}
		}
		catch (IOException e)
		{
			docServiceLog.log(docServiceLog.INFO, "ERROR: Moving the Import Folder " + sourcePath + " to " + destPath);
			e.printStackTrace();
			moveSuccess = false;
		}
		finally
		{
			source = null;
			destination = null;
		}
		
		docServiceLog.log(docServiceLog.INFO, "Leaving CEManager -> moveImportFolder()");
		
		return moveSuccess;
	}

	private boolean copyFile(File source, File dest, DocServicesLog docServiceLog) throws IOException
	{
		boolean fileCopied = false;
		
		if (!dest.exists())
		{
			dest.createNewFile();
		}
		FileInputStream in = null;
		FileOutputStream out = null;
		try
		{
			in = new FileInputStream(source);
			out = new FileOutputStream(dest);
			//Transfer bytes from In to Out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
			}
			//Check if file exists
			if (source.length() == dest.length())
			{
				docServiceLog.log(docServiceLog.INFO, "Destination File: " + dest.getName() + " created successfully");
				fileCopied = true;
			}
			else
			{
				docServiceLog.log(docServiceLog.INFO, "Destination File: " + dest.getName() + " did not create successfully.");
				fileCopied = false;
			}
			
		}
		finally
		{
			in.close();
			out.close();
		}
		return fileCopied;
	}
	
	private boolean copyDirectory(File sourceDir, File destDir, DocServicesLog docServiceLog) throws IOException
	{
		boolean dirCopied = false;
		
		if (!destDir.exists())
		{
			destDir.mkdirs();
			docServiceLog.log(docServiceLog.INFO, "Destination: " + destDir.getName() + " created");
		}
		
		File[] children = sourceDir.listFiles();
		
		for (File sourceChild : children)
		{
			String name = sourceChild.getName();
			File destChild = new File(destDir, name);
			if (sourceChild.isDirectory())
			{
				dirCopied = copyDirectory(sourceChild, destChild, docServiceLog);
			}
			else
			{
				dirCopied = copyFile(sourceChild, destChild, docServiceLog);
			}
		}
		return dirCopied;
	}
	
	private boolean deleteFile(File resource, DocServicesLog docServiceLog) throws IOException
	{
		if (resource.isDirectory())
		{
			File[] childFiles = resource.listFiles();
			for (File child : childFiles)
			{
				deleteFile(child, docServiceLog);
			}
		}
		return resource.delete();
	}
}
