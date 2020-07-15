package org.cricketmsf.in.websocket;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author greg
 */
public class ClientList extends ArrayList<WebsocketAdapter> {
    
    HashMap properties;
    
    public ClientList(HashMap properties){
        this.properties=properties;
    }
    
}
