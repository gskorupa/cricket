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
package org.cricketmsf.out.file;

import java.io.BufferedInputStream;
import org.cricketmsf.Adapter;
import org.cricketmsf.out.OutboundAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.ParameterMapResult;

/**
 *
 * @author greg
 */
public class FileReaderAdapter extends OutboundAdapter implements Adapter, FileReaderAdapterIface {

    private String rootPath;

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
    public byte[] readFile(File file) throws FileNotFoundException, IOException {
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
            /*
         the above style is a bit tricky: it places bytes into the 'result' array; 
         'result' is an output parameter;
         the while loop usually has a single iteration only.
             */
        } finally {
            input.close();
        }
        return result;
    }

    public ParameterMapResult getFile(RequestObject request) {
        ParameterMapResult result = new ParameterMapResult();
        result.setData(new HashMap(request.parameters));
        String filePath = request.pathExt;
        File f;
        String fileExt = "";

        Kernel.getInstance().handleEvent(Event.logFinest("FileReaderAdapter", "requested filePath=" + filePath));

        try {

            if (filePath.isEmpty() || filePath.endsWith("/")) {
                filePath = filePath.concat("index.html");
            }

            filePath = getRootPath() + filePath;

            f = new File(filePath);
            if (f.isDirectory()) {
                filePath = filePath.concat("/index.html");
                f = new File(filePath);
            }

            if (filePath.lastIndexOf(".") > 0) {
                fileExt = filePath.substring(filePath.lastIndexOf("."));
            }
            
            checkAccess(filePath);
            
            byte[] b = readFile(f);
            result.setPayload(b);
            result.setFileExtension(fileExt);
            result.setCode(HttpAdapter.SC_OK);
            result.setModificationDate(new Date(f.lastModified()));
            result.setMessage("");
        } catch (Exception e) {
            Kernel.getInstance().handleEvent(Event.logWarning("FileReaderAdapter", filePath + " not readable or not found"));
            byte[] emptyContent = {};
            //String content="<HTML><body>ERROR</body></HTML>";
            //result.setPayload(content.getBytes());
            result.setPayload(emptyContent);
            result.setFileExtension(".html");
            result.setCode(HttpAdapter.SC_NOT_FOUND);
            result.setMessage("file not found");
        }
        return result;
    }

    private void checkAccess(String filePath) throws FileNotFoundException {
        if (filePath.indexOf("..") > 0) {
            throw new FileNotFoundException("");
        }
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
