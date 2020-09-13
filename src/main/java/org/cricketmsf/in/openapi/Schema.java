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

/**
 *
 * @author greg
 */
public class Schema extends Element{

    private String type;
    private String format;
    private HashMap<String,SchemaProperty> properties;

    public Schema(SchemaType type) {
        this.type = type.name();
        this.format = null;
        properties=new HashMap<>();        
    }
    public Schema(SchemaType type, SchemaFormat format) {
        this.type = type.name();
        if (null == format) {
            this.format = null;
        } else {
            this.format = format.toString();
        }
        properties=new HashMap<>();
    }

    public Schema() {
        this.type = SchemaType.string.name();
        this.format = null;
        properties=new HashMap<>();
    }
    
    public Schema property(SchemaProperty property){
        properties.put(property.getName(),property);
        return this;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    public String toYaml(String indent) {
        String indent2=indent+indentStep;
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("schema:").append(lf);
        sb.append(indent2).append("type: \"").append(getType()).append("\"").append(lf);
        if("object".equals(getType())){
            sb.append(indent2).append("properties:").append(lf);
            properties.forEach((pname,prop)->{
                sb.append(indent2).append(indentStep).append(pname).append(":").append(lf);
                sb.append(indent2).append(indentStep).append(indentStep).append("type: \"").append(prop.getType()).append("\"").append(lf);
                if(null!=prop.getFormat()&& !prop.getFormat().isBlank()){
                    sb.append(indent2).append(indentStep).append(indentStep).append("format: \"").append(prop.getFormat()).append("\"").append(lf);
                }
                if(null!=prop.getDescription() && !prop.getDescription().isBlank()){
                    sb.append(indent2).append(indentStep).append(indentStep).append("description: \"").append(prop.getDescription()).append("\"").append(lf);
                }
            });
        }else if (null != getFormat()) {
            sb.append(indent2).append("format: \"").append(getFormat()).append("\"").append(lf);
        }
        return sb.toString();
    }

}
