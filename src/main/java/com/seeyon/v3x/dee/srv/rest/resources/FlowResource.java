package com.seeyon.v3x.dee.srv.rest.resources;

import com.seeyon.v3x.dee.DEEClient;
import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Parameters;
import net.sf.json.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

@Path("flow")
public class FlowResource {
	private Properties prop = new Properties();
	
	public FlowResource(){
	}
	
	//无参数执行任务
	@GET
	@Path("{flowId}")
	@Produces(MediaType.APPLICATION_XML)
	public Response excFlow(@PathParam("flowId") String flowId)
			throws Exception {
		if(flowId == null || "".equals(flowId))
			Response.noContent().build();
		Document doc = null;
		try{
			doc = this.executeFlow(flowId,null,null);
		}
		catch(Exception e){
			return Response.ok(toErrDoc(e.getMessage())).build();
		}
		return Response.ok(doc == null ? "" : doc.toString()).build();
	}
	@POST
	@Path("{flowId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_XML)
	public Response excFlow(@PathParam("flowId") String flowId, String data)
			throws Exception {
		if(flowId == null || "".equals(flowId) || data == null)
			Response.noContent().build();
		Document doc = null;
		try{
			Parameters params = new Parameters();
			JSONObject jsonObj = JSONObject.fromObject(data);
			for (Iterator iter = jsonObj.keys(); iter.hasNext();) {
			    String key = (String) iter.next();
				params.add(key, jsonObj.get(key));
			} 
			doc = this.executeFlow(flowId,null,params);
		}
		catch(Exception e){
			return Response.ok(toErrDoc(e.getMessage())).build();
		}
		return Response.ok(doc == null ? "" : doc.toString()).build();
	}
	
	private Document executeFlow(String flowName, Document input, Parameters params) throws Exception {
		// TODO Auto-generated method stub
		try{
			// 执行DEE中的Flow
			if(!isStartSrv()){
				throw new Exception("REST服务未开启，不能执行任务！");
			}
			DEEClient client = new DEEClient();
			if(input==null){
				return client.execute(flowName,params);
			}else{
				return client.execute(flowName,input,params);
			}
		}
		catch(Exception e){
			throw e;
		}
	}
	//构造error信息
	private String toErrDoc(String errMsg){
		String errXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		errXml += "<root><errmsg>"+errMsg+"</errmsg></root>";
		return errXml;
	}
	//读取参数
	private boolean isStartSrv() throws Exception {
		String deeHome = this.getProperty("DEE_HOME");
		String restFile = deeHome + File.separator + "conf" + File.separator + "config.properties";
		
		//获取REST服务远程调用开关信息
		InputStream config = null;//this.getClass().getResourceAsStream(deeRMI_target);
		try {
			config = new FileInputStream(restFile);
			prop.load(config);
			//是否支持调用
			return Boolean.parseBoolean(prop.getProperty("restSrv"));
		} catch (Exception e) {
			throw e;
		}
		finally {
			try {
				if(config != null) config.close();
			} 
			catch (IOException e) {
				throw new Exception(e.getMessage());
			}//写文件IO异常，catch不处理
		}
	}
	private String getProperty(String name) {
		// Property优先
		String v = System.getProperty(name);
		if (v == null)
			v = System.getenv(name);
		return v;
	}
}
