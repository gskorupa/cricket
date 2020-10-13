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
import org.cricketmsf.event.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.in.InboundAdapter;
import org.cricketmsf.in.http.HttpPortedAdapter;
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
        //super.getServiceHooks(adapterName);
        setFile(properties.getOrDefault("path", ""));
        logger.info("\tpath=" + fileName);
        setSamplingInterval(properties.getOrDefault("sampling-interval", "1000"));
        logger.info("\tsampling-interval=" + samplingInterval);
        procedureName = properties.getOrDefault("procedure-name", "DATA_READY");
        logger.info("\tprocedure-name =" + procedureName);
        super.registerEventCategory(procedureName, Event.class.getName());
    }

    @Override
    public void checkStatus() {
        if (file != null) {
            byte[] content = readFile();
            logger.debug("reading " + fileName);
            if (content.length > 0) {
                //handle(categoryName, new String(content));
                FileEvent ev = new FileEvent(content);
                Kernel.getInstance().dispatchEvent(ev);
            }
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                checkStatus();
                Thread.sleep(samplingInterval);
                //Thread.yield();
            }
        } catch (InterruptedException e) {
            logger.info("interrupted");
        }
    }

    /**
     * @param samplingInterval the samplingInterval to set
     */
    public void setSamplingInterval(String samplingInterval) {
        try {
            this.samplingInterval = Integer.parseInt(samplingInterval);
        } catch (NumberFormatException e) {
            logger.info(e.getMessage());
        }
    }

    /**
     * @param folderName the folderName to set
     */
    public void setFile(String folderName) {
        this.fileName = folderName;
        file = new File(folderName);
        try {
            if (!file.exists()) {
                file = null;
                logger.info("file not found");
            } else if (file.isDirectory()) {
                logger.info("directory found");
                file = null;
            }
        } catch (SecurityException e) {
            logger.info(e.getMessage());
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

}
