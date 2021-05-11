package com.pateo.qingcloud.canal.properties;

import com.pateo.qingcloud.canal.properties.druid.DruidInfo;
import com.pateo.qingcloud.canal.properties.es.EsSync;
import com.pateo.qingcloud.canal.properties.rdb.SourceInfo;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author gujiachun
 */
@Data
public class CanalClientPlusInfo {

    private DruidInfo datasource;

    private Map<String, List<EsSync>> esSync;

    private Map<String, SourceInfo> rdb;

}
