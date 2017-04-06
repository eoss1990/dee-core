package com.seeyon.v3x.dee.adapter.sap.webservice;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.adapter.InitializingAdapter;
import com.seeyon.v3x.dee.datasource.XMLDataSource;
import com.seeyon.v3x.dee.util.DocumentUtil;
import com.seeyon.v3x.dee.util.PageUtil;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SapWSProcessor implements Adapter,InitializingAdapter {

	public static final String RETURNTYPE_STRING = "string";
	public static final String RETURNTYPE_NUMBER = "number";
	public static final String RETURNTYPE_VOID = "void";

	private final static Log log = LogFactory.getLog(SapWSProcessor.class);
	private String serviceurl;
	private String namespace;
	private String method;
	private String username;
	private String password;
	private String returnType;
	private Map<String, Object> parameter;

	public SapWSProcessor() {
		parameter = new LinkedHashMap<String, Object>();
	}

	public Document execute(Document output) throws TransformException {
		try {
			ServiceClient sender = new ServiceClient();
			sender.setOptions(buildOptions());
			HttpTransportProperties.Authenticator basicauth = new HttpTransportProperties.Authenticator();
			basicauth.setUsername(username);
			basicauth.setPassword(password);
			sender.getOptions().setProperty(HTTPConstants.AUTHENTICATE,
					basicauth);
			OMElement resultOMElement = sender
					.sendReceive(getParametsOMElement(output));
			String result = StringEscapeUtils.unescapeXml(resultOMElement
					.toString());
			log.debug("WS " + serviceurl + ":" + method + "返回值为:" + result);
			output.getContext().setAttribute("WSResult", result);
			
			try{
				result = result.substring(result.indexOf("<root>"),
						result.indexOf("</root>") + 7);
				Document newInput = new XMLDataSource(result).parse();
				newInput = PageUtil.pageDocument(newInput, output.getContext().getParameters());
				output = DocumentUtil.merge(output, newInput);
			}
			catch(Exception e){
				//转换失败，继续执行下面任务	
				log.error("返回字符串转换document失败:"+e.getMessage()+"\n"+result, e);
			}
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new TransformException("调用WS" + serviceurl + ":" + method
					+ "异常：", e);
		}
		return output;
	}

	private Object[] getParamets(Document output) throws TransformException {
		List<Object> para = new LinkedList<Object>();
		for (Entry<String, Object> entry : parameter.entrySet()) {
			if (entry.getKey() == null || "".equals(entry.getKey().trim()))
				continue;
			para.add(entry.getValue().toString());
		}
		return para.size() == 0 ? new Object[] {} : para.toArray();
	}

	private OMElement getParametsOMElement(Document output)
			throws TransformException {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = fac.createOMNamespace(this.namespace, "tns");
		OMElement data = fac.createOMElement(this.method, omNs);
		for (Entry<String, Object> entry : parameter.entrySet()) {
			if (entry.getKey() == null || "".equals(entry.getKey().trim()))
				continue;
			OMElement inner = fac.createOMElement(entry.getKey(), null);
			inner.setText(entry.getValue().toString());
			data.addChild(inner);
		}
		return data;
	}

	private Options buildOptions() {
		Options options = new Options();
		options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		options.setTo(new EndpointReference(this.serviceurl));
		// options.setProperty(propertyKey, property)

		// enabling MTOM in the client side
		// options.setProperty(Constants.Configuration.ENABLE_MTOM,
		// Constants.VALUE_TRUE);
		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		return options;
	}

	public static OMElement plainString2OMElement(String targetNameSpace,
			String prefix, String method, String retString) {
		// 获取OMNamespace
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = fac.createOMNamespace(targetNameSpace, prefix);

		OMElement resp = fac.createOMElement(method, omNs);
		resp.setText(retString);
		return resp;
	}

	public String getMethod() {
		return method;
	}

	public Map<String, Object> getParameter() {
		return parameter;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getServiceurl() {
		return serviceurl;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public void setServiceurl(String serviceurl) {
		this.serviceurl = serviceurl;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setParameter(Map<String, Object> parameter) {
		this.parameter = parameter;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void evalParaBeforeExe(Parameters parameters) throws Exception {
		serviceurl = parameters.evalString(serviceurl);
		namespace = parameters.evalString(namespace);
		username = parameters.evalString(username);
		password = parameters.evalString(password);
		method = parameters.evalString(method);
		if (parameter != null&&parameter.size() > 0) {
			for (Entry<String, Object> entry : parameter.entrySet()) {
				parameter.put(entry.getKey(),parameters.evalString(entry.getValue().toString()));
			}
		}
	}
}
