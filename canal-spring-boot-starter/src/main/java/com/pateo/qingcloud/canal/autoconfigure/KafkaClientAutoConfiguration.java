package com.pateo.qingcloud.canal.autoconfigure;


import com.google.common.collect.Lists;
import com.pateo.qingcloud.canal.KafkaCanalClient;
import com.pateo.qingcloud.canal.factory.MapColumnModelFactory;
import com.pateo.qingcloud.canal.handler.EntryHandler;
import com.pateo.qingcloud.canal.handler.MessageHandler;
import com.pateo.qingcloud.canal.handler.RowDataHandler;
import com.pateo.qingcloud.canal.handler.impl.AsyncFlatMessageHandlerImpl;
import com.pateo.qingcloud.canal.handler.impl.MapRowDataHandlerImpl;
import com.pateo.qingcloud.canal.handler.impl.SyncFlatMessageHandlerImpl;
import com.pateo.qingcloud.canal.init.CanalClientPostBean;
import com.pateo.qingcloud.canal.properties.CanalClientPlusInfo;
import com.pateo.qingcloud.canal.properties.CanalClientPlusInfoMap;
import com.pateo.qingcloud.canal.properties.CanalClientProperties;
import com.pateo.qingcloud.canal.properties.kafka.Consumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Configuration
@EnableConfigurationProperties(CanalClientProperties.class)
@ConditionalOnBean(value = {EntryHandler.class})
@ConditionalOnProperty(value = CanalClientProperties.CANAL_MODE, havingValue = "kafka")
@Import({ThreadPoolAutoConfiguration.class, CanalClientPostBean.class})
public class KafkaClientAutoConfiguration {


    private CanalClientProperties canalClientProperties;

    private Map<String, CanalClientPlusInfo> canalClientPlusInfoMap;


    public KafkaClientAutoConfiguration(CanalClientPlusInfoMap canalClientPlusInfoMap, CanalClientProperties canalClientProperties) {
        this.canalClientPlusInfoMap = canalClientPlusInfoMap.getCanalClientPlusInfoMap();
        this.canalClientProperties = canalClientProperties;
    }


    @Bean
    public RowDataHandler<List<Map<String, String>>> rowDataHandler() {
        return new MapRowDataHandlerImpl(new MapColumnModelFactory());
    }

    @Bean
    @ConditionalOnProperty(value = CanalClientProperties.CANAL_ASYNC, havingValue = "false")
    public MessageHandler syncFlatMessageHandler(RowDataHandler<List<Map<String, String>>> rowDataHandler, List<EntryHandler> entryHandlers) {
        return new SyncFlatMessageHandlerImpl(entryHandlers, rowDataHandler);
    }

    @Bean
    @ConditionalOnProperty(value = CanalClientProperties.CANAL_ASYNC, havingValue = "true", matchIfMissing = true)
    public MessageHandler asyncFlatMessageHandler(RowDataHandler<List<Map<String, String>>> rowDataHandler,
                                                  List<EntryHandler> entryHandlers,
                                                  ExecutorService executorService) {
        return new AsyncFlatMessageHandlerImpl(entryHandlers, rowDataHandler, executorService);
    }


//    @Bean(initMethod = "start", destroyMethod = "stop")
//    public KafkaCanalClient kafkaCanalClient(MessageHandler messageHandler) {
//        return KafkaCanalClient.builder().servers(canalKafkaProperties.getServer())
//                .groupId(canalKafkaProperties.getGroupId())
//                .topic(canalKafkaProperties.getDestination())
//                .messageHandler(messageHandler)
//                .batchSize(canalKafkaProperties.getBatchSize())
//                .filter(canalKafkaProperties.getFilter())
//                .timeout(canalKafkaProperties.getTimeout())
//                .unit(canalKafkaProperties.getUnit())
//                .build();
//    }

    @Bean
    public List<KafkaCanalClient> kafkaCanalClientList(MessageHandler messageHandler) {
        List<KafkaCanalClient> clientList = Lists.newArrayList();
        if (canalClientPlusInfoMap.size() > 0) {
            for (Map.Entry<String, CanalClientPlusInfo> canalClientPlusInfoEntry : canalClientPlusInfoMap.entrySet()) {
                CanalClientPlusInfo value = canalClientPlusInfoEntry.getValue();
                for (Consumer c : value.getConsumers()) {
                    //Consumer c = consumer.get("consumer");
                    KafkaCanalClient client = KafkaCanalClient.builder().servers(canalClientProperties.getKafka())
                            .groupId(c.getGroupId())
                            .topic(c.getTopic())
                            .messageHandler(messageHandler)
                            .batchSize(canalClientProperties.getBatchSize())
                            .filter(canalClientProperties.getFilter())
                            .timeout(canalClientProperties.getTimeout())
                            .unit(canalClientProperties.getUnit())
                            .build();
                    clientList.add(client);
                }
            }
        }
        return clientList;
    }


}
