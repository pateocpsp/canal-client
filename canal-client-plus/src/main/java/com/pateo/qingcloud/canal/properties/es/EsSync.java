package com.pateo.qingcloud.canal.properties.es;


import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class EsSync {

    // 同步到es的源
    private String targetEs;
    // aviator表达式,tenant_code为上面fields定义的字段
    private String syncRule;
    // 数据库需要同步到es的字段
    private List<String> dbFields;
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
    private Map<String, Object> esFieldTypes;
    // 自定义的Es名称，如不定义默认使用数据库字段名称  数据库字段名称：es字段名称，这里配置了的属性最好在skips配置一下跳过
    private Map<String, Object> esFields;
    // 把指定字段插入到es某个数组里面，删除时需要删除数组里面数据,配置这个只会同步updateArrayFields里面定义的字段
    private List<Map<String, Object>> updateArrayFields;
    // es同步的事件，insert,update,delete,all,不配置默认为all
    private List<String> event;

}


