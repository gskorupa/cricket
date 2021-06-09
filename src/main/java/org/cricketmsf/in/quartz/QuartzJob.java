package org.cricketmsf.in.quartz;

import java.util.HashMap;
import org.cricketmsf.event.Event;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class QuartzJob extends Event implements Job {
    
    private HashMap<String,Object> data;
    
    public QuartzJob(){
        super();
        data=new HashMap<>();
    }
    
    public HashMap<String,Object> getData(){
        return data;
    }

    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        System.out.println("Dummy Quartz Job");
    }
}
