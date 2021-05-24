package com.pateo.qingcloud.canal.dao.impl;

import com.pateo.qingcloud.canal.dao.CommonDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class CommonDaoImpl implements CommonDao {

    private Logger logger = LoggerFactory.getLogger(CommonDaoImpl.class);

    @Override
    public void update(DataSource dataSource, String table, String idFieldName, Object idFieldValue, String nameFieldName, Object nameFieldValue) {
        Connection connection = null;
        PreparedStatement psTmt = null;
        try {
            connection = dataSource.getConnection();

            String sql = "update "+table+" set "+nameFieldName+" = ? where "+idFieldName+" = ?";
            logger.info("SQL ：{}", sql);
            logger.info("param,nameFieldValue={},idFieldValue={}", nameFieldValue, idFieldValue);
            psTmt = connection.prepareStatement(sql);
            psTmt.setObject(1, nameFieldValue);
            psTmt.setObject(2, idFieldValue);
            int count = psTmt.executeUpdate();
            logger.info("SUCCESS---{}冗余同步成功，count={}", table, count);

        }catch (Exception e) {
            logger.error("!!!冗余同步异常!!!" + e.getMessage());
        } finally {
            try {
                if (psTmt!= null && !psTmt.isClosed()) {
                    psTmt.close();
                    logger.info("psTmt已正常关闭！");
                }
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    logger.info("connection已正常关闭！");
                }
            } catch (Exception e) {
                logger.error("!!!连接池关闭发生异常!!!" + e.getMessage());
            }
        }
    }
}
