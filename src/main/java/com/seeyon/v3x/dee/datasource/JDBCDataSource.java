package com.seeyon.v3x.dee.datasource;

import com.seeyon.v3x.dee.resource.DbDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBCDataSource implements DbDataSource {
	private static Log log = LogFactory.getLog(JDBCDataSource.class);
	private String id;

	private String driver;
	private String url;
	private String userName;
	private String password;
	//连接池信息
	private DeePooledDataSource dpds;
	public JDBCDataSource() {
	}

	public JDBCDataSource(String driver, String url, String userName,
			String password) {
		this.driver = driver;
		this.url = url;
		this.userName = userName;
		this.password = password;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public DeePooledDataSource getDpds() {
		return dpds;
	}

	public void setDpds(DeePooledDataSource dpds) {
		this.dpds = dpds;
	}

	/**
	 * 
	 * @param sql
	 * @return
	 * @throws java.sql.SQLException
	 *
	 */
	private ResultSet executeQuery(String sql) throws Exception {
		Connection connection = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			log.debug("executeQuery:" + sql);
			return rs;
		} catch (SQLException e) {
			log.error("JDBCDataSource ExecuteQuery:" + e.getMessage(), e);
			throw e;
		}
	}

	public void executeBatch(List<String> sqlList) throws Exception {
		Connection connection = getConnection();
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			for (String sql : sqlList) {
				stmt.addBatch(sql);
				log.debug(this.getClass().getName() + ": " + sql);
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			log.error("JDBCDataSource executeBatch Error:" + e.getMessage(), e);
			throw e;
		} finally {
			release(connection, stmt, null);
		}
	}

    /**
     * execute sql within parameters
     *
     * @param sql sql
     * @param params parameters
     * @throws java.sql.SQLException
     */
    public void execute(String sql, String[] params) throws Exception {
        Connection connection = getConnection();
        PreparedStatement pstm = null;
        try {
            pstm = connection.prepareStatement(sql);
            for (int i=0; i<params.length; i++) {
                pstm.setString(i+1, params[i]);
            }
            pstm.execute();
            connection.commit();
        } catch (SQLException e) {
            log.error("JDBCDataSource execute Error:" + e.getMessage(), e);
            throw e;
        } finally {
            release(connection, pstm, null);
        }
    }

	/**
	 * 初始化数据库，去掉异常信息
	 *
	 * @return 执行sql脚本，不抛出异常
	 */
	public void initDbBatch(List<String> sqlList) throws Exception {
		Connection connection = getConnection();
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			for (String sql : sqlList) {
				if (this.isExistsData(stmt, sql))
					continue;
				stmt.addBatch(sql);
				log.debug(this.getClass().getName() + ": " + sql);
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			log.error("JDBCDataSource initDbBatch Error:" + e.getMessage(), e);
		} finally {
			release(connection, stmt, null);
		}
	}

	/**
	 * 取得数据库连接。
	 *
	 * @return 连接
	 * @throws Exception
	 */
	@Override
	public Connection getConnection() throws Exception {
		try {
			return DBConManager.getInstance().getJdbcDs(driver,url,userName,password,dpds).getConnection();
		} catch (SQLException e) {
			log.error("dee_error:driver="+driver+"|url="+url+"|user="+userName);
			log.error("JDBC getConnection Exception : " + e.getMessage(), e);
			throw e;
		}

	}

	/**
	 * 关闭所有连接，释放资源。
	 */
	public void close(Connection connection) {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			log.error("JDBC DataSource Close Connection Exception : " + e.getMessage(), e);
		}

	}

	public void close() {
		try {
			DBConManager.getInstance().removeDs(driver,url,userName,password);
		} catch (Exception e) {
			log.error("Close JDBC DataSource Exception : " + e.getMessage(), e);
		}
	}

	private void release(Connection connection, Statement stmt, ResultSet rs) {
		try {
			if (null != rs) {
				rs.close();
			}
			if (null != stmt) {
				stmt.close();
			}
			if (null != connection) {
				connection.close();
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 初始化脚本时，对SQL语句进行判断，防止每次启动日志报错<br>
	 * 原则： <br>
	 * DDL类型语句忽略，可以通过脚本中加入If not exists语句排除掉 <br>
	 * DML类型语句处理，先查询，是否含有内容再判断是否插入，暂时处理INSERT
	 *
	 * @param sql
	 * @date 2012-05-21
	 * @return true=已存在无需插入,false=不存在执行插入语句
	 */
	private Boolean isExistsData(Statement stmt, String sql) {
		Boolean b = false;
		if (StringUtils.isNotBlank(sql.trim())) {
			if (sql.contains("INSERT")) {
				Map<String, String> elements = splitSQLElement(sql);
				ResultSet rs = null;
				try {
					String _sql = "SELECT 1 FROM " + elements.get("table")
							+ " WHERE " + elements.get("key") + " = "
							+ elements.get("value");
					rs = stmt.executeQuery(_sql);
					if (rs.next())
						b = true;
				} catch (SQLException e) {
					// catch这个异常但忽略它，第一次初始化执行时，采用excuteBatch执行还没有表和数据会抛异常
				}//此处不需要关闭连接，外部调用的方法已经关闭了
			}
		}
		return b;
	}

	/**
	 * 仅用于校验初始化前是否已经有数据的方法 <br>
	 * 将sql语句拆成3个元素，1=表名，2=where的条件，3=条件等于的值
	 */
	private Map<String, String> splitSQLElement(String sql) {
		Map<String, String> elements = new HashMap<String, String>();
		sql.substring(sql.indexOf("INTO"), sql.indexOf("("));
		String[] tmp = sql.split("VALUES");
		elements.put(
				"table",
				tmp[0].substring(tmp[0].indexOf("INTO") + 4,
						tmp[0].indexOf("(")));
		elements.put("key",
				tmp[0].substring(tmp[0].indexOf("(") + 1, tmp[0].indexOf(",")));
		elements.put("value",
				tmp[1].substring(tmp[1].indexOf("(") + 1, tmp[1].indexOf(",")));
		return elements;
	}
	/**
	 * 执行指定的SQL语句，并返回结果集供回调方法调用进行处理。
	 * @param sql sql语句
	 * @param callback 回调方法
	 * @return 对结果集进行处理后的结果，亦即回调方法的返回值
	 * @throws java.sql.SQLException
	 */
	public <T> T executeQuery(String sql,ResultSetCallback<T> callback) throws Exception {
		Connection connection = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			log.debug("executeQuery:" + sql);
			return callback.execute(rs);
		} catch (SQLException e) {
			log.error("JDBCDataSource ExecuteQuery:" + e.getMessage(), e);
			throw e;
		} finally {
			release(connection,stmt,rs);
		}
	}
	/**
	 * JDBC结果集回调接口。
	 * @author wangwenyou
	 *
	 * @param <T> 对结果集进行处理后的返回值，如果无返回值，可指定为Object，并返回null。
	 */
	public interface ResultSetCallback<T>{
		T execute(ResultSet rs) throws SQLException;
	}

	public String getDriver() {
		return driver;
	}

	public String getUrl() {
		return url;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}


}
