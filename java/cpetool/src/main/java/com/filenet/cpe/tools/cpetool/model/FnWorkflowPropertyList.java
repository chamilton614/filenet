package com.filenet.cpe.tools.cpetool.model;

import java.util.ArrayList;
import java.util.List;

public class FnWorkflowPropertyList {
    private int count;
    private int errorFlag;
    private List<FnWorkflowProperty> fnWorkflowPropsList;

    public int getCount() {
        return this.getFnWorkflowPropsList().size();
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getErrorFlag() {
        return this.errorFlag;
    }

    public void setErrorFlag(int errorFlag) {
        this.errorFlag = errorFlag;
    }

    public List<FnWorkflowProperty> getFnWorkflowPropsList() {
        if (this.fnWorkflowPropsList == null) {
            this.fnWorkflowPropsList = new ArrayList<FnWorkflowProperty>();
        }
        return this.fnWorkflowPropsList;
    }

    public void setFnWorkflowPropsList(List<FnWorkflowProperty> fnWorkflowPropsList) {
        this.fnWorkflowPropsList = fnWorkflowPropsList;
    }

    public void addFnWorkflowProperty(FnWorkflowProperty fnWorkflowProperty) {
        this.getFnWorkflowPropsList().add(fnWorkflowProperty);
        this.count = this.getCount();
    }

    public void addFnWorkflowPropertyList(FnWorkflowPropertyList fnWorkflowPropsList) {
        if (fnWorkflowPropsList.getErrorFlag() > 0) {
            this.getFnWorkflowPropsList().addAll(fnWorkflowPropsList.getFnWorkflowPropsList());
            this.setErrorFlag(1);
            this.setCount(0);
        } else {
            this.getFnWorkflowPropsList().addAll(fnWorkflowPropsList.getFnWorkflowPropsList());
            this.count = this.getCount();
        }
    }

    public String toString() {
        return "FnWorkflowPropertyList [count=" + this.count + ", errorFlag=" + this.errorFlag + ", fnWorkflowPropsList=" + this.fnWorkflowPropsList + "]";
    }
}

