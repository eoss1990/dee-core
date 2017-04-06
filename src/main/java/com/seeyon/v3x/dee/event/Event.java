package com.seeyon.v3x.dee.event;

import java.util.EventObject;

/**
 * 事件对象，封装事件的上下文。<BR/>
 * 每一个事件对象唯一标识一类事件，不允许一个事件对象多种用途。<BR/>
 * 例如BeforeReadEvent只用于标识Reader执行前触发的事件，不允许重用或继承。<BR/>
 * 
 * 系统支持两种事件监听模式，一种是独立的脚本监听，只在XML配置定义中有效，由其书写位置确定是before还是after某某Reader...。<BR/>
 * 另外一种是基于Event接口，注解驱动监听，由每一个转换任务的EventDispacher实例分发的模型。
 * 为了避免重用时无法区分，事件监听只对特定任务有效，不绑定到Reader、Writer或Processor。<BR/>
 * 
 * @author wangwy
 * 
 */
public class Event extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1217740164841361028L;
	
	private String message;

	/**
	 * 事件构造器。
	 * 
	 * @param source
	 *            触发事件的来源对象。
	 */
	public Event(Object source) {
		super(source);
	}

	public String getMessage() {
		return message;
	}

	public Event setMessage(String message) {
		this.message = message;
		return this;
	}

}