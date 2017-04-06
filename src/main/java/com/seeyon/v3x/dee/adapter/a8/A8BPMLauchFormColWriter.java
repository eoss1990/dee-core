package com.seeyon.v3x.dee.adapter.a8;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.adapter.InitializingAdapter;
import com.seeyon.v3x.dee.util.A8WSTokenUtil;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * DEE内置A8BPM表单流程Writer
 * 
 * @author lilong
 * 
 */
public class A8BPMLauchFormColWriter implements Adapter,InitializingAdapter {
	/**
	 * 缺省从getToken的流程上下文中的A8地址+接口
	 */
	private String a8url;

	/**
	 * 缺省BPMService
	 */
	private String interfaceName;

	/**
	 * 用户名
	 */
	private String userName;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * BPMLauchFormCol缺省值为http://impl.flow.services.v3x.seeyon.com其他接口不同
	 */
	private String xmlns;

	/**
	 * 接口方法名称，本Writer缺省为launchFormCollaboration
	 */
	private String methodName;

	/**
	 * launchFormCollaboration方法内参数集
	 */
	private Map<String, String> paraMap;

	@Override
	public Document execute(Document output) throws TransformException {
		String a8ServicesUrl = this.a8url + "/seeyon/services/" + this.interfaceName;
		/**
		 * 定时器缓存了flow，所以不应该改变初始的paraMap的值
		 * 注释：不设置paraMap的值
		 */
//		String a8WSToken = this.getToken();
//		paraMap.put("data", output.toString());
//		paraMap.put("token", a8WSToken);
		try {
			// 获得客户端
			RPCServiceClient serviceClient = new RPCServiceClient();
			// 可以在该对象中设置服务端的验证信息
			Options options = serviceClient.getOptions();
			EndpointReference targetEPR = new EndpointReference(a8ServicesUrl);
			options.setTo(targetEPR);
			options.setTimeOutInMilliSeconds(86400000L);
			QName opAddEntry = new QName(xmlns, methodName);
			// 参数，如果有多个，继续往后面增加即可，不用指定参数的名称
			Object[] opAddEntryArgs = this.getParamets(output);
			// 返回参数类型，这个和axis1有点区别
			// invokeBlocking方法有三个参数，其中第一个参数的类型是QName对象，表示要调用的方法名；
			// 第二个参数表示要调用的WebService方法的参数值，参数类型为Object[]；
			// 第三个参数表示WebService方法的返回值类型的Class对象，参数类型为Class[]。
			// 当方法没有参数时，invokeBlocking方法的第二个参数值不能是null，而要使用new Object[]{}
			// 如果被调用的WebService方法没有返回值，应使用RPCServiceClient类的invokeRobust方法，
			// 该方法只有两个参数，它们的含义与invokeBlocking方法的前两个参数的含义相同
			OMElement response = serviceClient.invokeBlocking(opAddEntry, opAddEntryArgs);
			Iterator result = response.getFirstElement().getChildElements();
			String errorMsg = "";
			while (result.hasNext()) {
				OMNode node = (OMNode) result.next();
				if (node.getType() == OMNode.ELEMENT_NODE) {
					OMElement omElement = (OMElement) node;
					if ("errorMessage".equals(omElement.getLocalName())) {
						errorMsg = omElement.getText().trim();
					} else if ("result".equals(omElement.getLocalName())) {
						output.getContext().setAttribute("a8bpmFlowId", omElement.getText());
					}
				}
			}
			if (!"".equals(errorMsg)) {
				throw new TransformException("发起A8表单流程异常：" + errorMsg);
			}
		} catch (Exception e) {
			if (e instanceof TransformException) {
				throw (TransformException) e;
			} else {
				throw new TransformException("发起A8表单流程异常：", e);
			}
		}
		return output;
	}

	private Object[] getParamets(Document output) throws TransformException {
		List<Object> para = new LinkedList<Object>();
		for (Entry<String, String> entry : paraMap.entrySet()) {
			if ("data".equals(entry.getKey())) {
				para.add(output.toString());
			}
			else if("token".equals(entry.getKey())){
				para.add(this.getToken());
			}
			else if ("attachments".equals(entry.getKey())) {
				String attStr = entry.getValue();
				if (attStr == null || "".equals(attStr)) {
					para.add("");
				} else {
					List<String> attsList = new ArrayList<String>();
					String[] atts = attStr.split(",");
					for (String att : atts) {
						if ("".equals(att))
							continue;
						attsList.add(att);
					}
					para.add(attsList.toArray());
				}
			} else {
				para.add(entry.getValue());
			}
		}
		return para.size() == 0 ? new Object[] {} : para.toArray();
	}

	private String getToken() throws TransformException {
		try {
			String tokenStr = A8WSTokenUtil.getA8WSToken(userName, password, a8url);
			if ("-1".equals(tokenStr)) {
				throw new TransformException("Get A8 WebService Token Fail ！");
			} else {
				return tokenStr;
			}
		} catch (Exception e) {
			throw new TransformException("Get A8 WebService Token Error");
		}
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

	public Map<String, String> getParaMap() {
		return paraMap;
	}

	public void setParaMap(Map<String, String> paraMap) {
		this.paraMap = paraMap;
	}
	
	public String getA8url() {
		return a8url;
	}

	public void setA8url(String a8url) {
		this.a8url = a8url;
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

	@Override
	public void evalParaBeforeExe(Parameters parameters) throws Exception {
		a8url = parameters.evalString(a8url);
		userName = parameters.evalString(userName);
		password = parameters.evalString(password);

		if (paraMap !=null && paraMap.size()>0){
			for (Entry<String,String> entry:paraMap.entrySet()){
				paraMap.put(entry.getKey(),parameters.evalString(entry.getValue()));
			}
		}
	}

}
