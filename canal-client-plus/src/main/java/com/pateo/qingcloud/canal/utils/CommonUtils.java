package com.pateo.qingcloud.canal.utils;

import com.googlecode.aviator.AviatorEvaluator;
import com.pateo.qingcloud.canal.constant.CommonConstants;
import com.pateo.qingcloud.canal.properties.CanalClientPlusInfo;
import com.pateo.qingcloud.canal.properties.es.EsSync;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.util.*;

@Slf4j
public class CommonUtils {


    private static String toHupm(String foo) {

        String[] arr;
        String newData = null;
        //以'-'字符为切割点进行切割
        arr = foo.split("_");
        System.out.println(Arrays.toString(arr));
        for (int i = 0; i < arr.length; i++) {
            //首个切割片段首字母小写 + 首个切割片段剩余其他字符
            if (i == 0) {
                arr[i] = String.valueOf(arr[i].charAt(0)).toLowerCase() + arr[i].substring(1);
                newData = arr[i];
            }
            //非首个切割片段首字母大写 + 切割片段剩余其他字符
            else {
                arr[i] = String.valueOf(arr[i].charAt(0)).toUpperCase() + arr[i].substring(1);
                newData = newData + arr[i];
            }
        }
        return newData;
    }

    /**
     * 判断是否配置Aviator表达式
     *
     * @param esSync
     * @return
     */
    public static boolean checkAviator(EsSync esSync, Map<String, Object> map) {
        if (esSync.getSyncRule() == null) {
            return true;
        }
        Map<String, Object> env = new HashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            env.put(key, value);
        }

        log.info("aviator表达式得出结果为：{}，env：{}", esSync.getSyncRule(), env);
        Boolean result = (Boolean) AviatorEvaluator.execute(esSync.getSyncRule(), env);

        if (result) {
            return true;
        }
        return false;
    }


    /**
     * a,b,c转换成[a,b,c]数组
     *
     * @param mapStr
     * @return
     */
    public static List<String> stringToList(String mapStr) {

        List<String> list = new ArrayList<>(50);

        if (!StringUtils.isEmpty(mapStr)) {
            list.addAll(Arrays.asList(mapStr.split(",")));
        }

        return list;
    }


    public static void stringToListCover(CanalClientPlusInfo canalClientPlusInfo) {

        for (Map.Entry<String, List<EsSync>> entry : canalClientPlusInfo.getEsSync().entrySet()) {
            List<EsSync> values = entry.getValue();
            for (EsSync value : values) {
                if (!CollectionUtils.isEmpty(value.getDbFields())) {
                    value.setDbFields(stringToList(value.getDbFields().get(0)));
                }

                if (!CollectionUtils.isEmpty(value.getEvent())) {
                    value.setEvent(stringToList(value.getEvent().get(0)));
                }
            }
        }
    }

}
