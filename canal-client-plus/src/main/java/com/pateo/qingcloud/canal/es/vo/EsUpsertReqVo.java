package com.pateo.qingcloud.canal.es.vo;

import lombok.Data;

@Data
public class EsUpsertReqVo<E>{

    // 索引名称
    private String index;

    // es主键
    private String id;

    // 是否父文档,默认是
    private boolean parentFlag = true;

    // 指向父文档的主键id,只有添加类型不是父文档的时候才使用此字段
    private String routing;

    private E data;

}
