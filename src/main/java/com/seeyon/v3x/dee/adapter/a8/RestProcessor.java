package com.seeyon.v3x.dee.adapter.a8;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.common.a8rest.RestServiceManager;
import com.seeyon.v3x.dee.common.a8rest.model.RestFunctionBean;
import com.seeyon.v3x.dee.common.a8rest.model.RestParam;
import com.seeyon.v3x.dee.common.a8rest.util.RestUtil;
import com.seeyon.v3x.dee.util.DocumentUtil;
import com.seeyon.v3x.dee.util.rest.CTPRestClient;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rest适配器
 *
 * @author zhangfb
 */
public class RestProcessor implements Adapter {
    private final static Log log = LogFactory.getLog(RestProcessor.class);
    private final static String SEND_LOGIN_NAME = "流程发起人登录名";
    /**
     * 显示名称
     */
    private String name;

    /**
     * 权限校验的用户名
     */
    private String adminUserName;

    /**
     * 权限校验的密码
     */
    private String adminPassword;

    /**
     * 地址，如：http://localhost/seeyon/rest/
     */
    private String address;

    /**
     * 服务ID
     */
    private Integer serviceId;

    /**
     * 服务下的方法ID
     */
    private Integer functionId;

    /**
     * 返回值类型
     */
    private String responseType;

    /**
     * 返回值名称
     */
    private String responseName;

    /**
     * 是否合并到Document
     */
    private String mergeToDocument;

    /**
     * 参数列表
     */
    private Map<String, String> paramMap;

    /**
     * 参数列表，表名+外键
     */
    private Map<String, String> keyMap;

    private int count;

    @Override
    public Document execute(Document input) throws TransformException {
        // REST动态客户机
        CTPRestClient client = RestUtil.createCtpClient(address, adminUserName, adminPassword);

        // 获取REST方法信息
        RestFunctionBean functionBean = RestServiceManager.getInstance().getFunctionBean(serviceId, functionId);
        if (functionBean == null) {
            throw new TransformException("REST方法获取失败，服务ID：" + serviceId + "，方法ID:" + functionId);
        }

        Object retResult = null;
        try {        // 获取Rest变量列表
            List<RestParam> restParams = toRestParams();
            // 将方法路径用变量替换
            String functionPath = replaceParam(functionBean.getFunctionPath(),
                    restParams, input, RestUtil.PATH_PARAM, client);
            Object bodyTemplate = null;
            String type = MediaType.APPLICATION_JSON;
            if (functionBean.getBodyBean() != null) {
                type = functionBean.getBodyBean().getBodyType();
                // 如果是组织模型导入
                if (functionBean.getBodyBean().getBodyId() == 5) {
                    if (restParams == null || restParams.size() <= 0) {
                        throw new TransformException("导入组织模型数据失败，参数错误！");
                    }
                    String paramValue = restParams.get(0).getParamValue();
                    Long accountId = RestUtil.getAccountIdByName(client, paramValue);
                    if (accountId == null) {
                        throw new TransformException("导入组织模型数据失败，单位不存在：" + paramValue);
                    }
                    bodyTemplate = RestUtil.makeOrgSyncDoc(input, accountId.toString());
                }
                else if (functionBean.getBodyBean().getBodyId() == 201){
                    //获取token
                    if (StringUtils.isBlank(client.getToken())){
                        throw new TransformException("获取token为空");
                    }
                    //发起流程表单
                    bodyTemplate = getSingleDocs(input,restParams,client.getToken());
                }
                else {
                    bodyTemplate = replaceParam(functionBean.getBodyBean().getBodyTemplate(),
                            restParams, input, RestUtil.BODY_PARAM, client);
                }
            }
            log.info("function:"+functionBean.getFunctionType()+",bodyTemplate:"+bodyTemplate);
            log.info("functionPath:"+functionPath+",ReturnType:"+functionBean.getReturnType()+",type:"+type+",responseType"+responseType);
            if ("GET".equals(functionBean.getFunctionType())) {           // 查询
                retResult = client.get(functionPath, Class.forName(functionBean.getReturnType()), responseType);
            } else if ("POST".equals(functionBean.getFunctionType())) {   // 新增
                retResult = client.post(functionPath, bodyTemplate, Class.forName(functionBean.getReturnType()), type, responseType);
            } else if ("PUT".equals(functionBean.getFunctionType())) {    // 修改
                retResult = client.put(functionPath, bodyTemplate, Class.forName(functionBean.getReturnType()), type, responseType);
            } else if ("DELETE".equals(functionBean.getFunctionType())) { // 删除
                retResult = client.delete(functionPath, bodyTemplate, Class.forName(functionBean.getReturnType()), type, responseType);
            }
            input.getContext().getParameters().add(responseName, retResult);

        } catch (Exception e) {
            throw new TransformException(e.getLocalizedMessage(), e);
        }

        if ("true".equals(mergeToDocument)) {
            // 对结果进行解析
            Document document = RestUtil.dealResult(functionBean, retResult, responseType);
            if (document != null) {
                input = DocumentUtil.merge(input, document);
            }
        }
        return input;
    }

