package com.seeyon.v3x.dee.config.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.v3x.dee.adapter.a8.A8BPMLauchFormColWriter;
import com.seeyon.v3x.dee.adapter.a8.A8CommonWSWriter;
import com.seeyon.v3x.dee.adapter.a8.A8EnumReader;
import com.seeyon.v3x.dee.adapter.a8.A8EnumWriter;
import com.seeyon.v3x.dee.adapter.a8.A8FormWriteWriter;
import com.seeyon.v3x.dee.adapter.a8.A8MsgWriter;
import com.seeyon.v3x.dee.adapter.a8.OrgSyncWriter;
import com.seeyon.v3x.dee.adapter.a8.RestProcessor;
import com.seeyon.v3x.dee.adapter.colmap.ColumnMappingProcessor;
import com.seeyon.v3x.dee.adapter.database.JDBCReader;
import com.seeyon.v3x.dee.adapter.database.JDBCWriter;
import com.seeyon.v3x.dee.adapter.sap.jco.SapJcoProcessor;
import com.seeyon.v3x.dee.adapter.sap.webservice.SapWSProcessor;
import com.seeyon.v3x.dee.adapter.webservice.WSProcessor;
import com.seeyon.v3x.dee.adapter.xslt.XSLTProcessor;
import com.seeyon.v3x.dee.config.ParserEntry;
import com.seeyon.v3x.dee.config.RefBean;
import com.seeyon.v3x.dee.util.ConfigUtil;
import com.seeyon.v3x.dee.util.ReflectException;
import com.seeyon.v3x.dee.util.ReflectUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;


/**
 * 解析器默认实现
 *
 * @author zhangfb
 */
public class DefaultParser extends Parser {
    private static Log log = LogFactory.getLog(DefaultParser.class);

    public DefaultParser(ParserEntry entry) {
        super(entry);
    }

    @Override
    public Object execute(Element element) {
        String className = element.attributeValue(ParserConstants.CLASS);

        // 为兼容老版本适配器类名路径，需要使用类名转换器进行转化
        className = AdapterFixer.getInstance().convert(className);

        Object obj = reflect2Obj(className);
        if (obj != null) {
            parseProperty(obj, element);
            parseMap(obj, element);
            //解析子节点
            parseSon(obj, element);
        }

        return obj;
    }

    /**
     * 使用反射生成类实例对象
     *
     * @param className 类名称
     * @return 类的实例
     */
    private Object reflect2Obj(String className) {
        try {
            return ReflectUtil.reflectClass(className);
        } catch (ReflectException e) {
            log.error("生成类对象出错：" + className);
        }
        return null;
    }

    /**
     * 解析节点，如果节点属性值为value，则直接对对象赋值；如果节点属性值为ref，则将引用信息加入cache
     *
     * @param obj     赋值对象
     * @param element 节点对象
     */
    @SuppressWarnings("unchecked")
    private void parseProperty(Object obj, Element element) {
        List<Element> propElements = element.elements(ParserConstants.PROPERTY);
        for (Element e : propElements) {
            String name = e.attributeValue(ParserConstants.NAME);
            String value = e.attributeValue(ParserConstants.VALUE);
            String ref = e.attributeValue(ParserConstants.REF);
            String methodName = ConfigUtil.getMethodByField(name);
            if (value != null) {
                ReflectUtil.setValue(obj, methodName, value);
            } else if (ref != null) {
                RefBean refBean = new RefBean();
                refBean.setObj(obj);
                refBean.setRefId(ref);
                refBean.setFieldName(name);
                entry.putRefCache(refBean);
            }
        }
    }

