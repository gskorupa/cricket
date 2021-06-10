/*
 * Copyright 2019 Grzegorz Skorupa .
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
package org.cricketmsf.out.messaging;

import org.cricketmsf.queue.QueueIface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.event.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.exception.QueueException;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.OutboundAdapterIface;
import org.cricketmsf.in.queue.QueueCallbackIface;
import org.cricketmsf.out.dispatcher.MessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * If there are no subscribers connected, the oldest objects are deleted when
 * the maximum queue volume is exceeded.
 *
 * @author greg
 */
public class MessageBroker extends OutboundAdapter implements QueueIface, OutboundAdapterIface, Adapter {
    private static final Logger logger = LoggerFactory.getLogger(MessageBroker.class);
    private ConcurrentHashMap<String, ArrayList<QueueCallbackIface>> subscribers;
    private ConcurrentHashMap<String, QueueLinkedMap> channels;
    private ConcurrentHashMap<String, ArrayList<Object>> listChannels;
    private int notificationMode = NOTIFY_ALL;
    int sizeLimit = 100;

    @Override
    public synchronized void add(String channel, String key, Object value) throws QueueException {
        if (!channels.containsKey(channel)) {
            channels.put(channel, new QueueLinkedMap(sizeLimit, sizeLimit));
        }
        if (!notify(channel, value)) {
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
        if (getSubscribtionMode() == NOTIFY_NONE) {
            throw new QueueException(QueueException.SUBSCRIPTION_NOT_POSSIBLE, "Subscribing not possible in DETACHED mode");
        }
        if (!subscribers.containsKey(channel)) {
            subscribers.put(channel, new ArrayList<>());
        }
        subscribers.get(channel).add(callback);
        logger.debug(channel + " subscriber " + callback.getClass().getName());
    }

    @Override
    public void unsubscribe(String channel, QueueCallbackIface callback) throws QueueException {
        if (!subscribers.containsKey(channel) || null == callback) {
            return;
        }
        try {
            subscribers.get(channel).remove(subscribers.get(channel).indexOf(callback));
        } catch (Exception e) {
            throw new QueueException(QueueException.UNKNOWN, "unsubscribe not possible");
        }
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
    public synchronized void push(String channel, Object value) throws QueueException {
        if (!listChannels.containsKey(channel)) {
            listChannels.put(channel, new ArrayList<>());
        }
        if (!notify(channel, value)) {
            if (listChannels.get(channel).size() == sizeLimit) {
                listChannels.get(channel).remove(sizeLimit - 1);
            }
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
        boolean notified = false;
        if (notificationMode == NOTIFY_NONE) {
            return false;
        }
        if (!subscribers.containsKey(channel)) {
            subscribers.put(channel, new ArrayList<>());
        }
        if (!subscribers.containsKey("*")) {
            subscribers.put("*", new ArrayList<>());
        }
        if (subscribers.get(channel).isEmpty() && subscribers.get("*").isEmpty()) {
            return false;
        }
        if (subscribers.containsKey(channel)) {
            if (notificationMode == NOTIFY_ALL) {
                subscribers.get(channel).forEach((QueueCallbackIface client) -> {
                    client.call(channel, value);
                });
            } else {
                if (!subscribers.get(channel).isEmpty()) {
                    subscribers.get(channel).get(0).call(channel, value);
                    return true;
                }
            }
            notified = true;
        }
        if (subscribers.containsKey("*")) {
            if (notificationMode == NOTIFY_ALL) {
                subscribers.get("*").forEach((QueueCallbackIface client) -> {
                    client.call(channel, value);
                });
            } else {
                if (!subscribers.get("*").isEmpty()) {
                    subscribers.get("*").get(0).call(channel, value);
                    return true;
                }
            }
            notified = true;
        }
        return notified;
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        String mode = properties.getOrDefault("mode", "queue").toUpperCase();
        try {
            switch (mode) {
                case "QUEUE":
                    setSubscribtionMode(NOTIFY_FIRST);
                    break;
                case "TOPIC":
                    setSubscribtionMode(NOTIFY_ALL);
                    break;
                case "DETACHED":
                    setSubscribtionMode(NOTIFY_NONE);
                    break;
                default:
                    setSubscribtionMode(NOTIFY_NONE);
                    break;
            }
            logger.info("\tmode: " + mode);
        } catch (QueueException e) {
            logger.info("\tERROR mode " + mode + " not implemented");
        }
        subscribers = new ConcurrentHashMap<>();
        channels = new ConcurrentHashMap<>();
        listChannels = new ConcurrentHashMap<>();
    }

    @Override
    public long getSize(String channel) throws QueueException {
        final int LIST = 1;
        final int MAP = 2;
        int type = 0;
        if (listChannels.containsKey(channel)) {
            type = LIST;
        } else if (channels.containsKey(channel)) {
            type = MAP;
        } else {
            throw new QueueException(QueueException.QUEUE_NOT_DEFINED);
        }
        if (type == LIST) {
            return listChannels.get(channel).size();
        } else {
            return channels.get(channel).size();
        }
    }

    @Override
    public void setSubscribtionMode(int newMode) throws QueueException {
        if (newMode < 0 || newMode > 2) {
            throw new QueueException(QueueException.NOT_IMPLEMENTED);
        }
        notificationMode = newMode;
    }

    @Override
    public int getSubscribtionMode() {
        return notificationMode;
    }

    @Override
    public long getSize() throws QueueException {
        long size = 0;
        Iterator it = listChannels.values().iterator();
        while (it.hasNext()) {
            size += ((List) it.next()).size();
        }
        it = channels.values().iterator();
        while (it.hasNext()) {
            size += ((Map) it.next()).size();
        }
        return size;
    }

    class QueueLinkedMap extends LinkedHashMap {

        private int maxSize = 32;

        QueueLinkedMap(int initialCapacity, int limit) {
            maxSize = limit;
        }

        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > maxSize;
        }
    }
}
