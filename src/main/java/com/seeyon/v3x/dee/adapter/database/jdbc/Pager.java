package com.seeyon.v3x.dee.adapter.database.jdbc;

/**   
 *   
 *   @package：com.seeyon.v3x.dee.adapter.database.JDBC.Pager.java       
 *   @author    chenmeng <br/>    
 *   @create-time   2016年11月24日   下午3:35:35     
 **/
public class Pager {

	private int pageNumber;
	private int pageSize;
	
	/**
	 * 
	 */
	public Pager(int pageNumber,int pageSize) {
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
		
	}
	 
	public int getPageNumber() {
		return pageNumber;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	public int getOffset() {
		return pageSize * (pageNumber - 1);
	}
	
}
