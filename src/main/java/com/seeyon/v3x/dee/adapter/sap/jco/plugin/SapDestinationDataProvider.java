package com.seeyon.v3x.dee.adapter.sap.jco.plugin;

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SapDestinationDataProvider implements DestinationDataProvider {

	private Map<String, Properties> providers = new HashMap();
	private Properties ABAP_AS_properties;
	public SapDestinationDataProvider(String host,String sysnr,String client,String u,String p){
		
		Properties pro=new Properties();
		pro.setProperty(DestinationDataProvider.JCO_ASHOST,host);
		pro.setProperty(DestinationDataProvider.JCO_SYSNR,sysnr);
		pro.setProperty(DestinationDataProvider.JCO_CLIENT,client);
		pro.setProperty(DestinationDataProvider.JCO_USER,u);
		pro.setProperty(DestinationDataProvider.JCO_PASSWD,p);
		
		this.ABAP_AS_properties = pro;
	}
	@Override
	public Properties getDestinationProperties(String arg0) {
		// TODO Auto-generated method stub
	        return this.ABAP_AS_properties;
	}
	
	@Override
	public void setDestinationDataEventListener(
			DestinationDataEventListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean supportsEvents() {
		// TODO Auto-generated method stub
		return false;
	}

}
