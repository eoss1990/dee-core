package com.seeyon.v3x.dee.adapter.a8;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Document.Element;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.adapter.InitializingAdapter;
import com.seeyon.v3x.dee.util.A8WSTokenUtil;
import com.seeyon.v3x.dee.util.DocumentUtil;
import com.seeyon.v3x.dee.util.ParaMapToXMLUtil;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DEE内置A8WSWriter
 * @author dkywolf
 *
 */
public class A8CommonWSWriter implements Adapter,InitializingAdapter {
	private final static Log log = LogFactory.getLog(A8CommonWSWriter.class);

	private String name;
	private String a8ServicesUrl;           // 缺省从getToken的流程上下文中的A8地址+接口
	private String methodNameUrl;           //
	private String interfaceName;           // 缺省
	private String xmlns;                   // BPMLauchFormCol缺省值为http://impl.flow.services.v3x.seeyon.com其他接口不同
	private String methodName;              // 接口方法名称，本Writer缺省为launchFormCollaboration
	private String userName;
	private String password;                // 获取令牌（token）的用密码
	private String a8url;                   // 获取令牌（token）的a8 ws 地址
	private Map<String, String> paraMap;    // 方法内参数集

	@Override
	public Document execute(Document output) throws TransformException {

		String interfaceName = this.interfaceName;
		String methodName = this.methodName;
		this.a8ServicesUrl = a8url
				+ "/seeyon/services/" + interfaceName;
		this.methodNameUrl = this.a8ServicesUrl + "/" + methodName;
		
		String a8WSToken = "";
		Map<String, String> sqlMap = new LinkedHashMap<String, String>();
		String mainColName="";
		int i=0;
		for(Map.Entry<String, String> entry : paraMap.entrySet()){
			if("#".equalsIgnoreCase(entry.getKey().substring(0,1))){
				if(i == 0){
					mainColName = entry.getValue();
				}
				else{
					sqlMap.put(entry.getKey().substring(1), entry.getValue());
				}
				i++;
			}
		}
		Document newOutput = getSingleDoc(output,sqlMap,mainColName);
//		if(docList == null){
//			deelog.debug("Flow["+output.getContext().getId()+"]---执行A8CommonWSWriter 拆分document异常");
//			return output;
//		}
//		int docLen = docList.size();
//		String flowId = (String)output.getContext().getParameters().getValue("flowId");
//		String syncId = output.getContext().getId();
//		String redoId = (String)output.getContext().getAttribute(Flow.ATTRIBUTE_KEY_REDOID);
//		if (redoId != null && !"null".equals(redoId) && !"".equals(redoId)){
//			RedoBean bean = reDao.findById(redoId);
//			if(bean != null && bean.getSync_id() != null && !"".equals(bean.getSync_id()))
//				syncId = bean.getSync_id();
//		}
//		else{
////			if(docLen > 0)
//			this.setSyncLog(syncId,flowId, SyncState.STATE_FLAG_SUCESS.ordinal());
//		}
//		int errorCount = 0;
//		for(Document oput:docList){
			try{
				if("".equalsIgnoreCase(a8WSToken))
					a8WSToken = A8WSTokenUtil.getA8WSToken(this.userName, this.password, this.a8url);
				paraMap.put("data", DocumentUtil.escapeCDATA(DocumentUtil.toXML(newOutput)));
				String result = this.callA8WSLauch(paraMap, a8WSToken, output.getContext().getParameters());
				if(!"-1".equalsIgnoreCase(result)){
					log.debug("调用A8接口成功");
				}else{
//					errorCount++;
					String errormsg = "调用A8接口"+interfaceName+"."+methodName+"出错。";
					log.error(errormsg);
					throw new TransformException(errormsg);
//					this.insertErrorLog(syncId,errorCount,oput.toString(),flowId, output,errormsg); //在错误时执行插入到数据看
				}				
			}
			catch(TransformException e){
//				errorCount++;
			    String errormsg = "调用A8接口"+interfaceName+"."+methodName+"异常：" + e.getMessage();
				log.error(errormsg);
                throw new TransformException(errormsg);
//				this.insertErrorLog(syncId,errorCount,oput.toString(),flowId, output,e.getMessage()); //在错误时执行插入到数据看
			}
//		}
//		if (redoId != null && !"null".equals(redoId) && !"".equals(redoId)){ //是否重复
//			if(errorCount == 0){
//				reDao.updateState(redoId, RedoBean.STATE_FLAG_SUCESS);
//				reDao.updateCountById(redoId);
//				//检查redo中是否有错误记录，如果没有则修改日志状态
//				RedoBean newBean = new RedoBean();
//				newBean.setSync_id(syncId);
//				newBean.setState_flag(RedoBean.STATE_FLAG_FAILE);
//				if(reDao.findAll(newBean).size() == 0){
//					this.modifySyncLog(syncId, SyncState.STATE_FLAG_SUCESS.ordinal());
//				}
//			}
//			output.getContext().setAttribute("REDO", "1");
//		}
//		else{
//			if(!"".equalsIgnoreCase(syncId) && errorCount != 0){
//				if(errorCount == docLen){
//					this.modifySyncLog(syncId, SyncState.STATE_FLAG_FAILE.ordinal());
//				}
//				else{
//					this.modifySyncLog(syncId, SyncState.STATE_FLAG_PART.ordinal());
//				}
//			}
//		}
		return newOutput;
	}
	
