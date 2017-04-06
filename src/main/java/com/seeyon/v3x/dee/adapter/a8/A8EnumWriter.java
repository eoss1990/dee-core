package com.seeyon.v3x.dee.adapter.a8;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.adapter.InitializingAdapter;
import com.seeyon.v3x.dee.resource.DbDataSource;
import com.seeyon.v3x.dee.util.A8WSTokenUtil;
import com.seeyon.v3x.dee.util.WebServiceClientUtil;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;

/**
 * DEE内置A8EnumWriter
 *
 * @author zhangfb
 */
public class A8EnumWriter implements Adapter,InitializingAdapter {

    private final static Log log = LogFactory.getLog(A8EnumWriter.class);

    private String name;
    private String interfaceName;
    private String xmlns;
    private String methodName;
    private String userName;
    private String password;
    private String a8url;
    private String enumId;
    private DbDataSource dataSource;
    private String enumDataXml;

    @Override
    public Document execute(Document output) throws TransformException {
        String token = A8WSTokenUtil.getA8WSToken(userName, password, a8url);
        Object[] params = new Object[]{token, enumId, enumDataXml};
        Class[] returnType = new Class[]{OMElement.class};
        String address = a8url + "/seeyon/services/" + interfaceName;
        try {
            Object[] results = WebServiceClientUtil.invokeByRPC(address,
                    xmlns, methodName, params, returnType, 30000L);
            Iterator iterator = ((OMElement) results[0]).getChildElements();
            String errorMsg = "";
            String errorNumber = "";
            while (iterator.hasNext()) {
                OMNode node = (OMNode) iterator.next();
                if (node.getType() == OMNode.ELEMENT_NODE) {
                    OMElement omElement = (OMElement) node;
                    if ("errorMessage".equals(omElement.getLocalName())) {
                        errorMsg = omElement.getText().trim();
                    } else if ("errorNumber".equals(omElement.getLocalName())) {
                        errorNumber = omElement.getText().trim();
                    }
                }
            }
            if (!"0".equals(errorNumber)) {
                log.error("枚举导入异常：" + errorMsg);
                throw new TransformException("枚举导入异常：" + errorMsg);
            }
        } catch (AxisFault e) {
            log.error(e.getMessage(), e);
            throw new TransformException("枚举导入异常：" + e.getMessage());
        }
        return output;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getXmlns() {
        return xmlns;
    }

    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getA8url() {
        return a8url;
    }

    public void setA8url(String a8url) {
        this.a8url = a8url;
    }

    public String getEnumId() {
        return enumId;
    }

    public void setEnumId(String enumId) {
        this.enumId = enumId;
    }

    public DbDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DbDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getEnumDataXml() {
        return enumDataXml;
    }

    public void setEnumDataXml(String enumDataXml) {
        this.enumDataXml = enumDataXml;
    }

    @Override
    public void evalParaBeforeExe(Parameters parameters) throws Exception {
        userName = parameters.evalString(userName);
        password = parameters.evalString(password);
        a8url = parameters.evalString(a8url);
        enumDataXml = parameters.evalString(enumDataXml);
    }
}
