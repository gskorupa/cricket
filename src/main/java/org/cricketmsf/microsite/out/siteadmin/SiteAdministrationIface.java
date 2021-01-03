/*
 * Copyright 2020 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.microsite.out.siteadmin;

import org.cricketmsf.api.StandardResult;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.out.db.KeyValueDBIface;

/**
 *
 * @author greg
 */
public interface SiteAdministrationIface {

    public void initDatabases(
            KeyValueDBIface database,
            KeyValueDBIface userDB,
            KeyValueDBIface authDB
    );

    public void backupDatabases(
            KeyValueDBIface database,
            KeyValueDBIface userDB,
            KeyValueDBIface authDB,
            KeyValueDBIface cmsDB,
            String errorLevel
    );
    
    public void clearUserData(String userId);

    public void clearData(
            boolean demoMode,
            String category,
            String type,
            UserAdapterIface userAdapter,
            KeyValueDBIface database);
    
    public StandardResult getServiceInfo();

}
