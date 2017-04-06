package com.seeyon.v3x.dee.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangfb
 */
public class JdbcUtils {
    public static final Log log = LogFactory.getLog(JdbcUtils.class);

    public static void close(Connection x) {
        if (x == null) {
            return;
        }

        try {
            x.close();
        } catch (Exception e) {
            log.debug("close connection error", e);
        }
    }

    public static void close(ResultSet x) {
        if (x == null) {
            return;
        }

        try {
            x.close();
        } catch (Exception e) {
            log.debug("close error", e);
        }
    }

    public static void close(Statement x) {
        if (x == null) {
            return;
        }

        try {
            x.close();
        } catch (Exception e) {
            log.debug("close error", e);
        }
    }

    public static void close(Closeable x) {
        if (x == null) {
            return;
        }

        try {
            x.close();
        } catch (Exception e) {
            log.debug("close error", e);
        }
    }

    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        close(rs);
        close(stmt);
        close(conn);
    }

    public static void close(Connection conn, Statement stmt) {
        close(stmt);
        close(conn);
    }

    public static void close(Statement stmt, ResultSet rs) {
        close(rs);
        close(stmt);
    }

    public static void execute(Connection conn, String sql, List<Object> parameters) throws SQLException {
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            setParameters(stmt, parameters);

            stmt.executeUpdate();
        } finally {
            close(stmt);
        }
    }

    public static int executeUpdate(Connection conn, String sql, List<Object> parameters) throws SQLException {
        PreparedStatement stmt = null;

        int updateCount;
        try {
            stmt = conn.prepareStatement(sql);

            setParameters(stmt, parameters);

            updateCount = stmt.executeUpdate();
        } finally {
            close(stmt);
        }

        return updateCount;
    }

    public static List<Map<String, Object>> executeQuery(DataSource dataSource, String sql, Object... parameters)
            throws SQLException {
        return executeQuery(dataSource, sql, Arrays.asList(parameters));
    }

    public static List<Map<String, Object>> executeQuery(DataSource dataSource, String sql, List<Object> parameters)
            throws SQLException {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            return executeQuery(conn, sql, parameters);
        } finally {
            close(conn);
        }
    }

    public static List<Map<String, Object>> executeQuery(Connection conn, String sql, List<Object> parameters)
            throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(sql);

            setParameters(stmt, parameters);

            rs = stmt.executeQuery();

            ResultSetMetaData rsMeta = rs.getMetaData();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<String, Object>();

                for (int i = 0, size = rsMeta.getColumnCount(); i < size; ++i) {
                    String columName = rsMeta.getColumnLabel(i + 1);
                    Object value = rs.getObject(i + 1);
                    row.put(columName, value);
                }

                rows.add(row);
            }
        } finally {
            close(rs);
            close(stmt);
        }

        return rows;
    }

    public static String getJdbcType(Connection connection) throws SQLException {
        String driverName = connection.getMetaData().getDriverName();
        if (JdbcConstants.DB2_DRIVER.equals(driverName)) {
            return JdbcConstants.DB2;
        } else if (JdbcConstants.MYSQL_DRIVER.equals(driverName)) {
            return JdbcConstants.MYSQL;
        } else if (JdbcConstants.ORACLE_DRIVER.equals(driverName)) {
            return JdbcConstants.ORACLE;
        } else if (JdbcConstants.SQL_SERVER_DRIVER.equals(driverName)) {
            return JdbcConstants.SQL_SERVER;
        } else if (JdbcConstants.POSTGRESQL_DRIVER.equals(driverName)) {
            return JdbcConstants.POSTGRESQL;
        }
        return null;
    }

    private static void setParameters(PreparedStatement stmt, List<Object> parameters) throws SQLException {
        if (parameters == null) {
            return;
        }

        for (int i = 0, size = parameters.size(); i < size; ++i) {
            Object param = parameters.get(i);
            stmt.setObject(i + 1, param);
        }
    }
    /**
     * 是否含有sequence
     *
     * @param value 值
     * @return true：包含，false：不包含
     */
    public static boolean isSeq(String value) {
        //并排含有适配器和脚本的判断
        return value != null
                && !value.contains("<adapter ") && !value.contains("<script ")
                && (value.contains(".nextval") || value.contains(".currval"));
    }
}
