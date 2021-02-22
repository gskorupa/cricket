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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import org.cricketmsf.event.Procedures;
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

    private String fileName;
    private String reschedulingFile;
    private KeyValueStore database;
    private KeyValueStore databaseRs;
    protected boolean restored = false;
    long threadsCounter = 0;
    private String initialTasks;
    private ConcurrentHashMap<String, String> killList;

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
        setFileName(properties.get("file") + ".xml");
        reschedulingFile = properties.get("file") + "-reschedule.xml";
        logger.info("\tfile: " + getFileName());
        logger.info("\tscheduler database file location: " + getFileName());
        initialTasks = properties.getOrDefault("init", "");
        logger.info("\tinit: " + initialTasks);
        properties.put("init", initialTasks);
        database = new KeyValueStore();
        database.setStoragePath(getFileName());
        database.read();
        setRestored(database.getSize() > 0);
        databaseRs = new KeyValueStore();
        databaseRs.setStoragePath(reschedulingFile);
        databaseRs.read();
        processDatabase();
        killList = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        initScheduledTasks();
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
        if (null == event || !event.isValid()) {
            return false;
        }
        try {
            if (event.getExecutionTime() < 0) {
                logger.debug("event.getTimeMillis() < 0. It should not happen. {} {} {}",
                        event.getClass().getSimpleName(),
                        event.getProcedure(),
                        event.getExecutionTime());
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

            final Runnable runnable = new Runnable() {
                Event eventToRun;
                @Override
                public void run() {
                    // reset timepoint to prevent sending this event back from the service
                    eventToRun.setExecutionTime(-1);
                    // wait until Kernel finishes initialization process
                    while (!Kernel.getInstance().isStarted()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                        }
                    }
                    // execute target handler
                    if (!killList.containsKey("" + eventToRun.getId())) {
                        Kernel.getInstance().dispatchEvent(eventToRun);
                    }
                    threadsCounter--;
                    // remove event from persistance database
                    database.remove(eventToRun.getId());
                    if (eventToRun.isCyclic()) {
                        if (databaseRs.containsKey(eventToRun.getProcedure() + "@" + eventToRun.getClass().getName())) {
                            eventToRun.setEventDelay((Long) databaseRs.get("" + eventToRun.getProcedure()));
                        }
                        eventToRun.reschedule();
                        handleEvent(eventToRun);
                    }
                }
                public Runnable init(Event event) {
                    this.eventToRun = event;
                    return (this);
                }
            }.init(event);

            Delay delay = event.getDelay();
            if (delay.getDelay() >= 0) {
                if (systemStart) {
                    logger.info("event " + event.getProcedure() + " will start in " + (delay.getDelay() / 1000) + " seconds");
                }
                if (!(restored || event.isFromInit())) {
                    // save event to persistance database
                    database.put(event.getId(), event);
                }
                threadsCounter++;
                final ScheduledFuture<?> workerHandle = scheduler.schedule(runnable, delay.getDelay(), delay.getUnit());
            }
            return true;
        } catch (Exception e) {
            System.out.println("EXCEPTION " + e.getMessage());
            e.printStackTrace();
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
        if (initialTasks == null || initialTasks.isEmpty()) {
            return;
        }
        String[] params;
        String[] tasks;
        String className;
        int procedureNumber;
        String timeDefinition;
        String data;
        Event event;
        tasks = initialTasks.split(";");
        for (String task : tasks) {
            params = task.split(",");
            if (params.length < 2) {
                logger.warn("insufficient event definition {}", task);
                continue;
            }
            procedureNumber = Procedures.DEFAULT;
            className = null;
            if (params[0].contains(".")) {
                //event class name is provided
                className = params[0];
                try {
                    procedureNumber = Integer.parseInt(params[1]);
                } catch (NumberFormatException ex) {
                }
                timeDefinition = params[2];
                if (params.length > 3) {
                    data = params[3];
                } else {
                    data = null;
                }
            } else {
                try {
                    procedureNumber = Integer.parseInt(params[0]);
                } catch (NumberFormatException ex) {
                }
                timeDefinition = params[1];
                if (params.length > 2) {
                    data = params[2];
                } else {
                    data = null;
                }
            }
            if (null != className) {
                Class cls;
                try {
                    cls = Class.forName(className);
                    event = (Event) cls.getConstructor().newInstance();
                } catch (NoSuchMethodException | InvocationTargetException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                    logger.warn(ex.getMessage());
                    continue;
                }
            } else {
                event = new Event();
            }
            event.setProcedure(procedureNumber);
            event.setFromInit(true);
            event.calculateExecutionTime(timeDefinition);
            event.setData(data);
            event.setOrigin(this.getClass());
            handleEvent(event);
        }
    }

    @Override
    public void dispatch(Event event) throws DispatcherException {
        if (event.getExecutionTime() < 0) {
            Kernel.getInstance().handleEvent(event);
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
    public void reschedule(String className, int procedure, Long newDelay
    ) {
        databaseRs.put(procedure + "@" + className, newDelay);
    }
}
