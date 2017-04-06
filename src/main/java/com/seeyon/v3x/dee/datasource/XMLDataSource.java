package com.seeyon.v3x.dee.datasource;

import com.seeyon.v3x.dee.AbstractDocument;
import com.seeyon.v3x.dee.TransformFactory;
import com.seeyon.v3x.dee.util.DocumentUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * XML数据源。支持从文件读取XML数据和解析XML String。
 * 
 * @author wangwenyou
 * 
 */
public class XMLDataSource {
	private static Log log = LogFactory.getLog(XMLDataSource.class);
	private final SAXReader reader;
	
	private InputSource source;
	private String xmlFile;

	public XMLDataSource(File file) throws FileNotFoundException {
		super();
		reader = new SAXReader();
		this.source = new InputSource(new FileInputStream(file));
	}

	public XMLDataSource(String xml) {
		super();
		reader = new SAXReader();
		this.source = new InputSource(new StringReader(xml));
	}
	
	public XMLDataSource() {
		reader = new SAXReader();
	}

	/**
	 * SAXReader不支持XML中包含&符号，请大家使用中注意
	 * @return
	 * @throws DocumentException
	 */
	private Document load() throws DocumentException {
		
		return reader.read(source);
	}

	/**
	 * 解析XML数据，返回解析后的Document对象。
	 * 
	 * @return XML数据的Document对象。
	 * @throws org.dom4j.DocumentException
	 */
	public com.seeyon.v3x.dee.Document parse() throws DocumentException {

		return new DOM4JDocument(load());

	}

	static class DOM4JAttribute implements
			com.seeyon.v3x.dee.Document.Attribute {
		private final Attribute attribute;

		public DOM4JAttribute(Attribute attribute) {
			super();
			this.attribute = attribute;
		}

		@Override
		public String getName() {
			return this.attribute.getName();
		}

		@Override
		public Object getValue() {
			return this.attribute.getValue();
		}

		@Override
		public void setValue(Object value) {
			this.attribute.setValue(value.toString());

		}

	}

	/**
	 * DOM4J的Document decorator。
	 *
	 * @author wangwenyou
	 *
	 */
	class DOM4JDocument extends AbstractDocument implements com.seeyon.v3x.dee.Document {
		private com.seeyon.v3x.dee.Document.Element root;
		private Document source;
		public DOM4JDocument(Document document) {
			this.root = new DOM4JElement(document.getRootElement());
			this.source = document;
		}

		@Override
		public com.seeyon.v3x.dee.Document.Element createElement(String name) {
			throw new UnsupportedOperationException(
					"can't create standalone element,please use Element's addChild.");
		}

		@Override
		public com.seeyon.v3x.dee.Document.Element createElement(String name,
				String value) {
			throw new UnsupportedOperationException(
					"can't create standalone element,please use Element's addChild.");
		}

		@Override
		public Element getRootElement() {
			return root;
		}

		@Override
		public String toString() {
			StringWriter s = new StringWriter();
			XMLWriter writer = new XMLWriter(s);
			try {
				writer.write(this.source);
			} catch (IOException e) {
				log.error(e.getMessage(),e);
				return "";
			}
			return s.toString();
		}
		
		
	}

	/**
	 * Decorator，可能多个实例对应一个DOM Element。
	 * 
	 * @author wangwenyou
	 * 
	 */
	class DOM4JElement implements com.seeyon.v3x.dee.Document.Element {
		private final Element element;

		public DOM4JElement(Element element) {
			this.element = element;
		}

		/**
		 * 取得修饰的对象。
		 * 
		 * @return 修饰的DOM4J Element。
		 */
		public Element getSource() {
			return this.element;
		}

		@Override
		public com.seeyon.v3x.dee.Document.Element addChild(
				com.seeyon.v3x.dee.Document.Element element) {
			if (element instanceof DOM4JElement) {
				DOM4JElement e = (DOM4JElement) element;
				this.element.add(e.getSource());
			} else {
				throw new IllegalArgumentException("accept DOM4JElement only");
			}
			return element;
		}

		@Override
		public com.seeyon.v3x.dee.Document.Element addChild(String name) {
			return new DOM4JElement(this.element.addElement(name));
		}

