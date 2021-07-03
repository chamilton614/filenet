package com.hp.docsolutions.filenet.p8.base;

public interface ConstantsUtil {

	//********************************
	//Config Files
	//********************************
	public static final String GLOBAL_CONFIG = "GlobalConfig";
	public static final String BROWSE_LIST_CONFIG = "BrowseListConfig";
	public static final String EXPORT_DOCUMENT_CONFIG = "ExportDocumentConfig";
	public static final String INDEX_UNIVERSAL_CONFIG = "IndexUniversalConfig";
	public static final String UPDATE_EMPLOYER_FOLDER_CONFIG = "UpdateEmployerFolderConfig";
	public static final String UPDATE_INDEX_CONFIG = "UpdateIndexConfig";
	public static final String UPDATE_MEMBER_FOLDER_CONFIG = "UpdateMemberFolderConfig";
	
	//********************************
	//Document Class Config Files
	//********************************
	public static final String MEMBER_CONFIG = "MemberConfig";
	public static final String EMPLOYER_CONFIG = "EmployerConfig";
	
	//********************************
	//Request Parameters
	//********************************
	//Function ID
	public static final String FUNCTION_ID = "funId";
	//Class ID for Document Classes
	public static final String TYPE_ID = "typId";
	//Application ID
	public static final String APP_ID = "appId";
	//Application ID Filter
	public static final String APP_ID_FILTER = "appIdFltr";
	//Import ID for Importing Documents ONLY
	public static final String IMP_ID = "impId";
	//Document ID
	public static final String DOC_ID = "docId";
	
	//********************************
	//Global Properties
	//********************************
	public static final String CE_CONNECTION_URI = "ceConnectionURI";
	public static final String CE_OBJECTSTORE_NAME = "objectstoreName";
	public static final String CE_STANZA_NAME = "stanzaName";
	public static final String CE_USER_ID = "ceUserId";
	public static final String CE_USER_PASSWORD = "cePassword";
	public static final String FOLDER_SEPARATOR = "\\";
	public static final String GLOBAL_LOG_FILE_DIRECTORY = "logFilePath";
	public static final String DOCSERVICES_LOG_FILE_DIRECTORY = "docServicesLogFileDirectory";
	
	//********************************
	//DocService Codes
	//********************************
	//Browse List
	public static final int BROWSE_LIST_CODE = 1;
	//Document Class List
	public static final int DOC_CLASS_LIST_CODE = 2;
	//Document Type List
	public static final int DOC_TYPE_LIST_CODE = 3;
	//Both Document Class and Document Type List
	public static final int BOTH_DOC_CLASS_DOC_TYPE_LIST_CODE = 4;
	//Import Document
	public static final int IMPORT_DOCUMENTS_CODE = 5;
	//Export Document
	//public static final int EXPORT_DOCUMENT_CODE = 4;
	//Index Universal
	//public static final int INDEX_UNIVERSAL_CODE = 5;
	//Update Employer Folder
	//public static final int UPDATE_EMPLOYER_FOLDER_CODE = 6;
	//Update Index
	//public static final int UPDATE_INDEX_CODE = 7;
	//Update Member
	//public static final int UPDATE_MEMBER_FOLDER_CODE = 8;
		
	//Browse List Specific Properties
	/*public static final String BATCH_NAME = "batchName";
	public static final String DATE_MODIFIED = "dateLastModified";
	public static final String DOC_CATEGORY = "docCategory";
	public static final String DOC_TITLE = "documentTitle";
	public static final String FORM_NAME = "formName";
	public static final String FORM_NUMBER = "formNumber";
	public static final String DOC_ID = "docID";
	public static final String INDEXER_ID = "indexerID";
	public static final String DOC_NAME = "docName";
	public static final String PLAN = "plan";
	public static final String DATE_RECEIVED = "dateReceived";
	public static final String DATE_SCANNED = "dateScanned";
	public static final String SSN = "ssn";
	public static final String RETIREMENT_ID = "retirementID";*/
		
	//Export Document Specific Properties
	
	//Index Universal Specific Properties
	
	//Update Employer Folder Specific Properties
	
	//Update Index Specific Properties
	
	//Update Member Folder Specific Properties
	
}
