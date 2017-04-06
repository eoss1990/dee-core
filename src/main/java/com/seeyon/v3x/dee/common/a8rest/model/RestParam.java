package com.seeyon.v3x.dee.common.a8rest.model;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * Rest参数
 *
 * @author zhangfb
 */
public class RestParam implements Serializable {
    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 参数类型
     */
    private String paramType;

    /**
     * 参数显示值
     */
    private String showValue;

    /**
     * 参数值
     */
    private String paramValue;
    /**
     * 是否必填
     * 0：必填 1：选填
     */
    private String isRequired;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(paramName).append("|");
        builder.append(paramType).append("|");
        builder.append(showValue).append("|");
        if (StringUtils.isNotBlank(paramValue)) {
            int start = paramName.indexOf("[[dee_select_");
            int end = paramName.indexOf("]]");
            if (start >= 0 && end > start) {
                builder.append(paramValue);
            } else {
                builder.append(showValue);
            }
        } else {
            builder.append(showValue);
        }
        builder.append("|").append(StringUtils.isBlank(isRequired)?"0":isRequired);

        return builder.toString();
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getShowValue() {
        return showValue;
    }

    public void setShowValue(String showValue) {
        this.showValue = showValue;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(String isRequired) {
        this.isRequired = isRequired;
    }
}
