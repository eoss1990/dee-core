package com.seeyon.v3x.dee.adapter.a8;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Document.Element;
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
import java.util.Iterator;
import java.util.List;

public class OrgSyncWriter implements Adapter,InitializingAdapter {

	private final static Log log = LogFactory.getLog(OrgSyncWriter.class);
    /** 接口名称 */
    private String interfaceName;

    /** xmlns */
    private String xmlns;

    /** 方法名称 */
    private String methodName;

    /** 单位名称 */
    private String accountName;

    /** a8WS登录名 */
    private String userName;

    /** 密码 */
    private String password;

    /** url */
    private String a8url;
    
    private int count;
    
	@Override
	public Document execute(Document output) throws TransformException {
		// TODO Auto-generated method stub
		try {
			List<Element> fList = output.getRootElement().getChildren();
			if (fList == null || fList.size()<1) return output;
			int count = 0;//判断是否执行
			for(Element e0:fList) {
				if (e0 == null || e0.getAttribute("totalCount") == null || e0.getAttribute("totalCount").getValue() == null)
					continue;
				String totalValStr = e0.getAttribute("totalCount").getValue().toString();
				if ("0".equals(totalValStr)) continue;
				count++;
			}
			if (count < 1) return output;

			String a8ServicesUrl = this.a8url + "/seeyon/services/" + this.interfaceName;
			// 获得客户端
			RPCServiceClient serviceClient = new RPCServiceClient();
			// 可以在该对象中设置服务端的验证信息
			Options options = serviceClient.getOptions();
			EndpointReference targetEPR = new EndpointReference(a8ServicesUrl);
			options.setTo(targetEPR);
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
			OMElement response = serviceClient.invokeBlocking(opAddEntry,
					opAddEntryArgs);
			String errorMsg="";
			while(response != null){
				errorMsg = response.getText();
				response = response.getFirstElement();
			}
			if (!"".equals(errorMsg)) {
				throw new TransformException("调用组织机构同步接口异常,接口返回信息：\n" + errorMsg);
			}
		} catch (Exception e) {
			throw new TransformException(e.getLocalizedMessage());
		}
		return output;
	}

	private Object[] getParamets(Document output) throws TransformException {
		String a8WSToken = this.getToken();
//		String accountId = getAccountId();
		String doc = makeOrgSyncDoc(output);
		if(doc == null)
			throw new TransformException("来源为空数据集");
		Object[] syncObj = {a8WSToken,this.accountName,doc};
		return syncObj;
	}
	
	private String getToken() throws TransformException {
		try {			
			String tokenStr = A8WSTokenUtil.getA8WSToken(this.userName,this.password,this.a8url);
			if("-1".equals(tokenStr)) {
				throw new TransformException("Get A8 WebService Token Fail ！");
			} else {
				return tokenStr;
			}
		} catch (Exception e) {
			throw new TransformException("Get A8 WebService Token Error");
		}
	}
	
	private String getEleVal(Element e){
		if(e == null || e.getValue() == null)
			return "";
		return e.getValue().toString();
	}
	//拼写组织机构同步xml
	private String makeOrgSyncDoc(Document input) throws TransformException {
//		Document retDoc = null;
		StringBuffer retX = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		try {
			String accountId = getAccountId();
			List<Element> fList = input.getRootElement().getChildren();
			retX.append("<DataPojo type=\"IEOrganizationInfo\"  version=\"1\" isnull=\"false\" valuecount=\"1\">");
			for(Element e0:fList){
				if (e0 == null || e0.getAttribute("totalCount") == null || e0.getAttribute("totalCount").getValue() == null) continue;
				String totalValStr = e0.getAttribute("totalCount").getValue().toString();
				if ("0".equals(totalValStr)) continue;
				//部门同步
				if("depArray".equalsIgnoreCase(e0.getName())){
					retX.append(makeDeptXml(e0,accountId));
				}
				else if("ocupationArray".equalsIgnoreCase(e0.getName())){
					retX.append(makeOcuXml(e0,accountId));
				}
				else if("otypeArray".equalsIgnoreCase(e0.getName())){
					retX.append(makeOTypeXml(e0,accountId));
				}
				else if("personArray".equalsIgnoreCase(e0.getName())){
					retX.append(makeRemenberXml(e0,accountId));
				}
			}
			retX.append("</DataPojo>");
//			retDoc = DocumentUtil.parse(retX.toString());
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			throw e;
//		} catch (DocumentException e) {
//			// TODO Auto-generated catch block
//			throw new TransformException(e);
		}
		return retX.toString();
	}
	
