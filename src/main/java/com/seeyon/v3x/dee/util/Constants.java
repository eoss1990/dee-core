package com.seeyon.v3x.dee.util;

public class Constants {
    
    /** 系统登录密码及URL地址配置文件路径 */
    public static final String CONFIGURATOR_LOGIN_PROPERTY_FILENAME = "/login.properties";

    /** 系统属性配置文件路径 */
    public static final String CONFIGURATOR_SYSTEM_PROPERTY_FILENAME = "/system.properties";
    
    /** 环境变量DEE_HOME */
    public static final String DEE_HOME = "DEE_HOME";
    
    /** 参数类型-系统参数 */
    public static final int PARAMETER_TYPE_SYSTEM = 0;
    
    /** 参数类型-自定义全局参数 */
    public static final int PARAMETER_TYPE_CUSTOM = 1;
    
    
    /** 非法的TEMPLATEID */
    public static final int INVALID_TEMPLATEID = -1;
    
    
    /** A8流程表单xslt文件名称 */
    public static final String A8_FORM_FLOW = "SeeyonForm2_0.xsl";
    
    /** A8无流程表单xslt文件名称 */
    public static final String A8_FORM_NOFLOW = "SeeyonForm2_1.xsl";
    
    /** A8组织机构文件名称 */
    public static final String A8_ORGINPUT = "SeeyonOrgInput2_0.xsl";
    
    /** 适配器状态-新增 */
    public static final String ADAPTER_SAVE = "0";
    /** 适配器状态-修改 */
    public static final String ADAPTER_UPDATE = "1";
	public static final String CONFIGURATOR_PROPERTY_FILENAME = "/configurator.properties";
	public static final String SERIAL_PROPERTY_FILENAME = "/serial.properties";

	public static final String PROPERTYKEY_AUTODEPLOY = "autodeploy";

	public static final String PROPERTYKEY_KEYCODE = "KeyCode";

	public static final String PROPERTYKEY_AUTHORIZEDATE = "AuthorizeDate";

	public static final String PROPERTYKEY_DEADLINE = "Deadline";

	public static final String CONTENTTYPE = "application/x-msdownload";

	public static final String LICENSENUM = "250";

	public static final String PROPERTYKEY_SERIALNUMBER = "SerialNumber";

	public static final String REQUEST_MESSAGE_ERROR = "errorMsg";
	public static final String REQUEST_KEY_AUTHORIZEDATE = "authorizeDate";
	public static final String REQUEST_KEY_DEADLINE = "deadline";
	public static final String REQUEST_KEY_KEYCODE = "keyCode";
	public static final String REQUEST_KEY_SERIALNUMBER = "serialNumber";
	public static final String REQUEST_KEY_EXPIRED = "Expired";
	
	public static final String PARAMSCHECKMSG = "参数名称重复，修改后再保存！";

}
