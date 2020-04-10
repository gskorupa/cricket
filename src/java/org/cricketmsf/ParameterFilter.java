/*
 * Copyright 2015 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Filter.Chain;
import com.sun.net.httpserver.HttpExchange;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * This filter is used to recognize, parse and transform request parameters into
 * the parameters map which could be easily accessible within adapters or
 * service methods.
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 * Many thanks for Leonardo Marcelino https://leonardom.wordpress.com
 */
public class ParameterFilter extends Filter {

    public final static char CR = (char) 0x0D;
    public final static char LF = (char) 0x0A;
    private String parameterEncoding = null;
    private long fileSizeLimit = 0;

    public ParameterFilter() {
        super();
    }

    private void init() {
        setParameterEncoding((String) Kernel.getInstance().properties.getOrDefault("request-encoding", "UTF-8"));
        setFileSizeLimit((String) Kernel.getInstance().properties.getOrDefault("file.upload.maxsize", "1000000"));
    }

    @Override
    public String description() {
        return "Parses the requested URI for parameters";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain)
            throws IOException {
        exchange.setAttribute("body", null);
        exchange.setAttribute("parameters", new HashMap<String, Object>());
        if (null == parameterEncoding) {
            init();
        }
        String method = exchange.getRequestMethod().toUpperCase();
        try {
            switch (method) {
                case "GET":
                    exchange.setAttribute("parameters", parseGetParameters(exchange));
                    break;
                case "POST":
                case "PUT":
                case "DELETE":
                    //try {
                    Map<String, Object> tmp;
                    tmp = parsePostParameters(exchange);
                    /*
                    System.out.println("BODY PARMS PARSED:" + tmp.size());
                    tmp.forEach((k, v) -> {
                        System.out.println("BODY P:" + k + ":" + v);
                    });
                    */
                    if (tmp.containsKey("&&&data")) {
                        exchange.setAttribute("body", tmp.get("&&&data"));
                        //tmp.remove("&&&data");
                    }
                    exchange.setAttribute("parameters", tmp);
                    //} catch (IOException e) {
                    //    exchange.sendResponseHeaders(400, e.getMessage().length());
                    //    exchange.getResponseBody().write(e.getMessage().getBytes());
                    //    exchange.getResponseBody().close();
                    //    exchange.close();
                    //    return;
                    //}
                    break;
                default:
                    //System.out.println("BODY METHOD:" + method);
                    exchange.setAttribute("parameters", parseGetParameters(exchange));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        chain.doFilter(exchange);
    }

    private Map<String, Object> parseGetParameters(HttpExchange exchange)
            throws UnsupportedEncodingException {

        Map<String, Object> parameters = new HashMap<>();
        ArrayList<RequestParameter> list = parseQuery(exchange.getRequestURI().getRawQuery());
        list.forEach((param) -> {
            parameters.put(param.name, param.value);
        });
        return parameters;
    }

    private Map<String, Object> parsePostParameters(HttpExchange exchange)
            throws IOException {

        String contentTypeHeader = exchange.getRequestHeaders().getFirst("Content-Type");
        String contentType;
        if (contentTypeHeader != null) {
            if (contentTypeHeader.indexOf(";") > 0) {
                contentType = contentTypeHeader.substring(0, contentTypeHeader.indexOf(";")).toLowerCase();
            } else {
                contentType = contentTypeHeader.toLowerCase();
            }
        } else {
            //throw new IOException("no conent type header received");
            contentType = "";
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = new HashMap<>();
        InputStreamReader isr;
        BufferedReader br;
        String query;
        StringBuilder content = new StringBuilder();
        switch (contentType) {
            case "multipart/form-data":
                try {
                parameters = parseForm(contentTypeHeader.substring(contentType.length() + 11), exchange.getRequestBody());
            } catch (IndexOutOfBoundsException e) {
                throw new IOException("request content inconsistent with declared content type");
            }
            break;
            case "text/plain":
            case "text/csv":
            case "application/json":
            case "text/xml":
                //try {
                //isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                /*
                br = new BufferedReader(isr);
                while ((query = br.readLine()) != null) {
                    content.append(query);
                    content.append("\r\n");
                }
                 */
                Scanner sc = new Scanner(exchange.getRequestBody());
                //Reading line by line from scanner to StringBuffer
                StringBuilder sb = new StringBuilder();
                while (sc.hasNext()) {
                    sb.append(sc.nextLine());
                }
                //System.out.println("BODY:" + sb.toString());
                //} catch (IOException e) {
                //} finally {
                //    if (null != isr) {
                //        isr.close();
                //    }
                //}
                //parameters.put("data", content.toString()); //TODO: remove "data" parameter
                //parameters.put("&&&data", content.toString());
                parameters.put("data", sb.toString()); //TODO: remove "data" parameter
                parameters.put("&&&data", sb.toString());
                break;
            case "application/x-www-form-urlencoded":
                isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                br = new BufferedReader(isr);
                ArrayList<RequestParameter> list;
                while ((query = br.readLine()) != null) {
                    list = parseQuery(query);
                    for (RequestParameter param : list) {
                        if (null == param.value) {
                            parameters.put("&&&data", param.name);
                        } else if (parameters.containsKey(param.name)) {
                            Object obj = parameters.get(param.name);
                            if (obj instanceof List<?>) {
                                List<String> values = (List<String>) obj;
                                values.add(param.value);
                            } else if (obj instanceof String) {
                                List<String> values = new ArrayList<>();
                                values.add((String) obj);
                                values.add(param.value);
                                parameters.put(param.name, values);
                            }
                        } else {
                            parameters.put(param.name, param.value);
                        }
                    }
                }
                isr.close();
                break;
            default:
            /*
                isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                br = new BufferedReader(isr);
                ArrayList<RequestParameter> list;
                while ((query = br.readLine()) != null) {
                    list = parseQuery(query);
                    for (RequestParameter param : list) {
                        if (null == param.value) {
                            parameters.put("&&&data", param.name);
                        } else if (parameters.containsKey(param.name)) {
                            Object obj = parameters.get(param.name);
                            if (obj instanceof List<?>) {
                                List<String> values = (List<String>) obj;
                                values.add(param.value);
                            } else if (obj instanceof String) {
                                List<String> values = new ArrayList<>();
                                values.add((String) obj);
                                values.add(param.value);
                                parameters.put(param.name, values);
                            }
                        } else {
                            parameters.put(param.name, param.value);
                        }
                    }
                }
                isr.close();
             */
        }
        exchange.getRequestBody().close();
        return parameters;
    }

    private HashMap<String, Object> parseForm(String boundary, InputStream br)
            throws IOException {
        HashMap<String, Object> parameters = new HashMap<>();
        String line;
        String startLine = "--" + boundary;
        String endLine = startLine + "--";
        String contentDisposition;
        String paramName;
        String value;
        String contentType; //TODO: not used?
        String fileName;
        FileParameter fileParameter;
        line = readLine(br);
        try {
            do {
                //first line is boundary
                //read next
                contentDisposition = readLine(br);
                paramName = contentDisposition.substring(38, contentDisposition.length() - 1);
                fileName = null;
                if (paramName.startsWith("file\";")) {
                    fileName = paramName.substring(paramName.lastIndexOf("\"") + 1);
                    paramName = paramName.substring(0, paramName.indexOf("\""));
                }
                contentType = readLine(br);
                if (fileName == null || fileName.isEmpty()) {
                    value = "";
                    while (!(line = readLine(br)).startsWith(startLine)) {
                        value = value.concat(line);
                    }
                    parameters.put(paramName, value);
                } else {
                    fileParameter = readFileContent(br, startLine + CR + LF, endLine + CR + LF, getFileSizeLimit(), fileName);
                    parameters.put(paramName, contentType + ";" + fileParameter.fileSize + ";" + fileParameter.fileLocation);
                    line = fileParameter.nextLine;
                }

            } while (!endLine.equals(line));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parameters;
    }

    private FileParameter readFileContent(InputStream is, String startLine, String endLine, long fileSizeLimit, String fileName) throws IOException {
        FileParameter fileParameter = new FileParameter();
        int bufferLength = 1024;
        int endLineLength1 = endLine.length() - 1;
        byte[] buffer = new byte[bufferLength];
        int pos = 0;
        long totalSize = 0;
        int b;
        String line, sline;
        boolean startLineFound = false;
        boolean endLineFound = false;
        int targetPos;
        int bytesToMove;
        Path filePath;
        OutputStream output;
        boolean writingStarted = false;
        boolean fileTooLarge = false;
        String ext;

        assert fileName != null;
        assert !fileName.isEmpty();

        if (fileName.lastIndexOf(".") >= 0) {
            ext = fileName.substring(fileName.lastIndexOf("."));
        } else {
            ext = "";
        }
        filePath = Files.createTempFile("cricket-", ext);
        filePath.toFile().deleteOnExit();
        output = Files.newOutputStream(filePath);
        do {
            b = is.read();
            if (b == -1) {
                break;
            }
            buffer[pos] = (byte) b;
            pos++;
            if (pos > endLineLength1) {
                //check
                try {
                    //TODO: optimization?
                    line = new String(Arrays.copyOfRange(buffer, pos - endLine.length(), pos));
                    sline = new String(Arrays.copyOfRange(buffer, pos - startLine.length(), pos));
                } catch (Exception e) {
                    line = null;
                    sline = null;
                }
                if (startLine.equals(sline)) {
                    startLineFound = true;
                    fileParameter.nextLine = line.substring(0, startLine.length() - 2);
                    fileParameter.fileLocation = "" + filePath;
                } else if (endLine.equals(line)) {
                    endLineFound = true;
                    fileParameter.nextLine = line.substring(0, endLine.length() - 2);
                    fileParameter.fileLocation = "" + filePath;
                }
            }
            if (startLineFound || endLineFound) {
                //save to file - WITHOUT ENDING CRLF!
                //TODO: byte to file
                if (startLineFound) {
                    bytesToMove = pos - startLine.length();
                } else {
                    bytesToMove = pos - endLine.length();
                }
                if (output != null) {
                    if (writingStarted) {
                        //last buffer ends with CRLF
                        output.write(Arrays.copyOfRange(buffer, 0, bytesToMove - 2));
                        totalSize = totalSize + bytesToMove - 2;
                    } else {
                        //because first buffer starts with CRLF
                        //last buffer ends with CRLF
                        output.write(Arrays.copyOfRange(buffer, 2, bytesToMove - 2));
                        writingStarted = true;
                        totalSize = totalSize + bytesToMove - 4;
                    }
                }
            } else if (pos == bufferLength) {
                bytesToMove = bufferLength - endLine.length();
                if (output != null && !fileTooLarge) {
                    if (writingStarted) {
                        output.write(Arrays.copyOfRange(buffer, 0, bytesToMove));
                        totalSize = totalSize + bytesToMove;
                    } else {
                        //because first buffer starts with CRLF
                        output.write(Arrays.copyOfRange(buffer, 2, bytesToMove));
                        totalSize = totalSize + bytesToMove - 2;
                        writingStarted = true;
                    }
                }
                //shift buffer
                targetPos = 0;
                for (int i = bufferLength - endLine.length(); i < bufferLength; i++) {
                    buffer[targetPos] = buffer[i];
                    targetPos++;
                }
                pos = targetPos;
                if (fileSizeLimit > 0 && totalSize > fileSizeLimit) {
                    fileTooLarge = true;
                }
            }
        } while (!startLineFound && !endLineFound && !fileTooLarge);
        if (output != null) {
            output.close();
        }
        if (fileTooLarge) {
            fileParameter.fileSize = -1;
            fileParameter.fileLocation = null;
            try {
                Files.delete(filePath);
            } catch (IOException e) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this, e.getMessage()));
            }
        } else {
            fileParameter.fileSize = totalSize;
        }
        return fileParameter;
    }

    private String readLine(InputStream is) throws IOException {
        int c = -1;
        int prev;
        int pos = 0;
        byte[] bytes = new byte[1024];
        do {
            prev = c;
            c = is.read();
            if (prev == 13 && c == 10) {
                break;
            }
            if (c != 13) {
                bytes[pos++] = (byte) c;
            }
        } while (c != -1 && pos < 1024);
        return new String(Arrays.copyOfRange(bytes, 0, pos));
    }

    @SuppressWarnings("unchecked")
    private ArrayList parseQuery(String query)
            throws UnsupportedEncodingException {
        ArrayList<RequestParameter> list = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            return list;
        }
        String pairs[] = query.split("[&]");
        for (String pair : pairs) {
            String param[] = pair.split("[=]");
            String key;
            String value;
            if (param.length > 0) {
                key = URLDecoder.decode(param[0], getParameterEncoding());
            } else {
                key = null;
            }
            if (param.length > 1) {
                value = URLDecoder.decode(param[1], getParameterEncoding());
            } else {
                value = null;
            }
            list.add(new RequestParameter(key, value));
        }
        return list;
    }

    /**
     * @return the fileSizeLimit
     */
    public long getFileSizeLimit() {
        return fileSizeLimit;
    }

    /**
     * @param sizeLimit the fileSizeLimit to set
     */
    public void setFileSizeLimit(String sizeLimit) {
        try {
            this.setFileSizeLimit(Long.parseLong(sizeLimit));
        } catch (NumberFormatException | NullPointerException e) {
            this.setFileSizeLimit(1000000); // 1MB (SI)
        }
    }

    /**
     * @return the parameterEncoding
     */
    public String getParameterEncoding() {
        return parameterEncoding;
    }

    /**
     * @param parameterEncoding the parameterEncoding to set
     */
    public void setParameterEncoding(String parameterEncoding) {
        this.parameterEncoding = parameterEncoding;
    }

    /**
     * @param fileSizeLimit the fileSizeLimit to set
     */
    public void setFileSizeLimit(long fileSizeLimit) {
        this.fileSizeLimit = fileSizeLimit;
    }
}
