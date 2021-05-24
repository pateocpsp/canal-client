package com.pateo.qingcloud.canal.handler;

import com.pateo.qingcloud.canal.annotation.CanalTable;
import com.pateo.qingcloud.canal.constant.CommonConstants;
import com.pateo.qingcloud.canal.context.CanalContext;
import com.pateo.qingcloud.canal.dao.CommonDao;
import com.pateo.qingcloud.canal.es.EsService;
import com.pateo.qingcloud.canal.es.impl.EsServiceImpl;
import com.pateo.qingcloud.canal.es.vo.EsUpsertReqVo;
import com.pateo.qingcloud.canal.factory.DataSourceFactory;
import com.pateo.qingcloud.canal.factory.EsFactory;
import com.pateo.qingcloud.canal.model.CanalModel;
import com.pateo.qingcloud.canal.properties.CanalClientPlusInfo;
import com.pateo.qingcloud.canal.properties.CanalClientPlusInfoMap;
import com.pateo.qingcloud.canal.properties.es.EsSync;
import com.pateo.qingcloud.canal.properties.es.ParentChildren;
import com.pateo.qingcloud.canal.properties.rdb.SourceInfo;
import com.pateo.qingcloud.canal.properties.rdb.TableInfo;
import com.pateo.qingcloud.canal.utils.CommonUtils;
import com.pateo.qingcloud.canal.utils.EsFieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import javax.sql.DataSource;
import java.util.*;

/**
 * @author gujiachun
 */
@CanalTable(value = "all")
public class AllEntryHandler implements EntryHandler<Map<String, String>> {

    private final Logger logger = LoggerFactory.getLogger(AllEntryHandler.class);

    /**
     * 执行脚本
     * */
    private final static String ADD_SCRIPT = "if (ctx.op == 'create'){ctx._source=params.data} else {if (ctx._source.%s == null) {ctx._source.%s = [params.%s]} else {ctx._source.%s.add(params.%s)}}";
    private final static String REMOVE_SCRIPT = "ctx._source.%s.remove(ctx._source.%s.indexOf(params.%s))";

    public AllEntryHandler(CanalClientPlusInfoMap canalClientPlusInfoMap,
                           CommonDao commonDao,
                           DataSourceFactory dataSourceFactory,
                           EsFactory esFactory){

        this.dataSourceFactory = dataSourceFactory;
        this.commonDao = commonDao;
        this.esFactory = esFactory;
        String projectKey = canalClientPlusInfoMap.getCanalClientPlusInfoMap().keySet().iterator().next();
        this.projectKey = projectKey;
        this.canalClientPlusInfo = canalClientPlusInfoMap.getCanalClientPlusInfoMap().get(projectKey);
    }

    private CanalClientPlusInfo canalClientPlusInfo;

    private CanalClientPlusInfoMap canalClientPlusInfoMap;

    private DataSourceFactory dataSourceFactory;

    private EsFactory esFactory;

    private CommonDao commonDao;

    private String projectKey;

    @Override
    public void insert(Map<String, String> map) {
        logger.info("增加 {}", map);
        updateEs(CommonConstants.SYNC_EVENT.INSERT, map);
    }

    @Override
    public void update(Map<String, String> before, Map<String, String> after) {
        logger.info("修改 before {}", before);
        logger.info("修改 after {}", after);
        updateEs(CommonConstants.SYNC_EVENT.UPDATE, after);
        rdb(after);
    }

    @Override
    public void delete(Map<String, String> map) {
        logger.info("删除 {}", map);
        esDataDelete(map);
    }


