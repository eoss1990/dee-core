package com.seeyon.v3x.dee;

import java.io.File;

/**
 * 转换工厂，转换的入口，创建Document对象
 *
 * @author zhangfb
 */
public class TransformFactory {
    private static final TransformFactory INSTANCE = new TransformFactory();

    private String home;

    private TransformFactory() {
        home = System.getProperty(DEEConstants.DEE_HOME);
        if (home == null) {
            home = System.getenv(DEEConstants.DEE_HOME);
        }
        // home = "D:\\dee\\DEE_V2.1\\DEE_V2.0SP1\\DEE_HOME";
    }

    public static TransformFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 创建一个指定根元素名称的文档对象
     *
     * @param rootTagName 根元素名称
     * @return 文档对象实例
     */
    public Document newDocument(String rootTagName) {
        return new DocumentImpl(rootTagName);
    }

    /**
     * 取得配置文件的路径
     *
     * @param name 配置文件名
     * @return 配置文件路径
     */
    public String getConfigFilePath(String name) {
        StringBuilder builder = new StringBuilder();

        if (home != null) {
            builder.append(home);
        }
        builder.append(File.separator).append("conf").append(File.separator).append(name);

        return builder.toString();
    }

    public String getPluginFilePath(String fileName) {
        return home + "/plugin/" + fileName;
    }

    /**
     * 取得DEE的主目录
     *
     * @return 主目录名
     */
    public String getHomeDirectory() {
        return home;
    }
}
