package com.seeyon.v3x.dee.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

/**
 * WebService客户端调用工具类
 *
 * @author zhangfb
 */
public class WebServiceClientUtil {

	private final static Log log = LogFactory.getLog(WebServiceClientUtil.class);

	/**
	 * 使用rpc的方式调用WebService
	 *
	 * @param address  地址
	 * @param ns       命名空间
	 * @param method   方法名
	 * @param params   参数
	 * @param retTypes 返回类型
     * @param timeout  超时时间
	 * @return Object[]
	 * @throws org.apache.axis2.AxisFault
	 */
	@SuppressWarnings("rawtypes")
	public static Object[] invokeByRPC(String address,
									   String ns, String method, Object[] params,
									   Class[] retTypes, long timeout) throws AxisFault {
		return invokeByRPC(address, ns, method, params, retTypes, null, null, timeout);
	}

    public static Object[] invokeByRPC(String address,
                                       String ns, String method, Object[] params,
                                       Class[] retTypes, String userName, String password, long timeout) throws AxisFault {
        try {
            RPCServiceClient serviceClient = new RPCServiceClient();
            Options opts = serviceClient.getOptions();

            // 确定目标服务地址
            opts.setTo(new EndpointReference(address));
            // 确定调用方法
            opts.setAction(ns + method);
            opts.setTimeOutInMilliSeconds(timeout);

            // 确定命名空间和方法
            QName qName = new QName(ns, method);

            // 身份验证
            if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
                String[] usrName = userName.split(":");
                String[] pssWord = password.split(":");
                if (usrName.length != 3 || pssWord.length != 3 || !StringUtils.equals(usrName[0], pssWord[0])) {
                    throw new AxisFault("SoapHeader用户名密码格式错误！");
                }

                OMFactory omFactory = OMAbstractFactory.getOMFactory();
                OMNamespace omNs = omFactory.createOMNamespace(ns, "");
                OMElement header = omFactory.createOMElement(usrName[0], omNs);
                OMElement ome_user = omFactory.createOMElement(usrName[1], omNs);
                ome_user.setText(usrName[2]);
                header.addChild(ome_user);
                OMElement ome_pass = omFactory.createOMElement(pssWord[1], omNs);
                ome_pass.setText(pssWord[2]);
                header.addChild(ome_pass);
                serviceClient.addHeader(header);
            }
            // 确定返回类型
            if (retTypes == null || retTypes.length < 1 || void.class.equals(retTypes[0])) {
                serviceClient.invokeRobust(qName, params);
                return null;
            } else {
                // 得到返回结果，是一个数组
                return serviceClient.invokeBlocking(
                        qName, params, retTypes);
            }
        } catch (AxisFault e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

	/**
	 * 使用document的方式调用WebService
	 *
	 * @param address 地址
	 * @param ns      命名空间
	 * @param method  方法
	 * @param params  参数
	 * @return org.apache.axiom.om.OMElement
	 * @throws org.apache.axis2.AxisFault
	 */
	public static OMElement invokeByDocument(String address,
											 String ns, String method,
											 Map<String, Object> params) throws AxisFault {
		try {
			ServiceClient serviceClient = new ServiceClient();
			Options opts = new Options();
			// 确定目标服务地址
			opts.setTo(new EndpointReference(address));
			// 确定调用方法
			opts.setAction(ns + method);
			opts.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			serviceClient.setOptions(opts);
			// 发送请求并得到返回结果
			return serviceClient.sendReceive(getParamsOMElement(ns, method, params));
		} catch (AxisFault e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 生成OMElement
	 *
	 * @param ns     命名空间
	 * @param method 方法名
	 * @param params 参数
	 * @return org.apache.axiom.om.OMElement
	 */
	private static OMElement getParamsOMElement(String ns,
												String method, Map<String, Object> params) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		// 指定命名空间
		OMNamespace omNs = fac.createOMNamespace(ns, "");
		// 指定方法
		OMElement omMethod = fac.createOMElement(method, omNs);
		// 指定方法的参数
		for (Entry<String, Object> entry : params.entrySet()) {
			if (entry.getKey() == null || "".equals(entry.getKey().trim())) {
				continue;
			}
			OMElement value = fac.createOMElement(entry.getKey(), omNs);
			value.setText(entry.getValue() + "");
			omMethod.addChild(value);
		}
		return omMethod;
	}

	/**
	 * 使用cxf调用WebService
	 *
	 * @param address 地址
	 * @param method  方法
	 * @param params  参数
	 * @return Object[]
	 * @throws Exception
	 */
	public static Object[] invokeByCxf(String address, String method, Object[] params) throws Exception {
		return invokeByCxf(address, method, params, null);
	}

	/**
	 * 使用cxf调用WebService
	 *
	 * @param address     地址
	 * @param method      方法
	 * @param params      参数
	 * @param classLoader 类加载器
	 * @return Object[]                                                              Web
	 * @throws Exception
	 */
	public static Object[] invokeByCxf(String address, String method, Object[] params, ClassLoader classLoader) throws Exception {
        try {
            // CXF动态调用WS，调用CXFBusFactory而不使用SpringBusFactory
            JaxWsDynamicClientFactory dcf =
                    JaxWsDynamicClientFactory.newInstance(
                            BusFactory.newInstance(
                                    BusFactory.DEFAULT_BUS_FACTORY).createBus());

            Client client;
            if (classLoader != null) {
                client = dcf.createClient(address + "?wsdl", classLoader);
            } else {
                client = dcf.createClient(address + "?wsdl");
            }

            HTTPConduit http = (HTTPConduit) client.getConduit();
            HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
            httpClientPolicy.setConnectionTimeout(36000);
            httpClientPolicy.setAllowChunking(false);
            httpClientPolicy.setReceiveTimeout(32000);
            http.setClient(httpClientPolicy);

            return client.invoke(method, params);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

	/**
	 * 调用对象的方法
	 *
	 * @param owner      对象
	 * @param methodName 方法名
	 * @return 返回对象
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Object getMethod(Object owner, String methodName) throws Exception {
		Class ownerClass = owner.getClass();
		Method method = ownerClass.getMethod(methodName);
		method.setAccessible(true);
		return method.invoke(owner);
	}
}
