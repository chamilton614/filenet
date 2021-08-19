package com.filenet.cpe.tools.cpetool.model;

public class FnDocument {
    private String fnDocumentClass;
    private String fnDocumentName;
    private String fnDocumentID;
    private String fnDocumentAppId;
    private String fnDocumentURL;
    private String fnDocumentStatus;
    private String fnWorkflowID;
    private FnPropertyList fnPropertyList;
    private FnFileList fnFilesList;
    private int errorFlag;

    public String getFnDocumentClass() {
        return this.fnDocumentClass;
    }

    public void setFnDocumentClass(String fnDocumentClass) {
        this.fnDocumentClass = fnDocumentClass;
    }

    public String getFnDocumentName() {
        return this.fnDocumentName;
    }

    public void setFnDocumentName(String fnDocumentName) {
        this.fnDocumentName = fnDocumentName;
    }

    public String getFnDocumentID() {
        return this.fnDocumentID;
    }

    public void setFnDocumentID(String fnDocumentID) {
        this.fnDocumentID = fnDocumentID;
    }

    public String getFnDocumentAppId() {
        return this.fnDocumentAppId;
    }

    public void setFnDocumentAppId(String fnDocumentAppId) {
        this.fnDocumentAppId = fnDocumentAppId;
    }

    public String getFnDocumentURL() {
        return this.fnDocumentURL;
    }

    public void setFnDocumentURL(String fnDocumentURL) {
        this.fnDocumentURL = fnDocumentURL;
    }

    public String getFnDocumentStatus() {
        return this.fnDocumentStatus;
    }

    public void setFnDocumentStatus(String fnDocumentStatus) {
        this.fnDocumentStatus = fnDocumentStatus;
    }

    public String getFnWorkflowID() {
        return this.fnWorkflowID;
    }

    public void setFnWorkflowID(String fnWorkflowID) {
        this.fnWorkflowID = fnWorkflowID;
    }

    public int getErrorFlag() {
        return this.errorFlag;
    }

    public void setErrorFlag(int errorFlag) {
        this.errorFlag = errorFlag;
    }

    public FnPropertyList getFnPropertyList() {
        return this.fnPropertyList;
    }

    public void setFnPropertyList(FnPropertyList fnPropertyList) {
        this.fnPropertyList = fnPropertyList;
    }

    public FnFileList getFnFilesList() {
        return this.fnFilesList;
    }

    public void setFnFilesList(FnFileList fnFilesList) {
        this.fnFilesList = fnFilesList;
    }

    public String toString() {
        return "FnDocument [fnDocumentClass=" + this.fnDocumentClass + ", fnDocumentName=" + this.fnDocumentName + ", fnDocumentID=" + this.fnDocumentID + ", fnDocumentAppId=" + this.fnDocumentAppId + ", fnDocumentURL=" + this.fnDocumentURL + ", fnDocumentStatus=" + this.fnDocumentStatus + ", fnWorkflowID=" + this.fnWorkflowID + ", fnPropertyList=" + this.fnPropertyList + ", fnFilesList=" + this.fnFilesList + ", errorFlag=" + this.errorFlag + "]";
    }
}