	/**
	 * 拆分Document（将多条数据拆分，返回每条的document）
	 * @return 
	 * @throws org.dom4j.DocumentException
	 */
	private Document getSingleDoc(Document input,Map<String, String> sMap,String mColName){
//		List<Document> docList = new ArrayList<Document>();
//		if(sMap.size() < 1){
//			docList.add(input);
//			return docList;
//		}
		Document retDoc = null;
		String isMain="",colName="",colValue="",subFormName="";
		String sumXml = "";

		try {
			List<Element> fList = input.getRootElement().getChildren();
			List<Element> mainList = new ArrayList<Element>();
			List<Element> subList = new ArrayList<Element>();
			//拆分主从表
			for(Element e0:fList){
				if(!"formExport".equalsIgnoreCase(e0.getName()))
					continue;
				List<Element> gList = e0.getChildren();
				for(Element e1:gList){
					isMain = e1.getName();
					if("values".equalsIgnoreCase(isMain)){
						mainList.add(e1);
					}
					else if("subForms".equalsIgnoreCase(isMain)){
						List<Element> aList = e1.getChildren();
						for(Element e2:aList){
							subList.add(e2);
						}
					}
					else if("summary".equalsIgnoreCase(isMain)){
						sumXml = DocumentUtil.toXML(e1);
					}
				}
			}
			StringBuffer retXml = null;
			StringBuffer innerXml = null;
			Boolean rFlag = false;
			retXml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			retXml.append("<forms version=\"2.1\">");
			for(Element e01:mainList){
				retXml.append("<formExport>");
				retXml.append(sumXml);

				retXml.append(DocumentUtil.toXML(e01));
				retXml.append("<definitions/>\n");

				if(subList.size() > 0){
					retXml.append("<subForms>\n");
					List<Element> sList = e01.getChildren();
					for(Element e02:sList){
						colName = (String)e02.getAttribute("name").getValue();
						if(mColName.equalsIgnoreCase(colName)){
						colValue = (String)e02.getChild("value").getValue();
						for(Map.Entry<String, String> entry : sMap.entrySet()){
							for(Element e11:subList){
								subFormName = (String)e11.getAttribute("name").getValue();
								if(subFormName != null && subFormName.equalsIgnoreCase(entry.getKey())){
									innerXml = new StringBuffer();
									List<Element> eList = e11.getChildren();
									for(Element e12:eList){
										if(!"values".equalsIgnoreCase(e12.getName()))
											continue;
										List<Element> bList = e12.getChildren();
										for(Element e13:bList){
											if(!"row".equalsIgnoreCase(e13.getName()))
												continue;
											List<Element> cList = e13.getChildren();
											for(Element e14:cList){
												String cName = (String)e14.getAttribute("name").getValue();
												if(cName != null && cName.equalsIgnoreCase(entry.getValue())){
													String cValue = (String)e14.getChild("value").getValue();
													if(colValue.equalsIgnoreCase(cValue)){
														rFlag = true;
														break;
													}
												}
											}
											if(rFlag){
												innerXml.append(DocumentUtil.toXML(e12));
												rFlag = false;
											}
										}
									}
									if(innerXml.length() > 0){
										retXml.append("<subForm name=\""+subFormName+"\">");
										retXml.append("<definitions/>");
										retXml.append(innerXml.toString());
										retXml.append("</subForm>\n");
									}
								}
							}
						}
					}
					}
					retXml.append("</subForms>");
				}
				retXml.append("</formExport>");
			}
			retXml.append("</forms>");
			retDoc = DocumentUtil.parse(retXml.toString());
		} catch (DocumentException e) {
			log.error("拆分Document出错：" + e);
			retDoc = null;
		}
		return retDoc;
	}

