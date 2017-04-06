package com.seeyon.v3x.dee.util;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Document.Element;
import com.seeyon.v3x.dee.TransformFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import java.util.Iterator;
import java.util.List;

public class DocumentUtil {
	/**
	 * 标识当前Element的值为null的属性名称。
	 * 没有使用namespace，以避免带来不必要的复杂度。只供内部Document.toString和XMLDataSource.parse使用。
	 */
	public final static String ATTR_NULL_VALUE = "dee_isNull";
	public static String toXML(Document document){
		StringBuffer xml = new StringBuffer();
		xml.append(toXML(document.getRootElement()));
		return xml.toString();
	}
	public static String toXML(Document.Element element){
		Object value = element.getValue();
		StringBuffer xml = new StringBuffer();
		xml.append("<").append(element.getName());
		xml.append(toXML(element.getAttributes()));
		if(value==null && element.getChildren().size()==0 ){
			xml.append(" ").append(ATTR_NULL_VALUE).append("=\"true\"");
		}
		xml.append(">");
		if(value!=null){
			xml.append(escape(value));
		}
		for(Document.Element e:element.getChildren()){
			xml.append(toXML(e));
		}
		xml.append("</").append(element.getName()).append(">");
		return xml.toString();
	}
	public static String toXML(Document.Attribute attr){
		StringBuffer xml = new StringBuffer();
		xml.append(attr.getName()).append("=");
		xml.append("\"").append(escape(attr.getValue())).append("\"");
		return xml.toString();
	}
	public static String toXML(List<Document.Attribute> attrs){
		if(attrs.size()==0)
			return "";
		StringBuffer xml = new StringBuffer();
		for (Document.Attribute attr : attrs) {
			xml.append(toXML(attr)).append(" ");
		}
		return " " + xml.toString().trim();
	}
	/**
	 * 合并两个Document，返回一个新的Document实例。
	 * @param src 来源Document
	 * @param target
	 * @return 合并后的新的Document，root的name与第一个Document相同。
	 */
	public static Document merge(Document src, Document target) {
		Element srcRoot = src.getRootElement();
		Document doc = TransformFactory.getInstance().newDocument(srcRoot.getName());
		// 合并源
		doc = mergeSingle(doc, src);
		// 合并目标
		doc = mergeSingle(doc, target);
		// 设置上下文
		if (src.getContext() != null) {
			doc.setContext(src.getContext());
		}
		return doc;
	}

	/**
	 * 单一合并
	 * @param doc 当前Document
	 * @param mergedDoc 需要合并的Document
	 * @return 当前Document
	 */
	private static Document mergeSingle(Document doc, Document mergedDoc) {
        for (Element element : mergedDoc.getRootElement().getChildren()) {
            element.setAttribute("count", element.getChildren().size());
            if (element.getAttribute("totalCount") != null) {
                element.setAttribute("totalCount", element.getAttribute("totalCount").getValue());
            } else {
                element.setAttribute("totalCount", element.getChildren().size());
            }
            doc.getRootElement().addChild(element);
        }
        return doc;
	}

	/**
	 * @description 将其他xml解析成Dee document
	 * @date 2012-3-28
	 * @author liuls
	 * @param xml xml、字符串
	 * @return
	 * @throws DocumentException
	 */
	@SuppressWarnings("unchecked")
	public static Document parse(String xml) throws DocumentException{
		  org.dom4j.Document sDoc = (org.dom4j.Document) DocumentHelper.parseText(xml);
		  org.dom4j.Element re = sDoc.getRootElement();
		  Document deeDoc =TransformFactory.getInstance().newDocument(re.getName());
		  Iterator<org.dom4j.Element> sonIt = re.elementIterator();
		  while(sonIt.hasNext()){
			  parseElem(sonIt.next(),deeDoc.getRootElement());
		  }
		  return deeDoc;
			//Element root = document.getRootElement();
	}

	/**
	 * @description 指定的第二级节点下，根据第四级节点字段名与值移除第三级节点
	 * @date 2013-10-11
	 * @author dkywolf
	 * @param
	 * @return
	 * @throws DocumentException
	 */
	@SuppressWarnings("unchecked")
	public static Document remove(Document doc,String secName,String thrFieldName,String fieldValue) throws DocumentException{
		if(doc == null || "".equals(secName) || "".equals(thrFieldName) || "".equals(fieldValue))
			return doc;
		Element srcRoot = doc.getRootElement();
		Document newDoc = TransformFactory.getInstance().newDocument(srcRoot.getName());
        for (Element el : srcRoot.getChildren()) {
        	if(secName.equals(el.getName())){
        		Element newEl = newDoc.createElement(secName);
        		for(Element son:el.getChildren()){
        			if(!fieldValue.equals(getElValByName(son,thrFieldName)))
        				newEl.addChild(son);
        		}
        		newEl.setAttribute("count", newEl.getChildren().size());
            	newEl.setAttribute("totalCount", newEl.getChildren().size());
        		newDoc.getRootElement().addChild(newEl);
        	}
        	else
        		newDoc.getRootElement().addChild(el);

        }
		return newDoc;
	}

	//获取子节点信息忽略节点名大小写
	public static Object getElValByName(Element element,String fieldName){
		Object ret = null;
		if(element == null || fieldName == null)
			return ret;
		for(Element el: element.getChildren()){
			if(el == null) continue;
			if(fieldName.equalsIgnoreCase(el.getName())){
				ret = el.getValue();
				break;
			}
		}
		return ret;
	}

	/**
	 * @description 解析单个的Elment节点
	 * @date 2012-3-28
	 * @author liuls
	 * @param se source element ,dom4j的Element
	 * @param parentTargetElem, dee Element，且为父节点
	 */
	@SuppressWarnings({ "unchecked" })
	private static void parseElem(org.dom4j.Element se,com.seeyon.v3x.dee.Document.Element parentTargetElem ){
		Element tElem = parentTargetElem.addChild(se.getName());
		Iterator<Attribute> it = se.attributeIterator();
		Attribute a = null;
		while(it.hasNext()){
		   a = it.next();
		   tElem.setAttribute(a.getName(),a.getValue());
		}
		tElem.setValue(se.getText());
		
		List<org.dom4j.Element> sonIt = se.elements();
		for(org.dom4j.Element e: sonIt){
			 parseElem(e,tElem);
		}
		

		
	}
	private static String escape(Object value){
		if(value==null) return "";
		return StringEscapeUtils.escapeXml(value.toString());
	}

	/**
	 * 解决嵌套CDATA问题，将要放到CDATA中的数据进行转换。
	 * "]]>" needs to be broken into multiple CDATA segments, like: "Foo]]>Bar"
	 * becomes "<![CDATA[Foo]]]]><![CDATA[>Bar]]>" (the end of the first CDATA
	 * has the "]]", the other has ">")
	 * 
	 * @param unescaped
	 * @return
	 */
	public static String escapeCDATA(String unescaped) {
		if(unescaped.indexOf("<![CDATA[")<0) return unescaped;
		String escaped = unescaped.replaceAll("]]>", "]]]]><![CDATA[>");
		return escaped;
	}
}
