package com.seeyon.v3x.dee.config.parser;

import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.config.ParserEntry;
import com.seeyon.v3x.dee.context.Flow;
import com.seeyon.v3x.dee.adapter.script.groovy.StaticGroovyScript;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import java.util.List;

/**
 * Flow解析器
 *
 * @author zhangfb
 */
public class FlowParser extends Parser {
    private static final Log log = LogFactory.getLog(FlowParser.class);
    private static final String SYNC_LISTENER = "com.seeyon.v3x.dee.common.listener.SyncListener";

    public FlowParser(ParserEntry entry) {
        super(entry);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object execute(Element element) {
        String flowId = element.attributeValue(ParserConstants.NAME);
        Flow flow = new Flow(flowId);

        List<Element> elements = element.elements();

        for (Element e : elements) {
            // 如果节点为adapter、processor、judgement和loop，使用class类别解析器解析
            if (ParserConstants.ADAPTER.equals(e.getName()) ||
                    ParserConstants.PROCESSOR.equals(e.getName()) ||
                    ParserConstants.JUDGEMENT.equals(e.getName()) ||
                    ParserConstants.LOOP.equals(e.getName())) {
                Parser parser = entry.getByClass(e.attributeValue(ParserConstants.CLASS));
                Adapter adapter = (Adapter) parser.execute(e);
                flow.putAdapter(e.attributeValue(ParserConstants.NAME), adapter);
                continue;
            }

            // 如果节点为script，使用脚本解析器解析
            if (ParserConstants.SCRIPT.equals(e.getName())) {
                Parser parser = entry.getByTag(ParserConstants.SCRIPT);
                StaticGroovyScript script = (StaticGroovyScript) parser.execute(e);
                flow.putAdapter(e.attributeValue(ParserConstants.NAME), script);
                continue;
            }

            // 如果节点为listener，不做解析，直接将listener加入flow
//            if (ParserConstants.LISTENER.equals(e.getName())) {
//                String className = e.attributeValue(ParserConstants.CLASS);
//                try {
//                    Class<?> clazz = Class.forName(className);
//                    flow.addListener(clazz);
//                } catch (ClassNotFoundException e1) {
//                    log.error("加载listener失败：" + e1.getLocalizedMessage(), e1);
//                }
//            }
        }
        // 不管是否有listener节点，都添加监听
        try {
            Class<?> clazz = Class.forName(SYNC_LISTENER);
            flow.addListener(clazz);
        } catch (ClassNotFoundException e1) {
            log.error("加载listener失败：" + e1.getLocalizedMessage(), e1);
        }

        return flow;
    }
}