    /**
     * 拆分XML（将多条数据拆分，返回每条的XML）
     * @return  <主表字段值，主从表拆分xml>
     * @throws org.dom4j.DocumentException
     */
    private Map getSingleDocs(Document input,List<RestParam> paramList,String token) throws TransformException{
        Map putMap = new HashMap();
        List putList = new ArrayList();
        if (keyMap == null || keyMap.size() < 1){
            String retMsg = "请设置主表主键!";
            throw new TransformException(retMsg);
        }
        List<String> keys = new ArrayList<String>(keyMap.keySet());
        String mTabName = keys.get(0);
        String mColName = keyMap.get(mTabName);

        String isMain = "",sumXml = "";

        try {
            List<Document.Element> mainList = new ArrayList<Document.Element>();
            Map<String,Document.Element> subMap = new HashMap<String,Document.Element>();
            Document.Element e0 = input.getRootElement();
            if (e0 == null || e0.getChildren() == null || e0.getChildren().size() <1){
                String retMsg = "未取到来源数据，请检查document";
                throw new TransformException(retMsg);
            }
            //拆分主从表
            List<Document.Element> gList = e0.getChildren();
            for(Document.Element e1:gList){
                isMain = e1.getName();
                if("values".equalsIgnoreCase(isMain)){
                    mainList.add(e1);
                }
                else if("subForms".equalsIgnoreCase(isMain)){
                    List<Document.Element> aList = e1.getChildren();
                    for(Document.Element e2:aList){
                        if (e2 == null ||e2.getAttribute("name")==null) continue;
                        String tabName = (String) e2.getAttribute("name").getValue();
                        if (StringUtils.isBlank(tabName)) continue;
                        if (keyMap.containsKey(tabName)){
                            subMap.put(tabName,e2);
                        }
                    }
                }
                else if("summary".equalsIgnoreCase(isMain)){
                    sumXml = DocumentUtil.toXML(e1);
                }
            }
            StringBuffer retXml = null;
            StringBuffer innerXml = null;
            String colValue = "";
            for(Document.Element e01:mainList){
                //获取发起流程参数
                Map oneMap = getParamToMap(paramList,input);
                oneMap.put("token",token);
                retXml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                retXml.append("<formExport version=\"2.0\">");
                retXml.append(sumXml);
                //设置登录名
                String loginName = getValByFieldName(e01,SEND_LOGIN_NAME);
                if (StringUtils.isNotBlank(loginName)){
                    e01.removeChild(SEND_LOGIN_NAME); //移除发送人节点
                    oneMap.put("senderLoginName",loginName);
                }

                retXml.append(DocumentUtil.toXML(e01));
                retXml.append("<definitions/>\n");

                retXml.append("<subForms>\n");
                //取主表主键值
                colValue = getValByFieldName(e01,mColName);
                if(subMap.size() > 0 && StringUtils.isNotBlank(colValue)){
                    for (Map.Entry<String,Document.Element> entry : subMap.entrySet()){
                        if (entry == null||entry.getValue()==null||StringUtils.isBlank(entry.getKey())) continue;
                        //从表名
                        String subFormName = entry.getKey();
                        //从表外键字段
                        String subFieldName = keyMap.get(subFormName);
                        if (StringUtils.isBlank(subFieldName)) continue;
                        innerXml = new StringBuffer();
                        //取从表字段
                        List<Document.Element> vals = entry.getValue().getChildren();
                        if (vals == null || vals.get(0) == null) continue;
                        List<Document.Element> rows = vals.get(0).getChildren();
                        for (Document.Element e11:rows){
                            String rowVal = getValByFieldName(e11,subFieldName);
                            if (colValue.equals(rowVal)){
                                innerXml.append(DocumentUtil.toXML(e11));
                            }
                        }
                        if(innerXml.length() > 0){
                            retXml.append("<subForm name=\""+subFormName+"\">");
                            retXml.append("<definitions/>");
                            retXml.append("<values>");
                            retXml.append(innerXml.toString());
                            retXml.append("</values>");
                            retXml.append("</subForm>\n");
                        }
                    }
                }
                retXml.append("</subForms>");
                retXml.append("</formExport>");
                oneMap.put("data",retXml.toString());
                oneMap.put("keyFieldVal",colValue);
                putList.add(oneMap);
            }
            putMap.put("collList",putList);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error("解析或拆分doucment出现异常："+e);
            throw new TransformException(e);
        }
        return putMap;
    }
    /**
     * 根据字段名取对应value
     *
     * @return value
     */
    private String getValByFieldName(Document.Element e,String fieldName){
        String val = "";
        if (e == null) return val;
        List<Document.Element> ets = e.getChildren();
        for(Document.Element e0:ets){
            if (e0 == null || e0.getAttribute("name")==null) continue;
            String colName = (String)e0.getAttribute("name").getValue();
            if (fieldName.equals(colName)&&e0.getChild("value") != null){
                val = (String)e0.getChild("value").getValue();
                break;
            }
        }
        return val;
    }
    /**
     * 将字符串转换为List&lt;RestParam&gt;
     *
     * @return 转换后的List
     */
    private List<RestParam> toRestParams() {
        List<RestParam> restParams = new ArrayList<RestParam>();
        RestParam restParam = null;

        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            restParam = RestUtil.parseToRestParam(entry.getValue());
            if (restParam != null) {
                restParams.add(restParam);
            }
        }

