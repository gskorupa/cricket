package org.cricketmsf;

import java.util.HashMap;
import java.util.concurrent.ThreadFactory;

public class CricketThreadFactory implements ThreadFactory {

    private int counter;
    private String name;
    private ThreadGroup rootGroup;
    private HashMap<String, ThreadGroup> groups = new HashMap<>();

    public CricketThreadFactory(String name) {
        counter = 1;
        this.name = name;
        rootGroup = new ThreadGroup(name);
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread t = new Thread(runnable, "Scheduler");
        counter++;
        return t;
    }

    public Thread newThread(Runnable runnable, String name) {
        Thread t;
        if(this.name.equalsIgnoreCase(name)){
            t = new Thread(rootGroup, runnable, name + "-Thread_" + counter);
        }else if(groups.containsKey(name)){
            t = new Thread(groups.get(name), runnable, name + "-Thread_" + counter);
        }else{
            groups.put(name, new ThreadGroup(name));
            t = new Thread(groups.get(name), runnable, name + "-Thread_" + counter);
        }
        counter++;
        return t;
    }
    
    public void list(){
        rootGroup.list();
        for(ThreadGroup tg :groups.values()){
            tg.list();
        }
    }

}
