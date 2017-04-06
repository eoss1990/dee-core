package com.seeyon.v3x.dee.adapter.database.jdbc.format;


import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.database.jdbc.AbstractJdbcFormat;
import com.seeyon.v3x.dee.adapter.database.jdbc.Pager;

/**   
 *   
 *   @package：com.seeyon.v3x.dee.adapter.database.jdbc.format.SqlServerFormat.java       
 *   @author    chenmeng <br/>    
 *   @create-time   2016年11月24日   下午4:34:27     
 **/
public class SqlServer2012Format extends SqlServerFormat{
	
	
	/* (non-Javadoc)
	 * @see com.seeyon.v3x.dee.adapter.database.jdbc.JdbcFormat#formatPageQuery(java.lang.String, com.seeyon.v3x.dee.adapter.database.jdbc.Pager)
	 */
	@Override
	public String formatPageQuery(String sql,Pager pager,boolean requirePagination) throws TransformException {
		if (null != pager && pager.getPageNumber() > 0 && requirePagination)
			sql = sql + String.format(" OFFSET %d ROWS FETCH NEXT %d ROW ONLY", pager.getOffset(), pager.getPageSize());
		return sql;
	}
	
 
 
}
