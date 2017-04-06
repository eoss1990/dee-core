package com.seeyon.v3x.dee.adapter.database.jdbc;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.seeyon.v3x.dee.Document.Element;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;
 


/**   
 *   对于所有数据库的抽象实现
 *   @package：com.seeyon.v3x.dee.adapter.database.JDBC.JdbcExpert.java       
 *   @author    chenmeng <br/>    
 *   @create-time   2016年11月24日   下午3:31:45     
 **/
public interface JdbcFormat {
	
 
	
	/**
	 * 分页sql生成
	 * @param sql
	 * @param pager
	 * @param requirePagination 是否分页
	 * @return
	 * @throws Exception
	 */
	String formatPageQuery(String sql,Pager pager,boolean requirePagination) throws TransformException;
	
	/**
	 * 查询参数where sql 生成
	 * @param sql
	 * @param pager
	 * @return
	 * @throws Exception
	 */
	String formatWhereSql(Parameters params) throws TransformException;
	
	/**
	 * 数据查询
	 * @param sql
	 * @param tableName
	 * @param conn
	 * @param requirePagination
	 * @return
	 * @throws TransformException
	 */
	Element queryData(String sql,Pager pager,String tableName,Connection conn,boolean requirePagination) throws TransformException;
	
	void close(Connection conn)  throws TransformException ;
	
}
