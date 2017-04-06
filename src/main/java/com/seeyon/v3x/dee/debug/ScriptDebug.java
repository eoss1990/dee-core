package com.seeyon.v3x.dee.debug;

import com.seeyon.v3x.dee.util.FileUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangyu on 2016-3-4.
 */
public class ScriptDebug {

    private static ScriptDebug instance;
    private ScriptDebug (){}
    private String adapterName;
    private List<ParamBean> param = new ArrayList<ParamBean>();

    public static synchronized ScriptDebug getInstance() {
        if (instance == null) {
            instance = new ScriptDebug();
        }
        return instance;
    }

    public void setBean2Map(ParamBean pb)
    {
        if (!FileUtil.isA8Home())
        {
            pb.setAdapterName(this.adapterName);
            this.param.add(pb);
        }
    }

    public List getParam() {
        return param;
    }

    public void setParam(List param) {
        this.param = param;
    }

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }
}
