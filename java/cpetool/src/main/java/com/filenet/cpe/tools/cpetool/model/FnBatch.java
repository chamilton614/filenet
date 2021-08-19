package com.filenet.cpe.tools.cpetool.model;

import java.util.ArrayList;
import java.util.List;

public class FnBatch {
    private int count;
    private String priority;
    private String processed;
    private FnDocClass fnDocClass;
    private List<FnDocument> fnDocsList;
    private int errorFlag;

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getPriority() {
        return this.priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getProcessed() {
        return this.processed;
    }

    public void setProcessed(String processed) {
        this.processed = processed;
    }

    public FnDocClass getFnDocClass() {
        return this.fnDocClass;
    }

    public void setFnDocClass(FnDocClass fnDocClass) {
        this.fnDocClass = fnDocClass;
    }

    public int getErrorFlag() {
        return this.errorFlag;
    }

    public void setErrorFlag(int errorFlag) {
        this.errorFlag = errorFlag;
    }

    public List<FnDocument> getFnDocsList() {
        if (this.fnDocsList == null) {
            this.fnDocsList = new ArrayList<FnDocument>();
        }
        return this.fnDocsList;
    }

    public void setFnDocsList(List<FnDocument> fnDocsList) {
        this.fnDocsList = fnDocsList;
    }

    public void addFnDoc(FnDocument fnDoc) {
        this.getFnDocsList().add(fnDoc);
        this.count = this.getFnDocsList().size();
    }

    public String toString() {
        return "FnBatch [count=" + this.count + ", priority=" + this.priority + ", processed=" + this.processed + ", fnDocClass=" + this.fnDocClass + ", fnDocsList=" + this.fnDocsList + ", errorFlag=" + this.errorFlag + "]";
    }
}

