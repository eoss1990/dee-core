package com.seeyon.v3x.dee.datasource;

import com.seeyon.v3x.dee.resource.DbDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.sql.Connection;

/**
 * 支持Tomcat的JDNI连接池配置</br> 需要预先在tomcat内配置连接池，并且在工程web.xml中增加资源
 * 
 * @author lilong
 * 
 */
public class JNDIDataSource implements DbDataSource {
	private static Log log = LogFactory.getLog(JNDIDataSource.class);

	private String jndi;// "java:/comp/env/jdbc/mysql"

	@Override
	public synchronized Connection getConnection() throws Exception {
		Context initContext = new InitialContext();
		javax.sql.DataSource ds = (javax.sql.DataSource) initContext.lookup("java:comp//env//" + jndi);
		return ds.getConnection();
	}

	public String getJndi() {
		return jndi;
	}

	public void setJndi(String jndi) {
		this.jndi = jndi;
	}

	public void setAddress(String address) {
		this.jndi = address;
	}

}
