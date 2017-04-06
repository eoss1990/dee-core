package com.seeyon.v3x.dee.adapter.database.jdbc;


import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.TransformFactory;
import com.seeyon.v3x.dee.Document.Element;

 
 

/**   
 *   sql操作
 *   @package：com.seeyon.v3x.dee.adapter.database.jdbc.AbstractJdbcFormat.java       
 *   @author    chenmeng <br/>    
 *   @create-time   2016年11月24日   下午4:13:22     
 **/
public abstract class AbstractJdbcFormat implements JdbcFormat {
 
	private final static Log log = LogFactory.getLog(AbstractJdbcFormat.class);
 
	public String formatPageQuery(String sql, Pager pager)
			throws TransformException {
		return sql;
	}
	
	//时间类型参数转换sql
	public abstract String getDateVale(String name,String[] dateValues);

 
	@Override
	public String formatWhereSql(Parameters params) throws TransformException {
		//简单查询和pid的sql处理
		String DeeListPidStr = params.getValue("DeeListPidStr")!=null?params.getValue("DeeListPidStr").toString():null;
		String DeeListconditionStr = params.getValue("DeeListconditionStr")!=null?params.getValue("DeeListconditionStr").toString():null;
		String whereString = params.getValue("whereString")!=null?params.getValue("whereString").toString():null;
		if( DeeListPidStr != null && !"".equals(DeeListPidStr) ){
			whereString += " and " + DeeListPidStr;
		}
		if( DeeListconditionStr != null && !"".equals(DeeListconditionStr) ){
			whereString += " and " + DeeListconditionStr;
		}
		
		//高级查询sql处理
		String advanceSearch = params.getValue("advanceSearch")!=null?params.getValue("advanceSearch").toString():null;
		if(advanceSearch == null || "{}".equals(advanceSearch)){
			params.add("whereString", whereString);
			return whereString;
		}
			
		List<Map<String,Object>> ls = www.seeyon.com.utils.json.JSONUtils.parseJsonToListMap(advanceSearch);
		if(ls == null || ls.size() == 0 ){
			params.add("whereString", whereString);
			return whereString;
		}
		StringBuilder str = new StringBuilder(whereString);
		for(Map<String,Object> m:ls){
			String type = m.get("fieldType").toString();
			String value = m.get("value").toString();
			String name = m.get("name").toString();
			if(StringUtils.isNotBlank(value)){
				if("TIMESTAMP".equals(type) || "DATETIME".equals(type)){//时间
					String[] dates =  value.split("@");		
					str.append(getDateVale(name,dates));
				}else if("DECIMAL".equals(type)){//数字
					String oper = m.get("operater").toString();
					
					String operValue = "=";
					if("lt".equals(oper)){
						operValue = "<";
					}else if("gt".equals(oper)){
						operValue = ">";
					}
					str.append(" and ").append(name).append(" "+operValue+" ").append(value).append(" ");
				}else{//字符串
					//对特殊字符进行处理，转义
					StringBuilder buffer=new StringBuilder();
			    	for(int i=0;i<value.length();i++) {
			    		if(value.charAt(i)=='\'') {
			    			buffer.append("\\'");
			    		} else {
			    			buffer.append(value.charAt(i));
			    		}
			    	}
			    	value=SQLWildcardUtil.escape(buffer.toString());
			    	if(isChinese(value)){//如果有中文字符，加N前缀，让数据库已unicode进行解析
			    		str.append(" and ").append(name).append(" like N'%").append(value).append("%' ");
			    	}else{
			    		str.append(" and ").append(name).append(" like '%").append(value).append("%' ");
			    	}
				}
			}
		}
		log.debug("jdbcReader formatWhereSql:"+str.toString());
		params.add("whereString", str.toString());
		return str.toString();
	}
 
