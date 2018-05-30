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
package org.cricketmsf.out.log;

import java.io.IOException;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.out.OutboundAdapter;
import java.util.HashMap;
import org.cricketmsf.Kernel;

/**
 *
 * @author greg
 */
public class FileLogger extends OutboundAdapter implements Adapter, LoggerAdapterIface {

    private String name;
    private String fileLocation;
    private boolean available=false;
    

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
        setName(properties.get("name"));
        Kernel.getInstance().getLogger().print("\tlogger name: " + getName());
        setFileLocation(properties.get("log-file-name"));
        Kernel.getInstance().getLogger().print("\tlog-file-name: " + getFileLocation());
        available=printMe("");
    }

    @Override
    public void log(Event event) {
        StringBuilder sb=new StringBuilder();
        sb.append(Instant.ofEpochMilli(event.getCreatedAt()).toString())
                .append(":")
                .append(event.getId())
                .append(":")
                .append(""+event.getPayload());
        print(sb.toString());
    }

    @Override
    public void print(String message) {
        if(isAvailable()) printMe(message);
    }
    
    private boolean printMe(String message) {
        try {
            Files.write(Paths.get(getFileLocation()), (message + System.lineSeparator()).getBytes(UTF_8),StandardOpenOption.CREATE,StandardOpenOption.APPEND);
            return true;
        } catch (IOException ex) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this, ex.getMessage()));
            return false;
        }
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        if (null != name && !name.isEmpty()) {
            this.name = name;
        } else {
            this.name = this.getClass().getName();
        }
    }

    /**
     * @return the fileLocation
     */
    public String getFileLocation() {
        return fileLocation;
    }

    /**
     * @param fileLocation the fileLocation to set
     */
    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }


}