        return restParams;
    }

    /**
     * URL格式如下：/data/members/{accountName}，那么需要将{accountName}用变量做替换
     *
     * @param str       原始字符串
     * @param paramList 变量列表
     * @param document  document
     * @param paramType 变量类型，PathParam或者BodyParam
     * @param client    Rest Client
     * @return 替换后的字符串
     */
    private String replaceParam(String str,
                                List<RestParam> paramList, Document document, String paramType, CTPRestClient client) {
        String tmpStr = str;
        for (RestParam restParam : paramList) {
            if (restParam != null) {
                try {
                    String paramValue = document.getContext().getParameters().evalString(restParam.getParamValue());
                    if (RestUtil.PATH_PARAM.equals(paramType)) {
                        paramValue = RestUtil.encode(paramValue);
                    }
                    //restParam.setParamValue(paramValue);
                    if (paramType.equals(restParam.getParamType())) {
                        // 替换value值
                        //paramValue = RestUtil.getDeeCast(client, restParam);
                        String paramName = restParam.getParamName().replaceAll("\\[\\[", "\\\\[\\\\[");
                        paramName = paramName.replaceAll("\\]\\]", "\\\\]\\\\]");
                        tmpStr = tmpStr.replaceAll("\\{" + paramName + "\\}", paramValue);
                    }
                } catch (TransformException e) {
                    log.error(e.getLocalizedMessage(), e);
                }

            }
        }
        return tmpStr;
    }
    /**
     * URL格式如下：/data/members/{accountName}，那么需要将{accountName}用变量做替换
     *
     * @param paramList 变量列表
     * @param document  document
     * @return Map<参数名，参数值>
     */
    private Map getParamToMap(List<RestParam> paramList, Document document) {
        Map putMap = new HashMap();
        for (RestParam restParam : paramList) {
            if (restParam == null || StringUtils.isBlank(restParam.getParamName())) continue;
            try {
                String paramValue = document.getContext().getParameters().evalString(restParam.getParamValue());
                //jdk降1.6 不支持switch
                if ("[[dee_valid_orgDeptName]]流程标题".equals(restParam.getParamName())){
                    putMap.put("subject",paramValue);
                }
                else if("附件ID".equals(restParam.getParamName())){
                    List<Long> atts = new ArrayList();
                        /*
                            暂不支持附件
                         */
//                        if (StringUtils.isNotBlank(paramValue)){
//                            String[] attArrs = paramValue.split(",");
//                            for (String attArr : attArrs) {
//                                if (StringUtils.isBlank(attArr))
//                                    continue;
//                                try{
//                                    Long attArrLong = Long.valueOf(attArr);
//                                    atts.add(attArrLong);
//                                }catch (Exception e){
//                                    continue;
//                                }
//                            }
//                        }
                    putMap.put("attachments",atts);
                }
                else if("上传附件类型（col：协同，doc：文档中心）".equals(restParam.getParamName())){
                    putMap.put("relateDoc",paramValue);
                }
                else if("[[dee_valid_formSend]]是否立即发起(0:立即发起1:进入待发)".equals(restParam.getParamName())){
                    putMap.put("param",paramValue);
                }
                else if("A8表单模板号".equals(restParam.getParamName())){
                    putMap.put("templateCode",paramValue);
                }
            } catch (TransformException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
        return putMap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdminUserName() {
        return adminUserName;
    }

    public void setAdminUserName(String adminUserName) {
        this.adminUserName = adminUserName;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Integer functionId) {
        this.functionId = functionId;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getResponseName() {
        return responseName;
    }

    public void setResponseName(String responseName) {
        this.responseName = responseName;
    }

    public String getMergeToDocument() {
        return mergeToDocument;
    }

    public void setMergeToDocument(String mergeToDocument) {
        this.mergeToDocument = mergeToDocument;
    }

    public Map<String, String> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, String> paramMap) {
        this.paramMap = paramMap;
    }

    public Map<String, String> getKeyMap() {
        return keyMap;
    }

    public void setKeyMap(Map<String, String> keyMap) {
        this.keyMap = keyMap;
    }
}
