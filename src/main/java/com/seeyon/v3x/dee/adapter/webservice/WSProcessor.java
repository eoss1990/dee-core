package com.seeyon.v3x.dee.adapter.webservice;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.adapter.InitializingAdapter;
import com.seeyon.v3x.dee.adapter.webservice.addition.AddSoapHeader;
import com.seeyon.v3x.dee.adapter.webservice.addition.ClientSecurityHandler;
import com.seeyon.v3x.dee.datasource.XMLDataSource;
import com.seeyon.v3x.dee.util.DocumentUtil;
import com.seeyon.v3x.dee.util.PageUtil;
import com.seeyon.v3x.dee.util.WebServiceClientUtil;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.WSHandlerConstants;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

public class WSProcessor implements Adapter, InitializingAdapter {

    public static final String RETURNTYPE_STRING = "string";
    public static final String RETURNTYPE_NUMBER = "number";
    public static final String RETURNTYPE_VOID = "void";

    private final static Log log = LogFactory.getLog(WSProcessor.class);
    private String serviceurl;
    private String namespace;
    private String username;
    private String password;
    private String method;
    private String returnType;
    private String timeout;
    private Map<String, Object> parameter;

    public WSProcessor() {
        parameter = new LinkedHashMap<String, Object>();
    }

    public Document execute(Document output) throws TransformException {
        Object result;
        if (serviceurl.toLowerCase().endsWith("asmx")
                || serviceurl.toLowerCase().endsWith("svc")) {
            try {
                ServiceClient sender = new ServiceClient();
                sender.setOptions(buildOptions());

                if (StringUtils.isNotBlank(username)
                        && StringUtils.isNotBlank(password)) {
                    String[] usrName = username.split(":");
                    String[] pssWord = password.split(":");
                    if (usrName.length != 3 || pssWord.length != 3
                            || !usrName[0].equals(pssWord[0])) {
                        throw new TransformException("soapheader用户名密码格式错误！");
                    }

                    OMFactory omFactory = OMAbstractFactory.getOMFactory();
                    OMNamespace omNs = omFactory.createOMNamespace(namespace,
                            "");
                    OMElement header = omFactory.createOMElement(usrName[0],
                            omNs);
                    OMElement ome_user = omFactory.createOMElement(usrName[1],
                            omNs);
                    ome_user.setText(usrName[2]);
                    header.addChild(ome_user);
                    OMElement ome_pass = omFactory.createOMElement(pssWord[1],
                            omNs);
                    ome_pass.setText(pssWord[2]);
                    header.addChild(ome_pass);
                    sender.addHeader(header);
                }

                OMElement resultOMElement = sender
                        .sendReceive(getParametsOMElement(output));
                result = StringEscapeUtils.unescapeXml(resultOMElement
                        .toString());
                log.debug("WS " + serviceurl + ":" + method + "返回值为:" + result);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new TransformException("调用WS" + serviceurl + ":" + method
                        + "异常：" + e.getMessage(), e);
            }
        } else {
            try {

                /**
                 * cxf是创建bus的时候会去jar包的META-INF目录下寻找org.apache.cxf.bus.
                 * factory这个文件这个文件的内容是
                 * org.apache.cxf.bus.spring.SpringBusFactory
                 * org.springframework.context.ApplicationContext
                 * 当你的环境是spring的换件 而且classpath下他找到了
                 * org.apache.cxf.bus.spring.SpringBusFactory
                 * org.springframework.context.ApplicationContext这2个类
                 * ,随后他会去调用SpringBusFactory这个类, 这个类他引用了spring的jar中的beans类
                 * 如果你的spring版本和他所引入的不一致,那么就会导致 classnotfoundexception, 大多数情况
                 * 我们不可能为了引入一个cxf而改变公司原有的spring版本结构
                 * ,而我遇到的问题是对方公司的webservce要求用cxf2
                 * .4版本(那里要求的是spring3.0而这个系统比较老是spring1的),因此换了一种方式调用
                 * JaxWsProxyFactoryBean proxyFactory =new
                 * JaxWsProxyFactoryBean(); 使用JaxWsProxyFactoryBean 的setBus方法
                 * 调用busfactory产生cxf自带cxfbusfactory的实例
                 * proxyFactory.setBus(BusFactory
                 * .newInstance(BusFactory.DEFAULT_BUS_FACTORY).createBus());
                 */
                // CXF动态调用WS，调用CXFBusFactory而不使用SpringBusFactory
                JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory
                        .newInstance(BusFactory.newInstance(
                                BusFactory.DEFAULT_BUS_FACTORY).createBus());
                org.apache.cxf.endpoint.Client client = dcf.createClient(
                        serviceurl + "?wsdl", this.getClass().getClassLoader());

                // cxf常用的handler身份验证机制
                if (org.apache.commons.lang.StringUtils.isNotBlank(username)
                        && org.apache.commons.lang.StringUtils
                        .isNotBlank(password)) {
                    String[] usrName = username.split(":");
                    String[] pssWord = password.split(":");

                    if (usrName.length == 1) {
                        Endpoint endpoint = client.getEndpoint();
                        endpoint.getInInterceptors().add(
                                new LoggingInInterceptor());
                        endpoint.getOutInterceptors().add(
                                new LoggingOutInterceptor());
                        Map<String, Object> props = new HashMap<String, Object>();
                        props.put(WSHandlerConstants.ACTION,
                                WSHandlerConstants.USERNAME_TOKEN);
                        props.put(WSHandlerConstants.USER, username + ","
                                + password);
                        props.put(WSHandlerConstants.PASSWORD_TYPE,
                                WSConstants.PW_TEXT);
                        props.put(WSHandlerConstants.PW_CALLBACK_CLASS,
                                ClientSecurityHandler.class.getName());
                        WSS4JOutInterceptor wss4jOutInterceptor = new WSS4JOutInterceptor(
                                props);
                        endpoint.getOutInterceptors().add(wss4jOutInterceptor);
                    } else if (usrName.length == 3) {
                        if (usrName.length != 3 || pssWord.length != 3
                                || !usrName[0].equals(pssWord[0]))
                            throw new Exception("soapheader用户名密码格式错误！");
                        Map<String, String> fieldMap = new HashMap<String, String>();
                        fieldMap.put(usrName[1], usrName[2]);
                        fieldMap.put(pssWord[1], pssWord[2]);

                        Endpoint endpoint = client.getEndpoint();
                        endpoint.getOutInterceptors().add(
                                new AddSoapHeader(namespace, usrName[0],
                                        fieldMap));
                        endpoint.getOutInterceptors().add(
                                new LoggingOutInterceptor());
                    } else
                        throw new Exception("用户名密码格式错误！");
                }

                HTTPConduit http = (HTTPConduit) client.getConduit();
                HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
                httpClientPolicy.setConnectionTimeout(36000);
                httpClientPolicy.setAllowChunking(false);
                httpClientPolicy.setReceiveTimeout(conventTimeout());
                http.setClient(httpClientPolicy);

                // http身份验证机制
                // AuthorizationPolicy authorPolicy = http.getAuthorization();
                // authorPolicy.setUserName(username);
                // authorPolicy.setPassword(password);
                // http.setAuthorization(authorPolicy);

                // sayHello 为接口中定义的方法名称 张三为传递的参数 返回一个Object数组
                Object[] results = client.invoke(method, getParamets(output));

                /**
                 * axis2动态调用ws // 获得客户端 RPCServiceClient serviceClient = new
                 * RPCServiceClient(); // 可以在该对象中设置服务端的验证信息 Options options =
                 * serviceClient.getOptions(); options.setProperty(
                 * org.apache.axis2.transport.http.HTTPConstants.CHUNKED,
                 * Boolean.FALSE); options.setAction(namespace + method);
                 * EndpointReference targetEPR = new EndpointReference(
                 * serviceurl); options.setTo(targetEPR); //
                 * 在创建QName对象时，QName类的构造方法的第一个参数表示WSDL文件的命名空间名
                 * ，也就是<wsdl:definitions>元素的targetNamespace属性值 QName opAddEntry
                 * = new QName(namespace, method); //
                 * 参数，如果有多个，继续往后面增加即可，不用指定参数的名称 Object[] opAddEntryArgs =
                 * getParamets(output); // 返回参数类型，这个和axis1有点区别 //
                 * invokeBlocking方法有三个参数，其中第一个参数的类型是QName对象，表示要调用的方法名； //
                 * 第二个参数表示要调用的WebService方法的参数值，参数类型为Object[]； //
                 * 第三个参数表示WebService方法的返回值类型的Class对象，参数类型为Class[]。 //
                 * 当方法没有参数时，invokeBlocking方法的第二个参数值不能是null，而要使用new // Object[]{}
                 * //
                 * 如果被调用的WebService方法没有返回值，应使用RPCServiceClient类的invokeRobust方法，
                 * // 该方法只有两个参数，它们的含义与invokeBlocking方法的前两个参数的含义相同 Class[]
                 * classes = new Class[] { String.class }; if
                 * (RETURNTYPE_STRING.equals(returnType.toLowerCase())) { //
                 * classes = new Class[] { String.class }; } else if
                 * (RETURNTYPE_NUMBER.equals(returnType .toLowerCase())) {
                 * classes = new Class[] { BigDecimal.class }; } else if
                 * (RETURNTYPE_VOID.equals(returnType.toLowerCase())) {
                 * serviceClient.invokeRobust(opAddEntry, opAddEntryArgs);
                 * return output; } else { throw new TransformException("调用WS" +
                 * serviceurl + ":" + method + "异常!错误的返回值类型:" + returnType); }
                 *
                 * Object[] results = serviceClient.invokeBlocking(opAddEntry,
                 * opAddEntryArgs, classes);
                 **/
                result = results[0];
                client.destroy();
            } catch (Exception e) {
                // 记录cxf调用的异常记录
                log.error(e.getMessage(), e);

                Object[] params = getParamets(output);
                Class[] classes = new Class[]{Object.class};
                if (RETURNTYPE_STRING.equals(returnType.toLowerCase())) {
                    classes = new Class[]{String.class};
                } else if (RETURNTYPE_NUMBER.equals(returnType.toLowerCase())) {
                    classes = new Class[]{BigDecimal.class};
                } else if (RETURNTYPE_VOID.equals(returnType.toLowerCase())) {
                    classes = null;
                }

                try {
                    Object[] objects = WebServiceClientUtil.invokeByRPC(
                            serviceurl, namespace, method, params, classes,
                            username, password, conventTimeout());
                    result = objects[0];
                } catch (AxisFault ex) {
                    log.error(ex.getMessage(), ex);
                    throw new TransformException("调用WS" + serviceurl + ":"
                            + method + "异常：" + ex.getMessage(), ex);
                }
            }
        }

        output.getContext().setAttribute("WSResult", result);
        String resultStr = null;
        if (result instanceof String) {
            try {
                resultStr = (String) result;
                int start = resultStr.indexOf("<root>");
                int end = resultStr.indexOf("</root>");
                if (start != -1 && end != -1) {
                    resultStr = resultStr.substring(start, end + 7);
                }
                Document newInput = new XMLDataSource(resultStr).parse();
                newInput = PageUtil.pageDocument(newInput, output.getContext()
                        .getParameters());
                output = DocumentUtil.merge(output, newInput);
            } catch (Exception e) {
                // 转换失败，继续执行任务下面的适配器
                log.error("返回字符串转换document失败:" + e.getMessage() + "\n"
                        + resultStr, e);
            }
        }
        return output;
    }

