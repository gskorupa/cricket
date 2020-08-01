/*
 * Copyright 2016 Grzegorz Skorupa <g.skorupa at gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cricketmsf.in.scheduler;

import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.db.KeyValueStore;
import org.cricketmsf.in.InboundAdapter;
import org.cricketmsf.event.Delay;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.cricketmsf.event.EventDecorator;
import org.cricketmsf.exception.DispatcherException;
import org.cricketmsf.out.dispatcher.DispatcherIface;


/**
 *
 * @author greg
 */
public class Scheduler extends InboundAdapter implements SchedulerIface, DispatcherIface, Adapter {

    private String storagePath;
    private String envVariable;
    private String fileName;
    private KeyValueStore database;
    protected boolean restored = false;
    long threadsCounter = 0;
    private String initialTasks;
    private ConcurrentHashMap<String, String> killList;

    private long MINIMAL_DELAY = 1000;

    private ThreadFactory factory = Kernel.getInstance().getThreadFactory();
    public final ScheduledExecutorService scheduler
            = Executors.newScheduledThreadPool(10,factory);
    
    /**
     * This method is executed while adapter is instantiated during the service
     * start. It's used to configure the adapter according to the configuration.
     *
     * @param properties map of properties readed from the configuration file
     * @param adapterName name of the adapter set in the configuration file (can
     * be different from the interface and class name.
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        setStoragePath(properties.get("path"));
        Kernel.getInstance().getLogger().print("\tpath: " + getStoragePath());
        setEnvVariable(properties.get("envVariable"));
        Kernel.getInstance().getLogger().print("\tenvVAriable name: " + getEnvVariable());
        if (System.getenv(getEnvVariable()) != null) {
            setStoragePath(System.getenv(getEnvVariable()));
        }
        // fix to handle '.'
        if (getStoragePath().startsWith(".")) {
            setStoragePath(System.getProperty("user.dir") + getStoragePath().substring(1));
        }
        setFileName(properties.get("file"));
        Kernel.getInstance().getLogger().print("\tfile: " + getFileName());
        String pathSeparator = System.getProperty("file.separator");
        setStoragePath(
                getStoragePath().endsWith(pathSeparator)
                ? getStoragePath() + getFileName()
                : getStoragePath() + pathSeparator + getFileName()
        );
        Kernel.getInstance().getLogger().print("\tscheduler database file location: " + getStoragePath());

        initialTasks = properties.getOrDefault("init", "");
        Kernel.getInstance().getLogger().print("\tinit: " + initialTasks);

        properties.put("init", initialTasks);

        database = new KeyValueStore();
        database.setStoragePath(getStoragePath());
        database.read();
        setRestored(database.getSize() > 0);
        processDatabase();
        killList = new ConcurrentHashMap<>();

    }
    
    @Override
    public void run(){
        initScheduledTasks();
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

    @Override
    public boolean handleEvent(Event event) {
        return handleEvent(event, false, false);
    }

    public boolean handleEvent(Event event, boolean restored) {
        return handleEvent(event, restored, false);
    }

    @Override
    public boolean handleEvent(Event event, boolean restored, boolean systemStart) {
        try{
        if (event.getTimePoint() == null) {
            return false;
        }
        if (systemStart) {
            String oldCopy = "";
            //when events initialized on the service start, we need to create new instances of these events
            if (event.getName() != null && !event.getName().isEmpty()) {
                if (database.containsKey(event.getName())) {
                    oldCopy = ((Event) database.get(event.getName())).getId() + "";
                }
            } else {
                if (database.containsKey("" + event.getId())) {
                    oldCopy = ((Event) database.get("" + event.getId())).getId() + "";
                }
            }
            if (!oldCopy.isEmpty()) {
                killList.put(oldCopy, oldCopy);
            }
        }

        final Runnable runnable;
        runnable = new Runnable() {
            Event ev;

            @Override
            public void run() {
                // we should reset timepoint to prevent sending this event back from the service
                String remembered = ev.getTimePoint();
                ev.setTimePoint(null);
                // we should wait until Kernel finishes initialization process
                while (!Kernel.getInstance().isStarted()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                    }
                }

                if (!killList.containsKey("" + ev.getId())) {
                    Kernel.getInstance().dispatchEvent(ev);
                }

                threadsCounter--;
                database.remove("" + ev.getId());
                try {
                    if (ev.isCyclic()) {
                        //if timePoint has form dateformatted|*cyclicdelay
                        int pos = remembered.indexOf("|*");
                        if (pos > 0) {
                            remembered = remembered.substring(pos + 1);
                        }
                        ev.setTimePoint(remembered);
                        ev.reschedule();
                        handleEvent(ev);
                    }
                } catch (Exception e) {
                    Kernel.getLogger().log(Event.logWarning(this, "malformed event time definition - unable to reschedule"));
                }
            }

            public Runnable init(Event event) {
                this.ev = event;
                return (this);
            }
        }.init(event);

        Delay delay = getDelayForEvent(event, restored);
        if (delay.getDelay() >= 0) {
            if(systemStart){
                Kernel.getLogger().log(Event.logInfo(this, "event " + event.getName() + " will start in " + (delay.getDelay() / 1000) + " seconds"));
            }
            if (!restored) {
                if (event.getName() != null && !event.getName().isEmpty()) {
                    database.put(event.getName(), event);
                } else {
                    database.put("" + event.getId(), event);
                }
            }
            threadsCounter++;
            final ScheduledFuture<?> workerHandle  = scheduler.schedule(runnable, delay.getDelay(), delay.getUnit());
        }
        return true;
        }catch(Exception e){
            System.out.println("EXCEPTION "+e.getMessage());
            return false;
        }
    }

    private void processDatabase() {
        Iterator it = database.getKeySet().iterator();
        String key;
        while (it.hasNext()) {
            key = (String) it.next();
            //restore only events without name == not these created using 
            //scheduler properties
            if (((Event) database.get(key)).getName() == null) {
                handleEvent((Event) database.get(key), true);
            }
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
        if (dateDefinition.startsWith("+") || dateDefinition.startsWith("*")) {
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
            Kernel.getLogger().print("WARNING unsuported delay format: " + dateDefinition);
            return null;
        }
        return d;
    }

    private long getDelay(String dateStr) {
        long result;
        Date target;
        String dateStrNoRepeat;
        int pos = dateStr.indexOf("|");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss Z");
        if (pos > 0) {
            dateStrNoRepeat = dateStr.substring(0, pos);
        } else {
            dateStrNoRepeat = dateStr;
        }
        try {
            target = dateFormat.parse(dateStrNoRepeat);
            result = target.getTime() - System.currentTimeMillis();
        } catch (ParseException e) {
            try {
                String today = java.time.LocalDate.now(ZoneId.of("UTC")).toString();
                //String today = new SimpleDateFormat("yyy.MM.dd ").format(new Date());
                target = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").parse(today + " " + dateStrNoRepeat);
                result = target.getTime() - System.currentTimeMillis();
                if (result < 0) {
                    result = result + 24 * 60 * 60 * 1000;
                }
            } catch (ParseException ex) {
                Kernel.getLogger().log(Event.logWarning(this, ex.getMessage()));
                return -1;
            }
        }
        return result;
    }

    @Override
    public void destroy() {
        Kernel.getInstance().getLogger().print("Stopping scheduler ... ");
        List<Runnable> activeEvents = scheduler.shutdownNow();
        database.write();
        Kernel.getInstance().getLogger().print("done");
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
    @Override
    public boolean isRestored() {
        return restored;
    }

    /**
     * @param restored the restored to set
     */
    public void setRestored(boolean restored) {
        this.restored = restored;
    }

