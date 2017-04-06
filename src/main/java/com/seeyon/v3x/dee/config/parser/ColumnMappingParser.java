package com.seeyon.v3x.dee.config.parser;

import com.seeyon.v3x.dee.config.ParserEntry;
import com.seeyon.v3x.dee.adapter.colmap.ColumnMapping;
import org.dom4j.Element;

import java.util.List;

/**
 * @author zhangfb
 */
public class ColumnMappingParser extends Parser {
    public ColumnMappingParser(ParserEntry entry) {
        super(entry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object execute(Element element) {
        ColumnMapping mapping = new ColumnMapping();

        List<Element> propertyList = element.elements("property");

        for (Element e : propertyList) {
            String target = e.attributeValue("target");
            target = target == null ? e.attributeValue("name") : target;
            String src = e.attributeValue("source");
            src = src == null ? e.attributeValue("data") : src;
            String expr = e.attributeValue("expr");
            expr = expr == null ? e.attributeValue("decoder") : expr;
            mapping.mapping(src, target, expr);
        }
        return mapping;
    }
}
