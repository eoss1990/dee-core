package com.seeyon.v3x.dee.function;

import com.seeyon.v3x.dee.TransformException;


/**
 * @description 所有可执行function的父类
 * 
 * @author liuls
 */
public abstract class Tag {
	
	public String name;
	public String tagClass;
	
	public abstract Object execute() throws TransformException;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTagClass() {
		return tagClass;
	}
	public void setTagClass(String tagClass) {
		this.tagClass = tagClass;
	}
	
	

}
