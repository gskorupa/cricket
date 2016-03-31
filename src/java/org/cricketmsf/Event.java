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
package org.cricketmsf;

import org.cricketmsf.scheduler.Delay;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author greg
 */
public class Event {

    public static final String CATEGORY_LOG = "LOG";
    public static final String CATEGORY_GENERIC = "EVENT";

    public static final String LOG_ALL = "ALL";
    public static final String LOG_FINEST = "FINEST";
    public static final String LOG_FINE = "FINE";
    public static final String LOG_FINER = "FINER";
    public static final String LOG_INFO = "INFO";
    public static final String LOG_WARNING = "WARNING";
    public static final String LOG_SEVERE = "SEVERE";

    private long id = -1;
    private String category;
    private String type;
    private String origin;
    private Object payload;
    private String timePoint; // rename to timeDefinition
    private long calculatedTimePoint = -1; // rename to timeMillis
    private long createdAt=-1;

    public Event() {
        if (id == -1) {
            this.id = Kernel.getEventId();
        }
        createdAt=System.currentTimeMillis();
    }

    public Event(String origin, String category, String type, String timePoint, Object payload) {
        this.id = Kernel.getEventId();
        this.origin = origin;
        this.category = category;
        this.type = type;
        this.payload = payload;
        this.timePoint = timePoint;
        createdAt=System.currentTimeMillis();
        calculateTimePoint();
    }
    
    public static Event log(Object source, String level, String message){
        return new Event(
                source.getClass().getSimpleName(),
                Event.CATEGORY_LOG,
                level,
                null,
                message);
    }
    
    public static Event log(String source, String level, String message){
        return new Event(
                source,
                Event.CATEGORY_LOG,
                level,
                null,
                message);
    }
    
    public static Event logSevere(String source, String message){
        return new Event(
                source,
                Event.CATEGORY_LOG,
                Event.LOG_SEVERE,
                null,
                message);
    }

    public static Event logWarning(String source, String message){
        return new Event(
                source,
                Event.CATEGORY_LOG,
                Event.LOG_WARNING,
                null,
                message);
    }
    
    public static Event logInfo(String source, String message){
        return new Event(
                source,
                Event.CATEGORY_LOG,
                Event.LOG_INFO,
                null,
                message);
    }
    
    public static Event logFine(String source, String message){
        return new Event(
                source,
                Event.CATEGORY_LOG,
                Event.LOG_FINE,
                null,
                message);
    }
    
    public static Event logFiner(String source, String message){
        return new Event(
                source,
                Event.CATEGORY_LOG,
                Event.LOG_FINER,
                null,
                message);
    }
    
    public static Event logFinest(String source, String message){
        return new Event(
                source,
                Event.CATEGORY_LOG,
                Event.LOG_FINEST,
                null,
                message);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getId())
                .append(":")
                .append(getOrigin())
                .append(":")
                .append(getCategory())
                .append(":")
                .append(getType())
                .append(":")
                .append(getTimePoint())
                .append(":")
                .append(getPayload() != null ? getPayload().toString() : "");
        return sb.toString();
    }
    
    public String toLogString(){
        StringBuilder sb = new StringBuilder();
        sb.append(getId())
                .append(":")
                .append(getOrigin())
                .append(":")
                .append(getPayload() != null ? getPayload().toString() : "");
        return sb.toString();
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
     * @return the type
     */
    public String getType() {
        return type != null ? type : "";
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the origin
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * @param origin the origin to set
     */
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /**
     * @return the payload
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * @param payload the payload to set
     */
    public void setPayload(Object payload) {
        this.payload = payload;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category != null ? category : "";
    }

    /**
     * @param subtype the category to set
     */
    public void setCategory(String subtype) {
        this.category = subtype;
    }

    /**
     * @return the timePoint
     */
    public String getTimePoint() {
        return timePoint;
    }

    /**
     * @param timePoint the timePoint to set
     */
    public void setTimePoint(String timePoint) {
        this.timePoint = timePoint;
    }
    
    private void calculateTimePoint() {
        String dateDefinition = getTimePoint();
        if (dateDefinition == null) {
            calculatedTimePoint = -1;
            return;
        }
        long delay;
        if (dateDefinition.startsWith("+")) {
            try {
                delay = Long.parseLong(dateDefinition.substring(1, dateDefinition.length() - 1));
            } catch (NumberFormatException e) {
                setCalculatedTimePoint(-1);
                return;
            }
            String unit = dateDefinition.substring(dateDefinition.length() - 1);
            long multiplicator = 1;
            switch (unit) {
                case "d":
                    multiplicator = 24 * 60 * 60000;
                    break;
                case "h":
                    multiplicator = 60 * 60000;
                    break;
                case "m":
                    multiplicator = 60000;
                    break;
                case "s":
                    multiplicator = 1000;
                    break;
                default:
                    setCalculatedTimePoint(-1);
                    return;
            }
            setCalculatedTimePoint(multiplicator * delay + createdAt);
        } else {
            //parse date and replace with delay from now
            Date target;
            try {
                target = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss Z").parse(dateDefinition);
                setCalculatedTimePoint(target.getTime());
            } catch (ParseException e) {
                setCalculatedTimePoint(-1);
            }
        }
    }

    /**
     * @return the calculatedTimePoint
     */
    public long getCalculatedTimePoint() {
        return calculatedTimePoint;
    }

    /**
     * @param calculatedTimePoint the calculatedTimePoint to set
     */
    public void setCalculatedTimePoint(long calculatedTimePoint) {
        this.calculatedTimePoint = calculatedTimePoint;
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
        calculatedTimePoint = time;
    }
}
