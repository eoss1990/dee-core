package com.seeyon.v3x.dee.script;

import groovy.lang.Binding;
import javax.script.ScriptException;
import java.util.Map;

public class ScriptRunner {
	public ScriptRunner() {
	}

	/**
	 * 执行不含用户代码脚本（如函数字典等）
	 *
	 * @param codeText 脚本内容
	 * @param params   传入参数
	 * @return 脚本的返回值
	 * @throws ScriptException
	 */
	public Object eval(String codeText, Map<String, Object> params) throws ScriptException {
		try {
			//判断是否import用户代码
			//另写方法
			return GroovyCache.getInstance().eval(codeText,generateBinding(params));
		} catch (Exception e) {
			throw new ScriptException(e);
		}
	}

	/**
	 * 编译脚本
	 *
	 * @param codeText 脚本内容
	 * @return 脚本的返回值
	 * @throws ScriptException
	 */
	public Class<?> complie(String codeText) throws ScriptException {

		try {
			Class<?> classz = GroovyCache.getInstance().complie(codeText);
			return classz;
		} catch (Exception e) {
			throw new ScriptException(e);
		}
	}

	/**
	 * 将map参数对象，打包成Binding对象
	 *
	 * @param params 参数
	 * @return Binding键值对
	 */
	private Binding generateBinding(Map<String, Object> params) {
		Binding binding = new Binding();
		if (params != null) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				if (entry.getKey() != null) {
					binding.setVariable(entry.getKey(), entry.getValue());
				}
			}
		}
		return binding;
	}
}
