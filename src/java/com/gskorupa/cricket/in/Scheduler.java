/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gskorupa.cricket.in;

import com.gskorupa.cricket.Adapter;
import com.gskorupa.cricket.Event;
import com.gskorupa.cricket.Kernel;
import com.gskorupa.cricket.db.KeyValueStore;
import com.gskorupa.cricket.scheduler.Delay;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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

    private String storagePath;
    private String envVariable;
    private String fileName;
    private KeyValueStore database;
    protected boolean restored = false;

    private long MINIMAL_DELAY = 5000;
    
    public final ScheduledExecutorService scheduler
            = Executors.newScheduledThreadPool(1);

    @Override
    public void loadProperties(HashMap<String, String> properties) {

        setStoragePath(properties.get("path"));
        System.out.println("path: " + getStoragePath());
        setEnvVariable(properties.get("envVariable"));
        System.out.println("envVAriable name: " + getEnvVariable());
        if (System.getenv(getEnvVariable()) != null) {
            setStoragePath(System.getenv(getEnvVariable()));
        }
        // fix to handle '.'
        if (getStoragePath().startsWith(".")) {
            setStoragePath(System.getProperty("user.dir") + getStoragePath().substring(1));
        }
        setFileName(properties.get("file"));
        System.out.println("file: " + getFileName());
        String pathSeparator = System.getProperty("file.separator");
        setStoragePath(
                getStoragePath().endsWith(pathSeparator)
                ? getStoragePath() + getFileName()
                : getStoragePath() + pathSeparator + getFileName()
        );
        System.out.println("scheduler database file location: " + getStoragePath());
        database = new KeyValueStore();
        database.setStoragePath(getStoragePath());
        database.read();
        setRestored(database.getSize()>0);
        processDatabase();
    }

    private void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    private String getStoragePath() {
        return storagePath;
    }

    private void setEnvVariable(String envVariable) {
        this.envVariable = envVariable;
    }

    private String getEnvVariable() {
        return envVariable;
    }

    public void handleEvent(Event event) {
        handleEvent(event, false);
    }

    public void handleEvent(Event event, boolean restored) {

        if (event.getTimePoint() == null) {
            return;
        }

        final Runnable runnable = new Runnable() {
            Event ev;

            public void run() {
                // we should reset timepoint to prevent sending this event back from the service
                ev.setTimePoint(null);
                // get event handler of the Kernel
                try {
                    Method m = Kernel.getInstance().getClass().getMethod(getHookMethodNameForEvent(ev.getCategory()), Event.class);
                    m.invoke(Kernel.getInstance(), ev);
                    database.remove("" + ev.getId());
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            public Runnable init(Event event) {
                this.ev = event;
                return (this);
            }
        }.init(event);

        Delay delay = getDelayForEvent(event, restored);
        if (delay.getDelay() >= 0) {
            if (!restored) {
                database.put("" + event.getId(), event);
            }
            final ScheduledFuture<?> workerHandle
                    = scheduler.schedule(runnable, delay.getDelay(), delay.getUnit());
        }
        /*
        scheduler.schedule(new Runnable() {
            public void run() {
                workerHandle.cancel(true);
            }
        }, 60 * 60, SECONDS);
         */
    }

    private void processDatabase() {
        Iterator it = database.getKeySet().iterator();
        String key;
        while (it.hasNext()) {
            key = (String) it.next();
            handleEvent((Event) database.get(key), true);
        }
    }

    private Delay getDelayForEvent(Event ev, boolean restored) {
        Delay d = new Delay();
        if (restored) {
            d.setUnit(TimeUnit.MILLISECONDS);
            long delay = ev.getCalculatedTimePoint() - System.currentTimeMillis();
            if (delay < MINIMAL_DELAY) {
                delay = MINIMAL_DELAY;
            }
            d.setDelay(delay);
            return d;
        }

        boolean wrongFormat = false;
        String dateDefinition = ev.getTimePoint();
        if (dateDefinition.startsWith("+")) {
            try {
                d.setDelay(Long.parseLong(dateDefinition.substring(1, dateDefinition.length() - 1)));
            } catch (NumberFormatException e) {
                wrongFormat = true;
            }
            String unit = dateDefinition.substring(dateDefinition.length() - 1);
            switch (unit) {
                case "d":
                    d.setUnit(TimeUnit.DAYS);
                    break;
                case "h":
                    d.setUnit(TimeUnit.HOURS);
                    break;
                case "m":
                    d.setUnit(TimeUnit.MINUTES);
                    break;
                case "s":
                    d.setUnit(TimeUnit.SECONDS);
                    break;
                default:
                    wrongFormat = true;
            }
        } else {
            //parse date and replace with delay from now
            d.setUnit(TimeUnit.MILLISECONDS);
            d.setDelay(getDelay(dateDefinition));
        }
        if (wrongFormat) {
            System.out.println("WARNING unsuported delay format: "+dateDefinition);
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
        database.write();
        System.out.println("done");
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the restored
     */
    public boolean isRestored() {
        return restored;
    }

    /**
     * @param restored the restored to set
     */
    public void setRestored(boolean restored) {
        this.restored = restored;
    }

}
