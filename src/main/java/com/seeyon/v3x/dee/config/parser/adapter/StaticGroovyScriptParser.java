package com.seeyon.v3x.dee.config.parser.adapter;

import com.seeyon.v3x.dee.config.ParserEntry;
import com.seeyon.v3x.dee.config.parser.Parser;
import com.seeyon.v3x.dee.adapter.script.groovy.StaticGroovyScript;
import org.dom4j.Element;

/**
 * @author zhangfb
 */
public class StaticGroovyScriptParser extends Parser {
    public StaticGroovyScriptParser(ParserEntry entry) {
        super(entry);
    }

    @Override
    public Object execute(Element element) {
        StaticGroovyScript script = new StaticGroovyScript();
        script.setCodeText(element.getText());
        return script;
    }
}
