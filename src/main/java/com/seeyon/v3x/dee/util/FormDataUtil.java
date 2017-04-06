package com.seeyon.v3x.dee.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.seeyon.v3x.dee.resource.DbDataSource;

public class FormDataUtil {
	public static String getFormData(DbDataSource ds, String formId) throws Exception{
		Connection conn = null;
		PreparedStatement sta = null;
		ResultSet rs = null;
        StringBuffer sb = new StringBuffer();
		try {
			conn = ds.getConnection();
			String sql = "select FIELD_INFO from FORM_DEFINITION where id = ?";
			sta = conn.prepareStatement(sql);
			sta.setLong(1, Long.parseLong(formId));
			rs = sta.executeQuery();
			String xml = "";
			if(rs.next()){
				xml = rs.getString("FIELD_INFO");
			}
			if ("".equals(xml))
				return xml;
			Document doc = DocumentHelper.parseText(xml);
	        // 获取根节点
	        Element rootElement = doc.getRootElement();
	        List<Element> tableElement = rootElement.elements("Table");
	        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root formName='表单名'>");
	        for (Element e : tableElement) {
	        	sb.append("<" + e.attribute("name").getValue() + "><row>");
	        	if(e.attribute("name").getValue().contains("formmain")){
	        		sb.append("<id>4937605540874345548</id><state>1</state>"
	        				+ "<start_member_id>901373261631474415</start_member_id>"
	        				+ "<start_date>2016-12-13 13:36:06.5</start_date>"
	        				+ "<approve_member_id>0</approve_member_id>"
	        				+ "<approve_date>2016-12-13 13:36:10.642</approve_date>"
	        				+ "<finishedflag>0</finishedflag><ratifyflag>0</ratifyflag>"
	        				+ "<ratify_member_id>0</ratify_member_id>"
	        				+ "<ratify_date></ratify_date><sort>0</sort>"
	        				+ "<modify_member_id>901373261631474415</modify_member_id>"
	        				+ "<modify_date>2016-12-13 13:36:06.0</modify_date>"); 
	        	}
	        	List<Element> fieldListElement = e.elements("FieldList");
	        	for(Element fe : fieldListElement){
	        		List<Element> fieldElement = fe.elements("Field");
	            	for (Element ef : fieldElement) {
	            		if(ef.attribute("name").getValue().contains("field")){
	                    	sb.append("<" + ef.attribute("name").getValue() + " display='" 
	                    			+ ef.attribute("display").getValue() + "'>123</" 
	                    			+ ef.attribute("name").getValue() + ">");
	            		}
	                }
	        	}
	        	sb.append("</row></" + e.attribute("name").getValue() + ">");
	        }
	        sb.append("</root>");
		} catch (Exception e) {
			throw e;
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (sta != null) {
				sta.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
		return sb.toString();
	}
}
