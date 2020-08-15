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

import java.util.ArrayList;

/**
 *
 * @author greg
 */
public class Response extends Element{

    String codeName;
    private String description;
    private ArrayList<Content> content;

    public Response(String codeName) {
        this.codeName = codeName;
        description = "";
        content = new ArrayList<>();
    }

    public Response description(String description) {
        this.description = description;
        return this;
    }

    public Response content(Content content) {
        this.content.add(content);
        return this;
    }

    public Response content(String content) {
        this.content.add(new Content(content));
        return this;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String toYaml(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append(codeName).append(":").append(lf);
        if (null != description) {
            sb.append(indent).append(indentStep).append("description: \"").append(getDescription()).append("\"").append(lf);
        }
        if (content.size() > 0) {
            sb.append(indent).append(indentStep).append("content:").append(lf);
            content.forEach(cnt -> {
                sb.append(indent).append(indentStep).append(indentStep).append("\"").append(cnt.getName()).append("\":").append(" {}").append(lf);
            });
        }

        return sb.toString();
    }

}
