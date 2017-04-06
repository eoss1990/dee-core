package com.seeyon.v3x.dee.function;

import com.seeyon.v3x.dee.TransformException;

import java.util.Map;

public interface FunctionEngine {

	/**
	 * @description  根据tag名称，tag的属性与值组成的键值对，执行tag
	 * @date 2011-9-16
	 * @author liuls
	 * @param tagName tag名称
	 * @param map  tag的属性与值组成的键值对
	 * @throws com.seeyon.v3x.dee.TransformException
	 */
	public abstract Object executeTag(String tagName, Map<String, Object> map) throws TransformException;

}