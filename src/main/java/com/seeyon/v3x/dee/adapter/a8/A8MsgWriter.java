package com.seeyon.v3x.dee.adapter.a8;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.adapter.InitializingAdapter;
import com.seeyon.v3x.dee.util.A8WSTokenUtil;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.Map;

public class A8MsgWriter implements Adapter,InitializingAdapter {

	private final static Log log = LogFactory
	.getLog(A8MsgWriter.class);
	
	private String name;
	/** 接口名称 */
    private String interfaceName;

    /** xmlns */
    private String xmlns;

    /** 方法名称 */
    private String methodName;

    /** 消息接收人map */
    private Map<String, String> paraMap;

    /** a8WS登录名 */
    private String userName;

    /** 密码 */
    private String password;

    /** url */
    private String a8url;
    
    /** 消息内容 */
    private String msgContent;
	@Override
	public Document execute(Document output) throws TransformException {
		// TODO Auto-generated method stub
		String a8ServicesUrl = a8url + "/seeyon/services/" + interfaceName;
		try {
			// 获得客户端
			RPCServiceClient serviceClient = new RPCServiceClient();
			// 可以在该对象中设置服务端的验证信息
			Options options = serviceClient.getOptions();
			EndpointReference targetEPR = new EndpointReference(a8ServicesUrl);
			options.setTo(targetEPR);
			QName opAddEntry = new QName(xmlns, methodName);
			// 参数，如果有多个，继续往后面增加即可，不用指定参数的名称
			Object[] opAddEntryArgs = getParamets(output);
			// 返回参数类型，这个和axis1有点区别
			// invokeBlocking方法有三个参数，其中第一个参数的类型是QName对象，表示要调用的方法名；
			// 第二个参数表示要调用的WebService方法的参数值，参数类型为Object[]；
			// 第三个参数表示WebService方法的返回值类型的Class对象，参数类型为Class[]。
			// 当方法没有参数时，invokeBlocking方法的第二个参数值不能是null，而要使用new Object[]{}
			// 如果被调用的WebService方法没有返回值，应使用RPCServiceClient类的invokeRobust方法，
			// 该方法只有两个参数，它们的含义与invokeBlocking方法的前两个参数的含义相同
			OMElement response = serviceClient.invokeBlocking(opAddEntry,
					opAddEntryArgs);
			String errorMsg="";
			while(response != null){
				errorMsg = response.getText();
				response = response.getFirstElement();
			}
			if (!"".equals(errorMsg)) {
				throw new TransformException("调用消息发送接口异常,接口返回信息：\n" + errorMsg);
			}
		} catch (Exception e) {
			throw new TransformException(e.getLocalizedMessage());
		}
		return output;
	}
	private Object[] getParamets(Document output) throws TransformException {
		Parameters para = output.getContext().getParameters();
		//获取token
		String a8WSToken = getToken(para);
		//设置接收人登录名以及对应链接
		String[] loginNames = new String[paraMap.size()];
		String[] urls = new String[paraMap.size()];
		int i = 0;
		for(Map.Entry<String,String> key:paraMap.entrySet()){
			if(key == null)
				continue;
			loginNames[i] = para.evalString(key.getKey());
			urls[i] = key.getValue();
			i++;
		}
		Object[] msgObj = {a8WSToken,loginNames,msgContent,urls};
		return msgObj;
	}
	
	private String getToken(Parameters para) throws TransformException {
		try {			
			String tokenStr = A8WSTokenUtil.getA8WSToken(userName, password, a8url);
			if("-1".equals(tokenStr)) {
				throw new TransformException("Get A8 WebService Token Fail ！");
			} else {
				return tokenStr;
			}
		} catch (Exception e) {
			throw new TransformException("Get A8 WebService Token Error");
		}
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getInterfaceName() {
		return interfaceName;
	}
	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}
	public String getXmlns() {
		return xmlns;
	}
	public void setXmlns(String xmlns) {
		this.xmlns = xmlns;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getA8url() {
		return a8url;
	}
	public void setA8url(String a8url) {
		this.a8url = a8url;
	}
	public String getMsgContent() {
		return msgContent;
	}
	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}
	public Map<String, String> getParaMap() {
		return paraMap;
	}
	public void setParaMap(Map<String, String> paraMap) {
		this.paraMap = paraMap;
	}

	@Override
	public void evalParaBeforeExe(Parameters parameters) throws Exception {
		userName = parameters.evalString(userName);
		password = parameters.evalString(password);
		a8url = parameters.evalString(a8url);
		msgContent = parameters.evalString(msgContent);
	}
}