    private Object[] getParamets(Document output) throws TransformException {
        List<Object> para = new LinkedList<Object>();
        for (Entry<String, Object> entry : parameter.entrySet()) {
            if (entry.getKey() == null || "".equals(entry.getKey().trim()))
                continue;
            para.add(entry.getValue().toString());
        }
        return para.size() == 0 ? new Object[]{} : para.toArray();
    }

    private OMElement getParametsOMElement(Document output)
            throws TransformException {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(this.namespace, "");
        OMElement data = fac.createOMElement(this.method, omNs);
        for (Entry<String, Object> entry : parameter.entrySet()) {
            if (entry.getKey() == null || "".equals(entry.getKey().trim()))
                continue;
            OMElement inner = fac.createOMElement(entry.getKey(), omNs);
            inner.setText(entry.getValue().toString());
            data.addChild(inner);
        }
        return data;
    }

    private Options buildOptions() {
        Options options = new Options();
        options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        options.setProperty(HTTPConstants.CHUNKED, "false");
        if (serviceurl.toLowerCase().endsWith("svc")) {
            String[] names = this.serviceurl.split("/");
            String interfacename = names[names.length - 1].substring(0,
                    names[names.length - 1].indexOf("."));
            interfacename = "I" + interfacename;
            options.setAction(this.namespace + interfacename + "/"
                    + this.method);
        } else {
            options.setAction(this.namespace + this.method);
        }
        options.setTo(new EndpointReference(this.serviceurl));
        // options.setProperty(propertyKey, property)

        // enabling MTOM in the client side
        // options.setProperty(Constants.Configuration.ENABLE_MTOM,
        // Constants.VALUE_TRUE);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        return options;
    }

    private long conventTimeout() {
        long tmp = 30000L;
        if (timeout != null) {
            try {
                tmp = Long.parseLong(timeout) * 1000;
            } catch (NumberFormatException ignored) {
            }
        }
        return tmp;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, Object> getParameter() {
        return parameter;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getServiceurl() {
        return serviceurl;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public void setServiceurl(String serviceurl) {
        this.serviceurl = serviceurl;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setParameter(Map<String, Object> parameter) {
        this.parameter = parameter;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    @Override
    public void evalParaBeforeExe(Parameters parameters) throws Exception {
        serviceurl = parameters.evalString(serviceurl);
        namespace = parameters.evalString(namespace);
        username = parameters.evalString(username);
        password = parameters.evalString(password);
        method = parameters.evalString(method);
        if (parameter!=null&&parameter.size() > 0) {
            for (Entry<String, Object> entry : parameter.entrySet()) {
                parameter.put(entry.getKey(),parameters.evalString(entry.getValue().toString()));
            }
        }

    }
}
