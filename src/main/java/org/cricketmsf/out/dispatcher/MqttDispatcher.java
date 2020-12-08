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
package org.cricketmsf.out.dispatcher;

import org.cricketmsf.exception.DispatcherException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.event.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.event.Procedures;
import org.cricketmsf.in.mqtt.EventSubscriberCallback;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.mqtt.MqttPublisher;
import org.cricketmsf.out.mqtt.MqttPublisherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class MqttDispatcher extends OutboundAdapter implements Adapter, DispatcherIface{
    private static final Logger logger = LoggerFactory.getLogger(MqttDispatcher.class);
    private String clientID;
    private String brokerURL;
    private boolean debug = false;
    private int qos = 1;
    private String rootTopic = "events/";
    private ConcurrentHashMap eventMap = new ConcurrentHashMap();

    @Override
    public void dispatch(Event event) throws DispatcherException {
        String topic = event.getClass().getName() + "/" + Procedures.getName(event.getProcedure());
        if (eventMap.containsKey(event.getClass().getName() + "/*") || eventMap.containsKey(topic)) {
            try {
                MqttPublisher.publish(brokerURL, clientID, qos, debug, rootTopic+topic, (String) event.getData());
            } catch (MqttPublisherException ex) {
                logger.error(ex.getCode() + " " + ex.getMessage());
            }
        } else {
            throw new DispatcherException(DispatcherException.UNKNOWN_EVENT);
        }
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        clientID = properties.getOrDefault("client-id", "");
        if(clientID.isEmpty()){
            clientID=Kernel.getInstance().getUuid().toString();
        }
        this.properties.put("client-id", clientID);
        logger.info("\tclient-id: " + clientID);
        brokerURL = properties.get("url");
        this.properties.put("url", brokerURL);
        logger.info("\turl: " + brokerURL);
        try {
            this.properties.put("qos", properties.getOrDefault("qos", "0"));
            qos = Integer.parseInt(this.properties.getOrDefault("qos", "0"));
            if (qos > 2) {
                qos = 2;
            }
        } catch (NumberFormatException e) {
        }
        logger.info("\tqos: " + qos);
        try {
            this.properties.put("debug", properties.getOrDefault("debug", "false"));
            debug = Boolean.parseBoolean(this.properties.getOrDefault("debug", "false"));
        } catch (NumberFormatException e) {
        }
        logger.info("\tdebug: " + debug);
        try {
            rootTopic = properties.getOrDefault("root-topic", "CricketService");
            if (!rootTopic.endsWith("/")) {
                rootTopic = rootTopic.concat("/");
            }
            this.properties.put("root-topic", rootTopic);
            logger.info("\troot-topic: " + rootTopic);
            String eventTypes = properties.getOrDefault("event-types", "");
            registerEventTypes(eventTypes);
            logger.info("\tevent-types: " + eventTypes);
            logger.info("\tevent-types-configured: " + eventMap.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerEventType(String category, String type) throws DispatcherException {
        if (type == null || type.isEmpty()) {
            eventMap.put(category + "/*", null);
        } else {
            eventMap.put(category + "/" + type, null);
        }
    }

    @Override
    public void registerEventTypes(String pathsConfig) {
        String[] paths = pathsConfig.split(";");
        for (String path : paths) {
            if (!path.isEmpty()) {
                eventMap.put(path, null);
            }
        }
    }

    @Override
    public DispatcherIface getDispatcher() {
        return this;
    }
}
