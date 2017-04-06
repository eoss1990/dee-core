package com.seeyon.v3x.dee.common.a8rest;

import com.seeyon.v3x.dee.common.a8rest.model.RestBodyBean;
import com.seeyon.v3x.dee.common.a8rest.model.RestFunctionBean;
import com.seeyon.v3x.dee.common.a8rest.model.RestParam;
import com.seeyon.v3x.dee.common.a8rest.model.RestServiceBean;
import com.seeyon.v3x.dee.common.a8rest.util.RestUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Rest服务管理器
 *
 * @author zhangfb
 */
public class RestServiceManager {
    private final static Log log = LogFactory.getLog(RestServiceManager.class);
    private static RestServiceManager instance = new RestServiceManager();

    /**
     * 服务列表
     */
    private List<RestServiceBean> serviceBeans = new ArrayList<RestServiceBean>();

    /**
     * Body模板列表
     */
    private List<RestBodyBean> bodyBeans = new ArrayList<RestBodyBean>();

    private RestServiceManager() {
        init();
    }

    public static RestServiceManager getInstance() {
        return instance;
    }

    public void init() {
        SAXReader saxReader = new SAXReader();

        try {
            URL fileURL = RestServiceManager.class.getResource("/com/seeyon/v3x/dee/conf/rest-template.xml");
            Document document = saxReader.read(fileURL);
            Element root = document.getRootElement();
            // 解析body节点
            parseBody(root);
            // 解析service节点
            parseService(root);
            // 合并body和service
            mergeBodyAndService();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 解析body节点
     *
     * @param root
     */
    private void parseBody(Element root) {
        List<Element> bodyElements = (List<Element>) root.selectNodes("templates/body");
        for (Element bodyElement : bodyElements) {
            if (bodyElement != null) {
                RestBodyBean bodyBean = new RestBodyBean();
                bodyBean.setBodyId(Integer.parseInt(bodyElement.attributeValue("id")));
                bodyBean.setBodyName(bodyElement.attributeValue("name"));
                bodyBean.setBodyType(bodyElement.attributeValue("type"));
                Attribute dataAttribute = bodyElement.attribute("data");
                if (dataAttribute != null) {
                    bodyBean.setBodyData(dataAttribute.getValue());
                } else {
                    bodyBean.setBodyTemplate(bodyElement.getStringValue());
                }
                bodyBeans.add(bodyBean);
            }
        }
    }

    /**
     * 解析service节点
     *
     * @param root
     */
    private void parseService(Element root) {
        List<Element> serviceElements = (List<Element>) root.selectNodes("services/service");
        for (Element serviceElement : serviceElements) {
            if (serviceElement != null) {
                RestServiceBean serviceBean = new RestServiceBean();
                serviceBean.setServiceId(Integer.parseInt(serviceElement.attributeValue("id")));
                serviceBean.setServiceName(serviceElement.attributeValue("name"));
                Attribute dealClassAttribute = serviceElement.attribute("deal_class");
                if (dealClassAttribute != null) {
                    serviceBean.setDealClass(dealClassAttribute.getValue());
                }
                serviceBeans.add(serviceBean);

                // 解析function节点
                parseFunction(serviceElement, serviceBean);
            }
        }
    }

    /**
     * 解析function节点
     *
     * @param serviceElement service节点
     * @param serviceBean    serviceBean
     */
    private void parseFunction(Element serviceElement, RestServiceBean serviceBean) {
        if (serviceElement.element("functions") != null) {
            List<Element> serviceElements = (List<Element>) serviceElement.selectNodes("functions/function");
            for (Element functionElement : serviceElements) {
                if (functionElement != null) {
                    RestFunctionBean functionBean = new RestFunctionBean();
                    functionBean.setFunctionId(Integer.parseInt(functionElement.attributeValue("id")));
                    functionBean.setFunctionName(functionElement.attributeValue("name"));
                    functionBean.setFunctionType(functionElement.attributeValue("type"));
                    functionBean.setFunctionPath(functionElement.attributeValue("path"));
                    Attribute cfgTypeAttribute = functionElement.attribute("cfg_type");
                    if (cfgTypeAttribute != null) {
                        functionBean.setCfgType(cfgTypeAttribute.getValue());
                    }
                    Attribute responseTypeAttribute = functionElement.attribute("response_type");
                    if (responseTypeAttribute != null) {
                        functionBean.setResponseType(responseTypeAttribute.getValue());
                    }
                    Attribute dealMethodAttribute = functionElement.attribute("deal_method");
                    if (dealMethodAttribute != null) {
                        functionBean.setDealMethod(dealMethodAttribute.getValue());
                    }
                    Attribute returnTypeAttribute = functionElement.attribute("return_type");
                    if (returnTypeAttribute != null) {
                        functionBean.setReturnType(returnTypeAttribute.getValue());
                    } else {
                        functionBean.setReturnType("java.lang.String");
                    }
                    Attribute showTabAttribute = functionElement.attribute("show_tab");
                    if (showTabAttribute != null) {
                        functionBean.setShowTab(showTabAttribute.getValue());
                    } else {
                        functionBean.setShowTab("false");
                    }
                    functionBean.setServiceBean(serviceBean);
                    serviceBean.getFunctionBeans().add(functionBean);
                }
            }
        }
    }

    /**
     * 合并body和service
     */
    private void mergeBodyAndService() {
        for (RestServiceBean serviceBean : serviceBeans) {
            for (RestFunctionBean functionBean : serviceBean.getFunctionBeans()) {
                for (RestBodyBean bodyBean : bodyBeans) {
                    if (bodyBean.getBodyName().equals(
                            serviceBean.getServiceName() + ":" + functionBean.getFunctionName())) {
                        bodyBean.setFunctionBean(functionBean);
                        functionBean.setBodyBean(bodyBean);
                    }
                }
            }
        }
    }

    /**
     * 根据服务ID，查询方法列表
     *
     * @param serviceId 服务ID
     * @return 方法列表
     */
    public List<RestFunctionBean> listFunctionsByServiceId(Integer serviceId) {
        for (RestServiceBean serviceBean : serviceBeans) {
            if (serviceBean.getServiceId().equals(serviceId)) {
                return serviceBean.getFunctionBeans();
            }
        }
        return null;
    }

    /**
     * 根据服务ID和方法ID，查询方法信息
     *
     * @param serviceId  服务ID
     * @param functionId 方法ID
     * @return 方法信息
     */
    public RestFunctionBean getFunctionBean(Integer serviceId, Integer functionId) {
        for (RestServiceBean serviceBean : serviceBeans) {
            for (RestFunctionBean functionBean : serviceBean.getFunctionBeans()) {
                if (serviceBean.getServiceId().equals(serviceId) && functionBean.getFunctionId().equals(functionId)) {
                    return functionBean;
                }
            }
        }
        return null;
    }

    /**
     * 根据服务ID和方法ID，查询方法中的参数列表
     *
     * @param serviceId  服务ID
     * @param functionId 方法ID
     * @return 参数列表
     */
    public List<RestParam> listParams(Integer serviceId, Integer functionId) {
        List<RestParam> restParams = new ArrayList<RestParam>();
        SERVICE_LOOP:
        for (RestServiceBean serviceBean : serviceBeans) {
            for (RestFunctionBean functionBean : serviceBean.getFunctionBeans()) {
                if (serviceBean.getServiceId().equals(serviceId) && functionBean.getFunctionId().equals(functionId)) {
                    List<String> pathParams = RestUtil.matchParams(functionBean.getFunctionPath());
                    for (String pathParam : pathParams) {
                        RestParam restParam = new RestParam();
                        restParam.setParamName(pathParam);
                        restParam.setParamType(RestUtil.PATH_PARAM);
                        restParams.add(restParam);
                    }
                    if (functionBean.getBodyBean() != null) {
                        List<String> bodyParams = RestUtil.matchParams(functionBean.getBodyBean().getBodyTemplate());
                        for (String bodyParam : bodyParams) {
                            if (StringUtils.isBlank(bodyParam)) continue;
                            String[] bParams = bodyParam.split("::");
                            RestParam restParam = new RestParam();
                            if (bParams != null && bParams.length>1){
                                restParam.setParamName(bParams[0]);
                                restParam.setIsRequired(bParams[1]);
                            }
                            else{
                                restParam.setParamName(bodyParam);
                                restParam.setIsRequired("0");//默认必填
                            }
                            restParam.setParamType(RestUtil.BODY_PARAM);
                            restParams.add(restParam);
                        }
                    }
                    break SERVICE_LOOP;
                }
            }
        }
        return restParams;
    }

    /**
     * 返回服务列表
     *
     * @return 服务列表
     */
    public List<RestServiceBean> getServiceBeans() {
        return serviceBeans;
    }
}
