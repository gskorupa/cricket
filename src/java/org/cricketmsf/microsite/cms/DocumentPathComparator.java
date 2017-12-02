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
package org.cricketmsf.microsite.cms;

import org.cricketmsf.out.db.ComparatorIface;

/**
 * Checks if source origin starts with userId+"~"
 * Returns:
 * 0 - OK
 * 1 - NOK
 * -1 - source is not Event
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class DocumentPathComparator implements ComparatorIface {

    @Override
    public int compare(Object source, Object pattern) {
        try {
            if (((Document)pattern).getPath().equals(((Document)source).getPath())) {
                        return 0;
            } else {
                return 1;
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
            return -1;
        }
    }

}
