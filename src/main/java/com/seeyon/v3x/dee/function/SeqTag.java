package com.seeyon.v3x.dee.function;

import com.seeyon.v3x.dee.TransformException;

public class SeqTag extends Tag {

	private String retName;
	private int isCurr;
	@Override
	public Object execute() throws TransformException {
		// TODO Auto-generated method stub
		if(this.isCurr == 0)
			return this.retName + ".nextval";
		else
			return this.retName + ".currval";
	}
	
	public void setSeqName(String seqName){
		this.retName = seqName;
	}
	public void setCurr(int isC){
		this.isCurr = isC;
	}
}
