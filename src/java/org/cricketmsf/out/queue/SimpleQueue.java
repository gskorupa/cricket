/*
 * Copyright 2019 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.out.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.cricketmsf.Adapter;
import org.cricketmsf.exception.QueueException; 
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.OutboundAdapterIface;
import org.cricketmsf.in.queue.QueueCallbackIface;

/**
 *
 * @author greg
 */
public class SimpleQueue extends OutboundAdapter implements QueueIface, OutboundAdapterIface, Adapter {

    private HashMap<String, ArrayList<QueueCallbackIface>> subscribers;
    private HashMap<String, HashMap<String, Object>> channels;
    private HashMap<String, ArrayList<Object>> listChannels;
    private int notificationMode = NOTIFY_ALL;

    @Override
    public void add(String channel, String key, Object value) throws QueueException {
        if (!channels.containsKey(channel)) {
            channels.put(channel, new HashMap<String, Object>());
        }
        if(!notify(channel, value)){
            channels.get(channel).put(key, value);
        }
    }

    @Override
    public Object get(String channel, String key) throws QueueException {
        if (!channels.containsKey(channel)) {
            return null;
        }
        Object value = channels.get(channel).get(key);
        if (null != value) {
            channels.get(channel).remove(key);
        }
        return value;
    }

    @Override
    public Object show(String channel, String key) throws QueueException {
        if (!channels.containsKey(channel)) {
            return null;
        }
        return channels.get(channel).get(key);
    }
    
    @Override
    public Object show(String channel) throws QueueException {
        if (!listChannels.containsKey(channel)) {
            return null;
        }
        try {
            return listChannels.get(channel).get(0);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public void subscribe(String channel, QueueCallbackIface callback) throws QueueException {
        if (!subscribers.containsKey(channel)) {
            subscribers.put(channel, new ArrayList<>());
        }
        subscribers.get(channel).add(callback);
    }

    @Override
    public void unsubscribe(String channel, QueueCallbackIface callback) throws QueueException {
        if (!subscribers.containsKey(channel)) {
            return;
        }
        subscribers.get(channel).remove(subscribers.get(channel).indexOf(callback));
    }

    @Override
    public void purge(String channel) throws QueueException {
        if (channels.containsKey(channel)) {
            channels.get(channel).clear();
        }
        if (listChannels.containsKey(channel)) {
            listChannels.get(channel).clear();
        }
    }

    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public Map<String, String> getStatus(String name) {
        return super.getStatus(name);
    }

    @Override
    public void push(String channel, Object value) throws QueueException {
        if (!listChannels.containsKey(channel)) {
            listChannels.put(channel, new ArrayList<>());
        }
        if(!notify(channel, value)){
            listChannels.get(channel).add(value);
        }
    }

    @Override
    public Object pop(String channel) throws QueueException {
        if (!listChannels.containsKey(channel)) {
            return null;
        }
        try {
            return listChannels.get(channel).remove(0);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private boolean notify(String channel, Object value) {
        if(!subscribers.containsKey(channel)){
            subscribers.put(channel, new ArrayList<>());
            return false;
        }
        if(subscribers.get(channel).isEmpty()){
            return false;
        }
        if(notificationMode == NOTIFY_NONE){
            return false;
        }
        if (subscribers.containsKey(channel)) {
            if (notificationMode == NOTIFY_ALL) {
                subscribers.get(channel).forEach((QueueCallbackIface client) -> {
                    client.call(value);
                });
            } else {
                subscribers.get(channel).get(0).call(value);
            }
        }
        return true;
    }
    
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        //this.properties = (HashMap<String,String>)properties.clone();        
        //getStatus(adapterName); //required if we need to overwrite updateStatusItem() method
        subscribers = new HashMap<>();
        channels = new HashMap<>();
        listChannels = new HashMap<>();
    }

    @Override
    public long getSize(String channel) throws QueueException {
        final int LIST = 1;
        final int MAP = 2;
        int type = 0;
        if (listChannels.containsKey(channel)) {
            type=LIST;
        }else if (channels.containsKey(channel)) {
            type=MAP;
        }else{
            throw new QueueException(QueueException.QUEUE_NOT_DEFINED);
        }
        if(type==LIST){
            return listChannels.get(channel).size();
        }else{
            return channels.get(channel).size();
        }
    }

    @Override
    public void setSubscribtionMode(int newMode) throws QueueException {
        if(newMode<0 || newMode >2){
            throw new QueueException(QueueException.NOT_IMPLEMENTED);
        }
        notificationMode = newMode;
    }

    @Override
    public int getSubscribtionMode() {
        return notificationMode;
    }
}
