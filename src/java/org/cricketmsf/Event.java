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
import java.util.UUID;

/**
 * Event
 *
 * @author Grzegorz Skorupa
 */
public class Event {

    public static final String CATEGORY_LOG = "LOG";
    public static final String CATEGORY_HTTP_LOG = "HTTPLOG";
    public static final String CATEGORY_GENERIC = "EVENT";
    public static final String CATEGORY_HTTP = "HTTP";

    public static final String LOG_ALL = "ALL";
    public static final String LOG_FINEST = "FINEST";
    public static final String LOG_FINE = "FINE";
    public static final String LOG_FINER = "FINER";
    public static final String LOG_INFO = "INFO";
    public static final String LOG_WARNING = "WARNING";
    public static final String LOG_SEVERE = "SEVERE";

    private long id = -1;
    private String name = null;
    private String category;
    private String type;
    private String origin;
    private Object payload;
    private String timePoint; // rename to timeDefinition
    private long calculatedTimePoint = -1; // rename to timeMillis
    private long createdAt = -1;
    private String serviceId;
    private UUID serviceUuid;
    private long rootEventId = -1;
    private RequestObject request = null;
    private boolean cyclic = false;

    /**
     * Creates new Event instance. Sets new id and createdAt parameters.
     */
    public Event() {
        if (id == -1) {
            this.id = Kernel.getEventId();
        }
        createdAt = System.currentTimeMillis();
        serviceId = Kernel.getInstance().getId();
        serviceUuid = Kernel.getInstance().getUuid();
        category = Event.CATEGORY_GENERIC;
        calculateTimePoint();
    }

    /**
     * Used to create new Event instance. Values of id and createdAt parameters
     * are set within the constructor. Parameter timePoint can be one of two
     * forms: a) "+9u" defines distance from event creation. "9" - number, "u" -
     * unit (s,m,h,d - seconds, minutes, hours, days) where "9" means 10 seconds
     * after the event creation b) "yyyy.MM.dd HH:mm:ss Z" defines exact time
     * (see: SimpleDateFormat)
     *
     * @param origin the name of the source of this event
     * @param category event category
     * @param type event type (subcategory)
     * @param timePoint defines when this event should happen.
     * @param payload holds additional data
     */
    public Event(String origin, String category, String type, String timePoint, Object payload) {
        this.origin = origin;
        this.id = Kernel.getEventId();
        this.serviceId = Kernel.getInstance().getId();
        this.serviceUuid = Kernel.getInstance().getUuid();
        this.rootEventId = -1;
        this.category = category;
        this.type = type;
        this.payload = payload;
        if (timePoint != null && timePoint.isEmpty()) {
            this.timePoint = null;
        } else {
            this.timePoint = timePoint;
        }
        createdAt = System.currentTimeMillis();
        calculateTimePoint();
    }

    public Event(String origin, RequestObject request) {
        this.origin = origin;
        this.id = Kernel.getEventId();
        this.serviceId = Kernel.getInstance().getId();
        this.serviceUuid = Kernel.getInstance().getUuid();
        this.rootEventId = -1;
        this.category = Event.CATEGORY_HTTP;
        this.type = "";
        this.payload = null;
        this.request = request;
        this.timePoint = null;
        createdAt = System.currentTimeMillis();
        calculateTimePoint();
    }

    /**
     * Used to create new Event instance. Values of id and createdAt parameters
     * are set within the constructor. Parameter timePoint can be one of two
     * forms: a) "+9u" defines distance from event creation. "9" - number, "u" -
     * unit (s,m,h,d - seconds, minutes, hours, days) where "9" means 10 seconds
     * after the event creation b) "yyyy.MM.dd HH:mm:ss Z" defines exact time
     * (see: SimpleDateFormat)
     *
     * @param origin the name of the source of this event
     * @param category event category
     * @param type event type (subcategory)
     * @param rootEventId the ID of event which starts processing (not created
     * by other event)
     * @param timePoint defines when this event should happen.
     * @param payload holds additional data
     */
    public Event(String origin, String category, String type, long rootEventId, String timePoint, Object payload) {
        this.id = Kernel.getEventId();
        this.serviceId = Kernel.getInstance().getId();
        this.serviceUuid = Kernel.getInstance().getUuid();
        this.rootEventId = rootEventId;
        this.origin = origin;
        this.category = category;
        this.type = type;
        this.payload = payload;
        if (timePoint != null && timePoint.isEmpty()) {
            this.timePoint = null;
        } else {
            this.timePoint = timePoint;
        }
        createdAt = System.currentTimeMillis();
        calculateTimePoint();
    }

