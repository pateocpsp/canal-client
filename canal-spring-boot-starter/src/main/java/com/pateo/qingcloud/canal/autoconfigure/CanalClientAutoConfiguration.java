package com.pateo.qingcloud.canal.autoconfigure;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.pateo.qingcloud.canal.dao.CommonDao;
import com.pateo.qingcloud.canal.dao.impl.CommonDaoImpl;
import com.pateo.qingcloud.canal.factory.DataSourceFactory;
import com.pateo.qingcloud.canal.factory.EsFactory;
import com.pateo.qingcloud.canal.handler.AllEntryHandler;
import com.pateo.qingcloud.canal.properties.CanalClientPlusInfo;
import com.pateo.qingcloud.canal.properties.CanalClientPlusInfoMap;
import com.pateo.qingcloud.canal.properties.CanalClientProperties;
import com.pateo.qingcloud.canal.properties.Instance;
import com.pateo.qingcloud.canal.utils.CommonUtils;
import com.pateo.qingcloud.canal.utils.IpUtils;
import com.pateo.qingcloud.canal.utils.YamlUtils;
import com.pateo.qingcloud.canal.utils.ZooKeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@EnableConfigurationProperties(CanalClientProperties.class)
@Configuration
@Slf4j
public class CanalClientAutoConfiguration {

    @Value("${NACOS_SERVER}")
    private String nacosServer;

    @Value("${NACOS_USERNAME}")
    private String nacosUsername;

    @Value("${NACOS_PASSWORD}")
    private String nacosPassword;

    @Value("${NACOS_NAMESPACE}")
    private String nacosNameSpace;

    @Value("${nacos.config.group}")
    private String nacosGroup;

    private ZooKeeper zk = null;

    private CanalClientProperties canalClientProperties;

    public CanalClientAutoConfiguration(CanalClientProperties canalClientProperties) {
        this.canalClientProperties = canalClientProperties;
    }


    @Bean
    public CanalClientPlusInfoMap canalClientPlusInfoMap() {

        Map<String, CanalClientPlusInfo> canalClientPropertiesMap = new HashMap<>();

        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, nacosServer);
        properties.put(PropertyKeyConst.NAMESPACE, nacosNameSpace);
        properties.put(PropertyKeyConst.USERNAME, nacosUsername);
        properties.put(PropertyKeyConst.PASSWORD, nacosPassword);

        String mapStr = "";
        String instance = "";
        try {
            ConfigService configService = NacosFactory.createConfigService(properties);
            // ??????dataId???group?????????????????????????????????????????????. ?????????????????????????????????: dataId, group, ????????????
            ZooKeeperUtils zooKeeperUtils = new ZooKeeperUtils(canalClientProperties.getZkServers());
            String zkNode = canalClientProperties.getZkNode();
            zooKeeperUtils.create(zkNode, "", ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            for (Instance instanceData : canalClientProperties.getInstance()) {
                instance = instanceData.getName();
                String zkPath = zkNode + "/" +instance;
                zooKeeperUtils.create(zkPath, "", ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                int zkPathCount = zooKeeperUtils.getChildrenCount(zkPath);
                log.info("{}????????????????????????{}", zkPath, zkPathCount);
                // ??????zk?????????????????????????????????????????????????????????????????????????????????????????????
                if (zkPathCount < instanceData.getCluster()) {
                    // ?????????????????????ip?????????zk??????
                    zooKeeperUtils.create(zkPath + "/" + IpUtils.getLocalHostLANAddress().getHostAddress(), "", ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

                    String content = configService.getConfig(instance + ".yaml", nacosGroup, 3000L);
                    Map<String, Object> map = YamlUtils.parseYaml2Map(content);
                    mapStr = JSONObject.toJSONString(map.get(instance));
                    break;
                }
            }

            if ("".equals(mapStr)) {
                Instance instanceData = canalClientProperties.getInstance().get(canalClientProperties.getInstance().size() - 1);
                instance = instanceData.getName();
                String content = configService.getConfig(instance + ".yaml", nacosGroup, 3000L);
                Map<String, Object> map = YamlUtils.parseYaml2Map(content);
                mapStr = JSONObject.toJSONString(map.get(instance));

            }
            CanalClientPlusInfo canalClientPlusInfo = JSONObject.parseObject(mapStr, CanalClientPlusInfo.class);
            CommonUtils.stringToListCover(canalClientPlusInfo);
            canalClientPropertiesMap.put(instance, canalClientPlusInfo);
        } catch (Exception e) {
            log.error("??????nacos????????????????????????" + e.getMessage());
        }

        return new CanalClientPlusInfoMap(canalClientPropertiesMap);
    }

    @Bean
    public CommonDao commonDao() {
        return new CommonDaoImpl();
    }


    @Bean
    public DataSourceFactory dataSourceFactory(CanalClientPlusInfoMap canalClientPlusInfoMap) {
        return new DataSourceFactory(canalClientPlusInfoMap);
    }

    @Bean
    public EsFactory esFactory(CanalClientPlusInfoMap canalClientPlusInfoMap) {
        return new EsFactory(canalClientPlusInfoMap);
    }


    @Bean
    public AllEntryHandler allEntryHandler(CanalClientPlusInfoMap canalClientPlusInfoMap,
                                           CommonDao commonDao,
                                           DataSourceFactory dataSourceFactory,
                                           EsFactory esFactory) {
        return new AllEntryHandler(canalClientPlusInfoMap, commonDao, dataSourceFactory, esFactory);
    }

//    @Bean
//    public KafkaClientAutoConfiguration kafkaClientAutoConfiguration(CanalClientPlusInfoMap canalClientPlusInfoMap) {
//        return new KafkaClientAutoConfiguration(canalClientPlusInfoMap.getCanalClientPlusInfoMap(), canalClientProperties);
//    }

}
