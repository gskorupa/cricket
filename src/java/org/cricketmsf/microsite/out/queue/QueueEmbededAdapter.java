/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.microsite.out.queue;

import java.util.HashMap;
import java.util.List;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.microsite.out.user.UserException;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.KeyValueDBIface;

/**
 *
 * @author greg
 */
public class QueueEmbededAdapter extends OutboundAdapter implements Adapter, QueueAdapterIface {

    private String helperAdapterName;
    private KeyValueDBIface database = null;
    private boolean initialized = false;
    private int maxQueueSize = 1000;
    private boolean defaultPersistency = false;
    private String categoriesToHandle = null;
    private boolean handleAll = false;
    private HashMap handled;
    private String categoriesToIgnore = null;
    private HashMap ignored;

    //TODO: configured max queue size
    //TODO: configured queue persistency 
    //TODO: removing paths/queues ?
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        helperAdapterName = properties.get("helper-name");
        Kernel.getInstance().getLogger().print("\thelper-name: " + helperAdapterName);
        categoriesToHandle = properties.get("categories");
        Kernel.getInstance().getLogger().print("\tcategories: " + categoriesToHandle);
        categoriesToIgnore = properties.get("ignoring");
        Kernel.getInstance().getLogger().print("\tignoring: " + categoriesToIgnore);
        try {
            init(helperAdapterName, categoriesToHandle, categoriesToIgnore);
        } catch (QueueException e) {
            e.printStackTrace();
            Kernel.handle(Event.logSevere(this.getClass().getSimpleName(), e.getMessage()));
        }
    }

    @Override
    public void init(String helperName, String categories, String ignoring) throws QueueException {
        try {
            database = (KeyValueDBIface) Kernel.getInstance().getAdaptersMap().get(helperName);
            initialized = true;
        } catch (Exception e) {
            throw new QueueException(UserException.HELPER_NOT_AVAILABLE, "helper adapter not available");
        }
        handled = new HashMap();
        if (categories != null) {
            if ("*".equals(categories)) {
                handleAll = true;
            } else {
                String[] tmp = categories.split(",");
                for (int i = 0; i < tmp.length; i++) {
                    handled.put(tmp[i], tmp[i]);
                }
            }
        }
        ignored = new HashMap();
        if (ignoring != null) {
            String[] tmp = ignoring.split(",");
            for (int i = 0; i < tmp.length; i++) {
                ignored.put(tmp[i], tmp[i]);
            }
        }
    }

    @Override
    public List<Event> get(String path) throws QueueException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void send(String path, Event event) throws QueueException {
        // we can store event object without serializing
        // for other implementations an event serialization/deserialization shuld be implemented
        try {
            database.put(path, "" + event.getId(), event);
        } catch (KeyValueDBException e) {
            if (e.getCode() == KeyValueDBException.TABLE_NOT_EXISTS) {
                try {
                    database.addTable(path, maxQueueSize, defaultPersistency);
                    database.put(path, "" + event.getId(), event);
                } catch (KeyValueDBException e2) {
                    throw new QueueException(QueueException.UNKNOWN, e.getMessage());
                }
            }
        }
    }

    @Override
    public boolean isHandling(String eventCategoryName) {
        return handled.containsKey(eventCategoryName) || (handleAll && !ignored.containsKey(this));
    }

    @Override
    public void send(Event event) throws QueueException {
        send("/event/" + event.getCategory(), event);
    }

}
