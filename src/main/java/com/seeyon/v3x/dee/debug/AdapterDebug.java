package com.seeyon.v3x.dee.debug;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangyu on 2016-3-4.
 */
public class AdapterDebug {

    private static AdapterDebug instance;
    private AdapterDebug (){}
    private Map adapterMap = new HashMap();

    public static synchronized AdapterDebug getInstance() {
        if (instance == null) {
            instance = new AdapterDebug();
        }
        return instance;
    }

    public Map getAdapterMap() {
        return adapterMap;
    }

    public void setAdapterMap(Map adapterMap) {
        this.adapterMap = adapterMap;
    }
}
