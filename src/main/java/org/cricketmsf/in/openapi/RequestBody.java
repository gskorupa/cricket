/*
 * Copyright 2020 Grzegorz Skorupa .
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

/**
 *
 * @author greg
 */
public class RequestBody {
    private String description;
    private boolean required = false;
    private HashMap<String,BodyContent> content;
    
    public RequestBody(BodyContent content, boolean required, String description){
        this.content=new HashMap<>();
        this.content.put(content.getName(), content);
        this.description=description;
        this.required=required;
    }
    
    public RequestBody content(BodyContent content){
        this.getContent().put(content.getName(), content);
        return this;
    }


    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @return the content
     */
    public HashMap<String,BodyContent> getContent() {
        return content;
    }
    
}
