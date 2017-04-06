package com.seeyon.v3x.dee;

import com.seeyon.v3x.dee.util.DocumentUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 转换数据的内部存储结构，描述一次转换过程中输入和输出数据（Processor和Outbound Adapter层的输入和输出）。<br/>
 * 可以是数据库中多个表的多条数据，也可以是一个树状复杂结构的XML。<br/>
 * 考虑数据交换的场景，所有数据都放到内存中，只支持千条级别数据。如果有十万百万级别数据，请分拆进行多次转换。
 * 对于XML数据源，使用DOM的Document Decorator实现保证对大XML文件的支持。
 * 
 * @author wangwenyou
 * 
 */
public interface Document {
	/**
	 * 取得Document对象的根元素。
	 * @return Document对象根元素。
	 */
	Element getRootElement();

	Element createElement(String name);

	Element createElement(String name, String value);
	
	/**
	 * 取得当前转换任务的上下文。
	 * @return 转换上下文。
	 */
	TransformContext getContext();
	/**
	 * 设置转换任务上下文。调用需谨慎，原来的设置将被覆盖。
	 * @param context 转换上下文。
	 */
	void setContext(TransformContext context);
	
	interface Element {
		/**
		 * 取得元素的所有直接子元素。
		 * @return 下级子元素。
		 */
		List<Element> getChildren();

		List<Element> getChildren(String name);

		List<Attribute> getAttributes();

		Element getChild(String name);

		Attribute getAttribute(String name);

		String getName();

		Object getValue();

		Element addChild(String name);
		
		Element addChild(Element element);

		Attribute setAttribute(String name, Object value);

		void setValue(Object value);

		//移除子节点
		void removeChild(String name);
	}

	interface Attribute{
		String getName();

		Object getValue();

		void setValue(Object value);
	}
}

class DocumentImpl extends AbstractDocument implements Serializable {
	private static final long serialVersionUID = 6069798556080628288L;
	private Document.Element root;
	public DocumentImpl(Document.Element root) {
		super();
		this.root = root;
	}

	public DocumentImpl(String rootElementName) {
		super();
		this.root = createElement(rootElementName);
	}

	@Override
	public Document.Element getRootElement() {
		return root;
	}

	@Override
	public Document.Element createElement(String name) {
		return new ElementImpl(name);
	}

	@Override
	public Document.Element createElement(String name, String value) {

		Document.Element element = createElement(name);
		element.setValue(value);
		return element;
	}

	class ElementImpl implements Document.Element,Serializable {
		private final String name;
		private Object value;
		private List<Document.Element> children;
		private Map<String, Document.Attribute> attributes;

		public ElementImpl(String name) {
			super();
			this.name = name;
			this.children = new ArrayList<Document.Element>();
			this.attributes = new LinkedHashMap<String, Document.Attribute>();
		}

		@Override
		public List<Document.Element> getChildren() {
			return children;
		}

		@Override
		public List<Document.Attribute> getAttributes() {
			return new ArrayList<Document.Attribute>(this.attributes.values());
		}

		@Override
		public Document.Element getChild(String name) {
			Object o = CollectionUtils.find(this.children,
					new ElementNamePredicate(name));
			return o == null ? null : (Document.Element) o;
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<Document.Element> getChildren(String name) {
			return new ArrayList<Document.Element>(CollectionUtils.select(this.children,
					new ElementNamePredicate(name)));
		}

		@Override
		public Document.Attribute getAttribute(String name) {
			if (name == null)
				return null;
			return this.attributes.get(name);
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public Object getValue() {
			return this.value;
		}

		@Override
		public Document.Element addChild(Document.Element element) {
			this.children.add(element);
			return element;
		}
		@Override
		public Document.Element addChild(String name) {
			return addChild(createElement(name));
		}

		@Override
		public Document.Attribute setAttribute(String name, Object value) {
			Document.Attribute attr = getAttribute(name);
			if (attr == null) {
				attr = new AttributeImpl(name, value);
				this.attributes.put(name, attr);
			} else {
				attr.setValue(value);
			}
			return attr;
		}

		@Override
		public void setValue(Object value) {
			this.value = value;
		}

		@Override
		public void removeChild(String name) {
			if (name != null && !"".equals(name)){
				int i = 0;
				for (Element e:this.children){
					if (e == null || e.getAttribute("name") == null) continue;
					if (name.equals(e.getAttribute("name").getValue())){
						this.children.remove(i);
						break;
					}
					i++;
				}
			}
		}

		@Override
		public String toString() {
			return DocumentUtil.toXML(this);
		}
	}

	static class AttributeImpl implements Document.Attribute,Serializable {
		private final String name;
		private Object value;

		public AttributeImpl(String name, Object value) {
			super();
			this.name = name;
			this.value = value;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public Object getValue() {
			return this.value;
		}

		@Override
		public void setValue(Object value) {
			this.value = value;
		}
		@Override
		public String toString() {
			return DocumentUtil.toXML(this);
		}
	}

	static class ElementNamePredicate implements Predicate,Serializable {
		private final String name;

		public ElementNamePredicate(String name) {
			super();
			this.name = name;
		}

		public boolean evaluate(Object paramObject) {
			if (paramObject == null)
				return false;
			if (paramObject instanceof Document.Element) {
				Document.Element ele = (Document.Element) paramObject;
				return ele.getName().equals(name);
			} else {
				return false;
			}
		}
	}
}
