package com.seeyon.v3x.dee.srv.rmi;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiFunc extends Remote {
	/**
	 * 调用DEE任务。
	 * 
	 * @param flowName
	 *            DEE流程
	 * @return DEE任务执行输出的Document
	 * @throws com.seeyon.v3x.dee.TransformException
	 */
	public Document execute(String flowName) throws Exception;
	/**
	 * 调用DEE任务。
	 *
	 * @param flowName
	 *            DEE流程
	 * @param params
	 *            参数
	 * @return DEE任务执行输出的Document
	 * @throws com.seeyon.v3x.dee.TransformException
	 */
	public Document execute(String flowName, Parameters params) throws Exception;
	/**
	 * 调用DEE任务。
	 *
	 * @param flowName
	 *            DEE流程
	 * @param input 输入Document
	 * @param params
	 *            参数
	 * @return DEE任务执行输出的Document
	 * @throws com.seeyon.v3x.dee.TransformException
	 */
	public Document execute(String flowName, Document input, Parameters params) throws Exception;
	
	//测试调用
	public String getMsg() throws TransformException,RemoteException;
}
