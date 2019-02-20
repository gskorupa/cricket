/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.out.queue;

import org.cricketmsf.exception.QueueException;
import org.cricketmsf.in.queue.QueueCallbackIface;

/**
 *
 * @author greg
 */
public interface QueueClientIface {

    public void publish(String channel, String key, Object value) throws QueueException;
    public void publish(String channel, Object value) throws QueueException;
    public Object show(String channel, String key) throws QueueException;
    public Object get(String channel, String key) throws QueueException;
    public void push(String channel, Object value) throws QueueException;
    public Object pop(String channel) throws QueueException;
    public void purge(String channel) throws QueueException;
    
}
