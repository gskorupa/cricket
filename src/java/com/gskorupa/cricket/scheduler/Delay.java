/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gskorupa.cricket.scheduler;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author greg
 */
public class Delay {
    private TimeUnit unit;
    private long delay;
    private long definedTimepoint;

    /**
     * @return the unit
     */
    public TimeUnit getUnit() {
        return unit;
    }

    /**
     * @param unit the unit to set
     */
    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    /**
     * @return the delay
     */
    public long getDelay() {
        return delay;
    }

    /**
     * @param delay the delay to set
     */
    public void setDelay(long delay) {
        definedTimepoint=System.currentTimeMillis()+delay;
        this.delay = delay;
    }
    
    public long getDefinedTimepoint(){
        return definedTimepoint;
    }
    
    public String toString(){
        return "DELAY "+getDelay()+" "+getUnit();
    }
}
