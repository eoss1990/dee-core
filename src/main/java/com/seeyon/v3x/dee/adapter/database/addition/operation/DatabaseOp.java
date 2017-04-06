package com.seeyon.v3x.dee.adapter.database.addition.operation;

import com.seeyon.v3x.dee.Document.Element;
import com.seeyon.v3x.dee.TransformContext;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.resource.DbDataSource;
import org.apache.commons.collections.map.MultiValueMap;

import java.util.Map;

/**
 * 数据库操作接口
 *
 * @author zhangfb
 */
public interface DatabaseOp {
    /**
     * 生成插入或修改的sql映射map
     *
     * @param tableElement 表节点
     * @param targetIds    table和ID的映射
     * @param dataSource   数据库数据源
     * @param ctx          转换上下文
     * @return sqlMap
     * @throws TransformException
     */
    MultiValueMap generateSql(Element tableElement, Map<String, String> targetIds,
                              DbDataSource dataSource, TransformContext ctx) throws TransformException;
}
