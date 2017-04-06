package com.seeyon.v3x.dee.adapter.webservice.addition;

import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AddSoapHeader extends AbstractSoapInterceptor {
	
	private static String headerName;
	private static Map<String,String> fieldMap;
	private static String nameSpace;
	
	public AddSoapHeader(String nameSpace,String headerName,Map<String,String> fieldMap){
        super(Phase.WRITE);
        setHeaderName(headerName);
        setFieldMap(fieldMap);
        setNameSpace(nameSpace);
    }

	@Override
	public void handleMessage(SoapMessage message) throws Fault {
		QName qName=new QName(headerName);
		Document doc = DOMUtils.createDocument();
		Element root = doc.createElementNS(nameSpace,headerName);
		
		for(Entry<String, String> entry:fieldMap.entrySet())
		{
			Element ele=doc.createElement(entry.getKey());
			ele.setNodeValue(entry.getValue());
//			ele.setTextContent(entry.getValue());
			root.appendChild(ele);
		}
		
		SoapHeader header = new SoapHeader(qName, root);
		List<Header> headers = message.getHeaders();
		headers.add(header);
	}

	public static String getHeaderName() {
		return headerName;
	}

	public static void setHeaderName(String headerName) {
		AddSoapHeader.headerName = headerName;
	}

	public static Map<String, String> getFieldMap() {
		return fieldMap;
	}

	public static void setFieldMap(Map<String, String> fieldMap) {
		AddSoapHeader.fieldMap = fieldMap;
	}

	public static String getNameSpace() {
		return nameSpace;
	}

	public static void setNameSpace(String nameSpace) {
		AddSoapHeader.nameSpace = nameSpace;
	}

}
