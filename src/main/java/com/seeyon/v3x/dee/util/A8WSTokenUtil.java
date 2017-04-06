package com.seeyon.v3x.dee.util;

import com.seeyon.v3x.dee.TransformException;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class A8WSTokenUtil {
    private final static Log log = LogFactory.getLog(A8WSTokenUtil.class);

    public static String getA8WSToken(String userName, String password, String a8url) throws TransformException {
        try {
            // 获得客户端
            RPCServiceClient serviceClient = new RPCServiceClient();
            // 可以在该对象中设置服务端的验证信息
            Options options = serviceClient.getOptions();
            EndpointReference targetEPR = new EndpointReference(a8url + "/seeyon/services/authorityService");
            options.setTo(targetEPR);
            // 在创建QName对象时，QName类的构造方法的第一个参数表示WSDL文件的命名空间名，
            // 也就是<wsdl:definitions>元素的targetNamespace属性值
            QName opAddEntry = new QName("http://impl.services.v3x.seeyon.com", "authenticate");
            // 参数，如果有多个，继续往后面增加即可，不用指定参数的名称
            Object[] opAddEntryArgs = new Object[]{userName, password};
            // 返回参数类型，这个和axis1有点区别
            // invokeBlocking方法有三个参数，其中第一个参数的类型是QName对象，表示要调用的方法名；
            // 第二个参数表示要调用的WebService方法的参数值，参数类型为Object[]；
            // 第三个参数表示WebService方法的返回值类型的Class对象，参数类型为Class[]。
            // 当方法没有参数时，invokeBlocking方法的第二个参数值不能是null，而要使用new Object[]{}
            // 如果被调用的WebService方法没有返回值，应使用RPCServiceClient类的invokeRobust方法，
            // 该方法只有两个参数，它们的含义与invokeBlocking方法的前两个参数的含义相同
            OMElement result = serviceClient.invokeBlocking(opAddEntry, opAddEntryArgs);
            String token = "";
            if (result == null || result.getFirstElement() == null) return token;
            Iterator<OMElement> it = result.getFirstElement().getChildren();
            if (it == null) return token;
            while (it.hasNext()){
                OMElement e = it.next();
                if (e != null && "id".equals(e.getLocalName())){
                    token = e.getText();
                    break;
                }

            }
            return token;
        } catch (Exception e) {
            log.error("获取A8token错误：" + e.getLocalizedMessage(), e);
            throw new TransformException(e);
        }
    }
}