    /**
     * 正常es同步数据流程
     *
     * @param map
     * @return
     */
    private void updateEs(String event, Map<String, String> map) {
        EsService esService;
        Map<String, Object> esJsonMap = new HashMap<>(500);
        CanalModel canal = CanalContext.getModel();
        String dataBase = canal.getDatabase();
        String table = canal.getTable();
        logger.info("{}库{}表开始同步es···upsert", dataBase, table);
        if (canalClientPlusInfo == null || canalClientPlusInfo.getEsSync() == null) {
            logger.info("暂无配置不走同步逻辑");
            return;
        }
        List<EsSync> esSyncList = canalClientPlusInfo.getEsSync().get(dataBase.concat("-").concat(table));
        if (CollectionUtils.isEmpty(esSyncList)) {
            logger.info("暂无配置不走es同步逻辑");
            return;
        }

        for (EsSync esSync: esSyncList) {
            esService = new EsServiceImpl(esFactory.getRestHighLevelClient(projectKey.concat(esSync.getTargetEs())));

            // 判断配置哪种同步事件
            if (!EsFieldUtils.checkEvent(event, esSync)) {
                return;
            }

            EsUpsertReqVo<Map<String, Object>> reqVo = new EsUpsertReqVo<>();

            //////////索引和主键生成//////////////////
            String esId = map.get(esSync.getId());
            String index = esSync.getIndex();
            if (esSync.getIdPrefix() != null) {
                esId = esSync.getIdPrefix().concat(esId);
            }
            if (!StringUtils.isEmpty(map.get(esSync.getIndexDynamicFieldSuffix()))) {
                index = index.concat(map.get(esSync.getIndexDynamicFieldSuffix()));
            }
            //////////索引和主键生成//////////////////

            // 1.同步mysql中指定字段到es里面，2.特殊字段类型转换
            coverJsonMap(esSync, map, esJsonMap);

            // 判断是否配置aviator表达式,并且是否触发，如果false则不往下走逻辑
            Boolean isAviator = CommonUtils.checkAviator(esSync, esJsonMap);
            logger.info("aviator表达式得出结果为,{}", isAviator);
            if (!isAviator){
                logger.info("aviator表达式不通过，不走同步逻辑");
                continue;
            }

            // 如果配置了updateArrayFields则不走下面逻辑
            if (!CollectionUtils.isEmpty(esSync.getUpdateArrayFields())) {
                // 走同步单字段数组同步逻辑
                for (Map<String, Object> d : esSync.getUpdateArrayFields()) {
                    String key = d.keySet().iterator().next();//es数组字段
                    Object mapValue = d.get(key); // 数据库字段
                    coverAdd(esId, index, key, esJsonMap.get(mapValue), esService);
                }
                return;
            }

            // 是否配置了父子表关系
            if (esSync.getRelations() != null) {
                Map<String, Object> parentCustomerJoinInfoMap = new HashMap<>(500);
                String joinMapKey = esSync.getRelations().keySet().iterator().next();
                ParentChildren parentChildren = esSync.getRelations().get(joinMapKey);

                if (parentChildren != null && parentChildren.getName() != null) {
                    parentCustomerJoinInfoMap.put("name", parentChildren.getName());
                    // 判断是父文档还是子文档
                    // 如果是父文档则需要关联父文档
                    if (parentChildren.getParent() != null) {
                        parentCustomerJoinInfoMap.put("parent", map.get(parentChildren.getParent()));
                        reqVo.setParentFlag(false);
                        reqVo.setRouting(map.get(parentChildren.getParent()));
                    }
                    esJsonMap.put(joinMapKey, parentCustomerJoinInfoMap);
                }
            }


            // 判断是否给数据库字段取了别名
            Map<String, Object> esJsonMapReplaceCopy = new HashMap<>();
            if (!CollectionUtils.isEmpty(esSync.getEsFields())) {
                for(Map.Entry<String, Object> entry : esJsonMap.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (esSync.getEsFields().get(key) != null) {
                        String aliasField = esSync.getEsFields().get(key).toString();
                        esJsonMapReplaceCopy.put(aliasField, value);
                    }
                }
                if (!CollectionUtils.isEmpty(esJsonMapReplaceCopy)) {
                    for(Map.Entry<String, Object> entry : esJsonMapReplaceCopy.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        esJsonMap.put(key, value);
                    }
                }
            }


            // 判断是否有需要过滤的字段
            if (!CollectionUtils.isEmpty(esSync.getSkips())) {
                Iterator<String> skipsIterator = esJsonMap.keySet().iterator();
                while (skipsIterator.hasNext()) {
                    String key = skipsIterator.next();
                    if (esSync.getSkips().contains(key)) {
                        skipsIterator.remove();
                    }
                }
            }
            reqVo.setData(esJsonMap);
            reqVo.setId(esId);
            reqVo.setIndex(index);
            esService.upsert(reqVo);
        }


    }

    /**
     * es数据删除
     * event es同步的事件，insert,update,delete,all,不配置默认为all
     * @param map
     */
    public void esDataDelete(Map<String, String> map) {
        EsService esService;
        CanalModel canal = CanalContext.getModel();
        String dataBase = canal.getDatabase();
        String table = canal.getTable();
        logger.info("{}库{}表开始同步es···delete", dataBase, table);
        if (canalClientPlusInfo.getEsSync() == null) {
            logger.info("暂无配置不走同步逻辑");
            return;
        }
        List<EsSync> esSyncList = canalClientPlusInfo.getEsSync().get(dataBase.concat("-").concat(table));
        if (esSyncList == null) {
            logger.info("暂无配置不走同步逻辑");
            return;
        }
        for (EsSync esSync: esSyncList) {
            esService = new EsServiceImpl(esFactory.getRestHighLevelClient(projectKey.concat(esSync.getTargetEs())));
            // 判断配置哪种同步事件
            if (!EsFieldUtils.checkEvent(CommonConstants.SYNC_EVENT.DELETE, esSync)) {
                return;
            }

            Map<String, Object> esJsonMap = new HashMap<>(500);

            //////////索引和主键生成//////////////////
            String id = map.get(esSync.getId());
            String index = esSync.getIndex();
            if (!StringUtils.isEmpty(esSync.getIdPrefix())) {
                id = esSync.getIdPrefix().concat(id);
            }
            if (!StringUtils.isEmpty(map.get(esSync.getIndexDynamicFieldSuffix()))) {
                index = esSync.getIndex().concat(map.get(esSync.getIndexDynamicFieldSuffix()));
            }
            //////////索引和主键生成//////////////////

            // 1.同步mysql中指定字段到es里面，2.特殊字段类型转换
            coverJsonMap(esSync, map, esJsonMap);

            // 判断是否配置aviator表达式,并且是否触发，如果false则不往下走逻辑
            if (!CommonUtils.checkAviator(esSync, esJsonMap)){
                return;
            }

            // 如果配置了updateArrayFields则不走下面逻辑
            if (!CollectionUtils.isEmpty(esSync.getUpdateArrayFields())) {
                // 走同步单字段数组删除同步逻辑
                for (Map<String, Object> d : esSync.getUpdateArrayFields()) {
                    String key = d.keySet().iterator().next();//es数组字段
                    Object mapValue = d.get(key); // 数据库字段
                    coverRemove(id, index, key, esJsonMap.get(mapValue), esService);
                }
                return;
            }
            // 走正常删除逻辑
            esService.delete(index, id);
        }

    }

