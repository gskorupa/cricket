package org.cricketmsf.in.quartz;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TimeZone;
import org.cricketmsf.Adapter;
import org.cricketmsf.in.scheduler.SchedulerIface;
import org.quartz.CronScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.LoggerFactory;

/**
 *
 * @author greg
 */
public class QuartzScheduler extends org.cricketmsf.in.scheduler.Scheduler implements QuartzSchedulerIface, SchedulerIface, Adapter {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(QuartzScheduler.class);
    private boolean running = false;
    private Scheduler qScheduler;
    private String timezone = "UTC";

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        timezone = properties.getOrDefault("timezone", "UTC");
        try {
            qScheduler = new StdSchedulerFactory().getScheduler();
            qScheduler.start();
            running = true;
        } catch (SchedulerException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        try {
            qScheduler.shutdown(true);
        } catch (SchedulerException ex) {
            logger.error(ex.getMessage());
        }
    }

    @Override
    public boolean handleJob(QuartzJob event) {
        Job clazz = (Job) event;
        String cronConfig = null;
        String dateConfig = null;
        cronConfig = (String) ((HashMap<String, Object>) event.getData()).get("cronConfig");
        dateConfig = (String) ((HashMap<String, Object>) event.getData()).get("dateConfig");
        if (null == cronConfig && null == dateConfig) {
            logger.error("cronConfig or dateConfig parameter must be set");
            return false;
        }
        JobDetail job = JobBuilder.newJob(clazz.getClass()).build();
        Trigger trigger;
        if (null == dateConfig) {
            try {
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronConfig);
                trigger = TriggerBuilder
                        .newTrigger()
                        .withSchedule(scheduleBuilder)
                        .build();
            } catch (RuntimeException ex) {
                logger.error("malformed cronConfig event data field element: {}", ex.getMessage());
                return false;
            }
        } else {
            Iterator it = new StringTokenizer(dateConfig, "- :").asIterator();
            int i = 0;
            int[] dateParams = {0, 0, 0, 0, 0, 0};
            while (it.hasNext()) {
                if (i < dateParams.length) {
                    try {
                        dateParams[i] = Integer.parseInt((String) it.next());
                    } catch (NumberFormatException ex) {
                        logger.error("malformed dateConfig event data field element: {}", ex.getMessage());
                        return false;
                    }
                }
                i++;
            }
            if (i != dateParams.length) {
                logger.error("malformed dateConfig event data field");
                return false;
            } else {
                Date configuredDate = DateBuilder
                        .newDateInTimezone(TimeZone.getTimeZone(timezone))
                        .inYear(dateParams[0])
                        .inMonthOnDay(dateParams[1], dateParams[2])
                        .atHourMinuteAndSecond(dateParams[3], dateParams[4], dateParams[5])
                        .build();
                trigger = TriggerBuilder
                        .newTrigger()
                        .startAt(configuredDate)
                        .build();
            }
            trigger = TriggerBuilder
                    .newTrigger()
                    .startAt(DateBuilder.futureDate(0, DateBuilder.IntervalUnit.MINUTE))
                    .build();
        }
        try {
            qScheduler.scheduleJob(job, trigger);
        } catch (SchedulerException ex) {
            logger.error(ex.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void clearJobs() {
        try {
            qScheduler.clear();
        } catch (SchedulerException ex) {
            logger.error(ex.getMessage());
        }
    }

}
