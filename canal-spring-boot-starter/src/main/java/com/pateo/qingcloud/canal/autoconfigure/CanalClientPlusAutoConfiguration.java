package com.pateo.qingcloud.canal.autoconfigure;

import com.pateo.qingcloud.canal.dao.CommonDao;
import com.pateo.qingcloud.canal.dao.impl.CommonDaoImpl;
import com.pateo.qingcloud.canal.es.EsService;
import com.pateo.qingcloud.canal.factory.DataSourceFactory;
import com.pateo.qingcloud.canal.handler.AllEntryHandler;
import com.pateo.qingcloud.canal.properties.CanalClientPlusInfo;
import com.pateo.qingcloud.canal.properties.CanalClientPlusProperties;
import com.pateo.qingcloud.canal.properties.ElasticsearchMonitorProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gujiachun
 */
@Slf4j
@EnableConfigurationProperties(CanalClientPlusProperties.class)
@Configuration
public class CanalClientPlusAutoConfiguration {

    private CanalClientPlusProperties canalClientPlusProperties;

    public CanalClientPlusAutoConfiguration(CanalClientPlusProperties canalClientPlusProperties) {
        this.canalClientPlusProperties = canalClientPlusProperties;
    }

    @Bean
    public CanalClientPlusInfo canalClientPlusInfo(){
        CanalClientPlusInfo canalClientPlusInfo = new CanalClientPlusInfo();
        canalClientPlusInfo.setDatasource(this.canalClientPlusProperties.getDatasource());
        canalClientPlusInfo.setEsSync(this.canalClientPlusProperties.getEsSync());
        canalClientPlusInfo.setRdb(this.canalClientPlusProperties.getRdb());
        return  canalClientPlusInfo;
    }

    @Bean
    public CommonDao commonDao(){
        return new CommonDaoImpl();
    }

    @Bean
    public DataSourceFactory dataSourceFactory(CanalClientPlusInfo canalClientPlusInfo){
        return new DataSourceFactory(canalClientPlusInfo);
    }

    @Bean
    public AllEntryHandler allEntryHandler(CanalClientPlusInfo canalClientPlusInfo,
                                           EsService esService,CommonDao commonDao,
                                           DataSourceFactory dataSourceFactory){
        return new AllEntryHandler(canalClientPlusInfo, esService,commonDao,dataSourceFactory);
    }

}
