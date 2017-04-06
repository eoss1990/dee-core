package com.seeyon.v3x.dee.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事件管理器，事件触发和分发。为了保证隔离，每一个Task有一个独立的事件管理器实例。
 * 
 * @author wangwy
 * 
 */
public class EventDispatcher {
	private static final Log logger = LogFactory.getLog(EventDispatcher.class);

	private ListenerRegistry registry;

	public EventDispatcher() {
		this.registry = new ListenerRegistry();
	}

	/**
	 * 触发事件。
	 * 
	 * @param event
	 *            事件对象。
	 */
	public void fireEvent(Event event) {
		List<EventListener> listeners = this.registry.getListener(event
				.getClass());
		if (listeners.size() > 0) {
			for (EventListener listener : listeners) {
				try {
					listener.handle(event);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		} else {
			logger.debug("没有注册事件监听器：" + event.getClass().getCanonicalName());
		}

	}

	/**
	 * 解析指定类中的annotation监听并注册。
	 */
	public void parse(final Class clazz) {
		for (final Method m : clazz.getMethods()) {
			if (m.isAnnotationPresent(ListenEvent.class)) {
				try {
					ListenEvent annotation = m.getAnnotation(ListenEvent.class);
					Class<? extends Event> event = annotation.event();
					this.register(event, new EventListener() {
						@Override
						public void handle(Event event)
								throws IllegalArgumentException,
								IllegalAccessException,
								InvocationTargetException, InstantiationException {
							Object object = clazz.newInstance();
							m.invoke(object,event);

						}
					});

				} catch (Throwable ex) {
					logger.error(ex.getMessage() + " :" + m.getName(), ex);
				}
			}
		}
	}

	/**
	 * 注册事件监听。
	 * 
	 * @param eventType
	 *            事件类型，Event的子类，如BeforReadEvent.class
	 * @param listener
	 *            事件监听器。
	 */
	public void register(Class<? extends Event> eventType,
			EventListener listener) {
		this.registry.register(eventType, listener);
	}

	/**
	 * 事件监听器接口，仅供事件模型内部使用。
	 * 
	 * @author wangwy
	 * 
	 */
	interface EventListener {
		void handle(Event event) throws IllegalArgumentException,
				IllegalAccessException, InvocationTargetException, InstantiationException;
	}

	/**
	 * 事件监听器注册表，仅供事件模型内部使用。
	 * 
	 * @author wangwy
	 * 
	 */
	static class ListenerRegistry {

		private byte[] lock = new byte[0];
		private Map<String, List<EventListener>> listenerMap = new HashMap<String, List<EventListener>>();

		public ListenerRegistry() {
		}

		/**
		 * 按事件类型取得监听器列表。
		 * 
		 * @param eventType
		 * @return
		 */
		public List<EventListener> getListener(Class<? extends Event> eventType) {
			String key = buildKey(eventType);
			List<EventListener> listeners = listenerMap.get(key);
			return listeners == null ? new ArrayList<EventListener>(0)
					: listeners;
		}

		private String buildKey(Class<? extends Event> eventType) {
			String key = eventType.getCanonicalName();
			return key;
		}

		/**
		 * 注册监听器。
		 * 
		 * @param eventType
		 * @param listener
		 */
		public void register(Class<? extends Event> eventType,
				EventListener listener) {
			String key = buildKey(eventType);
			List<EventListener> list = listenerMap.get(key);
			synchronized (lock) {
				if (list == null) {
					list = new ArrayList<EventListener>();
					listenerMap.put(key, list);
				}
				list.add(listener);
			}
		}
	}
}
