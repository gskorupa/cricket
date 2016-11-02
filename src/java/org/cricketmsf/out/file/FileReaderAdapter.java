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
import static org.cricketmsf.Kernel.handle;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.ParameterMapResult;
import org.cricketmsf.in.http.Result;
import org.cricketmsf.out.db.KeyValueCacheAdapterIface;

/**
 *
 * @author greg
 */
public class FileReaderAdapter extends OutboundAdapter implements Adapter, FileReaderAdapterIface {

    private String rootPath;
    private String indexFileName;

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
        System.out.println("\troot path: " + getRootPath());
        indexFileName=properties.getOrDefault("index-file", "index.html");
        System.out.println("\tindex-file: " + indexFileName);
    }

    /**
     * Reads the file content
     * @param file file object to read from
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

/**
     * Reads the file content
     * @param filePath file path
     * @return file content
     */
    @Override
    public byte[] readFile(String filePath) {
        File file = new File(filePath);
        byte[] result = new byte[(int) file.length()];
        InputStream input=null;
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
        }catch(IOException e){
            result = new byte[0];
            return result;
        }
        finally {
            try{
            input.close();
            }catch(IOException e){}
        }
        return result;
    }

    @Override
    public String getFilePath(RequestObject request) {
        String filePath = request.pathExt;
        if (filePath.isEmpty() || filePath.endsWith("/")) {
            filePath = filePath.concat(indexFileName);
        }
        filePath = getRootPath() + filePath;
        System.out.println("FILEPATH="+filePath);
        return filePath;
    }

    @Override
    public String getFileExt(String filePath) {
        if (filePath.lastIndexOf(".") > 0) {
            return filePath.substring(filePath.lastIndexOf("."));
        } else {
            return "";
        }
    }

    @Override
    public byte[] getFileBytes(File file, String filePath){
        try {
            checkAccess(filePath);
            byte[] b = readFile(file);
            return b;
        } catch (Exception e) {
            Kernel.getInstance().handleEvent(Event.logWarning("FileReaderAdapter(1)", filePath + " not readable or not found"));
            byte[] emptyContent = {};
            return emptyContent;
        }
    }
    
    /*
    @Override
    public ParameterMapResult getFile(String filePath, HashMap parameters) {
        ParameterMapResult result = new ParameterMapResult();
        result.setData(parameters);
        File f;
        String fileExt;

        Kernel.getInstance().handleEvent(Event.logFinest("FileReaderAdapter", "requested filePath=" + filePath));
        try {
            f = new File(filePath);
            fileExt = getFileExt(filePath);

            checkAccess(filePath);

            byte[] b = readFile(f);
            result.setPayload(b);
            result.setFileExtension(fileExt);
            result.setCode(HttpAdapter.SC_OK);
            result.setModificationDate(new Date(f.lastModified()));
            result.setMessage("");
        } catch (Exception e) {
            Kernel.getInstance().handleEvent(Event.logWarning("FileReaderAdapter(2)", filePath + " not readable or not found"));
            byte[] emptyContent = {};
            result.setPayload(emptyContent);
            result.setFileExtension(".html");
            result.setCode(HttpAdapter.SC_NOT_FOUND);
            result.setMessage("file not found");
        }
        return result;
    }

    @Override
    public ParameterMapResult getFile(RequestObject request) {
        ParameterMapResult result = new ParameterMapResult();
        result.setData(new HashMap(request.parameters));
        String filePath = request.pathExt;
        File f;
        String fileExt = "";

        Kernel.getInstance().handleEvent(Event.logFinest("FileReaderAdapter", "requested filePath=" + filePath));

        try {

            if (filePath.isEmpty() || filePath.endsWith("/")) {
                filePath = filePath.concat(indexFileName);
            }

            filePath = getRootPath() + filePath;

            f = new File(filePath);
            if (f.isDirectory()) {
                //filePath = filePath.concat("/index.html");
                filePath = filePath.concat(indexFileName);
            }

            f = new File(filePath);

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
            Kernel.getInstance().handleEvent(Event.logWarning("FileReaderAdapter(3)", filePath + " not readable or not found"));
            byte[] emptyContent = {};
            result.setPayload(emptyContent);
            result.setFileExtension(".html");
            result.setCode(HttpAdapter.SC_NOT_FOUND);
            result.setMessage("file not found");
        }
        return result;
    }
*/
    
    public Result getFile(RequestObject request, KeyValueCacheAdapterIface cache){
        String filePath = getFilePath(request);
        byte[] content;
        ParameterMapResult result = new ParameterMapResult();
        result.setData(request.parameters);

        // we can use cache if available
        FileObject fo = null;
        boolean fileReady = false;
        if (cache!=null) {
            try {
                fo = (FileObject) cache.get(filePath);
                if (fo != null) {
                    fileReady = true;
                    result.setCode(HttpAdapter.SC_OK);
                    result.setMessage("");
                    result.setPayload(fo.content);
                    result.setFileExtension(fo.fileExtension);
                    result.setModificationDate(fo.modified);
                    handle(Event.logFine(this.getClass().getSimpleName(), "read from cache"));
                    return result;
                }
            } catch (ClassCastException e) {
            }
        }
        // if not in cache
        if (!fileReady) {
            File file = new File(filePath);
            content = getFileBytes(file, filePath);
            if (content.length == 0) {
                // file not found or empty file
                result.setCode(HttpAdapter.SC_NOT_FOUND);
                result.setMessage("file not found");
                
                result.setPayload("file not found".getBytes());
                return result;
            }
            fo = new FileObject();
            fo.content = content;
            fo.modified = new Date(file.lastModified());
            fo.filePath = filePath;
            fo.fileExtension = getFileExt(filePath);
            if (cache!=null && content.length > 0) {
                cache.put(filePath, fo);
            }
        }
        result.setCode(HttpAdapter.SC_OK);
        result.setMessage("");
        result.setPayload(fo.content);
        result.setFileExtension(fo.fileExtension);
        result.setModificationDate(fo.modified);
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
