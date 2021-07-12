/**
 * Copyright (C) Grzegorz Skorupa 2021
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package org.cricketmsf.microsite.out.auth;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author greg
 */
public class UserProxy {

    private String uid;
    private String role;

    public UserProxy() {
        role = "";
    }

    public UserProxy(String uid, String role) {
        setUid(uid);
        setRole(role);
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

    public String getRole() {
        return null != role ? role : "";
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
        if (null == role) {
            role = "";
        } else {
            this.role = role.toLowerCase();
        }
    }
}
