package com.filenet.cpe.tools.cpetool.managers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Iterator;
import javax.security.auth.Subject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.apache.commons.codec.binary.Base64;

//Apache Commons File Utils
/*import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;*/

//Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;
import com.filenet.cpe.tools.cpetool.CpetoolApplication;
import com.filenet.cpe.tools.cpetool.configuration.AppConfiguration;
import com.filenet.api.collection.ClassDefinitionSet;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.DocumentSet;
import com.filenet.api.collection.FolderSet;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.collection.PropertyDefinitionList;
import com.filenet.api.collection.ReferentialContainmentRelationshipSet;

import com.filenet.wcm.api.ObjectFactory;
import com.filenet.wcm.api.Session;

/*import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.io.FileChannelRandomAccessSource;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.BmpImage;
import com.itextpdf.text.pdf.codec.GifImage;
import com.itextpdf.text.pdf.codec.PngImage;
import com.itextpdf.text.pdf.codec.TiffImage;*/

@Service("im")
public class ImagingManager {
	
	@Autowired
	private WorkflowManager workflowManager;
	
	@Autowired
	private AppConfiguration appConfig;
	
	private static Logger log = LoggerFactory.getLogger(ImagingManager.class);
	
			
	//******************************************************
	//Public Main Methods
	//******************************************************
	public String connectionTest()
	{
		String result = "";
		//Get an ObjectStore Object
		ObjectStore os = null;
		
		try
		{
			log.info("Entered ImagingManager -> connectionTest()");
			//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
			
			//Login to the Imaging Server
			os = loginImaging();
						
			if (os != null)
			{
				log.info("Logged in successfully to the Imaging Server");
				result = "Imaging Login SUCCESSFUL";
			}
			else
			{
				log.info("Failed to login to the Imaging Server");
				result = "Imaging Login FAILED";
			}
			//Reset Object Store variable to kill the connection
			os = null;
		}
		catch(Exception e)
		{
			//log.error("Stack Trace", e.getStackTrace());
			log.error("Error Message", e.getMessage());
			result = "Imaging Login FAILED";
			//Reset Object Store variable to kill the connection
			os = null;
		}
		log.info("Leaving ImagingManager -> connectionTest()");
		log.info("===========================================================");
		return result;
	}
	
	private ObjectStore loginImaging()
	{
		//Create a ObjectStore Object
		ObjectStore os = null;
		
		try
		{
			log.info("Entered ImagingManager -> loginImaging()");
			//Login to the Imaging Server
			log.info("Logging into the Imaging Server");
			
			//CE Connection
			Connection conn = null;
			//Get the CE Connection
			conn = getCEConnection(appConfig.getCpeUsername(), appConfig.getCpePassword(), appConfig.getStanzaName(), appConfig.getCpeConnectionURI());
			if (conn != null)
			{
				log.info("Imaging Connection Successful");
				//Get a CE Domain Object
				Domain dom = null;
				dom = getDomain(conn, appConfig.getP8Domain());
				
				if (dom != null)
				{
					log.info("Imaging Domain Connection Successful");
					//Get the Object Store
					os = getObjectStore(dom, appConfig.getObjectstoreName());
				}
			}
		}
		catch(Exception e)
		{
			//log.error("Stack Trace", e.getStackTrace());
			log.error("Error Message", e.getMessage());
		}
		log.info("Leaving ImagingManager -> loginImaging()");
		log.info("===========================================================");
		return os;
	}
	
	private static Connection getCEConnection(String username, String password, String stanza, String uri) {
		//Make a connection
		Connection conn = Factory.Connection.getConnection(uri);
		Subject subject = null;
		subject = UserContext.createSubject(conn, username, password, stanza);
		UserContext.get().pushSubject(subject);
		return conn;
	}
	
	private static Domain getDomain(Connection conn, String p8Domain) {
		//Get the CPE Domain Object
		Domain dom = Factory.Domain.fetchInstance(conn, p8Domain, null);
		return dom;
	}
	
	private static ObjectStore getObjectStore(Domain domain, String objectstore) {
		//Get the CPE ObjectStore Object
		ObjectStore os = Factory.ObjectStore.fetchInstance(domain, objectstore, null);
		return os;
	}
	
