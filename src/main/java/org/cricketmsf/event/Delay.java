/*
 * Copyright 2016 Grzegorz Skorupa <g.skorupa at gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cricketmsf.event;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author greg
 */
public class Delay {
    private TimeUnit unit;
    private long delay;
    private long firstExecutionTime;
    private boolean cyclic = false;
    
    public Delay(){
        delay=-1;
        firstExecutionTime=-1;
        cyclic=false;
        unit=null;
    }

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
        firstExecutionTime=System.currentTimeMillis()+delay;
        this.delay = delay;
    }
    
    public long getFirstExecutionTime(){
        return firstExecutionTime;
    }
    
    public void setFirstExecutionTime(long timepoint){
        firstExecutionTime=timepoint;
    }
    
    @Override
    public String toString(){
        return "DELAY "+getDelay()+" "+getUnit();
    }

    /**
     * @return the cyclic
     */
    public boolean isCyclic() {
        return cyclic;
    }

    /**
     * @param cyclic the cyclic to set
     */
    public void setCyclic(boolean cyclic) {
        this.cyclic = cyclic;
    }
}
