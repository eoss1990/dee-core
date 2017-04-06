package com.seeyon.v3x.dee.srv.rmi;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.v3x.dee.schedule.QuartzManager;


public class DEERmiServer extends UnicastRemoteObject {
	private static Log log = LogFactory.getLog(DEERmiServer.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 3479574601456739455L;
	private String rmiPort;
	public DEERmiServer() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * 启动RMI服务(注册RMI)
	 */
	public void StartRmiSrv() throws Exception {
		try {
			if(rmiPort == null || "".equals(rmiPort)){
				throw new Exception("端口号不能为空");
			}
			// 创建并安装安全管理器
			if(System.getSecurityManager() != null){
				System.setSecurityManager(new RMISecurityManager());
			}

			// 将该对象实例与名称“RmiFunc”捆绑
			LocateRegistry.createRegistry(Integer.parseInt(rmiPort));
			RmiFunc rFunc = new RmiFuncImpl();
			
			/**
			 * 可以指定端口，缺省端口8086
			 * 
			 *  
			 */
			Naming.bind("rmi://localhost:" + rmiPort + "/DeeRmiSrv", rFunc);
			log.error("DEE RMI 服务启动!");
		} catch (Exception e) {
			log.error("DEE RMI 服务启动出现异常：" + e.getLocalizedMessage());
			throw e;
		} 
	}
	/**
	 * @param args
	 * @throws java.rmi.RemoteException
	 */
	public static void main(String[] args) throws RemoteException {
		// TODO Auto-generated method stub
		DEERmiServer rSrv = new DEERmiServer();
		try {
			rSrv.StartRmiSrv();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RemoteException(e.getLocalizedMessage());
		}
	}


	public String getRmiPort() {
		return rmiPort;
	}


	public void setRmiPort(String rmiPort) {
		this.rmiPort = rmiPort;
	}

}
