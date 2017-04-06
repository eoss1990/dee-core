package com.seeyon.v3x.dee.util.rest;

import javax.servlet.http.HttpSession;
import java.util.List;

public interface CTPRestClient {
    /**
     * 登录校验。
     *
     * @param userName 服务用户名
     * @param password 服务用户密码
     * @return 验证通过返回true
     */
    boolean authenticate(String userName, String password);

    //获取登录token
    String getToken();

    /**
     * 调用REST服务的GET请求。
     *
     * @param path  请求的路径，不包含上下文，如member/?loginName=test
     * @param clazz 期望的返回值类型，如V3xOrgMember.class
     * @return
     */

    <T> T get(String path, Class<T> clazz);

    <T> T get(String path, Class<T> clazz, String accept);

    <T> List<T> getList(String path, final Class<T> clazz);

    <T> T put(String path, Object body, Class<T> clazz);

    <T> T put(String path, Object body, Class<T> clazz, String accept);

    <T> T put(String path, Object body, Class<T> clazz, String type, String accept);

    <T> T post(String path, Object body, Class<T> clazz);

    <T> T post(String path, Object body, Class<T> clazz, String accept);

    <T> T post(String path, Object body, Class<T> clazz, String type, String accept);

    <T> T delete(String path, Object body, Class<T> clazz);

    <T> T delete(String path, Object body, Class<T> clazz, String accept);

    <T> T delete(String path, Object body, Class<T> clazz, String type, String accept);

    void setSession(String sessionId);

    void setSession(HttpSession session);
}