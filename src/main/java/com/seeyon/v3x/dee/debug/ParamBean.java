package com.seeyon.v3x.dee.debug;

/**
 * Created by yangyu on 2016-3-4.
 * 用于存储脚本调试时参数信息
 */
public class ParamBean {
    private String adapterName;
    private String line;
    private String name;
    private Object val;

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getVal() {
        return val;
    }

    public void setVal(Object val) {
        this.val = val;
    }
}
