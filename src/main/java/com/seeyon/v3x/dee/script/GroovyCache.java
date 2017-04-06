package com.seeyon.v3x.dee.script;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import javax.script.ScriptException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dkywolf on 2016-11-29.
 * 获取脚本缓存
 */
public class GroovyCache {
    private static GroovyCache instance;
    //将缓存对象改为缓存类
    private final Map<String,Class<?>> gCache = new HashMap<String,Class<?>>();
    //private final Map<String,Script> gCache = new HashMap<String,Script>();
    //记录任务与脚本映射<flowId,脚本>
    //private final Map<String,String>
    private GroovyClassLoader groovyCl;
    private GroovyCache(){
        groovyCl = new GroovyClassLoader(getClass().getClassLoader());
    }
    public static GroovyCache getInstance(){
        if (instance == null){
            synchronized (GroovyCache.class){
                if (instance == null){
                    instance = new GroovyCache();
                }
            }
        }
        return instance;
    }


    /**
     * 执行脚本
     *
     * @param codeText 脚本内容
     * @param binding   传入参数
     * @return 脚本的返回值
     * @throws ScriptException
     */
    public Object eval(String codeText,Binding binding) throws ScriptException {
        try {
            Object result = excute(codeText,binding);
            return result;
        } catch (Exception e) {
            throw new ScriptException(e);
        }
    }

    /**
     * 编译脚本
     *
     * @param codeText 脚本内容
     * @return 脚本的class
     * @throws ScriptException
     */
    public Class<?> complie(String codeText) throws ScriptException {

        try {
            //加载用户代码库
            //loadCode(importList);
            //编译当前脚本
            Class<?> clazz = groovyCl.parseClass(codeText);
            return clazz;
        } catch (Exception e) {
            throw new ScriptException(e);
        }
    }

    private Object excute(String codeText, Binding binding) throws ScriptException{
        try {
            Class<?> clazz = gCache.get(codeText);
            if (clazz == null){
                synchronized (gCache){
                    clazz = gCache.get(codeText);
                    if (clazz == null){
                        clazz = groovyCl.parseClass(codeText);
                        gCache.put(codeText, clazz);
                    }
                }
            }

            Script script = (Script) clazz.newInstance();
            script.setBinding(binding == null ? new Binding() : binding);
            // 执行脚本
            Object result = script.run();
            return result;
        }
        catch (Exception e){
            throw new ScriptException(e);
        }
    }
    //加载引入代码库
    public void loadCode(File[] impFiles) throws ScriptException {
        try {
            for (File im:impFiles){
                if (im == null) continue;
                if (!im.exists()) continue;
                if (!im.isFile()) continue;
                groovyCl.parseClass(im);
            }
        }
        catch (Exception e){
            throw new ScriptException(e);
        }
    }

    //清理缓存
    public void clearGCache(){
//        Map var1 = gCache;
        synchronized(this) {
            gCache.clear();
            groovyCl.clearCache();
        }
    }
}
