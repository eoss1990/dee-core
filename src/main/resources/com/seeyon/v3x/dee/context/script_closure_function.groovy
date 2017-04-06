package com.seeyon.v3x.dee.context

import com.seeyon.v3x.dee.Document
import com.seeyon.v3x.dee.datasource.JDBCDataSource
import com.seeyon.v3x.dee.debug.ParamBean
import com.seeyon.v3x.dee.debug.ScriptDebug
import com.seeyon.v3x.dee.util.DocumentUtil
import com.seeyon.v3x.dee.Document.Element;
import com.seeyon.v3x.dee.datasource.XMLDataSource;
import com.seeyon.v3x.dee.TransformFactory;
import com.seeyon.v3x.dee.common.db.resource.dao.DeeResourceDAO;
import com.seeyon.v3x.dee.util.DateUtil
import com.seeyon.v3x.dee.util.FileUtil;
import com.seeyon.v3x.dee.util.StrUtil;
import com.seeyon.v3x.dee.util.MathUtil;
import com.seeyon.v3x.dee.util.ProcedureUtil;
import com.seeyon.v3x.dee.util.FormDataUtil;
import com.seeyon.v3x.dee.common.util.ClearLogUtil;

/**
 * 取得当前上下文的指定参数的值
 * @param name
 * @return
 */
def param(name) {
    def params = document.getContext().getParameters()
    return params.getValue(name)
}
/**
 * 在当前上下文查找指定名称的实体
 * @param name
 * @return
 */
def lookup(name) {
    if (document == null) return null;
    def o = document.getContext().lookup(name)
    if (o == null) return o
    return o.getSource() == null ? o : o.getSource()
}

def debug(line, name, val) {
    ParamBean pb = new ParamBean()
    pb.setLine(line);
    pb.setName(name);
    pb.setVal(val);
    ScriptDebug.getInstance().setBean2Map(pb)
}

/**
 * 根据某个字段的值获取包含该值的document
 * @param document , name ,val
 * @return
 */
def getDocByVal(document, name, val) {
    List tableList = document.getRootElement().getChildren();
    Document doc = TransformFactory.getInstance().newDocument("root");
    try {
        for (int i = 0; i < tableList.size(); i++) {
            String tableName = ((Element)tableList.get(i)).getName();
            List rowList = ((Element)tableList.get(i)).getChildren();
            for (int j = 0; j < rowList.size(); j++) {
                if (String.valueOf(val).equals(((Element) rowList.get(j)).getChild(name).getValue().toString())) {
                    Element table = doc.getRootElement().getChild(tableName);
                    if (table!=null) {
                        table.addChild((Element)rowList.get(j));
                    }else
                    {
                        Element newTable = doc.createElement(tableName);
                        newTable.addChild((Element)rowList.get(j));
                        doc.getRootElement().addChild(newTable);
                    }
                }
            }
        }
    } catch (Exception e) {
        throw e;
    }
    return doc;
}

/**
 * 根据某个字段的拆分document
 * @param document name
 * @return
 */
def splitDocByName(document, name) {
    List tableList = document.getRootElement().getChildren();
    List docList = new ArrayList<>();
    List<String> valList = new ArrayList<String>();

    try {
        for (int i = 0; i < tableList.size(); i++) {
            List oldRow = tableList.get(i).getChildren();
            for (int j = 0; j < oldRow.size(); j++) {
                if (!valList.contains(oldRow.get(j).getChild(name).getValue())) {
                    valList.add(oldRow.get(j).getChild(name).getValue());
                }
            }
        }

        for (String val : valList) {
            Document doc = TransformFactory.getInstance().newDocument("root");
            for (Element ele : tableList) {
                String tableName = ele.getName();
                List rowList = ele.getChildren();
                for (Element e : rowList) {
                    if (String.valueOf(val).equals(e.getChild(name).getValue().toString())) {
                        Element table = doc.getRootElement().getChild(tableName);
                        if (table!=null) {
                            table.addChild(e);
                        }else
                        {
                            Element newTable = doc.createElement(tableName);
                            newTable.addChild(e);
                            doc.getRootElement().addChild(newTable);
                        }
                    }
                }
            }
            docList.add(doc);
        }
    }
    catch (Exception e) {
        throw e;
    }
    return docList;
}

/**
 * 根据某个字段的值获取该字段所在的节点的集合
 * @param document , name ,val
 * @return List < Element >
 */
def getRowListByVal(document, name, val) {
    List list = document.getRootElement().getChildren();
    List<Element> eleList = new ArrayList<Element>();
    try {
        for (int i = 0; i < list.size(); i++) {
            List oldRow = list.get(i).getChildren();
            for (int j = 0; j < oldRow.size(); j++) {
                if (val==oldRow.get(j).getChild(name).getValue()) {
                    eleList.add(oldRow.get(j));
                }
            }
        }
    } catch (Exception e) {
        throw e;
    }
    return eleList;
}

