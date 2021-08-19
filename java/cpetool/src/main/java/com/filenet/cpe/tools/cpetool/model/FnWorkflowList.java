package com.filenet.cpe.tools.cpetool.model;

import java.util.ArrayList;
import java.util.List;

public class FnWorkflowList {
    private String processName;
    private String stepName;
    private int count;
    private int errorFlag;
    private List<FnWorkflow> fnWorkflowList;

    public String getProcessName() {
        return this.processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getStepName() {
        return this.stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public int getCount() {
        return this.getFnWorkflowList().size();
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

    public List<FnWorkflow> getFnWorkflowList() {
        if (this.fnWorkflowList == null) {
            this.fnWorkflowList = new ArrayList<FnWorkflow>();
        }
        return this.fnWorkflowList;
    }

    public void setFnWorkflowList(List<FnWorkflow> fnWorkflowList) {
        this.fnWorkflowList = fnWorkflowList;
    }

    public void addFnWorkflow(FnWorkflow fnWorkflow) {
        if (fnWorkflow.getErrorFlag() > 0) {
            this.getFnWorkflowList().add(fnWorkflow);
            this.setErrorFlag(1);
            this.setCount(0);
        } else {
            this.getFnWorkflowList().add(fnWorkflow);
            this.count = this.getCount();
        }
    }

    public void addFnWorkflowList(FnWorkflowList fnWorkflowList) {
        if (fnWorkflowList.getErrorFlag() > 0) {
            this.getFnWorkflowList().addAll(fnWorkflowList.getFnWorkflowList());
            this.setErrorFlag(1);
            this.setCount(0);
        } else {
            this.getFnWorkflowList().addAll(fnWorkflowList.getFnWorkflowList());
            this.count = this.getCount();
        }
    }

    public String toString() {
        return "FnWorkflowList [processName=" + this.processName + ", stepName=" + this.stepName + ", count=" + this.count + ", errorFlag=" + this.errorFlag + ", fnWorkflowList=" + this.fnWorkflowList + "]";
    }
}

