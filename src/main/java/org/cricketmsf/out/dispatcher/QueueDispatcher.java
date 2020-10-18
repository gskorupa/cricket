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
package org.cricketmsf.out.dispatcher;

import org.cricketmsf.exception.DispatcherException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.event.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.exception.QueueException;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.queue.QueueClientIface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class QueueDispatcher extends OutboundAdapter implements Adapter, DispatcherIface {

    private static final Logger logger = LoggerFactory.getLogger(QueueDispatcher.class);
    private ConcurrentHashMap eventMap = new ConcurrentHashMap();
    private String queueClientName = null;
    private boolean handleAll = false;

    @Override
    public void dispatch(Event event) throws DispatcherException {
        if (null == queueClientName) {
            throw new DispatcherException(DispatcherException.QUEUE_CLIENT_NOT_DEFINED);
        }
        if (handleAll || eventMap.containsKey(event.getClass().getSimpleName())) {
            try {
                ((QueueClientIface) Kernel.getInstance().getAdaptersMap().get(queueClientName)).publish(event.getClass().getName(), event.toJson());
            } catch (QueueException ex) {
                ex.printStackTrace();
                throw new DispatcherException(DispatcherException.QUEUE_EXCEPTION, ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new DispatcherException(DispatcherException.QUEUE_EXCEPTION, ex.getMessage());
            }
        } else {
            throw new DispatcherException(DispatcherException.UNKNOWN_EVENT);
        }
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        String eventClasses = properties.getOrDefault("event-classes", "");
        registerEventTypes(eventClasses);
        logger.info("\tevent-classes: " + eventClasses);
        queueClientName = properties.get("queue-client-name");
        if (null == queueClientName || queueClientName.isEmpty()) {
            logger.warn("\tqueue-client-name parameter is not set.");
        } else {
            logger.info("\tqueue-client-name: " + queueClientName);
        }
    }

    @Override
    public void registerEventTypes(String eventClasses) {
        String[] classes = eventClasses.split(";");
        for (String clazz : classes) {
            if (!clazz.isEmpty()) {
                if ("*".equals(clazz)) {
                    handleAll = true;
                    continue;
                }
                eventMap.put(clazz, clazz);
            }
        }
    }

    @Override
    public DispatcherIface getDispatcher() {
        return this;
    }

}
