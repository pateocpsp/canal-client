package com.pateo.qingcloud.canal.autoconfigure;

import com.pateo.qingcloud.canal.es.EsService;
import com.pateo.qingcloud.canal.es.impl.EsServiceImpl;
import com.pateo.qingcloud.canal.properties.CanalSimpleProperties;
import com.pateo.qingcloud.canal.strategy.CustomConnectionKeepAliveStrategy;
import com.pateo.qingcloud.canal.properties.ElasticsearchMonitorProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gujiachun
 */
@Slf4j
@EnableConfigurationProperties(ElasticsearchMonitorProperties.class)
@Configuration
public class ElasticsearchAutoConfiguration {

    private ElasticsearchMonitorProperties esProperties;

    public ElasticsearchAutoConfiguration(ElasticsearchMonitorProperties esProperties) {
        this.esProperties = esProperties;
    }

    @Bean
    public RestHighLevelClient restHighLevelClient() {
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
            /*HttpHost proxy = new HttpHost("127.0.0.1", 22, "http");
            requestConfigBuilder.setProxy(proxy);*/
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

        return new RestHighLevelClient(builder);
    }

    @Bean
    public EsService esServiceImpl(RestHighLevelClient restHighLevelClient){
        return new EsServiceImpl(restHighLevelClient);
    }
}