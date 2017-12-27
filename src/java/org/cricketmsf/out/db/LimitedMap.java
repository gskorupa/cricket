/*
 * Copyright 2016 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.out.db;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author greg
 */
public class LimitedMap extends LinkedHashMap{
    
    private int maxSize =0;
    
    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return maxSize>0 ? size() > maxSize : false;
     }

    /**
     * @return the maxSize
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * @param maxSize the maxSize to set
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
    
    @Override
    public synchronized void clear(){
        super.clear();
    }
    
    @Override
    public Object clone(){
        LimitedMap result = (LimitedMap)super.clone();
        /*super.keySet().forEach( key -> {
            result.put(key, super.get(key));
        });*/
        return result;
    }
    
    public synchronized Object remove(String key) {
        return super.remove(key);
    }
    
    public synchronized void put(String key, Object value) {
        super.put(key, value);
    }
    
}
