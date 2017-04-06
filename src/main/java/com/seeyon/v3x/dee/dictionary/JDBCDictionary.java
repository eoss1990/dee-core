package com.seeyon.v3x.dee.dictionary;

import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.resource.DbDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 根据数据库表进行编码（枚举值）转换。实现逻辑为
 * <code>SELECT valueColumn FROM tableName WHERE keyColumn = ?</code>
 *
 * 异构系统进行数据交换，如果存在主键不一致的情况，可以通过主键映射表来适配，此时JDBCDictionary可用于读取主键映射表。只用于一对一的主键映射，
 * 不支持复合主键。<br/>
 * 后续需要支持两种模式： 1、一次性完全加载，需占用较多内存。 2、按需加载，使用LRU缓存部分数据。
 *
 * @author wangwenyou
 *
 * @param <K>
 * @param <V>
 */
public class JDBCDictionary<K, V>  implements Dictionary<K, V> {
	private DbDataSource dataSource;
	private String keyColumn;
	private String valueColumn;
	private String tableName;
	private int num = -1;
	private Map<K, V> map;

	private final byte[] lock = new byte[0];

	public JDBCDictionary() {
	};

	public JDBCDictionary(DbDataSource datasource, String tableName,
			String keyColumn, String valueColumn) throws TransformException {
		super();
		this.dataSource = datasource;
		this.tableName = tableName;
		this.keyColumn = keyColumn;
		this.valueColumn = valueColumn;
		this.load();
	}

	public void load() throws TransformException {
		// 缺省只缓存5000条，避免数据量太大导致内存溢出
		num = -1;
		this.map = new LinkedHashMap<K, V>(5000);
	}

	@Override
	public V get(K key) {
		if (this.map != null && this.map.containsKey(key)) {
            return this.map.get(key);
        }
        return fetchNcache(key);
    }

	private V fetchNcache(K key) {
		synchronized (lock) {
            ResultSet rs = null;
            Connection connection = null;
            PreparedStatement stmt = null;
            try {
            	if(num == -1){
                	String count = "select count(*) from " + tableName;
                	connection = dataSource.getConnection();
                    stmt = connection.prepareStatement(count);
                    rs = stmt.executeQuery();
                    while (rs.next()) {        
                    	num = rs.getInt(1);
                    }
            	}
            	if(num < 5000 && map.size() == 0){
            		 String select = "select " + keyColumn + ", " + valueColumn + " from " + tableName;
            		 if(connection == null){
            			 connection = dataSource.getConnection();
            		 }
            		 stmt = connection.prepareStatement(select);
                     rs = stmt.executeQuery();
                     while (rs.next()) {        
                         K k = (K) rs.getObject(1);
                         V value = (V) rs.getObject(2);
                         this.map.put(k, value);
                     }
 					 return map.get(key);
            	}else if (num < 5000 && map.size() != 0){
            		return map.get(key);
            	}
                StringBuilder sql = new StringBuilder("SELECT ");
                sql.append(valueColumn).append(" FROM ").append(tableName).append(" WHERE ").append(keyColumn).append("=?");

                if(connection == null){
       			 	connection = dataSource.getConnection();
       		 	}
                stmt = connection.prepareStatement(sql.toString());
                stmt.setObject(1, key);
                rs = stmt.executeQuery();
                if (rs.next()) {        // TODO 抛出异常的方式重构
                    V value = (V) rs.getObject(1);
                    this.map.put(key, value);
					return value;
                }
            } catch (Exception e) {
                throw new UnsupportedOperationException(key + "", e);
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (stmt != null) {
                        stmt.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException ignored) {
                }
            }
        }
		return null;
	}

	@Override
	public boolean containsKey(K key) {
		if (this.map.containsKey(key)) {
			return true;
		}
		return fetchNcache(key) != null;
	}

	@Override
	public void put(K key, V value) {
		this.map.put(key, value);
	}

	@Override
	public void clear() {
		this.map.clear();
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return this.map.keySet();
	}

	@Override
	public int size() {
		return this.map.size();
	}

	public DbDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DbDataSource datasource) {
		this.dataSource = datasource;
	}

	public String getKeyColumn() {
		return keyColumn;
	}

	public void setKeyColumn(String keyColumn) {
		this.keyColumn = keyColumn;
	}

	public String getValueColumn() {
		return valueColumn;
	}

	public void setValueColumn(String valueColumn) {
		this.valueColumn = valueColumn;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

}
