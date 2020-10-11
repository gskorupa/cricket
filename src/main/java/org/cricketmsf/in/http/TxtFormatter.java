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
package org.cricketmsf.in.http;

import java.util.List;
import java.util.Map;
import org.cricketmsf.event.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.services.BasicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class TxtFormatter {
    private static final Logger logger = LoggerFactory.getLogger(TxtFormatter.class);

    private static TxtFormatter instance = null;

    public static TxtFormatter getInstance() {
        if (instance != null) {
            return instance;
        } else {
            instance = new TxtFormatter();
            return instance;
        }
    }

    public String format(Result r) {
        StringBuilder sb = new StringBuilder();
        if (r.getData() == null) {
            return "";
        }
        try {
            if (r.getData() instanceof List) {
                List list = (List) r.getData();
                if (list.size() > 0) {
                    List header = (List) list.get(0);
                    List row;
                    for (int i = 1; i < list.size(); i++) {
                        row = (List) list.get(i);
                        for (int j = 0; j < header.size(); j++) {
                            sb.append(header.get(j));
                            sb.append("=");
                            sb.append(row.get(j));
                            sb.append("\r\n");
                        }
                        sb.append("\r\n");
                    }
                }
            } else if (r.getData() instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) r.getData();
                for (String key : data.keySet()) {
                    sb.append(key);
                    sb.append("=");
                    sb.append(data.get(key));
                    sb.append("\r\n");
                }
            } else {
                sb.append(r.getData().toString());
                sb.append("\r\n");
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.warn(e.getMessage());
            sb.append(e.getMessage());
        }
        return sb.toString();
    }

}