	private String makeDeptXml(Element e,String accountId){
		List<Element> rowList = e.getChildren();
		if(rowList == null || rowList.size() < 1)
			return "";
		StringBuffer retX = new StringBuffer("<DataProperty propertyname=\"depArray\" valuetype=\"10\"  isnull=\"false\" length=\""+rowList.size()+"\">");
		for(Element e1:rowList){
			retX.append("<DataPojo type=\"DepartmentInfoParam_All\"  version=\"1\"  valuecount=\"5\"  isnull=\"false\" >");
			retX.append("<DataProperty propertyname=\"accountId\"  valuetype=\"3\"  value=\""+accountId+"\" />");
			retX.append("<DataProperty propertyname=\"discursion\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("discursion"))+"</DataProperty>");
			count = 0;
			String deptVal = getDeptValue(rowList,e1);
			retX.append("<DataProperty propertyname=\"departmentName\"  valuetype=\"7\"  isnull=\"false\"  length=\""+count+"\" >");
			retX.append(deptVal);
			retX.append("</DataProperty>");
			retX.append("<DataProperty propertyname=\"dep_sort\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("dep_sort"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"departmentNumber\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("departmentNumber"))+"</DataProperty>");
			retX.append("</DataPojo>");
		}
		retX.append("</DataProperty>");
		return retX.toString();
	}
	private String getDeptValue(List<Element> rowList,Element e){
//		String retX = "";
		StringBuffer retX = new StringBuffer("");
		String pDeptId = getEleVal(e.getChild("parentDeptId"));
		count++;
		if(pDeptId == null || "".equals(pDeptId))
			retX.insert(0, "<DataValue isnull=\"false\" >"+getEleVal(e.getChild("departmentName"))+"</DataValue>");
		else{
			retX.insert(0, "<DataValue isnull=\"false\" >"+getEleVal(e.getChild("departmentName"))+"</DataValue>");
//			retX = "<DataValue isnull=\"false\" >"+e.getChild("departmentName").getValue()+"</DataValue>" + retX;
			for(Element e1:rowList){
				if(pDeptId.equals(getEleVal(e1.getChild("departmentId")))){
					retX.insert(0, getDeptValue(rowList,e1));
				}
			}
		}
		return retX.toString();
	}
	//岗位
	private String makeOcuXml(Element e,String accountId){
		List<Element> rowList = e.getChildren();
		if(rowList == null || rowList.size() < 1)
			return "";
		StringBuffer retX = new StringBuffer("<DataProperty propertyname=\"ocupationArray\" valuetype=\"10\"  isnull=\"false\" length=\""+rowList.size()+"\">");
		for(Element e1:rowList){
			retX.append("<DataPojo type=\"OcupationInfoParam_A8_All\"  version=\"1\"  valuecount=\"7\"  isnull=\"false\" >");
			retX.append("<DataProperty propertyname=\"accountId\"  valuetype=\"3\"  value=\""+accountId+"\" />");
			retX.append("<DataProperty propertyname=\"ocupationName\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("ocupationName"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"sortId\"  valuetype=\"0\" value=\""+getEleVal(e1.getChild("sortId"))+"\"/>");
			retX.append("<DataProperty propertyname=\"discursion\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("discursion"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"code\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("code"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"type\"  valuetype=\"3\" value=\""+getEleVal(e1.getChild("type"))+"\"/>");
			retX.append("<DataProperty propertyname=\"departmentArray\"  valuetype=\"10\"  value=\"\"  isnull=\"false\"  length=\"0\" />");
			retX.append("</DataPojo>");
		}
		retX.append("</DataProperty>");
		return retX.toString();
	}
	//职务级别
	private String makeOTypeXml(Element e,String accountId){
		List<Element> rowList = e.getChildren();
		if(rowList == null || rowList.size() < 1)
			return "";
		StringBuffer retX = new StringBuffer("<DataProperty propertyname=\"otypeArray\" valuetype=\"10\"  isnull=\"false\" length=\""+rowList.size()+"\">");
		for(Element e1:rowList){
			retX.append("<DataPojo type=\"OtypeInfoParam_A8_All\"  version=\"1\"  valuecount=\"6\"  isnull=\"false\" >");
			retX.append("<DataProperty propertyname=\"accountId\"  valuetype=\"3\"  value=\""+accountId+"\" />");
			retX.append("<DataProperty propertyname=\"levelId\"  valuetype=\"0\" value=\""+getEleVal(e1.getChild("levelId"))+"\" />");
			retX.append("<DataProperty propertyname=\"discursion\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("discursion"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"code\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("code"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"OTypeName\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("OTypeName"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"parentName\"  valuetype=\"1\"  isnull=\"true\" />");
			retX.append("</DataPojo>");
		}
		retX.append("</DataProperty>");
		return retX.toString();
	}
	//人员
	private String makeRemenberXml(Element e,String accountId){
		List<Element> rowList = e.getChildren();
		if(rowList == null || rowList.size() < 1)
			return "";
		StringBuffer retX = new StringBuffer("<DataProperty propertyname=\"personArray\" valuetype=\"10\"  isnull=\"false\" length=\""+rowList.size()+"\">");
		for(Element e1:rowList){
			retX.append("<DataPojo type=\"PersonInfoParam_All\"  version=\"1\"  valuecount=\"20\"  isnull=\"false\" >");
			retX.append("<DataProperty propertyname=\"accountId\"  valuetype=\"3\"  value=\""+accountId+"\" />");
			retX.append("<DataProperty propertyname=\"otypeName\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("otypeName"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"birthday\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("birthday"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"per_sort\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("per_sort"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"sex\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("sex"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"ocupationName\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("ocupationName"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"secondOcupationName\"  valuetype=\"7\" value=\""+getEleVal(e1.getChild("secondOcupationName"))+"\" isnull=\"false\" length=\"0\" />");
			retX.append("<DataProperty propertyname=\"trueName\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("trueName"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"discursion\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("discursion"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"familyPhone\"  valuetype=\"1\"  isnull=\"true\" >"+getEleVal(e1.getChild("familyPhone"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"officePhone\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("officePhone"))+"</DataProperty>");
			String[] departmentInfo =  createDepartmentInfo(e1);
			retX.append("<DataProperty propertyname=\"departmentName\"  valuetype=\"7\"  isnull=\"false\" length=\"" + departmentInfo[0] + "\">"+departmentInfo[1]+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"passWord\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("passWord"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"staffNumber\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("staffNumber"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"familyAddress\"  valuetype=\"1\"  isnull=\"true\" >"+getEleVal(e1.getChild("familyAddress"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"id\"  valuetype=\"3\" value=\""+getEleVal(e1.getChild("id"))+"\"/>");
			retX.append("<DataProperty propertyname=\"identity\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("identity"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"mobilePhone\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("mobilePhone"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"email\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("email"))+"</DataProperty>");
			retX.append("<DataProperty propertyname=\"loginName\"  valuetype=\"1\"  isnull=\"false\" >"+getEleVal(e1.getChild("loginName"))+"</DataProperty>");
			retX.append("</DataPojo>");
		}
		retX.append("</DataProperty>");
		return retX.toString();
	}
	private String getAccountId() throws TransformException {
		try {
			// 获得客户端
			RPCServiceClient serviceClient = new RPCServiceClient();
			// 可以在该对象中设置服务端的验证信息
			Options options = serviceClient.getOptions();
			EndpointReference targetEPR = new EndpointReference(this.a8url+
					"/seeyon/services/accountService");
			options.setTo(targetEPR);
			// 在创建QName对象时，QName类的构造方法的第一个参数表示WSDL文件的命名空间名，也就是<wsdl:definitions>元素的targetNamespace属性值
			QName opAddEntry = new QName("http://impl.organization.services.v3x.seeyon.com",
					"getAccountId");
			// 参数，如果有多个，继续往后面增加即可，不用指定参数的名称
			Object[] opAddEntryArgs = new Object[] { this.accountName };
			// 返回参数类型，这个和axis1有点区别
			// invokeBlocking方法有三个参数，其中第一个参数的类型是QName对象，表示要调用的方法名；
			// 第二个参数表示要调用的WebService方法的参数值，参数类型为Object[]；
			// 第三个参数表示WebService方法的返回值类型的Class对象，参数类型为Class[]。
			// 当方法没有参数时，invokeBlocking方法的第二个参数值不能是null，而要使用new Object[]{}
			// 如果被调用的WebService方法没有返回值，应使用RPCServiceClient类的invokeRobust方法，
			// 该方法只有两个参数，它们的含义与invokeBlocking方法的前两个参数的含义相同
			OMElement result = serviceClient.invokeBlocking(opAddEntry,
					opAddEntryArgs);
			Iterator ir = result.getFirstElement().getChildElements();
			String retId = "";
			while(ir.hasNext()){
				result = (OMElement) ir.next();
				if("result".equals(result.getLocalName()))
					retId = result.getText();
			}
			if("".equals(retId)){
				throw new TransformException("调用getAccountId接口异常,未取到AccountId!" );
			}
			return retId;
		} catch (Exception e) {
			log.error(e);
			throw new TransformException(e);
		}
	}

	private String[] createDepartmentInfo(Element e1) {
		String[] array = new String[2];
		StringBuilder dptContent = new StringBuilder();

		String name = getEleVal(e1.getChild("departmentName"));
		String[] names = name.split("/");
		int index = 0;
		for (String tmp : names) {
			if (tmp != null && !"".equals(tmp.trim())) {
				dptContent.append("<DataValue isnull=\"false\">").append(tmp).append("</DataValue>");
				index++;
			}
		}

		array[0] = String.valueOf(index == 0 ? 1 : index);
		array[1] = dptContent.toString();

		return array;
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

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
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

	@Override
	public void evalParaBeforeExe(Parameters parameters) throws Exception {
		accountName = parameters.evalString(accountName);
		userName = parameters.evalString(userName);
		password = parameters.evalString(password);
		a8url = parameters.evalString(a8url);
	}
}
