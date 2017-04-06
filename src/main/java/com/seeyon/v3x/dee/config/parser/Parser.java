package com.seeyon.v3x.dee.config.parser;

import com.seeyon.v3x.dee.config.ParserEntry;
import org.dom4j.Element;

/**
 * 解析器抽象类
 *
 * @author zhangfb
 */
public abstract class Parser {
    protected ParserEntry entry;

    public abstract Object execute(Element element);

    public Parser(ParserEntry entry) {
        this.entry = entry;
    }
}
