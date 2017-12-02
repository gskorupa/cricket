/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.microsite.out.queue;

import java.util.List;
import org.cricketmsf.Event;

/**
 * 
 * @author greg
 */
public interface QueueAdapterIface {
    public void init(String helperName, String categoriesToHandle, String categoriesToIgnore) throws QueueException;
    public boolean isHandling(String eventCategoryName);
    public List<Event> get(String path) throws QueueException;
    public void send(String path, Event event) throws QueueException;
    public void send(Event event) throws QueueException;
}
