package com.seeyon.v3x.dee.config.parser;

import com.seeyon.v3x.dee.config.ParserEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

/**
 * 数据源解析器
 *
 * @author zhangfb
 */
public class DataSourceParser extends Parser {
    private static Log log = LogFactory.getLog(DataSourceParser.class);

    public DataSourceParser(ParserEntry entry) {
        super(entry);
    }

    @Override
    public Object execute(Element element) {
        return entry.getDefaultParser().execute(element);
    }
}
