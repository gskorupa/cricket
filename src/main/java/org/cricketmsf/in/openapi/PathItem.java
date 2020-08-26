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
package org.cricketmsf.in.openapi;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author greg
 */
public class PathItem extends Element implements Comparable<PathItem> {

    private String path;
    private Operation get;
    private Operation put;
    private Operation post;
    private Operation delete;
    private Operation patch;
    private Operation head;
    private Operation options;
    private Operation trace;
    private Operation connect;

    public PathItem(String path) {
        setPath(path);
    }

    public String toYaml(String indent) {
        StringBuilder sb = new StringBuilder();
        Operation operation;

        operation = getGet();
        if (null != operation) {
            sb.append(indent).append("get:").append(lf);
            sb.append(operation.toYaml(indent + indentStep));
        }

        operation = getPost();
        if (null != operation) {
            sb.append(indent).append("post:").append(lf);
            sb.append(operation.toYaml(indent + indentStep));
        }

        operation = getPut();
        if (null != operation) {
            sb.append(indent).append("put:").append(lf);
            sb.append(operation.toYaml(indent + indentStep));
        }

        operation = getPatch();
        if (null != operation) {
            sb.append(indent).append("patch:").append(lf);
            sb.append(operation.toYaml(indent + indentStep));
        }

        operation = getDelete();
        if (null != operation) {
            sb.append(indent).append("delete:").append(lf);
            sb.append(operation.toYaml(indent + indentStep));
        }

        operation = getHead();
        if (null != operation) {
            sb.append(indent).append("head:").append(lf);
            sb.append(operation.toYaml(indent + indentStep));
        }

        operation = getOptions();
        if (null != operation) {
            sb.append(indent).append("options:").append(lf);
            sb.append(operation.toYaml(indent + indentStep));
        }

        operation = getConnect();
        if (null != operation) {
            sb.append(indent).append("connect:").append(lf);
            sb.append(operation.toYaml(indent + indentStep));
        }

        operation = getTrace();
        if (null != operation) {
            sb.append(indent).append("trace:").append(lf);
            sb.append(operation.toYaml(indent + indentStep));
        }

        return sb.toString();
    }
    
    public void setOperation(Operation op){
        switch(op.getMethod()){
            case "GET":
                setGet(op);
                break;
            case "POST":
                setPost(op);
                break;
            case "PUT":
                setPut(op);
                break;
            case "PATCH":
                setPatch(op);
                break;
            case "DELETE":
                setDelete(op);
                break;
            case "CONNECT":
                setConnect(op);
                break;
            case "HEAD":
                setHead(op);
                break;
            case "TRACE":
                setTrace(op);
                break;
            case "OPTIONS":
                setOptions(op);
                break;
        }
    }

    /**
     * @return the get
     */
    public Operation getGet() {
        return get;
    }

    /**
     * @param get the get to set
     */
    public void setGet(Operation get) {
        this.get=get;
        this.path=this.path+get.getPathModifier();
    }

    /**
     * @return the put
     */
    public Operation getPut() {
        return put;
    }

    /**
     * @param put the put to set
     */
    public void setPut(Operation put) {
        this.put = put;
    }

    /**
     * @return the post
     */
    public Operation getPost() {
        return post;
    }

    /**
     * @param post the post to set
     */
    public void setPost(Operation post) {
        this.post = post;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int compareTo(PathItem o) {
        return this.getPath().compareTo(o.getPath());
    }

    /**
     * @return the delete
     */
    public Operation getDelete() {
        return delete;
    }

    /**
     * @param delete the delete to set
     */
    public void setDelete(Operation delete) {
        this.delete = delete;
    }

    /**
     * @return the patch
     */
    public Operation getPatch() {
        return patch;
    }

    /**
     * @param patch the patch to set
     */
    public void setPatch(Operation patch) {
        this.patch = patch;
    }

    /**
     * @return the head
     */
    public Operation getHead() {
        return head;
    }

    /**
     * @param head the head to set
     */
    public void setHead(Operation head) {
        this.head = head;
    }

    /**
     * @return the options
     */
    public Operation getOptions() {
        return options;
    }

    /**
     * @param options the options to set
     */
    public void setOptions(Operation options) {
        this.options = options;
    }

    /**
     * @return the trace
     */
    public Operation getTrace() {
        return trace;
    }

    /**
     * @param trace the trace to set
     */
    public void setTrace(Operation trace) {
        this.trace = trace;
    }

    /**
     * @return the connect
     */
    public Operation getConnect() {
        return connect;
    }

    /**
     * @param connect the connect to set
     */
    public void setConnect(Operation connect) {
        this.connect = connect;
    }

}
