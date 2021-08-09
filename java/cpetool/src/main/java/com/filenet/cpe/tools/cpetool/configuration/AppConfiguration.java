package com.filenet.cpe.tools.cpetool.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfiguration {

	//CPE
	private String cpeServerName;
	private String cpeConnectionURI;
	private String p8Domain;
	private String objectstoreName;
	private String stanzaName;
	private String cpeUsername;
	private String cpePassword;
	private String peConnectionPoint;
	private String peWorkflowRegion;
	
	
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
	
	
	
}
