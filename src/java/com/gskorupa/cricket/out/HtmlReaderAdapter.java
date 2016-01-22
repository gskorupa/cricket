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
package com.gskorupa.cricket.out;

import com.gskorupa.cricket.Adapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 *
 * @author greg
 */
public class HtmlReaderAdapter extends OutboundAdapter implements Adapter, HtmlReaderAdapterIface {

    private String rootPath;

    /**
     * Configures the adapter
     * @param properties 
     */
    @Override
    public void loadProperties(HashMap<String,String> properties) {
        setRootPath(properties.get("root"));
        System.out.println("root path: " + getRootPath());
    }

    /**
     * Reads the file content 
     * @param path the file location (prepended with the rootPath)
     * @return file content
     * @throws FileNotFoundException
     * @throws IOException 
     */
    @Override
    public String readFile(String path) throws FileNotFoundException, IOException {
        String fileContent;
        String filePath = getRootPath() + path;
        if (!filePath.endsWith(".html")) {
            if (filePath.endsWith("/")) {
                filePath = filePath + "index.html";
            } else {
                filePath = filePath + "/index.html";
            }
        }
        StringBuilder sb = new StringBuilder();
        InputStream fis = new FileInputStream(new File(filePath));
        try (BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        fileContent = sb.toString();
        return fileContent;
    }

    /**
     * Sets the root path
     * 
     * @param rootPath 
     */
    private void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    /**
     * The root path is prepended to the file path while reading file content
     * 
     * @return root path
     */
    private String getRootPath() {
        return rootPath;
    }
}
