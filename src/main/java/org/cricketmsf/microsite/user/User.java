/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cricketmsf.microsite.user;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author greg
 */
public class User {

    public static final int USER = 0; // default type, normal user
    public static final int OWNER = 1; //
    public static final int APPLICATION = 2; // application
    public static final int DEMO = 3;
    public static final int FREE = 4;
    public static final int PRIMARY = 5;
    public static final int READONLY = 6;

    public static final int IS_REGISTERING = 0;
    public static final int IS_ACTIVE = 1;
    public static final int IS_UNREGISTERING = 2;
    public static final int IS_LOCKED = 3;

    private int type = USER;
    private String uid;
    private String email;
    private String name;
    private String surname;
    private String role;
    private boolean confirmed;
    private boolean unregisterRequested;
    private String confirmString;
    private String password;
    private int authStatus;
    private long createdAt;
    private long number;
    private long organization;

    public User() {
        confirmed = false;
        unregisterRequested = false;
        authStatus = IS_REGISTERING;
        createdAt = System.currentTimeMillis();
        organization = 0L;
    }

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

    /**
     * @return the confirmed
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * @param confirmed the confirmed to set
     */
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
        if (this.confirmed) {
            this.setStatus(IS_ACTIVE);
        } else {
            this.setStatus(IS_REGISTERING);
        }
    }

    /**
     * @return the waitingForUnregister
     */
    public boolean isUnregisterRequested() {
        return unregisterRequested;
    }

    /**
     * @param unregisterRequested the waitingForUnregister to set
     */
    public void setUnregisterRequested(boolean unregisterRequested) {
        this.unregisterRequested = unregisterRequested;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * newUser.setType(User.OWNER);
     *
     * @return the confirmString
     */
    public String getConfirmString() {
        return confirmString;
    }

    /**
     * @param confirmString the confirmString to set
     */
    public void setConfirmString(String confirmString) {
        this.confirmString = confirmString;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    public List getRoles() {
        return Arrays.asList(getRole().split(","));
    }

    public boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role.toLowerCase();
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
        // this.password = password;
    }

    public boolean checkPassword(String passToCheck) {
        return getPassword() != null && getPassword().equals(HashMaker.md5Java(passToCheck));
    }

    /**
     * @return the status
     */
    public int getStatus() {
        return authStatus;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.authStatus = status;
    }

    public long getOrganization() {
        return organization;
    }

    public void setOrganization(long organization) {
        this.organization = organization;
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
     * @return the number
     */
    public long getNumber() {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(long number) {
        this.number = number;
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
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @param surname the surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

}
