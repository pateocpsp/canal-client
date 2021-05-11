package com.pateo.qingcloud.canal.properties.rdb;


import java.util.Map;


public class Source {

    private String database;
    private String table;
    private Map<String, Object> sourceColumn;

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Map<String, Object> getSourceColumn() {
        return sourceColumn;
    }

    public void setSourceColumn(Map<String, Object> sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    @Override
    public String toString() {
        return "Source{" +
                "database='" + database + '\'' +
                ", table='" + table + '\'' +
                ", sourceColumn=" + sourceColumn +
                '}';
    }
}

