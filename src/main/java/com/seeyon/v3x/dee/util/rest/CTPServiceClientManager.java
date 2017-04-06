package com.seeyon.v3x.dee.util.rest;


public class CTPServiceClientManager {
	private String baseUrl;

	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * 取得指定服务主机的客户端管理器。
	 * 
	 * @param baseUrl
	 *            服务主机地址，包含{协议}{Ip}:{端口}，如http://127.0.0.1:80
	 * @return 客户端实例。
	 */
	public static CTPServiceClientManager getInstance(String baseUrl) {
		return new CTPServiceClientManager(baseUrl);
	}

	private CTPServiceClientManager(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	/**
	 * 取得REST动态客户机。
	 * 
	 * @return REST客户机实例。
	 */
	public CTPRestClient getRestClient() {
		return new GenericRestClient(baseUrl);
	}

	/**
	 * 取得Axis2 SOAP动态客户机。
	 * 
	 * @return SOAP客户机实例。
	 */
	/*public CTPAxis2Client getAxis2Client() {
		return new CTPAxis2Client(baseUrl);
	}*/
	/**
	 * 取得指定服务的客户端存根。
	 * @param clazz 服务接口，如AuthorityService、OrganizationDataService。
	 * @return 指定服务接口的客户端实例。
	 * @throws Exception 创建实例失败抛出异常。
	 */
	public <T> T getStub(Class<T> clazz) throws Exception {
		return StubRegistry.getInstance().newInstance(clazz, this);
	}
}