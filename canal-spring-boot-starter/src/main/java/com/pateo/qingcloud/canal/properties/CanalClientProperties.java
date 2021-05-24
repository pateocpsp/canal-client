package com.pateo.qingcloud.canal.properties;

import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author gujiachun
 */
@ConfigurationProperties(prefix = "pateo.canal")
@Data
public class CanalClientProperties {

    public static final String CANAL_MODE = "pateo.canal.mode";

    public static final String CANAL_ASYNC = "pateo.canal.async";

    private List<Instance> instance;

    private String zkServers;

    private String mode;

    private boolean async;

    private String kafka;

    private String zkNode;

    private String filter = StringUtils.EMPTY;

    private Integer batchSize = 1;

    private Long timeout = 1L;

    private TimeUnit unit = TimeUnit.SECONDS;

}
