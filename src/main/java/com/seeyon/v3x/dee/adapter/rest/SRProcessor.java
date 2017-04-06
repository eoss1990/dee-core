package com.seeyon.v3x.dee.adapter.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.adapter.InitializingAdapter;

import net.sf.json.JSONObject;

public class SRProcessor implements Adapter, InitializingAdapter {
	private final static Log log = LogFactory.getLog(SRProcessor.class);
	
	/**访问地址 */
	private String url;
	/**访问方式(POST、GET、PUT...)*/
	private String urlType;
	/**Content-type*/
	private String contentType;
	/**是否为A8接口*/
	private String isA8;
	private final String ISA8_STATE = "1";
	/**Headers*/
	private Map<String, Object> headers;
	/**Body */
	private Map<String, Object> bodys;

	@Override
	public void evalParaBeforeExe(Parameters parameters) throws Exception {
		url = parameters.evalString(url);
		urlType = parameters.evalString(urlType);
		contentType = parameters.evalString(contentType);
		isA8 = parameters.evalString(isA8);
        if (headers!=null&&headers.size() > 0) {
            for (Entry<String, Object> entry : headers.entrySet()) {
            	headers.put(entry.getKey(),parameters.evalString(entry.getValue().toString()));
            }
        }
        if (bodys!=null&&bodys.size() > 0) {
            for (Entry<String, Object> entry : bodys.entrySet()) {
            	bodys.put(entry.getKey(),parameters.evalString(entry.getValue().toString()));
            }
        }
	}

	@Override
	public Document execute(Document document) throws TransformException {
		StringBuffer body = new StringBuffer("{");
		for (String key : bodys.keySet()) {
			if("{".equals(body.toString())){
				body.append("\"" + key + "\":\"" + bodys.get(key) + "\"");
			}else{
				body.append(",\"" + key + "\":\"" + bodys.get(key) + "\"");
			}
		}
		body.append("}");
		String result;
		try {
			result = sendRequest(url, headers, "{\"Content-type\":\""+contentType+"\"}", 
					body.toString(), urlType);
			document.getContext().setAttribute("RestResult", result.trim());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new TransformException("调用rset接口：" + url + ",异常：" + e.getMessage(), e);
		}
		return document;
	}
	
	private String sendRequest(String url, Map<String, Object> jheader, String cType, String body, 
			String method) throws Exception {
		HttpURLConnection conn = null;
		JSONObject jcType = JSONObject.fromObject(cType);
		String token = "";
		if(ISA8_STATE.equals(isA8)){
			String getToken = url.substring(0, url.indexOf("rest")) + "rest/token/"
					+jheader.get("userName")+"/"+jheader.get("passWord");
			conn = (HttpURLConnection) new URL(getToken).openConnection();
	        conn.setRequestMethod("GET");
	        conn.setConnectTimeout(5000);
			int code = conn.getResponseCode();
		    String jsonResult = "";
		    if (code == 200) {
		        BufferedReader inStream = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		        jsonResult = getResponseString(inStream);
		        JSONObject result = JSONObject.fromObject(jsonResult);
		        token = result.getString("id");
		        jheader.put("token", token);
		        jheader.remove("userName");
		        jheader.remove("passWord");
		    }
		}
		conn = (HttpURLConnection) new URL(url).openConnection();
		for (String key : jheader.keySet()) {
			String value = (String) jheader.get(key);
			conn.setRequestProperty(key, value);
		}
		for (Iterator iter = jcType.keys(); iter.hasNext();) {
		    String key = (String) iter.next();
			String value = (String) jcType.get(key);
			conn.setRequestProperty(key, value);
		}
        conn.setRequestMethod(method);
        conn.setConnectTimeout(5000);
        conn.setDoOutput(true);// 是否输入参数
        if(!"{}".equals(body)){
        	byte[] bypes = body.getBytes();
        	conn.getOutputStream().write(bypes);// 输入参数
        }
        int code = conn.getResponseCode();
        String jsonResult = "";
        if (code == 200) {
        	BufferedReader inStream = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        	jsonResult = getResponseString(inStream);
        }else{
        	log.info("访问rest接口出错，错误代码：" + code);
			throw new TransformException("访问rest接口出错，错误代码：" + code);
        }
		return jsonResult;
	}
    private String getResponseString(BufferedReader inStream) throws Exception {
        String result = "";
        String lines = "";
        while((lines = inStream.readLine()) != null){
        	result+=lines;
        }
        return result;
    }

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrlType() {
		return urlType;
	}

	public void setUrlType(String urlType) {
		this.urlType = urlType;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getIsA8() {
		return isA8;
	}

	public void setIsA8(String isA8) {
		this.isA8 = isA8;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}

	public Map<String, Object> getBodys() {
		return bodys;
	}

	public void setBodys(Map<String, Object> bodys) {
		this.bodys = bodys;
	}

}
