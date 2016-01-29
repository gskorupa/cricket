/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gskorupa.cricket.scheduler;

import com.gskorupa.cricket.Event;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author greg
 */
public class Worker implements Runnable, Serializable {

    //private Runnable runnable;
    public String uid = "12345";
    private Event event;
    private Object task;

    public void run() {
        // we should reset timepoint to prevent sending this event back from the service
        getEvent().setTimePoint(null);
        // get event handler of the Kernel
        /*
                try {
                    Method m = Kernel.getInstance().getClass().getMethod(getHookMethodNameForEvent(event.getCategory()), Event.class);
                    m.invoke(Kernel.getInstance(), event);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
         */
        System.out.println("beep");
    }

    public Worker init(Event event) {
        this.setEvent(event);
        return this;
    }

    public Worker() {
        this.uid = "123";
    }
    
    public Worker(String uid, Event event){
        setUid(uid);
        setEvent(event);
    }

    /**
     * @return the runnable
     */
    //public Runnable getRunnable() {
    //  return runnable;
    //}
    /**
     * @param runnable the runnable to set
     */
    //public void setRunnable(Runnable runnable) {
    //  this.runnable = runnable;
    //}
    /**
     * @return the uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * @param uid the uid to set
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    private static final long serialVersionUID = 7526471155622776147L;

    private void readObject(ObjectInputStream aInputStream)
            throws ClassNotFoundException, IOException {
        // always perform the default de-serialization first
        aInputStream.defaultReadObject();
    }

    /**
     * This is the default implementation of writeObject. Customise if
     * necessary.
     */
    private void writeObject(ObjectOutputStream aOutputStream)
            throws IOException {
        // perform the default serialization for all non-transient, non-static
        // fields
        aOutputStream.defaultWriteObject();
    }

    /**
     * @return the event
     */
    public Event getEvent() {
        return event;
    }

    /**
     * @param event the event to set
     */
    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * @return the task
     */
    public Object getTask() {
        return task;
    }

    /**
     * @param task the task to set
     */
    public void setTask(Object task) {
        this.task = task;
    }
}
