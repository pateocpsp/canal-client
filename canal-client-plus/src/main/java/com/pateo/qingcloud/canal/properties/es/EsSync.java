package com.pateo.qingcloud.canal.properties.es;


import java.util.List;
import java.util.Map;


public class EsSync {
    // aviator表达式,tenant_code为上面fields定义的字段
    private String syncRule;
    // 数据库需要同步到es的字段
    private List<String> fields;
    private String index;
    // 索引后缀名
    private String indexDynamicFieldSuffix;
    // es主键id
    private String id;
    // es主键id自定义前缀
    private String idPrefix;
    // 父子表关联关系
    private Map<String, ParentChildren> relations;
    // 跳过的字段不需要同步到es
    private List<String> skips;
    // 特殊类型的字段
    private Map<String, Object> objFields;
    // 数据库字段存到es里面去的别名
    private Map<String, Object> aliasFields;
    // 把指定字段插入到es某个数组里面，删除时需要删除数组里面数据,配置这个只会同步updateArrayFields里面定义的字段
    private List<Map<String, Object>> updateArrayFields;
    // es同步的事件，insert,update,delete,all,不配置默认为all
    private List<String> event;

    @Override
    public String toString() {
        return "EsSync{" +
                "syncRule='" + syncRule + '\'' +
                ", fields=" + fields +
                ", index='" + index + '\'' +
                ", indexDynamicFieldSuffix='" + indexDynamicFieldSuffix + '\'' +
                ", id='" + id + '\'' +
                ", idPrefix='" + idPrefix + '\'' +
                ", relations=" + relations +
                ", skips=" + skips +
                ", objFields=" + objFields +
                ", aliasFields=" + aliasFields +
                ", updateArrayFields=" + updateArrayFields +
                ", event=" + event +
                '}';
    }

    public List<String> getEvent() {
        return event;
    }

    public void setEvent(List<String> event) {
        this.event = event;
    }

    public String getSyncRule() {
        return syncRule;
    }

    public void setSyncRule(String syncRule) {
        this.syncRule = syncRule;
    }

    public Map<String, Object> getAliasFields() {
        return aliasFields;
    }

    public void setAliasFields(Map<String, Object> aliasFields) {
        this.aliasFields = aliasFields;
    }


    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getIndexDynamicFieldSuffix() {
        return indexDynamicFieldSuffix;
    }

    public void setIndexDynamicFieldSuffix(String indexDynamicFieldSuffix) {
        this.indexDynamicFieldSuffix = indexDynamicFieldSuffix;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdPrefix() {
        return idPrefix;
    }

    public void setIdPrefix(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    public Map<String, ParentChildren> getRelations() {
        return relations;
    }

    public void setRelations(Map<String, ParentChildren> relations) {
        this.relations = relations;
    }

    public List<String> getSkips() {
        return skips;
    }

    public void setSkips(List<String> skips) {
        this.skips = skips;
    }

    public Map<String, Object> getObjFields() {
        return objFields;
    }

    public void setObjFields(Map<String, Object> objFields) {
        this.objFields = objFields;
    }

    public List<Map<String, Object>> getUpdateArrayFields() {
        return updateArrayFields;
    }

    public void setUpdateArrayFields(List<Map<String, Object>> updateArrayFields) {
        this.updateArrayFields = updateArrayFields;
    }
}


