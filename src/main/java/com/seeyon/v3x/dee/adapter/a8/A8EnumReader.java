package com.seeyon.v3x.dee.adapter.a8;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.TransformFactory;
import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.adapter.InitializingAdapter;
import com.seeyon.v3x.dee.datasource.XMLDataSource;
import com.seeyon.v3x.dee.resource.DbDataSource;
import com.seeyon.v3x.dee.util.A8WSTokenUtil;
import com.seeyon.v3x.dee.util.WebServiceClientUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;


/**
 * DEE内置A8EnumReader
 *
 * @author zhangfb
 */
public class A8EnumReader implements Adapter,InitializingAdapter {

    private final static Log log = LogFactory.getLog(A8EnumReader.class);

    private String name;
    private String interfaceName;
    private String xmlns;
    private String methodName;
    private String userName;
    private String password;
    private String a8url;
    private String enumIds;
    private DbDataSource dataSource;

    @Override
    public Document execute(Document document) throws TransformException {

        String token = A8WSTokenUtil.getA8WSToken(userName, password, a8url);
        String isAllPublic = "false";
        String publicEnumIds = "";
        String unitIds = "";
        String unitEnumIds = "";
        String[] array = enumIds.split(";");
        for (int i=0; i<array.length; i++) {
            String str = array[i];
            if (i == 0) {
                isAllPublic = str.substring(str.indexOf("{")+1, str.indexOf("}"));
            } else if (i == 1 || i == 2) {
                publicEnumIds += str.substring(str.indexOf("{")+1, str.indexOf("}")) + ",";
            } else if (i == 3) {
                unitIds = str.substring(str.indexOf("{")+1, str.indexOf("}")) + ",";
            } else if (i == 4 || i == 5) {
                unitEnumIds += str.substring(str.indexOf("{")+1, str.indexOf("}")) + ",";
            }
        }

        Object[] params = new Object[] {token, isAllPublic, publicEnumIds, unitIds, unitEnumIds};
        Class[] returnType = new Class[] {String.class};
        String address = a8url + "/seeyon/services/" + interfaceName;
        try {
            Object[] re = WebServiceClientUtil.invokeByRPC(address,
                    xmlns, methodName, params, returnType, 30000L);
            XMLDataSource xmlDataSource = new XMLDataSource(re[0].toString());
            return xmlDataSource.parse();
        } catch (AxisFault e) {
            log.error(e.getMessage(), e);
        } catch (DocumentException e) {
            log.error(e.getMessage(), e);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
        return TransformFactory.getInstance().newDocument("root");
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

    public String getEnumIds() {
        return enumIds;
    }

    public void setEnumIds(String enumIds) {
        this.enumIds = enumIds;
    }

    public DbDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DbDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void evalParaBeforeExe(Parameters parameters) throws Exception {
        a8url = parameters.evalString(a8url);
        userName = parameters.evalString(userName);
        password = parameters.evalString(password);
    }
}

