package com.seeyon.v3x.dee.util;

import com.seeyon.v3x.dee.TransformException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class DataChangeUtil {
    private final static Log log = LogFactory.getLog(DataChangeUtil.class);
    
    /**
     * Properties --> Map
     * 
     * @param props
     * @return
     */
    public static Map<Object, Object> propToMap(Properties props) {
        Map<Object, Object> maps = new LinkedHashMap<Object, Object>();
        
        Set<Object> keys = props.keySet();
        for (Object key : keys) {
            maps.put(key, props.get(key));
        }
        
        return maps;
    }
    
    /**
     * 根据文件路径，载入properties文件
     * 
     * @param filePath
     * @return
     */
    public static Properties loadProperties(String filePath) {
        if (filePath == null) {
            return new Properties();
        }
        
        InputStream in = null;
        Properties p = new Properties();
        try {
            in = new FileInputStream(filePath);
            BufferedReader bf = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            p.load(bf);
            return p;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (null != in) {
                    in.close();
                } 
            } catch (IOException e) {
                log.error("输入流关闭异常：" + e);
            }
        }
        return p;
    }
    
    /**
     * 根据文件路径，存储properties
     * 
     * @param prop
     * @param filePath 文件路径
     * @return true，成功；false，失败
     */
    public static boolean storeProperties(Properties prop, String filePath) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            prop.store(bw, null);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (null != out) {
                    out.close();
                } 
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return false;
    }
    
    /**
     * 直接输出流.
     * 
     * @date 2011-12-28
     * @author liuls
     * @param contentType 输出类型
     * @param inputStream 输入流
     * @param headers 协议头数组对象
     * @throws com.seeyon.v3x.dee.TransformException
     */
    public static void renderStream(HttpServletResponse resp, final String contentType,
            final InputStream inputStream, final String... headers) throws TransformException {
        try {
            resp.setContentType(contentType);
            resp.setHeader(headers[0],headers[1]);
            byte[] b = new byte[1024];
            int len = -1;
            while ((len = inputStream.read(b, 0, 1024)) != -1) {
                resp.getOutputStream().write(b, 0, len);
            }
            //resp.getWriter().flush();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw new TransformException(e.getMessage(),e);
        }
    }
    
    /**
     * 获取环境变量
     * 
     * @param name
     * @return
     */
    public static String getProperty(String name) {
        // Property优先
        String v = System.getProperty(name);
        if (v == null) {
            v = System.getenv(name);
        }
        return v;
    }
    
    /**
     * 获取绝对路径
     * 
     * @param name
     * @return
     */
    public static String getRealPath(String name) {
        String path = "";
        try {
            path = DataChangeUtil.class.getResource(name).toURI().getPath();
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
        }
        return path;
    }
    
}
