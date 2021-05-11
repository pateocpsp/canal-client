package com.pateo.qingcloud.canal.dao;

import javax.sql.DataSource;

public interface CommonDao {

    void update(DataSource dataSource, String table, String idFieldName, Object idFieldValue, String nameFieldName, Object nameFieldValue);
}
