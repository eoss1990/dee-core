package com.seeyon.v3x.dee.adapter.database.jdbc.format;

import java.sql.Connection;

import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.Document.Element;
import com.seeyon.v3x.dee.adapter.database.jdbc.AbstractJdbcFormat;
import com.seeyon.v3x.dee.adapter.database.jdbc.Pager;

/**   
 *   
 *   @package：com.seeyon.v3x.dee.adapter.database.jdbc.format.SqlServerFormat.java       
 *   @author    chenmeng <br/>    
 *   @create-time   2016年11月24日   下午4:34:27     
 **/
public class SqlServer2000Format extends SqlServerFormat{
	
	
	/* (non-Javadoc)
	 * @see com.seeyon.v3x.dee.adapter.database.jdbc.JdbcFormat#formatPageQuery(java.lang.String, com.seeyon.v3x.dee.adapter.database.jdbc.Pager)
	 */
	@Override
	public String formatPageQuery(String sql,Pager pager,boolean requirePagination) throws TransformException {
		// TODO Auto-generated method stub
		return sql;
	}
	
 
	/**
	 * 执行sql
	 */
	@Override
	public Element queryData(String sql,Pager pager,String tableName,Connection conn,boolean requirePagination) throws TransformException {
		//游标分页
		return queryDataByConcur(sql, pager, tableName, conn, requirePagination);
	}
	
	
	
}
