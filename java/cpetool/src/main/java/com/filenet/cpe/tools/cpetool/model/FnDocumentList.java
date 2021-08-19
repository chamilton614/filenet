package com.filenet.cpe.tools.cpetool.model;

import java.util.ArrayList;
import java.util.List;

public class FnDocumentList {
    private String className;
    private int count;
    private int errorFlag;
    private List<FnDocument> fnDocList;

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getCount() {
        return this.getFnDocList().size();
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

    public List<FnDocument> getFnDocList() {
        if (this.fnDocList == null) {
            this.fnDocList = new ArrayList<FnDocument>();
        }
        return this.fnDocList;
    }

    public void setFnDocList(List<FnDocument> fnDocList) {
        this.fnDocList = fnDocList;
    }

    public void addFnDoc(FnDocument fnDoc) {
        if (fnDoc.getErrorFlag() > 0) {
            this.getFnDocList().add(fnDoc);
            this.setErrorFlag(1);
            this.setCount(0);
        } else {
            this.getFnDocList().add(fnDoc);
            this.count = this.getCount();
        }
    }

    public void addFnDocList(FnDocumentList fnDocList) {
        if (fnDocList.getErrorFlag() > 0) {
            this.getFnDocList().addAll(fnDocList.getFnDocList());
            this.setErrorFlag(1);
            this.setCount(0);
        } else {
            this.getFnDocList().addAll(fnDocList.getFnDocList());
            this.count = this.getCount();
        }
    }

    public String toString() {
        return "FnDocumentList [className=" + this.className + ", count=" + this.count + ", errorFlag=" + this.errorFlag + ", fnDocList=" + this.fnDocList + "]";
    }
}

