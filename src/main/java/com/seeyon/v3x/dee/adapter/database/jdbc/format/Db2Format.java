package com.seeyon.v3x.dee.adapter.database.jdbc.format;

import org.apache.commons.lang.StringUtils;

import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.database.jdbc.AbstractJdbcFormat;
import com.seeyon.v3x.dee.adapter.database.jdbc.Pager;



/**   
 *   
 *   @package：com.seeyon.v3x.dee.adapter.database.JDBC.Db2JdbcExpert.java       
 *   @author    chenmeng <br/>    
 *   @create-time   2016年11月24日   下午3:51:59     
 **/
public class Db2Format extends AbstractJdbcFormat {

	 
	@Override
	public String formatPageQuery(String sql,Pager pager,boolean requirePagination) throws TransformException {
		if (null != pager && pager.getPageNumber() > 0 && requirePagination) {
            String pre = "SELECT * FROM (SELECT ROW_NUMBER() OVER() AS ROWNUM, T__.* FROM (";
            String last = String.format(    ") T__) AS A__ WHERE ROWNUM BETWEEN %d AND %d",
                    pager.getOffset() + 1,
                    pager.getOffset() + pager.getPageSize());
           return pre + sql + last;
        }
		return sql;
	}

	@Override
	public String getDateVale(String name, String[] dates) {
		StringBuilder str = new StringBuilder();
		if(StringUtils.isNotBlank(dates[0]))
			str.append(" and ").append("CHAR("+name+")").append(" >= '"+dates[0]+"' ") ;
		if(dates.length == 2 && StringUtils.isNotBlank(dates[1]))
			str.append(" and ").append("CHAR("+name+")").append(" <= '"+dates[1]+"' ");
		return str.toString();
	}
 

}
