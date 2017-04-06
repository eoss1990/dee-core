package com.seeyon.v3x.dee.debug;

/**
 * 用于存储适配器执行状态信息
 */
public class AdapterBeanLog {
	private String name;
	private int state;
	private String data;
	private String parms;
	
	public AdapterBeanLog(String name, int state, String data, String parms){
		this.name = name;
		this.state = state;
		this.data = data;
		this.parms = parms;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getParms() {
		return parms;
	}
	public void setParms(String parms) {
		this.parms = parms;
	}
}
