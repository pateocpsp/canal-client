package com.pateo.qingcloud.canal.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "elasticsearch")
@Data
public class ElasticsearchMonitorProperties {

    /**
     *  esAddress.
     */
    private String esAddress;

    /**
     * 连接超时时间
     */
    private int connectTimeOut;
    /**
     * 客户端从服务器读取数据的超时时间
     */
    private int socketTimeOut;
    /**
     * 获取连接的超时时间
     */
    private int connectionRequestTimeOut;
    /**
     * 最大连接数
     */
    private int maxConnectNum;
    /**
     * 最大路由连接数
     */
    private int maxConnectPerRoute;

    /**
     * keepAlive保活策略
     */
    private int keepAliveMinutes;

    /**
     * es用户名
     */
    private String username;

    /**
     * es密码
     */
    private String password;
}
