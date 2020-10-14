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
package org.cricketmsf.in.mqtt;

import org.cricketmsf.event.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.dispatcher.QueueDispatcher;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class Callback implements MqttCallback {
    
    private static final Logger logger = LoggerFactory.getLogger(Callback.class);
    private String rootTopic;
    private String typeSuffix;
    private int prefixLength = 0;

    public Callback(String rootTopic, String typeSuffix) {
        this.rootTopic = rootTopic;
        if (!this.rootTopic.endsWith("/")) {
            this.rootTopic = this.rootTopic.concat("/");
        }
        prefixLength = this.rootTopic.length();
        this.typeSuffix = typeSuffix;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String detailedTopic = topic.substring(prefixLength);
        if (detailedTopic.indexOf("/") < 1) {
            logger.warn("Unable to deserialize event " + detailedTopic);
            return;
        }
        String className = detailedTopic.substring(0, detailedTopic.indexOf("/"));
        String procedurename = detailedTopic.substring(detailedTopic.indexOf("/") + 1);
        Event event = (Event)Class.forName(className).newInstance();
        event.setProcedureName(procedurename);
        //event.setType(type + typeSuffix);
        event.setData(message.toString());
        Kernel.getInstance().dispatchEvent(event);
    }

    @Override
    public void connectionLost(Throwable cause) {
        cause.printStackTrace();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

}
