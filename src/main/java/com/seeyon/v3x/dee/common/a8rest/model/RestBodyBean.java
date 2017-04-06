package com.seeyon.v3x.dee.common.a8rest.model;

import java.io.Serializable;

/**
 * Rest的请求Body
 *
 * @author zhangfb
 */
public class RestBodyBean implements Serializable {
    /**
     * ID
     */
    private Integer bodyId;

    /**
     * 名称
     */
    private String bodyName;

    /**
     * 类型，如：application/json、application/xml...
     */
    private String bodyType;

    /**
     * 数据来源
     */
    private String bodyData;

    /**
     * Body模板
     */
    private String bodyTemplate;

    /**
     * 关联的方法
     */
    private RestFunctionBean functionBean;

    public Integer getBodyId() {
        return bodyId;
    }

    public void setBodyId(Integer bodyId) {
        this.bodyId = bodyId;
    }

    public String getBodyName() {
        return bodyName;
    }

    public void setBodyName(String bodyName) {
        this.bodyName = bodyName;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public RestFunctionBean getFunctionBean() {
        return functionBean;
    }

    public void setFunctionBean(RestFunctionBean functionBean) {
        this.functionBean = functionBean;
    }

    public String getBodyData() {
        return bodyData;
    }

    public void setBodyData(String bodyData) {
        this.bodyData = bodyData;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public void setBodyTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }
}
