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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class CsvFormatter {

    private static CsvFormatter instance = null;

    public static CsvFormatter getInstance() {
        if (instance != null) {
            return instance;
        } else {
            instance = new CsvFormatter();
            return instance;
        }
    }

    public String format(Result r) {
        StringBuilder sb = new StringBuilder();

        try {
            CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
            if (r.getData() instanceof List) {
                List list = (List) r.getData();
                if (list.size() > 0) {
                    printer.printRecord((List) list.get(0));
                    for (int i = 1; i < list.size(); i++) {
                        printer.printRecord((List) list.get(i));
                    }
                }
            } else if (r.getData() instanceof Map){
                Map data = (Map) r.getData();
                printer.printRecord(data.keySet());
                printer.printRecord(data.values());
            } else {
                sb.append("unsupported data format");
                //TODO: error code?
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            sb.append(e.getMessage());
        }
        return sb.toString();
    }

}
