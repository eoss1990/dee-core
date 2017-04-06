package com.seeyon.v3x.dee.function;

import com.seeyon.v3x.dee.TransformContext;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.dictionary.Dictionary;
import com.seeyon.v3x.dee.enumerate.EnumerateConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DictionaryTag extends Tag {
    private static Log log = LogFactory.getLog(DictionaryTag.class);

    private Object key;

    private String dictionary;

    private TransformContext context;

    @SuppressWarnings("unchecked")
    @Override
    public Object execute() throws TransformException {
        if (context == null) {
            throw new TransformException("error:context is null");
        }

        Object value = null;
        if (dictionary.contains(".")) {        // 例：test.dictTest.gener
            StringBuilder dictPlugPath = new StringBuilder();
            dictPlugPath.append(dictionary.substring(0, dictionary.lastIndexOf(".") + 1));
            dictionary = StringUtils.trim(dictionary.replaceAll(dictPlugPath.toString(), ""));
            dictPlugPath = dictPlugPath.deleteCharAt(dictPlugPath.length() - 1);      // 删除掉最后的"."
            EnumerateConfig rc = new EnumerateConfig(dictPlugPath.toString());        // 读取系统&插件字典合集
            value = rc.getEnumValue(dictionary, key == null ? null : key.toString());
            if (value != null) {
                return value;
            }
        } else {
            EnumerateConfig rc = EnumerateConfig.getInstance();
            value = rc.getEnumValue(dictionary, key == null ? null : key.toString());
            if (value != null) {
                return value;
            }
        }
        if (context.lookup(dictionary) != null) {
            Dictionary dict = (Dictionary) context.getParameters().getValue(dictionary);
            if (dict == null) {
                dict = (Dictionary) (context.lookup(dictionary));
                if (dict == null) {
                    throw new TransformException("找不到指定的Dictionary：" + dictionary);
                }
                dict.load();
                context.getParameters().add(dictionary, dict);
            }
            value = dict.get(key);
        }

        // 如果未找到对应枚举值则返回key值
        if (value == null) {
            return key;
        }

        return value;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public TransformContext getContext() {
        return context;
    }

    public void setContext(TransformContext context) {
        this.context = context;
    }

    public String getDictionary() {
        return dictionary;
    }

    public void setDictionary(String dictionary) {
        this.dictionary = dictionary;
    }
}
