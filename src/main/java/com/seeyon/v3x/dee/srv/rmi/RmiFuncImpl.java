package com.seeyon.v3x.dee.srv.rmi;

import com.seeyon.v3x.dee.DEEClient;
import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RmiFuncImpl extends UnicastRemoteObject implements RmiFunc {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2274137448643227229L;

	public RmiFuncImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 调用DEE任务。
	 * 
	 * @param flowName
	 *            DEE流程
	 * @return DEE任务执行输出的Document
	 * @throws Exception
	 */
	@Override
	public Document execute(String flowName) throws Exception {
		// TODO Auto-generated method stub
		return execute(flowName,new Parameters());
	}

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
	@Override
	public Document execute(String flowName, Parameters params)
			throws Exception {
		// TODO Auto-generated method stub
		return execute(flowName,null,params);
	}

	/**
	 * 调用DEE任务。
	 * 
	 * @param flowName
	 *            DEE流程
	 * @param input 输入Document
	 * @param params
	 *            参数
	 * @return DEE任务执行输出的Document
	 * @throws Exception
	 */
	@Override
	public Document execute(String flowName, Document input, Parameters params)
			throws Exception {
		// TODO Auto-generated method stub
		// 执行DEE中的Flow
		DEEClient client = new DEEClient();
		try
		{
			if(input==null){
				return client.execute(flowName,params);
			}else{
				return client.execute(flowName,input,params);
			}
		}catch(Exception e)
		{
			throw e;
		}
	}

	@Override
	public String getMsg() throws TransformException, RemoteException {
		// TODO Auto-generated method stub
		return "is ok!";
	}

}