	/**
	 * 执行sql,如果需要分页，内部产生分页sql
	 */
	@Override
	public Element queryData(String sql,Pager pager,String tableName, Connection conn,boolean requirePagination) throws TransformException {
		String pageSql = formatPageQuery(sql,pager,requirePagination);
		log.debug("jdbcReader formatPageQuerySql:"+pageSql);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Document document = TransformFactory.getInstance().newDocument("root");
		Element root = document.getRootElement();
		Element table = root.addChild(tableName);
		try {
			pstmt = conn.prepareStatement(pageSql);
			rs = pstmt.executeQuery();
			Map<String, Integer> cmap = getColumnInfo(rs);
			while(rs.next()){
				Element row = table.addChild("row");
				Iterator<String> it = cmap.keySet().iterator();
				while (it.hasNext()) {
					Object o = null;
					String columnName = it.next();
					Element column = row.addChild(columnName.trim());
					if ( Types.CLOB ==  cmap.get(columnName)) {
						Clob clob = rs.getClob(columnName);
						if(null!=clob)
							column.setValue(ClobToObj(clob));

					} else if (Types.TIMESTAMP == cmap.get(columnName)) {
                        o = rs.getObject(columnName);
                        if (o!=null && "oracle.sql.TIMESTAMP".equals(o.getClass().getName())) {
                            Class clz = o.getClass();
                            Method method = clz.getMethod("timestampValue");
                            column.setValue(method.invoke(o));
                        } else {
                            column.setValue(o);
                        }
                    } else {
						o = rs.getObject(columnName);
                        column.setValue(o);
					}
				}
			}
			setDocumentAtrr(sql,table,conn,requirePagination);
			
		} catch (Exception e) {
			log.error("分页sql执行失败，切换游标分页",e);
			//如果使用分页sql导致查询失败，切换为游标分页
			table = queryDataByConcur(sql, pager, tableName, conn, requirePagination);
		} finally{
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
 
			} catch (SQLException e) {
				throw new TransformException(
						"Close Statement or ResultSet  Exception e ：" + e);
			}
		}
 
		return table;
	}
 
	/**
	 * 查询sql，如果需要分页，内部使用游标分页
	 */
	protected Element queryDataByConcur(String sql,Pager pager,String tableName, Connection conn,boolean requirePagination) throws TransformException{
		Statement pstmt = null;
		ResultSet rs = null;
		Document document = TransformFactory.getInstance().newDocument("root");
		Element table = document.createElement(tableName);
		try {
			pstmt = conn.prepareStatement(sql);
			if (requirePagination) {
				// 为了分页，开销较大
				pstmt = conn.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
			} else {
				pstmt = conn.createStatement();
			} 
			rs = pstmt.executeQuery(sql);
			if (requirePagination && pager.getPageNumber() > 1) {
				rs.absolute(pager.getOffset());
			}
			
			Map<String, Integer> cmap = getColumnInfo(rs);
			int count = 0;
			while(rs.next()){
				Element row = table.addChild("row");
				Iterator<String> it = cmap.keySet().iterator();
				while (it.hasNext()) {
					Object o = null;
					String columnName = it.next();
					Element column = row.addChild(columnName.trim());
					if ( Types.CLOB ==  cmap.get(columnName)) {
						Clob clob = rs.getClob(columnName);
						if(null!=clob)
							column.setValue(ClobToObj(clob));

					} else if (Types.TIMESTAMP == cmap.get(columnName)) {
                        o = rs.getObject(columnName);
                        if (o!=null && "oracle.sql.TIMESTAMP".equals(o.getClass().getName())) {
                            Class clz = o.getClass();
                            Method method = clz.getMethod("timestampValue");
                            column.setValue(method.invoke(o));
                        } else {
                            column.setValue(o);
                        }
                    } else {
						o = rs.getObject(columnName);
                        column.setValue(o);
					}
				}
				count++;
				if (count == pager.getPageSize()) {
					break;
				}
			}
			
			if (count == 0) {
				// 输出nullRow
				Element row = table.addChild("nullrow");
				Iterator<String> it = cmap.keySet().iterator();
				while (it.hasNext()) {
					row.addChild( it.next().trim() );
				}
			}
			
			setDocumentAtrr(sql, table, conn, requirePagination);
			
		} catch (Exception e) {
			throw new TransformException("执行sql出错：" + e.getMessage(), e);
		} finally{
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
 
			} catch (SQLException e) {
				throw new TransformException(
						"Close Statement or ResultSet  Exception e ：" + e);
			}
		}
		return table;
	}
	
	
	@Override
	public void close(Connection conn) throws TransformException {
		try {
			if(conn!=null)
				conn.close();
		} catch (SQLException e) {
			throw new TransformException(
					"Close Connection in JDBCReader Exception e ：" + e);
		}
	}
	
	 
	protected void setDocumentAtrr(String sql,Element table,Connection conn,boolean requirePagination) throws TransformException{
		// 获取记录总数，分页时返回满足条件的记录计数
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int recordCount = table.getChildren("row").size();
		table.setAttribute("count", recordCount);
		if (requirePagination) {
			String countSql="";
			try {
				String clearSql = clearOrderBy(sql);
				countSql = "select count(1) from ("	+ clearSql + ") total_";
				pstmt = conn.prepareStatement(countSql);
				rs = pstmt.executeQuery();
				if (rs.next())
					recordCount = rs.getInt(1);
			} catch (Throwable e) {
				log.error("取结果集count出错，忽略错误。" + e.getMessage()+":"+countSql, e);
			}finally{
				try {
					if (rs != null) {
						rs.close();
					}
					if (pstmt != null) {
						pstmt.close();
					}
				} catch (SQLException e) {
					throw new TransformException(
							"Close Connection in JDBCReader Exception e ：" + e);
				}
			}
			
		}
		table.setAttribute("totalCount", recordCount);
		
	}
 
	
	public String clearOrderBy(String sqls){
		//修改String 的replaceAll替换order by 为StringBuffer的delete(start, end),防止部分中文字符无法匹配导致的无限递归
		StringBuffer sql = new StringBuffer(sqls);
		int index = sql.indexOf("order");
		if(index == -1){
			index = sql.indexOf("ORDER");
		}
		if(index != -1){
			int orderByStart = -1;
			int orderByEnd = -1;
//			char[] cs =  sql.toCharArray();
			//查找order by 
			for(int i=(index+5);i<sql.length();i++){
				char c = sql.charAt(i);
				if(Character.isSpaceChar(c)){
					continue;
				}else{
					if( Character.toLowerCase(c) == 'b' && Character.toLowerCase(sql.charAt(i+1)) == 'y'){
						orderByStart = i+2;
					}
					break;
				}
			}
			if(orderByStart!=-1){
				for(int i= orderByStart;i<sql.length();i++){
					char c = sql.charAt(i);
					if(c == ')'){
						orderByEnd = i;
						break;
					}
					if( i == (sql.length()-1)){
						orderByEnd = i+1;
					}
				}
				sql = sql.delete(index, orderByEnd);
//				String orderby = sql.substring(index, orderByEnd);
//				String s = sql.replaceAll(orderby, " ");
				return clearOrderBy(sql.toString());
			}
			return sql.toString();
		}else{
			return sql.toString();
		}
		
	}
	
