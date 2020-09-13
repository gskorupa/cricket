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

/**
 *
 * @author greg
 */
public class Parameter extends Element{

    private String name;
    private ParameterLocation in;
    private String description;
    private boolean required;
    private Schema schema;
    
    public Parameter(String name, ParameterLocation in, boolean required, String description) {
        this(name,in,required,description,new Schema());
    }

    public Parameter(String name, ParameterLocation in, boolean required, String description, Schema schema) {
        this.name = name;
        this.in = in;
        if (in == ParameterLocation.path) {
            this.required = true;
        } else {
            this.required = required;
        }
        this.description = description;
        this.schema = schema;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the in
     */
    public ParameterLocation getIn() {
        return in;
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

    public String toYaml(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("in: ").append(getIn().name()).append(lf);
        sb.append(indent).append("description: \"").append(getDescription()).append("\"").append(lf);
        sb.append(indent).append("required: ").append(isRequired()).append(lf);
        //sb.append(indent).append("schema:").append(lf);
        sb.append(schema.toYaml(indent));
        return sb.toString();
    }

}
