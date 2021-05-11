package com.pateo.qingcloud.canal.utils;

import com.alibaba.fastjson.JSONObject;
import com.pateo.qingcloud.canal.constant.CommonConstants;
import com.pateo.qingcloud.canal.properties.es.EsSync;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EsFieldUtils {

    public static String[] PARSE_PATTERNS = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * 自定义字段类型数据个还是转换
     *
     * @param fieldType
     * @param columnValue
     * @return
     */
    public static Object convertType(String fieldType, String columnValue) {
        SimpleDateFormat df = new SimpleDateFormat(CommonConstants.DATA_FORMAT);

        if (StringUtils.isEmpty(columnValue)) {
            return columnValue;
        }

        if (fieldType.equals(CommonConstants.CONVERT_TYPE.INT)) {
            return Integer.parseInt(columnValue);
        }

        if (fieldType.equals(CommonConstants.CONVERT_TYPE.DATE)) {
            return df.format(parseDate(columnValue));
        }

        if (fieldType.equals(CommonConstants.CONVERT_TYPE.ARRAY)) {
            return JSONObject.parseArray(columnValue);
        }

        if (fieldType.equals(CommonConstants.CONVERT_TYPE.DECIMAL)) {
            return new BigDecimal(columnValue);
        }

        return columnValue;
    }


    public static Date parseDate(String str) {
        if (str == null) {
            return null;
        }
        try {
            return org.apache.commons.lang.time.DateUtils.parseDate(str, PARSE_PATTERNS);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 验证同步事件insert,update,delete,all,不配置默认为all
     * @param event
     * @return
     */
    public static boolean checkEvent(String event, EsSync esSync) {



        if (CollectionUtils.isEmpty(esSync.getEvent())) {
            return true;
        }

        if (esSync.getEvent().contains(CommonConstants.SYNC_EVENT.ALL)) {
            return true;
        }

        if (esSync.getEvent().contains(event)) {
            return true;
        }

        return false;
    }
}
