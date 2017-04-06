package com.seeyon.v3x.dee.adapter.database.jdbc.format;

import org.apache.commons.lang.StringUtils;

import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.database.jdbc.AbstractJdbcFormat;
import com.seeyon.v3x.dee.adapter.database.jdbc.Pager;

/**   
 *   
 *   @package：com.seeyon.v3x.dee.adapter.database.jdbc.format.PsFormat.java       
 *   @author    chenmeng <br/>    
 *   @create-time   2016年11月24日   下午4:01:49     
 **/
public class PsFormat extends AbstractJdbcFormat {

	/* (non-Javadoc)
	 * @see com.seeyon.v3x.dee.adapter.database.jdbc.JdbcFormat#formatPageQuery(java.lang.String, com.seeyon.v3x.dee.adapter.database.jdbc.Pager)
	 */
	public String formatPageQuery(String sql,Pager pager,boolean requirePagination) throws TransformException {
		if (null != pager && pager.getPageNumber() > 0 && requirePagination) {
			return sql + String.format(" LIMIT %d OFFSET %d",
							pager.getPageSize(),
							pager.getOffset());
		}
		return sql;
	}

 
	@Override
	public String getDateVale(String name, String[] dates) {
		StringBuilder str = new StringBuilder();
		if(StringUtils.isNotBlank(dates[0]))
			str.append(" and ").append(name).append(" >= to_date('"+dates[0]+"','yyyy-mm-dd hh24:mi:ss') ") ;
		if(dates.length == 2 && StringUtils.isNotBlank(dates[1]))
			str.append(" and ").append(name).append(" <= to_date('"+dates[1]+"','yyyy-mm-dd hh24:mi:ss') ");
		return str.toString();
	}
}
