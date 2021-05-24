package com.pateo.qingcloud.canal.properties;

import lombok.Data;

import java.util.Map;

@Data
public class CanalClientPlusInfoMap {

    public CanalClientPlusInfoMap (Map<String, CanalClientPlusInfo> canalClientPlusInfoMap) {
        this.canalClientPlusInfoMap = canalClientPlusInfoMap;
    }

    Map<String, CanalClientPlusInfo> canalClientPlusInfoMap;
}
