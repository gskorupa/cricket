/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gskorupa.cricket.in;

import com.gskorupa.cricket.Adapter;
import com.gskorupa.cricket.Event;
import com.gskorupa.cricket.Kernel;
import com.gskorupa.cricket.scheduler.Delay;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author greg
 */
public class Scheduler extends InboundAdapter implements SchedulerIface, Adapter {

    public final ScheduledExecutorService scheduler
            = Executors.newScheduledThreadPool(1);

    public void loadProperties(HashMap<String, String> properties) {
        //todo: persistance
    }

    public void handleEvent(Event event) {

        if (event.getTimePoint() == null) {
            return;
        }

        final Runnable worker = new Runnable() {
            Event ev;

            public void run() {
                // we should reset timepoint to prevent sending this event back from the service
                ev.setTimePoint(null);
                // get event handler of the Kernel
                try {
                    Method m = Kernel.getInstance().getClass().getMethod(getHookMethodNameForEvent(ev.getCategory()), Event.class);
                    m.invoke(Kernel.getInstance(), event);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            public Runnable init(Event event) {
                this.ev = event;
                return (this);
            }
        }.init(event);

        Delay delay = getDelayForEvent(event);

        if (delay.delay > 0) {
            final ScheduledFuture<?> workerHandle
                    = scheduler.schedule(worker, delay.delay, delay.unit);
        }
        /*
        scheduler.schedule(new Runnable() {
            public void run() {
                workerHandle.cancel(true);
            }
        }, 60 * 60, SECONDS);
         */
    }

    private Delay getDelayForEvent(Event ev) {
        Delay d = new Delay();

        boolean wrongFormat = false;
        String dateDefinition = ev.getTimePoint();
        if (dateDefinition.startsWith("+")) {
            try {
                d.delay = Long.parseLong(dateDefinition.substring(1, dateDefinition.length() - 1));
            } catch (NumberFormatException e) {
                wrongFormat = true;
            }
            String unit = dateDefinition.substring(dateDefinition.length() - 1);
            switch (unit) {
                case "d":
                    d.unit = TimeUnit.DAYS;
                    break;
                case "h":
                    d.unit = TimeUnit.HOURS;
                    break;
                case "m":
                    d.unit = TimeUnit.MINUTES;
                    break;
                case "s":
                    d.unit = TimeUnit.SECONDS;
                    break;
                default:
                    wrongFormat = true;
            }
        } else {
            //parse date and replace with delay from now
            d.unit = TimeUnit.MILLISECONDS;
            d.delay = getDelay(dateDefinition);
        }
        if (wrongFormat) {
            return null;
        }
        return d;
    }

    private long getDelay(String dateStr) {
        Date target;
        try {
            target = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss Z").parse(dateStr);
        } catch (ParseException e) {
            return -1;
        }
        return target.getTime() - System.currentTimeMillis();
    }

    @Override
    public void destroy() {
        System.out.print("Stopping scheduler ... ");
        List<Runnable> activeEvents = scheduler.shutdownNow();
        //todo: persistance
        System.out.println("done");
    }

}
