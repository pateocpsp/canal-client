package com.pateo.qingcloud.canal.dao.impl;

import com.pateo.qingcloud.canal.dao.CommonDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.PreparedStatement;

public class CommonDaoImpl implements CommonDao {

    private Logger logger = LoggerFactory.getLogger(CommonDaoImpl.class);

    @Override
    public void update(DataSource dataSource, String table, String idFieldName, Object idFieldValue, String nameFieldName, Object nameFieldValue) {
        PreparedStatement psTmt = null;
        try {
            String sql = "update "+table+" set "+nameFieldName+" = ? where "+idFieldName+" = ?";
            logger.info("SQL ：{}", sql);
            logger.info("param,nameFieldValue={},idFieldValue={}", nameFieldValue, idFieldValue);
            psTmt = dataSource.getConnection().prepareStatement(sql);
            psTmt.setObject(1, nameFieldValue);
            psTmt.setObject(2, idFieldValue);
            int count = psTmt.executeUpdate();
            logger.info("SUCCESS---{}冗余同步成功，count={}", table, count);

        }catch (Exception e) {
            logger.error("!!!冗余同步异常!!!" + e.getMessage());
        }
    }
}
