package com.pateo.qingcloud.canal.properties.es;

import lombok.Data;

@Data
public class Consumer {

    private String topic;

    private String groupId;
}
