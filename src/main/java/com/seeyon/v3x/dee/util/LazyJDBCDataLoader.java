package com.seeyon.v3x.dee.util;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.database.JDBCAdapter;
import com.seeyon.v3x.dee.resource.DataSource;
import com.seeyon.v3x.dee.resource.DbDataSource;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 通过预置的Prepared SQL按照指定参数获取数据库的单条数据，并进行缓存。<br/>
 * 如select * from table where key1=? and key2=?，调用getValue({1,101})时返回满足key1=1
 * and key2=101条件的第一条数据。返回值按照Map封装。
 * 
 * @author wangwenyou
 * 
 */
public class LazyJDBCDataLoader extends JDBCAdapter {
	private final static Log log = LogFactory.getLog(LazyJDBCDataLoader.class);

	private final String sql;
	private Set<String> columnNames;

	private Map<Object, Object> cache = new HashMap<Object, Object>();

	public LazyJDBCDataLoader(DbDataSource ds, String sql) {
		setDataSource(ds);
		this.sql = sql;
	}

	public Object getValue(List key) throws TransformException {
		return getValue(key.toArray());
	}

	public Object getValue(Object[] key) throws TransformException {
		Object k = getKey(key);
		Object value = cache.get(k);
		if (value == null) {
			value = fetchValue(key);
			if(value==null) return value;
			cache.put(k, value);
		}
		return value;
	}

	public Object fetchValue(Object[] key) throws TransformException {
		PreparedStatement pstmt = null;
		Connection connection = null;
		try {
			connection = getConnection();
			pstmt = connection.prepareStatement(sql);

			for (int i = 0; i < key.length; i++) {
				pstmt.setObject(i + 1, key[i]);
			}
			ResultSet rs = pstmt.executeQuery();
			if (this.columnNames == null)
				this.columnNames = getColumnNames(rs);
			CaseInsensitiveMap map = new CaseInsensitiveMap();
			if (rs.next()) {
				for (String columnName : this.columnNames) {
					map.put(columnName, rs.getObject(columnName));
				}
			}else{
				return null;
			}
			if (rs.next()) {
				log.warn("按照指定条件返回了多条记录，忽略。" + key);
			}
			return map;
		} catch (SQLException e) {
			log.error(key + " " + e.getMessage(), e);
			throw new TransformException(e.getMessage(),e);
		} finally {
			try {
				if(pstmt != null) {
					pstmt.close();
				}
				if(connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				throw new TransformException("Close Connection in LazyJDBCDataLoader Exception e ：" + e);
			}
		}
	}

	private Object getKey(Object[] key) {
		return new MultiKey(key);
	}

	private Set<String> getColumnNames(ResultSet rs) throws SQLException {
		Set<String> columnNames = new HashSet<String>();
		ResultSetMetaData metaData = rs.getMetaData();

		int columnCount = metaData.getColumnCount();
		for (int i = 1; i < columnCount + 1; i++) {
			columnNames.add(metaData.getColumnLabel(i).toLowerCase());
		}
		return columnNames;
	}

	@Override
	public Document execute(Document document) throws TransformException {
		return null;
	}
}
