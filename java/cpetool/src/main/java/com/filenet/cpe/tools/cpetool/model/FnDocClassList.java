package com.filenet.cpe.tools.cpetool.model;

import java.util.ArrayList;
import java.util.List;

public class FnDocClassList {
    private int count;
    private List<FnDocClass> fnDocClassList;

    public int getCount() {
        return this.getFnDocClassList().size();
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<FnDocClass> getFnDocClassList() {
        if (this.fnDocClassList == null) {
            this.fnDocClassList = new ArrayList<FnDocClass>();
        }
        return this.fnDocClassList;
    }

    public void setFnDocClassList(List<FnDocClass> fnDocClassList) {
        this.fnDocClassList = fnDocClassList;
    }

    public void addFnDocClass(FnDocClass fnDocClass) {
        this.getFnDocClassList().add(fnDocClass);
        ++this.count;
    }

    public void addFnDocClassList(FnDocClassList fnDocClassList) {
        this.getFnDocClassList().addAll(fnDocClassList.getFnDocClassList());
        this.count = this.getCount();
    }

    public String toString() {
        return "FnDocClassList [count=" + this.count + ", fnDocClassList=" + this.fnDocClassList + "]";
    }
}

