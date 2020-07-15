/*
 * Copyright 2018 Grzegorz Skorupa <g.skorupa at gmail.com>.
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.cricketmsf.out.db.ComparatorIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class DocumentPathAndTagComparator implements ComparatorIface {

    @Override
    public int compare(Object source, Object pattern) {
        try {
            Document s = (Document) source;
            Document p = (Document) pattern;
            String path = p.getPath();
            if (!path.isEmpty()) {
                if (!path.equals(s.getPath())) {
                    return 1;
                }
            }
            if (!p.getTags().isEmpty()) {
                Set<String> patternSet = new HashSet<String>(Arrays.asList(p.tagsAsArray()));
                Set<String> sourceSet = new HashSet<String>(Arrays.asList(s.tagsAsArray()));
                sourceSet.retainAll(patternSet);
                if (sourceSet.isEmpty()) {
                    return 1;
                }
            }
            return 0;
        } catch (ClassCastException e) {
            e.printStackTrace();
            return -1;
        }
    }

}
