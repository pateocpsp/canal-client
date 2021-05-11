package com.pateo.qingcloud.canal.properties.rdb;


import java.util.List;
import java.util.Map;


public class TableInfo {

    private String datasource;
    private String table;
    private Map<String, Object> targetColumn;

    @Override
    public String toString() {
        return "TableInfo{" +
                "datasource='" + datasource + '\'' +
                ", table='" + table + '\'' +
                ", targetColumn=" + targetColumn +
                '}';
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Map<String, Object> getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(Map<String, Object> targetColumn) {
        this.targetColumn = targetColumn;
    }
}