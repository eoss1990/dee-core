package com.seeyon.v3x.dee.adapter.sap.jco;

import com.sap.conn.jco.JCoException;
import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.adapter.InitializingAdapter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.v3x.dee.adapter.sap.jco.plugin.DeeSapJco;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SapJcoProcessor implements Adapter, InitializingAdapter {

    private static Log log = LogFactory.getLog(SapJcoProcessor.class);

    /**
     * sap连接基本信息
     */
    private String jco_ashost;
    private String jco_sysnr;
    private String jco_client;
    private String jco_user;
    private String jco_passwd;

    /**
     * sap 方法名及输出参数
     */
    private String func;
    private String in_param;

    private String out_param;

    private String pr_type;

    /**
     * 参数map
     */
    private Map<String, String> paraMap;
    private Map<String, String> jcoReturnMap;
    private Map<String, String> jcoStructureMap;
    private Map<String, String> jcoTableMap;

    @Override
    public Document execute(Document input) throws TransformException {
        // TODO Auto-generated method stub
        Map<String, String> newParaMap = new HashMap<String, String>();
        if (paraMap != null) {
            for (Entry<String, String> entry : paraMap.entrySet()) {
                if ("".equals(entry.getKey()))
                    continue;
                newParaMap.put(entry.getKey(), input.getContext().getParameters().evalString(entry.getValue()));
            }
        }
        if ("reader".equals(pr_type)) {
            //来源配置
            try {
                if (StringUtils.isNotBlank(out_param)) {
                    jcoReturnMap = new HashMap<String, String>();
                    jcoReturnMap.put(out_param, "String");
                }
                if (func == null || jcoReturnMap == null)
                    throw new TransformException("方法或返回参数为空");
                log.info("连接信息：" + jco_ashost + "," + jco_sysnr + "," + jco_client + "," + jco_user + "," + jco_passwd);
                input = DeeSapJco.getInstance(jco_ashost, jco_sysnr, jco_client, jco_user, jco_passwd).getSAPJCOData(input, func, newParaMap, jcoReturnMap, jcoStructureMap, jcoTableMap);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                log.error(e.getMessage(), e);
                throw new TransformException("来源配置异常：", e);
            } catch (JCoException e) {
                // TODO Auto-generated catch block
                log.error(e.getMessage(), e);
                throw new TransformException("来源配置异常：", e);
            }
        } else {
            //目标配置
            try {
                if (StringUtils.isNotBlank(out_param)) {
                    jcoReturnMap = new HashMap<String, String>();
                    jcoReturnMap.put(out_param, "String");
                }
                if (func == null || jcoReturnMap == null)
                    throw new TransformException("方法或返回参数为空");
                input = DeeSapJco.getInstance(jco_ashost, jco_sysnr, jco_client, jco_user, jco_passwd).getSAPJCOData(input, func, newParaMap, jcoReturnMap, jcoStructureMap, jcoTableMap);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                log.error(e.getMessage(), e);
                throw new TransformException("目标配置异常：", e);
            } catch (JCoException e) {
                // TODO Auto-generated catch block
                log.error(e.getMessage(), e);
                throw new TransformException("目标配置异常：", e);
            }
        }
        return input;
    }

    public String getJco_ashost() {
        return jco_ashost;
    }

    public void setJco_ashost(String jco_ashost) {
        this.jco_ashost = jco_ashost;
    }

    public String getJco_sysnr() {
        return jco_sysnr;
    }

    public void setJco_sysnr(String jco_sysnr) {
        this.jco_sysnr = jco_sysnr;
    }

    public String getJco_client() {
        return jco_client;
    }

    public void setJco_client(String jco_client) {
        this.jco_client = jco_client;
    }

    public String getJco_user() {
        return jco_user;
    }

    public void setJco_user(String jco_user) {
        this.jco_user = jco_user;
    }

    public String getJco_passwd() {
        return jco_passwd;
    }

    public void setJco_passwd(String jco_passwd) {
        this.jco_passwd = jco_passwd;
    }

    public String getFunc() {
        return func;
    }

    public void setFunc(String func) {
        this.func = func;
    }

    public String getIn_param() {
        return in_param;
    }

    public void setIn_param(String in_param) {
        this.in_param = in_param;
    }

    public String getOut_param() {
        return out_param;
    }

    public void setOut_param(String out_param) {
        this.out_param = out_param;
    }

    public String getPr_type() {
        return pr_type;
    }

    public void setPr_type(String pr_type) {
        this.pr_type = pr_type;
    }

    public Map<String, String> getParaMap() {
        return paraMap;
    }

    public void setParaMap(Map<String, String> paraMap) {
        this.paraMap = paraMap;
    }

    public Map<String, String> getJcoReturnMap() {
        return jcoReturnMap;
    }

    public void setJcoReturnMap(Map<String, String> jcoReturnMap) {
        this.jcoReturnMap = jcoReturnMap;
    }

    public Map<String, String> getJcoStructureMap() {
        return jcoStructureMap;
    }

    public void setJcoStructureMap(Map<String, String> jcoStructureMap) {
        this.jcoStructureMap = jcoStructureMap;
    }

    public Map<String, String> getJcoTableMap() {
        return jcoTableMap;
    }

    public void setJcoTableMap(Map<String, String> jcoTableMap) {
        this.jcoTableMap = jcoTableMap;
    }

    @Override
    public void evalParaBeforeExe(Parameters parameters) throws Exception {
        jco_ashost = parameters.evalString(jco_ashost);
        jco_sysnr = parameters.evalString(jco_sysnr).trim();
        jco_client = parameters.evalString(jco_client).trim();
        jco_user = parameters.evalString(jco_user).trim();
        jco_passwd = parameters.evalString(jco_passwd).trim();
        func = parameters.evalString(func).trim();
        if (paraMap != null&&paraMap.size()>0) {
            for (Entry<String, String> entry : paraMap.entrySet()) {
                paraMap.put(entry.getKey(), parameters.evalString(entry.getValue()));
            }
        }
    }
}
