package com.pateo.qingcloud.canal.es;


import com.pateo.qingcloud.canal.es.vo.EsAddReqVo;
import com.pateo.qingcloud.canal.es.vo.EsUpsertReqVo;

import java.util.Map;

public interface EsService {


    /**
     * 添加es数据
     * @param reqVo
     */
    void add(EsAddReqVo reqVo);

    /**
     * 删除es数据
     * @param index
     * @param id
     */
    void delete(String index, String id);


    /**
     * upsert
     * @param reqVo
     */
     void upsert(EsUpsertReqVo reqVo);

    /**
     * 判断es数据是否存在
     * @param index
     * @param id
     * @return
     */
    boolean isExists(String index, String id);

    /**
     * 获取es详情
     * @param index
     * @param id
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T get(String index,String id, Class<T> clazz);

    /**
     * 根据script来更新文档
     * @param scriptStr
     * @param paramMap
     */
    void updateByScript(String index, String id, String scriptStr, Map<String, Object> paramMap);
}
