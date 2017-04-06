package com.seeyon.v3x.dee.common.a8rest.parse;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Document.Element;
import com.seeyon.v3x.dee.TransformFactory;
import com.seeyon.v3x.dee.datasource.XMLDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 组织机构结果解析器
 *
 * @author zhangfb
 */
public class OrgParser {
    private final static Log log = LogFactory.getLog(OrgParser.class);

    /**
     * A8岗位信息转换到Document
     * @param str
     * @param type
     * @return
     */
    public static Document postsTransport(String str, String type)	{
    	if("application/xml".equals(type))
    		return Transport(str,"A8_POSTS");
    	else
    		return null;
    }
    
    /**
     * A8部门信息转换到Document
     * @param str
     * @param type
     * @return
     */
    public static Document deptTransport(String str, String type)	{
    	if("application/xml".equals(type))
    		return Transport(str,"A8_DEPTS");
    	else
    		return null;
    }
    
    /**
     * A8人员信息转换到Document
     * @param str
     * @param type
     * @return
     */
    public static Document membersTransport(String str,String type){
    	if("application/xml".equals(type))
    		return Transport(str,"A8_MEMBERS");
    	else
    		return null;
    }
    
    /**
     * A8通讯录信息转换到Document
     * @param list
     * @param type
     * @return
     */
    public static Document addressTransport(List list,String type){
    	
    	if("application/json".equals(type))
    	{
    		Document doc = TransformFactory.getInstance().newDocument("root");
        	Element address = doc.createElement("A8_ADDRESS");
        	for(int i=0;i<list.size();i++)
        	{
        		Element row = doc.createElement("row");
        		Map<String,String> map = (Map) list.get(i);
        		for(Entry entry:map.entrySet())
        		{
        			Element rowField = doc.createElement(entry.getKey().toString());
        			rowField.setValue(entry.getValue());
    /*    			if(entry.getValue()!=null)
        			{
        				rowField.setValue(entry.getValue().toString());
        			}
        			else
        			{
        				rowField.setValue("");
        			}*/
        			row.addChild(rowField);
        		}
        		address.addChild(row);
        	}
        	doc.getRootElement().addChild(address);
        	return doc;
    	}else
    	{
    		return null;
    	}
    	
    }
    
    
    /**
     * A8XML(DataPojo)格式转换到Document
     * @param strXml
     * @param tableName
     * @return
     */
    private static Document Transport (String strXml, String tableName)
    {
    	Document doc = TransformFactory.getInstance().newDocument("root");
    	Document newInput=null;
        try {
			newInput = new XMLDataSource(strXml).parse();
		} catch (DocumentException e) {
			log.error(e.getMessage(), e);
		}
        Element root = newInput.getRootElement();
        Element dataProperty = root.getChild("DataProperty");
        List<Element> dataPojo = dataProperty.getChildren("DataPojo");
        
        Element posts = doc.createElement(tableName);
        for(Element po:dataPojo)
        {
        	Element row = doc.createElement("row");
        	for(Element field:po.getChildren())
        	{
        		Element rowField = doc.createElement(field.getAttribute("propertyname").getValue().toString());
        		if(field.getChildren().size()>0)
        		{
        			for(Element fieldSon:field.getChildren())
        			{
        				rowField.addChild(fieldSon);
        			}
        		}
        		else
        		{
        			if(field.getAttribute("value")==null)
            		{
            			rowField.setValue(field.getValue());
            		}else
            		{
            			rowField.setValue(field.getAttribute("value").getValue());
            		}
        		}
        		row.addChild(rowField);
        	}
        	posts.addChild(row);
        }
        posts.setAttribute("totalCount", dataPojo.size());
		posts.setAttribute("count", dataPojo.size());
        doc.getRootElement().addChild(posts);
        return doc;
        
    }
    
    public static Document organizationTransport (String strXml,String type)
    {
    	if("application/xml".equals(type))
    	{
    		Document doc = TransformFactory.getInstance().newDocument("root");
        	Document newInput=null;
            try {
    			newInput = new XMLDataSource(strXml).parse();
    		} catch (DocumentException e) {
    			log.error(e.getMessage(), e);
    		}
            Element root = newInput.getRootElement();
            List<Element> dataProperty = root.getChildren("DataProperty");
            
            for(Element dpo:dataProperty)
            {
            	Element table = doc.createElement(dpo.getAttribute("propertyname").getValue().toString());
            	List<Element> dataPojo = dpo.getChildren("DataPojo");
            	for(Element po:dataPojo)
                {
                	Element row = doc.createElement("row");
                	for(Element field:po.getChildren())
                	{
                		Element rowField = doc.createElement(field.getAttribute("propertyname").getValue().toString());
                		if(field.getChildren().size()>0)
                		{
                			for(Element fieldSon:field.getChildren())
                			{
                				rowField.addChild(fieldSon);
                			}
                		}
                		else
                		{
                			if(field.getAttribute("value")==null)
                    		{
                    			rowField.setValue(field.getValue());
                    		}else
                    		{
                    			rowField.setValue(field.getAttribute("value").getValue());
                    		}
                		}
                		row.addChild(rowField);
                	}
                	table.addChild(row);
                }
            	
            	table.setAttribute("totalCount", dataPojo.size());
            	table.setAttribute("count", dataPojo.size());
                doc.getRootElement().addChild(table);	
            }

            return doc;
    	}else
    	{
    		return null;
    	}
    	
    }

    public static Document getOrgLevels(String xml, String type) {
        if ("application/xml".equals(type)) {
            Document doc = TransformFactory.getInstance().newDocument("root");
            Document newInput = null;
            try {
                newInput = new XMLDataSource(xml).parse();
            } catch (DocumentException e) {
                log.error(e.getLocalizedMessage(), e);
                return null;
            }
            Element root = newInput.getRootElement();
            List<Element> dataProperty = root.getChildren("DataProperty");

            for (Element dpo : dataProperty) {
                Element table = doc.createElement(dpo.getAttribute("propertyname").getValue().toString());
                List<Element> dataPojo = dpo.getChildren("DataPojo");
                for (Element po : dataPojo) {
                    Element row = doc.createElement("row");
                    for (Element field : po.getChildren()) {
                        Element rowField = doc.createElement(field.getAttribute("propertyname").getValue().toString());
                        if (field.getChildren().size() > 0) {
                            for (Element fieldSon : field.getChildren()) {
                                rowField.addChild(fieldSon);
                            }
                        } else {
                            if (field.getAttribute("value") == null) {
                                rowField.setValue(field.getValue());
                            } else {
                                rowField.setValue(field.getAttribute("value").getValue());
                            }
                        }
                        row.addChild(rowField);
                    }
                    table.addChild(row);
                }

                table.setAttribute("totalCount", dataPojo.size());
                table.setAttribute("count", dataPojo.size());
                doc.getRootElement().addChild(table);
            }

            return doc;
        } else {
            return null;
        }
    }
}
