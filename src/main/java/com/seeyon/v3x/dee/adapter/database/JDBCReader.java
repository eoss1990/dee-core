package com.seeyon.v3x.dee.adapter.database;

import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.adapter.database.jdbc.JdbcFormat;
import com.seeyon.v3x.dee.adapter.database.jdbc.JdbcUtil;
import com.seeyon.v3x.dee.adapter.database.jdbc.Pager;
import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Document.Element;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.TransformFactory;






import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 数据库读取适配器，通过JDBC和SQL语句读取数据库数据进行转换。
 * 
 * @author wangwenyou
 * 
 */
public class JDBCReader extends JDBCAdapter{
	private final static Log log = LogFactory.getLog(JDBCReader.class);

	protected Map<String, String> sql = new LinkedHashMap<String, String>();
	// 分页配置，例如 “master :
	// Pagination_”，则master对应的sql使用参数中的Pagination_pageSize和Pagination_pageNumber值进行分页。如果没有传入Parameters，则忽略分页配置
	protected Map<String, String> pagination = new LinkedHashMap<String, String>();

	public Map<String, String> getPagination() {
		return pagination;
	}

	public void setPagination(Map<String, String> pagination) {
		this.pagination = pagination;
	}

	public void setSql(Map<String, String> sql) {
		this.sql = sql;
	}

	/**
	 * 取得SQL。
	 * 
	 * @return 获取数据的SQL,Key为SQL的别名，Value为SQL语句。
	 */
	public Map<String, String> getSql() {
		return this.sql;
	}

	/**
	 * 设置Reader读取数据的SQL语句。
	 * 
	 * @param name
	 *            SQL语句的别名。
	 * @param sql
	 *            SQL语句。
	 */
	public void setSql2Map(String name, String sql) {
		this.sql.put(name, sql);
	}

	/**
	 * 清除设置的SQL。
	 */
	public void clear() {
		this.sql.clear();
	}

	/**
	 * 读取数据库数据，解析为Document，格式如：
	 * <p>
	 * <code>&lt;root&gt;<br/>
	 * &lt;table1&gt;
	 * &lt;row&gt;
	 * &lt;column1&gt;value1&lt;/column1&gt;
	 * &lt;column2&gt;value2&lt;/column2&gt;...
	 * &lt;/row&gt;
	 * &lt;row&gt;...&lt;/row&gt;
	 * &lt;/tabel1&gt;<br/>
	 * &lt;table2&gt;&lt;/tabel2&gt;<br/>
	 * &lt;table3&gt;&lt;/tabel3&gt;<br/>
	 * &lt;/root&gt;
	 * </code>
	 * </p>
	 */
	@Override
	public Document execute(Document edocument) throws TransformException {
		String oldWhereString = "";//列表把高级查询添加拼接到whereString后，需要还原
		Parameters params = null;
		if (edocument.getContext() != null) {
			params = edocument.getContext().getParameters();
		}

		Document document = TransformFactory.getInstance().newDocument("root");
		Element root = document.getRootElement();
		
		// 设置读取最大记录数
		int defaultPageSize = 65535; // 最多取65535条
		try {
			if(params!=null)
			{
				if(params.getValue("dee.default.pagesize")!=null) {
                    defaultPageSize = Integer.parseInt(params.getValue("dee.default.pagesize") + "");
                }
			}
        } catch (Exception e) {
            // 记录警告信息
            log.warn(e.getMessage(), e);
        }

		Connection connection = null;
		try {
			connection = getConnection();
			for (Entry<String, String> entry : this.sql.entrySet()) {
				// 判断是否需要分页
				boolean requirePagination = false;
				int pageNumber = 0;
                int pageSize = defaultPageSize;
                Pager pager = new Pager(pageNumber, pageSize);
                String key = entry.getKey()!=null?entry.getKey().trim():"";
                String listTreeResultName = "";
                String treeResultName = "";
                if(params!=null){//获取dee控件的列表结果集和树结构结果集
                	listTreeResultName =  params.getValue("listTreeResultName")!=null? params.getValue("listTreeResultName").toString().trim():"";
                    treeResultName = params.getValue("treeResultName")!=null?params.getValue("treeResultName").toString():"";
                }
                
                //判断当前执行sql的名称是否是树的结果集名称，是则不分页，不是则进行分页判断
				if(!key.equals(treeResultName)){
					if (params != null) {
						String prefix = this.pagination != null ? this.pagination.get(entry.getKey()) : null;
						//如果是控件的list列表，必须分页
						if(!"".equals(listTreeResultName) && key.trim().equals(listTreeResultName.trim()) ){
							prefix = "Paging_";
						}
						if (prefix != null ) {
							try {
								Object o1 = params.getValue(prefix + "pageNumber");
								Object o2 = params.getValue(prefix + "pageSize");
								if (o1 != null && o2 != null) {
									pageNumber = Integer.valueOf(o1.toString());
									pageSize =Integer.valueOf(o2.toString()) > pageSize ? pageSize :Integer.valueOf(o2.toString());
									requirePagination = true;
									pager.setPageNumber(pageNumber);
									pager.setPageSize(pageSize);
								} else {
									log.warn("未设置分页参数，忽略，不进行分页。" + prefix
											+ "pageNumber" + "," + prefix
											+ "pageSize");
								}
							} catch (Throwable e) {
								log.warn("分页参数设置错误，忽略，不进行分页。" + e.getMessage(), e);
							}
						}
						
					}
				}
				
				JdbcFormat format = JdbcUtil.getFormat(connection);
				
				//如果是列表的read查询，拼接高级查询到whereString
				if(listTreeResultName.trim().equals(key.trim())){
					oldWhereString = params.getValue("whereString")!=null?params.getValue("whereString").toString():null;					
					//处理简单查询，pid,高级查询的值(控件列表)
					format.formatWhereSql(params);
				}
				String sql = StringEscapeUtils.unescapeXml(entry.getValue());
				if (params != null) {
					sql = params.evalString(StringEscapeUtils.escapeJava(sql));
				}
				//列表把高级查询添加拼接到whereString后，需要还原
				if(listTreeResultName.trim().equals(key.trim())){
					params.add("whereString", oldWhereString ); 
				}
				
				Element table = format.queryData(sql, pager, entry.getKey(), connection, requirePagination);
				root.addChild(table);
				
			}

			return document;
		} catch (OutOfMemoryError e) {
			throw new TransformException("结果集过大导致内存不足,请重新设置PageSize.", e);
		} catch (Exception e) {
			log.error(String.format(":Druid连接池removeAbandonedCount次数为%d次",getRemoveAbandonedCount()));

			if (e instanceof TransformException)
				throw (TransformException) e;
			throw new TransformException("执行sql出错：" + e.getMessage(), e);

		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				throw new TransformException(
						"Close Connection in JDBCReader Exception e ：" + e);
			}
		}
	}
 
 
 
}