package com.seeyon.v3x.dee.config.parser.adapter;

import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.adapter.stmt.JudgementAdapter;
import com.seeyon.v3x.dee.config.ParserEntry;
import com.seeyon.v3x.dee.config.parser.Parser;
import com.seeyon.v3x.dee.config.parser.ParserConstants;
import com.seeyon.v3x.dee.adapter.script.groovy.StaticGroovyScript;
import org.dom4j.Element;

import java.util.List;

/**
 * @author zhangfb
 */
public class JudgementAdapterParser extends Parser {
    public JudgementAdapterParser(ParserEntry entry) {
        super(entry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object execute(Element element) {
        JudgementAdapter retAdapter = new JudgementAdapter();

        Element conditionE = element.element("condition");
        if (conditionE != null) {
            retAdapter.setCondition(conditionE.getText());
        }

        Element trueE = element.element("true");
        List<Element> trueEs  = trueE.elements();
        for (Element e : trueEs) {
            if (ParserConstants.ADAPTER.equals(e.getName()) || ParserConstants.PROCESSOR.equals(e.getName())) {
                Parser parser = entry.getByClass(e.attributeValue(ParserConstants.CLASS));
                if (parser == null) {
                    parser = entry.getDefaultParser();
                }
                Adapter adapter = (Adapter) parser.execute(e);
                retAdapter.addTrueAdapter(adapter);
            } else if (ParserConstants.SCRIPT.equals(e.getName())) {
                Parser parser = entry.getByTag(ParserConstants.SCRIPT);
                StaticGroovyScript script = (StaticGroovyScript) parser.execute(e);
                retAdapter.addTrueAdapter(script);
            }
        }

        Element falseE = element.element("false");
        List<Element> falseEs = falseE.elements();
        for (Element e : falseEs) {
            if (ParserConstants.ADAPTER.equals(e.getName()) || ParserConstants.PROCESSOR.equals(e.getName())) {
                Parser parser = entry.getByClass(e.attributeValue(ParserConstants.CLASS));
                if (parser == null) {
                    parser = entry.getDefaultParser();
                }
                Adapter adapter = (Adapter) parser.execute(e);
                retAdapter.addFalseAdapter(adapter);
            } else if (ParserConstants.SCRIPT.equals(e.getName())) {
                Parser parser = entry.getByTag(ParserConstants.SCRIPT);
                StaticGroovyScript script = (StaticGroovyScript) parser.execute(e);
                retAdapter.addFalseAdapter(script);
            }
        }

        return retAdapter;
    }
}
