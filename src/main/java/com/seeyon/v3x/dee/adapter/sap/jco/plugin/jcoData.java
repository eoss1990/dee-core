package com.seeyon.v3x.dee.adapter.sap.jco.plugin;
/**
 * 用于储存JCoStructure与JCoTable映射配置的类
 * @author yangyu
 *
 */
public class jcoData {
	
	public jcoData()
	{}
	
	public jcoData(String jcoName,String jcoValue,String docName,String docValue){
		this.setJcoName(jcoName);
		this.setJcoValue(jcoValue);
		this.setDocName(docName);
		this.setDocValue(docValue);		
	}
	
	private String jcoName;
	private String jcoValue;
	private String docName;
	private String docValue;
	
	public String getJcoName() {
		return jcoName;
	}
	public void setJcoName(String jcoName) {
		this.jcoName = jcoName;
	}
	public String getJcoValue() {
		return jcoValue;
	}
	public void setJcoValue(String jcoValue) {
		this.jcoValue = jcoValue;
	}
	public String getDocName() {
		return docName;
	}
	public void setDocName(String docName) {
		this.docName = docName;
	}
	public String getDocValue() {
		return docValue;
	}
	public void setDocValue(String docValue) {
		this.docValue = docValue;
	}
	
	

}
