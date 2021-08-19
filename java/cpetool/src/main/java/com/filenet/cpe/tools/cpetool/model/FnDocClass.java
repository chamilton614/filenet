package com.filenet.cpe.tools.cpetool.model;

import java.util.ArrayList;
import java.util.List;

public class FnDocClass {
    private String name;
    private List<FnProperty> fnDocClassPropsList;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FnProperty> getFnDocClassPropsList() {
        if (this.fnDocClassPropsList == null) {
            this.fnDocClassPropsList = new ArrayList<FnProperty>();
        }
        return this.fnDocClassPropsList;
    }

    public void setFnDocClassPropsList(List<FnProperty> fnDocClassPropsList) {
        this.fnDocClassPropsList = fnDocClassPropsList;
    }

    public String toString() {
        return "FnDocClass [name=" + this.name + ", fnDocClassPropsList=" + this.fnDocClassPropsList + "]";
    }
}

