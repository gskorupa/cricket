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
package org.cricketmsf.out.html;

import org.cricketmsf.Adapter;
import org.cricketmsf.out.OutboundAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author greg
 */
public class HtmlReaderAdapter extends OutboundAdapter implements Adapter, HtmlReaderAdapterIface {

    private String rootPath;

    /**
     * Configures the adapter
     *
     * @param properties
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        setRootPath(properties.get("root"));
        System.out.println("root path: " + getRootPath());
    }

    /**
     * Reads the file content
     *
     * @param filePath the file location (prepended with the rootPath)
     * @return file content
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Override
    public byte[] readFile(String filePath) throws FileNotFoundException, IOException {
        String path=getRootPath()+filePath;
        if(path.endsWith("/")){
            path=path+"index.html";
        }
        byte[] result = {};
        FileInputStream fileInputStream = null;
        File file = new File(path);
        result = new byte[(int) file.length()];
        fileInputStream = new FileInputStream(file);
        fileInputStream.read(result);
        fileInputStream.close();
        return result;
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
