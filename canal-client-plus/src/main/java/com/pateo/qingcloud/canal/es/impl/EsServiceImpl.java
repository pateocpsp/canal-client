package com.pateo.qingcloud.canal.es.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.pateo.qingcloud.canal.es.EsService;
import com.pateo.qingcloud.canal.es.vo.EsAddReqVo;
import com.pateo.qingcloud.canal.es.vo.EsUpsertReqVo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class EsServiceImpl implements EsService {

    public EsServiceImpl(RestHighLevelClient restHighLevelClient){
        this.restHighLevelClient = restHighLevelClient;
    }

    private RestHighLevelClient restHighLevelClient;

    @Override
    public void add(EsAddReqVo reqVo) {
        IndexRequest indexRequest = new IndexRequest(reqVo.getIndex());
        // 规则，put /wf_index/_doc/1
        indexRequest.id(reqVo.getId());
        indexRequest.timeout(TimeValue.timeValueSeconds(5));
        // 如果不是父文档则需要指定routing
        if (!reqVo.isParentFlag()) {
            indexRequest.routing(reqVo.getRouting());
        }

        indexRequest.create(false);

        log.info("需要添加到es里面的数据为：{}-----{}", JSONObject.toJSONString(reqVo.getData(), SerializerFeature.WriteMapNullValue), indexRequest);
        //将我们数据放入请求,不过滤对象中的null值
        indexRequest.source(JSONObject.toJSONString(reqVo.getData(), SerializerFeature.WriteMapNullValue), XContentType.JSON);
        BytesReference bytesReference = indexRequest.source();
        try {
            //客户端发送请求
            IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            log.info("es添加数据成功***{}***", indexResponse.toString());
        } catch (IOException ioException) {
            log.error("！！！es新增数据发生异常！！！" + ioException.getMessage());
        } catch (Exception e) {
            log.error("！！！es新增数据发生未知异常！！！" + e.getMessage());
        }
    }

    @Override
    public void delete(String index, String id) {
        //参数为索引名，可以不指定，可以一个，可以多个
        DeleteByQueryRequest request = new DeleteByQueryRequest(index);
        // 更新时版本冲突
        request.setConflicts("proceed");
        // 设置查询条件，第一个参数是字段名，第二个参数是字段的值
        request.setQuery(new TermQueryBuilder("_id", id));
        // 更新最大文档数
        //request.setSize(10);
        request.setMaxDocs(10);
        // 最大重试次数
        request.setMaxRetries(10);
        // 批次大小
        request.setBatchSize(1000);
        // 并行
        request.setSlices(2);
        // 使用滚动参数来控制“搜索上下文”存活的时间
        request.setScroll(TimeValue.timeValueMinutes(10));
        // 超时
        request.setTimeout(TimeValue.timeValueMinutes(5));
        // 刷新索引
        request.setRefresh(true);
        try {
            log.info("开始删除es的数据--id={},index={}", id, index);
            //restHighLevelClient.deleteByQueryAsync();
            BulkByScrollResponse response = restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
            log.info("SUCCESS--es删除数据成功--id={}--{}", id, response.toString());
        } catch (IOException e) {
            log.error("！！！es删除数据发生异常！！！");
        }
    }

    @Override
    public void upsert(EsUpsertReqVo reqVo) {
        UpdateRequest updateRequest = new UpdateRequest(reqVo.getIndex(), reqVo.getId());
        log.info("需要修改的es数据为：{}", JSONObject.toJSONString(reqVo, SerializerFeature.WriteMapNullValue));
        updateRequest.doc(JSONObject.toJSONString(reqVo.getData(), SerializerFeature.WriteMapNullValue), XContentType.JSON);
        updateRequest.docAsUpsert(true);
        // 如果不是父文档则需要指定routing
        if (!reqVo.isParentFlag()) {
            updateRequest.routing(reqVo.getRouting());
        }
        updateRequest.retryOnConflict(20);
        //RLock lock = distributedLocker.lock(reqVo.getId());

        try {
            //尝试加锁，最多等待20秒，上锁以后20秒自动解锁
            //lock.lock(20, TimeUnit.SECONDS);
            //处理
            //updateRequest.upsert(JSONObject.toJSONString(reqVo.getData(), SerializerFeature.WriteMapNullValue), XContentType.JSON);
            UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            log.info("SUCCESS-----es修改数据成功***{}***{}", updateResponse.toString(), reqVo);
            //log.info("tryLock thread---{}, lock:{}", Thread.currentThread().getId(), lock);
        } catch (IOException ioException) {
            log.error("！！！es修改数据发生异常！！！{}----{}", ioException.getMessage(), reqVo);
        } catch (Exception e) {
            log.error("！！！异常！！！{}----{}", e.getMessage(), reqVo);
        } finally {
            //解锁
            //lock.unlock();
        }

    }

    @Override
    public boolean isExists(String index, String id) {

        boolean isExists;
        GetRequest getIndexRequest = new GetRequest(index, id);
        try {
            GetResponse getResponse = restHighLevelClient.get(getIndexRequest, RequestOptions.DEFAULT);
            isExists = getResponse.isExists();
        } catch (IOException ioException) {
            isExists = false;
            log.error("！！！判断es数据是否存在发生异常！！！" + ioException.getMessage());
        } catch (Exception e) {
            isExists = false;
            log.error("！！！判断es数据是否存在未知异常！！！" + e.getMessage());
        }
        return isExists;
    }

    @Override
    public <T> T get(String index, String id, Class<T> clazz) {

        GetRequest getIndexRequest = new GetRequest(index, id);
        try {
            GetResponse getResponse = restHighLevelClient.get(getIndexRequest, RequestOptions.DEFAULT);
            log.info("SUCCESS-----es数据查询成功：={}", JSONObject.toJSONString(getResponse));
            return JSONObject.parseObject(JSONObject.toJSONString(getResponse.getSource()), clazz);
        } catch (IOException ioException) {
            log.error("！！！查询es数据发生异常！！！" + ioException.getMessage());
            return null;
        } catch (Exception e) {
            log.error("！！！查询es数据发生未知异常！！！" + e.getMessage());
            return null;
        }
    }

    @Override
    public void updateByScript(String index, String id, String scriptStr, Map<String, Object> paramMap) {
        UpdateRequest updateRequest = new UpdateRequest(index, id);

        log.info("script-需要修改的es数据为：{}----{}", JSONObject.toJSONString(paramMap, SerializerFeature.WriteMapNullValue), scriptStr);
        Script script = new Script(ScriptType.INLINE, "painless", scriptStr, paramMap);
        updateRequest.script(script);
        updateRequest.retryOnConflict(20);
        //RLock lock = distributedLocker.lock(id);
        updateRequest.upsert();
        updateRequest.scriptedUpsert(true);
        //尝试加锁，最多等待10秒，上锁以后10秒自动解锁
        //lock.lock(20, TimeUnit.SECONDS);
        try {
            //处理
            UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            log.info("SUCCESS-----script-es修改数据成功***{}***{}", updateResponse.toString(), JSONObject.toJSONString(paramMap));
            //log.info("tryLock thread---{}, lock:{}", Thread.currentThread().getId(), lock);
        } catch (IOException ioException) {
            log.error("！！！script-es修改数据发生异常！！！{}，脚本为={}，数据为={}", ioException.getMessage(), scriptStr, JSONObject.toJSONString(paramMap));
        } catch (Exception e) {
            log.error("！！！异常！！！{}，脚本为={}，数据为={}", e.getMessage(), scriptStr, JSONObject.toJSONString(paramMap));
        } finally {
            //解锁
            //lock.unlock();
        }

    }
}
