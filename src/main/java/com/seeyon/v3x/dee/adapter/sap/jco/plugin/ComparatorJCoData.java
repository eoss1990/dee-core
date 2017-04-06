package com.seeyon.v3x.dee.adapter.sap.jco.plugin;

import com.seeyon.v3x.dee.adapter.sap.jco.plugin.jcoData;

import java.util.Comparator;

public class ComparatorJCoData implements Comparator<jcoData> {

	public int compare(jcoData jco1, jcoData jco2) {
		int flag = jco1.getJcoName().compareToIgnoreCase(jco2.getJcoName());
		if(flag==0)
			return jco1.getDocName().compareToIgnoreCase(jco2.getDocName());
		else
			return flag;
	}

	
}
