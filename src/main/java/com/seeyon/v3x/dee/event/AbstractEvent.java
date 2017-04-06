package com.seeyon.v3x.dee.event;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.TransformContext;

/**
 * 事件基本抽象实现，contextable。
 * @author wangwenyou
 *
 */
public class AbstractEvent extends Event {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4317041932047893994L;
	private TransformContext context;
	private Document document;

	public AbstractEvent(Object source) {
		super(source);
	}

	/**
	 * 设置转换上下文。
	 * 
	 * @param context
	 *            转换上下文。
	 * @return 当前事件实例。
	 */
	public Event setContext(TransformContext context) {
		this.context = context;
		return this;
	}

	/**
	 * 取得当前的转换上下文。
	 * 
	 * @return 当前转换任务的转换上下文。
	 */
	public TransformContext getContext() {
		return this.context;
	}

	public Document getDocument() {
		return document;
	}

	public Event setDocument(Document document) {
		this.document = document;
		return this;
	}

}
