package com.seeyon.v3x.dee.context;

import com.seeyon.v3x.dee.*;
import com.seeyon.v3x.dee.config.EngineContext;
import com.seeyon.v3x.dee.util.FileUtil;
import com.seeyon.v3x.dee.util.UuidUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 转换上下文实现类，用于记录以下信息：<br/>
 * <ul>
 *     <li>上下文属性</li>
 *     <li>上下文参数</li>
 *     <li>引擎上下文</li>
 * </ul>
 */
public class TransformContextImpl implements TransformContext {
	private static final long serialVersionUID = 2327832267666259317L;

	private static Log log = LogFactory.getLog(TransformContextImpl.class);

	private final String id;

	private Map<String, Object> attributes = new HashMap<String, Object>();

	private final Parameters params;

	private EngineContext context;

	public TransformContextImpl(Parameters params, EngineContext context) throws TransformException {
		this.id = UuidUtil.uuid();
		this.context = context;
		this.params = setProp(params);
	}

	@Override
	public void setAttribute(String name, Object object) {
		this.attributes.put(name, object);
	}

	@Override
	public Object getAttribute(String name) {
		return this.attributes.get(name);
	}

	@Override
	public Set<String> getAttributeNames() {
		return this.attributes.keySet();
	}

	@Override
	public void removeAttribute(String name) {
		this.attributes.remove(name);
	}

	@Override
	public Object lookup(String name) {
		return context.lookup(name);
	}

	@Override
	public Parameters getParameters() {
		return params;
	}

	@Override
	public String getId() {
		return id;
	}

	/**
	 * 加载$dee_home下的配置文件dee-resource.properties,将里面的配置加载到context的parameters中
	 *
	 * @param params 需要附加内容的parameters对象
	 * @throws TransformException
	 */
	@SuppressWarnings({ "unchecked" })
	private Parameters setProp(Parameters params) throws TransformException {
		if (params == null) {
			params = new Parameters();
		}

		InputStreamReader inputStreamReader = null;

		try {
			Properties prop = new Properties();
			String filePath = TransformFactory.getInstance().getConfigFilePath("dee-resource.properties");
			File file = new File(filePath);
			if (file.exists()) {
				inputStreamReader = new InputStreamReader(new FileInputStream(file), DEEConstants.CHARSET_UTF8);
				prop.load(inputStreamReader);
				for (Object obj : prop.entrySet()) {
					Map.Entry<String, String> entry = (Map.Entry<String, String>) obj;
					String name = entry.getKey();
					String value = entry.getValue();
					if (StringUtils.isBlank(value)) {
						log.debug(name + "没有对应的值，请检查");
					} else {
						params.add(name, value);
					}
				}
			}

			setA8Parameters(params);
		} catch (IOException e) {
			throw new TransformException("读取propeties文件出错：", e);
		} finally {
			FileUtil.close(inputStreamReader);
		}
		return params;
	}

	/**
	 * 获取A8环境变量
	 * @param params
	 */
	private void setA8Parameters(Parameters params) {
		//是否在A8环境中
		boolean isInA8 = FileUtil.isA8Home();
		if (!isInA8) return;
		try {
			Object app = Class.forName("com.seeyon.ctp.common.AppContext").getMethod("getCurrentUser", null).invoke(null, null);
			if (app != null) {
				//人员名称
				params.add("A8memberName", app.getClass().getMethod("getName", null).invoke(app, null));
				//登录名
				params.add("A8loginName", app.getClass().getMethod("getLoginName", null).invoke(app, null));
				//人员编号
				Object memberId = app.getClass().getMethod("getId", null).invoke(app, null);
				Object memberApp = Class.forName("com.seeyon.ctp.common.AppContext").getMethod("getBean", String.class).invoke(null, new Object[]{"orgManager"});
				if (memberApp != null) {
					Object orgMemberApp = memberApp.getClass().getMethod("getMemberById", Long.class).invoke(memberApp, new Object[]{memberId});
					if (orgMemberApp != null) {
						params.add("A8memberCode", orgMemberApp.getClass().getMethod("getCode", null).invoke(orgMemberApp, null));
					}
				}
				//部门编码
				params.add("A8orgDepartmentId", app.getClass().getMethod("getDepartmentId", null).invoke(app, null));
				//岗位（主岗）编码
				params.add("A8orgPostId", app.getClass().getMethod("getPostId", null).invoke(app, null));
				//职位级别编码
				params.add("A8orgLevelId", app.getClass().getMethod("getLevelId", null).invoke(app, null));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("系统错误：无法获取当前人员状态", e);
		}
	}
}
