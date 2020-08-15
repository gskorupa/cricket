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
public class Schema extends Element{

    private String type;
    private String format;

    public Schema(SchemaType type, SchemaFormat format) {
        this.type = type.name();
        if (null == format) {
            this.format = null;
        } else {
            this.format = format.toString();
        }
    }

    public Schema() {
        this.type = SchemaType.string.name();
        this.format = null;
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
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("type: \"").append(getType()).append("\"").append(lf);
        if (null != getFormat()) {
            sb.append(indent).append("format: \"").append(getFormat()).append("\"").append(lf);
        }
        return sb.toString();
    }

}
