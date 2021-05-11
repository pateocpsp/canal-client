package com.pateo.qingcloud.canal.autoconfigure;


import com.alibaba.otter.canal.protocol.CanalEntry;
import com.pateo.qingcloud.canal.ClusterCanalClient;
import com.pateo.qingcloud.canal.factory.EntryColumnModelFactory;
import com.pateo.qingcloud.canal.handler.EntryHandler;
import com.pateo.qingcloud.canal.handler.MessageHandler;
import com.pateo.qingcloud.canal.handler.RowDataHandler;
import com.pateo.qingcloud.canal.handler.impl.AsyncMessageHandlerImpl;
import com.pateo.qingcloud.canal.handler.impl.RowDataHandlerImpl;
import com.pateo.qingcloud.canal.handler.impl.SyncMessageHandlerImpl;
import com.pateo.qingcloud.canal.properties.CanalProperties;
import com.pateo.qingcloud.canal.properties.CanalSimpleProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Configuration
@EnableConfigurationProperties(CanalSimpleProperties.class)
@ConditionalOnBean(value = {EntryHandler.class})
@ConditionalOnProperty(value = CanalProperties.CANAL_MODE, havingValue = "cluster")
@Import(ThreadPoolAutoConfiguration.class)
public class ClusterClientAutoConfiguration {


    private CanalSimpleProperties canalSimpleProperties;


    public ClusterClientAutoConfiguration(CanalSimpleProperties canalSimpleProperties) {
        this.canalSimpleProperties = canalSimpleProperties;
    }

    @Bean
    public RowDataHandler<CanalEntry.RowData> rowDataHandler() {
        return new RowDataHandlerImpl(new EntryColumnModelFactory());
    }

    @Bean
    @ConditionalOnProperty(value = CanalProperties.CANAL_ASYNC, havingValue = "true", matchIfMissing = true)
    public MessageHandler asyncMessageHandler(RowDataHandler<CanalEntry.RowData> rowDataHandler, List<EntryHandler> entryHandlers,
                                              ExecutorService executorService) {
        return new AsyncMessageHandlerImpl(entryHandlers, rowDataHandler, executorService);
    }


    @Bean
    @ConditionalOnProperty(value = CanalProperties.CANAL_ASYNC, havingValue = "false")
    public MessageHandler syncMessageHandler(RowDataHandler<CanalEntry.RowData> rowDataHandler, List<EntryHandler> entryHandlers) {
        return new SyncMessageHandlerImpl(entryHandlers, rowDataHandler);
    }


    @Bean(initMethod = "start", destroyMethod = "stop")
    public ClusterCanalClient clusterCanalClient(MessageHandler messageHandler) {
        return ClusterCanalClient.Builder.builder().
                canalServers(canalSimpleProperties.getServer())
                .destination(canalSimpleProperties.getDestination())
                .userName(canalSimpleProperties.getUserName())
                .messageHandler(messageHandler)
                .password(canalSimpleProperties.getPassword())
                .batchSize(canalSimpleProperties.getBatchSize())
                .filter(canalSimpleProperties.getFilter())
                .timeout(canalSimpleProperties.getTimeout())
                .unit(canalSimpleProperties.getUnit()).build();
    }
}
