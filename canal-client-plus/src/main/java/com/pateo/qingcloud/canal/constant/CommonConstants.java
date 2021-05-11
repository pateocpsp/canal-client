package com.pateo.qingcloud.canal.constant;

public class CommonConstants {

    public final static String ES_INDEX_PREFIX = "customer_info_index";

    public final static String CUSTOMER_FIELD_PREFIX = "customer_field_";

    public final static String  CUSTOMER_JOIN_INFO_CHILD = "customer_info_field_value_detail";

    public final static String  CUSTOMER_JOIN_INFO_PARENT = "customer_info";

    public final static String DATA_FORMAT = "YYYY-MM-dd'T'HH:mm:ss.SSSZ";

    // 字段自定义类型枚举
    public final static class CONVERT_TYPE{

        public final static String INT = "int";

        public final static String DATE = "date";

        public final static String STRING = "string";

        public final static String ARRAY = "array";

        public final static String JSON = "json";

        public final static String DECIMAL = "decimal";

    }

    //同步事件枚举
    public final static class SYNC_EVENT{

        public final static String INSERT = "insert";

        public final static String UPDATE = "update";

        public final static String DELETE= "delete";

        public final static String ALL = "all";
    }
}