    /**
     * Creates new event based with rootEventId set to parent's rootEventId (if
     * not equals -1) or parent's ID (if parent.rootEventId==-1).
     *
     * @return child event
     */
    public Event createChild() {
        Event e = new Event(this.origin, this.category, this.type, this.timePoint, this.payload);
        if (this.rootEventId > -1) {
            e.rootEventId = this.rootEventId;
        } else {
            e.rootEventId = this.id;
        }
        return e;
    }

    
    /**
     * Creates an Event used for logging (category LOG)
     *
     * @param source the Event origin
     * @param level logging level as String ("SEVERE", "INFO", "WARNING" etc)
     * @param message log message
     * @return created Event
     */
    public static Event log(Object source, String level, String message) {
        return new Event(
                source.getClass().getSimpleName(),
                Event.CATEGORY_LOG,
                level,
                null,
                message);
    }

    /**
     * Creates an Event used for logging (category LOG)
     *
     * @param source the Event origin name
     * @param level logging level as String ("SEVERE", "INFO", "WARNING" etc)
     * @param message log message
     * @return created Event
     */
    public static Event log(String source, String level, String message) {
        return new Event(
                source,
                Event.CATEGORY_LOG,
                level,
                null,
                message);
    }

    /**
     * Creates an Event used for logging (category LOG) of SEVERE level
     *
     * @param source the Event origin name
     * @param message log message
     * @return created Event
     */
    public static Event logSevere(String source, String message) {
        return new Event(
                source,
                Event.CATEGORY_LOG,
                Event.LOG_SEVERE,
                null,
                message);
    }

    /**
     * Creates an Event used for logging (category LOG) of WARNING level
     *
     * @param source the Event origin name
     * @param message log message
     * @return created Event
     */
    public static Event logWarning(String source, String message) {
        return new Event(
                source,
                Event.CATEGORY_LOG,
                Event.LOG_WARNING,
                null,
                message);
    }

    /**
     * Creates an Event used for logging (category LOG) of INFO level
     *
     * @param source the Event origin name
     * @param message log message
     * @return created Event
     */
    public static Event logInfo(String source, String message) {
        return new Event(
                source,
                Event.CATEGORY_LOG,
                Event.LOG_INFO,
                null,
                message);
    }

    /**
     * Creates an Event used for logging (category LOG) of FINE level
     *
     * @param source the Event origin name
     * @param message log message
     * @return created Event
     */
    public static Event logFine(String source, String message) {
        return new Event(
                source,
                Event.CATEGORY_LOG,
                Event.LOG_FINE,
                null,
                message);
    }

    /**
     * Creates an Event used for logging (category LOG) of FINER level
     *
     * @param source the Event origin name
     * @param message log message
     * @return created Event
     */
    public static Event logFiner(String source, String message) {
        return new Event(
                source,
                Event.CATEGORY_LOG,
                Event.LOG_FINER,
                null,
                message);
    }

    /**
     * Creates an Event used for logging (category LOG) of FINEST level
     *
     * @param source the Event origin name
     * @param message log message
     * @return created Event
     */
    public static Event logFinest(String source, String message) {
        return new Event(
                source,
                Event.CATEGORY_LOG,
                Event.LOG_FINEST,
                null,
                message);
    }    
    

    /**
     * Creates an Event used for logging (category LOG) of SEVERE level
     *
     * @param source the Event origin name
     * @param message log message
     * @return created Event
     */
    public static Event logSevere(Object source, String message) {
        
        return logSevere(source.getClass().getSimpleName(), message);
    }

    /**
     * Creates an Event used for logging (category LOG) of WARNING level
     *
     * @param source the Event origin name
     * @param message log message
     * @return created Event
     */
    public static Event logWarning(Object source, String message) {
        return logWarning(source.getClass().getSimpleName(), message);
    }

