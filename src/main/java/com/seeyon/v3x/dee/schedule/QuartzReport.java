package com.seeyon.v3x.dee.schedule;

import com.seeyon.v3x.dee.DEEClient;
import com.seeyon.v3x.dee.Parameters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class QuartzReport implements Job {
    private static Log log = LogFactory.getLog(QuartzReport.class);

    public void execute(JobExecutionContext context) throws JobExecutionException {
        String flowName = (String) context.getJobDetail().getJobDataMap().get("flowName");
        try {
            DEEClient client = new DEEClient();
            if (client.lookup(flowName) != null) {
            	Parameters parms = new Parameters();
            	Map<String, String> formFlow_Data = new HashMap<String, String>();
    			formFlow_Data.put("name", "");
    			formFlow_Data.put("id", "");
    			formFlow_Data.put("person", "");
    			formFlow_Data.put("action", "定时器");
    			parms.add("formFlow_Data", formFlow_Data);
                client.execute(flowName, parms);
            }
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            log.error("定时器执行失败，flowId：" + flowName + "，" + target.getLocalizedMessage(), target);
            throw new JobExecutionException("schedule error or flow execute error", target);
        } catch (Throwable e) {
            log.error("定时器执行失败，flowId：" + flowName + "，" + e.getLocalizedMessage(), e);
            throw new JobExecutionException("schedule error or flow execute error", e);
        }
    }
}
