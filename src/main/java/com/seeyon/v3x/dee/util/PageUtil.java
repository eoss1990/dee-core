package com.seeyon.v3x.dee.util;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Document.Attribute;
import com.seeyon.v3x.dee.Document.Element;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * 物理分页
 * 
 * @author fubing.zhang
 * @date 2013-9-18上午10:59:15
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class PageUtil {

    private final static Log log = LogFactory.getLog(PageUtil.class);

    /**
     * document分页
     * 
     * @param newInput document
     * @param params 参数
     * @return
     */
    public static Document pageDocument(Document newInput, Parameters params) {
        if (newInput == null || params == null) {
            return newInput;
        }

        int pageNumber = 0;
        int pageSize = 0;
        // 每页默认记录数
        int defaultPageSize = string2Int(params.getValue("dee.default.pagesize")+"", 65535);

        Object oPageNumber = params.getValue("Paging_pageNumber");
        Object oPageSize = params.getValue("Paging_pageSize");
        boolean requirePagination = false;

        try {
            pageNumber = (Integer) oPageNumber;
            pageSize = (Integer) oPageSize;
            if (pageSize > defaultPageSize) {
                pageSize = defaultPageSize;
            }
            requirePagination = true;
        } catch (Exception e) {
            log.warn("分页参数有误，忽略，不进行分页。" + 
                    "pageNumber:" + oPageNumber +
                    ", pageSize:" + oPageSize);
        }

        // 进行实际的分页操作，先设置count和totalCount属性，再设置row节点
        if (requirePagination) {
            Document retInput = TransformFactory.getInstance().newDocument("root");
            retInput.setContext(newInput.getContext());

            List<Element> tables = newInput.getRootElement().getChildren();
            if (tables.size() > 0) {
                for (int i=0; i<tables.size(); i++) {
                    Element table = tables.get(i);
                    List<Element> rows = table.getChildren();
                    
                    Object total = null;
                    Attribute atrr = table.getAttribute("totalCount");
                    if(atrr != null){
                    	total = atrr.getValue();
                    }
                    int rowSize = 0;
                    if (total != null)
                    {
                      rowSize = Integer.parseInt(total.toString());
                    }
                    else if (rows != null){
                      rowSize = rows.size();
                    }
                    
                    Element targetTable = retInput.getRootElement().addChild(table.getName());
                    setAttr(targetTable, table.getAttributes());
                    targetTable.setAttribute("totalCount", rowSize);
                    
                    pageNumber = (Integer) oPageNumber;
                    //如果总条数大于实际取出的记录数，
                    //说明取得就是已经分好页的数据，则不用再分页，只显示这页
                    if(rowSize > rows.size()){
                    	pageNumber = 1;
                    }
                    int startPos = (pageNumber-1) * pageSize;
                    int endPos = pageNumber * pageSize;
                    if(rowSize > rows.size() && rows.size() < pageSize){
                    	endPos = rows.size();
                    }
                    
                    if (rowSize <= startPos) {
                        targetTable.setAttribute("count", 0);
                        continue;
                    } else if (rowSize > startPos && rowSize <= endPos) {
                        endPos = rowSize;
                    }

                    targetTable.setAttribute("count", endPos - startPos);

                    for (int j=startPos; j<endPos; j++) {
                        targetTable.addChild(rows.get(j));
                    }
                }
            }
            return retInput;
        }
        return newInput;
    }

    /**
     * 从"config.properties"文件中读取字段值
     * 
     * @param key
     * @return value
     */
    public static String getFromConfigProperties(String key) {
        File configFile = new File(TransformFactory.getInstance().getConfigFilePath("config.properties"));

        if (configFile.exists()) {
            try {
                Properties p = new Properties();
                p.load(new FileInputStream(configFile));
                return p.getProperty(key);
            } catch (Exception e) {
                // 获取配置异常
                log.warn(e.getMessage(), e);
            }
        }

        return null;
    }

    /**
     * String --> int
     * 
     * @param s 需要转换的字符串
     * @param defaultValue 默认返回值
     * @return
     */
    public static int string2Int(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            // 记录警告信息
            log.warn(e.getMessage(), e);
        }

        return defaultValue;
    }

    /**
     * 给节点设置属性
     * 
     * @param element 节点
     * @param props 属性
     * @return
     */
    private static Element setAttr(Element element, List<Attribute> props) {
        Iterator<Attribute> iter = props.iterator();

        while (iter.hasNext()) {
            Attribute attr = iter.next();
            if (attr != null) {
                element.setAttribute(attr.getName(), attr.getValue());
            }
        }

        return element;
    }
}