/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gskorupa.cricket.in.scheduler;

import com.gskorupa.cricket.Event;

/**
 *
 * @author greg
 */
public interface SchedulerIface {
    
    public void handleEvent(Event event);
    public boolean isRestored();
    
}
