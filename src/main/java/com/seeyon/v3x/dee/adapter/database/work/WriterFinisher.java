package com.seeyon.v3x.dee.adapter.database.work;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.taskdefs.Sleep;

import com.seeyon.v3x.dee.resource.DbDataSource;
import com.seeyon.v3x.dee.util.SyncFinisher;
import com.seeyon.v3x.dee.util.SyncFinisher.Worker;


public class WriterFinisher extends SyncFinisher{
	private final static Log log = LogFactory.getLog(WriterFinisher.class);
	
	private ExecutorService pool = null;
	
	
	/**
	 * 
	 */
	public WriterFinisher(int coreNum) {
		if(coreNum == 0)
			coreNum = 5;
		pool = Executors.newFixedThreadPool(coreNum);
	}
	 
	@Override
	public void start() {
		// TODO Auto-generated method stub
		
		countDownLatch = new CountDownLatch(workers.size());
		for (Worker worker : workers) {
			pool.execute(worker);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.seeyon.v3x.dee.util.SyncFinisher#clearWorker()
	 */
	@Override
	public void clearWorker() {
		// TODO Auto-generated method stub
		super.clearWorker();
		pool.shutdown();
		pool = null;
	}
	
	
	public class WriterWork extends Worker{

		private String sql;
		private Collection<List> rows;
		private Connection conn;
		
		public WriterWork(String sql,Collection<List> rows,Connection conn){
			this.sql = sql;
			this.rows = rows;
			this.conn = conn;
		
		}
  
		@Override
		public void work() {
 
			log.info("执行thread Name:"+Thread.currentThread().getName());
			PreparedStatement stmt = null;
			try {
				stmt = conn.prepareStatement(sql);
				for (List params : rows) {
					int i = 1;
					for (Object o : params) {
						stmt.setObject(i++, o);
					}
					stmt.addBatch();
				}
				stmt.executeBatch();
			} catch (Exception e) {
				log.error("writer work error,execute sql:"+sql,e);
			} finally{
				try {
					if(stmt!=null)
						stmt.close();
				}catch (SQLException e) {
					log.error("writer work close connection error",e);
				}
			}
			
			
		}
		
	}
	
}
 
