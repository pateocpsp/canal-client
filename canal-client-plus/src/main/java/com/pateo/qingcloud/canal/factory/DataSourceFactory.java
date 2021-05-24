package com.pateo.qingcloud.canal.factory;


import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.pateo.qingcloud.canal.properties.CanalClientPlusInfo;
import com.pateo.qingcloud.canal.properties.CanalClientPlusInfoMap;
import com.pateo.qingcloud.canal.properties.druid.DataBaseInfo;
import com.pateo.qingcloud.canal.properties.druid.DruidInfo;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gujiachun
 */
@Slf4j
public class DataSourceFactory {

    private CanalClientPlusInfoMap canalClientPlusInfoMap;

    private Map<String, DataSource> dataSourceMap;

    public DataSourceFactory(CanalClientPlusInfoMap canalClientPlusInfoMap){
        this.canalClientPlusInfoMap = canalClientPlusInfoMap;
        init();
    }

    private void init() {
        dataSourceMap = new ConcurrentHashMap<>(10);
        initDataSource();
    }


    /**
     * 通过库名+表名动态返回数据源
     * @param dataSouse
     * @return
     */
    public DataSource getDataSource(String dataSouse) {
        return dataSourceMap.get(dataSouse);
    }

    // JVM退出的时候执行
    /*@PreDestroy
    protected void destroy(){


    }*/

    /**
     * 初始化连接对象
     */
    private void initDataSource() {
        log.info("\n");
        log.info("********************MySql数据源初始化中···********************");
        log.info("====================================================================================================");
        DataSource ds = null;
        if (dataSourceMap.size() == 0) {
            synchronized (dataSourceMap) {
                if (dataSourceMap.size() == 0) {
                    if (canalClientPlusInfoMap != null && canalClientPlusInfoMap.getCanalClientPlusInfoMap().size() > 0) {
                        for(Map.Entry<String, CanalClientPlusInfo> entry : canalClientPlusInfoMap.getCanalClientPlusInfoMap().entrySet()){
                            String key = entry.getKey();
                            CanalClientPlusInfo canalClientPlusInfo = entry.getValue();

                            DruidInfo properties = canalClientPlusInfo.getDatasource();
                            Map<String, DataBaseInfo> proMap =  properties.getDruid();

                            for(Map.Entry<String, DataBaseInfo> dataBase : proMap.entrySet()){
                                String mapKey = dataBase.getKey();
                                DataBaseInfo mapValue = dataBase.getValue();
                                Properties pro = new Properties();
                                pro.setProperty(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, mapValue.getDriverClassName());
                                pro.setProperty(DruidDataSourceFactory.PROP_URL, mapValue.getUrl());
                                pro.setProperty(DruidDataSourceFactory.PROP_USERNAME, mapValue.getUsername());
                                pro.setProperty(DruidDataSourceFactory.PROP_PASSWORD, mapValue.getPassword());
                                pro.setProperty(DruidDataSourceFactory.PROP_INITIALSIZE, String.valueOf(properties.getInitialSize()));
                                pro.setProperty(DruidDataSourceFactory.PROP_MAXACTIVE, String.valueOf(properties.getMaxActive()));
                                pro.setProperty(DruidDataSourceFactory.PROP_MAXWAIT, String.valueOf(properties.getMaxWait()));
                                pro.setProperty(DruidDataSourceFactory.PROP_TIMEBETWEENEVICTIONRUNSMILLIS, String.valueOf(properties.getTimeBetweenEvictionRunsMillis()));
                                pro.setProperty(DruidDataSourceFactory.PROP_MINEVICTABLEIDLETIMEMILLIS, String.valueOf(properties.getMinEvictableIdleTimeMillis()));
                                pro.setProperty(DruidDataSourceFactory.PROP_TESTONBORROW, properties.getTestOnBorrow());
                                pro.setProperty(DruidDataSourceFactory.PROP_TESTWHILEIDLE, properties.getTestWhileIdle());
                                try {
                                    ds = DruidDataSourceFactory.createDataSource(pro);
                                    String dataSourceKey = key.concat(mapKey);
                                    // 获取连接
                                    dataSourceMap.put(dataSourceKey, ds);

                                    log.info("数据源初始化成功···");
                                    log.info("当前初始化的DataSourceFactory数据如下>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                                    log.info("当前初始化的项目名为：{}，配置的数据源名称为：{}", key, mapKey);
                                    log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");

                                } catch (Exception e) {
                                    log.error("获取数据库连接发生异常啦" + e.getMessage());
                                    log.info("error============{}", mapValue);
                                }
                            }
                        }
                    }
                }
            }
        }
        log.info("====================================================================================================\n");
    }
}
