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
package org.cricketmsf.queue;

import org.cricketmsf.exception.QueueException;

/**
 *
 * @author greg
 */
public interface QueueClientIface {

    public void publish(String channel, String key, Object value) throws QueueException;
    public void publish(String channel, Object value) throws QueueException;
    public Object getCopy(String channel, String key) throws QueueException;
    public Object getCopy(String channel) throws QueueException;
    public Object get(String channel, String key) throws QueueException;
    public void push(String channel, Object value) throws QueueException;
    public Object pop(String channel) throws QueueException;
    public void purge(String channel) throws QueueException;
    public long getQueueSize(String channel) throws QueueException;
    public long getQueueSize() throws QueueException;
}
