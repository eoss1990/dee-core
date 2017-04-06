package com.seeyon.v3x.dee.config;

import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.stmt.JudgementAdapter;
import com.seeyon.v3x.dee.adapter.stmt.LoopAdapter;
import com.seeyon.v3x.dee.config.parser.ColumnMappingParser;
import com.seeyon.v3x.dee.config.parser.DataSourceParser;
import com.seeyon.v3x.dee.config.parser.DefaultParser;
import com.seeyon.v3x.dee.config.parser.FlowParser;
import com.seeyon.v3x.dee.config.parser.Parser;
import com.seeyon.v3x.dee.config.parser.ScheduleParser;
import com.seeyon.v3x.dee.config.parser.adapter.StaticGroovyScriptParser;
import com.seeyon.v3x.dee.config.parser.adapter.JudgementAdapterParser;
import com.seeyon.v3x.dee.config.parser.adapter.LoopAdapterParser;
import com.seeyon.v3x.dee.util.ReflectException;
import com.seeyon.v3x.dee.util.ReflectUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析执行入口
 *
 * @author zhangfb
 */
public class ParserEntry {
    private static Log log = LogFactory.getLog(ParserEntry.class);

    /**
     * 默认解析器
     */
    private Parser defaultParser = null;

    /**
     * 标签解析器
     */
    private Map<String, Parser> tagMap = new HashMap<String, Parser>();

    /**
     * 类名解析器
     */
    private Map<String, Parser> classMap = new HashMap<String, Parser>();

    /**
     * 待处理的ref引用列表
     */
    private List<RefBean> refCache = new ArrayList<RefBean>();


    public ParserEntry() {
        init();
    }

    @SuppressWarnings("unchecked")
    public EngineContext parse(Document document) throws TransformException {
        EngineContext ctx = new EngineContext();

        Element root = document.getRootElement();
        List<Element> elements = root.elements();

        // 遍历根节点下所有节点，这些节点根据标签解析器进行解析。
        // 属性为name的值为ID标示，再加上解析器解析之后的对象，一起放入ctx的键值对中。
        for (Element element : elements) {
            Parser parser = getByTag(element.getName());
            if (parser != null) {
                String id = element.attributeValue("name");
                Object obj = parser.execute(element);
                if (obj != null) {
                    ctx.add(id, obj);
                }
            }
        }

        // 加载ref引用
        loadCache(ctx);

        return ctx;
    }

    public Parser getByTag(String name) {
        return tagMap.get(name);
    }

    public Parser getByClass(String name) {
        Parser parser = classMap.get(name);
        return parser != null ? parser : defaultParser;
    }

    public Parser getDefaultParser() {
        return defaultParser;
    }

    public void putRefCache(RefBean refBean) {
        refCache.add(refBean);
    }

    private void init() {
        defaultParser = new DefaultParser(this);

        tagMap.put("flow", new FlowParser(this));
        tagMap.put("dictionary", defaultParser);
        tagMap.put("schedule", new ScheduleParser(this));
        tagMap.put("datasource", new DataSourceParser(this));
        tagMap.put("column-mapping", new ColumnMappingParser(this));
        tagMap.put("script", new StaticGroovyScriptParser(this));

        classMap.put(JudgementAdapter.class.getName(), new JudgementAdapterParser(this));
        classMap.put(LoopAdapter.class.getName(), new LoopAdapterParser(this));
    }

    /**
     * 加载ref引用
     *
     * @param ctx 引擎上下文
     */
    private void loadCache(EngineContext ctx) {
        for (RefBean refBean : refCache) {
            // 获取引用ID
            String refId = refBean.getRefId();

            // 从引擎上下文中获取引用对象，如果没有获取到，则使用默认值
            Object value = ctx.lookup(refId);
            if (value == null) {
                value = refBean.getDefaultValue();
            }

            try {
                // 调用方法如：obj.methodName(value)
                Object obj = refBean.getObj();
                String fieldName = refBean.getFieldName();
                ReflectUtil.invokeMethodByFieldName(obj, fieldName, value);
            } catch (ReflectException e) {
                log.error("加载引用对象时出错：ref=" + refId + "，" + e.getLocalizedMessage(), e);
            }
        }
    }
}
