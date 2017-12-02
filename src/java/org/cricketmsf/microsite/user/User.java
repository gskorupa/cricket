/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.microsite.user;

/**
 *
 * @author greg
 */
public class User {

    /**
     * @return the generalNotificationChannel
     */
    public String getGeneralNotificationChannel() {
        return generalNotificationChannel;
    }

    /**
     * @return the infoNotificationChannel
     */
    public String getInfoNotificationChannel() {
        return infoNotificationChannel;
    }

    /**
     * @return the warningNotificationChannel
     */
    public String getWarningNotificationChannel() {
        return warningNotificationChannel;
    }

    /**
     * @return the alertNotificationChannel
     */
    public String getAlertNotificationChannel() {
        return alertNotificationChannel;
    }

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
    private String role;
    private boolean confirmed;
    private boolean unregisterRequested;
    private String confirmString;
    private String password;
    private String generalNotificationChannel = "";
    private String infoNotificationChannel = "";
    private String warningNotificationChannel = "";
    private String alertNotificationChannel = "";
    private int authStatus;
    private long createdAt;

    public User() {
        confirmed = false;
        unregisterRequested = false;
        authStatus = IS_REGISTERING;
        createdAt = System.currentTimeMillis();
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

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
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
        //this.password = password;
    }

    public boolean checkPassword(String passToCheck) {
        return getPassword() != null && getPassword().equals(HashMaker.md5Java(passToCheck));
    }

    public String[] getChannelConfig(String eventTypeName) {
        String channel = "";
        switch (eventTypeName) {
            case "GENERAL":
                channel = getGeneralNotificationChannel();
            case "INFO":
                channel = getInfoNotificationChannel();
            case "WARNING":
                channel = getWarningNotificationChannel();
            case "ALERT":
                channel = getAlertNotificationChannel();
        }
        if(channel==null){
            channel="";
        }
        return channel.split(":");
    }

    /**
     * @param generalNotificationChannel the generalNotificationChannel to set
     */
    public void setGeneralNotificationChannel(String generalNotificationChannel) {
        this.generalNotificationChannel = generalNotificationChannel;
    }

    /**
     * @param infoNotificationChannel the infoNotificationChannel to set
     */
    public void setInfoNotificationChannel(String infoNotificationChannel) {
        this.infoNotificationChannel = infoNotificationChannel;
    }

    /**
     * @param warningNotificationChannel the warningNotificationChannel to set
     */
    public void setWarningNotificationChannel(String warningNotificationChannel) {
        this.warningNotificationChannel = warningNotificationChannel;
    }

    /**
     * @param alertNotificationChannel the alertNotificationChannel to set
     */
    public void setAlertNotificationChannel(String alertNotificationChannel) {
        this.alertNotificationChannel = alertNotificationChannel;
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

}
