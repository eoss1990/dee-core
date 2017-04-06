package com.seeyon.v3x.dee.common.a8rest.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.common.a8rest.model.RestFunctionBean;
import com.seeyon.v3x.dee.common.a8rest.model.RestParam;
import com.seeyon.v3x.dee.util.rest.CTPRestClient;
import com.seeyon.v3x.dee.util.rest.CTPServiceClientManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rest工具类
 *
 * @author zhangfb
 */
public class RestUtil {
    public static final String PATH_PARAM = "PathParam";
    public static final String BODY_PARAM = "BodyParam";

    private static int count = 0;

    private final static Log log = LogFactory.getLog(RestUtil.class);

    /**
     * 将字符串解析成RestParam类对象<p/>  兼容老版本
     * 字符串格式如下：paramName|paramType|paramValue|isRequired
     *
     * @param paramStr Rest变量字符串
     * @return 解析后的RestParam对象
     */
//    public static RestParam parseToRestParam(String paramStr) {
//        if (StringUtils.isBlank(paramStr)) {
//            return null;
//        }
//        String[] pars = paramStr.split("\\|");
//        if (pars == null || pars.length < 4) return null;
//        RestParam restParam = new RestParam();
//        restParam.setParamName(pars[0]);
//        restParam.setParamType(pars[1]);
//        restParam.setShowValue(pars[2]);
//        if (pars.length > 4){
//            restParam.setIsRequired(pars[3]);
//            restParam.setParamValue(pars[4]);
//        }
//        else {
//            restParam.setIsRequired("0"); //默认必填
//            restParam.setParamValue(pars[3]);
//        }
//
//        return restParam;
//    }
    public static RestParam parseToRestParam(String paramStr) {
        if (StringUtils.isBlank(paramStr)) {
            return null;
        }
        //paramName|paramType|showValue|paramValue|isRequired
        // 取得paramName
        int paramNameIndex = paramStr.indexOf('|', 0);
        String paramName = paramStr.substring(0, paramNameIndex);

        // 取得paramType
        int paramTypeIndex = paramStr.indexOf('|', paramNameIndex + 1);
        String paramType = paramStr.substring(paramNameIndex + 1, paramTypeIndex);

        // 取得显示名称
        int showValueIndex = paramStr.indexOf('|', paramTypeIndex + 1);
        String showValue = paramStr.substring(paramTypeIndex + 1, showValueIndex);

        // 取得paramValue
        int paramIndex = paramStr.indexOf('|', showValueIndex + 1);

        // 是否必填
        String requiredValue = "0";
        String paramValue = "";
        if (paramIndex > -1){
            paramValue = paramStr.substring(showValueIndex + 1, paramIndex);
            requiredValue = paramStr.substring(paramIndex + 1);
        }
        else {
            paramValue =  paramStr.substring(showValueIndex + 1);
        }


        RestParam restParam = new RestParam();
        restParam.setParamName(paramName);
        restParam.setParamType(paramType);
        restParam.setShowValue(showValue);
        restParam.setParamValue(paramValue);
        restParam.setIsRequired(requiredValue);

        return restParam;
    }

    /**
     * 找出字符串中所有大括号括起来的变量
     *
     * @param content 内容
     * @return 变量列表
     */
    public static List<String> matchParams(String content) {
        List<String> list = new ArrayList<String>();

        if (content != null) {
            Pattern p = Pattern.compile(".+?(\\{.+?\\})");
            Matcher m = p.matcher(content);
            while (m.find()) {
                String x = m.group(1);
                if (x.length() > 0) {
                    list.add(x.substring(1, x.length() - 1));
                }
            }
        }

        return list;
    }
    /**
     * 找出字符串中所有大括号括起来的变量
     *
     * @param content 内容
     * @return 变量列表
     */
    public static Map matchPathParams(String content) {
        Map pathMap = new HashMap<String,String>();
        if (content != null) {
            Pattern p = Pattern.compile(".+?(\\{.+?\\})");
            Matcher m = p.matcher(content);
            while (m.find()) {
                String x = m.group(1);
                if (x.length() > 0) {
                    x = x.substring(1, x.length() - 1);
                    String[] retParm = x.split(":");
                    if (retParm == null || retParm.length <1) continue;
                    if (retParm.length>1){
                        pathMap.put(retParm[0],retParm[1]);
                    }
                    else {
                        pathMap.put(retParm[0],retParm[0]);
                    }
                }
            }
        }

        return pathMap;
    }



