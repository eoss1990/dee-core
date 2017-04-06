package com.seeyon.v3x.dee.adapter.database.addition.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库操作工厂类，用于根据Connection或驱动名，获取数据库操作类
 *
 * @author zhangfb
 */
public class DatabaseOpFactory {
    private static Log log = LogFactory.getLog(DatabaseOpFactory.class);

    private static DatabaseOpFactory INSTANCE = new DatabaseOpFactory();

    /**
     * 默认数据库操作对象
     */
    private DatabaseOp defaultOp = new newDefaultOp();

    /**
     * 数据库操作对象Map
     */
    private Map<String, DatabaseOp> opMap = new HashMap<String, DatabaseOp>();

    private DatabaseOpFactory() {
        opMap.put("dm.jdbc.driver.DmDriver", new DmOp());
        opMap.put("com.mysql.jdbc.Driver", new newDefaultOp());
        opMap.put("net.sourceforge.jtds.jdbc.Driver", new newDefaultOp());
        opMap.put("oracle.jdbc.driver.OracleDriver", new newDefaultOp());
        opMap.put("com.ibm.db2.jcc.DB2Driver", new newDefaultOp());
    }

    public static DatabaseOpFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 根据数据库连接，获取数据库操作对象
     *
     * @param connection 数据库连接
     * @return 数据库操作对象
     */
    public DatabaseOp getByConnection(Connection connection) {
        if (connection == null) {
            return defaultOp;
        }

        String driverName = getDriverNameByConnection(connection);
        return getByDriverName(driverName);
    }

    /**
     * 根据驱动名，获取数据库操作对象
     *
     * @param driverName 驱动名
     * @return 数据库操作对象
     */
    public DatabaseOp getByDriverName(String driverName) {
        if (driverName != null) {
            DatabaseOp op = opMap.get(driverName);
            if (op != null) {
                return op;
            }
        }
        return defaultOp;
    }

    /**
     * 根据数据库连接，获取驱动名
     *
     * @param connection 数据库连接
     * @return 驱动名
     */
    private String getDriverNameByConnection(Connection connection) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            return metaData.getDriverName();
        } catch (SQLException e) {
            log.error("数据库操作异常：" + e.getLocalizedMessage(), e);
        }

        return null;
    }
}
