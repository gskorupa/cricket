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
package com.gskorupa.cricket;

/**
 *
 * @author greg
 */
public class Event {

    public static final String CATEGORY_LOG = "LOG";
    public static final String CATEGORY_GENERIC = "EVENT";

    public static final String LOG_ALL = "ALL";
    public static final String LOG_FINEST = "FINEST";
    public static final String LOG_INFO = "INFO";
    public static final String LOG_WARNING = "WARNING";
    public static final String LOG_SEVERE = "SEVERE";

    private long id;
    private String category;
    private String type;
    private String origin;
    private Object payload;
    private String timePoint;

    public Event() {
        this.id = Kernel.getEventId();
    }

    public Event(String origin, String category, String type, String timePoint, Object payload) {
        this.id = Kernel.getEventId();
        this.origin = origin;
        this.category = category;
        this.type = type;
        this.payload = payload;
        this.timePoint=timePoint;
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
}
