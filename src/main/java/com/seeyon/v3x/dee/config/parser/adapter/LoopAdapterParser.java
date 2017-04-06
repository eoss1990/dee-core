package com.seeyon.v3x.dee.config.parser.adapter;

import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.adapter.stmt.LoopAdapter;
import com.seeyon.v3x.dee.config.ParserEntry;
import com.seeyon.v3x.dee.config.parser.Parser;
import com.seeyon.v3x.dee.config.parser.ParserConstants;
import com.seeyon.v3x.dee.adapter.script.groovy.StaticGroovyScript;
import org.dom4j.Element;

import java.util.List;

/**
 * @author zhangfb
 */
public class LoopAdapterParser extends Parser {
    public LoopAdapterParser(ParserEntry entry) {
        super(entry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object execute(Element element) {
        LoopAdapter retAdapter = new LoopAdapter();

        Element conditionE = element.element("condition");
        if (conditionE != null) {
            retAdapter.setCondition(conditionE.getText());
        }

        List<Element> elements  = element.elements();
        for (Element e : elements) {
            if (ParserConstants.ADAPTER.equals(e.getName()) || ParserConstants.PROCESSOR.equals(e.getName())) {
                Parser parser = entry.getByClass(e.attributeValue(ParserConstants.CLASS));
                if (parser == null) {
                    parser = entry.getDefaultParser();
                }
                Adapter adapter = (Adapter) parser.execute(e);
                retAdapter.addAdapter(adapter);
            } else if (ParserConstants.SCRIPT.equals(e.getName())) {
                Parser parser = entry.getByTag(ParserConstants.SCRIPT);
                StaticGroovyScript script = (StaticGroovyScript) parser.execute(e);
                retAdapter.addAdapter(script);
            }
        }

        return retAdapter;
    }
}