	/**
	 * 调用A8WebService并返回的信息
	 * @return
	 * @throws org.dom4j.DocumentException
	 */
	private String callA8WSLauch(Map<String, String> paraMap,
			String a8WSToken,Parameters params) throws TransformException {
		try {
			// 获得客户端
			RPCServiceClient serviceClient = new RPCServiceClient();
			// 可以在该对象中设置服务端的验证信息
			Options options = serviceClient.getOptions();
			EndpointReference targetEPR = new EndpointReference(this.a8ServicesUrl);
			options.setTo(targetEPR);
			options.setTimeOutInMilliSeconds(86400000L);
			// 在创建QName对象时，QName类的构造方法的第一个参数表示WSDL文件的命名空间名，也就是<wsdl:definitions>元素的targetNamespace属性值
			QName opAddEntry = new QName(this.xmlns,
					this.methodName);
			// 参数，如果有多个，继续往后面增加即可，不用指定参数的名称
//			Object[] opAddEntryArgs = new Object[] { a8WSToken, password };
			// 返回参数类型，这个和axis1有点区别
			// invokeBlocking方法有三个参数，其中第一个参数的类型是QName对象，表示要调用的方法名；
			// 第二个参数表示要调用的WebService方法的参数值，参数类型为Object[]；
			// 第三个参数表示WebService方法的返回值类型的Class对象，参数类型为Class[]。
			// 当方法没有参数时，invokeBlocking方法的第二个参数值不能是null，而要使用new Object[]{}
			// 如果被调用的WebService方法没有返回值，应使用RPCServiceClient类的invokeRobust方法，
			// 该方法只有两个参数，它们的含义与invokeBlocking方法的前两个参数的含义相同
			OMElement result = serviceClient.invokeBlocking(opAddEntry,
					ParaMapToXMLUtil.paraCommonMapToArr(paraMap, a8WSToken, params));
			String retStr = "";
			while(result != null){
				retStr = result.getText();
				result = result.getFirstElement();
			}
			return retStr;
		} catch (Exception e) {
			log.error(e);
			throw new TransformException(e);
		}
	}
	
	public String getA8ServicesUrl() {
		return a8ServicesUrl;
	}

	public void setA8ServicesUrl(String a8ServicesUrl) {
		this.a8ServicesUrl = a8ServicesUrl;
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

	public Map<String, String> getParaMap() {
		return paraMap;
	}

	public void setParaMap(Map<String, String> paraMap) {
		this.paraMap = paraMap;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void evalParaBeforeExe(Parameters parameters) throws Exception {
		methodName = parameters.evalString(methodName);
		interfaceName = parameters.evalString(interfaceName);
	}
}