    /**
     * 解析map节点，为了与老版本兼容，这儿加入jco相关map标签<p/>
     * map节点格式如下：<p/>
     * &lt;map name="paraMap"&gt;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;key name="P_ALL" value="X"/&gt;<br/>
     * &lt;/map&gt;
     *
     * @param obj     赋值对象
     * @param element 节点对象
     */
    @SuppressWarnings("unchecked")
    private void parseMap(Object obj, Element element) {
        List<Element> rootElements = new ArrayList<Element>();
        List<Element> mapElements = element.elements(ParserConstants.MAP);
        List<Element> jcoReturnMapElements = element.elements("jcoreturnmap");
        List<Element> jcoStructureMapElements = element.elements("jcostructuremap");
        List<Element> jcoTableMapElements = element.elements("jcotablemap");

        rootElements.addAll(mapElements);
        rootElements.addAll(jcoReturnMapElements);
        rootElements.addAll(jcoStructureMapElements);
        rootElements.addAll(jcoTableMapElements);

        for (Element e : rootElements) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();

            List<Element> keyElements = e.elements(ParserConstants.KEY);
            for (Element keyE : keyElements) {
                String key = keyE.attributeValue(ParserConstants.NAME);
                String value = keyE.attributeValue(ParserConstants.VALUE);
                map.put(key, value);
            }

            try {
                // map的name属性
                String name = e.attributeValue(ParserConstants.NAME);
                // 使用反射，调用map的set方法
                ReflectUtil.invokeMethodByFieldName(obj, name, map);
            } catch (ReflectException e1) {
                log.error("调用方法出错：" + e1.getLocalizedMessage(), e1);
            }
        }
    }

    /**
     * 解析子节点，如果节点属性值为value，则直接对对象赋值；
     *
     * @param obj     赋值对象
     * @param element 节点对象
     */
    @SuppressWarnings("unchecked")
    private void parseSon(Object obj, Element element) {
        List<Element> propElements = element.elements();
        for (Element e : propElements) {
        	String className = e.attributeValue(ParserConstants.CLASS);
        	if(className == null || "".equals(className)) continue;
        	
        	Object sonObj = execute(e);
            String name = e.attributeValue(ParserConstants.NAME);
            String methodName = ConfigUtil.getMethodByField(name);
            if (sonObj != null) {
                ReflectUtil.setObjValue(obj, methodName, sonObj);
            } 
        }
    }

    /**
     * 适配器类名路径转换器
     *
     * @author zhangfb
     */
    static class AdapterFixer {
        private static AdapterFixer INSTANCE = new AdapterFixer();

        private Map<String, String> oldAndNew = new HashMap<String, String>();

        private AdapterFixer() {
            oldAndNew.put("com.seeyon.v3x.dee.adapter.JDBCReader", JDBCReader.class.getName());
            oldAndNew.put("com.seeyon.v3x.dee.adapter.JDBCWriter", JDBCWriter.class.getName());
            oldAndNew.put("com.seeyon.v3x.dee.adapter.JDBCWriter", JDBCWriter.class.getName());
            oldAndNew.put("com.seeyon.v3x.dee.adapter.A8BPMLauchFormColWriter", A8BPMLauchFormColWriter.class.getName());

            oldAndNew.put("com.seeyon.v3x.dee.processor.WSProcessor", WSProcessor.class.getName());
            oldAndNew.put("com.seeyon.v3x.dee.processor.SapWSProcessor", SapWSProcessor.class.getName());
            oldAndNew.put("com.seeyon.v3x.dee.processor.ColumnMappingProcessor", ColumnMappingProcessor.class.getName());
            oldAndNew.put("com.seeyon.v3x.dee.processor.XSLTProcessor", XSLTProcessor.class.getName());

            oldAndNew.put("com.seeyon.v3x.dee.extend.SapJcoProcessor", SapJcoProcessor.class.getName());
            oldAndNew.put("com.seeyon.v3x.dee.extend.A8EnumReader", A8EnumReader.class.getName());
            oldAndNew.put("com.seeyon.v3x.dee.extend.A8EnumWriter", A8EnumWriter.class.getName());
            oldAndNew.put("com.seeyon.v3x.dee.extend.A8CommonWSWriter", A8CommonWSWriter.class.getName());
            oldAndNew.put("com.seeyon.v3x.dee.extend.A8MsgWriter", A8MsgWriter.class.getName());
            oldAndNew.put("com.seeyon.v3x.dee.extend.A8FormWriteWriter", A8FormWriteWriter.class.getName());
            oldAndNew.put("com.seeyon.v3x.dee.extend.OrgSyncWriter", OrgSyncWriter.class.getName());
            oldAndNew.put("com.seeyon.v3x.dee.extend.RestProcessor", RestProcessor.class.getName());
        }

        public static AdapterFixer getInstance() {
            return INSTANCE;
        }

        public String convert(String className) {
            String newClassName = oldAndNew.get(className);
            if (newClassName == null) {
                newClassName = className;
            }
            return newClassName;
        }
    }
}
