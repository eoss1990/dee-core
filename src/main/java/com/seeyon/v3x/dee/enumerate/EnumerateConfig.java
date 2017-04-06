package com.seeyon.v3x.dee.enumerate;

import com.seeyon.v3x.dee.DEEConstants;
import com.seeyon.v3x.dee.TransformFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class EnumerateConfig {
	private final static Log log = LogFactory.getLog(EnumerateConfig.class);
	public static final String CLASSPATH_URL_ENUMERATE = "dictionary.properties";
	private Properties propertie;
	private FileInputStream inputFile;

	private static EnumerateConfig enumerateConfig;

	public static EnumerateConfig getInstance() {
		if (enumerateConfig == null) {
			enumerateConfig = new EnumerateConfig();
		}
		return enumerateConfig;
	}

	public Properties getPropertie() {
		return propertie;
	}

	public void setPropertie(Properties propertie) {
		this.propertie = propertie;
	}

	/**
	 * @description 初始化Configuration类
	 * @date 2011-9-6
	 * @author liuls
	 */
	public EnumerateConfig() {
		propertie = new Properties();

		try {
			inputFile = new FileInputStream(TransformFactory.getInstance()
					.getConfigFilePath(CLASSPATH_URL_ENUMERATE));
			propertie.load(new InputStreamReader(inputFile, DEEConstants.CHARSET_UTF8));
			inputFile.close();
		} catch (FileNotFoundException ex) {
			log.error("File read fail!"+ex.getMessage(),ex);
		} catch (IOException ex) {
			log.error("Load file error!"+ex.getMessage(),ex);
		}
	}
	
	/**
	 * plugin目录下的字典文件</br>
	 * 同时读取系统静态字典与插件字典合集
	 * @author lilong
	 * @param dictPath 插件字典路径
	 */
	public EnumerateConfig(String dictPath) {
		propertie = new Properties();
		try {
			//先读取系统内容静态字典
			inputFile = new FileInputStream(TransformFactory.getInstance()
					.getConfigFilePath(CLASSPATH_URL_ENUMERATE));
			propertie.load(new InputStreamReader(inputFile, DEEConstants.CHARSET_UTF8));
			//这里读取plugin目录下文件
			//例DEE_HOME/plugin/test.dictTest/dictionary.properties
			inputFile = new FileInputStream(TransformFactory.getInstance()
					.getPluginFilePath(dictPath+File.separator+CLASSPATH_URL_ENUMERATE));
			propertie.load(new InputStreamReader(inputFile, DEEConstants.CHARSET_UTF8));
			inputFile.close();
		} catch (FileNotFoundException ex) {
			log.error("File read fail!"+ex.getMessage(),ex);
		} catch (IOException ex) {
			log.error("Load file error!"+ex.getMessage(),ex);
		}
	}

	/**
	 * @description 重载函数，得到key的值
	 * @date 2011-9-6
	 * @author liuls
	 * @param key
	 *            key 取得其值的键
	 * @return key的值
	 */
	public String getValue(String key) {
		if (propertie.containsKey(key)) {
			String value = propertie.getProperty(key);// 得到某一属性的值
			return value;
		} else
			return "";
	}

	/**
	 * @description 重载函数，得到key的值
	 * @date 2011-9-6
	 * @author liuls
	 * @param fileName
	 *            fileName properties文件的路径+文件名
	 * @param key
	 *            key 取得其值的键
	 * @return key的值
	 */
	public String getValue(String fileName, String key) {
		try {
			String value = "";
			inputFile = new FileInputStream(fileName);
			propertie.load(new InputStreamReader(inputFile, DEEConstants.CHARSET_UTF8));
			inputFile.close();
			if (propertie.containsKey(key)) {
				value = propertie.getProperty(key);
				return value;
			} else
				return value;
		} catch (FileNotFoundException e) {
			log.error("根据文件路径查找文件异常：" + e);
			return "";
		} catch (IOException e) {
			log.error("文件流异常：" + e);
			return "";
		} catch (Exception ex) {
			log.error("读取properties文件数据异常：" + ex);
			return "";
		}
	}

	/**
	 * @description 将property解析成对象列表，根据key前缀分类 如：dept.100=100:yangfa
	 *              dept.101=101:jishu 前缀dept相同，他们为同一个对象
	 * @date 2011-9-6
	 * @author liuls
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Enumerate> parseEunm() {

		Set<Object> keySet = propertie.keySet();
		String key;
		String subKey;
		List<Enumerate> enumList = new ArrayList<Enumerate>();
		Map<String, Object> map;
		Enumerate enumObj;
		String value;
		for (Object obj : keySet) {
			key = (String) obj;

			if (key.indexOf(".") > 0) {
				subKey = key.split("\\.")[0];
				enumObj = checkContainEnumerate(enumList, subKey);
				if (enumObj != null) {
					value = this.getValue(key);
					enumObj.getMap().put(value.split(":")[0],
							value.split(":")[1]);
				} else {
					map = new HashMap<String, Object>();
					enumObj = new Enumerate();
					enumObj.setName(subKey);
					value = this.getValue(key);
					if(value.indexOf(":")>-1){ //如果没有包含“:”,则表示格式不对
						map.put(value.split(":")[0], value.split(":")[1]);
						enumObj.setMap(map);
						enumList.add(enumObj);
					}
				}
			}
		}
		return enumList;

	}

	/**
	 * @description 检查是否含有指定名称的对象
	 * @date 2011-9-6
	 * @author liuls
	 * @param enumList
	 * @param name
	 * @return
	 */
	private Enumerate checkContainEnumerate(List<Enumerate> enumList,
			String name) {
		for (Enumerate enumObj : enumList) {
			if (name.equals(enumObj.getName())) {
				return enumObj;
			}
		}
		return null;

	}

	/**
	 * @description 根据key获取值
	 * @date 2011-9-6
	 * @author liuls
	 * @param enumName
	 *            Enumerate 名称staticEnum(${value},bool);表达式里的 bool）
	 * @param key
	 *            指定的key, staticEnum(bool,${value});表达式里的 key）
	 * @return key所对应的value
	 */
	@SuppressWarnings("unchecked")
	public Object getEnumValue(String enumName, String key) {
		/**
		 * ex.boolean=0:false ex.boolean=1:true
		 */
		for (Enumerate enumObj : this.parseEunm()) {
			if (enumName.equals(enumObj.getName())) {
				return enumObj.getMap().get(key);
			}
		}
		return null;

	}

	/**
	 * @description 清除properties文件中所有的key和其值
	 * @date 2011-9-6
	 * @author liuls
	 */
	public void clear() {
		propertie.clear();
	}

	/**
	 * @description 改变或添加一个key的值，当key存在于properties文件中时该key的值被value所代替，
	 *              当key不存在时，该key的值是value
	 * @date 2011-9-6
	 * @author liuls
	 * @param key
	 *            要存入的键
	 * @param value
	 *            要存入的值
	 */
	public void setValue(String key, String value) {
		propertie.setProperty(key, value);
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		EnumerateConfig rc = new EnumerateConfig();

//		System.out.println(rc.getEnumValue("dept", "104"));
		rc.getInstance().getEnumValue("dept", "104");

	}

}
