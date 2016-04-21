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
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.ParameterMapResult;

/**
 *
 * @author greg
 */
public class HtmlReaderAdapter extends OutboundAdapter implements Adapter, HtmlReaderAdapterIface {

    private String rootPath;

    /**
     * This method is executed while adapter is instantiated during the service start.
     * It's used to configure the adapter according to the configuration.
     * 
     * @param properties    map of properties readed from the configuration file
     * @param adapterName   name of the adapter set in the configuration file (can be different
     *  from the interface and class name.
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
    
    public ParameterMapResult getFile(RequestObject request){
        ParameterMapResult result= new ParameterMapResult();
        result.setData(new HashMap(request.parameters));
        byte[] emptyContent = {};
        String filePath = request.pathExt;
        Kernel.getInstance().handleEvent(Event.logFinest("HtmlReaderAdapter", "filePath="+filePath));
        String fileExt = "";
        if (!(filePath.isEmpty() || filePath.endsWith("/")) && filePath.indexOf(".") > 0) {
            fileExt = filePath.substring(filePath.lastIndexOf("."));
        }
        switch (fileExt.toLowerCase()) {
            case ".ico":
            case ".jpg":
            case ".jpeg":
            case ".gif":
            case ".png":
                break;
            default:
                fileExt = ".html";
        }
        try {
            byte[] b = readFile(filePath);
            result.setPayload(b);
            result.setFileExtension(fileExt);
            result.setCode(HttpAdapter.SC_OK);
            result.setMessage("");
        } catch (Exception e) {
            Kernel.getInstance().handleEvent(Event.logWarning("HtmlReaderAdapter", e.getMessage()));
            result.setPayload(emptyContent);
            result.setFileExtension(fileExt);
            result.setCode(HttpAdapter.SC_NOT_FOUND);
            result.setMessage("file not found");
        }
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
