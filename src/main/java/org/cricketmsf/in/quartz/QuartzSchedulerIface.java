package org.cricketmsf.in.quartz;

/**
 *
 * @author greg
 */
public interface QuartzSchedulerIface {
    
    public boolean handleJob(QuartzJob job);
    public void clearJobs();
}
