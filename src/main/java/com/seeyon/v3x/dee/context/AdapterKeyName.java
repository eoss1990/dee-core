package com.seeyon.v3x.dee.context;

import java.util.HashMap;
import java.util.Map;

public class AdapterKeyName {
	private static AdapterKeyName adapterKeyName;
	private Map<String, String> adapterMap = new HashMap<String, String>();
	private Map<String, String> flowMap = new HashMap<String, String>();
	private String a8Home = "";
	private String deeHome = "";
	
	public static AdapterKeyName getInstance() {
		if (adapterKeyName == null) {
			adapterKeyName = new AdapterKeyName();
		}
		return adapterKeyName;
	}

	public String getA8Home() {
		return a8Home;
	}

	public void setA8Home(String a8Home) {
		this.a8Home = a8Home;
	}

	public String getDeeHome() {
		return deeHome;
	}

	public void setDeeHome(String deeHome) {
		this.deeHome = deeHome;
	}

	public Map<String, String> getAdapterMap() {
		return adapterMap;
	}

	public void setAdapterMap(Map<String, String> adapterMap) {
		this.adapterMap = adapterMap;
	}

	public Map<String, String> getFlowMap() {
		return flowMap;
	}

	public void setFlowMap(Map<String, String> flowMap) {
		this.flowMap = flowMap;
	}
}
