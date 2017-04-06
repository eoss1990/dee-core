package com.seeyon.v3x.dee.util.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.seeyon.v3x.dee.common.a8rest.util.RestUtil;
import com.seeyon.v3x.dee.util.rest.user.UserToken;

public class GenericRestClient implements CTPRestClient {
    private static final String APPLICATION_JSON = MediaType.APPLICATION_JSON;
    private static final String APPLICATION_XML = MediaType.APPLICATION_XML;
    private final String baseUrl;
    private String token = "";
    private String sessionId = "";
    Client  client;
    private static JacksonJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider();

    public GenericRestClient(String baseUrl) {
        this.baseUrl = baseUrl + "/seeyon/rest/";
    }

    /*
     * (non-Javadoc)
     *
     * @see com.seeyon.client.CTPRestClient#authenticate(java.lang.String,
     * java.lang.String)
     */
    @Override
    public boolean authenticate(String userName, String password) {
        Map<String, String> body = new HashMap<String, String>();
        body.put("userName", userName);
        body.put("password", password);
        Map<String, String> retMap = post("token", body, Map.class);

        if (retMap != null) {
            String id = retMap.get("id");
            if (id != null) {
                this.token = id;
            }
        }
        return !UserToken.getNullToken().getId().equals(token);
    }

    @Override
    public String getToken() {
        return token;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.seeyon.client.CTPRestClient#get(java.lang.String,
     * java.lang.Class)
     */
    @Override
    public <T> T get(String path, Class<T> clazz) {
        return get(path, clazz, APPLICATION_JSON);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.seeyon.client.CTPRestClient#get(java.lang.String,
     * java.lang.Class, java.lang.String)
     */
    @Override
    public <T> T get(String path, Class<T> clazz, String accept) {
    	Response response = buildWebResource(path).request(accept)
        .header("token", token).get();
    	throwException(response);
        return response.readEntity(clazz);
    }

    public <T> List<T> getList(String path, final Class<T> clazz) {
    	Response response = buildWebResource(path).request(APPLICATION_JSON)
                .header("token", token).get();
        throwException(response);
        ParameterizedType parameterizedGenericType = new ParameterizedType() {
            public Type[] getActualTypeArguments() {
                return new Type[]{clazz};
            }

            public Type getRawType() {
                return List.class;
            }

            public Type getOwnerType() {
                return List.class;
            }
        };

        GenericType<List<T>> genericType = new GenericType<List<T>>(
                parameterizedGenericType) {
        };
        return response.readEntity(genericType);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.seeyon.client.CTPRestClient#put(java.lang.String,
     * java.lang.Class)
     */
    @Override
    public <T> T put(String path, Object body, Class<T> clazz) {
        return put(path, body, clazz, APPLICATION_JSON);
    }

    @Override
    public <T> T put(String path, Object body, Class<T> clazz, String accept) {
        return put(path, body, clazz, APPLICATION_JSON, accept);
    }

    @Override
    public <T> T put(String path, Object body, Class<T> clazz, String type, String accept) {
    	Builder build = buildWebResource(path).request(accept);
    	Response response = build.header("token", token).put(Entity.json(body));
        throwException(response);
        return response.readEntity(clazz);
    }

    @Override
    public <T> T post(String path, Object body, Class<T> clazz) {
        return post(path, body, clazz, APPLICATION_JSON);
    }

    @Override
    public <T> T post(String path, Object body, Class<T> clazz, String accept) {
        return post(path, body, clazz, MediaType.APPLICATION_JSON, accept);
    }

    @Override
    public <T> T post(String path, Object body, Class<T> clazz, String type, String accept) {
 
    	Builder build = buildWebResource(path).request(accept);
    	build.header("token", token);
    	Response response = build.post(Entity.json(body));
        throwException(response);
        return response.readEntity(clazz);
    }

    @Override
    public <T> T delete(String path, Object body, Class<T> clazz) {
        return delete(path, body, clazz, APPLICATION_JSON);
    }

    @Override
    public <T> T delete(String path, Object body, Class<T> clazz, String accept) {
        return delete(path, body, clazz, APPLICATION_JSON, accept);
    }

    @Override
    public <T> T delete(String path, Object body, Class<T> clazz, String type, String accept) {
    	Response response = buildWebResource(path).request(accept).header("token", token).method("DELETE", Entity.json(body));
        throwException(response);
        return response.readEntity(clazz);
    }

    private WebTarget buildWebResource(String path) {
        if (client == null) {
        	ClientConfig clientConfig = new ClientConfig();
        	clientConfig.register(jacksonJsonProvider);
        	client = ClientBuilder.newClient(clientConfig);
        }
        WebTarget resource =  client.target(this.baseUrl).path(path);
        resource.path(path);
        if (this.sessionId != null) {
            Cookie cookie = new Cookie("JSESSIONID", this.sessionId);
            resource.request().cookie(cookie);
            return resource;
        }else{
        	resource.request().cookie(null);
        }
        return  resource;
    }

    private void throwException(Response response) {
        if (response.getStatus() != 200) {
            if (response.getStatus() == 303) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus() + "：获取访问权限失败！");
            } else {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }
        }
    }
    

    public Object get(String path) throws Exception {
    	BufferedReader br = null;
        try {

            URL url = new URL(this.baseUrl + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", APPLICATION_JSON);

            throwException(conn);

            br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            StringBuilder output = new StringBuilder();
            String s = "";
            while ((s = br.readLine()) != null) {
                output.append(s);
            }

            conn.disconnect();
            return output.toString();

        } catch (Exception e) {
            throw e;
        } finally {
        	if(br != null){
        		br.close();
        	}
		}
    }

    private void throwException(HttpURLConnection conn) throws IOException {
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }
    }

    @Override
    public void setSession(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public void setSession(HttpSession session) {
        this.sessionId = session.getId();
    }

//    public static void main(String[] args) throws Exception {
//        CTPRestClient client = RestUtil.createCtpClient("http://127.0.0.1", "t1", "123456");
//        //组织机构  部门添加
////    	String body = "{\"superior\":-7235091524463475024,\"name\": \"bbb2222\",\"sortId\": 66,\"orgAccountId\": -7235091524463475024,\"entityType\": \"Department\"}";
////    	String s = client.post("/orgDepartment", body, String.class,MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
//    	//组织机构  部门人员添加
//    	String body="{\"orgDepartmentId\":1009155983901606094,\"name\": \"vgvg\",\"sortId\": 55,\"orgLevelId\": 1545660985647713208,\"orgPostId\": 4966952266600235949,\"orgAccountId\": -7235091524463475024,\"loginName\": \"vgvg\"}";
//    	String s = client.post("/orgMember", body, String.class,MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
//    	System.out.println(s);
//	}
}
