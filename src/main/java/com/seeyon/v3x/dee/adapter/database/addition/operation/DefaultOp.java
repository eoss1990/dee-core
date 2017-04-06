package com.seeyon.v3x.dee.adapter.database.addition.operation;

import com.seeyon.v3x.dee.Document.Element;
import com.seeyon.v3x.dee.TransformContext;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.resource.DbDataSource;
import com.seeyon.v3x.dee.util.JdbcUtils;
import com.seeyon.v3x.dee.util.LazyJDBCDataLoader;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 默认数据库操作
 *
 * @author zhangfb
 */
public class DefaultOp implements DatabaseOp {
    @Override
    public MultiValueMap generateSql(Element tableElement, Map<String, String> targetIds,
                                     DbDataSource dataSource, TransformContext ctx) throws TransformException {
        MultiValueMap sqlMap = new MultiValueMap();

        // 取得表名
        String tableName = tableElement.getName();
        // 取得ID数组
        String[] idArray = getIdsByTableName(targetIds, tableName, ctx);

        if (idArray != null) {
            LazyJDBCDataLoader loader = generateDataLoader(tableName, idArray, dataSource);

            // 遍历table节点下所有的row元素
            for (Element rowElement : tableElement.getChildren()) {
                // 忽略空行
                if ("nullrow".equals(rowElement.getName())) {
                    continue;
                }

                boolean flag = isNew(tableName, idArray, rowElement, loader);
                if (flag) {     // 如果目标数据库没有值，拼接插入字符串
                    generateInsertSql(tableName, sqlMap, rowElement);
                } else {        // 如果有值，拼接update字符串
                    generateUpdateSql(tableName, idArray, sqlMap, rowElement);
                }
            }
        }

        return sqlMap;
    }

    /**
     * 拼装插入sql，并放入sqlMap
     *
     * @param tableName  表名称
     * @param sqlMap     sql map
     * @param rowElement 行节点
     */
    private void generateInsertSql(String tableName, MultiValueMap sqlMap, Element rowElement) {
        List<Element> columns = rowElement.getChildren();
        List<Object> params = new ArrayList<Object>();
        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();
        StringBuilder sql = new StringBuilder(" insert into " + tableName);

        for (Element column : columns) {
            fields.append(column.getName()).append(",");

            Object columnValue = column.getValue();
            if (columnValue != null && JdbcUtils.isSeq(columnValue.toString())) {     // 增加sequence写入
                values.append(columnValue).append(",");
            } else {
                values.append("?,");
                params.add(columnValue);
            }
        }

        if (fields.length() > 1 && values.length() > 1) {                  // 如果字段和值都不是空的
            sql.append("(").append(fields.substring(0, fields.length() - 1)).append(") values ");
            sql.append("(").append(values.substring(0, values.length() - 1)).append(")");
        }

        sqlMap.put(sql.toString(), params);
    }

    /**
     * 拼装修改sql，并放入sqlMap
     *
     * @param tableName  表名称
     * @param idArray    ID数组
     * @param sqlMap     sql map
     * @param rowElement 行节点
     */
    private void generateUpdateSql(String tableName, String[] idArray, MultiValueMap sqlMap, Element rowElement) {
        List<Element> columns = rowElement.getChildren();

        List<Object> params = new ArrayList<Object>();
        String initSql = " update " + tableName + " set ";
        StringBuilder updateSql = new StringBuilder(initSql);
        for (Element column : columns) { // 循环 column节点
            // 排除id，不更新id
            String name = column.getName();
            //修改兼容，将idSet.contains(name)改写
            boolean isContain = false;
            if (name != null) {
                for (String idName1 : idArray) {
                    if (name.equalsIgnoreCase(idName1)) {
                        isContain = true;
                    }
                }
            }
            if (isContain) {
                continue;
            }
            Object columnValue = column.getValue();

            // 增加sequence写入
            if (columnValue != null && JdbcUtils.isSeq(columnValue.toString())) {
                updateSql.append(name).append("=").append(columnValue).append(",");
            } else {
                updateSql.append(name).append("=?,");
                params.add(columnValue);
            }
        }
        if (updateSql.length() > 1 && !initSql.equals(updateSql.toString())) {      // 2013-6-26 修改，用于判断仅有主键更新
            StringBuilder sWhere = new StringBuilder(" where ");
            for (String name : idArray) {
                Element column = getChildByName(rowElement, name);
                Object idValue = column.getValue();
                sWhere.append(name).append("=?");
                sWhere.append(" and ");
                params.add(idValue);
            }
            int len = sWhere.length();
            if (idArray.length > 0) {
                sWhere.delete(len - 5, len - 1);
            }
            String sql = updateSql.substring(0, updateSql.length() - 1) + sWhere;
            sqlMap.put(sql, params);
        }
    }

