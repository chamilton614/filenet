package com.filenet.cpe.tools.cpetool.model;

public class FnWorkflowProperty {
    private String name;
    private String value;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString() {
        return "FnWorkflowProperty [name=" + this.name + ", value=" + this.value + "]";
    }
}

