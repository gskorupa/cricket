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
package org.cricketmsf.in.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.Kernel;
import org.cricketmsf.in.InboundAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author greg
 */
public class FileReader extends InboundAdapter implements Adapter, WatchdogIface {

    private static final Logger logger = LoggerFactory.getLogger(FileReader.class);
    private String procedureName = "DATA_READY";

    private String fileName;
    File file;
    private int samplingInterval = 1000;
    private long lastModified = 0;
    private boolean running = false;

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
        setFile(properties.getOrDefault("path", ""));
        logger.info("\tpath: " + fileName);
        setSamplingInterval(properties.getOrDefault("sampling-interval", "1000"));
        logger.info("\tsampling-interval: " + samplingInterval);
        procedureName = properties.getOrDefault("procedure-name", "dataReady");
        logger.info("\tprocedure-name: " + procedureName);
        super.registerEventCategory(procedureName, FileEvent.class.getName());
        running=true;
    }
    
    private File getFile(){
        if(null==file){
            setFile(fileName);
        }
        return file;
    }

    @Override
    public void checkStatus() {
        if (getFile() != null) {
            long modified=file.lastModified();
            if (modified > lastModified) {
                byte[] content = readFile();
                lastModified=modified;
                logger.debug("reading " + fileName);
                if (content.length > 0) {
                    FileEvent ev = new FileEvent(content);
                    ev.setProcedureName(procedureName);
                    Kernel.getInstance().dispatchEvent(ev);
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            while (running) {
                checkStatus();
                Thread.sleep(samplingInterval);
            }
        } catch (InterruptedException e) {
            logger.warn("interrupted");
        }
    }

    /**
     * @param samplingInterval the samplingInterval to set
     */
    public void setSamplingInterval(String samplingInterval) {
        try {
            this.samplingInterval = Integer.parseInt(samplingInterval);
        } catch (NumberFormatException e) {
            logger.warn(e.getMessage());
        }
    }

    /**
     * @param fileName the folderName to set
     */
    public void setFile(String fileName) {
        this.fileName = fileName;
        file = new File(fileName);
        try {
            if (!file.exists()) {
                file = null;
                logger.warn("file not found");
            } else if (file.isDirectory()) {
                logger.warn("directory found");
                file = null;
            }
        } catch (SecurityException e) {
            logger.warn(e.getMessage());
        }
    }

    /**
     * Reads the file content
     *
     * @param filePath file path
     * @return file content
     */
    private byte[] readFile() {
        //File file = new File(filePath);
        byte[] result = new byte[(int) file.length()];
        InputStream input = null;
        try {
            int totalBytesRead = 0;
            input = new BufferedInputStream(new FileInputStream(file));
            while (totalBytesRead < result.length) {
                int bytesRemaining = result.length - totalBytesRead;
                //input.read() returns -1, 0, or more :
                int bytesRead = input.read(result, totalBytesRead, bytesRemaining);
                if (bytesRead > 0) {
                    totalBytesRead = totalBytesRead + bytesRead;
                }
            }
        } catch (IOException e) {
            result = new byte[0];
            return result;
        } finally {
            try {
                input.close();
            } catch (IOException e) {
            }
        }
        return result;
    }
    
    @Override
    public void destroy() {
        shutdown();
    }
    
    @Override
    public void shutdown(){
        running=false;
    }
}
