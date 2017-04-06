package com.seeyon.v3x.dee.config;

import com.seeyon.v3x.dee.TransformFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import java.io.File;

/**
 * 读取配置文件
 *
 * @author zhangfb
 */
public class EngineConfig {
    private static Log log = LogFactory.getLog(EngineConfig.class);

    public static final String FILENAME_MAIN_CONFIG = "dee.xml";

    private static EngineConfig instance = new EngineConfig();

    private EngineConfig() {
    }

    public static EngineConfig getInstance() {
        return instance;
    }

    /**
     * 解析引擎核心文件dee.xml
     *
     * @param configFilePath 配置文件路径
     * @return 引擎上下文
     * @throws Exception
     */
    public EngineContext parse(String configFilePath) throws Exception {
        File file = new File(configFilePath);
        SAXReader saxReader = new SAXReader();

        Document document = saxReader.read(file);
        return new ParserEntry().parse(document);
    }

    /**
     * 不传入参数，默认解析dee.xml
     * @return 引擎上下文
     * @throws Exception
     */
    public EngineContext parse() throws Exception {
        String deeConfigFile = TransformFactory.getInstance().getConfigFilePath(FILENAME_MAIN_CONFIG);
        return parse(deeConfigFile);
    }
}