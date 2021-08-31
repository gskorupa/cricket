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

import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Callback implements MqttCallback {

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
            Kernel.getInstance().dispatchEvent(Event.logWarning(this, "Unable to deserialize event " + detailedTopic));
            return;
        }
        String category = detailedTopic.substring(0, detailedTopic.indexOf("/"));
        String type = detailedTopic.substring(detailedTopic.indexOf("/") + 1);
        Event event = new Event();
        event.setCategory(category);
        event.setType(type + typeSuffix);
        event.setPayload(message.toString());
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
