/*
 * Copyright 2020 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.out.autostart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.exception.InitException;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.OutboundAdapterIface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Autostart extends OutboundAdapter implements OutboundAdapterIface, Adapter, AutostartIface {
    private static final Logger logger = LoggerFactory.getLogger(Autostart.class);
    
    HashMap<String, String> properties;
    String[] paths;
    
    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }
    
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        String pathNames = properties.getOrDefault("subfolders", "");
        logger.info("\tsubfolders: " + pathNames);
        paths = pathNames.split(":");
    }
    
    @Override
    public void execute() throws InitException {
        Path path;
        for (int i = 0; i < paths.length; i++) {
            path = Paths.get(paths[i]);
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    throw new InitException(1, "unable to create directory: "+paths[i]);
                }
            } else {
                if (!Files.isDirectory(path)) {
                    throw new InitException(1, "not a directory: "+paths[i]);
                } else {
                    if (!Files.isWritable(path)) {
                        throw new InitException(1, "read-only directory: "+paths[i]);
                    }
                }
            }
        }
    }
}