    /**
     * 根据单位名称，取得单位ID
     *
     * @param client      Rest Client
     * @param accountName 单位名称
     * @return 单位ID
     */
    public static Long getAccountIdByName(CTPRestClient client, String accountName) {
        String json = client.get("/account/name/" + encode(accountName), String.class, MediaType.APPLICATION_JSON);
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map map = mapper.readValue(json, Map.class);
            return (Long) map.get("orgAccountId");
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * 编码
     * @param s
     * @return
     */
    public static String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Rest结果处理方法
     *
     * @param functionBean 方法Bean
     * @param result       结果
     * @param resultType   结果类型JSON/XML
     * @return 转换后的Document
     */
    public static com.seeyon.v3x.dee.Document dealResult(RestFunctionBean functionBean, Object result, String resultType)
            throws TransformException {
        String dealClass = functionBean.getServiceBean().getDealClass();
        String dealMethod = functionBean.getDealMethod();

        // 校验处理类及方法是否存在
        if (dealClass != null && dealMethod != null) {
            try {
                Method method = Class.forName(dealClass).getMethod(dealMethod,
                        Class.forName(functionBean.getReturnType()), String.class);
                return (com.seeyon.v3x.dee.Document) method.invoke(null, result, resultType);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
                throw new TransformException(e.getLocalizedMessage(), e);
            }
        }
        return null;
    }

    /**
     * 校验后，创建CTPRestClient
     *
     * @param address       A8地址
     * @param adminUserName 用户名
     * @param adminPassword 密码
     * @return
     * @throws com.seeyon.v3x.dee.TransformException
     */
    public static CTPRestClient createCtpClient(String address,
                                                String adminUserName, String adminPassword) throws TransformException {
        CTPServiceClientManager clientManager = CTPServiceClientManager.getInstance(address);
        CTPRestClient client = clientManager.getRestClient();       // REST 动态客户机

        if (StringUtils.isBlank(address)) {
            throw new TransformException("REST权限校验失败：地址不能为空！");
        }

        if (StringUtils.isBlank(adminUserName) || StringUtils.isBlank(adminPassword)) {
            throw new TransformException("REST权限校验失败：用户名或密码不能为空！");
        }

        boolean authResult;
        try {
            authResult = client.authenticate(adminUserName, adminPassword);
        } catch (Exception e) {
            throw new TransformException("REST权限校验失败，地址错误：" + address);
        }

        // 只有用户名和密码校验通过，才能进行正式REST请求
        if (!authResult) {
            throw new TransformException("REST权限校验失败：用户名：" + adminUserName + "，密码：" + adminPassword);

        }
        return client;
    }

    /**
     * 拼写组织机构同步xml
     *
     * @param input
     * @param accountId
     * @return
     * @throws com.seeyon.v3x.dee.TransformException
     */
    public static String makeOrgSyncDoc(Document input, String accountId) throws TransformException {
        StringBuffer retX = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        try {
            List<Document.Element> fList = input.getRootElement().getChildren();
            retX.append("<DataPojo type=\"IEOrganizationInfo\"  version=\"1\" isnull=\"false\" valuecount=\"1\">");
            for (Document.Element e0 : fList) {
                if ("depArray".equalsIgnoreCase(e0.getName())) {                    // 部门同步
                    retX.append(makeDeptXml(e0, accountId));
                } else if ("ocupationArray".equalsIgnoreCase(e0.getName())) {       // 岗位同步
                    retX.append(makeOcuXml(e0, accountId));
                } else if ("otypeArray".equalsIgnoreCase(e0.getName())) {           // 职务级别同步
                    retX.append(makeOTypeXml(e0, accountId));
                } else if ("personArray".equalsIgnoreCase(e0.getName())) {          // 人员同步
                    retX.append(makeRemenberXml(e0, accountId));
                }
            }
            retX.append("</DataPojo>");
        } catch (Exception e) {
            throw new TransformException(e);
        }
        return retX.toString();
    }

    /**
     * 部门
     *
     * @param e
     * @param accountId
     * @return
     */
    private static String makeDeptXml(Document.Element e, String accountId) {
        List<Document.Element> rowList = e.getChildren();
        if (rowList == null || rowList.size() < 1) {
            return "";
        }
        StringBuffer retX = new StringBuffer("<DataProperty propertyname=\"depArray\" valuetype=\"10\"  isnull=\"false\" length=\"" + rowList.size() + "\">");
        for (Document.Element e1 : rowList) {
            retX.append("<DataPojo type=\"DepartmentInfoParam_All\"  version=\"1\"  valuecount=\"5\"  isnull=\"false\" >");
            retX.append("<DataProperty propertyname=\"accountId\"  valuetype=\"3\"  value=\"" + accountId + "\" />");
            retX.append("<DataProperty propertyname=\"discursion\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("discursion")) + "</DataProperty>");
            count = 0;
            String deptVal = getDeptValue(rowList, e1);
            retX.append("<DataProperty propertyname=\"departmentName\"  valuetype=\"7\"  isnull=\"false\"  length=\"" + count + "\" >");
            retX.append(deptVal);
            retX.append("</DataProperty>");
            retX.append("<DataProperty propertyname=\"dep_sort\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("dep_sort")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"departmentNumber\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("departmentNumber")) + "</DataProperty>");
            retX.append("</DataPojo>");
        }
        retX.append("</DataProperty>");
        return retX.toString();
    }

    /**
     * 单个部门
     *
     * @param rowList
     * @param e
     * @return
     */
    private static String getDeptValue(List<Document.Element> rowList, Document.Element e) {
        StringBuffer retX = new StringBuffer("");
        String pDeptId = getEleVal(e.getChild("parentDeptId"));
        count++;
        if (pDeptId == null || "".equals(pDeptId)) {
            retX.insert(0, "<DataValue isnull=\"false\" >" + getEleVal(e.getChild("departmentName")) + "</DataValue>");
        } else {
            retX.insert(0, "<DataValue isnull=\"false\" >" + getEleVal(e.getChild("departmentName")) + "</DataValue>");
            for (Document.Element e1 : rowList) {
                if (pDeptId.equals(getEleVal(e1.getChild("departmentId")))) {
                    retX.insert(0, getDeptValue(rowList, e1));
                }
            }
        }
        return retX.toString();
    }

    /**
     * 岗位
     *
     * @param e
     * @param accountId
     * @return
     */
    private static String makeOcuXml(Document.Element e, String accountId) {
        List<Document.Element> rowList = e.getChildren();
        if (rowList == null || rowList.size() < 1) {
            return "";
        }
        StringBuffer retX = new StringBuffer("<DataProperty propertyname=\"ocupationArray\" valuetype=\"10\"  isnull=\"false\" length=\"" + rowList.size() + "\">");
        for (Document.Element e1 : rowList) {
            retX.append("<DataPojo type=\"OcupationInfoParam_A8_All\"  version=\"1\"  valuecount=\"7\"  isnull=\"false\" >");
            retX.append("<DataProperty propertyname=\"accountId\"  valuetype=\"3\"  value=\"" + accountId + "\" />");
            retX.append("<DataProperty propertyname=\"ocupationName\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("ocupationName")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"sortId\"  valuetype=\"0\" value=\"" + getEleVal(e1.getChild("sortId")) + "\"/>");
            retX.append("<DataProperty propertyname=\"discursion\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("discursion")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"code\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("code")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"type\"  valuetype=\"3\" value=\"" + getEleVal(e1.getChild("type")) + "\"/>");
            retX.append("<DataProperty propertyname=\"departmentArray\"  valuetype=\"10\"  value=\"\"  isnull=\"false\"  length=\"0\" />");
            retX.append("</DataPojo>");
        }
        retX.append("</DataProperty>");
        return retX.toString();
    }

    /**
     * 职务级别
     *
     * @param e
     * @param accountId
     * @return
     */
    private static String makeOTypeXml(Document.Element e, String accountId) {
        List<Document.Element> rowList = e.getChildren();
        if (rowList == null || rowList.size() < 1) {
            return "";
        }
        StringBuffer retX = new StringBuffer("<DataProperty propertyname=\"otypeArray\" valuetype=\"10\"  isnull=\"false\" length=\"" + rowList.size() + "\">");
        for (Document.Element e1 : rowList) {
            retX.append("<DataPojo type=\"OtypeInfoParam_A8_All\"  version=\"1\"  valuecount=\"6\"  isnull=\"false\" >");
            retX.append("<DataProperty propertyname=\"accountId\"  valuetype=\"3\"  value=\"" + accountId + "\" />");
            retX.append("<DataProperty propertyname=\"levelId\"  valuetype=\"0\" value=\"" + getEleVal(e1.getChild("levelId")) + "\" />");
            retX.append("<DataProperty propertyname=\"discursion\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("discursion")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"code\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("code")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"OTypeName\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("OTypeName")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"parentName\"  valuetype=\"1\"  isnull=\"true\" />");
            retX.append("</DataPojo>");
        }
        retX.append("</DataProperty>");
        return retX.toString();
    }

    /**
     * 人员
     *
     * @param e
     * @param accountId
     * @return
     */
    private static String makeRemenberXml(Document.Element e, String accountId) {
        List<Document.Element> rowList = e.getChildren();
        if (rowList == null || rowList.size() < 1) {
            return "";
        }
        StringBuffer retX = new StringBuffer("<DataProperty propertyname=\"personArray\" valuetype=\"10\"  isnull=\"false\" length=\"" + rowList.size() + "\">");
        for (Document.Element e1 : rowList) {
            retX.append("<DataPojo type=\"PersonInfoParam_All\"  version=\"1\"  valuecount=\"20\"  isnull=\"false\" >");
            retX.append("<DataProperty propertyname=\"accountId\"  valuetype=\"3\"  value=\"" + accountId + "\" />");
            retX.append("<DataProperty propertyname=\"otypeName\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("otypeName")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"birthday\"  valuetype=\"1\"  isnull=\"true\" >" + getEleVal(e1.getChild("birthday")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"per_sort\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("per_sort")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"sex\"  valuetype=\"1\"  isnull=\"true\" >" + getEleVal(e1.getChild("sex")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"ocupationName\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("ocupationName")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"secondOcupationName\"  valuetype=\"7\" value=\"" + getEleVal(e1.getChild("secondOcupationName")) + "\" isnull=\"false\" length=\"0\" />");
            retX.append("<DataProperty propertyname=\"trueName\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("trueName")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"discursion\"  valuetype=\"1\"  isnull=\"true\" >" + getEleVal(e1.getChild("discursion")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"familyPhone\"  valuetype=\"1\"  isnull=\"true\" >" + getEleVal(e1.getChild("familyPhone")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"officePhone\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("officePhone")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"departmentName\"  valuetype=\"7\"  isnull=\"false\" length=\"1\"><DataValue isnull=\"false\" >" + getEleVal(e1.getChild("departmentName")) + "</DataValue></DataProperty>");
            retX.append("<DataProperty propertyname=\"passWord\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("passWord")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"staffNumber\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("staffNumber")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"familyAddress\"  valuetype=\"1\"  isnull=\"true\" >" + getEleVal(e1.getChild("familyAddress")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"id\"  valuetype=\"3\" value=\"" + getEleVal(e1.getChild("id")) + "\"/>");
            retX.append("<DataProperty propertyname=\"identity\"  valuetype=\"1\"  isnull=\"true\" >" + getEleVal(e1.getChild("identity")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"mobilePhone\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("mobilePhone")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"email\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("email")) + "</DataProperty>");
            retX.append("<DataProperty propertyname=\"loginName\"  valuetype=\"1\"  isnull=\"false\" >" + getEleVal(e1.getChild("loginName")) + "</DataProperty>");
            retX.append("</DataPojo>");
        }
        retX.append("</DataProperty>");
        return retX.toString();
    }

    private static String getEleVal(Document.Element e) {
        if (e == null || e.getValue() == null) {
            return "";
        }
        return e.getValue().toString();
    }
}
