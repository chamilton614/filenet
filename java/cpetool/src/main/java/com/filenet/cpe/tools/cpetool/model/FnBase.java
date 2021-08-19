package com.filenet.cpe.tools.cpetool.model;

import java.util.ArrayList;
import java.util.List;

public class FnBase {
    private int documentCount;
    private int workflowCount;
    private int errorFlag;
    private List<FnDocumentList> fnDocumentList;
    private List<FnWorkflowList> fnWorkflowList;

    public int getDocumentCount() {
        return this.documentCount;
    }

    public void setDocumentCount(int documentCount) {
        this.documentCount = documentCount;
    }

    public int getWorkflowCount() {
        return this.workflowCount;
    }

    public void setWorkflowCount(int workflowCount) {
        this.workflowCount = workflowCount;
    }

    public int getErrorFlag() {
        return this.errorFlag;
    }

    public void setErrorFlag(int errorFlag) {
        this.errorFlag = errorFlag;
    }

    public List<FnDocumentList> getFnDocumentList() {
        if (this.fnDocumentList == null) {
            this.fnDocumentList = new ArrayList<FnDocumentList>();
        }
        return this.fnDocumentList;
    }

    public void setFnDocumentList(List<FnDocumentList> fnDocumentList) {
        this.fnDocumentList = fnDocumentList;
    }

    public List<FnWorkflowList> getFnWorkflowList() {
        if (this.fnWorkflowList == null) {
            this.fnWorkflowList = new ArrayList<FnWorkflowList>();
        }
        return this.fnWorkflowList;
    }

    public void setFnWorkflowList(List<FnWorkflowList> fnWorkflowList) {
        this.fnWorkflowList = fnWorkflowList;
    }

    public void addFnDocumentList(FnDocumentList fnDocumentList) {
        if (fnDocumentList.getErrorFlag() > 0) {
            this.getFnDocumentList().add(fnDocumentList);
            this.setErrorFlag(1);
            this.setDocumentCount(0);
        } else {
            this.getFnDocumentList().add(fnDocumentList);
            this.documentCount = this.getDocumentCount() + fnDocumentList.getCount();
        }
    }

    public void addFnWorkflowList(FnWorkflowList fnWorkflowList) {
        if (fnWorkflowList.getErrorFlag() > 0) {
            this.getFnWorkflowList().add(fnWorkflowList);
            this.setErrorFlag(1);
            this.setWorkflowCount(0);
        } else {
            this.getFnWorkflowList().add(fnWorkflowList);
            this.workflowCount = this.getWorkflowCount() + fnWorkflowList.getCount();
        }
    }

    public String toString() {
        return "FnBase [documentCount=" + this.documentCount + ", workflowCount=" + this.workflowCount + ", errorFlag=" + this.errorFlag + ", fnDocumentList=" + this.fnDocumentList + ", fnWorkflowList=" + this.fnWorkflowList + "]";
    }
}