    @Override
    public long getThreadsCount() {
        return threadsCounter;
    }
    
    private String getThreadsInfo(){
        return "";
    }

    @Override
    public Map<String, Object> getStatus(String name) {
        Map m = super.getStatus(name);
        m.put("threads", "" + getThreadsCount());
        return m;
    }

    @Override
    public boolean isScheduled(String eventID) {
        return database.containsKey(eventID);
    }

    public void initScheduledTasks() {
        String[] params;
        String[] tasks;
        if (initialTasks != null && !initialTasks.isEmpty()) {
            tasks = initialTasks.split(";");
            for (String task : tasks) {
                params = task.split(",");
                if (params.length == 6) {
                    handleEvent(
                            new Event(
                                    params[1], //origin
                                    params[2], //category
                                    params[3], //type
                                    params[4], //timePoint
                                    params[5]  //payload
                            ).putName(params[0]), 
                            false, 
                            true
                    );
                }
            }
        }
    }

    @Override
    public void dispatch(Event event) throws DispatcherException {
        if(event.getTimePoint()==null){
            Kernel.getInstance().getEventProcessingResult(event);
        }else{
            handleEvent(event);
        }
    }
    
    @Override
    public void dispatch(EventDecorator event) throws DispatcherException {
        if(event.getTimePoint()==null){
            Kernel.getInstance().getEventProcessingResult(event);
        }else{
            handleEvent(event);
        }
    }
    
    @Override
    public DispatcherIface getDispatcher(){
        return this;
    }

    @Override
    public void registerEventTypes(String categories) throws DispatcherException {
    }
}
