/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.microsite.out.auth;

import org.cricketmsf.microsite.user.User;

/**
 *
 * @author greg
 */
public interface AuthAdapterIface {

    public Token login(String login, String password) throws AuthException;

    public void userAuthorize(String userId, String role) throws AuthException;

    public void cmsAuthorize(String docId, String role) throws AuthException;

    //public void appAuthorize(String id, String role) throws AuthException;
    public Token createToken(String userID) throws AuthException;
    public Token createConfirmationToken(String userID, String token, long timeout) throws AuthException;

    public boolean checkToken(String tokenID) throws AuthException;

    public boolean logout(String tokenID) throws AuthException;

    public User getUser(String tokenID) throws AuthException;
    
    public User getUser(String tokenID, boolean permanentToken) throws AuthException;
    
    public Token createPermanentToken(String userID, String issuerID, boolean neverExpires, String payload) throws AuthException;
    
    /**
     * Removes permanent token from database
     * @param tokenID token identifier
     * @throws AuthException
     */
    public void removePermanentToken(String tokenID) throws AuthException;
    
    public boolean checkPermanentToken(String tokenID) throws AuthException;
    
    public User getIssuer(String tokenID) throws AuthException;
    
    public void updateToken(String tokenID) throws AuthException;
}
