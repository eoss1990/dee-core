package com.seeyon.v3x.dee.common.a8rest.model;

import java.io.Serializable;

/**
 * Rest方法Bean
 *
 * @author zhangfb
 */
public class RestFunctionBean implements Serializable {
    /**
     * 方法ID
     */
    private Integer functionId;

    /**
     * 方法名称
     */
    private String functionName;

    /**
     * 方法类型，GET/PUT/POST/DELETE
     */
    private String functionType;

    /**
     * 方法的相对路径<br/>
     * 如果rest地址为：http://localhost/seeyon/rest/member/loginName/{loginName}<br/>
     * 那么方法的相对地址为：/member/loginName/{loginName}
     */
    private String functionPath;

    /**
     * 配置类型
     */
    private String cfgType;

    /**
     * 响应类型
     */
    private String responseType;

    /**
     * 结果处理方法
     */
    private String dealMethod;

    /**
     * 返回类型
     */
    private String returnType;

    /**
     * 是否显示主键列表
     */
    private String showTab;

    /**
     * 方法所属的服务
     */
    private RestServiceBean serviceBean;

    /**
     * 方法BodyParam
     */
    private RestBodyBean bodyBean;

    public Integer getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Integer functionId) {
        this.functionId = functionId;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionType() {
        return functionType;
    }

    public void setFunctionType(String functionType) {
        this.functionType = functionType;
    }

    public String getFunctionPath() {
        return functionPath;
    }

    public void setFunctionPath(String functionPath) {
        this.functionPath = functionPath;
    }

    public String getCfgType() {
        return cfgType;
    }

    public void setCfgType(String cfgType) {
        this.cfgType = cfgType;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getDealMethod() {
        return dealMethod;
    }

    public void setDealMethod(String dealMethod) {
        this.dealMethod = dealMethod;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public RestServiceBean getServiceBean() {
        return serviceBean;
    }

    public void setServiceBean(RestServiceBean serviceBean) {
        this.serviceBean = serviceBean;
    }

    public RestBodyBean getBodyBean() {
        return bodyBean;
    }

    public void setBodyBean(RestBodyBean bodyBean) {
        this.bodyBean = bodyBean;
    }

    public String getShowTab() {
        return showTab;
    }

    public void setShowTab(String showTab) {
        this.showTab = showTab;
    }
}
