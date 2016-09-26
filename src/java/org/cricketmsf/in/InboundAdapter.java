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
package org.cricketmsf.in;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import org.cricketmsf.Event;
import org.cricketmsf.annotation.InboundAdapterHook;
import org.cricketmsf.Kernel;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class InboundAdapter implements Runnable{
    
    protected HashMap<String, String> hookMethodNames;
    
    public InboundAdapter(){
        hookMethodNames = new HashMap<>();
    }
    
    public void destroy(){        
    }
    
    @Override
    public void run(){   
    }
    
    protected Result handle(String method, String payload){
        String hookMethodName = getHookMethodNameForMethod(method);
        Result result = null;
        if (hookMethodName == null) {
            Kernel.handle(
                    Event.logWarning(this.getClass().getSimpleName(), "hook is not defined for " + method)
            );
        }
        try {
            Kernel.handle(
                Event.logInfo(this.getClass().getSimpleName(), "sending event to hook " + method)
            );
            Event event = new Event();
            event.setOrigin(this.getClass().getSimpleName());
            event.setPayload(payload);
            Method m = Kernel.getInstance().getClass().getMethod(hookMethodName, Event.class);
            result = (Result) m.invoke(Kernel.getInstance(), event);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return result;
}
    
    protected void getServiceHooks(String adapterName) {
        if(hookMethodNames==null){
            hookMethodNames = new HashMap<>();
        }
        InboundAdapterHook ah;
        String requestMethod="X";
        // for every method of a Kernel instance (our service class extending Kernel)
        for (Method m : Kernel.getInstance().getClass().getMethods()) {
            ah = (InboundAdapterHook) m.getAnnotation(InboundAdapterHook.class);
            // we search for annotated method
            if (ah != null) {
                requestMethod = ah.inputMethod();
                if (ah.adapterName().equals(adapterName)) {
                    addHookMethodNameForMethod(requestMethod, m.getName());
                    System.out.println("kernel hook for method " + requestMethod + " : " + m.getName());
                }
            }
        }
    }
    
    public void addHookMethodNameForMethod(String requestMethod, String hookMethodName) {
        hookMethodNames.put(requestMethod, hookMethodName);
    }

    public String getHookMethodNameForMethod(String requestMethod) {
        String result = null;
        result = hookMethodNames.get(requestMethod);
        if (null == result) {
            result = hookMethodNames.get("*");
        }
        return result;
    }
}
