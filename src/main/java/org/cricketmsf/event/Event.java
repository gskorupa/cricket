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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.cricketmsf.util.JsonReader;
import org.cricketmsf.Kernel;

/**
 * Event
 *
 * @author Grzegorz Skorupa
 */
public class Event {

    private Long id = null;
    private String timeDefinition = null;
    private long timeMillis = -1;
    private long createdAt = -1;
    private boolean cyclic = false;
    private Object data;
    //private String procedureName;
    private String initialTimePoint;
    private boolean fromInit = false;
    private Class origin;
    private int procedure = Procedures.DEFAULT;

    /**
     * Creates new Event instance. Sets new id and createdAt parameters.
     */
    public Event() {
        if (null == id) {
            this.id = Kernel.getEventId();
        }
        createdAt = System.currentTimeMillis();
        //calculateTimePoint();
        //procedureName = null;
        procedure = Procedures.DEFAULT;
        data = null;
        fromInit = false;
        origin = null;
    }

    public Event(int procedure) {
        this(procedure, -1, null, false, null);
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
     * @param timePoint defines when this event should happen.
     * @param data holds additional data
     * @param fromInit
     * @param origin
     */
    public Event(int procedure, String timePoint, Object data, boolean fromInit, Class origin) {
        this.id = Kernel.getEventId();
        this.procedure = procedure;
        if (timePoint != null && timePoint.isEmpty()) {
            this.timeDefinition = null;
        } else {
            this.timeDefinition = timePoint;
        }
        createdAt = System.currentTimeMillis();
        this.fromInit = fromInit;
        calculateTimePoint();
        setData(data);
        this.origin = origin;
    }

    public Event(int procedure, long timePoint, Object data, boolean fromInit, Class origin) {
        this.id = Kernel.getEventId();
        this.procedure = procedure;
        createdAt = System.currentTimeMillis();
        calculateTimePoint(timePoint);
        setData(data);
        this.fromInit = fromInit;
        this.origin = origin;
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

    /**
     * @return the timePoint
     */
    public String getTimeDefinition() {
        return timeDefinition;
    }

    public boolean isFutureEvent() {
        return getTimeDefinition() != null;
    }

    /**
     * @param timeDefinition the timePoint to set
     */
    public void setTimeDefinition(String timeDefinition) {
        this.timeDefinition = timeDefinition;
    }

    public Event timePoint(String timePointDefinition) {
        setTimeDefinition(timePointDefinition);
        calculateTimePoint();
        return this;
    }

    public void reschedule() {
        if (isCyclic()) {
            calculateTimePoint();
        }
    }

    private void calculateTimePoint(long timeDelay) {
        if (timeDelay > -1) {
            setTimeMillis(timeDelay + createdAt);
        } else {
            setTimeMillis(-1);
        }
    }

    private void calculateTimePoint() {
        String dateDefinition = getTimeDefinition();
        if (fromInit && dateDefinition == null) {
            setTimeMillis(-1);
            return;
        }
        long delay;
        setCyclic(dateDefinition.startsWith("*") || dateDefinition.indexOf("|*") > 0);
        if (dateDefinition.startsWith("+") || dateDefinition.startsWith("*")) {
            try {
                delay = Long.parseLong(dateDefinition.substring(1, dateDefinition.length() - 1));
            } catch (NumberFormatException e) {
                setTimeMillis(-1);
                return;
            }
            String unit = dateDefinition.substring(dateDefinition.length() - 1);
            long multiplicator = 1;
            switch (unit) {
                case "d": //day
                    multiplicator = 24 * 60 * 60000;
                    break;
                case "h": //hour
                    multiplicator = 60 * 60000;
                    break;
                case "m": //minute
                    multiplicator = 60000;
                    break;
                case "s": //second
                    multiplicator = 1000;
                    break;
                default:
                    setTimeMillis(-1);
                    return;
            }
            if (isCyclic()) {
                setTimeMillis(multiplicator * delay + createdAt);
            } else {
                setTimeMillis(multiplicator * delay + getCreatedAt());
            }
        } else {
            //parse date and replace with delay from now
            Date target;
            try {
                target = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss Z").parse(dateDefinition);
                setTimeMillis(target.getTime());
            } catch (ParseException e) {
                setTimeMillis(-1);
            }
        }
    }

    /**
     * @return the calculatedTimePoint
     */
    public long getTimeMillis() {
        return timeMillis;
    }

    /**
     * @param timeMillis the calculatedTimePoint to set
     */
    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }

    public void setCalculatedTimePoint(Delay delay, long now) {
        long time = now;
        switch (delay.getUnit()) {
            case DAYS:
                time = time + delay.getDelay() * 3600000 * 24;
                break;
            case HOURS:
                time = time + delay.getDelay() * 3600000;
                break;
            case MILLISECONDS:
                time = time + delay.getDelay();
                break;
            case MINUTES:
                time = time + delay.getDelay() * 60000;
                break;
            case SECONDS:
                time = time + delay.getDelay() * 1000;
                break;
            default:
                time = 0;
        }
        timeMillis = time;
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

    /*
    public String getProcedureName() {
        return procedureName;
    }
    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }
     */
    public int getProcedure() {
        return procedure;
    }

    public void setProcedure(int procedure) {
        this.procedure = procedure;
    }

    /**
     * @return the initialTimePoint
     */
    public String getInitialTimePoint() {
        return initialTimePoint;
    }

    /**
     * @param initialTimePoint the initialTimePoint to set
     */
    public void setInitialTimePoint(String initialTimePoint) {
        this.initialTimePoint = initialTimePoint;
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

}
