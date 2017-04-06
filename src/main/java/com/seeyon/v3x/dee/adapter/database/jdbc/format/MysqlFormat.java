package com.seeyon.v3x.dee.adapter.database.jdbc.format;

import org.apache.commons.lang.StringUtils;

import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.database.jdbc.AbstractJdbcFormat;
import com.seeyon.v3x.dee.adapter.database.jdbc.Pager;




/**   
 *   
 *   @package：com.seeyon.v3x.dee.adapter.database.JDBC.MysqlJdbcExpert.java       
 *   @author    chenmeng <br/>    
 *   @create-time   2016年11月24日   下午3:39:44     
 **/
public class MysqlFormat extends AbstractJdbcFormat {

	/* (non-Javadoc)
	 * @see com.seeyon.v3x.dee.adapter.database.JDBC.JdbcExpert#formatPageQuery(java.lang.String, com.seeyon.v3x.dee.adapter.database.JDBC.Pager)
	 */
	@Override
	public String formatPageQuery(String sql,Pager pager,boolean requirePagination) throws TransformException {
		 if (null != pager && pager.getPageNumber() > 0 && requirePagination)
			  return sql + String.format(" LIMIT %d, %d", pager.getOffset(),pager.getPageSize());
		 return sql;
	}
 

	/* (non-Javadoc)
	 * @see com.seeyon.v3x.dee.adapter.database.jdbc.AbstractJdbcFormat#getDateVale(java.lang.String, java.lang.String)
	 */
	@Override
	public String getDateVale(String name, String[] dates) {
		StringBuilder str = new StringBuilder();
		if(StringUtils.isNotBlank(dates[0]))
			str.append(" and ").append(name).append(" >= '"+dates[0]+"' ");
		if(dates.length == 2 && StringUtils.isNotBlank(dates[1]))
			str.append(" and ").append(name).append(" <= '"+dates[1]+"' ");
		return str.toString();
	}
	
	
}