    /**
     * Creates an Event used for logging (category LOG) of INFO level
     *
     * @param source the Event origin name
     * @param message log message
     * @return created Event
     */
    public static Event logInfo(Object source, String message) {
        return logInfo(source.getClass().getSimpleName(), message);
    }

    /**
     * Creates an Event used for logging (category LOG) of FINE level
     *
     * @param source the Event origin name
     * @param message log message
     * @return created Event
     */
    public static Event logFine(Object source, String message) {
        return logFine(source.getClass().getSimpleName(), message);
    }

    /**
     * Creates an Event used for logging (category LOG) of FINER level
     *
     * @param source the Event origin name
     * @param message log message
     * @return created Event
     */
    public static Event logFiner(Object source, String message) {
        return logFiner(source.getClass().getSimpleName(), message);
    }

    /**
     * Creates an Event used for logging (category LOG) of FINEST level
     *
     * @param source the Event origin name
     * @param message log message
     * @return created Event
     */
    public static Event logFinest(Object source, String message) {
        return logFinest(source.getClass().getSimpleName(), message);
    }

    /**
     * Returns event toString
     *
     * @return String representation of the event
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getId())
                .append(":")
                .append(getRootEventId())
                .append(":")
                .append(getOrigin())
                .append(":")
                .append(getServiceId())
                .append(":")
                .append(getServiceUuid().toString())
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

    /**
     * Returns event toString formatted for logging
     *
     * @return String representation of the event
     */
    public String toLogString() {
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
    
    public boolean isFutureEvent(){
        return getTimePoint() != null;
    }

    /**
     * @param timePoint the timePoint to set
     */
    public void setTimePoint(String timePoint) {
        this.timePoint = timePoint;
    }
    
    public void reschedule(){
        if(isCyclic()){
            calculateTimePoint();
        }
    }

    private void calculateTimePoint() {
        String dateDefinition = getTimePoint();
        if (dateDefinition == null) {
            calculatedTimePoint = -1;
            return;
        }
        long delay;
        setCyclic(dateDefinition.startsWith("*") || dateDefinition.indexOf("|*")>0);
        if (dateDefinition.startsWith("+")||dateDefinition.startsWith("*")) {
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
            if(isCyclic()){
                setCalculatedTimePoint(multiplicator * delay + System.currentTimeMillis());
            }else{
                setCalculatedTimePoint(multiplicator * delay + getCreatedAt());
            }
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
     * @return the serviceId
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * @param serviceId the serviceId to set
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * @return the serviceUuid
     */
    public UUID getServiceUuid() {
        return serviceUuid;
    }

    /**
     * @param serviceUuid the serviceUuid to set
     */
    public void setServiceUuid(UUID serviceUuid) {
        this.serviceUuid = serviceUuid;
    }

    /**
     * @return the rootEventId
     */
    public long getRootEventId() {
        return rootEventId;
    }

    /**
     * @param rootEventId the rootEventId to set
     */
    public void setRootEventId(long rootEventId) {
        this.rootEventId = rootEventId;
    }

    public String getRequestParameter(String name) {
        String value = null;
        //try {
        //    value = (String) ((RequestObject) getPayload()).parameters.get(name);
        //} catch (Exception e) {
        //}
        value = (String)getRequest().parameters.get(name);
        return value;
    }

    /**
     * @return the request
     */
    public RequestObject getRequest() {
        //if(httpEvent){
          //  return (RequestObject)payload;
        //}
        //return null;
        return request;
    }
    
    @Override
    public Event clone(){
        Event clon = new Event();
        clon.name = name;
        clon.calculatedTimePoint = calculatedTimePoint;
        clon.category = category;
        clon.createdAt = createdAt;
        clon.origin = origin;
        clon.payload = payload;
        clon.rootEventId = rootEventId;
        clon.serviceId = serviceId;
        clon.serviceUuid = serviceUuid;
        clon.timePoint = timePoint;
        clon.type = type;
        // NOT CLONED:
        //clon.request = null;
        //clon.id = id;
        return clon;
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

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public Event setName(String name) {
        this.name = name;
        return this;
    }

}