    /**
     * 冗余同步逻辑
     */
    private void rdb(Map<String, String> map) {

        if (canalClientPlusInfo == null || canalClientPlusInfo.getRdb() == null) {
            logger.info("无rdb配置，不走冗余同步逻辑");
            return;
        }

        CanalModel canal = CanalContext.getModel();
        String dataBase = canal.getDatabase();
        String table = canal.getTable();
        String business = dataBase.concat("-").concat(table);
        SourceInfo sourceProperties = canalClientPlusInfo.getRdb().get(business);
        if (sourceProperties == null) {
            logger.info("无rdb配置，不走冗余同步逻辑");
            return;
        }
        logger.info("{}库{}表开始同步rdb···", dataBase, table);
        // 此map是需要冗余同步的字段 key是主键id value是id对应的值
        Map<String, Object> sourceColumn = sourceProperties.getSource().getSourceColumn();
        String sourceColumnKey = sourceColumn.keySet().iterator().next();
        Object sourceColumnValue = sourceColumn.get(sourceColumnKey);

        String dbSourceIdValue = map.get(sourceColumnKey);
        String dbSourceValue = map.get(sourceColumnValue);

        if (!CollectionUtils.isEmpty(sourceProperties.getTargets())) {
            for (TableInfo d : sourceProperties.getTargets()) {
                String datasource = d.getDatasource();
                // 获取数据库连接
                DataSource dataSource = dataSourceFactory.getDataSource(projectKey.concat(datasource));
                String targetTable = d.getTable();
                String targetIdField = d.getTargetColumn().keySet().iterator().next();
                String targetValueField = d.getTargetColumn().get(targetIdField).toString();
                commonDao.update(dataSource, targetTable, targetIdField, dbSourceIdValue, targetValueField, dbSourceValue);
            }
        }
    }

    /**
     * 删除es数据中数组数据
     *
     * @param id
     * @param esIndex
     * @param esField
     * @param dbFieldInfo
     */
    private void coverRemove(String id, String esIndex, String esField, Object dbFieldInfo, EsService esService) {
        String valueScript = String.format(REMOVE_SCRIPT, esField, esField, esField);
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put(esField, dbFieldInfo);
        esService.updateByScript(esIndex, id, valueScript, valueMap);
    }

    /**
     * 往es数据中的数组加数据
     *
     * @param id
     * @param esIndex
     * @param esField
     * @param dbFieldInfo
     */
    private void coverAdd(String id, String esIndex, String esField, Object dbFieldInfo, EsService esService) {
        String script = String.format(ADD_SCRIPT, esField, esField, esField, esField, esField);
        Map<String, Object> valueMap = new HashMap<>();

        ///////////////////初始化的数据/////////////////
        Map<String, Object> data = new HashMap<>();
        List<Object> dataList = new ArrayList<>(500);
        dataList.add(dbFieldInfo);
        data.put(esField, dataList);
        ///////////////////初始化的数据/////////////////

        valueMap.put("data", data);
        valueMap.put(esField, dbFieldInfo);

        esService.updateByScript(esIndex, id, script, valueMap);
    }

    /**
     * 生成要插入es的数据
     * @param esSync
     * @param map
     * @param esJsonMap
     */
    private void coverJsonMap(EsSync esSync, Map<String, String> map, Map<String, Object> esJsonMap) {
        // 判断是否配置特殊类型转换，如果配置了则需要特殊字段类型转换
        esSync.getDbFields().forEach(d -> {
            // 判断是否配置特殊类型转换，如果配置了则需要特殊字段类型转换
            if (esSync.getEsFieldTypes() != null && esSync.getEsFieldTypes().size() > 0 && esSync.getEsFieldTypes().get(d) != null) {
                String fieldType = esSync.getEsFieldTypes().get(d).toString();
                esJsonMap.put(d, EsFieldUtils.convertType(fieldType, map.get(d)));
            } else {
                esJsonMap.put(d, map.get(d));
            }
        });
    }

}