	//@SuppressWarnings("rawtypes")
//	private String getFNDocumentGUID(ObjectStore os, String docID, WIISCLog wiiscLog)
//		throws Exception
//	{
//		//FileNet Document GUID
//		String fnDocGUID = "";
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getFNDocumentGUID()");
//			wiiscLog.log(wiiscLog.INFO, "Getting the FileNet Document GUID for " + docID);
//			
//			//Verify Object Store Connected
//			if (os != null)
//			{
//				//Search CE for Documents Matching Criteria
//				//SearchScope
//				SearchScope searchScope = new SearchScope(os);
//				//SearchSQL
//				SearchSQL searchSQL = new SearchSQL();
//							
//				//Build Query from Doc Props
//				String sqlDocProps = "";
//				//SQL Query
//				String sql = "";
//				
//				//Initialize the Property Key Counter
//				int propCount = 1;
//				
//				wiiscLog.log(wiiscLog.INFO, "Building the SQL Query from System Properties");
//				//Using Global Props for Doc Title and ID...to use Version Series change while to <= 3
//				//and update the Global Props
//				while (propCount <= 2)
//				{
//					sqlDocProps = sqlDocProps + "d." + globalConfig.getString("prop" + propCount) + ", ";
//					//Increment the Property Key Counter
//					propCount++;
//				}
//								
//				//Check to see if any SQL Doc Properties were included
//				if (sqlDocProps.length() > 0)
//				{
//					//Remove the last comma and space from the String
//					sqlDocProps = sqlDocProps.substring(0, sqlDocProps.length()-2);
//				}
//				
//				//Check if the Doc ID value is a GUID or a Normal Number value
//				if (docID.substring(0,1).equals("{") && docID.substring(docID.length()-1).equals("}"))
//				{
//					wiiscLog.log(wiiscLog.INFO, "GUID was passed in");
//					//SQL Query for a Document in FileNet using a GUID
//					sql = "select d.this, " + sqlDocProps + " from Document d" + 
//						" where d.ID = " + docID;
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "NON-GUID was passed in");
//					//SQL Query for Documents in FileNet
//					sql = "select d.this, " + sqlDocProps + " from Document d" + 
//						" where " + "d." + globalConfig.getString("whereProperty") + " = " + docID;
//				}
//								
//				//Output the SQL Doc Props Query to the Log
//				wiiscLog.log(wiiscLog.INFO, "SQL Select: " + sqlDocProps);
//												
//				//Output Full Query
//				wiiscLog.log(wiiscLog.INFO, "SQL Query: " + sql);
//				
//				//Set Max Records to Process to 500
//				//searchSQL.setMaxRecords(500);
//				
//				//Set the SQL query
//				searchSQL.setQueryString(sql);
//				
//				//Independent Object Set
//				IndependentObjectSet objectSet = searchScope.fetchObjects(searchSQL, null, null, null);
//				
//				//Check to see if any Documents were Found
//				if (!objectSet.isEmpty())
//				{
//					Iterator iter = objectSet.iterator();
//					int i = 0;
//					
//					while (iter.hasNext())
//					{
//						//String tempDocPropList = "";
//						Document document = (Document) iter.next();
//						//Update Property Cache to include Custom Doc Properties
//						document.refresh();
//						Properties props;
//						props = document.getProperties();
//						
//						//Initialize the Property Key Counter
//						propCount = 1;
//						
//						wiiscLog.log(wiiscLog.INFO, "==============================================");
//						wiiscLog.log(wiiscLog.INFO, "Get Document System Properties");
//						
//						//Get Document System Properties
//						//Using Global Props for Doc Title and ID...to use Version Series change while to <= 3
//						//and update the Global Props
//						while (propCount <= 2)
//						{
//							String propName = globalConfig.getString("prop" + propCount);
//							
//							if (props.isPropertyPresent(propName))
//							{
//								wiiscLog.log(wiiscLog.INFO, "Property: " + propName);
//								
//								if (propName.equals("ID"))
//								{
//									//Save the FileNet GUID for ID
//									if (globalConfig.getString("viewIDtype").equals("id"))
//									{
//										fnDocGUID = props.getIdValue(propName).toString();
//									}
//									wiiscLog.log(wiiscLog.INFO, "Property Value: " + props.getIdValue(propName).toString());
//									wiiscLog.log(wiiscLog.INFO, "==============================================");
//								}
//								else if (propName.equals("VersionSeries"))
//								{
//									//Save the FileNet GUID for VersionSeries
//									if (globalConfig.getString("viewIDtype").equals("vsId"))
//									{
//										fnDocGUID = props.getIdValue(propName).toString();
//									}
//									wiiscLog.log(wiiscLog.INFO, "Property Value: " + props.getIdValue(propName).toString());
//									wiiscLog.log(wiiscLog.INFO, "==============================================");
//								}
//								else
//								{
//									wiiscLog.log(wiiscLog.INFO, "Property Value: " + props.getStringValue(propName));
//									wiiscLog.log(wiiscLog.INFO, "==============================================");
//								}
//							}
//							
//							//Increment the Property Key Counter
//							propCount++;
//						}
//						
//						//Decrement propCount since it started with an initial value of 1
//						propCount--;
//						
//						wiiscLog.log(wiiscLog.INFO, "Total Properties: " + propCount);
//						wiiscLog.log(wiiscLog.INFO, "==============================================");
//						//Increment Document Counter
//						i++;
//					}
//					//Output total documents found			
//					wiiscLog.log(wiiscLog.INFO, "Total Documents: " + i);
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "No Documents Found");
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Object Store Failed to Connect");		
//			}
//		}
//		catch(Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - getFNDocumentGUID");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Reset GUID
//			fnDocGUID = "";
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getFNDocumentGUID()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnDocGUID;
//	}
//	
//	private FnDocument getDocument(Document document, String[] propertiesData, WIISCLog wiiscLog)
//	{
//		//Create the FnDocument Object
//		FnDocument fnDocument = new FnDocument();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getDocument()");
//						
//			if (document != null)
//			{
//				//Get 1 FnDocument's Properties
//				FnPropertyList fnPropertyListSource = getDocumentProperties(document, propertiesData, wiiscLog);
//								
//				//Check the size of fnPropertyListSource
//				if (fnPropertyListSource.getCount() > 0)
//				{
//					//Add the FnPropertyList to the FnDocument
//					fnDocument.setFnPropertyList(fnPropertyListSource);
//					
//					//Update the Document Class, ID and Name
//					wiiscLog.log(wiiscLog.INFO, "Document Class is " + document.getClassName());
//					fnDocument.setFnDocumentClass(document.getClassName());
//					wiiscLog.log(wiiscLog.INFO, "Document ID is " + document.get_Id().toString());
//					fnDocument.setFnDocumentID(document.get_Id().toString());
//					wiiscLog.log(wiiscLog.INFO, "Document Name is " + document.get_Name());
//					fnDocument.setFnDocumentName(document.get_Name());
//					
//					//Generate the Document URL
//					//Check ViewDoc Host
//					if (globalConfig.getString("viewDocHost").length() == 0)
//					{
//						String aeServerName = "";
//						aeServerName = globalConfig.getString("aeServerName");
//						String aeApplicationPort = "";
//						aeApplicationPort = globalConfig.getString("aeApplicationPort"); 
//						
//						//Check if the View Doc and Properties URL will be used
//						if (globalConfig.getString("viewDocAndPropertiesEnabled").equals("true"))
//						{
//							//View Document and Properties
//							fnDocument.setFnDocumentURL(globalConfig.getString("httpHeader") + "://" + aeServerName + ":" + aeApplicationPort + globalConfig.getString("wiiscApplicationContext") + globalConfig.getString("viewDocAndPropertiesURL") + document.get_Id().toString());
//						}
//						else
//						{
//							//View Document ONLY
//							fnDocument.setFnDocumentURL(globalConfig.getString("httpHeader") + "://" + aeServerName + ":" + aeApplicationPort + globalConfig.getString("wiiscApplicationContext") + globalConfig.getString("viewDocURL") + document.get_Id().toString());
//						}
//					}
//					else
//					{
//						//Check if the View Doc and Properties URL will be used
//						if (globalConfig.getString("viewDocAndPropertiesEnabled").equals("true"))
//						{
//							//View Document and Properties
//							fnDocument.setFnDocumentURL(globalConfig.getString("httpHeader") + "://" + globalConfig.getString("viewDocHost") + ":" + globalConfig.getString("wiiscApplicationPort") + globalConfig.getString("wiiscApplicationContext") + globalConfig.getString("viewDocAndPropertiesURL") + document.get_Id().toString());
//						}
//						else
//						{
//							//View Document ONLY
//							fnDocument.setFnDocumentURL(globalConfig.getString("httpHeader") + "://" + globalConfig.getString("viewDocHost") + ":" + globalConfig.getString("wiiscApplicationPort") + globalConfig.getString("wiiscApplicationContext") + globalConfig.getString("viewDocURL") + document.get_Id().toString());
//						}
//					}
//				}
//			}//End If
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR,"ERROR getDocument()");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnDocument.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnDocument.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getDocument()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnDocument;
//	}
//	
//	/*private FnDocument getDocument(Document document, FnDocument fnDocumentSource, String docPropValue, WIISCLog wiiscLog)
//	{
//		//Create the FnDocument Object
//		FnDocument fnDocument = new FnDocument();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getDocument()");
//						
//			if (document != null)
//			{
//				//Get 1 FnDocument's Properties
//				FnPropertyList fnPropertyListSource = fnDocumentSource.getFnPropertyList();
//				List<FnProperty> fnProperties = fnPropertyListSource.getFnDocumentPropsList();
//				//Update Property Cache to include Custom Doc Properties
//				document.refresh();
//				//Get Doc Properties from FileNet
//				Properties docProperties = document.getProperties();
//				//Create the FnPropertyList for the FnDocument
//				FnPropertyList fnPropertyList = new FnPropertyList();
//				
//				//Check the size of fnPropertyListSource
//				if (fnPropertyListSource.getCount() > 0)
//				{
//					wiiscLog.log(wiiscLog.INFO, "===========================================================");
//					wiiscLog.log(wiiscLog.INFO, "Get Document Properties");
//
//					for (FnProperty oneFnProp : fnProperties)
//					{
//						wiiscLog.log(wiiscLog.INFO, "Property: " + oneFnProp.getName());
//						if (docProperties.isPropertyPresent(oneFnProp.getName()))
//						{
//							wiiscLog.log(wiiscLog.INFO, "Property EXISTS");
//							//Get the Document's Property Value and Save to FnProperty
//							Object value = null;
//							value = docProperties.getObjectValue(oneFnProp.getName());
//							
//							if (value != null)
//							{
//								oneFnProp.setValue(value.toString());
//								//wiiscLog.log(wiiscLog.INFO, "Value: " + oneFnProp.getValue());
//								//Document ID
//								if (oneFnProp.getName().equals("ID"))
//								{
//									//Set the Document ID for the FnDocument
//									fnDocument.setFnDocumentID(oneFnProp.getValue());
//									//Set the Document URL for the FnDocument
//									//Check ViewDoc Host
//									if (globalConfig.getString("viewDocHost").length() == 0)
//									{
//										fnDocument.setFnDocumentURL(globalConfig.getString("httpHeader") + "://" + globalConfig.getString("aeServerName") + ":" + globalConfig.getString("aeApplicationPort") + globalConfig.getString("wiiscApplicationContext") +"/Imaging/ViewDocumentAndProperties?docId=" + oneFnProp.getValue());
//									}
//									else
//									{
//										fnDocument.setFnDocumentURL(globalConfig.getString("httpHeader") + "://" + globalConfig.getString("viewDocHost") + ":" + globalConfig.getString("wiiscApplicationPort") + globalConfig.getString("wiiscApplicationContext") +"/Imaging/ViewDocumentAndProperties?docId=" + oneFnProp.getValue());
//									}
//								}
//							}
//							else
//							{
//								//Value is NULL so set to ""
//								oneFnProp.setValue("");
//							}
//							wiiscLog.log(wiiscLog.INFO, "Value: " + oneFnProp.getValue());
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "Property DOES NOT EXIST");
//						}
//						//Add the FnProperty to the FnPropertyList
//						fnPropertyList.addFnProperty(oneFnProp);
//					}
//				}
//				else
//				{
//					//Only get the ID and the Document URL
//					wiiscLog.log(wiiscLog.INFO, "===========================================================");
//					wiiscLog.log(wiiscLog.INFO, "Only Retrieving the ID and the URL");
//					
//					//Create a Property
//					FnProperty oneFnProp = new FnProperty();
//					oneFnProp.setName("ID");
//					wiiscLog.log(wiiscLog.INFO, "Property: " + oneFnProp.getName());
//					
//					//Check if this properties exists in the Document Property Cache
//					if (docProperties.isPropertyPresent(oneFnProp.getName()))
//					{
//						wiiscLog.log(wiiscLog.INFO, "Property EXISTS");
//						//Get the Document's Property Value and Save to FnProperty
//						Object value = null;
//						value = docProperties.getObjectValue(oneFnProp.getName());
//						if (value != null)
//						{
//							oneFnProp.setValue(value.toString());
//							//Document ID
//							//Check ViewDoc Host
//							if (globalConfig.getString("viewDocHost").length() == 0)
//							{
//								fnDocument.setFnDocumentURL(globalConfig.getString("httpHeader") + "://" + globalConfig.getString("aeServerName") + ":" + globalConfig.getString("aeApplicationPort") + globalConfig.getString("wiiscApplicationContext") +"/Imaging/ViewDocumentAndProperties?docId=" + oneFnProp.getValue());
//							}
//							else
//							{
//								fnDocument.setFnDocumentURL(globalConfig.getString("httpHeader") + "://" + globalConfig.getString("viewDocHost") + ":" + globalConfig.getString("wiiscApplicationPort") + globalConfig.getString("wiiscApplicationContext") +"/Imaging/ViewDocumentAndProperties?docId=" + oneFnProp.getValue());
//							}
//							wiiscLog.log(wiiscLog.INFO, "URL: " + fnDocument.getFnDocumentURL());
//						}
//						else
//						{
//							//Value is NULL so set to ""
//							oneFnProp.setValue("");
//						}
//						wiiscLog.log(wiiscLog.INFO, "Value: " + oneFnProp.getValue());
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "Property DOES NOT EXIST");
//					}
//					//Add the FnProperty to the FnPropertyList
//					fnPropertyList.addFnProperty(oneFnProp);
//				}
//				
//				//Add the FnPropertyList to the FnDocument
//				fnDocument.setFnPropertyList(fnPropertyList);
//				
//				//Update the Document Class, ID and Name
//				wiiscLog.log(wiiscLog.INFO, "Document Class is " + document.getClassName());
//				fnDocument.setFnDocumentClass(document.getClassName());
//				wiiscLog.log(wiiscLog.INFO, "Document ID is " + document.get_Id().toString());
//				fnDocument.setFnDocumentID(document.get_Id().toString());
//				wiiscLog.log(wiiscLog.INFO, "Document Name is " + document.get_Name());
//				fnDocument.setFnDocumentName(document.get_Name());
//				
//			}//End If
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR,"ERROR getDocument()");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getDocument()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnDocument;
//	}*/
//	
//	public FnBatchList getBatchList(String batchClass, String batchName, WIISCLog wiiscLog)
//	{
//		//List of FileNet Batches
//		FnBatchList fnBatchList = new FnBatchList();
//		//Get an ObjectStore Object
//		ObjectStore os = null;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getBatchList()");
//
//			//Login to the Imaging Server
//			os = loginImaging(wiiscLog);
//
//			//Verify Object Store Connected
//			if (os != null)
//			{
//				//Update errorFlag
//				fnBatchList.setErrorFlag(0);
//				//Update the ErrorMessage
//				fnBatchList.setErrorMessage("");
//				
//				wiiscLog.log(wiiscLog.INFO, "Batch Class: " + batchClass);
//				wiiscLog.log(wiiscLog.INFO, "Batch Name: " + batchName);
//				
//				//Check batchClass and batchName
//				if (batchClass.length() == 0 && batchName.length() == 0)
//				{
//					//No Batch Class and No Batch Name
//					//Get initial list of Batches - all classes all batch names
//					wiiscLog.log(wiiscLog.INFO, "Retrieving list of ALL Batches");
//					//Get the FnBatchList
//					fnBatchList = getBatches(os, batchClass, batchName, wiiscLog);
//				}
//				else if (batchClass.length() > 0 && batchName.length() > 0)
//				{
//					//Batch Class and Batch Name
//					//Check if the Batch Class is valid
//					if (checkDocClassExists(os, batchClass, wiiscLog))
//					{
//						//Get a Single Batch by Batch Class and Batch Name
//						wiiscLog.log(wiiscLog.INFO, "Retrieving the " + batchName + " " + batchClass + " Batch");
//						FnBatch fnBatch = new FnBatch();
//						fnBatch = getBatch(os, batchClass, batchName, wiiscLog);
//						//Check if any Documents were Found - is the batch valid
//						if (fnBatch.getCount() > 0)
//						{
//							//Add the fnBatch to the fnBatchList
//							fnBatchList.addFnBatch(fnBatch);
//							//Output the total documents in the Batch
//							wiiscLog.log(wiiscLog.INFO, "Total Documents in the Batch to View: " + fnBatch.getCount());
//						}
//					}
//					else
//					{
//						//Batch Class is not a valid Document Class
//						wiiscLog.log(wiiscLog.ERROR, batchClass + " is not a valid Batch Class");
//						//Update errorFlag
//						fnBatchList.setErrorFlag(1);
//						//Update the ErrorMessage
//						fnBatchList.setErrorMessage(batchClass + " is not a valid Batch Class");
//					}
//				}
//				else if (batchClass.length() == 0 && batchName.length() > 0)
//				{
//					//No Batch Class and have Batch Name
//					//Get list of Batches for specific Batch Class
//					wiiscLog.log(wiiscLog.INFO, "Retrieving the " + batchName + " Batch");
//					FnBatch fnBatch = new FnBatch();
//					fnBatch = getBatch(os, batchClass, batchName, wiiscLog);
//					//Check if any Documents were Found - is the batch valid
//					if (fnBatch.getCount() > 0)
//					{
//						//Add the fnBatch to the fnBatchList
//						fnBatchList.addFnBatch(fnBatch);
//						//Output the total documents in the Batch
//						wiiscLog.log(wiiscLog.INFO, "Total Documents in the Batch to View: " + fnBatch.getCount());
//					}
//				}
//				else if (batchClass.length() > 0 && batchName.length() == 0)
//				{
//					//Batch Class and No Batch Name
//					//Check if the Batch Class is valid
//					if (checkDocClassExists(os, batchClass, wiiscLog))
//					{
//						//Get list of Batches - specific class
//						wiiscLog.log(wiiscLog.INFO, "Retrieving list of " + batchClass + " Batches");
//						//Get the FnBatchList
//						fnBatchList = getBatches(os, batchClass, batchName, wiiscLog);
//					}
//					else
//					{
//						//Batch Class is not a valid Document Class
//						wiiscLog.log(wiiscLog.ERROR, batchClass + " is not a valid Batch Class");
//						//Update errorFlag
//						fnBatchList.setErrorFlag(1);
//						//Update the ErrorMessage
//						fnBatchList.setErrorMessage(batchClass + " is not a valid Batch Class");
//					}
//				}
//				else
//				{
//					//Batch Class or Batch Name was NULL
//					//Batch Class or Batch Name was empty - only allow both to be empty or both to be non-empty
//					wiiscLog.log(wiiscLog.ERROR, "Batch Class or Batch Name value was NULL");
//					//Update errorFlag
//					fnBatchList.setErrorFlag(1);
//					//Update the ErrorMessage
//					fnBatchList.setErrorMessage("Batch Class or Batch Name value was NULL");
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Imaging Login FAILED, Imaging Server may be unavailable.");
//				//Update the fnBatchList Object
//				fnBatchList.setErrorFlag(1);
//				//Update the ErrorMessage
//				fnBatchList.setErrorMessage("Imaging Login FAILED, Imaging Server may be unavailable.");
//			}
//
//			//Check the Batch List Size
//			if (fnBatchList.getCount() > 0)
//			{
//				//Output total batches stored in the Batch List
//				wiiscLog.log(wiiscLog.INFO, "Total Batches in the Batch List to View: " + fnBatchList.getCount());
//				wiiscLog.log(wiiscLog.INFO, "===========================================================");
//			}
//			else
//			{
//				//Output no batches stored in the Batch List
//				wiiscLog.log(wiiscLog.INFO, "No Batches in the Batch List to View");
//				wiiscLog.log(wiiscLog.INFO, "===========================================================");
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnBatchList.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnBatchList.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getBatchList()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnBatchList;
//	}
//	
//	private String truncateName(String source, int size)
//	{
//		String sOutput = "";
//		
//		sOutput = source.substring(0, size - 1);
//		
//		return sOutput;
//	}
//	
//	private String getCapitalLettersName(String sSource)
//	{
//		String sOutput = "";
//		for (int i = 0; i < sSource.length(); i++)
//		{
//			if (Character.isUpperCase(sSource.charAt(i)))
//			{
//				char w = sSource.charAt(i);
//				sOutput = sOutput + w;
//			}
//		}
//		return sOutput;
//	}
//	
//	public FnDocumentList getDocumentList(String docClassName, String docPropValue, WIISCLog wiiscLog)
//	{
//		//List of FileNet Documents
//		FnDocumentList fnDocumentList = new FnDocumentList();
//		//Get an ObjectStore Object
//		ObjectStore os = null;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getDocumentList()");
//
//			//Login to the Imaging Server
//			os = loginImaging(wiiscLog);
//
//			//Verify Object Store Connected
//			if (os != null)
//			{
//				//List of FileNet Document Classes
//				FnDocClassList fnDocClassList = new FnDocClassList();
//
//				//Check for Document Class Name
//				if (docClassName.length() > 0)
//				{
//					wiiscLog.log(wiiscLog.INFO, "Document Class: " + docClassName);
//					wiiscLog.log(wiiscLog.INFO, "Document Property Value: " + docClassName);
//					//Get the FnDocumentList
//					fnDocumentList = getDocuments(os, docClassName, docPropValue, wiiscLog);			
//				}
//				else
//				{
//					//Get a List of Custom Doc Classes
//					fnDocClassList = getCustomDocClasses(os, wiiscLog);
//
//					//for (int i = 0; i < fnDocClassList.getFnDocClassList().size(); i++)
//					for (FnDocClass onefnDocClass : fnDocClassList.getFnDocClassList())
//					{
//						//Check for Custom Doc Class Config Properties File
//						if (checkPropertiesFileExist(onefnDocClass.getName() + "Config", wiiscLog))
//						{
//							//Update the Document Class
//							docClassName = onefnDocClass.getName();
//							wiiscLog.log(wiiscLog.INFO, "Document Class: " + docClassName);
//							wiiscLog.log(wiiscLog.INFO, "Document Property Value: " + docClassName);
//							//Create an FnDocumentList
//							FnDocumentList oneFnDocumentList = new FnDocumentList();
//							//Get the FnDocumentList
//							oneFnDocumentList = getDocuments(os, docClassName, docPropValue, wiiscLog);
//							wiiscLog.log(wiiscLog.INFO, docClassName + " Documents Count: " + oneFnDocumentList.getCount() + " " + docClassName + " Documents List Total: " + oneFnDocumentList.getFnDocumentList().size());
//							//Add oneFnDocumentList to the FnDocumentList
//							fnDocumentList.addFnDocumentList(oneFnDocumentList);
//							//fnDocumentList.getFnDocList().addAll(oneFnDocumentList.getFnDocList());
//							wiiscLog.log(wiiscLog.INFO, "Documents Count: " + fnDocumentList.getCount() + " Documents List Total: " + fnDocumentList.getFnDocumentList().size());
//							wiiscLog.log(wiiscLog.INFO, "===========================================================");
//						}
//					}
//				}
//				//Update errorFlag
//				fnDocumentList.setErrorFlag(0);
//				//Update the ErrorMessage
//				fnDocumentList.setErrorMessage("");
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Imaging Login FAILED, Imaging Server may be unavailable.");
//				//Update the fnDocumentList Object
//				fnDocumentList.setErrorFlag(1);
//				//Update the ErrorMessage
//				fnDocumentList.setErrorMessage("Imaging Login FAILED, Imaging Server may be unavailable.");
//			}
//			//Reset Object Store variable
//			os = null;
//
//			//Verify the Doc List is not empty
//			if (fnDocumentList.getCount() > 0)
//			{
//				//Output total documents stored in the Doc List
//				wiiscLog.log(wiiscLog.INFO, "Total Documents in the Document List to View: " + fnDocumentList.getCount());
//
//				//Update the errorFlag to 0 to avoid it being set as an Error anywhere else.
//				fnDocumentList.setErrorFlag(0);
//				//Update the ErrorMessage
//				fnDocumentList.setErrorMessage("");
//			}
//			else
//			{
//				//Output total documents stored in the Doc List
//				wiiscLog.log(wiiscLog.INFO, "There are NO Documents in the Document List to View");
//				//This is for 0 documents returned and is not an Error.
//				//Update the errorFlag to 0 to avoid it being set as an Error anywhere else.
//				//fnDocumentList.setErrorFlag(0);
//				//Update the ErrorMessage
//				//fnDocumentList.setErrorMessage("");
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnDocumentList.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnDocumentList.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getDocumentList()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnDocumentList;
//	}
//	
//	//Output the FnDocumentList
//	private void outputFnDocumentList(FnDocumentList fnDocumentList, WIISCLog wiiscLog)
//	{
//		wiiscLog.log(wiiscLog.INFO, "outputFnDocumentList");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		for (int i = 0; i < fnDocumentList.getFnDocumentList().size(); i++)
//		{
//			FnDocument fnDocument = new FnDocument();
//			fnDocument = fnDocumentList.getFnDocumentList().get(i);
//			//Call outputFnDocument
//			outputFnDocument(fnDocument, wiiscLog);
//		}
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//	}
//	
//	//Output the FnDocument
//	private void outputFnDocument(FnDocument fnDocument, WIISCLog wiiscLog)
//	{
//		wiiscLog.log(wiiscLog.INFO, "App ID: " + fnDocument.getFnDocumentAppId());
//		wiiscLog.log(wiiscLog.INFO, "Document Class: " + fnDocument.getFnDocumentClass());
//		wiiscLog.log(wiiscLog.INFO, "Document ID: " + fnDocument.getFnDocumentID());
//		wiiscLog.log(wiiscLog.INFO, "Document Name: " + fnDocument.getFnDocumentName());
//		wiiscLog.log(wiiscLog.INFO, "Document Status: " + fnDocument.getFnDocumentStatus());
//		FnPropertyList fnPropertyList = new FnPropertyList();
//		fnPropertyList = fnDocument.getFnPropertyList();
//		//Call outputFnPropertyList
//		outputFnPropertyList(fnPropertyList, wiiscLog);
//	}
//	
//	//Output the FnPropertyList
//	private void outputFnPropertyList(FnPropertyList fnPropertyList, WIISCLog wiiscLog)
//	{
//		//wiiscLog.log(wiiscLog.INFO, "outputFnPropertyList");
//		//wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		for (int i = 0; i < fnPropertyList.getFnDocumentPropsList().size(); i++)
//		{
//			FnProperty fnProperty = new FnProperty();
//			fnProperty = fnPropertyList.getFnDocumentPropsList().get(i);
//			wiiscLog.log(wiiscLog.INFO, "Property: " + fnProperty.getName());
//			wiiscLog.log(wiiscLog.INFO, "Property Value: " + fnProperty.getValue());
//		}
//		//wiiscLog.log(wiiscLog.INFO, "===========================================================");
//	}
//	
//	private FnDocumentList searchAndDeleteDocuments(ObjectStore os, FnDocument fnDocumentRequest, WIISCLog wiiscLog)
//	{
//		//List of FileNet Documents
//		FnDocumentList fnDocumentList = new FnDocumentList();
//		//Document Class
//		String docClassName = "";
//		docClassName = fnDocumentRequest.getFnDocumentClass();
//		//Application ID - <DocumentAppID> used for Document Searches and where Property of Document Class Props file
//		String applicationID = "";
//		applicationID = fnDocumentRequest.getFnDocumentAppId();
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> searchAndDeleteDocuments()");
//
//			//Verify Object Store Connected
//			if (os != null)
//			{
//				//Check if the Doc Class was sent
//				if (docClassName.length() > 0)
//				{
//					//Check if the Application ID was sent
//					if (applicationID.length() > 0)
//					{
//						//Create the FnPropertyList
//						//FnPropertyList customFnPropertyList = getDocumentProperties(os, docClassName, wiiscLog);
//						
//						//Create the FnPropertyList
//						FnPropertyList fnPropertyList = new FnPropertyList();
//										
//						//Get the Document Properties Template to use
//						String[] propertiesData = null;
//						propertiesData = getDocumentPropertiesTemplate(os, docClassName, wiiscLog);
//						
//						//Build FnPropertyList for the Query
//						for (String prop : propertiesData)
//						{
//							FnProperty fnProp = new FnProperty();
//							fnProp.setName(prop);
//							//Add Property
//							fnPropertyList.addFnProperty(fnProp);
//						}
//						
//						//Create the ObjectStore Search
//						String sql = getObjectStoreQuery(fnPropertyList, docClassName, applicationID, wiiscLog);
//
//						//Verify SQL String has a value
//						if (sql.length() > 0)
//						{
//							//Search for Documents and get the Results
//							IndependentObjectSet objectSet = getObjectStoreQueryResults(os, sql, wiiscLog);
//
//							//Check to see if any Documents were Found
//							if (!objectSet.isEmpty())
//							{
//								//Loop through Documents
//								Iterator iter = objectSet.iterator();
//
//								while (iter.hasNext())
//								{
//									//wiiscLog.log(wiiscLog.INFO, "Found a Document");
//									//String tempDocPropList = "";
//									Document document = (Document) iter.next();
//									
//									//Verify the Document was found and perform Indexing
//									if (document != null)
//									{
//										wiiscLog.log(wiiscLog.INFO, "Document Found");
//										FnDocument fnDocument = new FnDocument();
//										//Process 1 FileNet Document to Delete the FileNet Document
//										if (deleteDocument(document, wiiscLog))
//										{
//											//Document Deleted Successfully
//											fnDocument.setErrorFlag(0);
//										}
//										else
//										{
//											//Document Failed to Delete
//											fnDocument.setErrorFlag(1);
//										}
//										//Check if the Error Flag was set
//										if (fnDocument.getErrorFlag() > 0)
//										{
//											//Process Error
//											wiiscLog.log(wiiscLog.INFO, "Document failed to delete in the Object Store");
//											wiiscLog.log(wiiscLog.INFO, "===========================================================");
//											//Update the Document Status
//											fnDocument.setFnDocumentStatus("Document failed to delete in the Object Store");
//											fnDocument.setErrorFlag(1);
//											fnDocument.setErrorMessage("Document failed to delete in the Object Store");
//										}
//										else
//										{
//											//No Error
//											wiiscLog.log(wiiscLog.INFO, "Document successfully deleted in the Object Store");
//											wiiscLog.log(wiiscLog.INFO, "===========================================================");
//											//Update the Document Status
//											fnDocument.setFnDocumentStatus("Document successfully deleted in the Object Store");
//											fnDocument.setErrorFlag(0);
//											fnDocument.setErrorMessage("Document successfully deleted in the Object Store");
//										}
//										//Add the FnDocument to the FnDocumentList
//										fnDocumentList.addFnDocument(fnDocument);
//									}
//									else
//									{
//										wiiscLog.log(wiiscLog.INFO, "Document was NOT Found");
//									}
//								}//End While
//							}
//							else
//							{
//								wiiscLog.log(wiiscLog.INFO, "No Documents Found for the Query");
//							}
//							
//							//Verify the Doc List is not empty
//							if (fnDocumentList.getCount() > 0)
//							{
//								//Output total documents deleted in the Doc List
//								wiiscLog.log(wiiscLog.INFO, "Total Documents Deleted: " + fnDocumentList.getCount());
//								wiiscLog.log(wiiscLog.INFO, "===========================================================");
//								//Update the errorFlag to 0 to avoid it being set as an Error anywhere else.
//								fnDocumentList.setErrorFlag(0);
//								//Update the ErrorMessage
//								fnDocumentList.setErrorMessage("");
//							}
//							else
//							{
//								wiiscLog.log(wiiscLog.INFO, "No Documents Deleted");
//								//Update the errorFlag to 0 because no Documents existed and this is not an Error
//								fnDocumentList.setErrorFlag(0);
//								//Update the ErrorMessage
//								fnDocumentList.setErrorMessage("No Documents Deleted");
//							}					
//						}
//						else
//						{
//							//SQL Query was empty
//							wiiscLog.log(wiiscLog.INFO, "SQL Query for the Documents was invalid");
//							//Update errorFlag to 1 because SQL Query was invalid
//							fnDocumentList.setErrorFlag(1);
//							//Update the ErrorMessage
//							fnDocumentList.setErrorMessage("SQL Query for the Documents was invalid");
//						}
//					}
//					else
//					{
//						//Application ID was empty
//						wiiscLog.log(wiiscLog.INFO, "Application ID was missing from the Request");
//						//Update errorFlag to 1
//						fnDocumentList.setErrorFlag(1);
//						//Update the ErrorMessage
//						fnDocumentList.setErrorMessage("Application ID was missing from the Request");
//					}
//				}
//				else
//				{
//					//Doc Class was empty
//					wiiscLog.log(wiiscLog.INFO, "Document Class was missing from the Request");
//					//Update errorFlag to 1
//					fnDocumentList.setErrorFlag(1);
//					//Update the ErrorMessage
//					fnDocumentList.setErrorMessage("Document Class was missing from the Request");
//				}
//			}//End If
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnDocumentList.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnDocumentList.setErrorMessage(e.getMessage());
//			//Reset Object Store variable
//			//os = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> searchAndDeleteDocuments()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnDocumentList;
//	}
//	
//	private FnDocumentList searchAndUpdateDocuments(ObjectStore os, FnDocument fnDocumentRequest, WIISCLog wiiscLog)
//	{
//		//List of FileNet Documents
//		FnDocumentList fnDocumentList = new FnDocumentList();
//		//Document Class
//		String docClassName = "";
//		docClassName = fnDocumentRequest.getFnDocumentClass();
//		//Application ID - <DocumentAppID> used for Document Searches and where Property of Document Class Props file
//		String applicationID = "";
//		applicationID = fnDocumentRequest.getFnDocumentAppId();
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> searchAndUpdateDocuments()");
//
//			//Verify Object Store Connected
//			if (os != null)
//			{
//				//Check if the Doc Class was sent
//				if (docClassName.length() > 0)
//				{
//					//Check if the Application ID was sent
//					if (applicationID.length() > 0)
//					{
//						//Create the FnPropertyList
//						//FnPropertyList customFnPropertyList = getDocumentProperties(os, docClassName, wiiscLog);
//						
//						//Create the FnPropertyList
//						FnPropertyList fnPropertyList = new FnPropertyList();
//										
//						//Get the Document Properties Template to use
//						String[] propertiesData = null;
//						propertiesData = getDocumentPropertiesTemplate(os, docClassName, wiiscLog);
//						
//						//Build FnPropertyList for the Query
//						for (String prop : propertiesData)
//						{
//							FnProperty fnProp = new FnProperty();
//							fnProp.setName(prop);
//							//Add Property
//							fnPropertyList.addFnProperty(fnProp);
//						}
//						
//						//Create the ObjectStore Search
//						String sql = getObjectStoreQuery(fnPropertyList, docClassName, applicationID, wiiscLog);
//
//						//Verify SQL String has a value
//						if (sql.length() > 0)
//						{
//							//Search for Documents and get the Results
//							IndependentObjectSet objectSet = getObjectStoreQueryResults(os, sql, wiiscLog);
//
//							//Check to see if any Documents were Found
//							if (!objectSet.isEmpty())
//							{
//								//Loop through Documents
//								Iterator iter = objectSet.iterator();
//
//								while (iter.hasNext())
//								{
//									//wiiscLog.log(wiiscLog.INFO, "Found a Document");
//									//String tempDocPropList = "";
//									Document document = (Document) iter.next();
//									
//									//Verify the Document was found and perform Indexing
//									if (document != null)
//									{
//										wiiscLog.log(wiiscLog.INFO, "Document Found");
//										//Process 1 FileNet Document with IndexDocument Method
//										FnDocument fnDocument = indexDocument(document, fnDocumentRequest, wiiscLog);
//										//Check if the Error Flag was set
//										if (fnDocument.getErrorFlag() > 0)
//										{
//											//Process Error - report bad indexing
//											wiiscLog.log(wiiscLog.INFO, "Document failed to update in the Object Store");
//											wiiscLog.log(wiiscLog.INFO, "===========================================================");
//											//Update the Document Status
//											fnDocument.setFnDocumentStatus("Document failed to update in the Object Store");
//											fnDocument.setErrorFlag(1);
//											fnDocument.setErrorMessage("Document failed to update in the Object Store");
//										}
//										else
//										{
//											//No Error
//											wiiscLog.log(wiiscLog.INFO, "Document successfully updated in the Object Store");
//											wiiscLog.log(wiiscLog.INFO, "===========================================================");
//											//Update the Document Status
//											fnDocument.setFnDocumentStatus("Document successfully updated in the Object Store");
//											fnDocument.setErrorFlag(0);
//											fnDocument.setErrorMessage("Document successfully updated in the Object Store");
//										}
//										//Add the FnDocument to the FnDocumentList
//										fnDocumentList.addFnDocument(fnDocument);
//									}
//									else
//									{
//										wiiscLog.log(wiiscLog.INFO, "Document was NOT Found");
//									}
//								}//End While
//							}
//							else
//							{
//								wiiscLog.log(wiiscLog.INFO, "No Documents Found for the Query");
//							}
//							
//							//Verify the Doc List is not empty
//							if (fnDocumentList.getCount() > 0)
//							{
//								//Output total documents stored in the Doc List
//								wiiscLog.log(wiiscLog.INFO, "Total Documents Updated: " + fnDocumentList.getCount());
//								wiiscLog.log(wiiscLog.INFO, "===========================================================");
//								//Update the errorFlag to 0 to avoid it being set as an Error anywhere else.
//								fnDocumentList.setErrorFlag(0);
//								//Update the ErrorMessage
//								fnDocumentList.setErrorMessage("");
//							}
//							else
//							{
//								wiiscLog.log(wiiscLog.INFO, "No Documents Updated");
//								//Update the errorFlag to 0 because no Documents existed and this is not an Error
//								fnDocumentList.setErrorFlag(0);
//								//Update the ErrorMessage
//								fnDocumentList.setErrorMessage("No Documents Updated");
//							}					
//						}
//						else
//						{
//							//SQL Query was empty
//							wiiscLog.log(wiiscLog.INFO, "SQL Query for the Documents was invalid");
//							//Update errorFlag to 1 because SQL Query was invalid
//							fnDocumentList.setErrorFlag(1);
//							//Update the ErrorMessage
//							fnDocumentList.setErrorMessage("SQL Query for the Documents was invalid");
//						}
//					}
//					else
//					{
//						//Application ID was empty
//						wiiscLog.log(wiiscLog.INFO, "Application ID was missing from the Request");
//						//Update errorFlag to 1
//						fnDocumentList.setErrorFlag(1);
//						//Update the ErrorMessage
//						fnDocumentList.setErrorMessage("Application ID was missing from the Request");
//					}
//				}
//				else
//				{
//					//Doc Class was empty
//					wiiscLog.log(wiiscLog.INFO, "Document Class was missing from the Request");
//					//Update errorFlag to 1
//					fnDocumentList.setErrorFlag(1);
//					//Update the ErrorMessage
//					fnDocumentList.setErrorMessage("Document Class was missing from the Request");
//				}
//			}//End If
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnDocumentList.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnDocumentList.setErrorMessage(e.getMessage());
//			//Reset Object Store variable
//			//os = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> searchAndUpdateDocuments()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnDocumentList;
//	}
//	
//	//@SuppressWarnings("rawtypes")
//	private FnBatchList getBatches(ObjectStore os, String batchClass, String batchName, WIISCLog wiiscLog)
//	{
//		//List of FileNet Batches
//		FnBatchList fnBatchList = new FnBatchList();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getBatches()");
//
//			//Verify Object Store Connected
//			if (os != null)
//			{
//				//Get Batch Name Property
//				String indexingBatchNameProperty = "";
//				indexingBatchNameProperty = globalConfig.getString("indexingBatchNameProperty");
//				
//				//Get the Indexing Batches Root Location from the Config
//				String indexingBatchesRootLocation = "";
//				indexingBatchesRootLocation = globalConfig.getString("indexingBatchesRootLocation");
//				
//				//Get the Indexing Folder Object where Batch Folders are created
//				Folder indexingFolder = Factory.Folder.fetchInstance(os, indexingBatchesRootLocation, null);
//				//Get the list of Subfolders (Batches) in the Indexing Folder - if there are none, then exit
//				FolderSet batchFolders = null;
//				batchFolders = indexingFolder.get_SubFolders();
//				//Check to see if there are any Batch folders in the "indexingBatchNameProperty" location
//				//If there aren't any, then skip everything
//				if (!batchFolders.isEmpty())
//				{
//					//It is assumed that there are Batch Folders to index documents
//					wiiscLog.log(wiiscLog.INFO, "Batch Folders were found for indexing");
//					
//					//Loop through Folders
//					Iterator batchIter = null;
//					batchIter = batchFolders.iterator();
//					
//					while (batchIter.hasNext())
//					{
//						//Get 1 Batch Folder
//						Folder oneBatch = null;
//						oneBatch = (Folder) batchIter.next();
//						//Get Batch Name - which is the Folder Name
//						String currentBatchName = "";
//						currentBatchName = oneBatch.get_Name();
//						wiiscLog.log(wiiscLog.INFO, "Batch Folder " + currentBatchName);
//						
//						//FileNet Batch
//						FnBatch currentFnBatch = new FnBatch();
//						//Call GetBatch
//						currentFnBatch = getBatch(os, batchClass, currentBatchName, wiiscLog);
//						
//						//Check if any Documents were Found - is the batch valid?
//						if (currentFnBatch.getCount() > 0)
//						{
//							//Set the Batch Class
//							if (batchClass.length() > 0)
//							{
//								currentFnBatch.setFnBatchClass(batchClass);
//							}
//							else
//							{
//								//Determine the Batch Class from Documents
//								FnDocument fnDocument = currentFnBatch.getFnDocumentList().get(0);
//								currentFnBatch.setFnBatchClass(fnDocument.getFnDocumentClass());
//							}
//							
//							//Set the Batch Name
//							currentFnBatch.setFnBatchName(currentBatchName);
//							//Remove Document Properties since we don't show this in general Batch list
//							currentFnBatch.removeDocumentProperties();
//							//Add the fnBatch to the fnBatchList
//							fnBatchList.addFnBatch(currentFnBatch);
//							//Output the total documents in the Batch
//							wiiscLog.log(wiiscLog.INFO, "Total Documents in the Batch to View: " + currentFnBatch.getCount());
//							wiiscLog.log(wiiscLog.INFO, "===========================================================");
//						}
//						
//					}//End While BatchIter
//				}
//				else
//				{
//					//There are No Batch Folders
//					wiiscLog.log(wiiscLog.INFO, "NO Batch Folders were found for indexing");
//					wiiscLog.log(wiiscLog.INFO, "===========================================================");
//					//Update the errorFlag to 0 because no Documents existed and this is not an Error
//					fnBatchList.setErrorFlag(0);
//					//Update the ErrorMessage
//					fnBatchList.setErrorMessage("");
//				}
//			}//End If
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnBatchList.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnBatchList.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getBatches()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnBatchList;
//	}
//	
//	private FnBatch getBatch(ObjectStore os, String batchClass, String batchName, WIISCLog wiiscLog)
//	{
//		FnBatch fnBatch = new FnBatch();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getBatch()");
//			
//			//Get Batch Name Property
//			String indexingBatchNameProperty = "";
//			indexingBatchNameProperty = globalConfig.getString("indexingBatchNameProperty");
//			
//			//Get the Indexing Batches Root Location from the Config
//			String indexingBatchesRootLocation = "";
//			indexingBatchesRootLocation = globalConfig.getString("indexingBatchesRootLocation");
//			
//			//Get the Batch Folder
//			Folder batchFolder = Factory.Folder.fetchInstance(os, indexingBatchesRootLocation + "/" + batchName, null);
//			//Get the list of Documents in the Batch Folder - if there are none, then exit
//			DocumentSet documentSet = null;
//			documentSet = batchFolder.get_ContainedDocuments();
//			//Check to see if there are any Documents in the Batch
//			//If there aren't any, then skip everything
//			if (!documentSet.isEmpty())
//			{
//				//Loop through Documents
//				Iterator documentIter = null;
//				documentIter = documentSet.iterator();
//				//Set the Document Object to be reused
//				Document aDocument = null;
//				
//				//Create the FnPropertyList
//				//FnPropertyList masterFnPropertyList = new FnPropertyList();
//								
//				//Check if Batch Class is passed in
//				if (batchClass.length() == 0)
//				{
//					wiiscLog.log(wiiscLog.INFO, "Batch Class was not passed in, so it will be determined");
//					aDocument = (Document) documentIter.next();
//					//Set the Batch Class
//					batchClass = aDocument.getClassName();
//				}
//				
//				//Get the Document Properties Template to use
//				String[] propertiesData = null;
//				propertiesData = getDocumentPropertiesTemplate(os, batchClass, wiiscLog);
//				
//				//Save the 1 Document if it was retrieved
//				if (aDocument != null)
//				{
//					wiiscLog.log(wiiscLog.INFO, "Found a " + batchClass + " Document - " + aDocument.get_Name());
//					//Create the FnPropertyList
//					//masterFnPropertyList = getDocumentProperties(os, batchClass, wiiscLog);
//					//Get the FileNet Document Info
//					FnDocument fnDocument = new FnDocument();
//					//fnDocument = getDocument(aDocument, masterFnPropertyList, batchClass, batchName, wiiscLog);
//					//fnDocument = getDocument(aDocument, fnDocumentTemplate, batchName, wiiscLog);
//					fnDocument = getDocument(aDocument, propertiesData, wiiscLog);
//					
//					//Add the FnDocument to the FnBatch
//					fnBatch.addFnDocument(fnDocument);
//					//Reset the Document Object
//					//aDocument = null;
//				}
//				
//				//Check for more Documents in the Document Iterator				
//				while (documentIter.hasNext())
//				{
//					//Get a Document
//					Document oneDocument = (Document) documentIter.next();
//					//Check if the Document matches the assumed Batch Class
//					if (!oneDocument.getClassName().equals(batchClass))
//					{
//						wiiscLog.log(wiiscLog.INFO, "===========================================================");
//						wiiscLog.log(wiiscLog.ERROR, "Batch Class does not match the Documents stored in the Batch");
//						wiiscLog.log(wiiscLog.INFO, "===========================================================");
//						//Update errorFlag
//						fnBatch.setErrorFlag(1);
//						//Update the ErrorMessage
//						fnBatch.setErrorMessage("Batch Class does not match the Documents stored in the Batch");
//						break;
//					}
//					wiiscLog.log(wiiscLog.INFO, "Found the " + batchClass + " Document - " + oneDocument.get_Name());
//					wiiscLog.log(wiiscLog.INFO, "===========================================================");
//					//Reset FnPropertyList
//					//masterFnPropertyList = null;
//					//Get a New FnPropertyList
//					//masterFnPropertyList = getDocumentProperties(os, batchClass, wiiscLog);
//					//Get the FileNet Document Info
//					FnDocument fnDocument = new FnDocument();
//					//fnDocument = getDocument(oneDocument, masterFnPropertyList, batchClass, batchName, wiiscLog);
//					//fnDocument = getDocument(oneDocument, fnDocumentTemplate, batchName, wiiscLog);
//					fnDocument = getDocument(oneDocument, propertiesData, wiiscLog);
//					//Add the FnDocument to the FnBatch
//					fnBatch.addFnDocument(fnDocument);
//					//Reset the Document Object
//					//oneDocument = null;
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "===========================================================");
//				wiiscLog.log(wiiscLog.INFO, "Batch - " + batchName + " does not contain any Documents for indexing");
//				wiiscLog.log(wiiscLog.INFO, "===========================================================");
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnBatch.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnBatch.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getBatch()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnBatch;
//	}
//	
//	//@SuppressWarnings("rawtypes")
//	private FnDocumentList getDocuments(ObjectStore os, String docClassName, String docPropValue, WIISCLog wiiscLog)
//	{
//		//Document List to keep track of updated documents
//		FnDocumentList fnDocumentListUpdated = new FnDocumentList();
//		//Document Name
//		String documentName = "";
//		//Document ID
//		String documentID = "";
//		//Document Class
//		String documentClass = "";
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getDocuments()");
//
//			//Verify Object Store Connected
//			if (os != null)
//			{
//				//Create the FnPropertyList
//				FnPropertyList fnPropertyList = new FnPropertyList();
//								
//				//Get the Document Properties Template to use
//				String[] propertiesData = null;
//				propertiesData = getDocumentPropertiesTemplate(os, docClassName, wiiscLog);
//				
//				//Build FnPropertyList for the Query
//				for (String prop : propertiesData)
//				{
//					FnProperty fnProp = new FnProperty();
//					fnProp.setName(prop);
//					//Add Property
//					fnPropertyList.addFnProperty(fnProp);
//				}
//								
//				//Create the ObjectStore Search
//				//String sql = getObjectStoreQuery(fnPropertyList, docClassName, docPropValue, wiiscLog);
//				String sql = getObjectStoreQuery(fnPropertyList, docClassName, docPropValue, wiiscLog);
//
//				//Verify SQL String has a value
//				if (sql.length() > 0)
//				{
//					//Search for Documents and get the Results
//					IndependentObjectSet objectSet = null;
//					objectSet = getObjectStoreQueryResults(os, sql, wiiscLog);
//
//					//Check to see if any Documents were Found
//					if (!objectSet.isEmpty())
//					{
//						//Get Document Search Limit Property to avoid large Document Searches
//						String documentSearchLimit = "";
//						documentSearchLimit = globalConfig.getString("documentSearchLimit");
//						//Get Document Search Limit as Integer
//						int documentSearchMax = 0;
//						
//						//Check Document Search Limit
//						if (documentSearchLimit.length() == 0)
//						{
//							documentSearchMax = 0;
//						}
//						else
//						{
//							documentSearchMax = Integer.parseInt(documentSearchLimit);
//						}
//						
//						//Loop through Documents
//						Iterator iter = null;
//						iter = objectSet.iterator();
//						//int x = 0;
//						while (iter.hasNext())
//						{
//							wiiscLog.log(wiiscLog.INFO, "Found a Document");
//							//String tempDocPropList = "";
//							Document document = null;
//							document = (Document) iter.next();
//							//Document document = (Document) iter.next();
//							documentName = document.get_Name();
//														
//							//Process 1 FileNet Document with IndexDocument Method
//							FnDocument fnDocumentUpdate = new FnDocument();
//							
//							//fnDocumentUpdate = getDocument(document, fnDocumentTemplate, docPropValue, wiiscLog);
//							fnDocumentUpdate = getDocument(document, propertiesData, wiiscLog);
//														
//							//Add the FnDocument to the FnDocumentList
//							fnDocumentListUpdated.addFnDocument(fnDocumentUpdate);
//							//x++;
//							
//							//Check if documentSearchMax is being used, otherwise skip
//							if (documentSearchMax > 0)
//							{
//								//Check if we need to Exit the While
//								if (fnDocumentListUpdated.getCount() >= documentSearchMax)
//								{
//									break;
//								}
//							}
//						}//End While
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "No Documents Found");
//					}
//										
//					//Verify the Doc List is not empty
//					if (fnDocumentListUpdated.getCount() > 0)
//					{
//						//Output total documents stored in the Doc List
//						wiiscLog.log(wiiscLog.INFO, "Total Documents in the Document List to View: " + fnDocumentListUpdated.getCount());
//						wiiscLog.log(wiiscLog.INFO, "===========================================================");
//						//Update the errorFlag to 0 to avoid it being set as an Error anywhere else.
//						fnDocumentListUpdated.setErrorFlag(0);
//						//Update the ErrorMessage
//						fnDocumentListUpdated.setErrorMessage("");
//					}
//					else
//					{
//						//Update the errorFlag to 0 because no Documents existed and this is not an Error
//						fnDocumentListUpdated.setErrorFlag(0);
//						//Update the ErrorMessage
//						fnDocumentListUpdated.setErrorMessage("");
//					}					
//				}
//				else
//				{
//					//SQL Query was empty
//					wiiscLog.log(wiiscLog.INFO, "SQL Query for the Documents was invalid");
//					//Update errorFlag to 1 because SQL Query was invalid
//					fnDocumentListUpdated.setErrorFlag(1);
//					//Update the ErrorMessage
//					fnDocumentListUpdated.setErrorMessage("SQL Query for the Documents was invalid");
//				}
//					
//			}//End If
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnDocumentListUpdated.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnDocumentListUpdated.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getDocuments()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnDocumentListUpdated;
//	}
//	
//	//@SuppressWarnings("rawtypes")
//	public FnDocumentList getCustomDocTypes(WIISCLog wiiscLog)
//	{
//		//CE Object Store
//		ObjectStore os = null;
//		//List to hold the Custom Doc Types List
//		FnDocumentList docList = new FnDocumentList();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getCustomDocTypes()");
//					
//			//Define the Global Properties Resource
//			//ResourceBundle globalConfig = null;
//			//globalConfig = ResourceBundle.getBundle("GlobalConfig");
//			String userId = globalConfig.getString(ConstantsUtil.CE_USER_ID);
//			String password = globalConfig.getString(ConstantsUtil.CE_USER_PASSWORD);
//			
//			//Get CE Object Store
//			os = getObjectStore(userId, password, wiiscLog);
//			
//			//Verify Object Store Connected
//			if (os != null)
//			{
//				//Get the Doc Types Location in the Object Store
//				String docTypesRootLocation = globalConfig.getString("docTypesRootLocation");
//				//Verify the docTypesRootLocation Folder exists in the Object Store
//				if (checkFolderExists(os,docTypesRootLocation,wiiscLog))
//				{
//					//Get the Document Class used for Doc Types
//					String docTypeClass = globalConfig.getString("docTypeClass");
//					//Get Custom Doc Props to be used for checking against any found Doc Type Documents
//					FnPropertyList fnProps = new FnPropertyList();
//					fnProps = getCustomDocProperties(os,docTypeClass,wiiscLog);
//					List<FnProperty> fnPropsList = fnProps.getFnDocumentPropsList(); 
//					//Get Doc Types Location Folder Object
//					Folder rootFolder = Factory.Folder.fetchInstance(os, docTypesRootLocation, null);
//					//Get the SubFolders for any specific class defined Doc Types
//					FolderSet subFolderFolderSet = rootFolder.get_SubFolders();
//					if (subFolderFolderSet != null && !subFolderFolderSet.isEmpty())
//					{
//						Iterator folderSetIterator = subFolderFolderSet.iterator();
//						while (folderSetIterator.hasNext())
//						{
//							Folder subFolder = (Folder) folderSetIterator.next();
//							//Check for Documents
//							if (!subFolder.get_ContainedDocuments().isEmpty())
//							{
//								//Get the List of Documents
//								DocumentSet subFolderDocumentSet = subFolder.get_ContainedDocuments();
//								if (subFolderDocumentSet != null && !subFolderDocumentSet.isEmpty())
//								{
//									Iterator documentSetIterator = subFolderDocumentSet.iterator();
//									while (documentSetIterator.hasNext())
//									{
//										Document document = (Document) documentSetIterator.next();
//										//Update Property Cache to include Custom Doc Properties
//										document.refresh();
//										FnDocument fnDoc = new FnDocument();
//										//Set the Doc Class
//										fnDoc.setFnDocumentClass(subFolder.get_FolderName());
//										//Set the Doc Name
//										fnDoc.setFnDocumentName(document.get_Name());
//										Properties props = document.getProperties();
//										FnPropertyList fnPropsForDocList = new FnPropertyList();
//										//Set Properties For Doc
//										//fnPropsForDocList = fnDoc.getFnDocProps();
//										for (int a = 0; a < fnPropsList.size(); a++)
//										{
//											//Get 1 FnProperty Object to compare against the Document
//											FnProperty fnProp = fnPropsList.get(a);
//											//Verify if the fnProp is present in the Document
//											if (props.isPropertyPresent(fnProp.getName()))
//											{
//												//fnDoc.setFnDocumentClass(subFolder.get_FolderName());
//												FnProperty fnPropForDoc = new FnProperty();
//												fnPropForDoc.setName(fnProp.getName());
//												fnPropForDoc.setValue(props.getStringValue(fnProp.getName()));
//												fnPropsForDocList.addFnProperty(fnPropForDoc);
//											}
//										}
//										//Verify fnPropsForDocList 
//										if (fnPropsForDocList.getCount() > 0 && fnPropsForDocList != null)
//										{
//											//Verify the fnDoc was setup and not null
//											if (fnDoc.getFnDocumentClass().length() > 0 && fnDoc != null)
//											{
//												//Add FnDocument to FnDocumentList
//												docList.addFnDocument(fnDoc);
//											}
//										}
//									} //End While
//								}
//							}
//						}
//					}
//					else
//					{
//						//Check for Documents
//						if (!rootFolder.get_ContainedDocuments().isEmpty())
//						{
//							//Get the List of Documents
//							DocumentSet folderDocumentSet = rootFolder.get_ContainedDocuments();
//							if (folderDocumentSet != null && !folderDocumentSet.isEmpty())
//							{
//								Iterator documentSetIterator = folderDocumentSet.iterator();
//								while (documentSetIterator.hasNext())
//								{
//									Document document = (Document) documentSetIterator.next();
//									//Update Property Cache to include Custom Doc Properties
//									document.refresh();
//									FnDocument fnDoc = new FnDocument();
//									//Set the Doc Class
//									fnDoc.setFnDocumentClass(rootFolder.get_FolderName());
//									//Set the Doc Name
//									fnDoc.setFnDocumentName(document.get_Name());
//									Properties props = document.getProperties();
//									FnPropertyList fnPropsForDocList = new FnPropertyList();
//									//Set Properties For Doc
//									//fnPropsForDocList = fnDoc.getFnDocProps();
//									for (int a = 0; a < fnPropsList.size(); a++)
//									{
//										//Get 1 FnProperty Object to compare against the Document
//										FnProperty fnProp = fnPropsList.get(a);
//										//Verify if the fnProp is present in the Document
//										if (props.isPropertyPresent(fnProp.getName()))
//										{
//											//fnDoc.setFnDocumentClass(rootFolder.get_FolderName());
//											FnProperty fnPropForDoc = new FnProperty();
//											fnPropForDoc.setName(fnProp.getName());
//											fnPropForDoc.setValue(props.getStringValue(fnProp.getName()));
//											fnPropsForDocList.addFnProperty(fnPropForDoc);
//										}
//									}
//									//Verify fnPropsForDocList
//									if (fnPropsForDocList.getCount() > 0 && fnPropsForDocList != null)
//									{
//										//Verify the fnDoc was setup and not null
//										if (fnDoc.getFnDocumentClass().length() > 0 && fnDoc != null)
//										{
//											//Add FnDocument to FnDocumentList
//											docList.addFnDocument(fnDoc);
//										}
//									}
//								} //End While
//							}
//						}
//					}
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "DocTypesRootLocation: " + docTypesRootLocation + " does not exist in the Object Store");
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Imaging Login FAILED, Imaging Server may be unavailable.");
//				//Update the docList Object
//				docList.setErrorFlag(1);
//				//Update the ErrorMessage
//				docList.setErrorMessage("Imaging Login FAILED, Imaging Server may be unavailable.");
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 2");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//		}	
//		
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getCustomDocTypes()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return docList;
//	}
//	
//	public String getDocumentViewerResponse(String docId, WIISCLog wiiscLog)
//	{
//		String responseBody = "";
//		CloseableHttpClient httpclient = null;
//		String documentViewerLoginPageURL = "";
//		//Used for Image Viewer and Image Browser
//		String scheme = "";
//		String host = "";
//		String port = "";
//		String docURL = "";
//		String docGUID = "";
//		BasicCookieStore cookieStore = new BasicCookieStore();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getDocumentViewerResponse()");
//			wiiscLog.log(wiiscLog.INFO, "DocId: " + docId);
//			
//			//CE Object Store
//			ObjectStore os = null;
//			String userId = globalConfig.getString(ConstantsUtil.CE_USER_ID);
//			String password = globalConfig.getString(ConstantsUtil.CE_USER_PASSWORD);
//
//			//Get CE Object Store
//			os = getObjectStore(userId, password, wiiscLog);
//
//			//Get the FileNet GUID
//			docGUID = getFNDocumentGUID(os, docId, wiiscLog);
//			wiiscLog.log(wiiscLog.INFO, "Found docGUID: " + docGUID);
//			//Reset docId
//			docId = "";			
//			
//			//Check docGUID to see if it has a value that needs to be used to replace docId
//			if (docGUID.length() > 0)
//			{
//				//Update the docId to use the GUID for Image Viewing
//				docId = docGUID;
//			}
//			
//			//Check to make sure docId has a value
//			if (docId.length() > 0)
//			{
//				//Get the Scheme
//				scheme = globalConfig.getString("httpHeader");
//				wiiscLog.log(wiiscLog.INFO, "Scheme is " + scheme);
//				//Get the Host
//				host = globalConfig.getString("aeServerName");
//				wiiscLog.log(wiiscLog.INFO, "Host is " + host);
//				//Get the Port
//				port = globalConfig.getString("aeApplicationPort");
//				wiiscLog.log(wiiscLog.INFO, "Port is " + port);
//				
//				//Create the HttpClient Object
//				//httpclient = HttpClients.createDefault();
//				httpclient = HttpClients.custom()
//						.setDefaultCookieStore(cookieStore)
//						.build();
//				wiiscLog.log(wiiscLog.INFO, "Launching HttpClient");
//
//				//Build the Document Viewer Login Page URL
//				//documentViewerLoginPageURL = scheme + "://" + host + ":" + port + globalConfig.getString("aeApplicationContext") + globalConfig.getString("aeApplicationLoginPage");
//				documentViewerLoginPageURL = scheme + "://" + host + ":" + port + globalConfig.getString("aeApplicationContext");
//				
//				//Get Login Page
//				HttpGet httpget = new HttpGet(documentViewerLoginPageURL);
//				wiiscLog.log(wiiscLog.INFO, "Login Page: " + documentViewerLoginPageURL);
//				CloseableHttpResponse response1 = httpclient.execute(httpget);
//				try
//				{
//					HttpEntity entity = response1.getEntity();
//					wiiscLog.log(wiiscLog.INFO, "Login Page GET " + response1.getStatusLine() + " " + response1.getStatusLine().getStatusCode());
//					responseBody = "";
//					responseBody = EntityUtils.toString(entity);
//					EntityUtils.consume(entity);
//					List<Cookie> cookies = cookieStore.getCookies();
//					if (cookies.isEmpty())
//					{
//						wiiscLog.log(wiiscLog.INFO, "NONE");
//					}
//					else
//					{
//						for (int i = 0; i < cookies.size(); i++)
//						{
//							wiiscLog.log(wiiscLog.INFO, "- " + cookies.get(i).toString());
//						}
//					}
//					wiiscLog.log(wiiscLog.INFO, "***Login Page GET Output***");
//					wiiscLog.log(wiiscLog.INFO, responseBody);
//				}
//				finally
//				{
//					response1.close();
//				}
//				
//				//Build Login Request
//				HttpUriRequest login = RequestBuilder.post()
//						.setUri(new URI(documentViewerLoginPageURL + "/j_security_check"))
//						.addParameter("j_username", userId)
//						.addParameter("j_password", password)
//						.build();
//				
//				//HttpPost authpost = new HttpPost(scheme + "://" + host + ":" + port + globalConfig.getString("aeApplicationContext") + "/j_security_check");
//				//wiiscLog.log(wiiscLog.INFO, "Login Request: " + scheme + "://" + host + ":" + port + globalConfig.getString("aeApplicationContext") + "/j_security_check");
//				//List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//				//nameValuePairs.add(new BasicNameValuePair("j_username", userId));
//				//nameValuePairs.add(new BasicNameValuePair("j_password", password));
//				//authpost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//				//CloseableHttpResponse response2 = httpclient.execute(authpost);
//				
//				CloseableHttpResponse response2 = httpclient.execute(login);
//				//User Login Token
//				String ut = "";
//				
//				try
//				{
//					HttpEntity entity = response2.getEntity();
//					wiiscLog.log(wiiscLog.INFO, "Login Page POST " + response2.getStatusLine() + " " + response2.getStatusLine().getStatusCode());
//					responseBody = "";
//					responseBody = EntityUtils.toString(entity);
//					EntityUtils.consume(entity);
//					List<Cookie> cookies = cookieStore.getCookies();
//					if (cookies.isEmpty())
//					{
//						wiiscLog.log(wiiscLog.INFO, "NONE");
//					}
//					else
//					{
//						for (int i = 0; i < cookies.size(); i++)
//						{
//							wiiscLog.log(wiiscLog.INFO, "- " + cookies.get(i).toString());
//							if (cookies.get(i).getName().equals("LtpaToken"))
//							{
//								//Get the User Token
//								ut = cookies.get(i).getValue();
//							}
//						}
//					}
//					wiiscLog.log(wiiscLog.INFO, "***Login Page POST Output***");
//					wiiscLog.log(wiiscLog.INFO, responseBody);
//				}
//				finally
//				{
//					response2.close();
//				}
//				
//				//Encode the User Token
//				ut = URLEncoder.encode(ut, "UTF-8");
//				
//				//Generate the DocURL to use the FileNet Viewer
//				docURL = scheme + "://" + host + ":" + port + globalConfig.getString("aeApplicationContext") + "/getContent?" + globalConfig.getString("viewIDtype") + "=" + docId + "&objectStoreName=" + globalConfig.getString("objectstoreName") + "&objectType=document&ut=" + ut;
//				wiiscLog.log(wiiscLog.INFO, "Doc URL: " + docURL);
//				
//				//Not Working
//				/*HttpUriRequest docViewer = RequestBuilder.get()
//						.setUri(new URI(docURL))
//						.build();
//				wiiscLog.log(wiiscLog.INFO, "Doc URL: " + docURL);
//				CloseableHttpResponse response3 = httpclient.execute(docViewer);*/
//				
//				//Working
//				/*HttpUriRequest httpget2 = RequestBuilder.get()
//						.setUri(new URI(documentViewerLoginPageURL + "/getContent"))
//						.addParameter("id", docId)
//						.addParameter("objectStoreName".getString("objectstoreName"))
//						.addParameter("objectType", "document")
//						.addParameter("ut", ut)
//						.build();
//				
//				//HttpGet httpget2 = new HttpGet(docURL);
//				wiiscLog.log(wiiscLog.INFO, "Doc URL: " + docURL);
//				CloseableHttpResponse response3 = httpclient.execute(httpget2);
//				
//				try
//				{
//					HttpEntity entity = response3.getEntity();
//					wiiscLog.log(wiiscLog.INFO, "Document Viewer Page GET " + response3.getStatusLine() + " " + response3.getStatusLine().getStatusCode());
//					responseBody = "";
//					responseBody = EntityUtils.toString(entity);
//					EntityUtils.consume(entity);
//					wiiscLog.log(wiiscLog.INFO, "***Document Viewer Page GET Output***");
//					wiiscLog.log(wiiscLog.INFO, responseBody);
//				}
//				finally
//				{
//					response3.close();
//				}*/
//			}
//		}
//		catch(Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			responseBody = "";
//		}
//		finally
//		{
//			if (httpclient != null)
//			{
//				try {
//					httpclient.close();
//				} catch (IOException e) {
//					wiiscLog.log(wiiscLog.ERROR, "Exception Closing HttpClient");
//					wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//				}
//			}
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getDocumentViewerResponse()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		//return responseBody;
//		return docURL;
//	}
//	
//	public String getImageBrowserResponse()
//	{
//		String responseBody = "";
//		//More to do
//		return responseBody;
//	}
//	
//	@SuppressWarnings("null")
//	public String getClaretyImageBrowserResponse(String docClass, String appId, String firstName, String lastName, String orgName, String privilege, String processId, WIISCLog wiiscLog)
//	{
//		String responseBody = "";
//		String imageBrowserURL = "";
//		String[] imageBrowserURLData1 = null;
//		String[] imageBrowserURLData2 = null;
//		String authStringEnc = "";
//		CloseableHttpClient httpclient = null;
//		//Used for Image Viewer
//		String workflowDocumentKeyField = "";
//		String docId = "";
//		//Used for Image Viewer and Image Browser
//		String scheme = "";
//		String host = "";
//		String port = "";
//		int portNum = 0;
//		String path = "";
//		URIBuilder builder = null;
//		URI uri = null;
//		String claretyImagingParams = "";
//		String claretyImagingParameterValue = "";
//		String[] claretyImagingParamsData = null;
//						
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getClaretyImageBrowserResponse()");
//			
//			//Check if Process ID exists and we need to do an Image Viewer instead of Image Browser
//			if (processId != null && processId.length() > 0)
//			{
//				//FnWorkflow Object used to store the Workflow returned from the Process ID search
//				FnWorkflow fnWorkflow = new FnWorkflow();
//				//Setup GlobalConfig for WorkflowManager
//				workflowManager.setGlobalConfig(getGlobalConfig());
//				fnWorkflow = workflowManager.getWorkflowInfo("", "", "", "process_id", processId, wiiscLog);
//				//Check FnWorkflow to see if it was valid
//				if (fnWorkflow != null || fnWorkflow.getErrorFlag() != 1)
//				{
//					//Get the Workflow Document Key Field
//					workflowDocumentKeyField = globalConfig.getString("claretyImagingDocumentKeyField");
//					
//					FnWorkflowPropertyList fnWorkflowPropertyList = new FnWorkflowPropertyList();
//					fnWorkflowPropertyList = fnWorkflow.getFnWorkflowPropertyList();
//					//Check FnWorkflow to see if any Workflow Properties were found - if not then exit
//					if (fnWorkflowPropertyList != null)
//					{
//						wiiscLog.log(wiiscLog.INFO, "Workflow Found");
//						
//						for (int i = 0; i < fnWorkflowPropertyList.getFnWorkflowPropsList().size(); i++)
//						{
//							FnWorkflowProperty fnWorkflowProperty = new FnWorkflowProperty();
//							fnWorkflowProperty = fnWorkflowPropertyList.getFnWorkflowPropsList().get(i);
//							//Check if the FnWorkflowProperty is the Document Key field
//							if (workflowDocumentKeyField.equals(fnWorkflowProperty.getName()))
//							{
//								wiiscLog.log(wiiscLog.INFO, "Workflow Document Key Field Found");
//								wiiscLog.log(wiiscLog.INFO, "Property: " + fnWorkflowProperty.getName());
//								//Check if the Document Key field value is null or empty
//								String tempDocId = "";
//								tempDocId = fnWorkflowProperty.getValue();
//								if (tempDocId != null && tempDocId.length() > 0 && !tempDocId.equals("0"))
//								{
//									wiiscLog.log(wiiscLog.INFO, "Value: " + fnWorkflowProperty.getValue());
//									//Save the Document ID
//									docId = fnWorkflowProperty.getValue();
//								}
//								else
//								{
//									wiiscLog.log(wiiscLog.INFO, "Value: ");
//									//Save the Document ID
//									docId = "";
//								}
//								break;
//							}
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "Workflow Not Found so No Document ID - using Image Browser List");
//					}
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "Error with Workflow - using Image Browser List");
//				}
//			}
//			
//			//Check if a valid Doc ID was found and we need to do an Image Viewer instead of Image Browser
//			if (docId.length() > 0)
//			{
//				//Get the Clarety Image Viewer URL from Global Config
//				imageBrowserURL = globalConfig.getString("claretyImagingViewerURL");
//								
//				//e.g. http://tstextappvm.tstp8.com:9080/BasicAuth/admin
//				imageBrowserURLData1 = imageBrowserURL.split(":");
//				//Get the Scheme - http
//				scheme = imageBrowserURLData1[0];
//				wiiscLog.log(wiiscLog.INFO, "Scheme: " + scheme);
//				
//				//Get the Host - tstextappvm.tstp8.com
//				//Check if the Image Browser URL has the Port Number in the URL
//				if (imageBrowserURLData1.length == 3)
//				{
//					String tempHost = "";
//					tempHost = imageBrowserURLData1[1];
//					//Strip off the front slashes //
//					host = tempHost.substring(2);
//					wiiscLog.log(wiiscLog.INFO, "Host: " + host);
//					//Get the Port and Path
//					imageBrowserURLData2 = imageBrowserURLData1[2].split("/");
//					port = imageBrowserURLData2[0];
//					portNum = Integer.parseInt(port);
//					wiiscLog.log(wiiscLog.INFO, "Port: " + port);
//					//Get the Path based on the remaining elements in imageBrowserURLData2
//					for (int i = 1; i < imageBrowserURLData2.length; i++)
//					{
//						path = path + "/" + imageBrowserURLData2[i];
//					}
//					wiiscLog.log(wiiscLog.INFO, "Path: " + path);
//				}
//				else
//				{
//					String tempHost = "";
//					//Strip off the front slashes //
//					tempHost = imageBrowserURLData1[1].substring(2); 
//					imageBrowserURLData2 = tempHost.split("/");
//					host = imageBrowserURLData2[0];
//					wiiscLog.log(wiiscLog.INFO, "Host: " + host);
//					//Get the Port based on if the scheme is http or https
//					if (scheme.equals("http"))
//					{
//						port = "80";
//						portNum = 80;
//					}
//					else if (scheme.equals("https"))
//					{
//						port = "443";
//						portNum = 443;
//					}
//					wiiscLog.log(wiiscLog.INFO, "Port: " + port);
//					//Get the Path based on the remaining elements in imageBrowserURLData2
//					for (int i = 1; i < imageBrowserURLData2.length; i++)
//					{
//						path = path + "/" + imageBrowserURLData2[i];
//					}
//					wiiscLog.log(wiiscLog.INFO, "Path: " + path);
//				}
//				
//				//Get the Authenticated Encoded String
//				authStringEnc = getAuthenticatedStringEnc(wiiscLog);
//				
//				//Create the HttpClient Object
//				httpclient = HttpClients.createDefault();
//				wiiscLog.log(wiiscLog.INFO, "Launching HttpClient");
//
//				//Create the URIBuilder for the URI
//				builder = new URIBuilder();
//				builder.setScheme(scheme);
//				builder.setHost(host);
//				builder.setPort(portNum);
//				builder.setPath(path);           
//				
//				//Get the Clarety Imaging Parameters for the Viewer
//				claretyImagingParams = globalConfig.getString("claretyImagingViewerURLParameters");
//				//Get the Clarety Imaging Parameter Value
//				claretyImagingParameterValue = globalConfig.getString("claretyImagingViewerURLParameterActionValue");
//				//Split the Clarety Imaging Parameters for the Viewer
//				claretyImagingParamsData = claretyImagingParams.split(",");
//				
//				//Build the Parameters for the URI
//				for (int i = 0; i < claretyImagingParamsData.length; i++)
//				{
//					//Set the 1st parameter equal to the parameter value
//					if (i == 0) //used for the action value
//					{
//						builder.setParameter(claretyImagingParamsData[i], claretyImagingParameterValue);
//					}
//					else if (i == 1) //used for the docId
//					{
//						builder.setParameter(claretyImagingParamsData[i], docId);
//					}
//					else //used for any additional parameters with no value
//					{
//						builder.setParameter(claretyImagingParamsData[i], "");
//					}
//				}
//				
//				//Build the URI
//				uri = builder.build();
//				wiiscLog.log(wiiscLog.INFO, "Built URI for Clarety Image Viewer");			
//			}
//			else
//			{
//				//Get the Clarety Image Browser URL from Global Config
//				imageBrowserURL = globalConfig.getString("claretyImagingBrowserURL");
//								
//				//e.g. http://tstextappvm.tstp8.com:9080/BasicAuth/admin
//				imageBrowserURLData1 = imageBrowserURL.split(":");
//				//Get the Scheme - http
//				scheme = imageBrowserURLData1[0];
//				wiiscLog.log(wiiscLog.INFO, "Scheme: " + scheme);
//				
//				//Get the Host - tstextappvm.tstp8.com
//				//Check if the Image Browser URL has the Port Number in the URL
//				if (imageBrowserURLData1.length == 3)
//				{
//					String tempHost = "";
//					tempHost = imageBrowserURLData1[1];
//					//Strip off the front slashes //
//					host = tempHost.substring(2);
//					wiiscLog.log(wiiscLog.INFO, "Host: " + host);
//					//Get the Port and Path
//					imageBrowserURLData2 = imageBrowserURLData1[2].split("/");
//					port = imageBrowserURLData2[0];
//					portNum = Integer.parseInt(port);
//					wiiscLog.log(wiiscLog.INFO, "Port: " + port);
//					//Get the Path based on the remaining elements in imageBrowserURLData2
//					for (int i = 1; i < imageBrowserURLData2.length; i++)
//					{
//						path = path + "/" + imageBrowserURLData2[i];
//					}
//					wiiscLog.log(wiiscLog.INFO, "Path: " + path);
//				}
//				else
//				{
//					String tempHost = "";
//					//Strip off the front slashes //
//					tempHost = imageBrowserURLData1[1].substring(2); 
//					imageBrowserURLData2 = tempHost.split("/");
//					host = imageBrowserURLData2[0];
//					wiiscLog.log(wiiscLog.INFO, "Host: " + host);
//					//Get the Port based on if the scheme is http or https
//					if (scheme.equals("http"))
//					{
//						port = "80";
//						portNum = 80;
//					}
//					else if (scheme.equals("https"))
//					{
//						port = "443";
//						portNum = 443;
//					}
//					wiiscLog.log(wiiscLog.INFO, "Port: " + port);
//					//Get the Path based on the remaining elements in imageBrowserURLData2
//					for (int i = 1; i < imageBrowserURLData2.length; i++)
//					{
//						path = path + "/" + imageBrowserURLData2[i];
//					}
//					wiiscLog.log(wiiscLog.INFO, "Path: " + path);
//				}
//				
//				//Get the Authenticated Encoded String
//				authStringEnc = getAuthenticatedStringEnc(wiiscLog);
//				
//				//Create the HttpClient Object
//				httpclient = HttpClients.createDefault();
//				wiiscLog.log(wiiscLog.INFO, "Launching HttpClient");
//
//				//If Member, combine First and Last Name; Otherwise, use OrgName
//				String name = "";
//				if (docClass.equals("Member"))
//				{
//					name = firstName + " " + lastName;
//				}
//				else if (docClass.equals("Employer"))
//				{
//					name = orgName;
//				}
//				else
//				{
//					name = "";
//				}
//				
//				//Create the URIBuilder for the URI
//				builder = new URIBuilder();
//				builder.setScheme(scheme);
//				builder.setHost(host);
//				builder.setPort(portNum);
//				builder.setPath(path);            
//				
//				//Get the Clarety Imaging Parameters for the Image Browser
//				claretyImagingParams = globalConfig.getString("claretyImagingBrowserURLParameters");
//				//Split the Clarety Imaging Parameters for the Image Browser
//				claretyImagingParamsData = claretyImagingParams.split(",");
//				
//				//Build the Parameters for the URI
//				for (int i = 0; i < claretyImagingParamsData.length; i++)
//				{
//					//Set the parameters
//					if (i == 0) //used for the doc class
//					{
//						builder.setParameter(claretyImagingParamsData[i], docClass);
//					}
//					else if (i == 1) //used for the name
//					{
//						builder.setParameter(claretyImagingParamsData[i], name);
//					}
//					else if (i == 2) //used for the appId
//					{
//						builder.setParameter(claretyImagingParamsData[i], appId);
//					}
//					else if (i == 3) //used for the privilege
//					{
//						builder.setParameter(claretyImagingParamsData[i], privilege);
//					}
//					else //used for any additional parameters with no value
//					{
//						//builder.setParameter(claretyImagingParamsData[i], "");
//					}
//				}
//				
//				//Build the URI
//				uri = builder.build();
//				wiiscLog.log(wiiscLog.INFO, "Built URI for Clarety Image Browser");				
//			}
//			
//			/*//Get the Clarety Image Browser URL from Global Config
//			imageBrowserURL = globalConfig.getString("claretyImagingBrowserURL");
//			//Get the Image Browser URI Parameters
//			String scheme = "";
//			String host = "";
//			String port = "";
//			int portNum = 0;
//			String path = "";
//			
//			//e.g. http://tstextappvm.tstp8.com:9080/BasicAuth/admin
//			imageBrowserURLData1 = imageBrowserURL.split(":");
//			//Get the Scheme - http
//			scheme = imageBrowserURLData1[0];
//			wiiscLog.log(wiiscLog.INFO, "Scheme: " + scheme);
//			
//			//Get the Host - tstextappvm.tstp8.com
//			//Check if the Image Browser URL has the Port Number
//			if (imageBrowserURLData1.length == 3)
//			{
//				String tempHost = "";
//				tempHost = imageBrowserURLData1[1];
//				host = tempHost.substring(2);
//				wiiscLog.log(wiiscLog.INFO, "Host: " + host);
//				//Get the Port and Path
//				imageBrowserURLData2 = imageBrowserURLData1[2].split("/");
//				port = imageBrowserURLData2[0];
//				portNum = Integer.parseInt(port);
//				wiiscLog.log(wiiscLog.INFO, "Port: " + port);
//				//Get the Path based on the remaining elements in imageBrowserURLData2
//				for (int i = 1; i < imageBrowserURLData2.length; i++)
//				{
//					path = path + "/" + imageBrowserURLData2[i];
//				}
//				wiiscLog.log(wiiscLog.INFO, "Path: " + path);
//			}
//			else
//			{
//				String tempHost = "";
//				tempHost = imageBrowserURLData1[1].substring(2); 
//				imageBrowserURLData2 = tempHost.split("/");
//				host = imageBrowserURLData2[0];
//				wiiscLog.log(wiiscLog.INFO, "Host: " + host);
//				//Get the Port based on if the scheme is http or https
//				if (scheme.equals("http"))
//				{
//					port = "80";
//					portNum = 80;
//				}
//				else if (scheme.equals("https"))
//				{
//					port = "443";
//					portNum = 443;
//				}
//				wiiscLog.log(wiiscLog.INFO, "Port: " + port);
//				//Get the Path based on the remaining elements in imageBrowserURLData2
//				for (int i = 1; i < imageBrowserURLData2.length; i++)
//				{
//					path = path + "/" + imageBrowserURLData2[i];
//				}
//				wiiscLog.log(wiiscLog.INFO, "Path: " + path);
//			}
//			
//			//Get the Authenticated Encoded String
//			authStringEnc = getAuthenticatedStringEnc(wiiscLog);
//			
//			//Create the HttpClient Object
//			httpclient = HttpClients.createDefault();
//			wiiscLog.log(wiiscLog.INFO, "Launching HttpClient");
//
//			//If Member, combine First and Last Name; Otherwise, use OrgName
//			String name = "";
//			if (docClass.equals("Member"))
//			{
//				name = firstName + " " + lastName;
//			}
//			else if (docClass.equals("Employer"))
//			{
//				name = orgName;
//			}
//			else
//			{
//				name = "";
//			}
//			//Build URI
//			URI uri = new URIBuilder()
//			.setScheme(scheme)
//			.setHost(host)
//			.setPort(portNum)
//			.setPath(path)            
//			.setParameter("docclass", docClass)
//			.setParameter("name", name)
//			.setParameter("value", appId)
//			.setParameter("privilege", privilege)
//			.build();
//
//			wiiscLog.log(wiiscLog.INFO, "Built URI");*/
//			
//			//Get the imagingBrowserRequestType GET or POST
//			String imagingBrowserRequestType = "";
//			imagingBrowserRequestType = globalConfig.getString("imagingBrowserRequestType");
//			
//			if (imagingBrowserRequestType.equals("GET"))
//			{
//				//HTTP GET
//				HttpGet httpget = new HttpGet(uri);
//				httpget.setHeader("Authorization", authStringEnc);
//				wiiscLog.log(wiiscLog.INFO, "Executing GET Request " + httpget.getRequestLine());
//								
//				//Custom Response Handler
//				ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
//
//					public String handleResponse(final HttpResponse httpResponse) throws ClientProtocolException, IOException {
//
//						int status = httpResponse.getStatusLine().getStatusCode();
//						if (status >= 200 && status < 300)
//						{
//							HttpEntity entity = httpResponse.getEntity();
//							return entity != null ? EntityUtils.toString(entity) : null;
//						}
//						else
//						{
//							System.out.println("ImagingManager -> getClaretyImageBrowserResponse() - Response Status was NOT > 200 and < 300");
//							throw new ClientProtocolException("Unexpected response status: " + status);
//						}
//					}
//				};
//
//				//HTTP GET for ResponseBody
//				responseBody = httpclient.execute(httpget, responseHandler);
//			}
//			else if (imagingBrowserRequestType.equals("POST"))
//			{
//				//HTTP POST
//				HttpPost httppost = new HttpPost(uri);
//				httppost.setHeader("Authorization", authStringEnc);
//				wiiscLog.log(wiiscLog.INFO, "Executing POST Request " + httppost.getRequestLine());
//				
//				//Custom Response Handler
//				ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
//
//					public String handleResponse(final HttpResponse httpResponse) throws ClientProtocolException, IOException {
//
//						int status = httpResponse.getStatusLine().getStatusCode();
//						if (status >= 200 && status < 300)
//						{
//							HttpEntity entity = httpResponse.getEntity();
//							return entity != null ? EntityUtils.toString(entity) : null;
//						}
//						else
//						{
//							System.out.println("ImagingManager -> getClaretyImageBrowserResponse() - Response Status was NOT > 200 and < 300");
//							throw new ClientProtocolException("Unexpected response status: " + status);
//						}
//					}
//				};
//
//				//HTTP POST for ResponseBody
//				responseBody = httpclient.execute(httppost, responseHandler);
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Imaging Browser Request was NOT GET or POST in the Global Config Properties file");
//				wiiscLog.log(wiiscLog.INFO, "Processing will end");
//			}
//		}
//		catch(Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			responseBody = "";
//		}
//		finally
//		{
//			if (httpclient != null)
//			{
//				try {
//					httpclient.close();
//				} catch (IOException e) {
//					wiiscLog.log(wiiscLog.ERROR, "Exception Closing HttpClient");
//					wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//				}
//			}
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getClaretyImageBrowserResponse()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return responseBody;
//	}
//	
//	public String getImageBrowserURL(String docClass, String appId, WIISCLog wiiscLog)
//	{
//		String imageBrowserURL = "";
//		//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getImageBrowserURL()");
//			//Get the Image Browser URL from Global Config
//			imageBrowserURL = globalConfig.getString("imagingBrowserURL");
//			
//			//Do Something with the URL and the Parameters
//			
//			
//			wiiscLog.log(wiiscLog.INFO, "imagingBrowserURL: " + imageBrowserURL);
//		}
//		catch(Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			imageBrowserURL = "";
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getImageBrowserURL()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return imageBrowserURL;
//	}
//	
//	public String getClaretyImageBrowserURL(String docClass, String appId, String firstName, String lastName, String orgName, String privilege, WIISCLog wiiscLog)
//	{
//		String imageBrowserURL = "";
//		String tempImageBrowserURL = "";
//		String imageBrowserURLParams = "";
//		String[] imageBrowserURLParamsData = null;
//		//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getClaretyImageBrowserURL()");
//			
//			//Get the Clarety Image Browser URL from Global Config
//			tempImageBrowserURL = globalConfig.getString("claretyImagingBrowserURL");
//			
//			//Append the Parameters to the URL
//			if (globalConfig.containsKey("claretyImagingBrowserURLParameters"))
//			{
//				if (globalConfig.getString("claretyImagingBrowserURLParameters").length() == 0)
//				{
//					wiiscLog.log(wiiscLog.INFO, "tempImageBrowserURL: " + tempImageBrowserURL);
//					//Update the ImageBrowserURL
//					imageBrowserURL = tempImageBrowserURL;
//				}
//				else
//				{
//					//Update the tempImageBrowserURL
//					tempImageBrowserURL = tempImageBrowserURL + "?";
//					
//					imageBrowserURLParams = globalConfig.getString("claretyImagingBrowserURLParameters");
//					imageBrowserURLParamsData = imageBrowserURLParams.split(",");
//										
//					//Get the Member or Employer Name for the URL
//					String propName = "";
//					if (docClass.equals("Member"))
//					{
//						propName = firstName + " " + lastName;
//					}
//					else if (docClass.equals("Employer"))
//					{
//						propName = orgName;
//					}
//					else
//					{
//						//Do Nothing
//					}
//					
//					//Build the URL
//					for (int z = 0; z < imageBrowserURLParamsData.length; z++)
//					{
//						if (imageBrowserURLParamsData[z].equalsIgnoreCase("docClass"))
//						{
//							//Update the tempImageBrowserURL
//							tempImageBrowserURL = tempImageBrowserURL + imageBrowserURLParamsData[z] + "=" + docClass + "&";
//							//wiiscLog.log(wiiscLog.INFO, "tempImageBrowserURL: " + tempImageBrowserURL);
//						}
//						else if (imageBrowserURLParamsData[z].equalsIgnoreCase("privilege"))
//						{
//							//Update the tempImageBrowserURL
//							tempImageBrowserURL = tempImageBrowserURL + imageBrowserURLParamsData[z] + "=" + privilege + "&";
//							//wiiscLog.log(wiiscLog.INFO, "tempImageBrowserURL: " + tempImageBrowserURL);
//						}
//						else if (imageBrowserURLParamsData[z].equalsIgnoreCase("name"))
//						{
//							//Update the tempImageBrowserURL
//							tempImageBrowserURL = tempImageBrowserURL + imageBrowserURLParamsData[z] + "=" + propName + "&";
//							//wiiscLog.log(wiiscLog.INFO, "tempImageBrowserURL: " + tempImageBrowserURL);
//						}
//						else if (imageBrowserURLParamsData[z].equalsIgnoreCase("value"))
//						{
//							//Update the tempImageBrowserURL
//							tempImageBrowserURL = tempImageBrowserURL + imageBrowserURLParamsData[z] + "=" + appId;
//							//wiiscLog.log(wiiscLog.INFO, "tempImageBrowserURL: " + tempImageBrowserURL);
//						}
//					}
//					wiiscLog.log(wiiscLog.INFO, "tempImageBrowserURL: " + tempImageBrowserURL);
//					
//					//Update the ImageBrowserURL
//					imageBrowserURL = tempImageBrowserURL;
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "tempImageBrowserURL: " + tempImageBrowserURL);
//				//Update the ImageBrowserURL
//				imageBrowserURL = tempImageBrowserURL;
//			}
//						
//			wiiscLog.log(wiiscLog.INFO, "imagingBrowserURL: " + imageBrowserURL);
//		}
//		catch(Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			imageBrowserURL = "";
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getClaretyImageBrowserURL()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return imageBrowserURL;
//	}
//	
//	public String getImageBrowserURL(FnDocumentList fnDocumentList, WIISCLog wiiscLog)
//	{
//		String imageBrowserURL = "";
//		String tempImageBrowserURL = "";
//		String imageBrowserURLParams = "";
//		String[] imageBrowserURLParamsData = null;
//		//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getImageBrowserURL()");
//			//Get the Image Browser URL from Global Config
//			tempImageBrowserURL = globalConfig.getString("imagingBrowserURL");
//			
//			//Update the ImageBrowserURL
//			imageBrowserURL = tempImageBrowserURL;
//			//More to do later as needed
//			wiiscLog.log(wiiscLog.INFO, "imagingBrowserURL: " + imageBrowserURL);
//		}
//		catch(Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			imageBrowserURL = "";
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getImageBrowserURL()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return imageBrowserURL;
//	}
//	
//	//Get the External Clarety Image Browser URL
//	public String getClaretyImageBrowserURL(FnDocumentList fnDocumentList, WIISCLog wiiscLog)
//	{
//		//******************************************************************************************
//		//External Clarety Image Browser URL is for a specific URL with parameters to be redirected
//		//******************************************************************************************
//		//Example URL
//		//https://<Server>:<Port>/<AppName>/SearchDocument.jsp
//		String imageBrowserURL = "";
//		String tempImageBrowserURL = "";
//		String imageBrowserURLParams = "";
//		String[] imageBrowserURLParamsData = null;
//		//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getClaretyImageBrowserURL()");
//			//Get the Image Browser URL from Global Config
//			tempImageBrowserURL = globalConfig.getString("claretyImagingBrowserURL");
//			
//			//Get the Document List Info
//			List<FnDocument> fnDocList = fnDocumentList.getFnDocumentList();
//			for (int i = 0; i < fnDocList.size(); i++)
//			{
//				//Get the Document Object
//				FnDocument fnDocument = fnDocList.get(i);
//				
//				if (globalConfig.containsKey("claretyImagingBrowserURLParameters"))
//				{
//					if (globalConfig.getString("claretyImagingBrowserURLParameters").length() == 0)
//					{
//						wiiscLog.log(wiiscLog.INFO, "tempImageBrowserURL: " + tempImageBrowserURL);
//						//Update the ImageBrowserURL
//						imageBrowserURL = tempImageBrowserURL;
//					}
//					else
//					{
//						//Update the tempImageBrowserURL
//						tempImageBrowserURL = tempImageBrowserURL + "?";
//						
//						imageBrowserURLParams = globalConfig.getString("claretyImagingBrowserURLParameters");
//						imageBrowserURLParamsData = imageBrowserURLParams.split(",");
//						
//						//Get the Document Class
//						String docClass = fnDocument.getFnDocumentClass();
//						//Get the Document App ID
//						String appId = fnDocument.getFnDocumentAppId();
//						//Get the Document Status
//						String docStatus = fnDocument.getFnDocumentStatus();
//						
//						FnPropertyList fnProperties = fnDocument.getFnPropertyList();
//						List<FnProperty> fnPropsList = fnProperties.getFnDocumentPropsList();
//						//Get the Member or Employer Name for the URL
//						String propName = "";
//						for (int y = 0; y < fnPropsList.size(); y++)
//						{
//							FnProperty fnProperty = fnPropsList.get(y);
//							if (propName.length() == 0)
//							{
//								propName = fnProperty.getValue();
//							}
//							else
//							{
//								propName = propName + " " + fnProperty.getValue();
//							}
//						}
//						//Build the URL
//						for (int z = 0; z < imageBrowserURLParamsData.length; z++)
//						{
//							if (imageBrowserURLParamsData[z].equalsIgnoreCase("docClass"))
//							{
//								//Update the tempImageBrowserURL
//								tempImageBrowserURL = tempImageBrowserURL + imageBrowserURLParamsData[z] + "=" + docClass + "&";
//								//wiiscLog.log(wiiscLog.INFO, "tempImageBrowserURL: " + tempImageBrowserURL);
//							}
//							else if (imageBrowserURLParamsData[z].equalsIgnoreCase("privilege"))
//							{
//								//Update the tempImageBrowserURL
//								tempImageBrowserURL = tempImageBrowserURL + imageBrowserURLParamsData[z] + "=" + docStatus + "&";
//								//wiiscLog.log(wiiscLog.INFO, "tempImageBrowserURL: " + tempImageBrowserURL);
//							}
//							else if (imageBrowserURLParamsData[z].equalsIgnoreCase("name"))
//							{
//								//Update the tempImageBrowserURL
//								tempImageBrowserURL = tempImageBrowserURL + imageBrowserURLParamsData[z] + "=" + propName + "&";
//								//wiiscLog.log(wiiscLog.INFO, "tempImageBrowserURL: " + tempImageBrowserURL);
//							}
//							else if (imageBrowserURLParamsData[z].equalsIgnoreCase("value"))
//							{
//								//Update the tempImageBrowserURL
//								tempImageBrowserURL = tempImageBrowserURL + imageBrowserURLParamsData[z] + "=" + appId;
//								//wiiscLog.log(wiiscLog.INFO, "tempImageBrowserURL: " + tempImageBrowserURL);
//							}
//						}
//						wiiscLog.log(wiiscLog.INFO, "tempImageBrowserURL: " + tempImageBrowserURL);
//						//Update the ImageBrowserURL
//						imageBrowserURL = tempImageBrowserURL;
//					}
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "tempImageBrowserURL: " + tempImageBrowserURL);
//					//Update the ImageBrowserURL
//					imageBrowserURL = tempImageBrowserURL;
//				}
//				wiiscLog.log(wiiscLog.INFO, "imageBrowserURL: " + imageBrowserURL);
//			}
//		}
//		catch(Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			imageBrowserURL = "";
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getClaretyImageBrowserURL()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return imageBrowserURL;
//	}
//	
//	public String getAuthenticatedStringEnc(ResourceBundle globalConfig)
//	{
//		String authString = "";
//		//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//		String authStringEnc = "";
//		
//		try
//		{
//			//Get the Username and Password from Global Config to build the authString
//			authString = globalConfig.getString("imagingBrowserUsername") + ":" + globalConfig.getString("imagingBrowserPassword");
//			authStringEnc = "Basic " + new String(new Base64().encode(authString.getBytes()));
//		}
//		catch(Exception e)
//		{
//			System.out.println("Exception - getAuthenticatedStringEnc No WIISCLog");
//			e.printStackTrace();
//			authStringEnc = "";
//		}
//		return authStringEnc;
//	}
//	
//	public String getAuthenticatedStringEnc(WIISCLog wiiscLog)
//	{
//		String authString = "";
//		//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//		String authStringEnc = "";
//		
//		try
//		{
//			//Get the Username and Password from Global Config to build the authString
//			authString = globalConfig.getString("imagingBrowserUsername") + ":" + globalConfig.getString("imagingBrowserPassword");
//			authStringEnc = "Basic " + new String(new Base64().encode(authString.getBytes()));
//		}
//		catch(Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			authStringEnc = "";
//		}
//		return authStringEnc;
//	}
//	
//	public String initCMWorkflow(String appId, String planId, String caseId, String process, WIISCLog wiiscLog)
//	{
//		String Result = "";
//		
//		System.out.println("Entered ImagingManager -> initCMWorkflow()");
//		
//		//process is the Process Code of which Workflow to launch
//		//processParameters is the CSV list of ordered values to use for the Case Manager Workflow
//		//the ordered list is part of the properties file
//		
//		wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> initCMWorkflow()");
//		
//		//Call CMWorkflow to create the case, apply the properties and launch the Workflow
//		//Setup GlobalConfig for WorkflowManager
//		workflowManager.setGlobalConfig(getGlobalConfig());
//		Result = workflowManager.createCase(appId,planId,caseId,process,wiiscLog);
//		
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> initCMWorkflow()");
//		
//		System.out.println("Leaving ImagingManager -> initCMWorkflow()");
//		
//		return Result;
//	}
//	
//	public File[] exportDocuments(ObjectStore os, String docId, WIISCLog wiiscLog)
//	{
//		File[] files = null;
//		//File f = exportDocument(os, docId, wiiscLog);
//		//files = new File[1];
//		//files[0] = f;
//		
//		return files;
//	}
//	
//	public String getDocumentURL(String docId, WIISCLog wiiscLog, boolean loggingEnabled)
//	{
//		String docURL = "";
//		//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//		String docGUID = "";
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getDocumentURL()");
//			wiiscLog.log(wiiscLog.INFO, "DocId: " + docId);
//			
//			//CE Object Store
//			ObjectStore os = null;
//			String userId = globalConfig.getString(ConstantsUtil.CE_USER_ID);
//			String password = globalConfig.getString(ConstantsUtil.CE_USER_PASSWORD);
//
//			//Get CE Object Store
//			os = getObjectStore(userId, password, wiiscLog);
//
//			//Get the FileNet GUID
//			docGUID = getFNDocumentGUID(os, docId, wiiscLog);
//			wiiscLog.log(wiiscLog.INFO, "Found docGUID: " + docGUID);
//			//Reset docId
//			docId = "";
//
//			//Check docGUID to see if it has a value that needs to be used to replace docId
//			if (docGUID.length() > 0)
//			{
//				//Update the docId to use the GUID for Image Viewing
//				docId = docGUID;
//			}
//			
//			//Check to make sure docId has a value
//			if (docId.length() > 0)
//			{
//				//Check if viewDocImageViewerEnabled is Enabled
//				if (globalConfig.getString("viewDocImageViewerEnabled").equals("true"))
//				{
//					//viewDocImageViewerEnabled is True
//					//Check if viewDocImageViewerConvertToPDF is Enabled
//					if (globalConfig.getString("viewDocImageViewerConvertToPDF").equals("true"))
//					{
//						//viewDocImageViewerConvertToPDF is Enabled
//						//Prepare to Download the Image files
//						//File[] files = exportDocuments(os, docId, wiiscLog);
//						//File exportedFile = null;
//						String exportedFile = "";
//						//File outputFile = null;
//						String outputFile = "";
//						//Export File from FileNet and Get MIME Type
//						exportedFile = exportDocument(os, docId, wiiscLog);
//						//Convert the Exported File to PDF or Download
//						outputFile = convertFileToPDFOrDownload(exportedFile, wiiscLog);
//						
//						//Delete exportedFile if its not the same as the outputFile
//						if (!exportedFile.equals(outputFile))
//						{
//							File f = null;
//							f = new File(exportedFile);
//							f.delete();
//							f = null;
//						}
//						//Update the docURL						
//						if (outputFile.length() > 0)
//						{
//							docURL = "";
//						}
//						else
//						{
//							docURL = outputFile;
//						}
//					}
//					else
//					{
//						//Uses Builtin ViewDocument Viewer instead of COTS Viewer
//					}
//				}
//				else
//				{
//					//Create CE User Session Token for viewing documents
//					Session session = ObjectFactory.getSession("UserToken", null, globalConfig.getString("ceUserId"), globalConfig.getString("cePassword"));
//					session.setConfiguration(new FileInputStream(new File(globalConfig.getString("wiiscAppRootDirectory") + "/" + globalConfig.getString("wiiscConfigFileDirectory") + "/" + "WcmApiConfig.properties")));
//					//Generate a Token URL with Session ID
//					String token = session.getToken(false);
//					//Old Deprecated way
//					//token = URLEncoder.encode(token);
//					//New way - 10/19/14
//					token = URLEncoder.encode(token, "UTF-8");
//					
//					//Generate the DocURL to use the FileNet Viewer
//					//Without &impersonate=true
//					//docURL = globalConfig.getString("httpHeader") + "://" + globalConfig.getString("aeServerName") + ":" + globalConfig.getString("aeApplicationPort") + globalConfig.getString("aeApplicationContext") + "/getContent?" + globalConfig.getString("viewIDtype") + "=" + docId + "&objectStoreName=" + globalConfig.getString("objectstoreName") + "&objectType=document&ut=" + token;
//					//With &impersonate=true - found in an IBM RedBook
//					docURL = globalConfig.getString("httpHeader") + "://" + globalConfig.getString("aeServerName") + ":" + globalConfig.getString("aeApplicationPort") + globalConfig.getString("aeApplicationContext") + "/getContent?" + globalConfig.getString("viewIDtype") + "=" + docId + "&objectStoreName=" + globalConfig.getString("objectstoreName") + "&objectType=document&ut=" + token + "&impersonate=true";
//				}
//				
//				if (loggingEnabled)
//				{
//					wiiscLog.log(wiiscLog.INFO, "docURL: " + docURL);
//					wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getDocumentURL()");
//				}
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "ERROR geting the FileNet Document URL");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			docGUID = "";
//			//Reset docId
//			docId = "";
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getDocumentURL()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		//Return the URL
//		return docURL;
//	}
//	
//	//Get Document Property List
//	public FnDocument getDocumentInfo(String docID, WIISCLog wiiscLog)
//	{
//		//Create the FnDocument
//		FnDocument fnDocument = new FnDocument();
//		//FileNet Document ID
//		String fileNetDocumentID = "";
//		//Get an ObjectStore Object
//		ObjectStore os = null;
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getDocumentInfo()");
//			//Login to the Imaging Server
//			os = loginImaging(wiiscLog);
//
//			//Verify Object Store Connected
//			if (os != null)
//			{
//				//Check the Doc ID
//				if (docID != null)
//				{
//					//1 FileNet Document
//					Document document = null;
//					//Get a FileNet Document from the Request
//					wiiscLog.log(wiiscLog.INFO, "Get Document: " + docID);
//					
//					//Get the FileNet GUID from the passed in Document ID which may already be a GUID
//					//We still need to verify that its valid
//					fileNetDocumentID = getFNDocumentGUID(os, docID, wiiscLog);
//											
//					//Check to make sure the FileNet Document ID was found
//					if (fileNetDocumentID.length() > 0)
//					{
//						wiiscLog.log(wiiscLog.INFO, "FileNet Document GUID Found: " + fileNetDocumentID);
//						//FileNet Document for the Document ID
//						document = Factory.Document.fetchInstance(os, new Id(fileNetDocumentID), null);
//						
//						//Verify the Document was found
//						if (document != null)
//						{
//							wiiscLog.log(wiiscLog.INFO, "Document Found");
//							String docClassName = document.getClassName();
//							
//							//Get the Document Properties Template to use
//							String[] propertiesData = null;
//							propertiesData = getDocumentPropertiesTemplate(os, docClassName, wiiscLog);
//							
//							//Get the FileNet Document Info
//							fnDocument = getDocument(document, propertiesData, wiiscLog);
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "Document was NOT Found");
//						}
//					}
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Imaging Login FAILED, Imaging Server may be unavailable.");
//				//Update the FnDocument Object
//				fnDocument.setErrorFlag(1);
//				//Update the ErrorMessage
//				fnDocument.setErrorMessage("Imaging Login FAILED, Imaging Server may be unavailable.");
//			}
//			//Reset Object Store variable
//			os = null;
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnDocument.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnDocument.setErrorMessage(e.getMessage());
//			//Reset Object Store variable
//			os = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getDocumentInfo()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnDocument;
//	}
//	
//	//Delete Document
//	public FnDocument deleteDocument(FnDocument fnDocumentRequest, WIISCLog wiiscLog)
//	{
//		//Create the FnDocument
//		FnDocument fnDocument = new FnDocument();
//		//FileNet Document ID
//		String fileNetDocumentID = "";
//		//Get an ObjectStore Object
//		ObjectStore os = null;
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> deleteDocument()");
//			//Login to the Imaging Server
//			os = loginImaging(wiiscLog);
//
//			//Verify Object Store Connected
//			if (os != null)
//			{
//				//Document ID
//				String fnDocumentID = fnDocumentRequest.getFnDocumentID();
//				
//				//Check if Document GUID ID was passed in the request or a Search is needed based on Properties
//				if (fnDocumentID != null)
//				{
//					//1 FileNet Document
//					Document document = null;
//					
//					wiiscLog.log(wiiscLog.INFO, "Get Document: " + fnDocumentID);
//					
//					//Get the FileNet GUID from the passed in Document ID which may already be a GUID
//					//We still need to verify that its valid
//					fileNetDocumentID = getFNDocumentGUID(os, fnDocumentID, wiiscLog);
//											
//					//Check to make sure the FileNet Document ID was found
//					if (fileNetDocumentID.length() > 0)
//					{
//						wiiscLog.log(wiiscLog.INFO, "FileNet Document GUID Found: " + fileNetDocumentID);
//						//FileNet Document for the Document ID
//						document = Factory.Document.fetchInstance(os, new Id(fileNetDocumentID), null);
//						
//						//Verify the Document was found and perform Delete
//						if (document != null)
//						{
//							wiiscLog.log(wiiscLog.INFO, "FileNet Document Found");
//							//Delete FileNet Document
//							if (deleteDocument(document, wiiscLog))
//							{
//								//Document Deleted Successfully
//								fnDocument.setErrorFlag(0);
//							}
//							else
//							{
//								//Document Failed to Delete
//								fnDocument.setErrorFlag(1);
//							}
//														
//							//Check if the Error Flag was set
//							if (fnDocument.getErrorFlag() > 0)
//							{
//								//Process Error
//								wiiscLog.log(wiiscLog.INFO, "Document failed to delete in the Object Store");
//								wiiscLog.log(wiiscLog.INFO, "===========================================================");
//								//Update the Document Status
//								fnDocument.setFnDocumentStatus("Document failed to delete in the Object Store");
//								fnDocument.setErrorFlag(1);
//								fnDocument.setErrorMessage("Document failed to delete in the Object Store");
//							}
//							else
//							{
//								//No Error
//								wiiscLog.log(wiiscLog.INFO, "Document successfully deleted in the Object Store");
//								wiiscLog.log(wiiscLog.INFO, "===========================================================");
//								//Update the Document Status
//								fnDocument.setFnDocumentStatus("Document successfully deleted in the Object Store");
//								fnDocument.setErrorFlag(0);
//								fnDocument.setErrorMessage("Document successfully deleted in the Object Store");
//							}
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "FileNet Document was NOT Found");
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "FileNet Document GUID was NOT Found");
//					}
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "Document ID was not in the request, so a Document Search will be performed");
//					//Temp FnDocumentList for ANY Documents Updated that matched the Document Search and were updated
//					FnDocumentList tempFnDocumentList = new FnDocumentList();
//					//Document Search based on properties in the Request and returns Document List
//					tempFnDocumentList = searchAndDeleteDocuments(os, fnDocumentRequest, wiiscLog);
//					//Check the size of FnDocumentList
//					if (tempFnDocumentList.getFnDocumentList().size() > 0)
//					{
//						//Get FnDocument from FnDocumentList
//						for (int i = 0; i < tempFnDocumentList.getFnDocumentList().size(); i++)
//						{
//							//Get the FnDocument
//							fnDocument = tempFnDocumentList.getFnDocumentList().get(i);
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "FileNet Document was NOT Found");
//					}
//				}	
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Imaging Login FAILED, Imaging Server may be unavailable.");
//				//Update the FnDocument Object
//				fnDocument.setErrorFlag(1);
//				//Update the ErrorMessage
//				fnDocument.setErrorMessage("Imaging Login FAILED, Imaging Server may be unavailable.");
//			}
//			//Reset Object Store variable
//			os = null;
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnDocument.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnDocument.setErrorMessage(e.getMessage());
//			//Reset Object Store variable
//			os = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> deleteDocument()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnDocument;
//	}
//	
//	//Delete Documents
//	public FnDocumentList deleteDocuments(FnDocumentList fnDocumentListRequest, WIISCLog wiiscLog)
//	{
//		//Create the FnDocumentList
//		FnDocumentList fnDocumentList = new FnDocumentList();
//		//FileNet Document ID
//		String fileNetDocumentID = "";
//		//Get an ObjectStore Object
//		ObjectStore os = null;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> deleteDocuments()");
//			//Login to the Imaging Server
//			os = loginImaging(wiiscLog);
//
//			//Verify Object Store Connected
//			if (os != null)
//			{
//				//Loop through the FnDocumentListRequest
//				for (int i = 0; i < fnDocumentListRequest.getFnDocumentList().size(); i++)
//				{
//					//1 FnDocument
//					FnDocument fnDocumentRequest = fnDocumentListRequest.getFnDocumentList().get(i);
//					//Document ID
//					String fnDocumentID = fnDocumentRequest.getFnDocumentID();
//					
//					//Check if Document GUID ID was passed in the request or a Search is needed based on Properties
//					if (fnDocumentID != null)
//					{
//						//1 FileNet Document
//						Document document = null;
//						
//						wiiscLog.log(wiiscLog.INFO, "Get Document: " + fnDocumentID);
//						
//						//Get the FileNet GUID from the passed in Document ID which may already be a GUID
//						//We still need to verify that its valid
//						fileNetDocumentID = getFNDocumentGUID(os, fnDocumentID, wiiscLog);
//												
//						//Check to make sure the FileNet Document ID was found
//						if (fileNetDocumentID.length() > 0)
//						{
//							wiiscLog.log(wiiscLog.INFO, "FileNet Document GUID Found: " + fileNetDocumentID);
//							//FileNet Document for the Document ID
//							document = Factory.Document.fetchInstance(os, new Id(fileNetDocumentID), null);
//							
//							//Verify the Document was found and perform Indexing
//							if (document != null)
//							{
//								wiiscLog.log(wiiscLog.INFO, "FileNet Document Found");
//								//Create the FnDocument
//								FnDocument fnDocument = new FnDocument();
//								//Delete FileNet Document
//								if (deleteDocument(document, wiiscLog))
//								{
//									//Document Deleted Successfully
//									fnDocument.setErrorFlag(0);
//								}
//								else
//								{
//									//Document Failed to Delete
//									fnDocument.setErrorFlag(1);
//								}
//								
//								//Check if the Error Flag was set
//								if (fnDocument.getErrorFlag() > 0)
//								{
//									//Process Error
//									wiiscLog.log(wiiscLog.INFO, "Document failed to delete in the Object Store");
//									wiiscLog.log(wiiscLog.INFO, "===========================================================");
//									//Update the Document Status
//									fnDocument.setFnDocumentStatus("Document failed to delete in the Object Store");
//									fnDocument.setErrorFlag(1);
//									fnDocument.setErrorMessage("Document failed to delete in the Object Store");
//								}
//								else
//								{
//									//No Error
//									wiiscLog.log(wiiscLog.INFO, "Document successfully deleted in the Object Store");
//									wiiscLog.log(wiiscLog.INFO, "===========================================================");
//									//Update the Document Status
//									fnDocument.setFnDocumentStatus("Document successfully deleted in the Object Store");
//									fnDocument.setErrorFlag(0);
//									fnDocument.setErrorMessage("Document successfully deleted in the Object Store");
//								}
//								
//								//Add the FnDocument to the FnDocumentList
//								fnDocumentList.addFnDocument(fnDocument);
//							}
//							else
//							{
//								wiiscLog.log(wiiscLog.INFO, "FileNet Document was NOT Found");
//							}
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "FileNet Document GUID was NOT Found");
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "Document ID was not in the request, so a Document Search will be performed");
//						//Temp FnDocumentList for ANY Documents Updated that matched the Document Search and were updated
//						FnDocumentList tempFnDocumentList = new FnDocumentList();
//						//Document Search based on properties in the Request and returns Document List
//						tempFnDocumentList = searchAndDeleteDocuments(os, fnDocumentRequest, wiiscLog);
//						//Check the size of FnDocumentList
//						if (tempFnDocumentList.getFnDocumentList().size() > 0)
//						{
//							//Add the Temp FnDocumentList to the FnDocumentList
//							fnDocumentList.addFnDocumentList(tempFnDocumentList);
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "NO FileNet Documents Found");
//						}
//					}
//				}//For Loop
//				
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Imaging Login FAILED, Imaging Server may be unavailable.");
//				//Update the fnDocumentList Object
//				fnDocumentList.setErrorFlag(1);
//				//Update the ErrorMessage
//				fnDocumentList.setErrorMessage("Imaging Login FAILED, Imaging Server may be unavailable.");
//			}
//			//Reset Object Store variable
//			os = null;
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnDocumentList.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnDocumentList.setErrorMessage(e.getMessage());
//			//Reset Object Store variable
//			os = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> deleteDocuments()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnDocumentList;
//	}
//	
//	//Update Document Properties - reindexing function
//	public FnDocument updateDocument(FnDocument fnDocumentRequest, WIISCLog wiiscLog)
//	{
//		//Create the FnDocument
//		FnDocument fnDocument = new FnDocument();
//		//FileNet Document ID
//		String fileNetDocumentID = "";
//		//Get an ObjectStore Object
//		ObjectStore os = null;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> updateDocument()");
//			//Login to the Imaging Server
//			os = loginImaging(wiiscLog);
//
//			//Verify Object Store Connected
//			if (os != null)
//			{
//				//Document ID
//				String fnDocumentID = fnDocumentRequest.getFnDocumentID();
//
//				//Check if Document GUID ID was passed in the request or a Search is needed based on Properties
//				if (fnDocumentID != null)
//				{
//					//1 FileNet Document
//					Document document = null;
//
//					wiiscLog.log(wiiscLog.INFO, "Get Document: " + fnDocumentID);
//
//					//Get the FileNet GUID from the passed in Document ID which may already be a GUID
//					//We still need to verify that its valid
//					fileNetDocumentID = getFNDocumentGUID(os, fnDocumentID, wiiscLog);
//
//					//Check to make sure the FileNet Document ID was found
//					if (fileNetDocumentID.length() > 0)
//					{
//						wiiscLog.log(wiiscLog.INFO, "FileNet Document GUID Found: " + fileNetDocumentID);
//						//FileNet Document for the Document ID
//						document = Factory.Document.fetchInstance(os, new Id(fileNetDocumentID), null);
//
//						//Verify the Document was found and perform Indexing
//						if (document != null)
//						{
//							wiiscLog.log(wiiscLog.INFO, "FileNet Document Found");
//							//Process FileNet Document with IndexDocument Method
//							fnDocument = indexDocument(document, fnDocumentRequest, wiiscLog);
//
//							//Check if the Error Flag was set
//							if (fnDocument.getErrorFlag() > 0)
//							{
//								//Process Error - report bad indexing
//								wiiscLog.log(wiiscLog.INFO, "Document failed to update in the Object Store");
//								wiiscLog.log(wiiscLog.INFO, "===========================================================");
//								//Update the Document Status
//								fnDocument.setFnDocumentStatus("Document failed to update in the Object Store");
//								fnDocument.setErrorFlag(1);
//								fnDocument.setErrorMessage("Document failed to update in the Object Store");
//							}
//							else
//							{
//								//No Error
//								wiiscLog.log(wiiscLog.INFO, "Document successfully updated in the Object Store");
//								wiiscLog.log(wiiscLog.INFO, "===========================================================");
//								//Update the Document Status
//								fnDocument.setFnDocumentStatus("Document successfully updated in the Object Store");
//								fnDocument.setErrorFlag(0);
//								fnDocument.setErrorMessage("Document successfully updated in the Object Store");
//							}
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "FileNet Document was NOT Found");
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "FileNet Document GUID was NOT Found");
//					}
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "Document ID was not in the request, so a Document Search will be performed");
//					//Temp FnDocumentList for ANY Documents Updated that matched the Document Search and were updated
//					FnDocumentList tempFnDocumentList = new FnDocumentList();
//					//Document Search based on properties in the Request and Re-Index each one found
//					tempFnDocumentList = searchAndUpdateDocuments(os, fnDocumentRequest, wiiscLog);
//					//Check the size of FnDocumentList
//					if (tempFnDocumentList.getFnDocumentList().size() > 0)
//					{
//						//Get FnDocument from FnDocumentList
//						for (int i = 0; i < tempFnDocumentList.getFnDocumentList().size(); i++)
//						{
//							//Get the FnDocument
//							fnDocument = tempFnDocumentList.getFnDocumentList().get(i);
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "FileNet Document was NOT Found");
//					}
//				}	
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Imaging Login FAILED, Imaging Server may be unavailable.");
//				//Update the FnDocument Object
//				fnDocument.setErrorFlag(1);
//				//Update the ErrorMessage
//				fnDocument.setErrorMessage("Imaging Login FAILED, Imaging Server may be unavailable.");
//			}
//			//Reset Object Store variable
//			os = null;
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnDocument.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnDocument.setErrorMessage(e.getMessage());
//			//Reset Object Store variable
//			os = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> updateDocument()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnDocument;
//	}
//
//	//Update Documents Properties - reindexing function
//	public FnDocumentList updateDocuments(FnDocumentList fnDocumentListRequest, WIISCLog wiiscLog)
//	{
//		//Create the FnDocumentList
//		FnDocumentList fnDocumentList = new FnDocumentList();
//		//FileNet Document ID
//		String fileNetDocumentID = "";
//		//Get an ObjectStore Object
//		ObjectStore os = null;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> updateDocuments()");
//			//Login to the Imaging Server
//			os = loginImaging(wiiscLog);
//
//			//Verify Object Store Connected
//			if (os != null)
//			{
//				//Loop through the FnDocumentListRequest
//				for (int i = 0; i < fnDocumentListRequest.getFnDocumentList().size(); i++)
//				{
//					//1 FnDocument
//					FnDocument fnDocumentRequest = fnDocumentListRequest.getFnDocumentList().get(i);
//					//Document ID
//					String fnDocumentID = fnDocumentRequest.getFnDocumentID();
//
//					//Check if Document GUID ID was passed in the request or a Search is needed based on Properties
//					if (fnDocumentID != null)
//					{
//						//1 FileNet Document
//						Document document = null;
//
//						wiiscLog.log(wiiscLog.INFO, "Get Document: " + fnDocumentID);
//
//						//Get the FileNet GUID from the passed in Document ID which may already be a GUID
//						//We still need to verify that its valid
//						fileNetDocumentID = getFNDocumentGUID(os, fnDocumentID, wiiscLog);
//
//						//Check to make sure the FileNet Document ID was found
//						if (fileNetDocumentID.length() > 0)
//						{
//							wiiscLog.log(wiiscLog.INFO, "FileNet Document GUID Found: " + fileNetDocumentID);
//							//FileNet Document for the Document ID
//							document = Factory.Document.fetchInstance(os, new Id(fileNetDocumentID), null);
//
//							//Verify the Document was found and perform Indexing
//							if (document != null)
//							{
//								wiiscLog.log(wiiscLog.INFO, "FileNet Document Found");
//								//Process 1 FileNet Document with IndexDocument Method
//								FnDocument fnDocument = indexDocument(document, fnDocumentRequest, wiiscLog);
//
//								//Check if the Error Flag was set
//								if (fnDocument.getErrorFlag() > 0)
//								{
//									//Process Error - report bad indexing
//									wiiscLog.log(wiiscLog.INFO, "Document failed to update in the Object Store");
//									wiiscLog.log(wiiscLog.INFO, "===========================================================");
//									//Update the Document Status
//									fnDocument.setFnDocumentStatus("Document failed to update in the Object Store");
//									fnDocument.setErrorFlag(1);
//									fnDocument.setErrorMessage("Document failed to update in the Object Store");
//								}
//								else
//								{
//									//No Error
//									wiiscLog.log(wiiscLog.INFO, "Document successfully updated in the Object Store");
//									wiiscLog.log(wiiscLog.INFO, "===========================================================");
//									//Update the Document Status
//									fnDocument.setFnDocumentStatus("Document successfully updated in the Object Store");
//									fnDocument.setErrorFlag(0);
//									fnDocument.setErrorMessage("Document successfully updated in the Object Store");
//								}
//
//								//Add the FnDocument to the FnDocumentList
//								fnDocumentList.addFnDocument(fnDocument);
//							}
//							else
//							{
//								wiiscLog.log(wiiscLog.INFO, "FileNet Document was NOT Found");
//							}
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "FileNet Document GUID was NOT Found");
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "Document ID was not in the request, so a Document Search will be performed");
//						//Temp FnDocumentList for ANY Documents Updated that matched the Document Search and were updated
//						FnDocumentList tempFnDocumentList = new FnDocumentList();
//						//Document Search based on properties in the Request and Re-Index each one found
//						tempFnDocumentList = searchAndUpdateDocuments(os, fnDocumentRequest, wiiscLog);
//						//Check the size of FnDocumentList
//						if (tempFnDocumentList.getFnDocumentList().size() > 0)
//						{
//							//Add the Temp FnDocumentList to the FnDocumentList
//							fnDocumentList.addFnDocumentList(tempFnDocumentList);
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "NO FileNet Documents Found");
//						}
//					}
//				}
//
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Imaging Login FAILED, Imaging Server may be unavailable.");
//				//Update the fnDocumentList Object
//				fnDocumentList.setErrorFlag(1);
//				//Update the ErrorMessage
//				fnDocumentList.setErrorMessage("Imaging Login FAILED, Imaging Server may be unavailable.");
//			}
//			//Reset Object Store variable
//			os = null;
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnDocumentList.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnDocumentList.setErrorMessage(e.getMessage());
//			//Reset Object Store variable
//			os = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> updateDocuments()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnDocumentList;
//	}
//	
//	//Import files and XMLs are locally specified in the GlobalConfig Properties file - folder to import is a parameter
//	public FnDocumentList importDocumentsLocal(String importFolder, WIISCLog wiiscLog)
//	{
//		//Create the FnDocumentList
//		FnDocumentList fnDocumentList = new FnDocumentList();
//		
//		//More To Do
//		
//		return fnDocumentList;
//	}
//	
//	public FnDocumentList importDocuments(FnDocumentList fnDocumentListRequest, WIISCLog wiiscLog)
//	{
//		//Create the FnDocumentList
//		FnDocumentList fnDocumentList = new FnDocumentList();
//		//Get an ObjectStore Object
//		ObjectStore os = null;
//		//Boolean check for if Document is valid
//		boolean docReady = false;
//		//Boolean for Document Stored success or failed
//		boolean docStored = false;
//		String existingErrors = "";
//				
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> importDocuments()");
//
//			//Check the Document List Request to make sure its valid
//			if (fnDocumentListRequest.getCount() > 0)
//			{
//				//Login to the Imaging Server
//				os = loginImaging(wiiscLog);
//
//				//Verify Object Store Connected
//				if (os != null)
//				{
//					//Define the Doc Class Properties Resource
//					//ResourceBundle docClassConfigProps = null;
//								
//					//Loop through and process Documents from the Document List
//					//for (int i = 0; i < fnDocumentListRequest.getFnDocumentList().size(); i++)
//					for (FnDocument fnDocumentRequest : fnDocumentListRequest.getFnDocumentList())
//					{
//						//Get 1 FnDocument from the Request
//						//FnDocument fnDocumentRequest = new FnDocument();
//						//fnDocumentRequest = fnDocumentListRequest.getFnDocumentList().get(i);
//						
//						//Create 1 FnDocument for the Result
//						FnDocument fnDocument = new FnDocument();
//						fnDocument.setFnDocumentName(fnDocumentRequest.getFnDocumentName());
//						
//						//Debug FnDocument
//						//outputFnDocument(fnDocumentRequest, wiiscLog);
//						
//						//Document Class
//						String docClassName = "";
//						docClassName = fnDocumentRequest.getFnDocumentClass();
//						
//						//Check for Custom Doc Properties File
//						if (checkPropertiesFileExist(docClassName + "Config", wiiscLog))
//						{
//							//docClassConfigProps = ResourceBundle.getBundle(docClassName + "Config");
//							if (docClassConfig == null)
//							{
//								LocalResource resConfig = getLocalResource(docClassName + "Config");
//								docClassConfig = resConfig.getLocalBundle(resConfig.getBundlePath(), resConfig.getBundleFile());
//							}
//						}
//						
//						//Check the Date Fields format to see if they are correct
//						if (docClassConfig.containsKey("docClassDateFields") && docClassConfig.containsKey("docClassDateFieldsFormat"))
//						{
//							wiiscLog.log(wiiscLog.INFO, "Verifying Date Fields for " + fnDocument.getFnDocumentName());
//							
//							String docClassDateFields = "";
//							String docClassDateFieldsFormat = "";
//							String[] docClassDateFieldsData = null;
//							
//							docClassDateFields = docClassConfig.getString("docClassDateFields");
//							docClassDateFieldsFormat = docClassConfig.getString("docClassDateFieldsFormat");
//							docClassDateFieldsData = docClassDateFields.split(",");
//							
//							//FnPropertyList
//							FnPropertyList requestPropertyList = new FnPropertyList();
//							requestPropertyList = fnDocumentRequest.getFnPropertyList();
//							
//							//Boolean to make sure Fields are Good
//							boolean fieldFound = false;
//							boolean fieldsGood = false;
//							String fieldName = "";
//							//Loop through fnDocumentRequest to check the Date fields
//							for (FnProperty requestProp : requestPropertyList.getFnDocumentPropsList())
//							{
//								//wiiscLog.log(wiiscLog.INFO, "Checking Field " + requestProp.getName());
//								for (String oneField : docClassDateFieldsData)
//								{
//									//wiiscLog.log(wiiscLog.INFO, "Checking Date Field " + oneField);
//									fieldName = "";
//									//Check to see if we have a Date field to check
//									if (requestProp.getName().equals(oneField))
//									{
//										fieldFound = true;
//										//Check Date Field
//										fieldsGood = checkDateField(requestProp.getValue(), docClassDateFieldsFormat, wiiscLog);
//										//Get the Field Name
//										fieldName = oneField;
//										break;
//									}
//									else
//									{
//										fieldsGood = false;
//									}
//								}
//								//Check Boolean to see if the Field was found
//								if (fieldFound)
//								{
//									//Reset
//									fieldFound = false;
//									//Check Boolean to see if Date field was bad
//									if (fieldsGood)
//									{
//										wiiscLog.log(wiiscLog.INFO, "The " + fieldName + " Field had a Valid Date Format");
//										docReady = true;
//										fieldsGood = false;
//									}
//									else
//									{
//										wiiscLog.log(wiiscLog.INFO, "The " + fieldName + " Field had an Invalid Date Format");
//										docReady = false;
//										break;
//									}
//								}
//								else
//								{
//									fieldFound = false;
//								}
//							}
//						}
//						else
//						{
//							//No Date Fields to check and no further validation needed.
//							//Document is assumed to be ready for import
//							docReady = true;
//						}
//						
//						//Document is Ready and Valid
//						if (docReady)
//						{
//							//Process 1 FnDocumentRequest with ImportDocument Method
//							//fnDocument = importDocument(os, fnDocumentRequest, wiiscLog);
//							Document document = importDocument(os, fnDocumentRequest, wiiscLog);
//							//Check if the Error Flag was set to 1
//							if (document == null)
//							{
//								//Update the Document Status
//								fnDocument.setFnDocumentStatus(fnDocument.getFnDocumentName() + " Failed to import in the Object Store.");
//								fnDocument.setErrorFlag(1);
//								fnDocument.setErrorMessage(fnDocument.getFnDocumentName() + " Failed to import in the Object Store.");
//								//Add the FnDocument to the FnDocumentList
//								fnDocumentList.addFnDocument(fnDocument);
//								//Update the Document List Error Flag
//								fnDocumentList.setErrorFlag(1);
//								//Update the ErrorMessage
//								//fnDocumentList.setErrorMessage(fnDocument.getFnDocumentStatus());
//							}
//							else
//							{
//								//Index the Document
//								//Process 1 FnDocument with IndexDocument Method
//								fnDocument = indexDocument(document, fnDocumentRequest, wiiscLog);
//								//Check if the Error Flag was set to 1
//								if (fnDocument.getErrorFlag() == 1)
//								{
//									//Process Error - report bad indexing
//									wiiscLog.log(wiiscLog.INFO, "Document failed to index in the Object Store");
//									wiiscLog.log(wiiscLog.INFO, "===========================================================");
//									//Delete the Document
//									document.delete();
//						        	document.save(RefreshMode.NO_REFRESH);
//						        	document = null;
//						        	wiiscLog.log(wiiscLog.INFO, "Document Object has been deleted.");
//									
//									//Update the Document Status
//									fnDocument.setFnDocumentStatus(fnDocument.getFnDocumentName() + " Failed to index in the Object Store.");
//									fnDocument.setErrorFlag(1);
//									fnDocument.setErrorMessage(fnDocument.getFnDocumentName() + " Failed to index in the Object Store.");
//									//Add the FnDocument to the FnDocumentList
//									fnDocumentList.addFnDocument(fnDocument);
//									//Update the Document List Error Flag
//									fnDocumentList.setErrorFlag(1);
//									//Update the ErrorMessage
//									//fnDocumentList.setErrorMessage(fnDocument.getFnDocumentName() + " Failed to index in the Object Store");
//								}
//								else
//								{
//									//Check if the FnDocument has a property value for the Document Location
//									//Document Location Property
//									String docLocationProp = "";
//									//Document Location Value
//									String docLocation = "";
//									docLocationProp = globalConfig.getString("documentPropFiledFolderLocation");
//									if (docLocationProp.length() > 0)
//									{
//										//Set docLocation equal to docLocationProp value
//										docLocation = getCustomFnPropertyValue(fnDocument, docLocationProp, wiiscLog);
//									}
//									else
//									{
//										//No value so default to ""
//										docLocation = "";
//									}
//									//Store and File the Document
//									docStored = storeDocument(os, document, fnDocument, docLocation, wiiscLog);
//									//Check if the Document Stored successfully
//									if (docStored)
//									{
//										wiiscLog.log(wiiscLog.INFO, "Document successful, adding to the Document List");
//										wiiscLog.log(wiiscLog.INFO, "===========================================================");
//										//Update the Document Status
//										fnDocument.setFnDocumentStatus(fnDocument.getFnDocumentName() + " Successfully stored in the Object Store.");
//										fnDocument.setErrorFlag(0);
//										//Update the ErrorMessage
//										fnDocument.setErrorMessage("");
//										//Add the FnDocument to the FnDocumentList
//										fnDocumentList.addFnDocument(fnDocument);
//										//Update the errorFlag to 0 to avoid it being set as an Error anywhere else.
//										//fnDocumentList.setErrorFlag(0);
//									}
//									else
//									{
//										wiiscLog.log(wiiscLog.INFO, "Document failed to store in the Object Store");
//										wiiscLog.log(wiiscLog.INFO, "===========================================================");
//										//Update the Document Status
//										fnDocument.setFnDocumentStatus(fnDocument.getFnDocumentName() + " Failed to store in the Object Store.");
//										fnDocument.setErrorFlag(1);
//										//Update the ErrorMessage
//										fnDocument.setErrorMessage(fnDocument.getFnDocumentName() + " Failed to store in the Object Store.");
//										//Add the FnDocument to the FnDocumentList
//										fnDocumentList.addFnDocument(fnDocument);
//										//Update the Document List Error Flag
//										fnDocumentList.setErrorFlag(1);
//										//Update the ErrorMessage
//										//fnDocumentList.setErrorMessage(fnDocument.getFnDocumentName() + " Failed to store in the Object Store");
//									}
//								}
//							}
//						}//DocReady is True
//						else
//						{
//							//Update the Document Status
//							fnDocument.setFnDocumentStatus(fnDocument.getFnDocumentName() + " Failed validation checks and will not be imported.");
//							fnDocument.setErrorFlag(1);
//							fnDocument.setErrorMessage(fnDocument.getFnDocumentName() + " Failed validation checks and will not be imported.");
//							//Add the FnDocument to the FnDocumentList
//							fnDocumentList.addFnDocument(fnDocument);
//							//Update the Document List Error Flag
//							fnDocumentList.setErrorFlag(1);
//							//Update the ErrorMessage
//							//fnDocumentList.setErrorMessage(fnDocument.getFnDocumentStatus());
//						}
//					}//End For
//					//Verify the Doc List is not empty
//					if (fnDocumentList.getCount() > 0)
//					{
//						//Check if the Document List had any Errors
//						if (fnDocumentList.getErrorFlag() > 0)
//						{
//							//Output Total Documents Imported
//							wiiscLog.log(wiiscLog.INFO, "Some Documents Failed to Import, review the logs for further information");
//							//Update the errorFlag to 1 to avoid it being set as an Error anywhere else.
//							//fnDocumentList.setErrorFlag(1);
//						}
//						else
//						{
//							//Output Total Documents Imported
//							wiiscLog.log(wiiscLog.INFO, "Total Documents Imported: " + fnDocumentList.getCount());
//							//Update the errorFlag to 0 to avoid it being set as an Error anywhere else.
//							//fnDocumentList.setErrorFlag(0);
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "No Documents were Imported.");
//						//Update the errorFlag to 1 to reflect that the Documents Count was 0 signaling something is wrong
//						fnDocumentList.setErrorFlag(1);
//						//Update the ErrorMessage
//						fnDocumentList.setErrorMessage("No Documents were Imported.");
//					}
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "Imaging Login FAILED, Imaging Server may be unavailable.");
//					//Update the fnDocumentList Object
//					fnDocumentList.setErrorFlag(1);
//					//Update the ErrorMessage
//					fnDocumentList.setErrorMessage("Imaging Login FAILED, Imaging Server may be unavailable.");
//				}
//				//Reset Object Store variable
//				os = null;
//			}//End Valid Document List Request
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Import Documents Request XML was not valid.");
//				//Update the errorFlag to 1 to reflect that the Documents Count was 0 signaling something is wrong
//				fnDocumentList.setErrorFlag(1);
//				//Update the ErrorMessage
//				fnDocumentList.setErrorMessage("Import Documents Request XML was not valid.");
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnDocumentList.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnDocumentList.setErrorMessage(e.getMessage());
//			//Reset Object Store variable
//			os = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> importDocuments()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnDocumentList;
//	}
//	
//	
//	
//	//******************************************************
//	//Private Helper Methods
//	//******************************************************
//	private FnPropertyList getDocClassSystemPropertiesFromConfig(WIISCLog wiiscLog)
//	{
//		//Create the FnPropertyList
//		FnPropertyList fnPropertyList = new FnPropertyList();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getDocClassSystemPropertiesFromConfig()");
//			//Integer for the Property Count to use from the Doc Class Config Properties file
//			int propCount = 1;
//			//Get Doc Class System Properties
//			while (docClassConfig.containsKey("prop" + propCount))
//			{
//				FnProperty fnProperty = new FnProperty();
//				fnProperty.setName(docClassConfig.getString("prop" + propCount));
//				fnPropertyList.addFnProperty(fnProperty);
//				propCount++;
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnPropertyList.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnPropertyList.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getDocClassSystemPropertiesFromConfig()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnPropertyList;
//	}
//	
//	private FnPropertyList getCustomFnPropertyList(ObjectStore os, String docClassName, ResourceBundle docConfigProps, WIISCLog wiiscLog)
//	{
//		//Create the FnPropertyList
//		FnPropertyList fnPropertyList = new FnPropertyList();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getCustomFnPropertyList()");
//			
//			if (docClassConfig == null)
//			{
//				LocalResource resConfig = getLocalResource(docClassName + "Config");
//				docClassConfig = resConfig.getLocalBundle(resConfig.getBundlePath(), resConfig.getBundleFile());
//			}
//			
//			//Get the FileNet Document System Properties from the DocClass Config Properties file
//			FnPropertyList docClassSystemPropertiesList = getDocClassSystemPropertiesFromConfig(wiiscLog);
//			
//			//Add Property List to Master Property List
//			fnPropertyList.addFnPropertyList(docClassSystemPropertiesList);
//			
//			//Get the FileNet Custom Document Properties
//			//fnPropertyList = getCustomDocProperties(os, docClassName, wiiscLog);
//			FnPropertyList customDocPropertiesList = getCustomDocProperties(os, docClassName, wiiscLog);
//			
//			//Consolidate Properties List to remove any duplicates
//			FnPropertyList cleanPropertyList = new FnPropertyList();
//			for (int a = 0; a < fnPropertyList.getFnDocumentPropsList().size(); a++)
//			{
//				FnProperty fnProp1 = fnPropertyList.getFnDocumentPropsList().get(a);
//				
//				//Check with Clean List
//				//Verify this does not already exist in the Clean List
//				if (cleanPropertyList.getFnDocumentPropsList().size() > 0)
//				{
//					boolean goodProperty = false;
//					for (int b = 0; b < cleanPropertyList.getFnDocumentPropsList().size(); b++)
//					{
//						FnProperty fnProp2 = cleanPropertyList.getFnDocumentPropsList().get(b);
//						//Add the Property if fnProp2 does not exist
//						if (!fnProp1.getName().equals(fnProp2.getName()))
//						{
//							//Property does not exist
//							goodProperty = true;
//						}
//						else
//						{
//							//Property already exists
//							goodProperty = false;
//							break;
//						}
//					}
//					//Add the Property if its good
//					if (goodProperty)
//					{
//						//Add fnProp2 to the Clean List
//						cleanPropertyList.addFnProperty(fnProp1);
//					}
//				}
//				else
//				{
//					//Add fnProp1 to the Clean List since its the 1st item
//					cleanPropertyList.addFnProperty(fnProp1);
//				}
//				//Compare fnProp1 with fnProp3
//				for (int c = 0; c < customDocPropertiesList.getFnDocumentPropsList().size(); c++)
//				{
//					FnProperty fnProp3 = customDocPropertiesList.getFnDocumentPropsList().get(c);
//					//Add the Property if they don't match
//					if (!fnProp1.getName().equals(fnProp3.getName()))
//					{
//						//Verify this does not already exist in the Clean List
//						if (cleanPropertyList.getFnDocumentPropsList().size() > 0)
//						{
//							boolean goodProperty = false;
//							for (int d = 0; d < cleanPropertyList.getFnDocumentPropsList().size(); d++)
//							{
//								FnProperty fnProp4 = cleanPropertyList.getFnDocumentPropsList().get(d);
//								//Add the Property if fnProp2 does not exist
//								if (!fnProp3.getName().equals(fnProp4.getName()))
//								{
//									//Property does not exist
//									goodProperty = true;
//								}
//								else
//								{
//									//Property already exists
//									goodProperty = false;
//									break;
//								}
//							}
//							//Add the Property if its good
//							if (goodProperty)
//							{
//								//Add fnProp2 to the Clean List
//								cleanPropertyList.addFnProperty(fnProp3);
//							}
//						}
//					}
//				}
//			}
//			
//			//Update the FnPropertyList
//			fnPropertyList.clear();
//			fnPropertyList.addFnPropertyList(cleanPropertyList);
//			
//			//Add Property List to Master Property List
//			//fnPropertyList.addFnPropertyList(customDocPropertiesList);
//			
//			//Get the Custom Configured Document Properties filter from the DocClass Config Properties file
//			String documentPropIncludeList = docConfigProps.getString("documentPropIncludeList");
//			String documentPropExcludeList = docConfigProps.getString("documentPropExcludeList");
//
//			//documentPropIncludeList is not empty
//			if (documentPropIncludeList.length() > 0)
//			{
//				wiiscLog.log(wiiscLog.INFO, "documentPropIncludeList NOT Empty");
//				wiiscLog.log(wiiscLog.INFO, "documentPropIncludeList: " + documentPropIncludeList);
//				//Temporary FnPropertyList to hold the New FnPropertyList
//				FnPropertyList tempFnPropertyList = new FnPropertyList();
//				
//				//Document Property Include list with comma separation
//				if (documentPropIncludeList.contains(","))
//				{
//					String[] documentPropIncludeListData = null;
//					documentPropIncludeListData = documentPropIncludeList.split(",");
//					//wiiscLog.log(wiiscLog.INFO, "documentPropIncludeList is " + documentPropIncludeListData.length);
//					//Loop through the Custom FileNet Document Properties list to create a New List
//					for (int x = 0; x < fnPropertyList.getFnDocumentPropsList().size(); x++)
//					{
//						//wiiscLog.log(wiiscLog.INFO, "1st For Loop");
//						FnProperty fnProp = fnPropertyList.getFnDocumentPropsList().get(x);
//						//Loop through the Custom Configured Properties list to compare with the Custom FileNet Document Properties list
//						for (int y = 0; y < documentPropIncludeListData.length; y++)
//						{
//							//wiiscLog.log(wiiscLog.INFO, "2nd For Loop");
//							if (fnProp.getName().equals(documentPropIncludeListData[y]))
//							{
//								//wiiscLog.log(wiiscLog.INFO, "Adding " + fnProp.getName());
//								tempFnPropertyList.addFnProperty(fnProp);
//								break;
//							}
//						}
//					}
//				}
//				//Document Property Include list with 1 property no comma separation
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "documentPropIncludeList is 1");
//					//wiiscLog.log(wiiscLog.INFO, "documentPropIncludeList: " + documentPropIncludeList);
//					//Loop through the Custom FileNet Document Properties list to create a New List
//					for (int x = 0; x < fnPropertyList.getFnDocumentPropsList().size(); x++)
//					{
//						//wiiscLog.log(wiiscLog.INFO, "Only For Loop");
//						FnProperty fnProp = fnPropertyList.getFnDocumentPropsList().get(x);
//						if (fnProp.getName().equals(documentPropIncludeList))
//						{
//							//wiiscLog.log(wiiscLog.INFO, "Adding " + fnProp.getName());
//							tempFnPropertyList.addFnProperty(fnProp);
//						}
//					}					
//				}
//				//Check the size of the tempFnPropertyList
//				if (tempFnPropertyList.getFnDocumentPropsList().size() > 0)
//				{
//					//wiiscLog.log(wiiscLog.INFO, "Temp Property List has a size of " + tempFnPropertyList.getFnDocumentPropsList().size());
//					//Clear the FnPropertyList
//					fnPropertyList.clear();
//					//Update the FnPropertyList to use the tempFnPropertyList
//					fnPropertyList.addFnPropertyList(tempFnPropertyList);
//				}
//				//Clear the TempFnPropertyList
//				tempFnPropertyList.clear();
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "documentPropIncludeList IS Empty");
//			}
//			
//			//documentPropExcludeList is not empty
//			if (documentPropExcludeList.length() > 0)
//			{
//				wiiscLog.log(wiiscLog.INFO, "documentPropExcludeList NOT Empty");
//				wiiscLog.log(wiiscLog.INFO, "documentPropExcludeList: " + documentPropExcludeList);
//				//Temporary FnPropertyList to hold the New FnPropertyList
//				FnPropertyList tempFnPropertyList = new FnPropertyList();
//				//FnPropertyList tempBadFnPropertyList = new FnPropertyList();
//				
//				//Document Property Exclude list with comma separation
//				if (documentPropExcludeList.contains(","))
//				{
//					String[] documentPropExcludeListData = null;
//					documentPropExcludeListData = documentPropExcludeList.split(",");
//					//wiiscLog.log(wiiscLog.INFO, "documentPropExcludeList is " + documentPropExcludeListData.length);
//					//wiiscLog.log(wiiscLog.INFO, "Current Property Size " + fnPropertyList.getFnDocumentPropsList().size());
//					//Loop through the Custom FileNet Document Properties list to create a New List
//					for (int x = 0; x < fnPropertyList.getFnDocumentPropsList().size(); x++)
//					{
//						//wiiscLog.log(wiiscLog.INFO, "1st For Loop");
//						FnProperty fnProp = fnPropertyList.getFnDocumentPropsList().get(x);
//						boolean goodProperty = false;
//						//Loop through the Custom Configured Properties list to compare with the Custom FileNet Document Properties list
//						for (int y = 0; y < documentPropExcludeListData.length; y++)
//						{
//							//wiiscLog.log(wiiscLog.INFO, "2nd For Loop");
//							if (fnProp.getName().equals(documentPropExcludeListData[y]))
//							{
//								wiiscLog.log(wiiscLog.INFO, "Excluding " + fnProp.getName());
//								//tempBadFnPropertyList.addFnProperty(fnProp);
//								goodProperty = false;
//								break;
//							}
//							else
//							{
//								goodProperty = true;
//							}
//						}
//						//Check to see if the Property was good
//						if (goodProperty)
//						{
//							//wiiscLog.log(wiiscLog.INFO, "Adding " + fnProp.getName());
//							tempFnPropertyList.addFnProperty(fnProp);
//						}
//					}
//				}
//				//Document Property Exclude list with 1 property no comma separation
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "documentPropExcludeList is 1");
//					//wiiscLog.log(wiiscLog.INFO, "documentPropExcludeList: " + documentPropExcludeList);
//					//wiiscLog.log(wiiscLog.INFO, "Current Property Size " + fnPropertyList.getFnDocumentPropsList().size());
//					//Loop through the Custom FileNet Document Properties list to create a New List
//					for (int x = 0; x < fnPropertyList.getFnDocumentPropsList().size(); x++)
//					{
//						//wiiscLog.log(wiiscLog.INFO, "Only For Loop");
//						FnProperty fnProp = fnPropertyList.getFnDocumentPropsList().get(x);
//						if (fnProp.getName().equals(documentPropExcludeList))
//						{
//							wiiscLog.log(wiiscLog.INFO, "Excluding " + fnProp.getName());
//							//tempBadFnPropertyList.addFnProperty(fnProp);
//						}
//						else
//						{
//							//wiiscLog.log(wiiscLog.INFO, "Adding " + fnProp.getName());
//							tempFnPropertyList.addFnProperty(fnProp);
//						}
//					}					
//				}
//				//Check the size of the tempFnPropertyList
//				if (tempFnPropertyList.getFnDocumentPropsList().size() > 0)
//				{
//					//wiiscLog.log(wiiscLog.INFO, "Temp Property List has a size of " + tempFnPropertyList.getFnDocumentPropsList().size());
//					//Clear the FnPropertyList
//					fnPropertyList.clear();
//					//Update the FnPropertyList to use the tempFnPropertyList
//					fnPropertyList.addFnPropertyList(tempFnPropertyList);
//				}
//				//Clear the TempFnPropertyList
//				tempFnPropertyList.clear();
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "documentPropExcludeList IS Empty");
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnPropertyList.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnPropertyList.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getCustomFnPropertyList()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnPropertyList;
//	}
//	
//	private void setCustomFnPropertyValue(FnDocument fnDocument, String propertyName, String propertyValue, WIISCLog wiiscLog)
//	{
//		//FnDocument fnDocumentResult = new FnDocument();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> setCustomFnPropertyValue()");
//			//Boolean to check if Property value was set
//			boolean propUpdated = false;
//			//Create the FnPropertyList Result
//			FnPropertyList fnPropertyListResult = new FnPropertyList();
//			//Create the FnPropertyList
//			FnPropertyList fnPropertyList = new FnPropertyList();
//			//Get the FnPropertyList from the FnDocument
//			fnPropertyList = fnDocument.getFnPropertyList();
//			//Create the List<FnProperty> and initialize
//			List<FnProperty> fnPropsList = new ArrayList<FnProperty>(10);
//			//Get the List<FnProperty> from the FnPropertyList
//			fnPropsList = fnPropertyList.getFnDocumentPropsList();
//			//Loop through the List<FnProperty> to find the Property Name and get the Property Value
//			for (int x = 0; x < fnPropsList.size(); x++)
//			{
//				//wiiscLog.log(wiiscLog.INFO, "Only For Loop");
//				FnProperty fnProp = fnPropsList.get(x);
//				if (fnProp.getName().equals(propertyName))
//				{
//					wiiscLog.log(wiiscLog.INFO, "Property: " + propertyName);
//					wiiscLog.log(wiiscLog.INFO, "Value: " + fnProp.getValue());
//					//Set the Property value to the passed in value
//					fnProp.setValue(propertyValue);
//					wiiscLog.log(wiiscLog.INFO, "New Value: " + fnProp.getValue());
//					//Update propUpdated
//					propUpdated = true;
//				}
//				//Add the FnProperty to the New FnPropertyList
//				//fnPropertyListResult.addFnProperty(fnProp);
//			}
//			
//			//Add the FnPropertyList to the New FnDocument
//			//fnDocumentResult.setFnPropertyList(fnPropertyListResult);
//			
//			//Check if the Property Value was updated
//			if (propUpdated)
//			{
//				wiiscLog.log(wiiscLog.INFO, "Property " + propertyName + " was updated");
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Property " + propertyName + " was not updated");
//			}
//			
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> setCustomFnPropertyValue()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		//return fnDocumentResult;
//	}
//	
//	private String getCustomFnPropertyValue(FnDocument fnDocument, String propertyName, WIISCLog wiiscLog)
//	{
//		String propertyValue = "";
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getCustomFnPropertyValue()");
//			//Create the FnPropertyList
//			FnPropertyList fnPropertyList = new FnPropertyList();
//			//Get the FnPropertyList from the FnDocument
//			fnPropertyList = fnDocument.getFnPropertyList();
//			//Create the List<FnProperty> and initialize
//			List<FnProperty> fnPropsList = new ArrayList<FnProperty>(10);
//			//Get the List<FnProperty> from the FnPropertyList
//			fnPropsList = fnPropertyList.getFnDocumentPropsList();
//			//Loop through the List<FnProperty> to find the Property Name and get the Property Value
//			for (int x = 0; x < fnPropsList.size(); x++)
//			{
//				//wiiscLog.log(wiiscLog.INFO, "Only For Loop");
//				FnProperty fnProp = fnPropsList.get(x);
//				if (fnProp.getName().equals(propertyName))
//				{
//					wiiscLog.log(wiiscLog.INFO, "Property: " + propertyName);
//					wiiscLog.log(wiiscLog.INFO, "Value: " + fnProp.getValue());
//					//Save the value to the propertyValue
//					propertyValue = fnProp.getValue();
//					//Exit the loop
//					break;
//				}
//			}
//			
//			//Check if the Property Value was found
//			if (propertyValue.length() > 0)
//			{
//				wiiscLog.log(wiiscLog.INFO, "Property " + propertyName + " was found");
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Property " + propertyName + " was not found");
//				//Reset propertyValue
//				propertyValue = "";
//			}
//			
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getCustomFnPropertyValue()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return propertyValue;
//	}
//	
//	private IndependentObjectSet getObjectStoreQueryResults(ObjectStore os, String sql, WIISCLog wiiscLog)
//	{
//		//Create an IndependentObjectSet
//		IndependentObjectSet objectSet = null;
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getObjectStoreQueryResults()");
//			//SearchScope
//			SearchScope searchScope = new SearchScope(os);
//			//SearchSQL
//			SearchSQL searchSQL = new SearchSQL();
//			
//			//Set Max Records to Process to 500
//			searchSQL.setMaxRecords(500);
//			
//			//Set the SQL query
//			searchSQL.setQueryString(sql);
//			
//			//Independent Object Set
//			objectSet = searchScope.fetchObjects(searchSQL, null, null, null);
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getObjectStoreQueryResults()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return objectSet;
//	}
//	
//	private String getObjectStoreQueryForBatches(FnPropertyList fnPropertyList, String batchClass, String batchName, WIISCLog wiiscLog)
//	{
//		String sql = "";
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getObjectStoreQueryForBatches()");
//			//Build Query from Doc Props
//			String sqlDocProps = "";
//			String addComma = "";
//			
//			//Get Batch Name Property
//			String indexingBatchNameProperty = "";
//			indexingBatchNameProperty = globalConfig.getString("indexingBatchNameProperty");
//			
//			//Get Batches Root Location
//			String indexingBatchesRootLocation = "";
//			indexingBatchesRootLocation = globalConfig.getString("indexingBatchesRootLocation");
//			
//			//Initialize the Property Key Counter
//			//int propCount = 1;
//			
//			String globalDocAppIDProperty = "";
//			String[] globalDocAppIDPropertyData = null;
//			String globalWhere = "";
//			
//			//Set the Global Doc App ID Property
//			globalDocAppIDProperty = globalConfig.getString("documentAppIDProperty");
//			//Split if there is more than 1 property
//			if (globalDocAppIDProperty.contains(","))
//			{
//				globalDocAppIDPropertyData = globalDocAppIDProperty.split(",");
//			}
//			
//			wiiscLog.log(wiiscLog.INFO, "Building the SQL Query from the Document Properties");
//			
//			//Build the SQL Properties string
//			for (int x = 0; x < fnPropertyList.getFnDocumentPropsList().size(); x++)
//			{
//				String propName = "";
//				String propValue = "";
//				propName = fnPropertyList.getFnDocumentPropsList().get(x).getName();
//				propValue = fnPropertyList.getFnDocumentPropsList().get(x).getValue();
//				//Update SQL Select
//				sqlDocProps = sqlDocProps + propName + ", ";
//				
//				if (globalDocAppIDPropertyData != null)
//				{
//					for (int i = 0; i < globalDocAppIDPropertyData.length; i++)
//					{
//						if (globalDocAppIDPropertyData[i].equals(propName))
//						{
//							if (globalWhere.length() == 0)
//							{
//								globalWhere = propName + " = '" + propValue + "'";
//							}
//							else
//							{
//								globalWhere = globalWhere + " and " + propName + " = '" + propValue + "'";
//							}
//							break;
//						}
//					}
//				}
//				else if (globalDocAppIDProperty.equals(propName))
//				{
//					globalWhere = propName + " = '" + propValue + "'";
//				}
//				
//			}
//			
//			//Remove the last comma and space from the String
//			if (sqlDocProps.contains(","))
//			{
//				sqlDocProps = sqlDocProps.substring(0, sqlDocProps.length()-2);
//				addComma = ", ";
//			}
//			else
//			{
//				sqlDocProps = "ID, DocumentTitle";
//				addComma = ", ";
//			}
//						
//			//Output the SQL Doc Props Query to the Log
//			wiiscLog.log(wiiscLog.INFO, "SQL Select: " + sqlDocProps);
//			
//			//Define the Batch Class Properties Resource
//			ResourceBundle batchClassConfigProps = null;
//			
//			//Check for Custom Doc Properties File
//			if (checkPropertiesFileExist(batchClass + "Config", wiiscLog))
//			{
//				//batchClassConfigProps = ResourceBundle.getBundle(batchClass + "Config");
//				LocalResource resConfig = getLocalResource(batchClass + "Config");
//				batchClassConfigProps = resConfig.getLocalBundle(resConfig.getBundlePath(), resConfig.getBundleFile());
//			}
//						
//			//SQL Query for specific Documents
//			if (batchName.length() > 0)
//			{
//				if (batchClassConfigProps != null)
//				{
//					sql = "select d.this" + addComma + sqlDocProps + " from " + batchClass + " d" + 
//							" where " + indexingBatchNameProperty + " = '" + batchName + "' and d.this INFOLDER '" + indexingBatchesRootLocation + "/" + batchName + "'";
//				}
//				else
//				{
//					//Scenario where there is a Missing batchClassConfigProps file (Properties file missing)
//					sql = "select d.this" + addComma + sqlDocProps + " from " + batchClass + " d" + 
//							" where " + globalWhere + " and " + indexingBatchNameProperty + " = '" + batchName + "' and d.this INFOLDER '" + indexingBatchesRootLocation + "/" + batchName + "'";
//				}
//			}
//			else
//			{
//				//Assumes there is only a batchClass value and batchName is empty
//				sql = "select d.this" + addComma + sqlDocProps + " from " + batchClass + " d where d.this INSUBFOLDER '" + indexingBatchesRootLocation + "'";
//			}
//								
//			//Output Full Query
//			wiiscLog.log(wiiscLog.INFO, "SQL Query: " + sql);
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getObjectStoreQueryForBatches()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return sql;
//	}
//	
//	private String getObjectStoreQuery(FnPropertyList fnPropertyList, String docClassName, String docPropValue, WIISCLog wiiscLog)
//	{
//		String sql = "";
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getObjectStoreQuery()");
//			//Build Query from Doc Props
//			String sqlDocProps = "";
//			String addComma = "";
//			
//			//Initialize the Property Key Counter
//			int propCount = 1;
//			
//			String globalDocAppIDProperty = "";
//			String[] globalDocAppIDPropertyData = null;
//			String globalWhere = "";
//			
//			//Set the Global Project Root Object Store Path
//			String objectStoreProjectRootPath = "";
//			objectStoreProjectRootPath = globalConfig.getString("objectStoreProjectRootPath");
//			
//			//Set the Global Doc App ID Property
//			globalDocAppIDProperty = globalConfig.getString("documentAppIDProperty");
//			//Split if there is more than 1 property
//			if (globalDocAppIDProperty.contains(","))
//			{
//				globalDocAppIDPropertyData = globalDocAppIDProperty.split(",");
//			}
//			
//			wiiscLog.log(wiiscLog.INFO, "Building the SQL Query from the Document Properties");
//			
//			//Build the SQL Properties string
//			for (int x = 0; x < fnPropertyList.getFnDocumentPropsList().size(); x++)
//			{
//				String propName = "";
//				String propValue = "";
//				propName = fnPropertyList.getFnDocumentPropsList().get(x).getName();
//				propValue = fnPropertyList.getFnDocumentPropsList().get(x).getValue();
//				//Update SQL Select
//				sqlDocProps = sqlDocProps + "d." + propName + ", ";
//				
//				if (globalDocAppIDPropertyData != null)
//				{
//					for (int i = 0; i < globalDocAppIDPropertyData.length; i++)
//					{
//						if (globalDocAppIDPropertyData[i].equals(propName))
//						{
//							if (globalWhere.length() == 0)
//							{
//								globalWhere = "d." + propName + " = '" + propValue + "'";
//							}
//							else
//							{
//								globalWhere = globalWhere + " and " + "d." + propName + " = '" + propValue + "'";
//							}
//							break;
//						}
//					}
//				}
//				else if (globalDocAppIDProperty.equals(propName))
//				{
//					globalWhere = "d." + propName + " = '" + propValue + "'";
//				}
//				
//			}
//			
//			//Remove the last comma and space from the String
//			if (sqlDocProps.contains(","))
//			{
//				sqlDocProps = sqlDocProps.substring(0, sqlDocProps.length()-2);
//				addComma = ", ";
//			}
//			else
//			{
//				sqlDocProps = "d.ID, d.DocumentTitle";
//				addComma = ", ";
//			}
//						
//			//Output the SQL Doc Props Query to the Log
//			wiiscLog.log(wiiscLog.INFO, "SQL Select: " + sqlDocProps);
//			
//			//Check if Doc Class Config has been set
//			if (docClassConfig == null)
//			{
//				//Define the Doc Class Properties Resource
//				//ResourceBundle docClassConfigProps = null;
//				
//				//Check for Custom Doc Properties File
//				if (checkPropertiesFileExist(docClassName + "Config", wiiscLog))
//				{
//					//docClassConfigProps = ResourceBundle.getBundle(docClassName + "Config");
//					LocalResource resConfig = getLocalResource(docClassName + "Config");
//					docClassConfig = resConfig.getLocalBundle(resConfig.getBundlePath(), resConfig.getBundleFile());
//				}
//			}
//						
//			//SQL Query for specific Documents
//			if (docPropValue.length() > 0)
//			{
//				if (docClassConfig != null)
//				{
//					//Documents by Doc Class and Properties where specific Where Property Documents reside in the Document Class Folder Path e.g. /Project/DocClass
//					sql = "select d.this" + addComma + sqlDocProps + " from " + docClassName + " d where " + 
//							"d." + docClassConfig.getString("whereProperty") + " = '" + docPropValue + "'";
//				}
//				else
//				{
//					//Documents by Doc Class and Properties where global ID Property Documents reside in the Project Root Folder Path and below e.g. /Project
//					sql = "select d.this" + addComma + sqlDocProps + " from " + docClassName + " d where " + globalWhere;
//				}
//			}
//			else
//			{
//				//Documents by Doc Class and Properties where ALL Documents reside in the Document Class Folder Path
//				//sql = "select d.this" + addComma + sqlDocProps + " from " + docClassName + " d where d.this INSUBFOLDER '" + docClassConfigProps.getString("docClassDocsRootLocation") + "'";
//				sql = "select d.this" + addComma + sqlDocProps + " from " + docClassName + " d";
//			}
//								
//			//Output Full Query
//			wiiscLog.log(wiiscLog.INFO, "SQL Query: " + sql);
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getObjectStoreQuery()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return sql;
//	}
//	//@SuppressWarnings("unused")
//	private ObjectStore getObjectStore(String userId, String password, WIISCLog wiiscLog)
//	{
//		ObjectStore os = null;
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getObjectStore()");
//			//ResourceBundle globalConfig = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG);
//			String connectionURI = globalConfig.getString(ConstantsUtil.CE_CONNECTION_URI);
//			String stanzaName = globalConfig.getString(ConstantsUtil.CE_STANZA_NAME);
//			String objectStoreName = globalConfig.getString(ConstantsUtil.CE_OBJECTSTORE_NAME);
//			
//			//Get a CE Connection
//			Connection conn = Factory.Connection.getConnection(connectionURI);
//			if (conn != null)
//			{
//				wiiscLog.log(wiiscLog.INFO, "CE Connection Successful");
//				//Get a CE Connection Subject
//				UserContext uc = UserContext.get();
//				Subject subject = UserContext.createSubject(conn, userId, password, stanzaName);
//				uc.pushSubject(subject);
//				
//				if (uc != null)
//				{
//					wiiscLog.log(wiiscLog.INFO, "CE Connection Subject Successful");
//					//Get a CE Domain Object
//					Domain dom = Factory.Domain.fetchInstance(conn, null, null);
//					
//					if (dom != null)
//					{
//						wiiscLog.log(wiiscLog.INFO, "CE Domain Connection Successful");
//						//Get the Object Store
//						os = Factory.ObjectStore.fetchInstance(dom, objectStoreName, null);
//						
//						if (os != null)
//						{
//							wiiscLog.log(wiiscLog.INFO, "CE Object Store Connection Successful");
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "CE Object Store Connection Failed");
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "CE Domain Connection Failed");
//					}
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "CE Connection Failed");
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getObjectStore()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return os;
//	}
//	
//	private boolean checkPropertiesFileExist(String propName, WIISCLog wiiscLog)
//	{
//		boolean propertiesExist = false;
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> checkPropertiesFileExist()");
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
//			wiiscLog.log(wiiscLog.ERROR, "Properties file " + propName + " does not exist");
//			System.out.println("Properties file " + propName + " does not exist");
//			propertiesExist = false;
//		}
//		catch (Exception e)
//		{
//			//Properties file does not exist
//			wiiscLog.log(wiiscLog.ERROR, "Properties file " + propName + " does not exist");
//			System.out.println("Properties file " + propName + " does not exist");
//			propertiesExist = false;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> checkPropertiesFileExist()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		
//		return propertiesExist;
//	}
//	
//	private LocalResource getLocalResource(String propsFile)
//	{
//		LocalResource res = null;
//		String wiiscConfigsPath = "";
//		wiiscConfigsPath = System.getProperty("wiisc.config.properties.path");
//		if (wiiscConfigsPath == null || wiiscConfigsPath.length() == 0)
//		{
//			System.out.println("ImagingManager -> getLocalResource() cannot find the JVM Property wiisc.config.properties.path using WIISCConfig.properties file");
//			res = LocalResource.setResource(ResourceBundle.getBundle(ConstantsUtil.WIISC_CONFIG).getString("wiiscConfigFilesPath"), "\\" + propsFile);
//		}
//		else
//		{
//			//Assumes the JVM Properties Path includes /Configs e.g. C:/WIISC/<Env>/Configs
//			System.out.println("ImagingManager -> getLocalResource() JVM Property wiisc.config.properties.path " + wiiscConfigsPath);
//			res = LocalResource.setResource(wiiscConfigsPath, "\\" + propsFile);
//		}
//		
//		return res;
//	}
//	
//	private boolean checkDocClassExists(ObjectStore os, String docClassName, WIISCLog wiiscLog)
//	{
//		boolean docClassExists = false;
//		
//		//Fetch selected class definition from the server
//		ClassDefinition docClassDef = Factory.ClassDefinition.fetchInstance(os, docClassName, null);
//		//Check the Doc Class Definition returned
//		if (docClassDef.get_Id().toString().length() == 0)
//		{
//			//Doc Class Does not exist
//			docClassExists = false;
//		}
//		else
//		{
//			//Doc Class Does exist
//			docClassExists = true;
//		}
//		return docClassExists;
//	}
//	
//	private boolean checkFolderExists(ObjectStore os, String folderToVerify, WIISCLog wiiscLog)
//	{
//		boolean folderExists = false;
//		//Folder value Found
//		String folderValue = "";
//		
//		//wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> checkFolderInCE()");
//		//wiiscLog.log(wiiscLog.INFO, "Verifying the Folder Path: " + folderToVerify);
//		try
//		{
//			if (folderToVerify.length() > 0)
//			{
//				//Check if the Folder exists in the Object Store
//				Folder folder = Factory.Folder.fetchInstance(os, folderToVerify, null);
//				//Return the ID of the Folder proving that the Folder exists in the Object Store
//				folderValue = folder.get_Id().toString();
//				//wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> checkFolderInCE()");
//				//return folderValue;
//				if (folderValue.length() > 0)
//				{
//					//Return True for the Folder to show that it does exist
//					folderExists = true;
//				}
//				else
//				{
//					//Return False for the Folder to show that it does not exist
//					folderExists = false;
//				}
//			}
//			else
//			{
//				//wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> checkFolderInCE()");
//				//Return Empty String for the Folder to show that it does not exist in the Object Store
//				//return "";
//				//Return False for the Folder to show that it does not exist
//				folderExists = false;
//			}
//		}
//		catch (Exception e)
//		{
//			//wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> checkFolderInCE()");
//			//Return Empty String for the Folder to show that it does not exist in the Object Store
//			//return "";
//			//Return False for the Folder to show that it does not exist
//			folderExists = false;
//		}
//				
//		return folderExists;
//	}
//	
//	private String[] getDocumentPropertiesTemplate(ObjectStore os, String docClassName, WIISCLog wiiscLog)
//	{
//		//String array Properties Template
//		String[] propertiesTemplate = null;
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getPropertiesTemplate()");
//			//Get the Document Class Properties
//			FnPropertyList fnDocClassProperties = getCustomDocProperties(os, docClassName, wiiscLog);
//			
//			//Get the Filtered Document Class Properties
//			FnPropertyList fnDocClassFilteredProperties = getCustomFilteredProperties(fnDocClassProperties, wiiscLog);
//			
//			//Master Properties to be used for each Document
//			propertiesTemplate = new String[fnDocClassFilteredProperties.getCount()];
//			for (FnProperty fnProp : fnDocClassFilteredProperties.getFnDocumentPropsList())
//			{
//				propertiesTemplate[fnDocClassFilteredProperties.getFnDocumentPropsList().indexOf(fnProp)] = fnProp.getName();
//			}
//			
//			wiiscLog.log(wiiscLog.INFO, "Found " + propertiesTemplate.length + " Properties");
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getPropertiesTemplate()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return propertiesTemplate;
//	}
//	
//	private FnDocClassList getCustomDocClasses(ObjectStore os, WIISCLog wiiscLog)
//	{
//		//List to hold the Custom Doc Classes List
//		FnDocClassList fnDocClassList = new FnDocClassList();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getCustomDocClasses()");
//					
//			//Define the Global Properties Resource
//			//ResourceBundle globalConfig = null;
//			//globalConfig = ResourceBundle.getBundle("GlobalConfig");
//			//String userId = globalProps.getString(ConstantsUtil.CE_USER_ID);
//			//String password = globalProps.getString(ConstantsUtil.CE_USER_PASSWORD);
//			
//			//Verify Object Store Connected
//			if (os != null)
//			{
//				//Exclude List of Doc Classes
//				String docClassExcludeList = "";
//				docClassExcludeList = globalConfig.getString("documentClassExcludeList");
//				//Array to hold Doc Class Exclude List
//				String[] docClassExcludeData = null;
//				boolean excludeListSplit = false;
//				
//				//Split the list because there is more than 1 Doc Class to exclude
//				if (docClassExcludeList.contains(","))
//				{
//					//Split the list to hold in the array
//					docClassExcludeData = docClassExcludeList.split(",");
//					excludeListSplit = true;
//				}
//						
//				//Fetch selected class definition from the server
//				ClassDefinition docClassDef = Factory.ClassDefinition.fetchInstance(os, "Document", null);
//				//Get the SubClasses
//				ClassDefinitionSet subDocClassDefSet = docClassDef.get_ImmediateSubclassDefinitions();
//								
//				//PropertyDefinitionList propDefList = docClassDef.get_PropertyDefinitions();
//				Iterator iter = subDocClassDefSet.iterator();
//				ClassDefinition classDef = null;
//				
//				wiiscLog.log(wiiscLog.INFO, "===========================================================");
//				
//				//Loop until Class Definitions are found
//				while (iter.hasNext())
//				{	        				
//					//Get the Class Definition
//					classDef = (ClassDefinition) iter.next();
//					//Output Class Name
//					//wiiscLog.log(wiiscLog.INFO, "Class: " + classDef.get_DisplayName());
//					
//					//Only want the Custom Class Definitions			
//					//if (!classDef.get_IsHidden() && !classDef.get_IsSystemOwned() && classDef.get_IsPersistent()
//					//		&& classDef.get_InstalledByAddOn() == null)
//					if (!classDef.get_IsHidden() && !classDef.get_IsSystemOwned() && classDef.get_IsPersistent() && classDef.get_InstalledByAddOn() == null)
//					{
//						boolean skipClassDef = false;
//						//excludeListSplit is true, then we need to check each exclude name against the classDef
//						if (excludeListSplit)
//						{
//							for (int x = 0; x < docClassExcludeData.length; x++)
//							{
//								//if (docClassExcludeData[x].equals(classDef.get_DisplayName()))
//								if (docClassExcludeData[x].equals(classDef.get_SymbolicName()))
//								{
//									skipClassDef = true;
//								}
//							}
//						}
//						else //Exclude List of 1 Doc Class
//						{
//							//if (docClassExcludeList.equals(classDef.get_DisplayName()))
//							if (docClassExcludeList.equals(classDef.get_SymbolicName()))
//							{
//								skipClassDef = true;
//							}
//						}
//						if (skipClassDef == false)
//						{
//							//Get the SubClasses
//							ClassDefinitionSet subDocClassSet = classDef.get_ImmediateSubclassDefinitions();
//							//Verify if the Class Definition has SubClasses.  We only want the last set of SubClasses
//							if (!subDocClassSet.isEmpty())
//							{
//								Iterator iter2 = subDocClassSet.iterator();
//								ClassDefinition subClassDef = null;
//								while (iter2.hasNext())
//								{
//									//Get the SubClass Definition
//									subClassDef = (ClassDefinition) iter2.next();
//									//Output Sub Class Name
//									//wiiscLog.log(wiiscLog.INFO, "Sub Class Definition: " + subClassDef.get_DisplayName());
//									//if (subClassDef.get_IsHidden() == false && subClassDef.get_IsSystemOwned() == false && subClassDef.get_IsPersistent() == true
//									//		&& subClassDef.get_InstalledByAddOn() == null)
//									if (!subClassDef.get_IsHidden() && !subClassDef.get_IsSystemOwned() && subClassDef.get_IsPersistent() && subClassDef.get_InstalledByAddOn() == null)
//									{
//										//Create the Doc Class Object
//										FnDocClass fnDocClass = new FnDocClass();
//										//Set the Name of the Doc Class
//										//fnDocClass.setName(subClassDef.get_DisplayName());
//										fnDocClass.setName(subClassDef.get_SymbolicName());
//										//wiiscLog.log(wiiscLog.INFO, "Class: " + subClassDef.get_DisplayName());
//										wiiscLog.log(wiiscLog.INFO, "Class: " + subClassDef.get_SymbolicName());
//										//Get the List of Properties for the Doc Class
//										//FnPropertyList fnPropsList = new FnPropertyList();
//										//fnPropsList = getCustomDocProperties(os, subClassDef.get_DisplayName(), wiiscLog);
//										//Add Props List to Class
//										//fnDocClass.setFnDocClassProps(fnPropsList);
//										//fnDocClass.setFnDocClassPropsList(fnPropsList.getFnDocumentPropsList());
//										//Add the Doc Class to the Doc Class List
//										fnDocClassList.addFnDocClass(fnDocClass);
//										//wiiscLog.log(wiiscLog.INFO, "Saving Class Definition: " + subClassDef.get_DisplayName());
//										//wiiscLog.log(wiiscLog.INFO, "=============================================");
//									}
//								}
//							}
//							else
//							{
//								//Create the Doc Class Object
//								FnDocClass fnDocClass = new FnDocClass();
//								//Set the Name of the Doc Class
//								//fnDocClass.setName(classDef.get_DisplayName());
//								fnDocClass.setName(classDef.get_SymbolicName());
//								//wiiscLog.log(wiiscLog.INFO, "Class: " + classDef.get_DisplayName());
//								wiiscLog.log(wiiscLog.INFO, "Class: " + classDef.get_SymbolicName());
//								//Get the List of Properties for the Doc Class
//								//FnPropertyList fnPropsList = new FnPropertyList();
//								//fnPropsList = getCustomDocProperties(os, classDef.get_DisplayName(), wiiscLog);
//								//Add the Properties to the Doc Class
//								//fnDocClass.setFnDocClassProps(fnPropsList);							
//								//fnDocClass.setFnDocClassPropsList(fnPropsList.getFnDocumentPropsList());
//								//Add the Doc Class to the Doc Class List
//								fnDocClassList.addFnDocClass(fnDocClass);
//								//wiiscLog.log(wiiscLog.INFO, "Saving Class Definition: " + classDef.get_DisplayName());
//								//wiiscLog.log(wiiscLog.INFO, "=============================================");
//							}
//						}
//					}
//				}
//				//Reset Object Store variable
//				//os = null;
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 2");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Reset Object Store variable
//			os = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getCustomDocClasses()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");	
//		return fnDocClassList;
//	}
//	
//	private FnPropertyList getDocumentProperties(Document document, String[] propertiesData, WIISCLog wiiscLog)
//	{
//		FnPropertyList fnDocumentProperties = new FnPropertyList();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getDocumentProperties()");
//						
//			if (document != null)
//			{
//				//Update Property Cache to include Custom Doc Properties
//				document.refresh(propertiesData);
//				//Get Doc Properties from FileNet
//				Properties docProperties = document.getProperties();
//				
//				wiiscLog.log(wiiscLog.INFO, "===========================================================");
//				wiiscLog.log(wiiscLog.INFO, "Get Document Properties");
//				
//				//Loop through Properties
//				Iterator iter = null;
//				iter = docProperties.iterator();
//				
//				while (iter.hasNext())
//				{
//					Property oneProperty = null;
//					oneProperty = (Property)iter.next();
//					FnProperty oneFnProperty = new FnProperty();
//					//Save the Property Name
//					oneFnProperty.setName(oneProperty.getPropertyName());
//					wiiscLog.log(wiiscLog.INFO, "Property: " + oneFnProperty.getName());
//					
//					//Get the Document's Property Value and Save to FnProperty
//					Object value = null;
//					value = oneProperty.getObjectValue();
//					if (value != null)
//					{
//						//Save the Property Value
//						oneFnProperty.setValue(value.toString());
//					}
//					else
//					{
//						//Save the Property Value
//						oneFnProperty.setValue("");
//					}
//					wiiscLog.log(wiiscLog.INFO, "Value: " + oneFnProperty.getValue());
//					//Add the FnProperty to the List
//					fnDocumentProperties.addFnProperty(oneFnProperty);
//				}
//			}			
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR,"ERROR getDocumentProperties()");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getDocumentProperties()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnDocumentProperties;
//	}
//	
//	private FnPropertyList getDocumentProperties(ObjectStore os, String docClassName, WIISCLog wiiscLog)
//	{
//		//Create the FnPropertyList
//		FnPropertyList fnPropertyList = new FnPropertyList();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getDocumentProperties()");
//			
//			//Check if Doc Class Config Global has been set
//			if (docClassConfig == null)
//			{
//				//Check for Custom Doc Properties File
//				if (checkPropertiesFileExist(docClassName + "Config", wiiscLog))
//				{
//					//docClassConfigProps = ResourceBundle.getBundle(docClassName + "Config");
//					LocalResource resConfig = getLocalResource(docClassName + "Config");
//					docClassConfig = resConfig.getLocalBundle(resConfig.getBundlePath(), resConfig.getBundleFile());
//					
//					//Check for Document Class Name
//					if (docClassName.length() > 0)
//					{
//						//Get Custom FileNet Document Properties List
//						FnPropertyList customFnPropertyList = new FnPropertyList();
//						customFnPropertyList = getCustomFnPropertyList(os, docClassName, docClassConfig, wiiscLog);
//						//Output FnPropertyList - debugging
//						//outputFnPropertyList(customFnPropertyList, wiiscLog);
//						
//						//Add the Custom Document Property List to the Final Document Property List
//						fnPropertyList.addFnPropertyList(customFnPropertyList);
//					}
//				}
//				else
//				{
//					//Using Global Config Properties
//					//Check for Document Class Name
//					if (docClassName.length() > 0)
//					{
//						//Get Custom FileNet Document Properties List
//						FnPropertyList customFnPropertyList = new FnPropertyList();
//						customFnPropertyList = getCustomFnPropertyList(os, docClassName, globalConfig, wiiscLog);
//						
//						//Output FnPropertyList - debugging
//						//outputFnPropertyList(customFnPropertyList, wiiscLog);
//						
//						//Add the Custom Document Property List to the Final Document Property List
//						fnPropertyList.addFnPropertyList(customFnPropertyList);
//					}
//				}
//			}
//			else
//			{
//				//Check for Document Class Name
//				if (docClassName.length() > 0)
//				{
//					//Get Custom FileNet Document Properties List
//					FnPropertyList customFnPropertyList = new FnPropertyList();
//					customFnPropertyList = getCustomFnPropertyList(os, docClassName, docClassConfig, wiiscLog);
//					//Output FnPropertyList - debugging
//					//outputFnPropertyList(customFnPropertyList, wiiscLog);
//					
//					//Add the Custom Document Property List to the Final Document Property List
//					fnPropertyList.addFnPropertyList(customFnPropertyList);
//				}
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnPropertyList.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnPropertyList.setErrorMessage(e.getMessage());
//		}
//		
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getDocumentProperties()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		
//		return fnPropertyList;
//	}
//	
//	private FnPropertyList getCustomRequiredDocProperties(ObjectStore os, String docClassName, WIISCLog wiiscLog)
//	{
//		//Create the FnPropertyList
//		FnPropertyList fnPropertyList = new FnPropertyList();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getCustomRequiredDocProperties()");
//			//Construct property filter to ensure only Custom PropertyDefinitions are returned
//			PropertyFilter pf = new PropertyFilter();
//			pf.addIncludeType(0, null, Boolean.TRUE, FilteredPropertyType.ANY, null); 
//
//			//Fetch selected class definition from the server
//			ClassDefinition docClassDef = Factory.ClassDefinition.fetchInstance(os, docClassName, pf);
//			PropertyDefinitionList propDefList = docClassDef.get_PropertyDefinitions();
//			
//			Iterator iter = propDefList.iterator();
//			PropertyDefinition propDef = null;
//			
//			wiiscLog.log(wiiscLog.INFO, "Looping through Property Definitions");
//			
//			//Loop until property definition found
//			while (iter.hasNext())
//			{	        				
//				//Get the Property Definition
//				propDef = (PropertyDefinition) iter.next();
//				
//				//Only want the Custom Property Definitions			
//				if (!propDef.get_IsHidden() && !propDef.get_IsSystemOwned() && propDef.get_CopyToReservation() && propDef.get_IsValueRequired())
//				{
//					//wiiscLog.log(wiiscLog.INFO, "Found Property Definition");
//					//Create the Property Object
//					FnProperty fnProperty = new FnProperty();
//					//Set the Name of the Property
//					//fnProperty.setName(propDef.get_DisplayName());
//					fnProperty.setName(propDef.get_SymbolicName());
//					//Add the Property to the List
//					fnPropertyList.addFnProperty(fnProperty);
//					//wiiscLog.log(wiiscLog.INFO, "Property " + propDef.get_DisplayName() + " is REQUIRED");
//					wiiscLog.log(wiiscLog.INFO, "Property " + propDef.get_SymbolicName() + " is REQUIRED");
//				}
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnPropertyList.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnPropertyList.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getCustomRequiredDocProperties()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnPropertyList;
//	}
//	
//	private FnPropertyList getCustomDocProperties(ObjectStore os, String docClassName, WIISCLog wiiscLog)
//	{
//		//Create the FnPropertyList
//		FnPropertyList fnPropertyList = new FnPropertyList();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getCustomDocProperties()");
//			//Construct property filter to ensure only Custom PropertyDefinitions are returned
//			PropertyFilter pf = new PropertyFilter();
//			pf.addIncludeType(0, null, Boolean.TRUE, FilteredPropertyType.ANY, null); 
//
//			//Fetch selected class definition from the server
//			ClassDefinition docClassDef = Factory.ClassDefinition.fetchInstance(os, docClassName, pf);
//			PropertyDefinitionList propDefList = docClassDef.get_PropertyDefinitions();
//			
//			Iterator iter = propDefList.iterator();
//			PropertyDefinition propDef = null;
//			
//			//wiiscLog.log(wiiscLog.INFO, "Looping through Property Definitions");
//			
//			//Loop until property definition found
//			while (iter.hasNext())
//			{	        				
//				//Get the Property Definition
//				propDef = (PropertyDefinition) iter.next();
//				
//				//Only want the Custom Property Definitions			
//				if (!propDef.get_IsHidden() && !propDef.get_IsSystemOwned() && propDef.get_CopyToReservation())
//				{
//					//wiiscLog.log(wiiscLog.INFO, "Found Property Definition");
//					//Create the Property Object
//					FnProperty fnProperty = new FnProperty();
//					//Set the Name of the Property
//					//fnProperty.setName(propDef.get_DisplayName());
//					fnProperty.setName(propDef.get_SymbolicName());
//					
//					//Add the Property to the List
//					fnPropertyList.addFnProperty(fnProperty);
//										
//					//Check if Property is Required
//					if (propDef.get_IsValueRequired())
//					{
//						//wiiscLog.log(wiiscLog.INFO, "Property " + propDef.get_DisplayName() + " is REQUIRED");
//						wiiscLog.log(wiiscLog.INFO, "Property: " + propDef.get_SymbolicName() + " is REQUIRED");
//					}
//					else
//					{
//						//wiiscLog.log(wiiscLog.INFO, "Property: " + propDef.get_DisplayName());
//						wiiscLog.log(wiiscLog.INFO, "Property: " + propDef.get_SymbolicName());
//					}
//				}
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnPropertyList.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnPropertyList.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getCustomDocProperties()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnPropertyList;
//	}
//	
//	private FnPropertyList getCustomFilteredProperties(FnPropertyList fnPropertyListSource, WIISCLog wiiscLog)
//	{
//		//Create the FnPropertyList
//		FnPropertyList fnPropertyListFiltered = new FnPropertyList();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getCustomFilteredProperties()");
//			//Check if fnPropertyListSource is valid
//			if (fnPropertyListSource.getCount() > 0)
//			{
//				//Check if docClassConfig is valid
//				if (docClassConfig != null)
//				{
//					//Get Filtered System List
//					FnPropertyList fnPropsSystemList = new FnPropertyList();
//					fnPropsSystemList = getCustomSystemProperties(wiiscLog);
//					//Add to the Filtered List
//					fnPropertyListFiltered.addFnPropertyList(fnPropsSystemList);
//					
//					//Get documentPropIncludeList
//					String docIncludeList = "";
//					docIncludeList = docClassConfig.getString("documentPropIncludeList");
//					//Check if there are particular Properties to include only
//					if (docIncludeList.length() > 0)
//					{
//						String[] docIncludeListData = null;
//						docIncludeListData = docIncludeList.split(",");
//						//Parse the fnPropertyListSource and the Include Properties List to create the Filtered List
//						List<FnProperty> fnProperties = fnPropertyListSource.getFnDocumentPropsList();
//						for (int i = 0; i < fnProperties.size(); i++)
//						{
//							FnProperty oneFnProperty = fnProperties.get(i);
//							//Check against the Include List
//							for (int j = 0; j < docIncludeListData.length; j++)
//							{
//								String oneIncludeValue = docIncludeListData[j];
//								if (oneFnProperty.getName().equals(oneIncludeValue))
//								{
//									wiiscLog.log(wiiscLog.INFO, "Adding Property " + oneIncludeValue + " to Filtered List");
//									//Add Property to Filtered List
//									fnPropertyListFiltered.addFnProperty(oneFnProperty);
//									break;
//								}
//							}
//						}
//					}
//					else
//					{
//						//Include ALL Properties
//						//Add fnPropertyListSource to the Filtered List
//						fnPropertyListFiltered.addFnPropertyList(fnPropertyListSource);
//					}
//				}
//				else
//				{
//					//Get Filtered System List
//					FnPropertyList fnPropsSystemList = new FnPropertyList();
//					fnPropsSystemList = getCustomSystemProperties(wiiscLog);
//					//Add to the Filtered List
//					fnPropertyListFiltered.addFnPropertyList(fnPropsSystemList);
//					//Get documentPropIncludeList
//					String docIncludeList = "";
//					docIncludeList = globalConfig.getString("documentPropIncludeList");
//					//Check if there are particular Properties to include only
//					if (docIncludeList.length() > 0)
//					{
//						String[] docIncludeListData = null;
//						docIncludeListData = docIncludeList.split(",");
//						//Parse the fnPropertyListSource and the Include Properties List to create the Filtered List
//						List<FnProperty> fnProperties = fnPropertyListSource.getFnDocumentPropsList();
//						for (int i = 0; i < fnProperties.size(); i++)
//						{
//							FnProperty oneFnProperty = fnProperties.get(i);
//							//Check against the Include List
//							for (int j = 0; j < docIncludeListData.length; j++)
//							{
//								String oneIncludeValue = docIncludeListData[j];
//								if (oneFnProperty.getName().equals(oneIncludeValue))
//								{
//									wiiscLog.log(wiiscLog.INFO, "Adding Property " + oneIncludeValue + " to Filtered List");
//									//Add Property to Filtered List
//									fnPropertyListFiltered.addFnProperty(oneFnProperty);
//									break;
//								}
//							}
//						}
//					}
//					else
//					{
//						//Include ALL Properties
//						//Add fnPropertyListSource to the Filtered List
//						fnPropertyListFiltered.addFnPropertyList(fnPropertyListSource);
//					}	
//				}
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnPropertyListFiltered.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnPropertyListFiltered.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getCustomFilteredProperties()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnPropertyListFiltered;
//	}
//	
//	private FnPropertyList getCustomSystemProperties(WIISCLog wiiscLog)
//	{
//		//Create the FnPropertyList
//		FnPropertyList fnPropertyListSystem = new FnPropertyList();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getCustomSystemProperties()");
//			//Check if docClassConfig is valid
//			if (docClassConfig != null)
//			{
//				//Get prop this is a System Property and ALWAYS added to the Filtered List
//				String prop = "";
//				int propCount = 1;
//				boolean propMissing = false;
//				while(!propMissing)
//				{
//					prop = "";
//					if (docClassConfig.containsKey("prop" + propCount))
//					{
//						prop = docClassConfig.getString("prop" + propCount);
//						wiiscLog.log(wiiscLog.INFO, "Adding Property " + prop + " to System List");
//						//Add Property to Filtered List
//						FnProperty fnProp = new FnProperty();
//						fnProp.setName(prop);
//						fnPropertyListSystem.addFnProperty(fnProp);
//						propMissing = false;
//					}
//					else
//					{
//						propMissing = true;
//						//break;
//					}
//					//Increment Property Counter
//					propCount++;
//				}
//			}
//			else
//			{
//				//Get prop this is a System Property and ALWAYS added to the Filtered List
//				String prop = "";
//				int propCount = 1;
//				boolean propMissing = false;
//				while(!propMissing)
//				{
//					prop = "";
//					if (globalConfig.containsKey("prop" + propCount))
//					{
//						prop = globalConfig.getString("prop" + propCount);
//						wiiscLog.log(wiiscLog.INFO, "Adding Property " + prop + " to System List");
//						//Add Property to Filtered List
//						FnProperty fnProp = new FnProperty();
//						fnProp.setName(prop);
//						fnPropertyListSystem.addFnProperty(fnProp);
//						propMissing = false;
//					}
//					else
//					{
//						propMissing = true;
//						//break;
//					}
//					//Increment Property Counter
//					propCount++;
//				}
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnPropertyListSystem.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnPropertyListSystem.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getCustomSystemProperties()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnPropertyListSystem;
//	}
//	
//	private String getMimeTypeExtension(String mimeType, WIISCLog wiiscLog)
//	{
//		//Use the mimeType value to determine the MIME TYPE Extension
//		String mimeTypeExtension = "";
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getMimeTypeExtension()");
//			//Check MIME Types
//			if (mimeType.equals(ConstantsUtil.TIFF_IMAGE))
//			{
//				wiiscLog.log(wiiscLog.INFO, "TIFF");
//				mimeTypeExtension = ".tiff";
//			}
//			else if (mimeType.equals(ConstantsUtil.JPEG_IMAGE))
//			{
//				wiiscLog.log(wiiscLog.INFO, "JPEG");
//				mimeTypeExtension = ".jpg";
//			}
//			else if (mimeType.equals(ConstantsUtil.BMP_IMAGE))
//			{
//				wiiscLog.log(wiiscLog.INFO, "BMP");
//				mimeTypeExtension = ".bmp";
//			}
//			else if (mimeType.equals(ConstantsUtil.GIF_IMAGE))
//			{
//				wiiscLog.log(wiiscLog.INFO, "GIF");
//				mimeTypeExtension = ".gif";
//			}
//			else if (mimeType.equals(ConstantsUtil.PNG_IMAGE))
//			{
//				wiiscLog.log(wiiscLog.INFO, "PNG");
//				mimeTypeExtension = ".png";
//			}
//			else if (mimeType.equals(ConstantsUtil.PDF_FILE))
//			{
//				wiiscLog.log(wiiscLog.INFO, "PDF");
//				mimeTypeExtension = ".pdf";
//			}
//			else if (mimeType.equals(ConstantsUtil.ZIP_FILE))
//			{
//				wiiscLog.log(wiiscLog.INFO, "ZIP");
//				mimeTypeExtension = ".zip";
//			}
//			else if (mimeType.equals(ConstantsUtil.TXT_FILE))
//			{
//				wiiscLog.log(wiiscLog.INFO, "TXT");
//				mimeTypeExtension = ".txt";
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "BAD");
//				mimeTypeExtension = ".bad";
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getMimeTypeExtension()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return mimeTypeExtension;
//	}
//	
//	private boolean checkTempDirectories(ResourceBundle globalConfig, WIISCLog wiiscLog)
//	{
//		boolean tempDirectoriesExist = false;
//		File tempFilesDir = null;
//		File currentDateDir = null;
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> checkTempDirectories()");
//			//Check if the Temp Files Directory
//	    	tempFilesDir = new File(globalConfig.getString("wiiscAppRootDirectory") + "/" + globalConfig.getString("wiiscTempFileDir"));
//	    	//Check if the Temp Files Current Date Directory
//	    	currentDateDir = new File(globalConfig.getString("wiiscAppRootDirectory") + "/" + globalConfig.getString("wiiscTempFileDir") + "/" + getDateWithDashes());
//	    	if (!tempFilesDir.exists())
//	    	{
//	    		//Create the Temp Directory
//	    		wiiscLog.log(wiiscLog.INFO, "Temp Files directory does not exist and needs created");
//	    		boolean tempFilesDirCreated = false;
//	    		tempFilesDirCreated = tempFilesDir.mkdir();
//	    		if (!tempFilesDirCreated)
//	    		{
//	    			wiiscLog.log(wiiscLog.INFO, "Temp Files directory could not be created");
//	    			tempDirectoriesExist = false;
//	    		}
//	    		else
//	    		{
//	    			wiiscLog.log(wiiscLog.INFO, "Temp Files directory has been created");
//	    			tempDirectoriesExist = true;
//	    			//Temp File Directory exists, now check if the Current Date Directory exists
//		    		if (!currentDateDir.exists())
//		    		{
//		    			//Create the Current Date Directory for Images
//			    		wiiscLog.log(wiiscLog.INFO, "Current Date directory does not exist and needs created");
//			    		boolean currentDateDirCreated = false;
//			    		currentDateDirCreated = currentDateDir.mkdir();
//			    		if (!currentDateDirCreated)
//			    		{
//			    			wiiscLog.log(wiiscLog.INFO, "Current Date directory could not be created");
//			    			tempDirectoriesExist = false;
//			    		}
//			    		else
//			    		{
//			    			wiiscLog.log(wiiscLog.INFO, "Current Date directory has been created");
//			    			tempDirectoriesExist = true;
//			    		}
//		    		}
//		    		else
//		    		{
//		    			tempDirectoriesExist = true;
//		    		}
//	    		}
//	    	}
//	    	else
//	    	{
//	    		//Temp File Directory exists, now check if the Current Date Directory exists
//	    		tempDirectoriesExist = true;
//	    		if (!currentDateDir.exists())
//	    		{
//	    			//Create the Current Date Directory
//		    		wiiscLog.log(wiiscLog.INFO, "Current Date directory does not exist and needs created");
//		    		boolean currentDateDirCreated = false;
//		    		currentDateDirCreated = currentDateDir.mkdir();
//		    		if (!currentDateDirCreated)
//		    		{
//		    			wiiscLog.log(wiiscLog.INFO, "Current Date directory could not be created");
//		    			tempDirectoriesExist = false;
//		    		}
//		    		else
//		    		{
//		    			wiiscLog.log(wiiscLog.INFO, "Current Date directory has been created");
//		    			tempDirectoriesExist = true;
//		    		}
//	    		}
//	    		else
//	    		{
//	    			tempDirectoriesExist = true;
//	    		}
//	    	}
//		}
//		catch(Exception e)
//	    {
//			wiiscLog.log(wiiscLog.ERROR, "Exception in checkTempDirectories()");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//	    }
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> checkTempDirectories()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return tempDirectoriesExist;
//	}
//	
//	private String convertFileToPDFOrDownload(String inputFilePath, WIISCLog wiiscLog)
//	{
//		File f = null;
//		com.itextpdf.text.Document pdfDocument = new com.itextpdf.text.Document();
//		String outputFileName = "";
//		boolean PDFConvert = false;
//		String fileExtension = "";
//		long contentSize = 0;
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> convertFileToPDFOrDownload()");
//			
//			//List of file extensions that get converted to PDF
//			String convertToPDF = "";
//			String[] convertToPDFData = null;
//			convertToPDF = globalConfig.getString("viewDocImageViewerFileTypesToPDF");
//			convertToPDFData = convertToPDF.split(",");
//			fileExtension = inputFilePath.substring(inputFilePath.lastIndexOf("."));
//			//Determine if we need to convert or download the file based on the file extension
//			for (int i = 0; i < convertToPDFData.length; i++)
//			{
//				String extension = convertToPDFData[i];
//				wiiscLog.log(wiiscLog.INFO, "Checking extension " + extension);
//				if (fileExtension.contains("." + extension))
//				{
//					wiiscLog.log(wiiscLog.INFO, "Extension " + extension + " Found");
//					PDFConvert = true;
//					break;
//				}
//			}
//			
//			//Check if PDFConvert was set
//			if (PDFConvert)
//			{
//				//Setup the inputFile
//				f = new File(inputFilePath);
//				contentSize = f.length();
//				int contentSizeValue = 0;
//				contentSizeValue = (int) Math.max(Math.min(Integer.MAX_VALUE, contentSize), Integer.MIN_VALUE);
//				outputFileName = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".") + 1) + "pdf";
//				wiiscLog.log(wiiscLog.INFO, "Creating the PDF " + outputFileName);
//				PdfWriter writer = PdfWriter.getInstance(pdfDocument, new FileOutputStream(outputFileName));
//				writer.setStrictImageSequence(true);
//				writer.setCompressionLevel(0);
//				Rectangle rect = writer.getPageSize();
//							
//				int filesAdded = 0;
//				
//				try
//				{
//					FileInputStream fstream = new FileInputStream(f);
//					FileChannelRandomAccessSource source = new FileChannelRandomAccessSource(fstream.getChannel());
//					RandomAccessFileOrArray file = new RandomAccessFileOrArray(source);
//					//Image Object
//					Image img = null;
//					
//					//TIFF
//					if (fileExtension.contains("tif"))
//					{
//						int pages = TiffImage.getNumberOfPages(file);
//						for (int page = 1; page <= pages; page++)
//						{
//							//Image Object
//							img = TiffImage.getTiffImage(file, page, false);
//							//img = TiffImage.getTiffImage(file, page);
//							
//							if (img != null)
//							{
//								//Increment the Page
//								if (page > 1)
//								{
//									pdfDocument.newPage();
//								}
//								
//								float imageWidth = 0f;
//								float imageHeight = 0f;
//								
//								//Output Width
//								imageWidth = img.getWidth();
//														
//								//Output Height
//								imageHeight = img.getHeight();
//														
//								//Set the Writer to the size of the Image
//								Rectangle imageBox = new Rectangle(0f,0f,imageWidth,imageHeight);
//								writer.setBoxSize("imageBox", imageBox);
//								
//								//Document Page Size and Margins
//								pdfDocument.setPageSize(imageBox);
//								pdfDocument.setMargins(0f, 0f, 0f, 0f);
//								//Align the Image
//								img.setAlignment(Image.MIDDLE);
//								//Open PDF Document Object
//								pdfDocument.open();
//								if (pdfDocument.add(img))
//								{
//									filesAdded++;
//								}
//								//Release
//								img = null;
//							}
//							
//						}
//					}
//					else if (fileExtension.contains("jp"))
//					{
//						//Image Object
//						img = Image.getInstance(getStreamAsByteArray(fstream,contentSizeValue,wiiscLog));
//						
//						if (img != null) 
//						{
//							float imageWidth = 0f;
//							float imageHeight = 0f;
//
//							//Output Width
//							imageWidth = img.getWidth();
//							
//							//Output Height
//							imageHeight = img.getHeight();
//							
//							//Set the Writer to the size of the Image
//							Rectangle imageBox = new Rectangle(0f,0f,imageWidth,imageHeight);
//							writer.setBoxSize("imageBox", imageBox);
//							
//							//Document Page Size and Margins
//							pdfDocument.setPageSize(imageBox);
//							pdfDocument.setMargins(0f, 0f, 0f, 0f);
//							//Align the Image
//							img.setAlignment(Image.MIDDLE);
//							//Open the iText Document
//							pdfDocument.open();
//							//Add the Image to the Document
//							if (pdfDocument.add(img))
//							{
//								filesAdded++;
//							}
//							//Release
//							img = null;
//						}
//					}
//					else if (fileExtension.contains("bmp"))
//					{
//						//Image Object				
//						img = BmpImage.getImage(getStreamAsByteArray(fstream,contentSizeValue,wiiscLog));
//												
//						if (img != null)
//						{
//							float imageWidth = 0f;
//							float imageHeight = 0f;
//							
//							//Output Width
//							imageWidth = img.getWidth();
//														
//							//Output Height
//							imageHeight = img.getHeight();
//														
//							//Set the Writer to the size of the Image
//							Rectangle imageBox = new Rectangle(0f,0f,imageWidth,imageHeight);
//							writer.setBoxSize("imageBox", imageBox);
//														
//							//Document Page Size and Margins
//							pdfDocument.setPageSize(imageBox);
//							pdfDocument.setMargins(0f, 0f, 0f, 0f);
//							//Align the Image
//							img.setAlignment(Image.MIDDLE);
//							//Open the iText Document
//							pdfDocument.open();
//							//Add the Image to the Document
//							if (pdfDocument.add(img))
//							{
//								filesAdded++;
//							}
//							//Release
//							img = null;
//						}
//					}
//					else if (fileExtension.contains("gif"))
//					{
//						//Image Object
//						GifImage gifImg = null;
//												
//						//Image Object
//						gifImg = new GifImage(getStreamAsByteArray(fstream,contentSizeValue,wiiscLog));
//						
//						if (gifImg != null)
//						{
//							for (int i = 1; i <= gifImg.getFrameCount(); i++)
//							{
//								img = Image.getInstance(gifImg.getImage(i));
//								if (img != null)
//								{
//									float imageWidth = 0f;
//									float imageHeight = 0f;
//									
//									//Output Width
//									imageWidth = img.getWidth();
//																		
//									//Output Height
//									imageHeight = img.getHeight();
//																		
//									//Set the Writer to the size of the Image
//									Rectangle imageBox = new Rectangle(0f,0f,imageWidth,imageHeight);
//									writer.setBoxSize("imageBox", imageBox);
//																		
//									//Document Page Size and Margins
//									pdfDocument.setPageSize(imageBox);
//									pdfDocument.setMargins(0f, 0f, 0f, 0f);
//									//Align the Image
//									img.setAlignment(Image.MIDDLE);
//									//Open the iText Document
//									pdfDocument.open();
//									//Add the Image to the Document
//									if (pdfDocument.add(img))
//									{
//										filesAdded++;
//									}
//									//Release
//									img = null;
//								}
//							}
//							//Release
//							gifImg = null;
//						}
//					}
//					else if (fileExtension.contains("png"))
//					{
//						//Image Object				
//						img = PngImage.getImage(getStreamAsByteArray(fstream,contentSizeValue,wiiscLog));
//												
//						if (img != null)
//						{
//							float imageWidth = 0f;
//							float imageHeight = 0f;
//							
//							//Output Width
//							imageWidth = img.getWidth();
//														
//							//Output Height
//							imageHeight = img.getHeight();
//														
//							//Set the Writer to the size of the Image
//							Rectangle imageBox = new Rectangle(0f,0f,imageWidth,imageHeight);
//							writer.setBoxSize("imageBox", imageBox);
//														
//							//Document Page Size and Margins
//							pdfDocument.setPageSize(imageBox);
//							pdfDocument.setMargins(0f, 0f, 0f, 0f);
//							//Align the Image
//							img.setAlignment(Image.MIDDLE);
//							//Open the iText Document
//							pdfDocument.open();
//							//Add the Image to the Document
//							if (pdfDocument.add(img))
//							{
//								filesAdded++;
//							}
//							//Release
//							img = null;
//						}
//					}
//					wiiscLog.log(wiiscLog.INFO, "PDF Created with " + filesAdded + " Page(s)");
//					//Close RandomAccessFileOrArray
//					file.close();
//					//Close FileChannelRandomAccessSource
//					source.close();
//					//Close the FileInputStream
//					fstream.close();
//				}
//				catch (IOException e)
//				{
//					wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//				}
//				
//				//Close PDF Document Object
//				pdfDocument.close();
//			}
//			else
//			{
//				//Setup URL to Download the File
//				outputFileName = inputFilePath;
//			}
//		}
//		catch (FileNotFoundException ex1)
//		{
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(ex1));
//		}
//		catch (DocumentException ex2)
//		{
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(ex2));
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> convertFileToPDFOrDownload()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");		
//		return outputFileName;
//	}
//	
//	private FnDocumentList getFnDocumentListFromXML(String xmlPath, WIISCLog wiiscLog)
//	{
//		FnDocumentList fnDocumentList = new FnDocumentList();
//		FnBaseXML fnBaseXML = new FnBaseXML();
//		//InputStream to read the XML
//		FileInputStream is = null;
//        try
//        {
//        	wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> getFnDocumentListFromXML()");
//        	is = new FileInputStream(xmlPath);
//        	JAXBContext jaxbContext = JAXBContext.newInstance(FnBaseXML.class);
//    		unmarshaller = jaxbContext.createUnmarshaller();
//    		fnBaseXML = (FnBaseXML) unmarshaller.unmarshal(new StreamSource(is));
//    		
//    		if (fnBaseXML.getFnDocumentList().size() > 0)
//    		{
//    			for (int i = 0; i < fnBaseXML.getFnDocumentList().size(); i++)
//    			{
//    				FnDocumentList oneFnDocumentList = new FnDocumentList();
//    				oneFnDocumentList = fnBaseXML.getFnDocumentList().get(i);
//    				//Add FnDocumentList
//    				fnDocumentList.addFnDocumentList(oneFnDocumentList);
//    			}
//    		}
//        }
//        catch (Exception e)
//        {
//        	wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//        }
//        finally
//        {
//            if (is != null) {
//                try {
//					is.close();
//				} catch (IOException e) {
//					wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//				}
//            }
//        }
//        wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> getFnDocumentListFromXML()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnDocumentList;
//	}
//	private static byte[] getStreamAsByteArray(InputStream is, int contentSize, WIISCLog wiiscLog)
//    {
//    	int read = 0;
//		byte[] bytes = new byte[contentSize];
//		
//		try
//		{
//			while (read != bytes.length)
//			{
//				read = is.read(bytes, 0, bytes.length);
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//		}
//		
//    	return bytes;
//    }
//	
//	private String exportDocument(ObjectStore os, String docId, WIISCLog wiiscLog)
//	{
//		//Get the Document Content from the Object Store and Save to a Local File
//		String exportedFile = "";
//		File f = null;
//		
//		//FileNet Document for the Document ID
//		Document document = null;
//				
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> exportDocument()");
//			//Get the FileNet Document
//			document = Factory.Document.fetchInstance(os, new Id(docId), null);
//			//Verify the Document was found
//			if (document != null)
//			{
//				wiiscLog.log(wiiscLog.INFO, "Document Found");
//				
//				boolean tempDirectoriesExist = false;
//				
//				//Check to make sure the Temp Files Directories exist
//				tempDirectoriesExist = checkTempDirectories(globalConfig, wiiscLog);
//				
//				if (tempDirectoriesExist)
//				{
//					//Get the Content Element List
//					ContentElementList cel = null;
//					cel = document.get_ContentElements();
//					Iterator iter = cel.iterator();
//					int elementCount = 0;
//					String randomFileNamePart = "";
//					randomFileNamePart = getDateTime("yyyyMMddHHmmssSSS") + new Random().nextInt(100000);
//					while (iter.hasNext())
//					{
//					    ContentTransfer ct = (ContentTransfer) iter.next();
//					    //Increment elementCount
//					    elementCount++;
//					    //Print element sequence number and content type of the element.
//					    wiiscLog.log(wiiscLog.INFO, "Element Sequence Number: " + ct.get_ElementSequenceNumber().intValue());
//					    String mime = "";
//					    String mimeExt = "";
//					    String storedFileName = "";
//					    String generatedFileName = "";
//					    
//					    mime = ct.get_ContentType();
//					    wiiscLog.log(wiiscLog.INFO, "Content Type: " + mime);
//					    
//					    //Get the MIME Extension
//					    mimeExt = getMimeTypeExtension(mime, wiiscLog);
//					    
//					    //Get the Stored Filename
//					    storedFileName = ct.get_RetrievalName();
//					    wiiscLog.log(wiiscLog.INFO, "Stored Filename: " + storedFileName + " with extension " + storedFileName.substring(storedFileName.lastIndexOf(".")));
//					    
//					    //Check to make sure a valid MIME Type was Found
//					    if (mimeExt.equals(".bad"))
//					    {
//					    	//wiiscLog.log(wiiscLog.INFO, "Invalid MIME Type Found...exiting");
//					    	//break;
//					    	mimeExt = storedFileName.substring(storedFileName.lastIndexOf("."));
//					    }
//					    //Create generatedFileName
//					    generatedFileName = globalConfig.getString("wiiscAppRootDirectory") + "/" + globalConfig.getString("wiiscTempFileDir") + "/" + getDateWithDashes() + "/" + storedFileName.substring(0, storedFileName.length() - 4) + elementCount + randomFileNamePart + mimeExt;
//					    wiiscLog.log(wiiscLog.INFO, "Generated Filename: " + generatedFileName);
//					    
//					    // Get and print the content of the element.
//					    InputStream inputStream = null;
//					    OutputStream outputStream = null;
//						
//						inputStream = ct.accessContentStream();
//						wiiscLog.log(wiiscLog.INFO, "Content Stream accessed");
//						//String readStr = "";
//					    try
//					    {
//					    	//Create the Temp Image File
//					    	f = new File(generatedFileName);
//							outputStream = new FileOutputStream(f);
//							wiiscLog.log(wiiscLog.INFO, "Output Stream initialized");
//					    	
//							//New way to copy inputStream to outputStream
//							if (globalConfig.getString("viewDocImageViewerUseIOUtils").equals("true"))
//							{
//								wiiscLog.log(wiiscLog.INFO, "Copy using IOUtils");
//								IOUtils.copy(inputStream, outputStream);
//							}
//							else
//							{
//								//Old way
//								int read = 0;
//								int byteSize = 1024;
//								byte[] bytes = null;
//								//Check if the size was used in the GlobalConfig
//								if (globalConfig.getString("viewDocImageViewerBufferSize").length() > 0)
//								{
//									String buffer = "";
//									buffer = globalConfig.getString("viewDocImageViewerBufferSize");
//									wiiscLog.log(wiiscLog.INFO, "Copy using byteSize " + buffer);
//									byteSize = Integer.parseInt(buffer);
//								}
//								else
//								{
//									byteSize = 1024;
//									wiiscLog.log(wiiscLog.INFO, "Copy using byteSize 1024");
//								}
//								//Set the Byte Size
//								bytes = new byte[byteSize];
//								
//								while ((read = inputStream.read(bytes)) != -1) {
//									outputStream.write(bytes, 0, read);
//								}
//							}
//					    	
//					    	//Release File Object
//							if (f.exists() && f.length() > 0)
//							{
//								wiiscLog.log(wiiscLog.INFO, "Output File Created");
//								//Save the Generated File Path
//								exportedFile = generatedFileName;
//								//Close the File Object
//								f = null;
//							}
//							//Release CT Object
//							ct = null;
//					    }
//					    catch(IOException ioe)
//					    {
//					    	wiiscLog.log(wiiscLog.ERROR, "IOException - catch");
//					    	wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(ioe));
//					    }
//					    finally
//					    {
//							if (inputStream != null)
//							{
//								try
//								{							
//									inputStream.close();
//								}
//								catch (IOException e)
//								{
//									wiiscLog.log(wiiscLog.ERROR, "IOException - inputStream - finally");
//									wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//								}
//							}
//							if (outputStream != null)
//							{
//								try
//								{
//									outputStream.flush();
//									outputStream.close();
//								}
//								catch (IOException e)
//								{
//									wiiscLog.log(wiiscLog.ERROR, "IOException - outputStream - finally");
//									wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//								}
//							}
//						}//End Finally 
//				     }//End While
//				}//End TempDirectoriesExist
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "The Temp Files Directories failed to be created");
//				}
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> exportDocument()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		
//		return exportedFile;
//	}
//			
//	private Document importDocument(ObjectStore os, FnDocument fnDocumentRequest, WIISCLog wiiscLog)
//	{
//		//Create the Document
//		Document document = null;
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> importDocument()");
//			//Document Name
//			String docName = fnDocumentRequest.getFnDocumentName();
//			wiiscLog.log(wiiscLog.INFO, "Doc Name: " + docName);
//			//Get the Document Title Character limit			
//			String documentTitleCharacterLimit = "";
//			documentTitleCharacterLimit = globalConfig.getString("documentTitleCharacterLimit");
//			if (documentTitleCharacterLimit.length() == 0)
//			{
//				documentTitleCharacterLimit = "255";
//			}
//			//Check Document Name Length and truncate if necessary.
//			//Document Title has a max of 255 characters and Document Container will use 44 characters + 20 for DateTime
//			//If the Document Name is greater than 255 characters this creates a problem and an undefined Document Name
//			if (docName.length() > Integer.parseInt(documentTitleCharacterLimit))
//			{
//				//Call truncateName method to shrink the string to the specified length
//				docName = truncateName(docName, Integer.parseInt(documentTitleCharacterLimit));
//				wiiscLog.log(wiiscLog.INFO, "Document Name exceeded " + documentTitleCharacterLimit + " characters");
//				wiiscLog.log(wiiscLog.INFO, "New Doc Name: " + docName);
//			}
//			
//			//Document Class
//			String docClassName = fnDocumentRequest.getFnDocumentClass();
//			wiiscLog.log(wiiscLog.INFO, "Doc Class Name: " + docClassName);
//			//Get the Files to use for the Document
//			FnFileList fnFileList = fnDocumentRequest.getFnFilesList();
//			wiiscLog.log(wiiscLog.INFO, "File List: " + fnFileList.getFnFilesList());
//			//Get the Document Properties
//			FnPropertyList fnPropertyList = fnDocumentRequest.getFnPropertyList();
//			
//			//Check if its a valid Document Class
//			if (checkDocClassExists(os, docClassName, wiiscLog))
//			{
//				String filePath = fnFileList.getFnFilesList().get(0);
//				//Import File - should only be 1 in the list
//				File importFile = new File(filePath);
//				//Check if the Import File exists
//				if (importFile.exists())
//				{
//					//Create the FileNet Document
//					document = createDocument(os, docClassName, importFile, fnPropertyList, wiiscLog);
//					//Check if the FileNet Document created successfully
//					if (document != null)
//					{
//						//Update the Document Name
//						if (docName != null)
//						{
//							document.getProperties().putValue("DocumentTitle", docName);
//							wiiscLog.log(wiiscLog.INFO, "Applying Doc Name " + docName);
//							document.save(RefreshMode.NO_REFRESH);
//						}
//						else
//						{
//							//Default Name - <DocClassName>Document-yyyyMMddHHmmss
//							docName = docClassName + "_Document_" + getDateTime("yyyyMMdd_HHmmssSSS");
//							document.getProperties().putValue("DocumentTitle", docName);
//							wiiscLog.log(wiiscLog.INFO, "Applying Doc Name " + docName);
//							document.save(RefreshMode.NO_REFRESH);
//						}
//						//Update the Document ID
//						//fnDocument.setFnDocumentID(document.getProperties().getIdValue("ID").toString());
//						//Update the Status
//						//fnDocument.setErrorFlag(0);
//						//Update the ErrorMessage
//						//fnDocument.setErrorMessage("");
//					}
//					else
//					{
//						//Delete the Document
//						if (deleteDocument(document, wiiscLog))
//						{
//							wiiscLog.log(wiiscLog.INFO, "Document failed to import in the Object Store - FileNet Document Deleted");
//							wiiscLog.log(wiiscLog.INFO, "===========================================================");
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "Document failed to import in the Object Store");
//							wiiscLog.log(wiiscLog.INFO, "FileNet Document failed to delete. Manual delete needed");
//							wiiscLog.log(wiiscLog.INFO, "===========================================================");
//						}
//						wiiscLog.log(wiiscLog.INFO, "Failed to create the FileNet Document");
//						//Update the Status
//						//fnDocument.setErrorFlag(1);
//						//Update the ErrorMessage
//						//fnDocument.setErrorMessage("Failed to create the FileNet Document");
//					}
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "The Import File " + fnDocumentRequest.getFnDocumentURL() + " is missing for the Document");
//					//Update the Status
//					//fnDocument.setErrorFlag(1);
//					//Update the ErrorMessage
//					//fnDocument.setErrorMessage("The Import File " + fnDocumentRequest.getFnDocumentURL() + " is missing for the Document");
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "The Document Class " + docClassName + " is invalid for the Document");
//				//Update the Status
//				//fnDocument.setErrorFlag(1);
//				//Update the ErrorMessage
//				//fnDocument.setErrorMessage("The Document Class " + docClassName + " is invalid for the Document");
//			}
//		}
//		catch (Exception e)
//		{
//			deleteDocument(document, wiiscLog);
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			//fnDocument.setErrorFlag(1);
//			//Update the ErrorMessage
//			//fnDocument.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> importDocument()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return document;
//	}
//	
//	private boolean deleteFolder(ObjectStore os, String folderPath, WIISCLog wiiscLog)
//	{
//		boolean folderDeleted = false;
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> deleteFolder()");
//			//Get the Folder Object
//			Folder folder = Factory.Folder.getInstance(os, null, folderPath);
//			//Delete the Folder
//			folder.delete();
//			folder.save(RefreshMode.NO_REFRESH);
//			folder = null;
//			//Update the Boolean
//			folderDeleted = true;
//			wiiscLog.log(wiiscLog.INFO, "Folder Deleted Successfully");
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update the Boolean
//			folderDeleted = false;
//			wiiscLog.log(wiiscLog.ERROR, "Folder Failed to Delete");
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> deleteFolder()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return folderDeleted;
//	}
//	
//	private boolean deleteDocument(Document document, WIISCLog wiiscLog)
//	{
//		boolean docDeleted = false;
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> deleteDocument()");
//			//Delete the Document
//			document.delete();
//			document.save(RefreshMode.NO_REFRESH);
//			document = null;
//			//Update the Boolean
//			docDeleted = true;
//			wiiscLog.log(wiiscLog.INFO, "Document Deleted Successfully");
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update the Boolean
//			docDeleted = false;
//			wiiscLog.log(wiiscLog.ERROR, "Document Failed to Delete");
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> deleteDocument()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return docDeleted;
//	}
//	
//	private FnDocument indexDocument(Document document, FnDocument fnDocumentRequest, WIISCLog wiiscLog)
//	{
//		//Create the FnDocument
//		FnDocument fnDocument = new FnDocument();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> indexDocument()");
//			//Get 1 FnDocument's Properties
//			FnPropertyList fnPropertyListRequest = fnDocumentRequest.getFnPropertyList();
//			List<FnProperty> fnDocumentPropsListRequest = fnPropertyListRequest.getFnDocumentPropsList();
//			//Refresh the Document Properties
//			document.refresh();
//			//Get Doc Properties from FileNet
//			Properties properties = document.getProperties();
//			//FnDocument Property List
//			FnPropertyList fnPropertyList = new FnPropertyList();
//			
//			//Loop through and process Document Properties
//			for (int j = 0; j < fnDocumentPropsListRequest.size(); j++)
//			{
//				FnProperty fnProperty = fnDocumentPropsListRequest.get(j);
//				//Check if fnProperty is null
//				if (fnProperty != null)
//				{
//					//Check if the FileNet Property exists on the Document
//					if (properties.isPropertyPresent(fnProperty.getName()))
//					{
//						wiiscLog.log(wiiscLog.INFO, "Property " + fnProperty.getName() + " exists");
//						//Object value = props.getObjectValue(fnProperty.getName());
//						Object value = fnProperty.getValue();
//						wiiscLog.log(wiiscLog.INFO, "Applying Value: " + value.toString());
//						//Update the Document Property
//						properties.putObjectValue(fnProperty.getName(), value);
//						//Add the Property and Value to the FnDocument Property List
//						fnPropertyList.addFnProperty(fnProperty);
//					}
//				}
//			}
//			
//			wiiscLog.log(wiiscLog.INFO, "Finished Applying Values");
//						
//			//Check the FnDocumentRequest status to see if this is for a new or existing
//			wiiscLog.log(wiiscLog.INFO, "Checking Document Status");
//			if (fnDocumentRequest.getFnDocumentStatus() == null)
//			{
//				wiiscLog.log(wiiscLog.INFO, "Document Status is NULL");
//				//Save the Doc - no round trip since this is a new document
//				//document.save(RefreshMode.NO_REFRESH);
//				document.save(RefreshMode.REFRESH);
//			}
//			else if (fnDocumentRequest.getFnDocumentStatus().equals("Update"))
//			{
//				wiiscLog.log(wiiscLog.INFO, "Document Status is Update");
//				//Existing Document to get updated so we save and do a refresh
//				//Save the Doc - a round trip since this is an existing document
//				document.save(RefreshMode.REFRESH);
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Document Status is New/Existing");
//				//Save the Doc - no round trip since we are not sure if this is a new or existing
//				document.save(RefreshMode.NO_REFRESH);
//			}
//			
//			//Add the FnPropertyList to the FnDocument
//			//fnDocument.setFnDocPropsList(fnPropertyList.getFnDocumentPropsList());
//			wiiscLog.log(wiiscLog.INFO, "Adding Property List to FnDocument");
//			fnDocument.setFnPropertyList(fnPropertyList);
//			//Update the Document Class, ID and Name
//			wiiscLog.log(wiiscLog.INFO, "Document Class is " + document.getClassName());
//			fnDocument.setFnDocumentClass(document.getClassName());
//			wiiscLog.log(wiiscLog.INFO, "Document ID is " + document.get_Id().toString());
//			fnDocument.setFnDocumentID(document.get_Id().toString());
//			wiiscLog.log(wiiscLog.INFO, "Document Name is " + document.get_Name());
//			fnDocument.setFnDocumentName(document.get_Name());
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnDocument.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnDocument.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> indexDocument()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnDocument;
//	}
//	
//	
//	private void outputRequestToLog(Map<String, String> requestMap, WIISCLog wiiscLog)
//	{
//		wiiscLog.log(wiiscLog.INFO, "=========================================");
//		wiiscLog.log(wiiscLog.INFO, "REQUEST VALUES");
//		wiiscLog.log(wiiscLog.INFO, "=========================================");
//		
//		//Check the Map size
//		if (requestMap.size() > 0)
//		{
//			for (Map.Entry<String, String> entry : requestMap.entrySet()) {
//				wiiscLog.log(wiiscLog.INFO, "Name: " + entry.getKey());
//				wiiscLog.log(wiiscLog.INFO, "Value: " + entry.getValue());
//			}
//		}
//		else
//		{
//			wiiscLog.log(wiiscLog.INFO, "There were no values passed in the Request");
//		}
//		
//		wiiscLog.log(wiiscLog.INFO, "=========================================");
//	}
//	
//	public FnBaseXML commitBatchDocumentsWithWorkflows(FnBatch fnBatchRequest, FnWorkflowList fnWorkflowListRequest, WIISCLog wiiscLog)
//	{
//		FnBaseXML fnBaseXML = new FnBaseXML();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> commitBatchDocumentsWithWorkflows()");
//			
//			//Call commitBatchDocuments
//			FnBatchList fnBatchList = new FnBatchList();
//			fnBatchList = commitBatchDocuments(fnBatchRequest, wiiscLog);
//			
//			//Check to make sure FnBatchList has a size
//			if (fnBatchList.getErrorFlag() == 0)
//			{
//				//Batch of Documents committed successfully
//				wiiscLog.log(wiiscLog.INFO, "FileNet Document Committed Successfully");
//				//Update errorFlag
//				fnBatchList.setErrorFlag(0);
//				//Update the ErrorMessage
//				fnBatchList.setErrorMessage("FileNet Document Committed Successfully");
//				//Add the FnBatchList to the FnBaseXML
//				fnBaseXML.addFnBatchList(fnBatchList);
//				//FnWorkflow List Results
//				FnWorkflowList fnWorkflowList = new FnWorkflowList();
//				//Initiate Workflows for the Documents
//				List<FnWorkflow> fnWorkflows = new ArrayList<FnWorkflow>(10);
//				fnWorkflows = fnWorkflowListRequest.getFnWorkflowList();
//				//Launch each Workflow
//				for (int i = 0; i < fnWorkflows.size(); i++)
//				{
//					//Get a Workflow to launch
//					FnWorkflow fnWorkflow = new FnWorkflow();
//					fnWorkflow = fnWorkflows.get(i);
//					FnWorkflow fnWorkflowResult = new FnWorkflow();
//					//Setup GlobalConfig for WorkflowManager
//					workflowManager.setGlobalConfig(getGlobalConfig());
//					fnWorkflowResult = workflowManager.initiateWorkflow(fnWorkflow, wiiscLog);
//					//Add the FnWorkflow to the FnWorkflowList
//					fnWorkflowList.addFnWorkflow(fnWorkflowResult);
//				}
//				//Check to make sure there were No Errors
//				if (fnWorkflowList.getErrorFlag() == 0)
//				{
//					//Workflows initiated successfully
//					wiiscLog.log(wiiscLog.INFO, "FileNet Workflow Initiated Successfully");
//					//Update errorFlag
//					fnWorkflowList.setErrorFlag(0);
//					//Update the ErrorMessage
//					fnWorkflowList.setErrorMessage("FileNet Workflow Initiated Successfully");
//					//Add the FnWorkflowList to the FnBaseXML
//					fnBaseXML.addFnWorkflowList(fnWorkflowList);
//				}
//				else
//				{
//					//Workflows did not initiate successfully
//					wiiscLog.log(wiiscLog.ERROR, "FileNet Workflow Initiate Failed");
//					//Update errorFlag
//					fnWorkflowList.setErrorFlag(1);
//					//Update the ErrorMessage
//					fnWorkflowList.setErrorMessage("FileNet Workflow Initiate Failed");
//					//Add the FnWorkflowList to the FnBaseXML
//					fnBaseXML.addFnWorkflowList(fnWorkflowList);
//				}
//			}
//			else
//			{
//				//Batch of Documents did not commit successfully
//				wiiscLog.log(wiiscLog.ERROR, "FileNet Document Commit Failed");
//				//Update errorFlag
//				fnBatchList.setErrorFlag(1);
//				//Update the ErrorMessage
//				fnBatchList.setErrorMessage("FileNet Document Commit Failed");
//				//Add the FnBatchList to the FnBaseXML
//				fnBaseXML.addFnBatchList(fnBatchList);
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnBaseXML.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnBaseXML.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> commitBatchDocumentsWithWorkflows()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		
//		return fnBaseXML;
//	}
//	
//	public FnBatchList createBatches(String batchClass, String batchCount, String documentCount, WIISCLog wiiscLog)
//	{
//		//List of FileNet Batches
//		FnBatchList fnBatchList = new FnBatchList();
//		//Get an ObjectStore Object
//		ObjectStore os = null;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> createBatches()");
//
//			//Update errorFlag
//			fnBatchList.setErrorFlag(0);
//			//Update the ErrorMessage
//			fnBatchList.setErrorMessage("");
//
//			//Number of Batches
//			int numOfBatches = 0;
//			numOfBatches = Integer.parseInt(batchCount);
//			//Number of Documents
//			int numOfDocuments = 0;
//			numOfDocuments = Integer.parseInt(documentCount);
//
//			//Batches Root Location e.g. /_TO_BE_INDEXED
//			String batchesRoot = "";
//			batchesRoot = globalConfig.getString("indexingBatchesRootLocation");
//
//			//Batch Base Name yyyyMMdd_HHmmss
//			String batchBaseName = "Batch" + getDateTime("yyyyMMdd");
//			//Batch Name Property e.g. Batch_ID
//			String batchNameDocProp = "";
//			batchNameDocProp = globalConfig.getString("indexingBatchNameProperty");
//
//			//Document Location Property e.g. Doc_Location
//			String docLocationProp = "";
//			docLocationProp = globalConfig.getString("documentPropFiledFolderLocation");
//
//			//Test XML Location
//			String testDocumentsPath = "";
//			testDocumentsPath = globalConfig.getString("wiiscAppRootDirectory") + "/" + globalConfig.getString("wiiscTestDocsDir");
//
//			//FnDocumentList used for the Test XML
//			FnDocumentList masterFnDocumentList = new FnDocumentList();
//			//Get the FnDocumentList from the XML
//			masterFnDocumentList = getFnDocumentListFromXML(testDocumentsPath + "/Test.xml", wiiscLog);
//
//			//Output FnDocumentList
//			outputFnDocumentList(masterFnDocumentList, wiiscLog);
//
//			boolean goodFolder = false;
//			int batchCounter = 1;
//			int previousBatchCounter = 0;
//			String batchName = "";
//			String batchPath = "";
//			String documentName = "";
//			
//			//Loop Batch Count
//			for (int i = 0; i < numOfBatches; i++)
//			{
//				//Setup the Batch Name
//				batchName = "";
//				//Set the Object Store Batch Path
//				batchPath = "";
//				documentName = "";
//				goodFolder = false;
//				//Check to see if we use the previousBatchCounter
//				if (i > 0)
//				{
//					batchCounter = previousBatchCounter;
//				}
//
//				//Login to the Imaging Server
//				os = loginImaging(wiiscLog);
//
//				//Verify Object Store Connected
//				if (os != null)
//				{
//					while (!goodFolder)
//					{
//						//Update batchName
//						batchName = batchBaseName + batchCounter;
//						wiiscLog.log(wiiscLog.INFO, "BatchName: " + batchName);
//						//Update batchPath
//						batchPath = batchesRoot + "/" + batchName;
//						wiiscLog.log(wiiscLog.INFO, "BatchPath: " + batchPath);
//						if (!checkFolderExists(os, batchPath, wiiscLog))
//						{
//							//Create Batch Folder
//							createFolder(os, batchesRoot, batchName, wiiscLog);
//							//Update Good Folder
//							goodFolder = true;
//							//Reset Object Store variable
//							os = null;
//							//Exit while
//							break;
//						}
//						//Update batchCounter
//						batchCounter++;
//					}
//
//					//Save previousBatchCounter
//					previousBatchCounter = batchCounter;
//
//					//Create the FnBatch
//					FnBatch fnBatch = new FnBatch();
//					//FnDocumentList Running copy
//					//FnDocumentList fnDocumentList = new FnDocumentList();
//					FnDocumentList currentFnDocumentList = copyFnDocumentList(masterFnDocumentList, wiiscLog);
//					//Output FnDocumentList
//					//outputFnDocumentList(masterFnDocumentList, wiiscLog);
//					//outputFnDocumentList(currentFnDocumentList, wiiscLog);
//					
//					//Update some Document Properties in the original FnDocumentList
//					FnDocumentList fnDocumentListUpdated = new FnDocumentList();
//					List<FnDocument> currentFnDocuments = new ArrayList<FnDocument>(10);
//					currentFnDocuments = currentFnDocumentList.getFnDocumentList();
//					for (int z = 0; z < currentFnDocuments.size(); z++)
//					{
//						//FnDocument
//						//FnDocument fnDocument = new FnDocument();
//						FnDocument fnDocument = currentFnDocuments.get(z);
//						//Update Batch ID property
//						//FnDocument fnDocument1 = new FnDocument();
//						//fnDocument1 = setCustomFnPropertyValue(fnDocument, batchNameDocProp, batchName, wiiscLog);
//						setCustomFnPropertyValue(fnDocument, batchNameDocProp, batchName, wiiscLog);
//						//Update Doc Location property
//						//FnDocument fnDocument2 = new FnDocument();
//						//fnDocument2 = setCustomFnPropertyValue(fnDocument1, docLocationProp, batchPath, wiiscLog);
//						setCustomFnPropertyValue(fnDocument, docLocationProp, batchPath, wiiscLog);
//						//Add Document to Updated FnDocumentList
//						fnDocumentListUpdated.addFnDocument(fnDocument);
//					}
//
//					//Check FnDocumentList to see if more than 1 Document is in the XML
//					//If so, then we will do that number of Documents per Batch instead of the 
//					//passed in value from the request
//					if (fnDocumentListUpdated.getCount() > 1)
//					{
//						wiiscLog.log(wiiscLog.INFO, "Document List has more than 1 Document");
//						//FnDocument List of Imported Documents
//						FnDocumentList fnDocumentListImported = new FnDocumentList();
//						//Call importDocuments
//						fnDocumentListImported = importDocuments(fnDocumentListUpdated, wiiscLog);
//						//Add FnDocumentList to FnBatch
//						fnBatch.addFnDocumentList(fnDocumentListImported);
//					}
//					else if (fnDocumentListUpdated.getCount() == 1)
//					{
//						wiiscLog.log(wiiscLog.INFO, "Document List has only 1 Document");
//						wiiscLog.log(wiiscLog.INFO, "Document Name: " + documentName);
//						documentName = "";
//						//documentName = fnDocumentListUpdated.getFnDocumentList().get(0).getFnDocumentName();
//						//wiiscLog.log(wiiscLog.INFO, "Document Name: " + documentName);
//						List<FnDocument> fnDocuments = new ArrayList<FnDocument>(10);
//						fnDocuments = fnDocumentListUpdated.getFnDocumentList();
//						FnDocument fnDocumentMaster = new FnDocument();
//						for (int x = 0; x < fnDocuments.size(); x++)
//						{
//							fnDocumentMaster = fnDocuments.get(x);
//						}
//						documentName = fnDocumentMaster.getFnDocumentName();
//						wiiscLog.log(wiiscLog.INFO, "Document Name: " + documentName);
//						//Loop Document Count
//						for (int j = 0; j < numOfDocuments; j++)
//						{
//							//FnDocument List of Imported Documents
//							FnDocumentList fnDocumentListImported = new FnDocumentList();
//							String docName = "";
//							docName = documentName;
//							docName = docName + j;
//							//Update Document Name
//							fnDocumentMaster.setFnDocumentName(docName);
//							//FnDocument oldFnDocument = new FnDocument();
//							//oldFnDocument = fnDocumentListUpdated.getFnDocumentList().get(0);
//							//oldFnDocument.setFnDocumentName(documentName + j);
//							fnDocumentListUpdated.getFnDocumentList().remove(0);
//							fnDocumentListUpdated.addFnDocument(fnDocumentMaster);
//							//Call importDocuments
//							fnDocumentListImported = importDocuments(fnDocumentListUpdated, wiiscLog);
//							//Add FnDocumentList to FnBatch
//							fnBatch.addFnDocumentList(fnDocumentListImported);
//							//reset
//							docName = "";
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "No Documents found from the XML.");
//						//Update errorFlag
//						fnBatchList.setErrorFlag(1);
//						//Update the ErrorMessage
//						fnBatchList.setErrorMessage("No Documents found from the XML.");
//						//Exit for loop
//						break;
//					}
//
//					//Add FnBatch to FnBatchList
//					fnBatchList.addFnBatch(fnBatch);
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "Imaging Login FAILED, Imaging Server may be unavailable.");
//					//Update the fnBatchList Object
//					fnBatchList.setErrorFlag(1);
//					//Update the ErrorMessage
//					fnBatchList.setErrorMessage("Imaging Login FAILED, Imaging Server may be unavailable.");
//					//Exit for loop
//					break;
//				}
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnBatchList.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnBatchList.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> createBatches()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnBatchList;
//	}
//	
//	public FnBatchList commitBatchDocuments(FnBatch fnBatchRequest, WIISCLog wiiscLog)
//	{
//		//List of FileNet Batches
//		FnBatchList fnBatchList = new FnBatchList();
//		//Get an ObjectStore Object
//		ObjectStore os = null;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> commitBatchDocuments()");
//
//			//Login to the Imaging Server
//			os = loginImaging(wiiscLog);
//
//			//Verify Object Store Connected
//			if (os != null)
//			{
//				//Update errorFlag
//				fnBatchList.setErrorFlag(0);
//				//Update the ErrorMessage
//				fnBatchList.setErrorMessage("");
//				
//				//Define the Doc Class Properties Resource
//				//ResourceBundle docClassProps = null;
//				if (docClassConfig == null)
//				{
//					LocalResource resConfig = getLocalResource(fnBatchRequest.getFnBatchClass() + "Config");
//					docClassConfig = resConfig.getLocalBundle(resConfig.getBundlePath(), resConfig.getBundleFile());
//				}
//								
//				//List of Documents from the Batch
//				List<FnDocument> fnDocumentList = new ArrayList<FnDocument>(10);
//				//Get the List of Documents from the Batch
//				fnDocumentList = fnBatchRequest.getFnDocumentList();
//				//FnDocument from the Request
//				FnDocument fnDocumentRequest = new FnDocument();
//				//Batch Class
//				String batchClass = "";
//				batchClass = fnBatchRequest.getFnBatchClass();
//				//Batch Name
//				String batchName = "";
//				batchName = fnBatchRequest.getFnBatchName();
//				//Batches Root Location
//				String batchesRoot = "";
//				batchesRoot = globalConfig.getString("indexingBatchesRootLocation");
//				
//				//Get Each Document from the List
//				for (int i = 0; i < fnDocumentList.size(); i++)
//				{
//					//A Document from the list
//					fnDocumentRequest = fnDocumentList.get(i);
//					
//					//FnBatch Object
//					FnBatch fnBatch = new FnBatch();
//					
//					//FnDocument after update1
//					FnDocument fnDocumentUpdated1 = new FnDocument();
//					//FnDocument after update2
//					FnDocument fnDocumentUpdated2 = new FnDocument();
//					//boolean to determine if Document was stored successfully
//					boolean docStored = false;
//					//FileNet Document ID
//					String fileNetDocumentID = "";	
//					//Document ID
//					String fnDocumentID = fnDocumentRequest.getFnDocumentID();
//					
//					//Check if Document GUID ID was passed in the request or a Search is needed based on Properties
//					if (fnDocumentID != null)
//					{
//						//1 FileNet Document
//						Document document = null;
//						
//						wiiscLog.log(wiiscLog.INFO, "Get Document: " + fnDocumentID);
//						
//						//Get the FileNet GUID from the passed in Document ID which may already be a GUID
//						//We still need to verify that its valid
//						fileNetDocumentID = getFNDocumentGUID(os, fnDocumentID, wiiscLog);
//												
//						//Check to make sure the FileNet Document ID was found
//						if (fileNetDocumentID.length() > 0)
//						{
//							wiiscLog.log(wiiscLog.INFO, "FileNet Document GUID Found: " + fileNetDocumentID);
//							//FileNet Document for the Document ID
//							document = Factory.Document.fetchInstance(os, new Id(fileNetDocumentID), null);
//							
//							//Verify the Document was found and perform Indexing
//							if (document != null)
//							{
//								wiiscLog.log(wiiscLog.INFO, "FileNet Document Found");
//															
//								//Get Index Document With Commit Property
//								String indexDocumentWithCommit = "";
//								indexDocumentWithCommit = globalConfig.getString("indexingBatchesIndexDocumentWithCommit");
//								
//								//Check to see if we need to do IndexDocument
//								if (indexDocumentWithCommit.length() > 0 && indexDocumentWithCommit.equals("true"))
//								{
//									wiiscLog.log(wiiscLog.INFO, "Performing Index Document and Store Batch Document");
//									//Check FnProperty Value for Doc_Location in FnDocument
//									String documentPropFiledFolderLocation = "";
//									String propValue = "";
//									//Check if the Document Folder location property is used and get the Document Property that is used for this
//									documentPropFiledFolderLocation = globalConfig.getString("documentPropFiledFolderLocation");
//									wiiscLog.log(wiiscLog.INFO, "documentPropFiledFolderLocation: " + documentPropFiledFolderLocation);
//									//Check FnProperty Value for Doc_Location in FnDocument
//									if (documentPropFiledFolderLocation != null && documentPropFiledFolderLocation.length() > 0)
//									{
//										propValue = getCustomFnPropertyValue(fnDocumentRequest, documentPropFiledFolderLocation, wiiscLog);
//										wiiscLog.log(wiiscLog.INFO, "propValue: " + propValue);
//										//Get the Object Store Folder location based off the Current Date
//										String documentObjectStoreLocation = "";
//										documentObjectStoreLocation = docClassConfig.getString("docClassDocsRootLocation") + "/" + getDateTime("yyyy/MM/dd");
//										wiiscLog.log(wiiscLog.INFO, "documentObjectStoreLocation: " + documentObjectStoreLocation);
//										
//										//Check if the FnDocument contains a value for this Property and if it does not match, then update the FnDocument
//										if (!documentObjectStoreLocation.equals(propValue))
//										{
//											//Update the FnDocument
//											//fnDocumentUpdated1 = setCustomFnPropertyValue(fnDocumentRequest, documentPropFiledFolderLocation, documentObjectStoreLocation, wiiscLog);
//											setCustomFnPropertyValue(fnDocumentRequest, documentPropFiledFolderLocation, documentObjectStoreLocation, wiiscLog);
//											//Do Index Document
//											fnDocumentUpdated2 = indexDocument(document, fnDocumentRequest, wiiscLog);
//											//Do Store Batch Document
//											docStored = storeBatchDocument(os, document, wiiscLog);
//										}
//										else
//										{
//											//Doc Location Property in FnDocument matches the correct document location
//											//Do Index Document
//											fnDocumentUpdated1 = indexDocument(document, fnDocumentRequest, wiiscLog);
//											//Do Store Batch Document
//											docStored = storeBatchDocument(os, document, wiiscLog);
//										}
//									}
//									else
//									{
//										//No Doc Location Property in FnDocument so the filed document location will not be stored with the Document
//										//Do Index Document
//										fnDocumentUpdated1 = indexDocument(document, fnDocumentRequest, wiiscLog);
//										//Do Store Batch Document
//										docStored = storeBatchDocument(os, document, wiiscLog);
//									}
//								}
//								else
//								{
//									wiiscLog.log(wiiscLog.INFO, "Performing Store Batch Document");
//									//No Index Document just do Store Batch Document
//									docStored = storeBatchDocument(os, document, wiiscLog);
//								}
//								
//								//Check if the document was stored successfully
//								if (docStored)
//								{
//									//Document Committed successfully
//									wiiscLog.log(wiiscLog.INFO, "FileNet Document Commit was Successful");
//																	
//									//Get the remaining Documents in the Batch
//									wiiscLog.log(wiiscLog.INFO, "Checking Batch for any remaining Documents");
//									fnBatch = getBatch(os, batchClass, batchName, wiiscLog);
//									
//									//Check FnBatch for more Documents
//									if (fnBatch.getCount() > 0)
//									{
//										wiiscLog.log(wiiscLog.INFO, fnBatch.getCount() + " remaining Documents for this Batch");
//										//Update errorFlag
//										fnBatchList.setErrorFlag(0);
//										//Update the ErrorMessage
//										fnBatchList.setErrorMessage("FileNet Document Commit was Successful");
//									}
//									else
//									{
//										wiiscLog.log(wiiscLog.INFO, "No remaining Documents for this Batch");
//										//Update errorFlag
//										fnBatchList.setErrorFlag(0);
//										//Update the ErrorMessage
//										fnBatchList.setErrorMessage("FileNet Document Commit was Successful. No remaining Documents for this Batch");
//										//Remove the Batch Folder from _TO_BE_INDEXED since it is now empty
//										if (deleteFolder(os, batchesRoot + "/" + batchName, wiiscLog))
//										{
//											wiiscLog.log(wiiscLog.INFO, "Batch Folder " + batchName + " Deleted");
//										}
//										else
//										{
//											wiiscLog.log(wiiscLog.INFO, "Batch Folder " + batchName + " was NOT Deleted");
//										}
//									}
//									
//									//Add the FnBatch to the FnBatchList
//									fnBatchList.addFnBatch(fnBatch);
//								}
//								else
//								{
//									//Document did not commit successfully
//									wiiscLog.log(wiiscLog.ERROR, "FileNet Document Commit Failed");
//									//Update errorFlag
//									fnBatchList.setErrorFlag(1);
//									//Update the ErrorMessage
//									fnBatchList.setErrorMessage("FileNet Document Commit Failed");
//								}
//							}
//							else
//							{
//								wiiscLog.log(wiiscLog.ERROR, "FileNet Document was NOT Found");
//								//Update errorFlag
//								fnBatchList.setErrorFlag(1);
//								//Update the ErrorMessage
//								fnBatchList.setErrorMessage("FileNet Document was NOT Found");
//							}
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.ERROR, "FileNet Document GUID was NOT Found");
//							//Update errorFlag
//							fnBatchList.setErrorFlag(1);
//							//Update the ErrorMessage
//							fnBatchList.setErrorMessage("FileNet Document GUID was NOT Found");
//						}
//					}
//				}//End Document List For Loop
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Imaging Login FAILED, Imaging Server may be unavailable.");
//				//Update the fnBatchList Object
//				fnBatchList.setErrorFlag(1);
//				//Update the ErrorMessage
//				fnBatchList.setErrorMessage("Imaging Login FAILED, Imaging Server may be unavailable.");
//			}
//			//Reset Object Store variable
//			os = null;
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnBatchList.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnBatchList.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> commitBatchDocuments()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnBatchList;
//	}
//	
//	public FnBatchList fileBatchDocuments(String batchClass, String batchName, WIISCLog wiiscLog)
//	{
//		//List of FileNet Batches
//		FnBatchList fnBatchList = new FnBatchList();
//		//FileNet Document ID
//		String fileNetDocumentID = "";
//		//Get an ObjectStore Object
//		ObjectStore os = null;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> fileBatchDocuments()");
//
//			//Login to the Imaging Server
//			os = loginImaging(wiiscLog);
//
//			//Verify Object Store Connected
//			if (os != null)
//			{
//				//Update errorFlag
//				fnBatchList.setErrorFlag(0);
//				//Update the ErrorMessage
//				fnBatchList.setErrorMessage("");
//				
//				//Get the Indexing Batches Root Location from the Config
//				String indexingBatchesRootLocation = "";
//				indexingBatchesRootLocation = globalConfig.getString("indexingBatchesRootLocation");
//				
//				//Set the Object Store Batch Path
//				String batchPath = "";
//				batchPath = indexingBatchesRootLocation + "/" + batchName;
//				
//				//boolean to determine if Document was stored successfully
//				boolean docStored = false;
//				//Get the Batch Folder
//				Folder batchFolder = Factory.Folder.fetchInstance(os, "Unfiled Documents", null);
//				//Get the list of Documents in the Batch Folder - if there are none, then exit
//				DocumentSet documentSet = null;
//				documentSet = batchFolder.get_ContainedDocuments();
//				//Check to see if there are any Documents in the Batch
//				//If there aren't any, then skip everything
//				if (!documentSet.isEmpty())
//				{
//					//Loop through Documents
//					Iterator documentIter = null;
//					documentIter = documentSet.iterator();
//					//Set the Document Object to be reused
//					Document oneDocument = null;
//					//Check for more Documents in the Document Iterator				
//					while (documentIter.hasNext())
//					{
//						//Get a Document
//						oneDocument = (Document) documentIter.next();
//						
//						wiiscLog.log(wiiscLog.INFO, "Performing Create Batch Document");
//						//Create Batch Document to /_TO_BE_INDEXED/BatchName
//						docStored = createBatchDocument(os, oneDocument, batchPath, wiiscLog);
//						
//						//Check if the document was stored successfully
//						if (docStored)
//						{
//							//Document Committed successfully
//							wiiscLog.log(wiiscLog.INFO, "FileNet Document Move to a Batch was Successful");
//							//Update errorFlag
//							fnBatchList.setErrorFlag(0);
//							//Update the ErrorMessage
//							fnBatchList.setErrorMessage("FileNet Document Move to a Batch was Successful");
//
//						}
//						else
//						{
//							//Document did not commit successfully
//							wiiscLog.log(wiiscLog.ERROR, "FileNet Document Move to a Batch Failed");
//							//Update errorFlag
//							fnBatchList.setErrorFlag(1);
//							//Update the ErrorMessage
//							fnBatchList.setErrorMessage("FileNet Document Move to a Batch Failed");
//						}
//					}//End While
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Imaging Login FAILED, Imaging Server may be unavailable.");
//				//Update the fnBatchList Object
//				fnBatchList.setErrorFlag(1);
//				//Update the ErrorMessage
//				fnBatchList.setErrorMessage("Imaging Login FAILED, Imaging Server may be unavailable.");
//			}
//			//Reset Object Store variable
//			os = null;
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnBatchList.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnBatchList.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> fileBatchDocuments()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnBatchList;
//	}
//	
//	public FnBatchList updateBatchDocument(FnBatch fnBatchRequest, WIISCLog wiiscLog)
//	{
//		//List of FileNet Batches
//		FnBatchList fnBatchList = new FnBatchList();
//		//FileNet Document ID
//		String fileNetDocumentID = "";
//		//Get an ObjectStore Object
//		ObjectStore os = null;
//
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> updateBatchDocument()");
//
//			//Login to the Imaging Server
//			os = loginImaging(wiiscLog);
//
//			//Verify Object Store Connected
//			if (os != null)
//			{
//				//Update errorFlag
//				fnBatchList.setErrorFlag(0);
//				//Update the ErrorMessage
//				fnBatchList.setErrorMessage("");
//				
//				//FnBatch Object
//				FnBatch fnBatch = new FnBatch();
//				
//				//FnDocument from the Request
//				FnDocument fnDocumentRequest = new FnDocument();
//				//FnDocument after update
//				FnDocument fnDocumentUpdated = new FnDocument();
//				//boolean to determine if Document was stored successfully
//				boolean docStored = false;
//				
//				//Document ID
//				String fnDocumentID = fnDocumentRequest.getFnDocumentID();
//				
//				//Check if Document GUID ID was passed in the request or a Search is needed based on Properties
//				if (fnDocumentID != null)
//				{
//					//1 FileNet Document
//					Document document = null;
//					
//					wiiscLog.log(wiiscLog.INFO, "Get Document: " + fnDocumentID);
//					
//					//Get the FileNet GUID from the passed in Document ID which may already be a GUID
//					//We still need to verify that its valid
//					fileNetDocumentID = getFNDocumentGUID(os, fnDocumentID, wiiscLog);
//											
//					//Check to make sure the FileNet Document ID was found
//					if (fileNetDocumentID.length() > 0)
//					{
//						wiiscLog.log(wiiscLog.INFO, "FileNet Document GUID Found: " + fileNetDocumentID);
//						//FileNet Document for the Document ID
//						document = Factory.Document.fetchInstance(os, new Id(fileNetDocumentID), null);
//						
//						//Verify the Document was found and perform Indexing
//						if (document != null)
//						{
//							wiiscLog.log(wiiscLog.INFO, "FileNet Document Found");
//
//							//Do Index Document
//							fnDocumentUpdated = indexDocument(document, fnDocumentRequest, wiiscLog);
//							//Check if the Error Flag was set
//							if (fnDocumentUpdated.getErrorFlag() > 0)
//							{
//								//Process Error - report bad indexing
//								wiiscLog.log(wiiscLog.INFO, "Document failed to update in the Object Store");
//								wiiscLog.log(wiiscLog.INFO, "===========================================================");
//								//Update the Document Status
//								fnDocumentUpdated.setFnDocumentStatus("Document failed to update in the Object Store");
//								fnDocumentUpdated.setErrorFlag(1);
//								fnDocumentUpdated.setErrorMessage("Document failed to update in the Object Store");
//							}
//							else
//							{
//								//No Error
//								wiiscLog.log(wiiscLog.INFO, "Document successfully updated in the Object Store");
//								wiiscLog.log(wiiscLog.INFO, "===========================================================");
//								//Update the Document Status
//								fnDocumentUpdated.setFnDocumentStatus("Document successfully updated in the Object Store");
//								fnDocumentUpdated.setErrorFlag(0);
//								fnDocumentUpdated.setErrorMessage("Document successfully updated in the Object Store");
//							}
//							
//							//Add the FnDocument back to the FnBatchList
//							fnBatch.addFnDocument(fnDocumentUpdated);
//
//							//Add the FnBatch to the FnBatchList
//							fnBatchList.addFnBatch(fnBatch);
//							
//							//Check if the document was updated successfully
//							if (fnBatchList.getErrorFlag() > 0)
//							{
//								//Document did not index successfully
//								wiiscLog.log(wiiscLog.ERROR, "FileNet Document indexing failed");
//								//Update errorFlag
//								fnBatchList.setErrorFlag(1);
//								//Update the ErrorMessage
//								fnBatchList.setErrorMessage("FileNet Document indexing failed");
//							}
//							else
//							{								
//								//Document Indexed successfully
//								wiiscLog.log(wiiscLog.INFO, "FileNet Document indexing was successful");
//								//Update errorFlag
//								fnBatchList.setErrorFlag(0);
//								//Update the ErrorMessage
//								fnBatchList.setErrorMessage("FileNet Document indexing was successful");
//							}
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "FileNet Document was NOT Found");
//						}
//					}
//					else
//					{
//						wiiscLog.log(wiiscLog.INFO, "FileNet Document GUID was NOT Found");
//					}
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Imaging Login FAILED, Imaging Server may be unavailable.");
//				//Update the fnBatchList Object
//				fnBatchList.setErrorFlag(1);
//				//Update the ErrorMessage
//				fnBatchList.setErrorMessage("Imaging Login FAILED, Imaging Server may be unavailable.");
//			}
//			//Reset Object Store variable
//			os = null;
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			fnBatchList.setErrorFlag(1);
//			//Update the ErrorMessage
//			fnBatchList.setErrorMessage(e.getMessage());
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> updateBatchDocument()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnBatchList;
//	}
//	
//	private boolean storeDocument(ObjectStore os, Document document, FnDocument fnDocument, String docLocation, WIISCLog wiiscLog)
//	{
//		//boolean for the document stored and filed
//		boolean docStored = false;
//		//Define the Doc Class Properties Resource
//		//ResourceBundle docClassProps = null;
//		//docClassProps = ResourceBundle.getBundle(document.getClassName() + "Config");
//		if (docClassConfig == null)
//		{
//			LocalResource resConfig = getLocalResource(document.getClassName() + "Config");
//			docClassConfig = resConfig.getLocalBundle(resConfig.getBundlePath(), resConfig.getBundleFile());
//		}
//				
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> storeDocument()");
//			
//			//Get the Document Container Name based off the Document Name and the Current Date and Time
//			String documentContainerName = "";
//			String tempDocumentContainerName = "";
//			//Fix Document Name to remove spaces and special characters to truncate the name to be shorter
//			//using only the Upper case characters
//			//E.g. Benefit Demographics/Application Form to BDAF
//			tempDocumentContainerName = document.get_Name().replaceAll(" ", "");
//			//Save the 1st letter of each word in the Document Name to documentContainerName
//			tempDocumentContainerName = getCapitalLettersName(tempDocumentContainerName);
//			//Make sure Name is less than 44 characters to allow for 20 characters in the date time stamp
//			if (tempDocumentContainerName.length() > 44)
//			{
//				//Call truncateName method to shrink the string to the specified length
//				tempDocumentContainerName = truncateName(tempDocumentContainerName, 44);
//			}
//			//documentContainerName = fnDocument.getFnDocumentName() + "_" + getDateTime("yyyyMMdd_HHmmssSSS");
//			documentContainerName = tempDocumentContainerName + "_" + getDateTime("yyyyMMdd_HHmmssSSS");
//			wiiscLog.log(wiiscLog.INFO, "documentContainerName: " + documentContainerName);
//			
//			//Object Store Location for the Document
//			String documentObjectStoreLocation = "";
//			
//			//Check if a docLocation was passed in
//			if (docLocation.length() > 0)
//			{
//				//Get the Object Store Folder location based off the passed in docLocation value
//				documentObjectStoreLocation = docLocation;
//			}
//			else
//			{
//				//Get the Object Store Folder location based off the Current Date
//				documentObjectStoreLocation = docClassConfig.getString("docClassDocsRootLocation") + "/" + getDateTime("yyyy/MM/dd");
//			}
//			wiiscLog.log(wiiscLog.INFO, "documentObjectStoreLocation: " + documentObjectStoreLocation);
//			
//			//Verify the Object Store Folder Location exists
//			if (!checkFolderExists(os, documentObjectStoreLocation, wiiscLog))
//			{
//				String[] objectStoreFolders = documentObjectStoreLocation.split("/");
//				while (!checkFolderExists(os, documentObjectStoreLocation, wiiscLog))
//				{
//					wiiscLog.log(wiiscLog.INFO, "Creating the Object Store Folder Path");
//					String folderPath = "";
//					String previousPath = "";
//					for (int i = 0; i < objectStoreFolders.length; i++)
//					{
//						//Save the Previous Folder Path
//						previousPath = folderPath;
//						//Create a New Folder Path to Test
//						folderPath = folderPath + "/" + objectStoreFolders[i];
//						wiiscLog.log(wiiscLog.INFO, "Checking " + folderPath);
//						if (!checkFolderExists(os, folderPath, wiiscLog))
//						{
//							//Create the Folder in the Object Store
//							if (i == 0)
//							{
//								createFolder(os, "/", objectStoreFolders[i], wiiscLog);
//							}
//							else
//							{
//								createFolder(os, previousPath, objectStoreFolders[i], wiiscLog);
//							}
//						}
//					}
//				}
//			}
//			
//			//CheckIn Doc
//			wiiscLog.log(wiiscLog.INFO, "Check In the Document");
//			document.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
//			document.save(RefreshMode.NO_REFRESH);
//	        
//			//File the Doc
//	        ReferentialContainmentRelationship rcr = null;
//	        //File the Document
//	        rcr = fileDocument(os, document, documentContainerName, documentObjectStoreLocation, wiiscLog);
//	        	        
//			//Verify the ReferentialContainmentRelationship
//	        if (document != null && rcr != null)
//	        {
//	        	//Update the Document Location Property
//	        	//Get Doc Properties from FileNet
//				Properties docProperties = document.getProperties();
//				//Check if the Document Location Property is in the Property Cache
//				if (docProperties.isPropertyPresent(globalConfig.getString("documentPropFiledFolderLocation")))
//				{
//					//Update the Document Location Property
//					docProperties.putValue(globalConfig.getString("documentPropFiledFolderLocation"), documentObjectStoreLocation);
//				}
//	        	//Save FileNet Doc
//	        	//document.save(RefreshMode.REFRESH);
//	        	document.save(RefreshMode.NO_REFRESH);
//		        wiiscLog.log(wiiscLog.INFO, "Document: " + document.get_Name() + " stored successfully.");
//		        wiiscLog.log(wiiscLog.INFO, "Document ID: " + document.get_Id().toString());
//				//Update the Document Store Status to True
//		        docStored = true;        
//	        }
//	        else
//	        {
//	        	//Document or RCR is NULL
//	        	//Failed to Add the Document to FileNet - possible RCR failure
//	        	wiiscLog.log(wiiscLog.INFO, "Document: " + document.get_Name() + " failed to store in FileNet.");
//	        	//Delete the Document
//	        	document.delete();
//	        	document.save(RefreshMode.NO_REFRESH);
//	        	document = null;
//	        	wiiscLog.log(wiiscLog.INFO, "Document Object has been deleted.");
//	        	//Update FnDoc Status
//		       	//Set fnDoc Status to 1
//	        	//fnDocument.setErrorFlag(1);
//	        	//Update the Document Store Status to False
//	        	docStored = false;
//	        }
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			//fnDocument.setErrorFlag(1);
//			//Update the Document Store Status to False
//        	docStored = false;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> storeDocument()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return docStored;
//	}
//	
//	private boolean createBatchDocument(ObjectStore os, Document document, String batchPath, WIISCLog wiiscLog)
//	{
//		//boolean for the document stored and filed
//		boolean docStored = false;
//		//Define the Doc Class Properties Resource
//		//ResourceBundle docClassProps = null;
//		if (docClassConfig == null)
//		{
//			LocalResource resConfig = getLocalResource(document.getClassName() + "Config");
//			docClassConfig = resConfig.getLocalBundle(resConfig.getBundlePath(), resConfig.getBundleFile());
//		}
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> createBatchDocument()");
//
//			//Get the Document Container Name from the Document's ReferentialContainmentRelationship
//			String documentContainerName = "";
//			ReferentialContainmentRelationshipSet rcrs = document.get_Containers();
//			Iterator iter = rcrs.iterator();
//			while (iter.hasNext())
//			{
//				ReferentialContainmentRelationship rcr = (ReferentialContainmentRelationship)iter.next();
//				//Reuse the original Container name for the Document filing (storing in new folder)
//				documentContainerName = rcr.get_ContainmentName();
//				wiiscLog.log(wiiscLog.INFO, "Original Document Container Name to be reused: " + documentContainerName);
//				break;
//			}
//			//Reset the ReferentialContainmentRelationshipSet
//			rcrs = null;
//
//			//Get the Object Store Folder location based off the Current Date
//			//String documentObjectStoreLocation = "";
//			//documentObjectStoreLocation = docClassProps.getString("docClassDocsRootLocation") + "/" + getDateTime("yyyy/MM/dd");
//			//wiiscLog.log(wiiscLog.INFO, "documentObjectStoreLocation: " + documentObjectStoreLocation);
//			wiiscLog.log(wiiscLog.INFO, "batchPath: " + batchPath);
//			
//			//Verify the Object Store Folder Location exists
//			if (!checkFolderExists(os, batchPath, wiiscLog))
//			{
//				String[] objectStoreFolders = batchPath.split("/");
//				while (!checkFolderExists(os, batchPath, wiiscLog))
//				{
//					wiiscLog.log(wiiscLog.INFO, "Creating the Object Store Folder Path");
//					String folderPath = "";
//					String previousPath = "";
//					for (int i = 0; i < objectStoreFolders.length; i++)
//					{
//						//Save the Previous Folder Path
//						previousPath = folderPath;
//						//Create a New Folder Path to Test
//						folderPath = folderPath + "/" + objectStoreFolders[i];
//						wiiscLog.log(wiiscLog.INFO, "Checking " + folderPath);
//						if (!checkFolderExists(os, folderPath, wiiscLog))
//						{
//							//Create the Folder in the Object Store
//							if (i == 0)
//							{
//								createFolder(os, "/", objectStoreFolders[i], wiiscLog);
//							}
//							else
//							{
//								createFolder(os, previousPath, objectStoreFolders[i], wiiscLog);
//							}
//						}
//					}
//				}
//			}
//
//			//DO NOT NEED TO CHECKIN DOC IT ALREADY EXISTS IN _TO_BE_INDEXED BATCH FOLDER
//			/*//CheckIn Doc
//					wiiscLog.log(wiiscLog.INFO, "Check In the Document");
//					document.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
//					document.save(RefreshMode.NO_REFRESH);*/
//
//			//File the Doc
//			ReferentialContainmentRelationship rcr = null;
//			//File the Document
//			rcr = fileDocument(os, document, documentContainerName, batchPath, wiiscLog);
//
//			//Verify the ReferentialContainmentRelationship
//			if (document != null && rcr != null)
//			{
//				//Save FileNet Doc
//				//document.save(RefreshMode.REFRESH);
//				document.save(RefreshMode.NO_REFRESH);
//				wiiscLog.log(wiiscLog.INFO, "Document: " + document.get_Name() + " stored successfully.");
//				wiiscLog.log(wiiscLog.INFO, "Document ID: " + document.get_Id().toString());
//				//Update the Document Store Status to True
//				docStored = true;        
//			}
//			/*else
//			        {
//			        	//Document or RCR is NULL
//			        	//Failed to Add the Document to FileNet - possible RCR failure
//			        	wiiscLog.log(wiiscLog.INFO, "Document: " + document.get_Name() + " failed to store in FileNet.");
//			        	//Delete the Document
//			        	document.delete();
//			        	document.save(RefreshMode.NO_REFRESH);
//			        	document = null;
//			        	wiiscLog.log(wiiscLog.INFO, "Document Object has been deleted.");
//			        	//Update FnDoc Status
//				       	//Set fnDoc Status to 1
//			        	//fnDocument.setErrorFlag(1);
//			        	//Update the Document Store Status to False
//			        	docStored = false;
//			        }*/
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			//fnDocument.setErrorFlag(1);
//			//Update the Document Store Status to False
//			docStored = false;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> createBatchDocument()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return docStored;
//	}
//	
//	private boolean storeBatchDocument(ObjectStore os, Document document, WIISCLog wiiscLog)
//	{
//		//boolean for the document stored and filed
//		boolean docStored = false;
//		//Define the Doc Class Properties Resource
//		//ResourceBundle docClassProps = null;
//		if (docClassConfig == null)
//		{
//			LocalResource resConfig = getLocalResource(document.getClassName() + "Config");
//			docClassConfig = resConfig.getLocalBundle(resConfig.getBundlePath(), resConfig.getBundleFile());
//		}
//				
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> storeBatchDocument()");
//			
//			//Get the Document Container Name from the Document's ReferentialContainmentRelationship
//			String documentContainerName = "";
//			ReferentialContainmentRelationshipSet rcrs = document.get_Containers();
//			Iterator iter = rcrs.iterator();
//			while (iter.hasNext())
//			{
//				ReferentialContainmentRelationship rcr = (ReferentialContainmentRelationship)iter.next();
//				//Reuse the original Container name for the Document filing (storing in new folder)
//				documentContainerName = rcr.get_ContainmentName();
//				wiiscLog.log(wiiscLog.INFO, "Original Document Container Name to be reused: " + documentContainerName);
//				rcr.delete();
//				rcr.save(RefreshMode.REFRESH);
//				break;
//			}
//			//Reset the ReferentialContainmentRelationshipSet
//			rcrs = null;
//			
//			//Get the Object Store Folder location based off the Current Date
//			String documentObjectStoreLocation = "";
//			documentObjectStoreLocation = docClassConfig.getString("docClassDocsRootLocation") + "/" + getDateTime("yyyy/MM/dd");
//			wiiscLog.log(wiiscLog.INFO, "documentObjectStoreLocation: " + documentObjectStoreLocation);
//			//Verify the Object Store Folder Location exists
//			if (!checkFolderExists(os, documentObjectStoreLocation, wiiscLog))
//			{
//				String[] objectStoreFolders = documentObjectStoreLocation.split("/");
//				while (!checkFolderExists(os, documentObjectStoreLocation, wiiscLog))
//				{
//					wiiscLog.log(wiiscLog.INFO, "Creating the Object Store Folder Path");
//					String folderPath = "";
//					String previousPath = "";
//					for (int i = 0; i < objectStoreFolders.length; i++)
//					{
//						//Save the Previous Folder Path
//						previousPath = folderPath;
//						//Create a New Folder Path to Test
//						folderPath = folderPath + "/" + objectStoreFolders[i];
//						wiiscLog.log(wiiscLog.INFO, "Checking " + folderPath);
//						if (!checkFolderExists(os, folderPath, wiiscLog))
//						{
//							//Create the Folder in the Object Store
//							if (i == 0)
//							{
//								createFolder(os, "/", objectStoreFolders[i], wiiscLog);
//							}
//							else
//							{
//								createFolder(os, previousPath, objectStoreFolders[i], wiiscLog);
//							}
//						}
//					}
//				}
//			}
//			
//			//DO NOT NEED TO CHECKIN DOC IT ALREADY EXISTS IN _TO_BE_INDEXED BATCH FOLDER
//			/*//CheckIn Doc
//			wiiscLog.log(wiiscLog.INFO, "Check In the Document");
//			document.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
//			document.save(RefreshMode.NO_REFRESH);*/
//	        
//			//File the Doc
//	        ReferentialContainmentRelationship rcr = null;
//	        //File the Document
//	        rcr = fileDocument(os, document, documentContainerName, documentObjectStoreLocation, wiiscLog);
//	        	        
//			//Verify the ReferentialContainmentRelationship
//	        if (document != null && rcr != null)
//	        {
//	        	//Update the Document Location Property
//	        	//Get Doc Properties from FileNet
//				Properties docProperties = document.getProperties();
//				//Check if the Document Location Property is in the Property Cache
//				if (docProperties.isPropertyPresent(globalConfig.getString("documentPropFiledFolderLocation")))
//				{
//					//Update the Document Location Property
//					docProperties.putValue(globalConfig.getString("documentPropFiledFolderLocation"), documentObjectStoreLocation);
//				}
//	        	//Save FileNet Doc
//	        	//document.save(RefreshMode.REFRESH);
//	        	document.save(RefreshMode.NO_REFRESH);
//		        wiiscLog.log(wiiscLog.INFO, "Document: " + document.get_Name() + " stored successfully.");
//		        wiiscLog.log(wiiscLog.INFO, "Document ID: " + document.get_Id().toString());
//				//Update the Document Store Status to True
//		        docStored = true;        
//	        }
//	        /*else
//	        {
//	        	//Document or RCR is NULL
//	        	//Failed to Add the Document to FileNet - possible RCR failure
//	        	wiiscLog.log(wiiscLog.INFO, "Document: " + document.get_Name() + " failed to store in FileNet.");
//	        	//Delete the Document
//	        	document.delete();
//	        	document.save(RefreshMode.NO_REFRESH);
//	        	document = null;
//	        	wiiscLog.log(wiiscLog.INFO, "Document Object has been deleted.");
//	        	//Update FnDoc Status
//		       	//Set fnDoc Status to 1
//	        	//fnDocument.setErrorFlag(1);
//	        	//Update the Document Store Status to False
//	        	docStored = false;
//	        }*/
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - ErrorFlag = 1");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			//Update errorFlag
//			//fnDocument.setErrorFlag(1);
//			//Update the Document Store Status to False
//        	docStored = false;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> storeBatchDocument()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return docStored;
//	}
//	
//	private FnPropertyList mergeFnPropertyListData(FnPropertyList sourceList, FnPropertyList filterList, WIISCLog wiiscLog)
//	{
//		//This method takes the sourceList which is the data from a Request
//		//and merges only the Properties that match the filterList Properties
//		FnPropertyList fnPropertyList = new FnPropertyList();
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> mergeFnPropertyListData()");
//			//Source List Array
//			List<FnProperty> sourceListProperties = sourceList.getFnDocumentPropsList();
//			//Filter List Array
//			List<FnProperty> filterListProperties = filterList.getFnDocumentPropsList();
//			//Loop through the Source List to see if any Property Names match the Filtered List
//			for (int i = 0; i < sourceListProperties.size(); i++)
//			{
//				FnProperty sourceProperty = sourceListProperties.get(i);
//				for (int j = 0; j < filterListProperties.size(); j++)
//				{
//					FnProperty filterProperty = filterListProperties.get(j);
//					//Compare Source Property and Filter Property
//					if (sourceProperty.getName().equals(filterProperty.getName()))
//					{
//						if (sourceProperty.getValue().length() > 0)
//						{
//							//Add the FnProperty to the FnPropertyList
//							fnPropertyList.addFnProperty(sourceProperty);
//							wiiscLog.log(wiiscLog.INFO, "Property " + sourceProperty.getName() + " ADDED");
//						}
//						else
//						{
//							wiiscLog.log(wiiscLog.INFO, "Property " + sourceProperty.getName() + " was NOT added since there was no property value.");
//						}
//					}
//				}
//			}
//			//Verify the FnPropertyList size matches the Filter List to make sure all of the Properties were included
//			if (fnPropertyList.getFnDocumentPropsList().size() != filterListProperties.size())
//			{
//				wiiscLog.log(wiiscLog.INFO, "Merged Property List is missing 1 or more Properties from the Filter List.");
//				wiiscLog.log(wiiscLog.INFO, "Merged Property List will be set to 0");
//				fnPropertyList.clear();
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - mergeFnPropertyListData");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> mergeFnPropertyListData()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return fnPropertyList;
//	}
//	
//	private String[] buildRequiredPropertiesData(String type, FnPropertyList fnPropertyList, WIISCLog wiiscLog)
//	{
//		String[] requiredProps = new String[fnPropertyList.getFnDocumentPropsList().size()];
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> buildRequiredPropertiesData()");
//			
//			for (int i = 0; i < fnPropertyList.getFnDocumentPropsList().size(); i++)
//			{
//				//Determine if we want the Property Name or Property Value
//				if (type.equals("Name"))
//				{
//					//Populate String Array with the Property Name from the Property List
//					requiredProps[i] = fnPropertyList.getFnDocumentPropsList().get(i).getName();
//				}
//				else
//				{
//					//Populate String Array with the Property Value from the Property List
//					requiredProps[i] = fnPropertyList.getFnDocumentPropsList().get(i).getValue();
//				}
//			}
//			
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - buildRequiredPropertiesData");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			requiredProps = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> buildRequiredPropertiesData()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return requiredProps;
//	}
//	
//	private Document createDocument(ObjectStore os, String docClass, File importFile, FnPropertyList fnPropertyListRequest, WIISCLog wiiscLog)
//    {
//		Document doc = null;
//		boolean saveTheDocument = false;
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> createDocument()");
//			
//			//Create Content Transfer Object and Read the File
//			ContentTransfer ct = createContentTransfer(importFile,wiiscLog);
//			
//			//Create Content Element List Object
//			ContentElementList cel = createContentElements(ct,wiiscLog);
//			
//			if (docClass.length() == 0)
//	        {
//				wiiscLog.log(wiiscLog.INFO, "Creating a Document using the Default Doc Class");
//				doc = Factory.Document.createInstance(os, null);
//	        }
//	        else
//	        {
//	        	wiiscLog.log(wiiscLog.INFO, "Creating a Document using the " + docClass + " Doc Class");
//	        	doc = Factory.Document.createInstance(os, docClass);
//	        }
//	        
//			//Get any Custom Required Properties from the Document Class
//			FnPropertyList fnPropertyListRequired = getCustomRequiredDocProperties(os, docClass, wiiscLog);
//			
//			//Check if the Document Class has any required properties to be set before doing a Save
//			if (fnPropertyListRequired.getFnDocumentPropsList().size() > 0)
//			{
//				//Check if the Request Property List has a size greater than 0
//				if (fnPropertyListRequest.getFnDocumentPropsList().size() > 0)
//				{
//					//Get the Custom Required Property List with values from the fnPropertyListRequest
//					FnPropertyList fnPropertyList = mergeFnPropertyListData(fnPropertyListRequest, fnPropertyListRequired, wiiscLog);
//					
//					//Check if the merged Property List was created
//					if (fnPropertyList.getFnDocumentPropsList().size() > 0)
//					{
//						//Build the String Array of Required Property Names
//						//String[] requiredPropNames = buildRequiredPropertiesData("Name", fnPropertyList, wiiscLog);
//						//Get the Required Document Properties to be set
//						//doc.fetchProperties(requiredPropNames);
//						//Set the Required Properties to the Document
//						List<FnProperty> fnDocumentPropsList = fnPropertyList.getFnDocumentPropsList();
//						//Get Doc Properties from FileNet
//						Properties properties = doc.getProperties();
//						wiiscLog.log(wiiscLog.INFO, "Applying the Required Document Properties");
//						//Loop through and process Document Properties
//						for (int j = 0; j < fnDocumentPropsList.size(); j++)
//						{
//							FnProperty fnProperty = fnDocumentPropsList.get(j);
//							//Check if fnProperty is null
//							if (fnProperty != null)
//							{
//								Object value = fnProperty.getValue();
//								//Update the Document Property
//								properties.putObjectValue(fnProperty.getName(), value);
//								wiiscLog.log(wiiscLog.INFO, fnProperty.getName() + " Value: " + value.toString());
//							}
//						}
//						wiiscLog.log(wiiscLog.INFO, "Finished Applying the Required Document Properties");
//						saveTheDocument = true;
//					}
//				}
//				else
//				{
//					wiiscLog.log(wiiscLog.INFO, "The Request Property List had a size of 0, so the Required Document Properties could not be set");
//					saveTheDocument = false;
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "There are No Required Document Properties to be set");
//				saveTheDocument = true;
//			}
//			
//			//Save the Document
//			if (saveTheDocument)
//			{
//				//Save the Doc - NO R/T
//				doc.save(RefreshMode.NO_REFRESH);
//				//Save the Doc - R/T
//				//doc.save(RefreshMode.REFRESH);
//				//Set the Content Element				
//		        if (cel != null) {
//					doc.set_ContentElements(cel);
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Document was not saved due to missing requirements");
//				doc = null;
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - createDocument");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			doc = null;
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> createDocument()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return doc;
//    }
//	
//	private String getDateTime()
//	{
//		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
//	    Date date = new Date();
//	    return dateFormat.format(date);
//	}
//	
//	private String getDateWithDashes()
//	{
//		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//	    Date date = new Date();
//	    return dateFormat.format(date);
//	}
//	
//	private String getDateTime(String format)
//	{
//		DateFormat dateFormat = new SimpleDateFormat(format);
//	    Date date = new Date();
//	    return dateFormat.format(date);
//	}
//	
//	private String getDateWithMonthName(String dateValue)
//	{
//		String dateWithMonthName = "";
//		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy/MM/dd");
//		try
//		{
//			Date date = formatter.parse(dateValue);
//			dateWithMonthName = date.toString();
//		}
//		catch (Exception e)
//		{
//			e.getMessage();
//		}
//		return dateWithMonthName;
//	}
//	
//	private String convertDateFormat(String sDate, String format)
//	{
//		String result = "";
//		
//		//Convert sDate to MM/DD/YYYY
//		if (format.equals("MM/DD/YYYY"))
//		{
//			String[] sDateData = null;
//			//Check sDate Format
//			if (sDate.contains("-"))
//			{
//				//Assumes yyyy-mm-dd
//				//sDateData 3 records
//				sDateData = sDate.split("-");
//				result = sDateData[1] + "/" + sDateData[2] + "/" + sDateData[0];
//			}
//		}
//		
//		return result;
//	}
//	
//	private boolean checkDateField(String sDate, String format, WIISCLog wiiscLog)
//	{
//		boolean validDate = false;
//		
//		//wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> checkDateField()");
//		
//		if (format.equals("MM/DD/YYYY"))
//		{
//			//wiiscLog.log(wiiscLog.INFO, "Format is MM/DD/YYYY");
//			//Check if sDate matches regular expression MM/DD/YYYY
//			if (sDate.matches("\\d{2}/\\d{2}/\\d{4}"))
//			{
//				//wiiscLog.log(wiiscLog.INFO, sDate + " Valid Date Format");
//				validDate = true;
//			}
//			else
//			{
//				//wiiscLog.log(wiiscLog.INFO, sDate + " Invalid Date Format");
//				validDate = false;
//			}
//		}
//		//wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> checkDateField()");
//		return validDate;
//	}
//	
//	private ContentTransfer createContentTransfer(File f, WIISCLog wiiscLog)
//    {
//		ContentTransfer ctNew = null;
//        
//		//wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> createContentTransfer()");
//		
//		if(readDocContentFromFile(f,wiiscLog) != null)
//        {
//        	ctNew = Factory.ContentTransfer.createInstance();
//            ByteArrayInputStream is = new ByteArrayInputStream(readDocContentFromFile(f,wiiscLog));
//            ctNew.setCaptureSource(is);
//            ctNew.set_RetrievalName(f.getName());
//            //TO DO
//            //ctNew.set_ContentType("Some Mime Type");
//        }
//		//wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> createContentTransfer()");
//		return ctNew;
//    }
//	
//	//@SuppressWarnings("unchecked")
//	private ContentElementList createContentElements(ContentTransfer ct, WIISCLog wiiscLog)
//    {
//		ContentElementList cel = null;
//        
//		//wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> createContentElements()");
//		
//		if(ct != null)
//        {
//        	cel = Factory.ContentElement.createList();
//            cel.add(ct);
//        }
//		
//		//wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> createContentElements()");
//		return cel;
//    }
//	
//	private byte[] readDocContentFromFile(File f, WIISCLog wiiscLog)
//    {
//		FileInputStream is;
//        byte[] b = null;
//        
//        //wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> readDocContentFromFile()");
//        
//        int fileLength = (int)f.length();
//        if(fileLength != 0)
//        {
//        	try
//        	{
//        		is = new FileInputStream(f);
//        		b = new byte[fileLength];
//        		is.read(b);
//        		is.close();
//        	}
//        	catch (FileNotFoundException e)
//        	{
//        		e.printStackTrace();
//        	}
//        	catch (IOException e)
//        	{
//        		e.printStackTrace();
//        	}
//        }
//        
//        //wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> readDocContentFromFile()");
//        return b;
//    }
//	
//	private ReferentialContainmentRelationship fileDocument(ObjectStore os, Document doc, String DocContainerName, String folderName, WIISCLog wiiscLog)
//	{
//		ReferentialContainmentRelationship rcr = null;
//		Folder f = Factory.Folder.getInstance(os, null, folderName);
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> fileDocument()");
//			wiiscLog.log(wiiscLog.INFO, "Document " + DocContainerName + " filed in " + folderName);
//			rcr = (ReferentialContainmentRelationship) f.file(doc, AutoUniqueName.NOT_AUTO_UNIQUE, DocContainerName, DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
//			rcr.save(RefreshMode.NO_REFRESH);
//		}
//		catch (Exception e)
//		{
//			if (e instanceof EngineRuntimeException) 
//			{
//				EngineRuntimeException fnEx = (EngineRuntimeException) e;
//				if (fnEx.getExceptionCode().equals(ExceptionCode.E_NOT_UNIQUE)) 
//				{			
//					wiiscLog.log(wiiscLog.ERROR, "Document is a duplicate because it is NOT UNIQUE.");
//					wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//					System.out.println("Document is a duplicate because it is NOT UNIQUE.");
//					System.out.println("Exception: " + e.getMessage());		        
//		        }
//				//Reset the ReferentialContainmentRelationship to NULL
//				rcr = null;
//				//Document will be deleted by the calling method
//			}
//		    else
//		    {
//		    	// A standard Java exception.
//		        System.out.println("Exception: " + e.getMessage());
//		    }
//		}
//		
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> fileDocument()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return rcr;
//	}
//
//	private void createFolder(ObjectStore os, String fPath, String fName, WIISCLog wiiscLog)
//    {
//		//wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> createFolder()");
//		Folder f = Factory.Folder.getInstance(os, null, fPath);
//		Folder nf = f.createSubFolder(fName);
//		nf.save(RefreshMode.NO_REFRESH);
//		//wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> createFolder()");
//    }
//
//	private String createFolderForImport(ObjectStore os, String docClass, WIISCLog wiiscLog)
//	{
//		//Folder Location for Document
//		String docDestinationPath = "";
//		//Doc Class Config Properties file
//		//ResourceBundle docClassConfigProps = null;
//		
//		try
//		{
//			wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> createFolderForImport()");
//			
//			//docClassConfigProps = ResourceBundle.getBundle(docClass + "Config");
//			if (docClassConfig == null)
//			{
//				LocalResource resConfig = getLocalResource(docClass + "Config");
//				docClassConfig = resConfig.getLocalBundle(resConfig.getBundlePath(), resConfig.getBundleFile());
//			}
//			
//			//Get Folder Location to store FileNet Doc by Date
//			String docClassDocsRootLocation = docClassConfig.getString("docClassDocsRootLocation");
//			
//			//Used to split the result from getDateTime which is in the format YYYY/MM/DD hh:mm:ss:SSS
//			String[] currentDateData1 = null;
//			//Used to split the result from currentDateData1[2] which is in the format DD hh:mm:ss:SSS to remove hh:mm:ss:SSS
//			String[] currentDateData2 = null;
//			//Get the YYYY/MM/DD hh:mm:ss split up
//			currentDateData1 = getDateTime().split("/");
//			//Get the DD hh:mm:ss split up
//			currentDateData2 = currentDateData1[2].split(" ");
//			//Update docDestinationPath
//			docDestinationPath = docClassDocsRootLocation + "/" + currentDateData1[0] + "/" + currentDateData1[1] + "/" + currentDateData2[0];
//			//Verify the Folder exists and if not, then create the folder path
//			if (!checkFolderExists(os,docDestinationPath,wiiscLog))
//			{
//				wiiscLog.log(wiiscLog.INFO, "Folder Path " + docDestinationPath + " does not exist and will be created.");
//				//Create the Folder path and verify it again
//				String[] docDestPathData = docDestinationPath.split("/");
//				String tempDestPath = "";
//				String tempDestPath2 = "";
//				//Set tempDestPath to docClassDocsRootLocation
//				//tempDestPath = docClassDocsRootLocation;
//				boolean destinationPathExists = false;
//				while (!destinationPathExists)
//				{
//					for (int z = 0; z < docDestPathData.length; z++)
//					{
//						//wiiscLog.log(wiiscLog.INFO, "tempDestPath: " + tempDestPath);
//						//wiiscLog.log(wiiscLog.INFO, "docDestPathData: " + docDestPathData[z]);
//						if (tempDestPath.length() == 0)
//						{
//							//First run through loop because it includes the Default Root Path
//							tempDestPath = docDestPathData[z] + "/";
//							//wiiscLog.log(wiiscLog.INFO, "1st Folder Path " + tempDestPath);
//						}
//						else if (tempDestPath.equals(docDestinationPath))
//						{
//							wiiscLog.log(wiiscLog.INFO, "Folder Path " + tempDestPath + " exists and is ready for Document import.");
//							destinationPathExists = true;
//							break;
//						}
//						else
//						{
//							//Backup Previous tempDestPath
//							tempDestPath2 = tempDestPath;
//							//If first char is /
//							if (tempDestPath.equals("/"))
//							{
//								tempDestPath = tempDestPath + docDestPathData[z] + "/";
//							}
//							else
//							{
//								tempDestPath = tempDestPath + "/" + docDestPathData[z] + "/";
//							}
//							//Remove the trailing /
//							tempDestPath = tempDestPath.substring(0, tempDestPath.length() - 1);
//							//wiiscLog.log(wiiscLog.INFO, "Folder Path " + tempDestPath);
//							if (checkFolderExists(os,tempDestPath,wiiscLog))
//							{
//								//wiiscLog.log(wiiscLog.INFO, "Folder Path " + tempDestPath + " exists.");
//								if (tempDestPath.equals(docDestinationPath))
//								{
//									wiiscLog.log(wiiscLog.INFO, "Folder Path " + tempDestPath + " exists and is ready for Document import.");
//									destinationPathExists = true;
//									break;
//								}
//							}
//							else
//							{
//								wiiscLog.log(wiiscLog.INFO, "Folder Path " + tempDestPath + " does not exist and will be created.");
//								//Create Sub Folder
//								createFolder(os,tempDestPath2,docDestPathData[z],wiiscLog);
//							}
//						}
//					}
//				}
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Doc Destination Path " + docDestinationPath + " already exists.");
//			}
//		}
//		catch (Exception e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "Exception - createFolderForImport");
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			docDestinationPath = "";
//		}
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> createFolderForImport()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return docDestinationPath;
//	}
//
//	private boolean moveImportFolder(String sourcePath, String destPath, WIISCLog wiiscLog)
//	{
//		boolean moveSuccess = false;
//		
//		File source = new File(sourcePath);
//		File destination = new File(destPath);
//		
//		wiiscLog.log(wiiscLog.INFO, "Entered ImagingManager -> moveImportFolder()");
//		wiiscLog.log(wiiscLog.INFO, "Source Path: " + sourcePath);
//		wiiscLog.log(wiiscLog.INFO, "Destination Path: " + destPath);
//		
//		try
//		{
//			if (source.isDirectory() && source.exists())
//			{
//				//Move the Source to the Destination
//				FileUtils.moveDirectory(source, destination);
//				moveSuccess = true;
//				wiiscLog.log(wiiscLog.INFO, "Import Folder moved successfully");
//			}
//		}
//		catch (IOException e)
//		{
//			wiiscLog.log(wiiscLog.ERROR, "ERROR: Moving the Import Folder " + sourcePath + " to " + destPath);
//			wiiscLog.log(wiiscLog.ERROR, wiiscLog.getStackTrace(e));
//			moveSuccess = false;
//		}
//		finally
//		{
//			source = null;
//			destination = null;
//		}
//		
//		wiiscLog.log(wiiscLog.INFO, "Leaving ImagingManager -> moveImportFolder()");
//		wiiscLog.log(wiiscLog.INFO, "===========================================================");
//		return moveSuccess;
//	}
//
//	private boolean copyFile(File source, File dest, WIISCLog wiiscLog) throws IOException
//	{
//		boolean fileCopied = false;
//		
//		if (!dest.exists())
//		{
//			dest.createNewFile();
//		}
//		FileInputStream in = null;
//		FileOutputStream out = null;
//		try
//		{
//			in = new FileInputStream(source);
//			out = new FileOutputStream(dest);
//			//Transfer bytes from In to Out
//			byte[] buf = new byte[1024];
//			int len;
//			while ((len = in.read(buf)) > 0)
//			{
//				out.write(buf, 0, len);
//			}
//			//Check if file exists
//			if (source.length() == dest.length())
//			{
//				wiiscLog.log(wiiscLog.INFO, "Destination File: " + dest.getName() + " created successfully");
//				fileCopied = true;
//			}
//			else
//			{
//				wiiscLog.log(wiiscLog.INFO, "Destination File: " + dest.getName() + " did not create successfully.");
//				fileCopied = false;
//			}
//			
//		}
//		finally
//		{
//			in.close();
//			out.close();
//		}
//		return fileCopied;
//	}
//	
//	private FnDocumentList copyFnDocumentList(FnDocumentList sourceFnDocumentList, WIISCLog wiiscLog)
//	{
//		FnDocumentList destinationFnDocumentList = new FnDocumentList();
//		List<FnDocument> sourceFnDocuments = new ArrayList<FnDocument>(10);
//		sourceFnDocuments = sourceFnDocumentList.getFnDocumentList();
//		for (int i = 0; i < sourceFnDocuments.size(); i++)
//		{
//			//Get 1 FnDocument
//			FnDocument oneFnDocument = sourceFnDocuments.get(i);
//			//Add 1 FnDocument to FnDocumentList
//			destinationFnDocumentList.addFnDocument(oneFnDocument);
//		}
//		
//		return destinationFnDocumentList;
//	}
//	
//	private boolean copyDirectory(File sourceDir, File destDir, WIISCLog wiiscLog) throws IOException
//	{
//		boolean dirCopied = false;
//		
//		if (!destDir.exists())
//		{
//			destDir.mkdirs();
//			wiiscLog.log(wiiscLog.INFO, "Destination: " + destDir.getName() + " created");
//		}
//		
//		File[] children = sourceDir.listFiles();
//		
//		for (File sourceChild : children)
//		{
//			String name = sourceChild.getName();
//			File destChild = new File(destDir, name);
//			if (sourceChild.isDirectory())
//			{
//				dirCopied = copyDirectory(sourceChild, destChild, wiiscLog);
//			}
//			else
//			{
//				dirCopied = copyFile(sourceChild, destChild, wiiscLog);
//			}
//		}
//		return dirCopied;
//	}
//	
//	private boolean deleteFile(File resource, WIISCLog wiiscLog) throws IOException
//	{
//		if (resource.isDirectory())
//		{
//			File[] childFiles = resource.listFiles();
//			for (File child : childFiles)
//			{
//				deleteFile(child, wiiscLog);
//			}
//		}
//		return resource.delete();
//	}

}

