package com.seeyon.v3x.dee.adapter.database.jdbc.format;

import org.apache.commons.lang.StringUtils;

import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.database.jdbc.AbstractJdbcFormat;
import com.seeyon.v3x.dee.adapter.database.jdbc.Pager;

/**   
 *   
 *   @package：com.seeyon.v3x.dee.adapter.database.jdbc.format.OracleFormat.java       
 *   @author    chenmeng <br/>    
 *   @create-time   2016年11月24日   下午3:59:35     
 **/
public class OracleFormat extends AbstractJdbcFormat {

	/* (non-Javadoc)
	 * @see com.seeyon.v3x.dee.adapter.database.jdbc.JdbcFormat#formatPageQuery(java.lang.String, com.seeyon.v3x.dee.adapter.database.jdbc.Pager)
	 */
	@Override
	public String formatPageQuery(String sql,Pager pager,boolean requirePagination) throws TransformException {
		 if (null != pager && pager.getPageNumber() > 0 &&  requirePagination) {
			 String pre = "SELECT * FROM (SELECT T__.*, ROWNUM RN FROM (";
	         String last = String.format(") T__ WHERE ROWNUM <= %d) WHERE RN > %d",
                                        pager.getOffset() + pager.getPageSize(),
                                        pager.getOffset());
	         return pre + sql + last;
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
