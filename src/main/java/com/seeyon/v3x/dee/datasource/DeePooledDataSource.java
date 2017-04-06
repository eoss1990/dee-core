package com.seeyon.v3x.dee.datasource;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class DeePooledDataSource implements Serializable {
	private final static Log log = LogFactory.getLog(DeePooledDataSource.class);
	//连接池中保留的最大连接数
	private Integer maxPoolSize; 
	//连接池中保留的最小连接数
    private Integer minPoolSize;
  //初始化连接池中的连接数，取值应在minPoolSize与maxPoolSize之间，默认为3
    private Integer initialPoolSize; 
    //最大空闲时间，60秒内未使用则连接被丢弃。若为0则永不丢弃。默认值: 0 
    private Integer maxIdleTime; 
    //当连接池连接耗尽时，客户端调用getConnection()后等待获取新连接的时间，
    //超时后将抛出SQLException，如设为0则无限期等待。单位毫秒。默认: 0    
    private Integer checkoutTimeout; 
    //当连接池中的连接耗尽的时候c3p0一次同时获取的连接数。默认值: 3   
    private Integer acquireIncrement;
   //定义在从数据库获取新连接失败后重复尝试的次数。默认值: 30 ；
    //小于等于0表示无限次
    private Integer acquireRetryAttempts;
    //重新尝试的时间间隔，默认为：1000毫秒
    private Integer acquireRetryDelay;
    //关闭连接时，是否提交未提交的事务，默认为false，
    //即关闭连接，回滚未提交的事务  
    private String autoCommitOnClose;  
    //c3p0将建一张名为Test的空表，并使用其自带的查询语句进行测试。如果定义了这个参数那么属性preferredTestQuery将被忽略。你不能在这张Test表上进行任何操作，它将只供c3p0测试使用。默认值: null -->   
    //<property name="automaticTestTable">Test</property>  
    //如果为false，则获取连接失败将会引起所有等待连接池来获取连接的线程抛出异常，
    //但是数据源仍有效保留，并在下次调用getConnection()的时候继续尝试获取连接。
    //如果设为true，那么在尝试获取连接失败后该数据源将申明已断开并永久关闭。
    //默认: false  
    private String breakAfterAcquireFailure;
    //每60秒检查所有连接池中的空闲连接。默认值: 0，不检查  
//    private Integer idleConnectionTestPeriod;  
    //JDBC的标准参数，用以控制数据源内加载的PreparedStatements数量。但由于预缓存的statements 
    //属于单个connection而不是整个连接池。所以设置这个参数需要考虑到多方面的因素。 
    //如果maxStatements与maxStatementsPerConnection均为0，则缓存被关闭。Default: 0  
    private Integer maxStatements;  
    //定义了连接池内单个连接所拥有的最大缓存statements数。默认值: 0   
    private Integer maxStatementsPerConnection;
    
    public DeePooledDataSource(){
    }
    //解析连接池参数
    public DeePooledDataSource(String dsXml){
        try {
        	if(dsXml == null || "".equals(dsXml)){
            	initPool();
                return;
        	}
            Document document = DocumentHelper.parseText(dsXml);
            Element root = document.getRootElement();
            if(root.element("pool") == null){
            	initPool();
                return;
            }
            List<Element> elList = root.element("pool").elements("property");
    		
            //连接池中保留的最大连接数
            String tmpVal = getPoolInfoFromList(elList,"maxPoolSize");
            this.maxPoolSize = tmpVal == null || "".equals(tmpVal)?50:Integer.parseInt(tmpVal);
    		
            //连接池中保留的最小连接数
            tmpVal = getPoolInfoFromList(elList,"minPoolSize");
            this.minPoolSize = tmpVal == null || "".equals(tmpVal)?2:Integer.parseInt(tmpVal);
    	    
            //初始化连接池中的连接数，取值应在minPoolSize与maxPoolSize之间，默认为3
            tmpVal = getPoolInfoFromList(elList,"initialPoolSize");
            this.initialPoolSize = tmpVal == null || "".equals(tmpVal)?1:Integer.parseInt(tmpVal);
    	    
            //最大空闲时间，60秒内未使用则连接被丢弃。若为0则永不丢弃。默认值: 0 
            tmpVal = getPoolInfoFromList(elList,"maxIdleTime");
            this.maxIdleTime = tmpVal == null || "".equals(tmpVal)?60:Integer.parseInt(tmpVal);
    	    
            //当连接池连接耗尽时，客户端调用getConnection()后等待获取新连接的时间，
    	    //超时后将抛出SQLException，如设为0则无限期等待。单位毫秒。默认: 0    
            tmpVal = getPoolInfoFromList(elList,"checkoutTimeout");
            this.checkoutTimeout = tmpVal == null || "".equals(tmpVal)?5000:Integer.parseInt(tmpVal);
    	    
            //当连接池中的连接耗尽的时候c3p0一次同时获取的连接数。默认值: 3   
            tmpVal = getPoolInfoFromList(elList,"acquireIncrement");
            this.acquireIncrement = tmpVal == null || "".equals(tmpVal)?3:Integer.parseInt(tmpVal);
    	    
            //定义在从数据库获取新连接失败后重复尝试的次数。默认值: 30 ；
    	    //小于等于0表示无限次
            tmpVal = getPoolInfoFromList(elList,"acquireRetryAttempts");
            this.acquireRetryAttempts = tmpVal == null || "".equals(tmpVal)?2:Integer.parseInt(tmpVal);
    	    
            //重新尝试的时间间隔，默认为：1000毫秒
            tmpVal = getPoolInfoFromList(elList,"acquireRetryDelay");
            this.acquireRetryDelay = tmpVal == null || "".equals(tmpVal)?1000:Integer.parseInt(tmpVal);
    	    
            //关闭连接时，是否提交未提交的事务，默认为false，
    	    //即关闭连接，回滚未提交的事务  
            tmpVal = getPoolInfoFromList(elList,"autoCommitOnClose");
            this.autoCommitOnClose = tmpVal == null || "".equals(tmpVal)?"false":tmpVal;
    	    
            //c3p0将建一张名为Test的空表，并使用其自带的查询语句进行测试。如果定义了这个参数那么属性preferredTestQuery将被忽略。你不能在这张Test表上进行任何操作，它将只供c3p0测试使用。默认值: null -->   
    	    //<property name="automaticTestTable">Test</property>  
    	    //如果为false，则获取连接失败将会引起所有等待连接池来获取连接的线程抛出异常，
    	    //但是数据源仍有效保留，并在下次调用getConnection()的时候继续尝试获取连接。
    	    //如果设为true，那么在尝试获取连接失败后该数据源将申明已断开并永久关闭。
    	    //默认: false  
            tmpVal = getPoolInfoFromList(elList,"breakAfterAcquireFailure");
            this.breakAfterAcquireFailure = tmpVal == null || "".equals(tmpVal)?"false":tmpVal;
    	    
            //每60秒检查所有连接池中的空闲连接。默认值: 0，不检查  
//            tmpVal = getPoolInfoFromList(elList,"idleConnectionTestPeriod");
//            this.idleConnectionTestPeriod = tmpVal == null || "".equals(tmpVal)?0:Integer.parseInt(tmpVal);
    	    
            //JDBC的标准参数，用以控制数据源内加载的PreparedStatements数量。但由于预缓存的statements 
    	    //属于单个connection而不是整个连接池。所以设置这个参数需要考虑到多方面的因素。 
    	    //如果maxStatements与maxStatementsPerConnection均为0，则缓存被关闭。Default: 0  
            tmpVal = getPoolInfoFromList(elList,"maxStatements");
            this.maxStatements = tmpVal == null || "".equals(tmpVal)?0:Integer.parseInt(tmpVal);
    	    
            //定义了连接池内单个连接所拥有的最大缓存statements数。默认值: 0   
            tmpVal = getPoolInfoFromList(elList,"maxStatementsPerConnection");
            this.maxStatementsPerConnection = tmpVal == null || "".equals(tmpVal)?0:Integer.parseInt(tmpVal);
        } catch(DocumentException e) {
			log.error(e.getMessage(), e);
        }
    }
    
    private String getPoolInfoFromList(List<Element> nodes,String eleName){
    	if(eleName == null || "".equals(eleName)) return null;
    	for(Element e: nodes){
    		if(e != null && eleName.equalsIgnoreCase(e.attribute("name").getValue())){
    			return e.attribute("value").getValue();
    		}
    	}
		return null;
    }

	/**
	 * 2017.03.11 by yangyu
	 * 连接池属性优化
	 * initialPoolSize从10改为1，初始化线程池时无需直接初始化10个连接，有点浪费资源，改为1
	 */
	private void initPool(){
        this.maxPoolSize = 50;
        this.minPoolSize = 2;
        this.initialPoolSize = 1;
        this.maxIdleTime = 60;
        this.checkoutTimeout = 5000;
        this.acquireIncrement = 3;
        this.acquireRetryAttempts = 2;
        this.acquireRetryDelay = 1000;
        this.autoCommitOnClose = "false";
        this.breakAfterAcquireFailure = "false";
        this.maxStatements = 0;
        this.maxStatementsPerConnection = 0;
    }
    
	public Integer getMaxPoolSize() {
		return maxPoolSize;
	}
	public void setMaxPoolSize(Integer maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}
	public Integer getMinPoolSize() {
		return minPoolSize;
	}
	public void setMinPoolSize(Integer minPoolSize) {
		this.minPoolSize = minPoolSize;
	}
	public Integer getInitialPoolSize() {
		return initialPoolSize;
	}
	public void setInitialPoolSize(Integer initialPoolSize) {
		this.initialPoolSize = initialPoolSize;
	}
	public Integer getMaxIdleTime() {
		return maxIdleTime;
	}
	public void setMaxIdleTime(Integer maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
	}
	public Integer getCheckoutTimeout() {
		return checkoutTimeout;
	}
	public void setCheckoutTimeout(Integer checkoutTimeout) {
		this.checkoutTimeout = checkoutTimeout;
	}
	public Integer getAcquireIncrement() {
		return acquireIncrement;
	}
	public void setAcquireIncrement(Integer acquireIncrement) {
		this.acquireIncrement = acquireIncrement;
	}
	public Integer getAcquireRetryAttempts() {
		return acquireRetryAttempts;
	}
	public void setAcquireRetryAttempts(Integer acquireRetryAttempts) {
		this.acquireRetryAttempts = acquireRetryAttempts;
	}
	public Integer getAcquireRetryDelay() {
		return acquireRetryDelay;
	}
	public void setAcquireRetryDelay(Integer acquireRetryDelay) {
		this.acquireRetryDelay = acquireRetryDelay;
	}
	public String getAutoCommitOnClose() {
		return autoCommitOnClose;
	}
	public void setAutoCommitOnClose(String autoCommitOnClose) {
		this.autoCommitOnClose = autoCommitOnClose;
	}
	public String getBreakAfterAcquireFailure() {
		return breakAfterAcquireFailure;
	}
	public void setBreakAfterAcquireFailure(String breakAfterAcquireFailure) {
		this.breakAfterAcquireFailure = breakAfterAcquireFailure;
	}
	public Integer getMaxStatements() {
		return maxStatements;
	}
	public void setMaxStatements(Integer maxStatements) {
		this.maxStatements = maxStatements;
	}
	public Integer getMaxStatementsPerConnection() {
		return maxStatementsPerConnection;
	}
	public void setMaxStatementsPerConnection(Integer maxStatementsPerConnection) {
		this.maxStatementsPerConnection = maxStatementsPerConnection;
	}
}
