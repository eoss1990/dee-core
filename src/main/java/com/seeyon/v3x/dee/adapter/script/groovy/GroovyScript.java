package com.seeyon.v3x.dee.adapter.script.groovy;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.v3x.dee.TransformFactory;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

/**
 * 获取外部Groovy脚本文件提供调用执行
 * 
 * @author wuyz
 * 
 */
public class GroovyScript {
	private static Log log = LogFactory.getLog(GroovyScript.class);
	private GroovyObject groovyObject;
	/**
	 * 外部groovy文件路径
	 */
	private String path;
	/**
	 * key：方法名 value：参数对象数组
	 */
	private Map<String, Object[]> methodMap;

	public Object execute(String methodName) {
		ClassLoader parent = getClass().getClassLoader();
		GroovyClassLoader loader = null;
		try {
			loader = new GroovyClassLoader(parent);
			Class groovyClass = loader.parseClass(new File(TransformFactory
					.getInstance().getHomeDirectory() + path));
			groovyObject = (GroovyObject) groovyClass.newInstance();
			return groovyObject.invokeMethod(methodName,
					methodMap.get(methodName));
		} catch (Exception e) {
			log.error("执行脚本方法异常：" + e);
			return null;
		} finally {
			try {
				if(loader != null){
					loader.close();
				}
			} catch (IOException e) {
				log.error("GroovyClassLoader关闭异常：" + e);
			}
		}
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Map getMethodMap() {
		return methodMap;
	}

	public void setMethodMap(Map<String, Object[]> methodMap) {
		this.methodMap = methodMap;
	}

}
