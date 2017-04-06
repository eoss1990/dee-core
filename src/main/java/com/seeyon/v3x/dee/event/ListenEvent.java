package com.seeyon.v3x.dee.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事件监听注解，将方法注册为指定类型事件的监听器。<BR/>
 * 形如
 * <p>
 * @listenEvent(event=BeforeReadEvent.class)。
 * </p>
 * 适用于方法，方法名称不限，但返回值必须为void，只有一个Event或其子类类型的参数。如
 * <p>
 * <CODE>public void beforeReadData(BeforeReadEvent event)</CODE>
 * </p>
 * 
 * @author wangwy
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface ListenEvent {
	/**
	 * 监听的事件类型。
	 * 
	 * @return 监听的事件类型。
	 */
	Class<? extends Event> event();
}