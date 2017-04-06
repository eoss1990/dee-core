package com.seeyon.v3x.dee.schedule;

import com.seeyon.v3x.dee.TransformFactory;
import com.seeyon.v3x.dee.config.EngineConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class QuartzManager {
	/**
	 * @Title:Quartz管理类
	 * @Description:
	 * @Copyright:
	 * @author liuls 2012-3-30
	 * 
	 */
	private static Log log = LogFactory.getLog(QuartzManager.class);
	private final static QuartzManager INSTANCE = new QuartzManager();
	public static final String JOB_GROUP_NAME = "DEEJOBGROUP";
	public static final String TRIGGER_GROUP_NAME = "DEETRIGGER";
	private static Scheduler sd;
	private static List<String> list = new ArrayList<String>();

	static{
		SchedulerFactory sf = null;

		String filePath = TransformFactory.getInstance().getConfigFilePath("dee-quartz.properties");
		File file = new File(filePath);
		if (file.exists()) {
			InputStream fis = null;
			try {
				fis = new FileInputStream(file);
				Properties properties = new Properties();
				properties.load(fis);
				sf = new StdSchedulerFactory(properties);
				log.info("DEE定时器初始化完成！");
			} catch (IOException e) {
				log.error("定时器初始化IO异常：" + e.getLocalizedMessage(), e);
			} catch (SchedulerException e) {
				log.error("定时器初始化异常：" + e.getLocalizedMessage(), e);
			} finally {
				try {
					if (fis != null) {
						fis.close();
					}
				} catch (IOException ignored) {
				}
			}
		}

		if (sf == null) {
			sf = new StdSchedulerFactory();
		}
		try {
			sd = sf.getScheduler();
		} catch (SchedulerException e) {
			log.error(e);
		}
	}
	/**
	 * 创建一个调度对象
	 * 
	 * @return
	 * @throws org.quartz.SchedulerException
	 */
	public static QuartzManager getInstance() {
		INSTANCE.getScheduleInst();
		return INSTANCE;
	}

	private static Scheduler getScheduler() {
		Scheduler scheduler = null;
		try {
			if (sd == null) {
				SchedulerFactory sf = new StdSchedulerFactory();
				scheduler = sf.getScheduler();
			} else {
				return sd;
			}
		} catch (SchedulerException e) {
			log.error("scheduler获取异常：" + e);
		}
		return scheduler;
	}

	public Scheduler getScheduleInst() {
		return sd;
	}

	public List<String> getJobList() {
		return list;
	}

	/**
	 * @description 解析dee.xml发起flow quartz
	 * @date 2012-3-30
	 * @author liuls
	 */
	@SuppressWarnings({ "unchecked" })
	public synchronized void flowSchedule() {
		String file = TransformFactory.getInstance().getConfigFilePath(
				EngineConfig.FILENAME_MAIN_CONFIG);
		File f = new File(file);
		if (!f.exists()) {
			return;
		}
		SAXReader saxReader = new SAXReader();
		try {
			// 解析dee配置文件
			Document document = saxReader.read(f);
			Element root = document.getRootElement();
			List<Element> s = root.selectNodes("//schedule");
			String flowName = "";
			String quartzTime = "";
			Map<String, String> propMap = new HashMap<String, String>();

			int i = 0;
			for (Element e : s) {
				i++;
				List<Element> pElemList = e.selectNodes("property");
				for (Element p : pElemList) {
					if ("flow".equals(p.attributeValue("name"))) {
						flowName = p.attributeValue("value");
					} else if ("quartzTime".equals(p.attributeValue("name"))) {
						quartzTime = p.attributeValue("value");
					} else {
						propMap.put(p.attributeValue("name"),
								p.attributeValue("value"));
					}
				}
				Class clazz = Class.forName(e.attributeValue("class"))
						.newInstance().getClass();
				String jobName = "job" + UUID.randomUUID();
				list.add(jobName);
				addJob(jobName, clazz, quartzTime, flowName, propMap);

			}
		} catch (Exception e) {
			log.error("解析dee配置文件" + file + "出错", e);
		}
	}

	/** */
	/**
	 * 添加一个定时任务，使用默认的任务组名，触发器名，触发器组名
	 * 
	 * @param jobName
	 *            任务名
	 * @param job
	 *            任务
	 * @param time
	 *            时间设置，参考quartz说明文档
	 * @throws org.quartz.SchedulerException
	 * @throws java.text.ParseException
	 */
	@SuppressWarnings("unchecked")
	public static void addJob(String jobName, Class job, String time,
			String flowName, Map propMap) throws SchedulerException,
			ParseException {
		JobDetail jobDetail = new JobDetail(jobName, JOB_GROUP_NAME, job);// 任务名，任务组，任务执行类
		jobDetail.getJobDataMap().put("flowName", flowName);
		// jobDetail.getJobDataMap().put( "propMap" ,
		// propMap);//因为环境不同，该语句会有问题，暂时不保存其他的参数
		// 触发器
		CronTrigger trigger = new CronTrigger(jobName, TRIGGER_GROUP_NAME);// 触发器名,触发器组
		trigger.setCronExpression(time);// 触发器时间设定
		sd.scheduleJob(jobDetail, trigger);
		if (!sd.isShutdown()) {
			sd.start();
		}
	}

	/**
	 * @description 重新启动quartz
	 * @date 2012-3-30
	 * @author liuls
	 * @throws org.quartz.SchedulerException
	 */
	public void refresh() throws SchedulerException {
		sd.pauseAll();
		synchronized (list) {
			for (String s : list) {
//				System.out.println("删除job=======================================："+s);
				sd.deleteJob(s, JOB_GROUP_NAME);
			}
			list.clear();
			getInstance().flowSchedule();
			sd.resumeAll();
		}
	}
}