		@Override
		public com.seeyon.v3x.dee.Document.Attribute getAttribute(String name) {
			Attribute attribute = this.element.attribute(name);
			if (attribute == null) {
				return null;
			}
			return new DOM4JAttribute(attribute);
		}

		@Override
		public List<com.seeyon.v3x.dee.Document.Attribute> getAttributes() {
			return transformAttributes(new ArrayList(this.element.attributes()));
		}

		@Override
		public com.seeyon.v3x.dee.Document.Element getChild(String name) {
			Element ele = this.element.element(name);
			return ele == null ? null : new DOM4JElement(ele);
		}

		@Override
		public List<com.seeyon.v3x.dee.Document.Element> getChildren() {
			return transformElements(new ArrayList(this.element.elements()));
		}

		@Override
		public List<com.seeyon.v3x.dee.Document.Element> getChildren(String name) {
			return transformElements(new ArrayList(this.element.elements(name)));
		}

		@Override
		public String getName() {
			return this.element.getName();
		}

		@Override
		public Object getValue() {
			// isNull="true"，返回null
			com.seeyon.v3x.dee.Document.Attribute attribute = getAttribute(DocumentUtil.ATTR_NULL_VALUE);
			if(attribute!=null && "true".equals(attribute.getValue())){
				return null;
			}
			return this.element.getText();
		}

		@Override
		public com.seeyon.v3x.dee.Document.Attribute setAttribute(String name,
				Object value) {
			com.seeyon.v3x.dee.Document.Attribute attr = getAttribute(name);
			if (attr == null) {
				this.element.addAttribute(name, value.toString());
				attr = getAttribute(name);
			} else {
				attr.setValue(value);
			}
			return attr;
		}

		@Override
		public void setValue(Object value) {
			this.element.setText(value.toString());
		}

		@Override
		public void removeChild(String name) {
			if (name != null && !"".equals(name)){
				Iterator<Element> it = element.elements().iterator();
				while (it.hasNext()){
					Element e = it.next();
					if (e == null || e.attribute("name") == null) continue;
					if (name.equals(e.attribute("name").getValue())){
						it.remove();
						break;
					}
				}
			}
		}

		private List<com.seeyon.v3x.dee.Document.Attribute> transformAttributes(
				List l) {
			CollectionUtils.transform(l, new Transformer() {
				@Override
				public Object transform(Object o) {
					if (o instanceof Attribute) {
						return new DOM4JAttribute((Attribute) o);
					} else {
						throw new IllegalArgumentException(
								"accept org.dom4j.Attribute only");
					}
				}
			});
			return l;
		}

		private List<com.seeyon.v3x.dee.Document.Element> transformElements(
				List l) {
			CollectionUtils.transform(l, new Transformer() {
				@Override
				public Object transform(Object o) {
					if (o instanceof Element) {
						return new DOM4JElement((Element) o);
					} else {
						throw new IllegalArgumentException(
								"accept org.dom4j.Element only");
					}
				}
			});
			return l;
		}
	}
	public void setXmlFile(String xmlFile) throws FileNotFoundException {
		File file = new File(xmlFile);
		if(!file.exists())
			file = new File(TransformFactory.getInstance().getConfigFilePath(xmlFile));
		this.xmlFile = file.getAbsolutePath();
		this.source = new InputSource(new FileInputStream(file));
	}
	public String getXmlFile() {
		return xmlFile;
	}

	/**
	 * 测试了一下，SAXReader不支持&符号，请大家使用的时候注意
	 * @param args
	 */
	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder("<root>");
		sb.append("<table>");
		sb.append("<row>");
		sb.append("<name>").append("yangyu & yu").append("</name>");
		sb.append("<PN.NO>").append("44/333/22").append("</PN.NO>");
		sb.append("</row>");
		sb.append("</table>");
		sb.append("</root>");

		XMLDataSource xmlDataSource = new XMLDataSource(sb.toString());
		try {
			com.seeyon.v3x.dee.Document document = xmlDataSource.parse();
		} catch (DocumentException e) {
			log.error("xml转化document异常：" + e);
		}
	}
}