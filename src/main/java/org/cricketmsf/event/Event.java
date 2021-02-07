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

import com.cedarsoftware.util.io.JsonWriter;
import java.util.concurrent.TimeUnit;
import org.cricketmsf.util.JsonReader;
import org.cricketmsf.Kernel;

/**
 * Event
 *
 * @author Grzegorz Skorupa
 */
public class Event {

    private long id;
    private String timeDefinition;
    private long executionTime;
    private long createdAt;
    private boolean cyclic;
    private long cycleLength; //TODO
    private Object data;
    private boolean fromInit;
    private Class origin;
    private int procedure;
    private long eventDelay;
    private boolean valid = true;

    /**
     * Creates new Event instance. Sets new id and createdAt parameters.
     */
    public Event() {
        this.id = Kernel.getEventId();
        this.procedure = Procedures.DEFAULT;
        this.timeDefinition = null;
        this.createdAt = System.currentTimeMillis();
        this.data = null;
        this.fromInit = false;
        this.origin = null;

        this.eventDelay = 0;
        this.executionTime = -1;
        this.cyclic = false;
    }

    /**
     * Used to create new Event instance.Values of id and createdAt parameters
     * are set within the constructor.Parameter timePoint can be one of two
     * forms: a) "+9u" defines distance from event creation."9" - number, "u" -
     * unit (s,m,h,d - seconds, minutes, hours, days) where "9" means 10 seconds
     * after the event creation b) "yyyy.MM.dd HH:mm:ss Z" defines exact time
     * (see: SimpleDateFormat) )
     *
     * @param procedure
     * @param timeDefinition defines when this event should happen.
     * @param data holds additional data
     * @param fromInit
     * @param origin
     */
    public Event(int procedure, String timeDefinition, Object data, boolean fromInit, Class origin) {
        this.id = Kernel.getEventId();
        this.procedure = procedure;
        this.timeDefinition = timeDefinition;
        this.createdAt = System.currentTimeMillis();
        this.data = data;
        this.fromInit = fromInit;
        this.origin = origin;
        calculate(timeDefinition);
    }

    public Event(int procedure, long delay, Object data, boolean fromInit, Class origin) {
        this.id = Kernel.getEventId();
        this.procedure = procedure;
        this.timeDefinition = null;
        this.createdAt = System.currentTimeMillis();
        this.data = data;
        this.fromInit = fromInit;
        this.origin = origin;
        calculate(delay);
    }

    public Event(int procedure) {
        this(procedure, -1, null, false, null);
    }

    public Event(Class origin, int procedure, String timePoint, Object data) {
        this(procedure, timePoint, data, false, origin);
    }

    public Event(Class origin, int procedure, long timePoint, Object data) {
        this(procedure, timePoint, data, false, origin);
    }

    public Event(Object origin, String timePoint, Object data) {
        this(-1, timePoint, data, false, null);
    }

    public Event(Object origin, long timePoint, Object data) {
        this(-1, timePoint, data, false, null);
    }

        private void calculate(String timeDefinition) {
        Delay delay = EventUtils.getDelayFromDateDefinition(timeDefinition, createdAt);
        if (null != delay) {
            this.eventDelay = delay.getDelay();
            this.executionTime = delay.getFirstExecutionTime();
            this.cyclic = delay.isCyclic();
        } else {
            valid = false;
        }
    }

    private void calculate(long delay) {
        this.eventDelay = delay;
        this.executionTime = createdAt + eventDelay;
        this.cyclic = false;
    }

    private void calculate() {
        this.executionTime = createdAt + eventDelay;
        this.cyclic = false;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    public boolean isFutureEvent() {
        return eventDelay > 0;
    }

    public void reschedule() {
        if (isCyclic() && eventDelay > 0) {
            setExecutionTime(eventDelay + System.currentTimeMillis());
        }
    }

    public void calculateExecutionTime(String dateDefinition) {
        Delay delay = EventUtils.getDelayFromDateDefinition(dateDefinition, createdAt);
        if (null != delay) {
            this.eventDelay = delay.getDelay();
            this.executionTime = delay.getFirstExecutionTime();
            this.cyclic = delay.isCyclic();
        } else {
            valid = false;
        }
    }

    /**
     * @return the calculatedTimePoint
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * @param executionTime the calculatedTimePoint to set
     */
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    /**
     * @return the createdAt
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt the createdAt to set
     */
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
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

    public String toJson() {
        return JsonWriter.objectToJson(this);
    }

    public static Event fromJson(String json) {
        return (Event) JsonReader.jsonToJava(json);
    }

    /**
     * @return the data
     */
    public Object getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Object data) {
        this.data = data;
    }

    public String serialize() {
        return JsonWriter.objectToJson(getData());
    }

    public void deserialize(String jsonString) {
        setData(JsonReader.jsonToJava(jsonString));
    }

    public int getProcedure() {
        return procedure;
    }

    public void setProcedure(int procedure) {
        this.procedure = procedure;
    }

    /**
     * @return the fromInit
     */
    public boolean isFromInit() {
        return fromInit;
    }

    /**
     * @param fromInit the fromInit to set
     */
    public void setFromInit(boolean fromInit) {
        this.fromInit = fromInit;
    }

    /**
     * @return the origin
     */
    public Class getOrigin() {
        return origin;
    }

    /**
     * @param origin the origin to set
     */
    public void setOrigin(Class origin) {
        this.origin = origin;
    }

    /**
     * @return the originalDelay
     */
    public long getEventDelay() {
        return eventDelay;
    }

    /**
     * @param eventDelay the originalDelay to set
     */
    public void setEventDelay(long eventDelay) {
        this.eventDelay = eventDelay;
        calculate();
    }

    /**
     * @return the timeDefinition
     */
    public String getTimeDefinition() {
        return timeDefinition;
    }

    public Delay getDelay() {
        Delay result = new Delay();
        result.setCyclic(cyclic);
        result.setDelay(eventDelay);
        result.setFirstExecutionTime(executionTime);
        result.setUnit(TimeUnit.MILLISECONDS);
        return result;
    }

    /**
     * @return the valid
     */
    public boolean isValid() {
        return valid;
    }

}
