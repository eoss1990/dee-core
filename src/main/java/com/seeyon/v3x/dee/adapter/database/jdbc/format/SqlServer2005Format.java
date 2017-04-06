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
public class SqlServer2005Format extends SqlServerFormat{
	
	
	/* (non-Javadoc)
	 * @see com.seeyon.v3x.dee.adapter.database.jdbc.JdbcFormat#formatPageQuery(java.lang.String, com.seeyon.v3x.dee.adapter.database.jdbc.Pager)
	 */
	@Override
	public String formatPageQuery(String sql,Pager pager,boolean requirePagination) throws TransformException {
		sql = sql.trim();
		if (null != pager && pager.getPageNumber() > 0 && requirePagination) {
			if (!sql.toUpperCase().startsWith("SELECT "))
				throw new TransformException("错误，非查询sql");
			String xSql = sql.substring(6);
			String pre = String.format(    "select * from(select row_number()over(order by __tc__)__rn__,* from(select top %d 0 __tc__, ",
                      pager.getOffset() + pager.getPageSize());
			String last = String.format(") t__ ) tt__ where __rn__ > %d", pager.getOffset());
			sql = pre + xSql + last;
	    }
		return sql;
	}
	
 
	 
	
	
}
