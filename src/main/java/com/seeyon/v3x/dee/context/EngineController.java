package com.seeyon.v3x.dee.context;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.init.MclclzUtil;
import com.seeyon.v3x.dee.DirectoryWatcher;
import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.LicenceWatcher;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformContext;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.TransformFactory;
import com.seeyon.v3x.dee.config.EngineConfig;
import com.seeyon.v3x.dee.config.EngineContext;
import com.seeyon.v3x.dee.util.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.regex.Pattern;

public class EngineController {
	private static Log log = LogFactory.getLog(EngineController.class);

	private static EngineController controller;

	private EngineContext context;

	private String configFilePath ;

	private Set<String> licenceFlowIdList = new HashSet<String>();

	private boolean isInner = false;

	private static boolean isInA8 = false;

	private static boolean deeEnabled = true;


	private EngineController() {
		configFilePath = TransformFactory.getInstance().getConfigFilePath(EngineConfig.FILENAME_MAIN_CONFIG);
	}

	private EngineController(String configFilePath) {
		this.configFilePath = configFilePath;
	}
	
	public static EngineController getInstance(String configFilePath){
		if (controller == null) {
			controller = configFilePath == null ? new EngineController() : new EngineController(configFilePath);
//			isInA8 = FileUtil.isA8Home();
//			if(isInA8) {
//				try {
//					Object tmp = AppContext.getThreadContext("DEE.dee.enable");
//					deeEnabled = (tmp != null && Boolean.TRUE.equals(tmp));
//					if (deeEnabled) {
//						controller.deeLicenceRun();
//					}
//				} catch (Exception ignored) {
//				}
//			}
		}
		return controller;
	}

	/**
	 * 按名称查找当前配置中的对象。
	 *
	 * @param name XML文件中配置的name属性。
	 * @return 对应的对象实例。
	 * @throws com.seeyon.v3x.dee.TransformException
	 */
	public Object lookup(String name) throws TransformException {
		return this.getContext().lookup(name);
	}

	public Document executeFlow(String flowName) throws TransformException {
		return executeFlow(flowName, TransformFactory.getInstance().newDocument("root"));
	}

	public Document executeFlow(String flowName, Parameters params) throws TransformException {
		return executeFlow(flowName, TransformFactory.getInstance().newDocument("root"), params);
	}

	public Document executeFlow(String flowName, Document input) throws TransformException {
		return executeFlow(flowName, input, null);
	}

	public Document executeFlow(String flowName, Document input, Parameters params) throws TransformException {
		if (isInA8) {
			try {
				//System.out.println("=================================================="	+ com.seeyon.ctp.common.AppContext.hasPlugin("dee") + "===================================================");
				deeEnabled = ((Boolean) Class.forName("com.seeyon.ctp.common.AppContext")
						.getMethod("hasPlugin", String.class).invoke(null, "dee")).booleanValue();
			} catch (Exception e1) {
				log.error("系统错误：无法获取DEE插件状态",e1);
				throw new TransformException("系统错误：无法获取DEE插件状态",e1);
			}
			if(!deeEnabled){
				log.error("系统错误：DEE插件已被禁用");
				throw new TransformException("系统错误：DEE插件已被禁用");
			}

//			boolean hasLicence = true;
//			try {
//				Class<?> c1s = MclclzUtil.ioiekc("com.seeyon.ctp.product.ProductInfo");
//				Boolean isDev = (Boolean) c1s.getMethod("isDev").invoke(null);
//				if (!isDev) {
//					hasLicence = this.hasLicence(flowName);
//				}
//			} catch (Exception e) {
//				log.error("检查时flowName：" + flowName + " licence时失败：", e);
//				throw new TransformException("检查时flowName：" + flowName
//						+ " licence时失败：", e);
//			}
//			if (!hasLicence) {
//				log.error("flowName: " + flowName
//						+ "  licence检验失效，请联系你的管理员");
//				throw new TransformException("flowName: " + flowName
//						+ "  licence检验失效，请联系你的管理员");
//			}

		}
		Flow f = this.getContext().getFlowByName(flowName);
		if(f!=null){
			if(params == null){
				params = new Parameters();
			}
			params.add("flowId", flowName);
			params.add("flow", f);
			TransformContext context = new TransformContextImpl(params,this.context);
			return f.execute(input,context,params);
		}else{
			throw new TransformException("not found:"+flowName);
		}
	}

	private EngineContext getContext() throws TransformException {
		if(context==null){
			refreshContext();
		}
		return  context;
		
	}
	public void refreshContext() throws TransformException {
		EngineConfig config = EngineConfig.getInstance();
		try {
			long start = System.currentTimeMillis();
			log.debug("开始配置解析：");
			EngineContext  tmpContext =  config.parse(configFilePath);
			if (context != null) {
				context.closeAllDataSource();
			}
			context = tmpContext;
			log.debug("结束配置解析：耗时 " + (System.currentTimeMillis()-start)+" 毫秒");
		} catch (Throwable e) {
			throw new TransformException(e);
		}
	}

	/**
	 * @description 轮询licence文件，加载或者更新flowID
	 * @date 2012-4-27
	 * @author liuls
	 */
	private void deeLicenceRun() {
		//监视flow licence key文件，如果发现更新或新增就进行加载
		DirectoryWatcher lw = new LicenceWatcher(TransformFactory.getInstance().getHomeDirectory()
				+ File.separator + "licence", Pattern.compile("(?:.+\\.seeyonkey)"));
		Timer t2 = new Timer();
		t2.schedule(lw, 10000, 60000);
	}

	/**
	 * @description 检查flow是否在licence文件中存在
	 * @date 2012-4-26
	 * @author liuls
	 * @param flowName 任务名称
	 * @return
	 * @throws Exception
	 */
	private boolean hasLicence(String flowName) throws Exception {
		if (isInner) {	//如果是内部用户，就不用做检查
			return true;
		}
		if (this.getLicenceFlowIdList().contains(flowName)) {
			return true;
		}
		return false;
	}

	public Set<String> getLicenceFlowIdList() {
		return licenceFlowIdList;
	}

	public void setInner(boolean isInner) {
		this.isInner = isInner;
	}
}
