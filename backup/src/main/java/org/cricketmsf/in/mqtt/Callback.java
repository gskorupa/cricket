/*
 * Copyright 2018 Grzegorz Skorupa .
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

import java.util.logging.Level;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa 
 */
public class Callback implements MqttCallback, MqttSubscriberCallback {

    private static final Logger logger = LoggerFactory.getLogger(Callback.class);

    private MqttClient client;

    public Callback() {
    }
    
    @Override
    public void setClient(MqttClient client){
        this.client=client;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        
    }

    @Override
    public void connectionLost(Throwable cause) {
        try {
            client.reconnect();
        } catch (MqttException ex) {
            logger.warn(ex.getMessage());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

}
