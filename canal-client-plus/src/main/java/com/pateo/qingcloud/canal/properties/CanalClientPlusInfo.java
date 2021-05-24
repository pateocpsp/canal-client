package com.pateo.qingcloud.canal.properties;

import com.pateo.qingcloud.canal.properties.druid.DruidInfo;
import com.pateo.qingcloud.canal.properties.es.ElasticsearchMonitorProperties;
import com.pateo.qingcloud.canal.properties.es.EsSync;
import com.pateo.qingcloud.canal.properties.kafka.Consumer;
import com.pateo.qingcloud.canal.properties.rdb.SourceInfo;
import java.util.List;
import java.util.Map;

/**
 * @author gujiachun
 */
public class CanalClientPlusInfo {

    private DruidInfo datasource;

    private Map<String, List<EsSync>> esSync;

    private Map<String, SourceInfo> rdb;

    private Map<String, ElasticsearchMonitorProperties> elasticsearch;

    private List<Consumer> consumers;


    @Override
    public String toString() {
        return "CanalClientPlusInfo{" +
                "datasource=" + datasource +
                ", esSync=" + esSync +
                ", rdb=" + rdb +
                ", elasticsearch=" + elasticsearch +
                ", consumers=" + consumers +
                '}';
    }

    public List<Consumer> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<Consumer> consumers) {
        this.consumers = consumers;
    }

    public DruidInfo getDatasource() {
        return datasource;
    }

    public void setDatasource(DruidInfo datasource) {
        this.datasource = datasource;
    }

    public Map<String, List<EsSync>> getEsSync() {
        return esSync;
    }

    public void setEsSync(Map<String, List<EsSync>> esSync) {
        this.esSync = esSync;
    }

    public Map<String, SourceInfo> getRdb() {
        return rdb;
    }

    public void setRdb(Map<String, SourceInfo> rdb) {
        this.rdb = rdb;
    }

    public Map<String, ElasticsearchMonitorProperties> getElasticsearch() {
        return elasticsearch;
    }

    public void setElasticsearch(Map<String, ElasticsearchMonitorProperties> elasticsearch) {
        this.elasticsearch = elasticsearch;
    }


}
