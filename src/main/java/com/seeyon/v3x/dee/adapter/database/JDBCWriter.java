package com.seeyon.v3x.dee.adapter.database;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Document.Element;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.adapter.database.addition.operation.DatabaseOp;
import com.seeyon.v3x.dee.adapter.database.addition.operation.DatabaseOpFactory;
import com.seeyon.v3x.dee.adapter.database.work.WriterFinisher;
import com.seeyon.v3x.dee.util.JdbcUtils;

/**
 * JDBC写入适配器
 */
public class JDBCWriter extends JDBCAdapter{
	private final static Log log = LogFactory.getLog(JDBCWriter.class);

	/**
	 * 目标数据库主键ID的映射，Map格式如下：<br/>
	 * {tableA : "id1, id2"}<br/>
	 * {tableB : "id1"}
	 */
	private Map<String, String> targetIds;
	
    
	@Override
	public Document execute(Document document) throws TransformException {
		if (document == null) {
			return null;
		}
		Parameters params = null;
		if (document.getContext() != null) {
			params = document.getContext().getParameters();
		}
		int coreNum = 5;
		int num = 1000;
		if(params!=null && params.getValue("dee.default.threadNum")!=null) {
			coreNum = Integer.parseInt(params.getValue("dee.default.threadNum").toString()); 
		}
		if(params!=null && params.getValue("dee.default.threadDataNum")!=null) {
			num = Integer.parseInt(params.getValue("dee.default.threadDataNum").toString()); 
		}
		log.info("jdbcWriter core Num:"+coreNum+",threadDataNum:"+num);
		Connection connection = null;
		try {
			connection = getConnection();
			DatabaseOp op = DatabaseOpFactory.getInstance().getByConnection(connection);
			WriterFinisher finisher = new WriterFinisher(coreNum); 
			List<Element> tableElements = document.getRootElement().getChildren();
			connection.setAutoCommit(false);
			for (Element tableElement : tableElements) {
				MultiValueMap map = op.generateSql(tableElement, targetIds, getDataSource(), document.getContext());
				if (map == null) {
					continue;
				}
				for (Object key : map.keySet()) {
					String sql = key.toString();
					Collection<List> values = map.getCollection(key);
					if(values!=null && values.size()>0){
						List<List> rows = new ArrayList<List>(values);
						values =null;
						List<List> datas = null;
						if(rows.size() > num){
							int length = rows.size()/num;
							for(int i=0;i<length;i++){
								datas = rows.subList(i*num, i*num+num);
								finisher.addWorker(finisher.new WriterWork(sql, datas,connection));
							}
							//加载剩下的数据
							datas = rows.subList( ((length-1)*num+num), rows.size());
							if(datas!=null && datas.size()>0)
								finisher.addWorker(finisher.new WriterWork(sql, datas, connection));
						}else{
							finisher.addWorker(finisher.new WriterWork(sql, rows, connection ));
						}
					}
					
				}
			}
			
			finisher.start();
			finisher.await();
			finisher.clearWorker();
			connection.commit();
		} catch (Exception e) {
			log.error(String.format(":Druid连接池removeAbandonedCount次数为%d次",getRemoveAbandonedCount()));
			throw new TransformException(e);
		} finally {
			JdbcUtils.close(connection);
		}

		return document;
	}

	public Map<String, String> getTargetIds() {
		return targetIds;
	}
 
	
	/**
	 * 计算使用线程数
	 * @param
	 * @param
	 * @param
	 * @return
	 */
//	private int calThreads(Document document,int num,int coreNum){
//		int count = 0;
//		int threads = 0;
//		int tables = 0;
//		try{
//			List<Element> tableElements = document.getRootElement().getChildren();
//			for (Element tableElement : tableElements) {
//				count+=tableElement.getChildren().size();
//				if(targetIds.containsKey(tableElement.getName())){
//					tables++;
//				}
//			}
//			if(count/num > 1){
//				threads = count/num;
//			}else{
//				threads = 1;
//			}
//			if( tables > threads )
//				threads = tables;
//			if(threads > coreNum)
//				threads = coreNum;
//		}catch(Exception e){
//			threads = coreNum;
//			log.error("计算线程失败，使用默认线程数:"+coreNum,e);
//		}
//		return threads;
//	}

	public void setTargetIds(Map<String, String> targetIds) {
		this.targetIds = targetIds;
	}
	
 
}
