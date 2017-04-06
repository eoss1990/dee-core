package com.seeyon.v3x.dee;

/**
 * 参数，封装向XML配置和转换传递的数据。作用域为全局或具体的转换任务。应用对象：Flow、DataSource等。
 * 参数由String型的名称和Object值组成
 * ，为了解除XML配置的局限性，值可以支持groovy表达式。这样就把参数值简化为具体值和脚本两类，同时也可以解决bean引用的问题。
 * 
 * @author wangwenyou
 * 
 */
public interface Parameter {
	String getName();
	Object getValue();
	void setValue(Object value);
	void setScript(String script);
}
