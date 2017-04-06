package com.seeyon.v3x.dee.resource;

import java.io.Serializable;

/**
 * 数据源，描述操作的异构系统，如数据库连接信息、WebService的EPR、文件的URI。
 * 2017.2.10增加序列化支持 by yangyu
 */
public interface DataSource extends Serializable{
	// caution:不允许在DataSource中添加任何方法。
}
