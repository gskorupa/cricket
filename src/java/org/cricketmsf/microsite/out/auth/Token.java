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
package org.cricketmsf.microsite.out.auth;

import java.util.Base64;

/**
 * Session token object
 *
 * @author grzesk
 */
public class Token {

    private static final String PERMANENT_TOKEN_PREFIX = "==";

    private String uid;
    private long timestamp;
    private long eofLife;
    private String token;
    private String issuer;
    private String payload;

    public Token(String userID, long lifetime, boolean permanent) {
        timestamp = System.currentTimeMillis();
        setLifetime(lifetime, permanent);
        uid = userID;
        token = Base64.getEncoder().encodeToString((uid + ":" + timestamp).getBytes());
        if (permanent) {
            token = PERMANENT_TOKEN_PREFIX + token;
        }
    }

    public boolean isValid() {
        //return eofLife - System.currentTimeMillis() > 0;
        return (eofLife < 0 || eofLife - System.currentTimeMillis() > 0);
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
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the eofLife
     */
    public long getEofLife() {
        return eofLife;
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @return the issuer
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * @param issuer the issuer to set
     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * @return the payload
     */
    public String getPayload() {
        return payload;
    }

    /**
     * @param payload the payload to set
     */
    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setLifetime(long lifetime, boolean permanent) {
        if (lifetime < 0 && permanent) {
            eofLife = timestamp + 315360000000L;
            //eofLife = -1;
        } else if (lifetime < 0) {
            eofLife = timestamp + 180*1000; //3min
        } else {
            eofLife = timestamp + lifetime*1000;
        }
    }

    public void setEndOfLife(long eofl) {
        eofLife = eofl;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void refresh() {
        long lt = 600; //10 min
        setTimestamp(System.currentTimeMillis());
        setLifetime(lt,false);
    }
    
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(getToken()).append(":").append(getUid()).append(":").append(getIssuer()).append(":").append(isValid());
        return sb.toString();
    }
}