/**
 * 快速生成以root为根节点的document
 *
 * @return
 */
def buildDoc() {
    return TransformFactory.getInstance().newDocument("root");
}

/*
*  根据xml文件生成document
*  @param xml
*  @return document
* */

def createDocByXml(xml) {
    Document doc = null;
    try {
        doc = (new XMLDataSource(xml)).parse();
    } catch (Exception e) {
        throw e;
    }
    return doc;
}

/*
*  根据xml文件生成document
*  @param path
*  @return document
* */

def createDocByFile(path) {
    Document doc = null;
    try {
        File file = new File(path);
        doc = (new XMLDataSource(file)).parse();
    } catch (Exception e) {
        throw e;
    }
    return doc;
}

/**
 * 快速生成element节点
 * @param document name
 * @return
 */
def buildElement(document, name) {
    return document.createElement(name);
}

/**
 * 获取document的根节点element
 * @param document
 * @return
 */
def getRootNodeOfDoc(document) {
    return document.getRootElement();
}

/**
 * 获取element的所有子节点
 * @param element
 * @return
 */
def getAllChildOfElement(element) {
    List list = element.getChildren();
    return list;
}

/**
 * 根据名称获取element的子节点
 * @param element name
 * @return
 */
def getChildByName(element, name) {
    return element.getChild(name);
}

/**
 * 向element中插入值
 * @param element value
 * @return
 */
def setVal2Element(element, value) {
    element.setValue(value);
}
/**
 * 从element中取值
 * @param element name
 * @return
 */
def getElementValue(element) {
    return element.getValue();
}

/**
 * 删除element节点
 * @param document , name
 * @return document
 */
def delElement(document, name) {
    Element e = document.getRootElement().getChild(name);
    document.getRootElement().getChildren().remove(e);
    return document;
}

/**
 * 替换element节点
 * @param document , element , name
 * @return document
 */
def replaceElement(document, name, element) {
    Element e = document.getRootElement().getChild(name);
    document.getRootElement().getChildren().remove(e);
    document.getRootElement().addChild(element);
    return document;
}

/**
 * 向Element节点插入子节点
 * @param document element
 */
def addElement(elementParent, elementChild) {
    elementParent.addChild(elementChild);
}

/**
 * 合并document
 * @param document
 * @return
 */
def mergeDoc(document, retDocument) {
    return DocumentUtil.merge(document, retDocument);
}

/**
 * 根据名称获取参数值
 * @param document name
 * @return
 */
def getParaByName(document, name) {
    return document.getContext().getParameters().getValue(name);
}

/**
 * 获取webservice 适配器返回值
 * @para document
 * @return
 */
def getWsResult(document) {
    return document.getContext().getAttribute("WSResult");
}

/**
 * 根据名称获取数据源
 * @param name
 * @return
 */
def getDsByName(name) {
    DeeResourceDAO dsDao = new DeeResourceDAO()
    String id = dsDao.getDbIdByName(name);
    if (document == null) return null;
    def ds = document.getContext().lookup(id)
    return ds
}

/**
 * 设置参数
 * @param document name  value
 * @return
 */
def setParam(document, name, value) {
    document.getContext().getParameters().add(name, value);
}

def getRootElement(document) {
    return  document.getRootElement();
}

/**
 * 获取开发高级字符串
 * @param document,status ispop reason remark
 * @return
 */
def setHighSetStr(document,status, ispop, reason, remark) {
    String xml = null;

    xml = "<root>" +
            "<deeblockrtn count=\"1\" totalCount=\"1\">" +
            "<row>" +
            "<deestatus>" + status + "</deestatus >" +
            "<dialogispop>" + ispop + "</dialogispop>" +
            "<reason>" + reason + "</reason>" +
            "<remark>" + remark + "</remark>" +
            "</row>" +
            "</deeblockrtn>" +
            "</root>";

    Document d = (new XMLDataSource(xml)).parse();
    return DocumentUtil.merge(document, d);
}
/**
 * 中断任务
 */
def interruptTask() {
    document.getContext().getParameters().add("isInterruptTask", "true_1");
}
/**
 * formData解析
 * @param string
 * @return document
 */
def formDataToDoc() {
	String xml = document.getContext().getParameters().getValue("FormData");
	if(xml == null){
		return null;
	}
    Document doc = null;
    try {
        doc = (new XMLDataSource(xml)).parse();
    } catch (Exception e) {
        throw e;
    }
    return doc;
}

/**
*当前时间，返回格式
		 yyyy-MM-dd HH:mm:ss的字符串
*/
def date_now(){
	return DateUtil.now();
}
/**
*当前日期，返回格式  yyyy-MM-dd
*/
def date_thisDay(){
	return DateUtil.today();
}

