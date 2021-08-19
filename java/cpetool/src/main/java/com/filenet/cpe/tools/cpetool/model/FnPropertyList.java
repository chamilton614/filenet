package com.filenet.cpe.tools.cpetool.model;

import java.util.ArrayList;
import java.util.List;

public class FnPropertyList {
    private int count;
    private int errorFlag;
    private List<FnProperty> fnDocumentPropsList;

    public int getCount() {
        this.count = this.getFnDocumentPropsList().size();
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<FnProperty> getFnDocumentPropsList() {
        if (this.fnDocumentPropsList == null) {
            this.fnDocumentPropsList = new ArrayList<FnProperty>();
        }
        return this.fnDocumentPropsList;
    }

    public void setFnDocumentPropsList(List<FnProperty> fnDocumentPropsList) {
        this.fnDocumentPropsList = fnDocumentPropsList;
    }

    public void addFnProperty(FnProperty fnProperty) {
        this.getFnDocumentPropsList().add(fnProperty);
        this.count = this.getCount();
    }

    public void addFnPropertyList(FnPropertyList fnPropertyList) {
        if (fnPropertyList.getErrorFlag() > 0) {
            this.getFnDocumentPropsList().addAll(fnPropertyList.getFnDocumentPropsList());
            this.setErrorFlag(1);
            this.setCount(0);
        } else {
            this.getFnDocumentPropsList().addAll(fnPropertyList.getFnDocumentPropsList());
            this.count = this.getCount();
        }
    }

    public void clear() {
        if (this.getFnDocumentPropsList().size() > 0) {
            this.getFnDocumentPropsList().clear();
            this.count = 0;
            this.errorFlag = 0;
        } else {
            this.count = 0;
            this.errorFlag = 0;
        }
    }

    public int getErrorFlag() {
        return this.errorFlag;
    }

    public void setErrorFlag(int errorFlag) {
        this.errorFlag = errorFlag;
    }

    public String toString() {
        return "FnPropertyList [count=" + this.count + ", errorFlag=" + this.errorFlag + ", fnDocumentPropsList=" + this.fnDocumentPropsList + "]";
    }
}

