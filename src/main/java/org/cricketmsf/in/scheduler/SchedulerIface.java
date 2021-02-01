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
package org.cricketmsf.in.scheduler;

import org.cricketmsf.event.Event;

/**
 *
 * @author greg
 */
public interface SchedulerIface {
    
    public boolean handleEvent(Event event);
    public boolean handleEvent(Event event, boolean restored, boolean systemStart);
    //public boolean forceHandleEvent(Event event);
    public boolean isRestored();
    public long getThreadsCount();
    public boolean isScheduled(String eventID);
    public String getProperty(String name);
    public void initScheduledTasks();
    public void reschedule(String className, int procedure, Long newDelay);
}
