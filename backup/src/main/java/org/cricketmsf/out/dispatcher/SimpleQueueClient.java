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

import java.util.HashMap;
import java.util.logging.Level;
import org.cricketmsf.Adapter;
import org.cricketmsf.Kernel;
import org.cricketmsf.event.Event;
import org.cricketmsf.event.EventDto;
import org.cricketmsf.exception.DispatcherException;
import org.cricketmsf.exception.QueueException;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.queue.QueueClientIface;
import org.cricketmsf.queue.QueueIface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author greg
 */
public class SimpleQueueClient extends OutboundAdapter implements DispatcherIface, QueueClientIface, Adapter {

    private static final Logger logger = LoggerFactory.getLogger(SimpleQueueClient.class);
    private QueueIface queue = null;
    String queueAdapterName = null;
    private boolean ready = false;

    private QueueIface getQueue() throws QueueException {
        if (null == queue) {
            try {
                queue = (QueueIface) Kernel.getInstance().getAdaptersMap().get(queueAdapterName);
            } catch (Exception e) {
            }
        }
        if (null == queue) {
            throw new QueueException(QueueException.QUEUE_NOT_DEFINED);
        }
        return queue;
    }

    @Override
    public void publish(String channel, String key, Object value) throws QueueException {
        getQueue().add(channel, key, value);
    }

    @Override
    public void publish(String channel, Object value) throws QueueException {
        getQueue().push(channel, value);
    }

    @Override
    public Object getCopy(String channel, String key) throws QueueException {
        return getQueue().show(channel, key);
    }

    @Override
    public Object getCopy(String channel) throws QueueException {
        return getQueue().show(channel);
    }

    @Override
    public Object get(String channel, String key) throws QueueException {
        return getQueue().get(channel, key);
    }

    @Override
    public void push(String channel, Object value) throws QueueException {
        getQueue().push(channel, value);
    }

    @Override
    public Object pop(String channel) throws QueueException {
        return getQueue().pop(channel);
    }

    @Override
    public void purge(String channel) throws QueueException {
        getQueue().purge(channel);
    }

    @Override
    public long getQueueSize(String channel) throws QueueException {
        return getQueue().getSize(channel);
    }

    @Override
    public long getQueueSize() throws QueueException {
        return getQueue().getSize();
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        queueAdapterName = properties.get("queue-adapter-name");
        if (null == queueAdapterName || queueAdapterName.isEmpty()) {
            logger.info("\tWARNING! queue-adapter-name parameter is not set.");
        } else {
            logger.info("\tqueue-adapter-name: " + queueAdapterName);
        }
    }

    @Override
    public void dispatch(Event event) throws DispatcherException {
        try {
            EventDto dto = new EventDto(event);
            publish(dto.eventClassName, dto.toJson());
        } catch (QueueException ex) {
            throw new DispatcherException(DispatcherException.QUEUE_EXCEPTION, ex.getMessage());
        } catch (ClassCastException ex) {
            throw new DispatcherException(DispatcherException.UNKNOWN, ex.getMessage());
        }
    }

    @Override
    public void registerEventTypes(String categories) throws DispatcherException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void start() {
        try {
            getQueue();
            ready = true;
        } catch (QueueException ex) {
            logger.error(ex.getMessage());
        }
    }

    @Override
    public boolean isReady() {
        return ready;
    }

}
