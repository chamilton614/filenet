package com.filenet.cpe.tools.cpetool.model;

import java.util.ArrayList;
import java.util.List;

public class FnFileList {
    private int count;
    private int errorFlag;
    private List<String> fnFilesList;

    public int getCount() {
        return this.getFnFilesList().size();
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

    public List<String> getFnFilesList() {
        if (this.fnFilesList == null) {
            this.fnFilesList = new ArrayList<String>();
        }
        return this.fnFilesList;
    }

    public void setFnFilesList(List<String> fnFilesList) {
        this.fnFilesList = fnFilesList;
    }

    public void addFnFile(String fnFile) {
        if (fnFile.equals("")) {
            this.setErrorFlag(1);
            this.setCount(0);
        } else {
            this.getFnFilesList().add(fnFile);
            this.count = this.getCount();
        }
    }

    public void addFnFileList(FnFileList fnFileList) {
        if (fnFileList.getErrorFlag() > 0) {
            this.getFnFilesList().addAll(fnFileList.getFnFilesList());
            this.setErrorFlag(1);
            this.setCount(0);
        } else {
            this.getFnFilesList().addAll(fnFileList.getFnFilesList());
            this.count = this.getCount();
        }
    }

    public String toString() {
        return "FnFileList [count=" + this.count + ", errorFlag=" + this.errorFlag + ", fnFilesList=" + this.fnFilesList + "]";
    }
}

