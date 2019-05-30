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
import org.apache.commons.csv.QuoteMode;

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

    public String format(List list) {
        StringBuilder sb = new StringBuilder();
        try {
            CSVPrinter printer = new CSVPrinter(sb, CSVFormat.EXCEL.withRecordSeparator("\r\n"));
            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    printer.printRecord((List) list.get(i));
                }
            }
        } catch (IOException e) {
            sb.append(e.getMessage());
        }
        return sb.append("\r\n").toString();
    }

    public String format(Map data) {
        StringBuilder sb = new StringBuilder();
        try {
            CSVPrinter printer = new CSVPrinter(sb, CSVFormat.EXCEL.withRecordSeparator("\r\n"));
            printer.printRecord(data.values());
        } catch (IOException e) {
            sb.append(e.getMessage());

        }
        return sb.append("\r\n").toString();
    }

    public String format(Result r) {
        if (r.getData() instanceof List) {
            return format((List) r.getData());
        } else if (r.getData() instanceof Map) {
            return format((Map) r.getData());
        } else {
            return "unsupported data format\r\n";
            //TODO: error code?
        }
    }

}
