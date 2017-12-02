/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.microsite.out.user;

import java.util.Map;
import org.cricketmsf.microsite.user.User;

/**
 * 
 * @author greg
 */
public interface UserAdapterIface {
    public User get(String uid) throws UserException;
    public Map getAll() throws UserException;
    public User register(User user) throws UserException;
    public void modify(User user) throws UserException;
    public void confirmRegistration(String uid) throws UserException;
    //public void unregister(String uid) throws UserException;
    public void remove(String uid) throws UserException;
    public boolean checkPassword(String uid, String password) throws UserException;
}
