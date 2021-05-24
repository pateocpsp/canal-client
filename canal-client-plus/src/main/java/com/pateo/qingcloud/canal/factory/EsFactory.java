package com.pateo.qingcloud.canal.factory;

import com.pateo.qingcloud.canal.properties.CanalClientPlusInfo;
import com.pateo.qingcloud.canal.properties.CanalClientPlusInfoMap;
import com.pateo.qingcloud.canal.properties.es.ElasticsearchMonitorProperties;
import com.pateo.qingcloud.canal.strategy.CustomConnectionKeepAliveStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import java.nio.file.LinkOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class EsFactory {

    private Map<String, RestHighLevelClient> restHighLevelClientMap = new ConcurrentHashMap<>(10);

    private CanalClientPlusInfoMap canalClientPlusInfoMap;

    public EsFactory(CanalClientPlusInfoMap canalClientPlusInfoMap) {
        this.canalClientPlusInfoMap = canalClientPlusInfoMap;
        initRestHighLevelClients();

    }

    /**
     * 获取es对象
     * @param key
     * @return
     */
    public RestHighLevelClient getRestHighLevelClient(String key) {
       return restHighLevelClientMap.get(key);
    }

    /**
     * 初始化es连接工厂
     */
    public void initRestHighLevelClients() {
        log.info("\n");
        log.info("********************ES数据源初始化中···********************");
        log.info("====================================================================================================\n");
        if (restHighLevelClientMap.size() == 0) {
            synchronized (this.restHighLevelClientMap) {
                if (restHighLevelClientMap.size() == 0) {
                    for (Map.Entry<String, CanalClientPlusInfo> entry : canalClientPlusInfoMap.getCanalClientPlusInfoMap().entrySet()) {
                        String key = entry.getKey();
                        CanalClientPlusInfo canalClientPlusInfo = entry.getValue();

                        for (Map.Entry<String, ElasticsearchMonitorProperties> es : canalClientPlusInfo.getElasticsearch().entrySet()) {
                            String esKey = es.getKey();
                            ElasticsearchMonitorProperties esProperties = es.getValue();
                            String[] urlArr = esProperties.getEsAddress().split(",");
                            HttpHost[] httpPostArr = new HttpHost[urlArr.length];
                            for (int i = 0; i < urlArr.length; i++) {
                                HttpHost httpHost = new HttpHost(urlArr[i].split(":")[0].trim(),
                                        Integer.parseInt(urlArr[i].split(":")[1].trim()), "http");
                                httpPostArr[i] = httpHost;
                            }
                            RestClientBuilder builder = RestClient.builder(httpPostArr);
                            // 异步httpclient连接延时配置
                            builder.setRequestConfigCallback(requestConfigBuilder -> {
                                requestConfigBuilder.setConnectTimeout(esProperties.getConnectTimeOut());
                                requestConfigBuilder.setSocketTimeout(esProperties.getSocketTimeOut());
                                requestConfigBuilder.setConnectionRequestTimeout(esProperties.getConnectionRequestTimeOut());

                                //HttpHost proxy = new HttpHost("127.0.0.1", 22, "http");
                                //requestConfigBuilder.setProxy(proxy);

                                return requestConfigBuilder;
                            });

                            // 异步httpclient配置
                            builder.setHttpClientConfigCallback(httpClientBuilder -> {

                                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                                credentialsProvider.setCredentials(AuthScope.ANY,
                                        new UsernamePasswordCredentials(esProperties.getUsername(), esProperties.getPassword()));  //es账号密码（默认用户名为elastic）

                                // httpclient连接数配置
                                httpClientBuilder.setMaxConnTotal(esProperties.getMaxConnectNum());
                                httpClientBuilder.setMaxConnPerRoute(esProperties.getMaxConnectPerRoute());
                                // httpclient保活策略
                                httpClientBuilder.setKeepAliveStrategy(
                                        CustomConnectionKeepAliveStrategy.getInstance(esProperties.getKeepAliveMinutes()));
                                httpClientBuilder.disableAuthCaching();
                                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                                return httpClientBuilder;
                            });

                            String restHighLevelClientKey = key.concat(esKey);
                            restHighLevelClientMap.put(restHighLevelClientKey, new RestHighLevelClient(builder));

                            log.info("ES数据源初始化成功···");
                            log.info("当前初始化的EsFactory数据如下>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                            log.info("当前初始化的项目名为：{}，配置的数据源名称为：{}", key, esKey);
                            log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");

                        }
                    }
                }
            }
        }
        log.info("====================================================================================================\n");
    }
}
