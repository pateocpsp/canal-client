package com.pateo.qingcloud.canal.properties;

import com.pateo.qingcloud.canal.properties.druid.DruidInfo;
import com.pateo.qingcloud.canal.properties.es.EsSync;
import com.pateo.qingcloud.canal.properties.rdb.SourceInfo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * @author gujiachun
 */
@ConfigurationProperties(prefix = "pateo")
@Data
public class CanalClientPlusProperties {

    private DruidInfo datasource;

    private Map<String, List<EsSync>> esSync;

    private Map<String, SourceInfo> rdb;
}
