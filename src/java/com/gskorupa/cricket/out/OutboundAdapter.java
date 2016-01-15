/*
 * Copyright 2015 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package com.gskorupa.cricket.out;

import com.gskorupa.cricket.Event;
import com.gskorupa.cricket.EventHook;
import com.gskorupa.cricket.Kernel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class OutboundAdapter {

    private HashMap<String, String> eventHookMethods =new HashMap();
    
    public OutboundAdapter(){
        getEventHooks();
    }
    
    public void addHookMethodNameForEvent(String eventCategory, String hookMethodName) {
        eventHookMethods.put(eventCategory, hookMethodName);
    }

    protected void getEventHooks() {
        EventHook ah;
        String eventCategory;
        // for every method of a Kernel instance (our service class extending Kernel)
        for (Method m : Kernel.getInstance().getClass().getMethods()) {
            ah = (EventHook) m.getAnnotation(EventHook.class);
            // we search for annotated method
            if (ah != null) {
                eventCategory = ah.eventCategory();
                addHookMethodNameForEvent(eventCategory, m.getName());
                System.out.println("hook method for event category " + eventCategory + " : " + m.getName());
            }
        }
    }
    
    public String getHookMethodNameForEvent(String eventCategory) {
        String result = null;
        result = eventHookMethods.get(eventCategory);
        if (null == result) {
            result = eventHookMethods.get("*");
        }
        return result;
    }
    
    protected void sendEvent(Event event){
        try {
            Method m = Kernel.getInstance().getClass()
                    .getMethod(getHookMethodNameForEvent(event.getCategory()),Event.class);
            m.invoke(Kernel.getInstance(), event);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    
    public void destroy(){
        
    }

}
