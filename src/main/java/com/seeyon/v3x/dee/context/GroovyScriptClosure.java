package com.seeyon.v3x.dee.context;

import com.seeyon.v3x.dee.util.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DEE Script tag上下文闭包封装的函数。
 * 
 * @author wangwenyou
 * 
 */
public class GroovyScriptClosure {
	private final static Log log = LogFactory.getLog(GroovyScriptClosure.class);

	private static String script = "";

	static {
		try {
			script = FileUtil.getResource("com/seeyon/v3x/dee/context/script_closure_function.groovy");
			log.debug("加载预置function完毕：" + script);
		} catch (Throwable e) {
			log.error("加载script_closure_function.groovy失败。" + e.getLocalizedMessage(), e);
		}
	}

	public static String getClosure() {
		return script;
	}
}
