/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

    public Event(String origin, String category, String type, String payload) {
        this.origin = origin;
        this.category = category;
        this.type = type;
        this.payload = payload;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getOrigin())
                .append(":")
                .append(getCategory())
                .append(":")
                .append(getType())
                .append(":")
                .append(getPayload().toString());
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
}
