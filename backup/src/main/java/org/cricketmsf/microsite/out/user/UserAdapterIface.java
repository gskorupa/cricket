/*
 * Copyright 2017 Grzegorz Skorupa .
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
package org.cricketmsf.microsite.out.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cricketmsf.api.Result;

/**
 * 
 * @author greg
 */
public interface UserAdapterIface {
    public Result handleGet(HashMap params);
    public Result handleRegisterUser(User newUser);
    public Result handleUpdateUser(HashMap params);
    public Result handleDeleteUser(HashMap params);
    
    public User get(String uid) throws UserException;
    public User getByNumber(long number) throws UserException;
    public Map getAll() throws UserException;
    public User register(User user) throws UserException;
    public void modify(User user) throws UserException;
    public void confirmRegistration(String uid) throws UserException;
    //public void unregister(String uid) throws UserException;
    public void remove(String uid) throws UserException;
    public boolean checkPassword(String uid, String password) throws UserException;
}
