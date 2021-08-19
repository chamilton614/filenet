package com.filenet.cpe.tools.cpetool.model;

import java.util.ArrayList;
import java.util.List;

public class FnBatchList {
    private int count;
    private int errorFlag;
    private List<FnBatch> fnBatchesList;

    public int getCount() {
        return this.getFnBatchesList().size();
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

    public List<FnBatch> getFnBatchesList() {
        if (this.fnBatchesList == null) {
            this.fnBatchesList = new ArrayList<FnBatch>();
        }
        return this.fnBatchesList;
    }

    public void setFnBatchesList(List<FnBatch> fnBatchesList) {
        this.fnBatchesList = fnBatchesList;
    }

    public void addFnBatch(FnBatch fnBatch) {
        if (fnBatch.getErrorFlag() > 0) {
            this.getFnBatchesList().add(fnBatch);
            this.setErrorFlag(1);
            this.setCount(0);
        } else {
            this.getFnBatchesList().add(fnBatch);
            this.count = this.getCount();
        }
    }

    public void addFnBatchList(FnBatchList fnBatchList) {
        if (fnBatchList.getErrorFlag() > 0) {
            this.getFnBatchesList().addAll(fnBatchList.getFnBatchesList());
            this.setErrorFlag(1);
            this.setCount(0);
        } else {
            this.getFnBatchesList().addAll(fnBatchList.getFnBatchesList());
            this.count = this.getCount();
        }
    }

    public String toString() {
        return "FnBatchList [count=" + this.count + ", errorFlag=" + this.errorFlag + ", fnBatchesList=" + this.fnBatchesList + "]";
    }
}