/**
*当前月份
*/
def date_thisMonth(){
	return DateUtil.thisMonth();
}
/**
*当前年份
*/
def date_thisYear(){
	return DateUtil.thisYear();
}
/**
*根据时间获取月份
*/
def date_getMonth(date){
	return DateUtil.month(date);
}
/**
*根据时间获取年份
*/
def date_getYear(date){
	return DateUtil.year(date);
}

/**
*根据时间获得季节
*/
def date_getSeason(date){
	return DateUtil.season(date);
}
 
/**
* 根据特定格式格式化日期字符串
*/
def date_format(date, format){
	return DateUtil.format(date,format);
} 
 
/**
* 将特定格式的日期转换为Date对象
* 
*/
def date_parse(dateStr,formatStr){
    return DateUtil.parseDateStr(dateStr, formatStr);
}

/**
 * 字符串是否为空，空的定义如下 1、为null <br>
 * 2、为""<br>
 * @param str 被检测的字符串
 * @return 是否为空
 */
def str_isEmpty(str){
	return StrUtil.isEmpty(str);
}
/**
 * 字符串是否为非空白 空白的定义如下
 *  1、为null <br>
 * 2、为""<br>
 * @param str 被检测的字符串
 * @return 是否为空
 */
def str_isNotEmpty(str){
	return StrUtil.isNotEmpty(str);
} 
/**
 * 除去字符串头尾部的空白 
 * @param str 要处理的字符串
 * @return 除去空白的字符串，如果原字串为null，则返回null
 */
def str_trim(str){
	return StrUtil.trim(str);
}	
/**
 * 是否以指定字符串开头
 * @param str 被监测字符串
 * @param prefix 开头字符串
 * @param isIgnoreCase 是否忽略大小写
 * @return 是否以指定字符串开头
 */
def str_startWith(str, prefix,isIgnoreCase){
	return StrUtil.startWith(str, prefix,isIgnoreCase);
}
/**
 * 是否以指定字符串结尾
 * @param str 被监测字符串
 * @param suffix 结尾字符串
 * @param isIgnoreCase 是否忽略大小写
 * @return 是否以指定字符串结尾
 */
def str_endWith(str, suffix,isIgnoreCase){
	return StrUtil.endWith(str, suffix,isIgnoreCase);
}

/**
 * 切分字符串<br>
 * a#b#c -> [a,b,c]  
 * a##b#c -> [a,"",b,c]
 * @param str 被切分的字符串
 * @param separator 分隔符字符
 * @return 切分后字符数组
 */
def str_split(str, separator){
	return StrUtil.split(str, separator);
}
/**
 * 切分字符串
 * index从0开始计算，最后一个字符为-1<br>
 * 如果from和to位置一样，返回 "" example: abcdefgh 2 3 -> c abcdefgh 2 -3 -> cde
 * @param  str 字符串
 * @param fromIndex 开始的index（包括）
 * @param toIndex 结束的index（不包括）
 * @return 字串
 */
def str_sub(str, fromIndex, toIndex){
	return StrUtil.sub(str, fromIndex, toIndex);
}
/**
 * 数字金额大写转换
 * 先写个完整的然后将如零拾替换成零
 * @param n 数字
 * @return 中文大写数字
 */
def math_digitUppercase(number) {
	return MathUtil.math_digitUppercase(number);
}

/**
 * 加法
 */
def math_add(number1, number2){
	return MathUtil.math_add(number1, number2);
}
/**
 * 减法
 */
def math_sub(number1, number2){
	return MathUtil.math_sub(number1, number2);
}
/**
 * 乘法
 */
def math_mul(number1, number2){
	return MathUtil.math_mul(number1, number2);
}
/**
 * 除法
 */
def math_div(number1, number2){
	return MathUtil.math_div(number1, number2);
}
/**
 * 存储过程调用
 * @param sourceName, procedureName parms
 * @return document
 */
def exeProcedure(sourceName, procedureName, parms) {
	def ds = getDsByName(sourceName);
	try {
        DocumentUtil.merge(document, ProcedureUtil.exeProcedure(procedureName, parms, ds));
	} catch (Exception e) {
		throw e;
	}
}
/**
 * 获取Restservice 适配器返回值
 * @para document
 * @return
 */
def getRestResult(document) {
    return document.getContext().getAttribute("RestResult");
}
/**
 * FormData模拟数据解析
 * @param sourceName, formId
 * @return document
 */
def getTestFormData(sourceName, formId) {
    if (sourceName==null||sourceName==""||formId==null||formId=="")
        return null;
    if (FileUtil.isA8Home())
        return null;
	def ds = getDsByName(sourceName);
    if (ds==null)
        return null;
	try {
		String xml = FormDataUtil.getFormData(ds, formId);
        document.getContext().getParameters().add("FormData",xml);
	} catch (Exception e) {
		throw e;
	}
}
/**
 * 日志清理
 * @param day
 * @return 
 */
def clearLogs(day) {
	try {
		ClearLogUtil.delLogs(day);
	} catch (Exception e) {
		throw e;
	}
}
 