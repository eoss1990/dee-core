package com.seeyon.v3x.dee.adapter.database.jdbc.format;

import org.apache.commons.lang.StringUtils;

import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.database.jdbc.AbstractJdbcFormat;
import com.seeyon.v3x.dee.adapter.database.jdbc.Pager;

/**   
 *   
 *   @package：com.seeyon.v3x.dee.adapter.database.jdbc.format.SqlServerFormat.java       
 *   @author    chenmeng <br/>    
 *   @create-time   2016年11月28日   上午10:31:33     
 **/
public class SqlServerFormat extends AbstractJdbcFormat {
 
	@Override
	public String getDateVale(String name, String[] dates) {
		StringBuilder str = new StringBuilder();
		if(StringUtils.isNotBlank(dates[0]))
			str.append(" and ").append(name).append(" >= '"+dates[0]+"' ");
		if(dates.length == 2 && StringUtils.isNotBlank(dates[1]))
			str.append(" and ").append(name).append(" <= '"+dates[1]+"' ");
		return str.toString();
	}
	
	/* (non-Javadoc)
	 * @see com.seeyon.v3x.dee.adapter.database.jdbc.JdbcFormat#formatPageQuery(java.lang.String, com.seeyon.v3x.dee.adapter.database.jdbc.Pager, boolean)
	 */
	@Override
	public String formatPageQuery(String sql, Pager pager,
			boolean requirePagination) throws TransformException {
		return sql;
	}
}
