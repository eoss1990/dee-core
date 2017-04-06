package com.seeyon.v3x.dee.config;

import com.seeyon.v3x.dee.datasource.JDBCDataSource;
import com.seeyon.v3x.dee.dictionary.Dictionary;
import com.seeyon.v3x.dee.schedule.Schedule;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.context.Flow;
import com.seeyon.v3x.dee.resource.DataSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EngineContext {
	private Map<String,Object> beans = new LinkedHashMap<String, Object>();

	/**
	 * 按名称查找当前配置中的对象。
	 *
	 * @param name XML文件中配置的name属性。
	 * @return 对应的对象实例。
	 */
	public Object lookup(String name) {
		return this.beans.get(name);
	}

	/**
	 * 按类型查找所有配置对象。
	 *
	 * @param clazz 类型
	 * @return 指定类型的对象列表。
	 */
	public <T> List<T> findAll(Class<T> clazz) {
		List<T> l = new ArrayList<T>();
		for (Object bean : beans.values()) {
			if (bean != null && bean.getClass() == clazz) {
				l.add((T) bean);
			}
		}
		return l;
	}

	public void add(String name,Object bean) throws TransformException {
		// 单线程，不加锁
		if(this.beans.containsKey(name)){
			throw new TransformException("指定名称"+name+"的实体已存在，请检查您的配置文件。");
		}
		this.beans.put(name, bean);
	}

	public Flow getFlowByName(String name) {
		return find(name,Flow.class);
	}

	public DataSource getDataSourceByName(String name) {
		return find(name,DataSource.class);
	}

	public Schedule getScheduleByName(String name) {
		return find(name,Schedule.class);
	}

	public Dictionary getDictionaryByName(String name) {
		return find(name,Dictionary.class);
	}

	/**
	 * @description 根据节点名称查找flow子节点及其兄弟节点
	 * @date 2011-9-5
	 * @author liuls
	 * @param objectName
	 * @return
	 */
	public Object getElementByName(String objectName) {
		if (objectName == null) {
			return null;
		}
		Flow flow = getFlowByName(objectName);
		if (flow != null) {
			return flow;
		}
		DataSource datasource = getDataSourceByName(objectName);
		if (datasource != null) {
			return datasource;
		}
		Schedule schedule = getScheduleByName(objectName);
		if (schedule != null) {
			return schedule;
		}
		Dictionary dictionary = getDictionaryByName(objectName);
		if (dictionary != null) {
			return dictionary;
		}
		// TODO 代码非常神奇，没看懂，将下面两行放到最前面，单元测试过不了
		Object o = lookup(objectName);
		if(o!=null) return o;
		return null;
	}

	public List<DataSource> getDatasourceList() {
		return findAll(DataSource.class);
	}
	public List<Flow> getFlowList() {
		return findAll(Flow.class);
	}
	public List<Schedule> getScheduleList() {
		return findAll(Schedule.class);
	}
	public List<Dictionary> getDictionaryList() {
		return findAll(Dictionary.class);
	}

	public void closeAllDataSource() {
		//关闭所有连接池
        List<JDBCDataSource> dataSources = findAll(JDBCDataSource.class);
        for (JDBCDataSource dataSource : dataSources) {
            if (dataSource != null) {
                dataSource.close();
            }
        }
    }

	private <T> T find(String name, Class<T> clazz) {
		Object o = lookup(name);
		if (o != null && clazz.isAssignableFrom(o.getClass())) {
			return (T) o;
		}
		return null;
	}
}
