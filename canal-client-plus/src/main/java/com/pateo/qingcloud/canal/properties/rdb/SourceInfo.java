package com.pateo.qingcloud.canal.properties.rdb;

import java.util.List;


public class SourceInfo {

    private String instance;
    private Source source;
    private List<TableInfo> targets;

    @Override
    public String toString() {
        return "SourceProperties{" +
                "instance='" + instance + '\'' +
                ", source=" + source +
                ", targets=" + targets +
                '}';
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public List<TableInfo> getTargets() {
        return targets;
    }

    public void setTargets(List<TableInfo> targets) {
        this.targets = targets;
    }
}

