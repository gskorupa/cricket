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

import java.lang.reflect.InvocationTargetException;
import org.cricketmsf.Adapter;
import org.cricketmsf.event.Event;
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
import org.cricketmsf.exception.DispatcherException;
import org.cricketmsf.out.dispatcher.DispatcherIface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author greg
 */
public class Scheduler extends InboundAdapter implements SchedulerIface, DispatcherIface, Adapter {

    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    //private String storagePath;
    private String fileName;
    private String reshedulingFile;
    private KeyValueStore database;
    private KeyValueStore databaseRs;
    protected boolean restored = false;
    long threadsCounter = 0;
    private String initialTasks;
    private ConcurrentHashMap<String, String> killList;

    private long MINIMAL_DELAY = 1000;

    private ThreadFactory factory = Kernel.getInstance().getThreadFactory();
    public final ScheduledExecutorService scheduler
            = Executors.newScheduledThreadPool(10, factory);

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
        //setStoragePath(properties.get("path"));
        //logger.info("\tpath: " + getStoragePath());
        // fix to handle '.'
        //TODO: ?
        //if (getStoragePath().startsWith(".")) {
        //    setStoragePath(System.getProperty("user.dir") + getStoragePath().substring(1));
        //}
        setFileName(properties.get("file") + ".xml");
        reshedulingFile = properties.get("file") + "-reschedule.xml";
        logger.info("\tfile: " + getFileName());
        /*String pathSeparator = System.getProperty("file.separator");
        setStoragePath(
                getStoragePath().endsWith(pathSeparator)
                ? getStoragePath() + getFileName()
                : getStoragePath() + pathSeparator + getFileName()
        );
         */
        logger.info("\tscheduler database file location: " + getFileName());

        initialTasks = properties.getOrDefault("init", "");
        logger.info("\tinit: " + initialTasks);

        properties.put("init", initialTasks);

        database = new KeyValueStore();
        database.setStoragePath(getFileName());
        database.read();
        setRestored(database.getSize() > 0);
        databaseRs = new KeyValueStore();
        databaseRs.setStoragePath(reshedulingFile);
        databaseRs.read();
        processDatabase();
        killList = new ConcurrentHashMap<>();

    }

    @Override
    public void run() {
        initScheduledTasks();
    }

    /*private void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }
    

    private String getStoragePath() {
        return storagePath;
    }
     */
    @Override
    public boolean handleEvent(Event event) {
        return handleEvent(event, false, false);
    }

    public boolean handleEvent(Event event, boolean restored) {
        return handleEvent(event, restored, false);
    }

    @Override
    public boolean handleEvent(Event event, boolean restored, boolean systemStart) {
        try {
            if (event.getTimePoint() == null) {
                logger.debug("event.getTimePoint() is null. It should not happen. {} {} {} {}",
                        event.getClass().getSimpleName(),
                        event.getProcedure(),
                        event.getTimePoint(),
                        event.getInitialTimePoint());
                return false;
            }
            if (systemStart) {
                String oldCopy = "";
                //when events initialized on the service start, we need to create new instances of these events
                if (database.containsKey(event.getId())) {
                    oldCopy = "" + ((Event) database.get(event.getId())).getId();
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
                    ev.setInitialTimePoint(remembered);
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
                    database.remove(ev.getId());
                    try {
                        if (ev.isCyclic()) {
                            //if timePoint has form dateformatted|*cyclicdelay
                            int pos = remembered.indexOf("|*");
                            if (pos > 0) {
                                remembered = remembered.substring(pos + 1);
                            }
                            if (databaseRs.containsKey(""+ev.getProcedure())) {
                                ev.setTimePoint((String) databaseRs.get(""+ev.getProcedure()));
                            } else {
                                ev.setTimePoint(remembered);
                            }
                            ev.reschedule();
                            handleEvent(ev);
                        }
                    } catch (Exception e) {
                        logger.warn("malformed event time definition - unable to reschedule");
                    }
                }

                public Runnable init(Event event) {
                    this.ev = event;
                    return (this);
                }
            }.init(event);

            Delay delay = getDelayForEvent(event, restored);
            if (delay.getDelay() >= 0) {
                if (systemStart) {
                    logger.info("event " + event.getProcedure() + " will start in " + (delay.getDelay() / 1000) + " seconds");
                }
                if (!(restored || event.isFromInit())) {
                    database.put(event.getId(), event);
                }
                threadsCounter++;
                final ScheduledFuture<?> workerHandle = scheduler.schedule(runnable, delay.getDelay(), delay.getUnit());
            }
            return true;
        } catch (Exception e) {
            System.out.println("EXCEPTION " + e.getMessage());
            return false;
        }
    }

    private void processDatabase() {
        Iterator it = database.getKeySet().iterator();
        Long key;
        while (it.hasNext()) {
            key = (Long) it.next();
            //TODO: check this
            //restore only events without name == not these created using 
            //scheduler properties
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
            logger.info("WARNING unsuported delay format: " + dateDefinition);
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
                logger.warn(ex.getMessage());
                return -1;
            }
        }
        return result;
    }

    @Override
    public void destroy() {
        logger.info("Stopping scheduler ... ");
        List<Runnable> activeEvents = scheduler.shutdownNow();
        database.write();
        databaseRs.write();
        logger.info("done");
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

    private String getThreadsInfo() {
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

    @Override
    public void initScheduledTasks() {
        String[] params;
        String[] tasks;
        String firstParam;
        if (initialTasks != null && !initialTasks.isEmpty()) {
            tasks = initialTasks.split(";");
            for (String task : tasks) {
                params = task.split(",");
                firstParam = params[0];
                if (firstParam.contains(".")) {
                    Class cls;
                    try {
                        cls = Class.forName(firstParam);
                        Event event = (Event) cls.getConstructor().newInstance();
                        event.setProcedure(Integer.parseInt(params[1]));
                        event.setTimePoint(params[2]);
                        if (params.length > 3) {
                            event.setData(params[3]);
                        }
                        event.setFromInit(true);
                        event.setOrigin(this.getClass());
                    } catch (NoSuchMethodException | InvocationTargetException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                        logger.warn(ex.getMessage());
                    }
                } else {
                    handleEvent(
                            new Event(
                                    Integer.parseInt(firstParam), //name
                                    params[1], //timePoint
                                    params.length > 2 ? params[2] : null, //data
                                    true,
                                    this.getClass()
                            ));
                }
            }
        }
    }

    @Override
    public void dispatch(Event event) throws DispatcherException {
        if (event.getTimePoint() == null) {
            Kernel.getInstance().getEventProcessingResult(event);
        } else {
            handleEvent(event);
        }
    }

    @Override
    public DispatcherIface getDispatcher() {
        return this;
    }

    @Override
    public void registerEventTypes(String categories) throws DispatcherException {
    }

    @Override
    public void reschedule(String processName, String newTimepoint) {
        databaseRs.put(processName, newTimepoint);
    }
}
