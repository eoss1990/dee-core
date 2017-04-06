package com.seeyon.v3x.dee;

import com.seeyon.ctp.common.init.MclclzUtil;
import com.seeyon.v3x.dee.datasource.XMLDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

/**
 * DEE调用工具类<br/>
 * 提供本地调用和远程调用
 * 
 * @author lilong
 */
public class DEEClient {
	private final static Log log = LogFactory.getLog(DEEClient.class);

	private final boolean isLocal;// 远程调用标识
	/*
	 * private Properties properties = new Properties(); public Properties
	 * getProperties() { return properties; }
	 * 
	 * public void setProperties(Properties properties) { if(properties==null)
	 * return; this.properties = properties; // TODO
	 * 重构，目前是为了避免A8插件代码中直接引用JDBCDataSource，隐藏实现细节 // 注册DEE元数据数据源 String driver =
	 * properties.getProperty("dee.meta.datasource.driver"); String url =
	 * properties.getProperty("dee.meta.datasource.url"); String userName =
	 * properties.getProperty("dee.meta.datasource.userName"); String password =
	 * properties.getProperty("dee.meta.datasource.password"); DataSource ds =
	 * new JDBCDataSource(driver, url, userName, password);
	 * DataSourceManager.getInstance().bind("dee_meta",ds); }
	 */

	private String url;
	private String userName;
	private String pwd;
	private String path;// 预留参数
	private static final Class<?> c1 = MclclzUtil.ioiekc("com.seeyon.v3x.dee.context.EngineController");
	private static Object controller;

	public DEEClient() {
		this.isLocal = true;
		controller = MclclzUtil.invoke(c1, "getInstance",
				new Class[]{String.class}, null,
				new String[]{null});
	}

	public DEEClient(String url, String userName, String pwd) {
		this.url = url;
		this.userName = userName;
		this.pwd = pwd;
		this.isLocal = false;
	}

	/**
	 * 调用DEE任务。
	 * 
	 * @param flowName
	 *            DEE流程
	 * @return DEE任务执行输出的Document
	 * @throws TransformException
	 * @throws NoSuchMethodException
	 * @throws java.lang.reflect.InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	public Document execute(String flowName) throws TransformException,
			IllegalArgumentException, SecurityException,
			ClassNotFoundException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		return execute(flowName, new Parameters());
	}

	/**
	 * 调用DEE任务。
	 *
	 * @param flowName
	 *            DEE流程
	 * @param params
	 *            参数
	 * @return DEE任务执行输出的Document
	 * @throws TransformException
	 * @throws NoSuchMethodException
	 * @throws java.lang.reflect.InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	public Document execute(String flowName, Parameters params)
			throws TransformException, IllegalArgumentException,
			SecurityException, ClassNotFoundException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		return execute(flowName, null, params);
	}

	/**
	 * 远程调用
	 *
	 * @param flowName
	 *            DEE流程
	 * @param input
	 *            输入Document
	 * @param params
	 *            参数
	 * @return DEE任务执行输出的Document
	 * @throws TransformException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws java.lang.reflect.InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	public Document execute(String flowName, Document input, Parameters params)
			throws TransformException, ClassNotFoundException,
			IllegalArgumentException, SecurityException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		if (isLocal) {
			// 执行DEE中的Flow
			controller = MclclzUtil.invoke(c1, "getInstance",
					new Class[]{String.class}, null,
					new String[]{null});
			if (input == null) {
				return (Document) controller
						.getClass()
						.getMethod("executeFlow",
								new Class[] { String.class, Parameters.class })
						.invoke(controller, flowName, params);
			} else {
				return (Document) controller
						.getClass()
						.getMethod(
								"executeFlow",
								new Class[] { String.class, Document.class,
										Parameters.class })
						.invoke(controller, flowName, input, params);
			}
		} else {
			String url = this.getConnectUrl(flowName, params);// 组装远程调用的地址
			if ("".equals(url) || null == url) {
				throw new TransformException("获取调用地址异常 URL==" + url);
			}
			Document document = null;
			DataInputStream dis = null;
			ByteArrayOutputStream dos = null;
			URLConnection uc;
			try {
				uc = new URL(url).openConnection();
				dis = new DataInputStream(uc.getInputStream());
				dos = new ByteArrayOutputStream();
				byte[] buffer = new byte[4096];
				int count = 0;
				while ((count = dis.read(buffer)) > 0) {
					dos.write(buffer, 0, count);
				}
				String xml = dos.toString(DEEConstants.CHARSET_UTF8);
				XMLDataSource ds = new XMLDataSource(xml);
				document = ds.parse();
				dos.flush();
				dos.close();
				dis.close();
				return document;
			} catch (Exception e) {
				// MalformedURLException,IOException,DocumentException
				// 产生三个异常，统一用Exception捕获
				log.error("Remote Fail! url=" + url);
				throw new TransformException("Remote Fail: "
						+ e.getLocalizedMessage());
			}
		}
	}

	/**
	 * 组装远程调用地址
	 * 
	 * @param params
	 * @return 远程调用URL
	 */
	private String getConnectUrl(String flowName, Parameters params) {
		StringBuffer urlTemp = new StringBuffer();
		urlTemp.append(url);// http://localhost:8086/jersey/rest/
		if (!urlTemp.toString().endsWith("/")) {
			urlTemp.append("/");
		}
		urlTemp.append(userName).append("/").append(pwd);
		urlTemp.append("/").append(flowName).append("?");

		StringBuffer urlParams = new StringBuffer();
		for (Parameter parameter : params) {
			try {
				// 第一次encode，让值的一些特殊符号被转码
				urlParams
						.append(parameter.getName())
						.append("=")
						.append(java.net.URLEncoder.encode(parameter.getValue()
								.toString(), DEEConstants.CHARSET_UTF8));
			} catch (Exception e) {
				log.error("组装URL后encode异常 in getConnectUrl"
						+ e.getLocalizedMessage());
			}
			urlParams.append(",");
		}
		urlParams.deleteCharAt(urlParams.length() - 1);// 去除最后对于的符号
		String urlParamsStr = "";
		try {
			// 第二次encode
			urlParamsStr = java.net.URLEncoder.encode(urlParams.toString(),
					DEEConstants.CHARSET_UTF8);
		} catch (UnsupportedEncodingException e) {
			log.error("组装URL后encode异常 in getConnectUrl"
					+ e.getLocalizedMessage());
		}
		urlTemp.append("paras=" + urlParamsStr);
		if (log.isDebugEnabled()) {
			log.debug("远程调用地址URL==" + urlTemp.toString());
		}
		return urlTemp.toString();
	}

	/**
	 * 重新加载配置，只支持本地调用模式。
	 * 
	 * @throws Throwable
	 */
	public void refreshContext() throws Throwable {
		if (isLocal) {
			controller.getClass().getMethod("refreshContext", null)
					.invoke(controller);
		}
	}

	public void setInner(boolean isInner) throws Throwable {
		controller.getClass().getMethod("setInner", Boolean.TYPE)
				.invoke(controller, isInner);
	}

	public Set<String> getLicenceFlowIdList() throws Throwable {
		return (Set<String>) controller.getClass()
				.getMethod("getLicenceFlowIdList", null)
				.invoke(controller, null);
	}

	public Object lookup(String name) throws Throwable {
		return  controller.getClass()
				.getMethod("lookup", String.class).invoke(controller, name);
	}

	/*****************************************/
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
