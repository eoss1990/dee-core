package com.seeyon.v3x.dee.adapter.database.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.database.jdbc.format.Db2Format;
import com.seeyon.v3x.dee.adapter.database.jdbc.format.DmFormat;
import com.seeyon.v3x.dee.adapter.database.jdbc.format.MysqlFormat;
import com.seeyon.v3x.dee.adapter.database.jdbc.format.OracleFormat;
import com.seeyon.v3x.dee.adapter.database.jdbc.format.PsFormat;
import com.seeyon.v3x.dee.adapter.database.jdbc.format.SqlServer2000Format;
import com.seeyon.v3x.dee.adapter.database.jdbc.format.SqlServer2005Format;
import com.seeyon.v3x.dee.adapter.database.jdbc.format.SqlServer2012Format;

/**   
 *   
 *   @package：com.seeyon.v3x.dee.adapter.database.jdbc.JdbcUtil.java       
 *   @author    chenmeng <br/>    
 *   @create-time   2016年11月24日   下午4:59:47     
 **/
public class JdbcUtil {
	private static Pattern pat2005 = Pattern.compile("microsoft sql server.*(9|10)[.].+", Pattern.DOTALL & Pattern.CASE_INSENSITIVE);
	private static Pattern pat2000 = Pattern.compile("microsoft sql server.*(8)[.].+", Pattern.DOTALL & Pattern.CASE_INSENSITIVE);
	private static Pattern pat2012 = Pattern.compile("microsoft sql server.*(11|12|13|14|15)[.].+", Pattern.DOTALL & Pattern.CASE_INSENSITIVE);
	
	public static final String mysql = "mysql";
	public static final String postgresql = "postgresql";
	public static final String oracle = "oracle";
	public static final String db2 = "db2";
	public static final String server2000 = "server2000";
	public static final String server2005 = "server2005";
	public static final String server2012 = "server2012";
	public static final String dm = "dm";
	public static final String other = "other";
	
	private static Map<String,JdbcFormat> JdbcFormats = new HashMap<String, JdbcFormat>();
	static{
		JdbcFormats.put(mysql,new MysqlFormat());
		JdbcFormats.put(postgresql,new PsFormat());
		JdbcFormats.put(oracle,new OracleFormat());
		JdbcFormats.put(db2,new Db2Format());
		JdbcFormats.put(server2000,new SqlServer2000Format());
		JdbcFormats.put(server2005,new SqlServer2005Format());
		JdbcFormats.put(server2012,new SqlServer2012Format());
		JdbcFormats.put(dm,new DmFormat());
		JdbcFormats.put(other,new MysqlFormat());
	}

	
	public static JdbcFormat getFormat(Connection conn) throws TransformException {
		String dataProductName = "";
		String version = "";
		
		try {
			dataProductName = conn.getMetaData().getDatabaseProductName().toLowerCase();
			version = conn.getMetaData().getDatabaseProductVersion();
		
		} catch (SQLException e) {
			throw new TransformException(dataProductName + "获取异常:"+e.getMessage());
		}
		if (dataProductName.startsWith("postgresql")) {
			return JdbcFormats.get(postgresql); 
		} else if (dataProductName.startsWith("mysql")) {
			return JdbcFormats.get(mysql); 
		} else if (dataProductName.startsWith("oracle")) {
			return JdbcFormats.get(oracle); 
		} else if (dataProductName.startsWith("db2")) {
			return JdbcFormats.get(db2); 
		} else if (dataProductName.startsWith("microsoft sql")) {
			String dbName = String.format("%s %s", dataProductName, version).toLowerCase();
			if (pat2000.matcher(dbName).find()){
				 return JdbcFormats.get(server2000); 
			}
			if (pat2005.matcher(dbName).find()){//同时处理2005和2008
				 return JdbcFormats.get(server2005); 
			}
			if (pat2012.matcher(dbName).find()){
				 return JdbcFormats.get(server2012); 
			}
			return JdbcFormats.get(server2000); //如果什么版本都没有匹配，使用游标分页
		} else if (dataProductName.startsWith("dm")) {
			return JdbcFormats.get(dm); 
		} else {
			return JdbcFormats.get(other); 
		}
	}
}
