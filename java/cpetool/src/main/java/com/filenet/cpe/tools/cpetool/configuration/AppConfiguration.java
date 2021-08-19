package com.filenet.cpe.tools.cpetool.configuration;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfiguration {

	//Global
	private String RootDirectory;
	private String ConfigFileDirectory;
	private String LogFileDirectory;
	private String TempFileDir;
	
	//CPE
	private String cpeServerName;
	private String cpeConnectionURI;
	private String p8Domain;
	private String objectstoreName;
	private String stanzaName;
	private String cpeUsername;
	private String cpePassword;
	
	//Workflow
	private String peConnectionPoint;
	private String peWorkflowRegion;
	private String workflowSearchLimit;
	private String enableWorkflowOutput;
	private String workflowOutputPath;
	private String workflowSearchUserField;
	private String workflowDBSearchEnabled;
	private String workflowDBServerName;
	private String workflowDBServerPort;
	private String workflowDBName;
	private String workflowDBUser;
	private String workflowDBUserPassword;
	private List<String> workflowDataFields;
	
	
	public String getRootDirectory() {
		return RootDirectory;
	}
	public void setRootDirectory(String rootDirectory) {
		RootDirectory = rootDirectory;
	}
	public String getConfigFileDirectory() {
		return ConfigFileDirectory;
	}
	public void setConfigFileDirectory(String configFileDirectory) {
		ConfigFileDirectory = configFileDirectory;
	}
	public String getLogFileDirectory() {
		return LogFileDirectory;
	}
	public void setLogFileDirectory(String logFileDirectory) {
		LogFileDirectory = logFileDirectory;
	}
	public String getTempFileDir() {
		return TempFileDir;
	}
	public void setTempFileDir(String tempFileDir) {
		TempFileDir = tempFileDir;
	}
	public String getCpeServerName() {
		return cpeServerName;
	}
	public void setCpeServerName(String cpeServerName) {
		this.cpeServerName = cpeServerName;
	}
	public String getCpeConnectionURI() {
		return cpeConnectionURI;
	}
	public void setCpeConnectionURI(String cpeConnectionURI) {
		this.cpeConnectionURI = cpeConnectionURI;
	}
	public String getP8Domain() {
		return p8Domain;
	}
	public void setP8Domain(String p8Domain) {
		this.p8Domain = p8Domain;
	}
	public String getObjectstoreName() {
		return objectstoreName;
	}
	public void setObjectstoreName(String objectstoreName) {
		this.objectstoreName = objectstoreName;
	}
	public String getStanzaName() {
		return stanzaName;
	}
	public void setStanzaName(String stanzaName) {
		this.stanzaName = stanzaName;
	}
	public String getCpeUsername() {
		return cpeUsername;
	}
	public void setCpeUsername(String cpeUsername) {
		this.cpeUsername = cpeUsername;
	}
	public String getCpePassword() {
		return cpePassword;
	}
	public void setCpePassword(String cpePassword) {
		this.cpePassword = cpePassword;
	}
	public String getPeConnectionPoint() {
		return peConnectionPoint;
	}
	public void setPeConnectionPoint(String peConnectionPoint) {
		this.peConnectionPoint = peConnectionPoint;
	}
	public String getPeWorkflowRegion() {
		return peWorkflowRegion;
	}
	public void setPeWorkflowRegion(String peWorkflowRegion) {
		this.peWorkflowRegion = peWorkflowRegion;
	}
	public String getWorkflowSearchLimit() {
		return workflowSearchLimit;
	}
	public void setWorkflowSearchLimit(String workflowSearchLimit) {
		this.workflowSearchLimit = workflowSearchLimit;
	}
	public String getEnableWorkflowOutput() {
		return enableWorkflowOutput;
	}
	public void setEnableWorkflowOutput(String enableWorkflowOutput) {
		this.enableWorkflowOutput = enableWorkflowOutput;
	}
	public String getWorkflowOutputPath() {
		return workflowOutputPath;
	}
	public void setWorkflowOutputPath(String workflowOutputPath) {
		this.workflowOutputPath = workflowOutputPath;
	}
	public String getWorkflowSearchUserField() {
		return workflowSearchUserField;
	}
	public void setWorkflowSearchUserField(String workflowSearchUserField) {
		this.workflowSearchUserField = workflowSearchUserField;
	}
	public String getWorkflowDBSearchEnabled() {
		return workflowDBSearchEnabled;
	}
	public void setWorkflowDBSearchEnabled(String workflowDBSearchEnabled) {
		this.workflowDBSearchEnabled = workflowDBSearchEnabled;
	}
	public String getWorkflowDBServerName() {
		return workflowDBServerName;
	}
	public void setWorkflowDBServerName(String workflowDBServerName) {
		this.workflowDBServerName = workflowDBServerName;
	}
	public String getWorkflowDBServerPort() {
		return workflowDBServerPort;
	}
	public void setWorkflowDBServerPort(String workflowDBServerPort) {
		this.workflowDBServerPort = workflowDBServerPort;
	}
	public String getWorkflowDBName() {
		return workflowDBName;
	}
	public void setWorkflowDBName(String workflowDBName) {
		this.workflowDBName = workflowDBName;
	}
	public String getWorkflowDBUser() {
		return workflowDBUser;
	}
	public void setWorkflowDBUser(String workflowDBUser) {
		this.workflowDBUser = workflowDBUser;
	}
	public String getWorkflowDBUserPassword() {
		return workflowDBUserPassword;
	}
	public void setWorkflowDBUserPassword(String workflowDBUserPassword) {
		this.workflowDBUserPassword = workflowDBUserPassword;
	}
	public List<String> getWorkflowDataFields() {
		return workflowDataFields;
	}
	public void setWorkflowDataFields(List<String> workflowDataFields) {
		this.workflowDataFields = workflowDataFields;
	}
		
	
}