    /**
     * 判断是插入还是修改
     *
     * @param tableName  表名称
     * @param idArray    ID数组
     * @param rowElement 行节点
     * @param loader     惰性加载器
     * @return true：新增，false：修改
     * @throws TransformException
     */
    private boolean isNew(String tableName, String[] idArray,
                          Element rowElement, LazyJDBCDataLoader loader) throws TransformException {
        boolean isNew = false;
        List<Object> idValues = new ArrayList<Object>();

        for (String id : idArray) {
            Element column = getChildByName(rowElement, id); //修改兼容
            if (column == null) {
                throw new TransformException("数据错误：" + tableName + "表的主键字段" + id + "没有设置值。");
            }
            // 增加sequence写入
            if (column.getValue() != null && JdbcUtils.isSeq(column.getValue().toString())) {
                isNew = true;
                break;
            }
            idValues.add(column.getValue());
        }

        if (!isNew && loader != null) {
            isNew = loader.getValue(idValues) == null;
        }
        return isNew;
    }

    /**
     * 从键值对中取出表名关联的ID，并使用上下文参数转换
     *
     * @param targetIds 表名和ID的键值对
     * @param tableName 表名称
     * @param ctx       转换上下文
     * @return ID数组，如果没取到，则返回null
     * @throws TransformException
     */
    private String[] getIdsByTableName(Map<String, String> targetIds, String tableName, TransformContext ctx) throws TransformException {
        if (targetIds == null) {
            return null;
        }

        String idName = targetIds.get(tableName);
        if (idName != null && ctx != null) {
            idName = ctx.getParameters().evalString(idName);
        }

        List<String> list = new ArrayList<String>();
        if (idName != null) {
            // 如果ID名称为“id1,id2”，则会根据“,”，将其分割成ID数组
            for (String tmp : StringUtils.split(idName, ",")) {
                list.add(StringUtils.trim(tmp));
            }
        }

        return list.size() == 0 ? null : list.toArray(new String[list.size()]);
    }

    /**
     * 生成惰性加载器
     *
     * @param tableName  表名称
     * @param idArray    ID数组
     * @param dataSource 数据源引用
     * @return 惰性加载器
     */
    private LazyJDBCDataLoader generateDataLoader(String tableName, String[] idArray, DbDataSource dataSource) {
        StringBuilder sql = new StringBuilder("select * from ");
        sql.append(tableName).append(" where ");
        for (String id : idArray) {
            sql.append(id).append("=? and ");
        }
        sql.append(" 1=1 ");
        return new LazyJDBCDataLoader(dataSource, sql.toString());
    }

    /**
     * 获取父节点下的节点名为指定名称的节点，节点名忽略大小写
     *
     * @param parent    父节点
     * @param fieldName 子节点名
     * @return 子节点，当未查询到节点时，返回null
     */
    private Element getChildByName(Element parent, String fieldName) {
        if (parent == null || fieldName == null) {
            return null;
        }

        for (Element tmpElement : parent.getChildren()) {
            if (tmpElement != null && fieldName.equalsIgnoreCase(tmpElement.getName())) {
                return tmpElement;
            }
        }

        return null;
    }
}
