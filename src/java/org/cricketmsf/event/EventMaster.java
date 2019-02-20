/*
 * Copyright 2019 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.event;

import java.util.TreeMap;
import org.cricketmsf.exception.EventException;

/**
 *
 * @author greg
 */
public class EventMaster {
    
    public static TreeMap<String,String> register = new TreeMap<>();
    
    public static void registerEventCategories(String[] categories, String eventClassName) throws EventException {
        
        for(int i=0; i<categories.length; i++){
            if(register.containsKey(categories[i])){
                throw new EventException(EventException.CATEGORY_ALREADY_DEFINED, "Event category "+categories[i]+" alredy registered by "+register.get(categories[i]));
            }
            register.put(categories[i], eventClassName);
        }
        
    }
    
}
