package com.seeyon.v3x.dee.adapter.colmap;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Document.Element;
import com.seeyon.v3x.dee.TransformContext;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.TransformFactory;
import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.function.DictionaryTag;
import com.seeyon.v3x.dee.function.SeqTag;
import com.seeyon.v3x.dee.function.UUIDTag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.*;

/**
 * 列映射转换Processor，根据列映射规则转换数据。只适用与数据库表到数据库表之间的转换。
 * <p>
 * 如转换规则<br/>
 * <code>
 * {"TableA/ColumnA01":"TableB/ColumnB01","TableA01/ColumnA0101":"TableB01/ColumnB0101"}</code>
 * <br/>
 * ，意为输入表TableA的ColumnA01字段对应于输出表TableB的ColumnB01字段。 则数据<br/>
 * <code>{"TableA":{"ColumnA01":"value1"
 * ,"ColumnA02","value2"},"TableA01":{"ColumnA0101":"value3"}} </code><br/>
 * 转换后输出<br/>
 * <code>{"TableB":{"ColumnB01"
 * :"value1","ColumnA02","value2"},"TableB01":{"ColumnB0101":"value3"}}</code><br/>
 * 没有进行映射的数据使用原列名输出。
 * </p>
 *
 * @author wangwenyou
 */
public class ColumnMappingProcessor implements Adapter {
    private static Log log = LogFactory.getLog(ColumnMappingProcessor.class);
    // private final static DeeLog deelog =
    // DeeLogFactory.getInstance().getDeeLog();
    private static final String TABLE_COLUMN_SEP = "/";
    private ColumnMapping mapping = new ColumnMapping();

    private String transformXML;
    private boolean transNoMapping = true;

    public ColumnMapping getMapping() {
        return mapping;
    }

    public void setMapping(ColumnMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public Document execute(Document input) throws TransformException {
        this.getColumnMap();
        Document output = this.transByXML(input);
        return output;
    }

    public String getTransformXML() {
        return transformXML;
    }

    public void setTransformXML(String transformXML) throws TransformException {
        String name = TransformFactory.getInstance().getConfigFilePath(
                transformXML);
        File file = new File(name);
        if (!file.exists())
            throw new TransformException("file not found: " + name);
        this.transformXML = name;
    }

    public boolean isTransNoMapping() {
        return transNoMapping;
    }

    public void setTransNoMapping(String transNoMapping) {
        this.transNoMapping = Boolean.parseBoolean(transNoMapping);
    }

    public ColumnMappingProcessor() {
        super();
    }

    public ColumnMappingProcessor(Map<String, String> mapping) {
        super();
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            String srcField = entry.getKey();
            String targetField = entry.getValue();
            mapping(srcField, targetField);
        }
    }

    /**
     * 建立表映射或列映射。
     *
     * @param src    表名称或列名称，列名称需带表名称前缀，形如table或table/column。
     * @param target 目标表名称或列名称，形如table或table.column。
     */
    public void mapping(String src, String target) {
        this.put2List(src, target, null);
    }

    /**
     * @param src
     * @param target
     * @param expr
     * @description
     * @date 2011-9-27
     * @author liuls
     */
    private void put2List(String src, String target, String expr) {
        this.mapping.mapping(src, target, expr);
    }

    private ColumnMapping.Mapping getColumnMappingFromList(String tableName,
                                                           String columnName) {
        String key = tableName + TABLE_COLUMN_SEP + columnName;
        for (ColumnMapping.Mapping map : this.mapping) {
            if (key.equals(map.getSource())) {
                return map;
            }
        }
        return null;
    }

    // add by dkywolf 20120925 将getColumnMappingFromList方法的按映射列循环改为按字段循环匹配
    private Element getColumnElementFromList(ColumnMapping.Mapping map,
                                             String tableName, Element row) {
        String key = "";
        for (Element column : row.getChildren()) {
            key = tableName + TABLE_COLUMN_SEP + column.getName();
            if (key.equalsIgnoreCase(map.getSource())) {
                return column;
            }
        }
        return null;
    }

    public List<String> getMappingTableNames(String tableName) {
        ArrayList<String> targetTableNames = new ArrayList<String>();
        for (ColumnMapping.Mapping map : this.mapping) {
            if (tableName.equalsIgnoreCase(map.getSource())) {
                targetTableNames.add(map.getTarget()[0]);
            }
        }
        return targetTableNames;
    }