//	public static void main(String[] args) {
//		System.out.println(new MysqlFormat().clearOrderBy(" select * form (select 1,2 from where 1=1 order by 1 group by 2) order by id"));
//	}
	
	public Map<String, Integer> getColumnInfo(ResultSet rs)
			throws SQLException {
		ResultSetMetaData metaData = rs.getMetaData();
		Map<String, Integer> cmap = new HashMap<String, Integer>();
		int columnCount = metaData.getColumnCount();
		// 某些数据库字段名称和别名都是大写，转换为小写，在规范中要求必须使用全小写引用
		for (int i = 1; i < columnCount + 1; i++) {
//			cmap.put(metaData.getColumnLabel(i).toLowerCase(),  
//					metaData.getColumnType(i));
			//2005 sqlserver分页语句会产生2个多余字段
			if("__rn__".equals(metaData.getColumnLabel(i))){
				continue;
			}
			if("__tc__".equals(metaData.getColumnLabel(i))){
				continue;
			}
			cmap.put(metaData.getColumnLabel(i), //取消掉转换小写
					metaData.getColumnType(i));
		}
		return cmap;
	}
	
	
	//oracle.sql.Clob类型转换成String类型
	public Object ClobToObj(Clob clob) {
		String reString = "";
		java.io.Reader is = null;
		try {
			is = clob.getCharacterStream();
		} catch (Exception e) {
			//非数据流，不用转换
			return clob;
		}
		// 得到流
		BufferedReader br = new BufferedReader(is);
		String s = null;
		try {
			s = br.readLine();
		} catch (Exception e) {
			log.error(e);
		}
		StringBuffer sb = new StringBuffer();
		while (s != null) {
			//执行循环将字符串全部取出付值给StringBuffer由StringBuffer转成STRING
			sb.append(s);
			sb.append("\r\n");
			try {
				s = br.readLine();
			} catch (Exception e) {
				log.error(e);
			}
		}
		if(br != null){
			try {
				br.close();
			} catch (IOException e) {
				log.error(e);
			}
		}
		reString = sb.toString();
		return reString;
	}

	
	// 完整的判断中文汉字和符号
	private boolean isChinese(String strName) {
		if(StringUtils.isBlank(strName)){
			return false;
		}
		char[] ch = strName.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isChinese(c)) {
				return true;
			}
		}
		return false;
	}
 
	// 根据Unicode编码完美的判断中文汉字和符号
	private boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(ub)
				|| Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(ub)
				|| Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(ub)
				|| Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B.equals(ub)
				|| Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION.equals(ub)
				|| Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS.equals(ub)
				|| Character.UnicodeBlock.GENERAL_PUNCTUATION.equals(ub)) {
			return true;
		}
		return false;
	}	
}
