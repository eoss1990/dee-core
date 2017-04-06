package com.seeyon.v3x.dee.function;

import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.TransformFactory;
import com.seeyon.v3x.dee.script.ScriptRunner;
import com.seeyon.v3x.dee.util.ConfigUtil;
import com.seeyon.v3x.dee.util.FileUtil;
import com.seeyon.v3x.dee.util.ReflectException;
import com.seeyon.v3x.dee.util.ReflectUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FunctionEngineManager implements FunctionEngine {
	private static Log log = LogFactory.getLog(FunctionEngineManager.class);
	private final static String FUNCTION_FILE_NAME = "function.xml";
	private Document document;
	private List<Tag> tagList;
//	private Function function;
	private String functionScript = null;
	
	private static FunctionEngineManager INSTANCE ;
	private static byte[] mutex = new byte[0];
	private FunctionEngineManager(){
		loadFunction();
	}
	public static FunctionEngineManager getInstance() {
		synchronized (mutex) {
		    if (INSTANCE == null) {  
		    	INSTANCE = new FunctionEngineManager();
		    	
//		    	INSTANCE.parse();
		    }  
		}
	    return INSTANCE;  
    }
	private void loadFunction(){
		try {
			functionScript = FileUtil.getResource("com/seeyon/v3x/dee/function/function.groovy");
			
//			functionScript ="import com.seeyon.v3x.dee.function.UUIDTag;def uuid(){def o = new com.seeyon.v3x.dee.function.UUIDTag();o.execute();}";
			log.debug("加载预置function完毕："+functionScript);
		} catch (Throwable e) {
			log.error("加载function.groovy失败。"+e.getMessage(),e);
		}
	}
	public void parse(){
		SAXReader saxReader = new SAXReader();
//		this.function = new Function();
		try {
			document = saxReader.read(TransformFactory.getInstance().getConfigFilePath(FUNCTION_FILE_NAME));
			List<Element> tagElementList = document.selectNodes("//tag" ); //解析flow节点
//			function.setTagList(parseTagList(tagElementList));
			parseTagList(tagElementList);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}

	private List<Tag> parseTagList(List<Element> tagElementList ) throws ReflectException {
		this.tagList = new ArrayList<Tag>();
		for(Element tagElement:tagElementList){
			Tag tag  = (Tag) ReflectUtil.reflectClass(tagElement.elementText("tag-class"));
			tag.setName(tagElement.element("name").getTextTrim());
			this.getTagList().add(tag);
		}
		return tagList;
		
	}
	
	/**
	 * @description  根据tag名称获得tag
	 * @date 2011-9-16
	 * @author liuls
	 * @param tagName tag名称
	 * @return
	 */
	public Tag getTag(String tagName){
		for(Tag tag : tagList){
			if(tagName.equals(tag.getName())){
				return tag;
			}
		}
		return null;
	}
	/**
	 * @description  根据tag的名字执行tag
	 * @date 2011-9-16
	 * @author liuls
	 * @param tagName tag名称
	 * @return 执行后的结果
	 * @throws com.seeyon.v3x.dee.TransformException
	 */
	public Object executeTag(String tagName) throws TransformException {
	   return this.getTag(tagName).execute();
	}
	/**
	 * @description 执行tag，根据map的key值将value用反射机制设置到tag里去，再执行tag通用算法 executeTag()
	 *              表达式如<property name="formDate(value=${dateValue}, formate='YYYY-MM-dd')"  type="java.lang.String"/>
	 *              被解析成 name=formDate ,map{(value:103),(formate:YYYY-MM-dd)},做为该函数的两个参数
	 * @date 2011-9-16
	 * @author liuls
	 * @param map 存有tag属性名称和属性值的键值对map
	 * @return 执行后的结果
	 * @throws com.seeyon.v3x.dee.TransformException
	 */
	@SuppressWarnings("unchecked")
	public Object executeTag(String tagName,Map<String,Object> map) throws TransformException {
		log.debug("execute:"+tagName);
		Tag tag = this.getTag(tagName);
		if(tag==null){
			log.error("unkown function:"+tagName);
			return null;
		}
		if(map!=null){
			Set<String> key = map.keySet();
		    String methodName = "";
	        for (Iterator it = key.iterator(); it.hasNext();) {
	            String s = (String) it.next();
				Class<? extends Tag> clazz = tag.getClass();
	            methodName = ConfigUtil.getMethodByField(s);
	    		Object obj[] = {map.get(s)};
	    		try {  
	    			ReflectUtil.invokeMethod(tag, methodName, obj);
	    		} catch (Exception e) {
	    			log.error(e.getMessage(),e);
	    		}
	        }
		}
        return tag.execute();

	}
	
	public Object execute(String script,Map<String,Object> params){
		ScriptRunner runner = new ScriptRunner();
		Object o;
		try {
			Map<String,Object> binding = new HashMap<String,Object>();
			// 加func_前缀，避免名称冲突
			if(params!=null){
				for (Map.Entry<String,Object> entry : params.entrySet()) {
					//flow对象不加func_前缀
					if("flow".equals(entry.getKey()))
						binding.put(entry.getKey(), entry.getValue());
					else
						binding.put("func_"+entry.getKey(), entry.getValue());
				}
			}
			o = runner.eval(this.functionScript+";"+script,binding);
//			System.out.println(o);
			return o;
		} catch (ScriptException e) {
			log.debug(e.getMessage(),e);
			return null;
		}
	}
	
//	/**
//	 * @description 执行tag，表达式如<property name="formDate(value=${dateValue}, formate='YYYY-MM-dd')"/>
//	 *              被解析成 name=formDate ,map{(value:103),(formate:YYYY-MM-dd)},做为该函数的两个参数，其中
//	 *               ${dateValue} 表示document的一个类似xpath的路径（table1/column1），需要document解析去找到value 
//	 * @date 2011-9-26
//	 * @author liuls 
//	 * @param tagName 所执行tag的名称
//	 * @param map  参数键值对
//	 * @param document  对于外部参数，需要document参数区解析获得值
//	 * @return
//	 */
//	public Object executeTag(String tagName,Map <String,Object>map,Document document){
//		Tag tag = this.getTag(tagName);
//		Set<String> key = map.keySet();
//		String methodName = "";
//		for (Iterator it = key.iterator(); it.hasNext();) {
//			String s = (String) it.next();
//			Class<? extends Tag> clazz = tag.getClass();
//			methodName = ConfigUtil.getMethodByField(s);
//			Object obj[] = {map.get(s)};
//			try {  
//				ConfigUtil.invokeMethod(tag, methodName, obj);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return tag.executeTag();
//		
//	}
	
/*	public Function getFunction() {
		return function;
	} */
	
	public List<Tag> getTagList() {
		return tagList;
	}
	

}
