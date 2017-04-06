package com.seeyon.v3x.dee.common.a8rest.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Rest服务Bean
 *
 * @author zhangfb
 */
public class RestServiceBean implements Serializable {
    /**
     * 服务ID
     */
    private Integer serviceId;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 结果处理类
     */
    private String dealClass;

    /**
     * 服务下所有的方法列表
     */
    private List<RestFunctionBean> functionBeans = new ArrayList<RestFunctionBean>();

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<RestFunctionBean> getFunctionBeans() {
        return functionBeans;
    }

    public void setFunctionBeans(List<RestFunctionBean> functionBeans) {
        this.functionBeans = functionBeans;
    }

    public String getDealClass() {
        return dealClass;
    }

    public void setDealClass(String dealClass) {
        this.dealClass = dealClass;
    }
}