    /**
     * @param input
     * @return
     * @throws com.seeyon.v3x.dee.TransformException
     * @description 根据配置文件翻译属性值
     * @date 2011-9-28
     * @author liuls
     */
    private Document transByXML(Document input) throws TransformException {
        Map<String,String> columnMappingInfo = new HashMap();
        if (input == null)
            return null;
        Element root = input.getRootElement();
        TransformFactory factory = TransformFactory.getInstance();
        Document output = factory.newDocument(root.getName());
        Element targetRoot = output.getRootElement();
        for (Element table : root.getChildren()) { // 循环input
            String tableName = table.getName();
            List<String> targetTableNames = getMappingTableNames(tableName);
            // 未找到映射表，使用源表名称
            if (targetTableNames.size() == 0 && this.transNoMapping) {
                targetTableNames.add(tableName);
            }
            for (String targetTableName : targetTableNames) {
                Element targetTable = targetRoot.addChild(targetTableName);
                for (Element row : table.getChildren()) {
                    Element targetRow = targetTable.addChild(row.getName());

                    for (ColumnMapping.Mapping map : this.mapping) {
                        if (map.getTarget() == null
                                || map.getTarget().length != 2
                                || !targetTableName.equals(map.getTarget()[0]))
                            continue; // 去掉映射表
                        String targetColumnName = map.getTarget()[1];
                        Element column = this.getColumnElementFromList(map,
                                tableName, row);
                        if (column == null) {
                            columnMappingInfo.put(map.getSource(),"");
                            continue; // 未找到映射列，可能映射源字段填写有误
                        }
                        //
                        // }
                        // for (Element column : row.getChildren()) {
                        // String columnName = column.getName();
                        // ColumnMapping.Mapping map =
                        // this.getColumnMappingFromList(
                        // tableName, columnName);
                        // String targetColumnName;
                        // if (map != null) {
                        // targetColumnName = map.getTarget()[1];
                        // } else {
                        // // 无列映射，使用源列名称
                        // targetColumnName = columnName;
                        // if (!this.transNoMapping) // 根据配置判断是否转换无mapping的字段
                        // continue;
                        // }
                        Element targetColumn = targetRow
                                .addChild(targetColumnName);
                        Object value = column.getValue();
                        String expression = map.getExpression();
                        if (expression != null) {
                            Object o = executeExpression(expression, value,
                                    row, input.getContext());
                            targetColumn.setValue(o);
                        } else {
                            targetColumn.setValue(value);
                        }
                        // if (map != null) {
                        // String expression = map.getExpression();
                        // if (expression != null) {
                        // Object o = executeExpression(expression,
                        // value,row,input.getContext());
                        // targetColumn.setValue(o);
                        // } else {
                        // targetColumn.setValue(value);
                        // }
                        // } else {
                        // targetColumn.setValue(value);
                        // }
                    }
                    if (this.transNoMapping)
                        addTargetColunmWithoutSource(row, targetRow,
                                targetTableName, input.getContext());// 添加没有源数据的字段
                    if (targetRow.getChildren().size() == 0)
                        // 如果该行数据都没有转换则删除该行
                        targetTable.getChildren().remove(targetRow);
                    if (table.getAttribute("count") != null) {
                        targetTable.setAttribute("count", table.getAttribute("count").getValue());
                    }
                    if (table.getAttribute("totalCount") != null) {
                        targetTable.setAttribute("totalCount", table.getAttribute("totalCount").getValue());
                    }
                }
            }
        }
        log.debug(this.getClass().getName() + ": " + output);
        if (columnMappingInfo.size() > 0) {
            input.getContext().getParameters().add("columnMappingInfo", columnMappingInfo);
        }
        return output;
    }

    /**
     * @param targetRow 目标row节点
     * @description 设置目标字段中没有源数据字段对应的节点
     * @date 2011-9-28
     * @author liuls
     */
    private void addTargetColunmWithoutSource(Element srcRow,
                                              Element targetRow, String targetTableName, TransformContext context)
            throws TransformException {
        for (ColumnMapping.Mapping map : this.mapping) {
            if (map.getSource() == null || "".equals(map.getSource().trim())
                    || "/".equals(map.getSource().trim())) {
                if (!targetTableName.equals(map.getTarget()[0])) {
                    continue; // 如果是表名不同，略过
                }
                String name = map.getTarget()[1];
                Element targetColumn = targetRow.getChild(name);
                if (targetColumn == null) {
                    targetColumn = targetRow.addChild(name);
                }
                Object value = null;
                Element child = srcRow.getChild(name);
                if (child != null)
                    value = child.getValue();
                String expression = map.getExpression();
                if (expression != null) {
                    targetColumn.setValue(executeExpression(expression, value,
                            srcRow, context));
                }
            }
        }
    }

    /**
     * @description 获取对象影射
     * @date 2011-9-26
     * @author liuls
     */
    private void getColumnMap() {
        if (transformXML != null) {
            SAXReader saxReader = new SAXReader();
            org.dom4j.Document document = null;
            try {
                document = saxReader.read(transformXML);

                // TODO
                /*ColumnMappingParser parser = new ColumnMappingParser();
                this.mapping.merge(parser.execute(document.getRootElement()
						.element("mapping"), new HashMap()));*/
            } catch (Throwable e) {
                log.error(e);
            }
        }
    }

    private Object executeExpression(final String expression,
                                     final Object value, final Element row,
                                     final TransformContext context) throws TransformException {
        try {
            String[] expr = expression.split("'");
            if (expr[0].startsWith("uuid")) {
                return new UUIDTag().execute();
            } else if (expr[0].startsWith("dict")) {
                DictionaryTag tag = new DictionaryTag();
                tag.setContext(context);
                tag.setDictionary(expr[1]);
                tag.setKey(value);
                return tag.execute();
            } else if (expr[0].startsWith("seq")) {
                SeqTag o = new com.seeyon.v3x.dee.function.SeqTag();
                o.setSeqName(expr[1]);
                o.setCurr(0);
                return o.execute();
            } else if (expr[0].startsWith("curr")) {
                SeqTag o = new com.seeyon.v3x.dee.function.SeqTag();
                o.setSeqName(expr[1]);
                o.setCurr(1);
                return o.execute();
            }
            return null;
//            Map params = new HashMap() {
//                {
//                    put("context", context);
//                    put("value", value);
//                    put("currentRow", row);
//                    put("flow", context.getParameters().getValue("flow"));
//                }
//            };
//            return FunctionEngineManager.getInstance().execute(expression,
//                    params);
        } catch (Exception e) {
            log.error(e);
            throw new TransformException("执行表达式出错：" + expression + " " + value);
        }
    }

    @Override
    public String toString() {
        return "ColumnMappingProcessor [mapping=" + mapping + ", transformXML="
                + transformXML + ", transNoMapping=" + transNoMapping + "]";
    }

}
