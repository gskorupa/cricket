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
package org.cricketmsf.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author greg
 */
public class EventUtils {

    private static final Logger logger = LoggerFactory.getLogger(EventUtils.class);
    public static long MINIMAL_DELAY = 0;

    public static Delay getDelayFromDateDefinition(String dateDefinition, long eventCreationDate) {
        Delay delay = new Delay();
        boolean wrongFormat = false;
        // +10s
        // *10s
        // 2021.01.31 23:59:00 UTC
        //2021.01.31 23:59:00 UTC|*60s //TODO: not implemented

        if (dateDefinition.startsWith("+") || dateDefinition.startsWith("*")) {
            delay.setCyclic(dateDefinition.startsWith("*"));
            try {
                delay.setDelay(parseDelay(dateDefinition.substring(1, dateDefinition.length() - 1)));
            } catch (NumberFormatException e) {
                wrongFormat = true;
            }
            delay.setUnit(parseUnit(dateDefinition.substring(dateDefinition.length() - 1)));
            if (null == delay.getUnit()) {
                wrongFormat = true;
            }
            if (TimeUnit.SECONDS == delay.getUnit()) {
                delay.setDelay(delay.getDelay() * 1000);
                delay.setFirstExecutionTime(delay.getDelay() + eventCreationDate);
            }
            if (TimeUnit.MINUTES == delay.getUnit()) {
                delay.setDelay(delay.getDelay() * 1000 * 60);
                delay.setFirstExecutionTime(delay.getDelay() + eventCreationDate);
            }
            if (TimeUnit.HOURS == delay.getUnit()) {
                delay.setDelay(delay.getDelay() * 1000 * 60 * 60);
                delay.setFirstExecutionTime(delay.getDelay() + eventCreationDate);
            }
            if (TimeUnit.DAYS == delay.getUnit()) {
                delay.setDelay(delay.getDelay() * 1000 * 60 * 60 * 24);
                delay.setFirstExecutionTime(delay.getDelay() + eventCreationDate);
            }

        } else {
            //parse date and replace with delay from now
            delay.setExecutionDateDefined(true);
            long tmp = parseFirstExecutionTime(dateDefinition, eventCreationDate);
            if (tmp <= 0) {
                wrongFormat = true;
            }
            delay.setFirstExecutionTime(tmp);
            delay.setUnit(TimeUnit.MILLISECONDS);
            /*
            if (delay.isCyclic()) {
                try {
                    delay.setDelay(parseDelay(params[1].substring(1, params[1].length() - 1)));
                } catch (NumberFormatException e) {
                    wrongFormat = true;
                }
                delay.setUnit(parseUnit(params[1].substring(params[1].length() - 1)));
                if (null == delay.getUnit()) {
                    wrongFormat = true;
                }
            }
             */
        }
        if (wrongFormat) {
            logger.warn("WARNING unsuported delay format: {}", dateDefinition);
            return null;
        }

        return delay;
    }

    private static long parseDelay(String definition) {
        return Long.parseLong(definition);
    }

    private static TimeUnit parseUnit(String definition) {
        switch (definition) {
            case "d":
                return TimeUnit.DAYS;
            case "h":
                return TimeUnit.HOURS;
            case "m":
                return TimeUnit.MINUTES;
            case "s":
                return TimeUnit.SECONDS;
            default:
                return null;
        }
    }

    private static long parseFirstExecutionTime(String dateStr, long eventCreationDate) {
        /*
        String[] params = dateStr.split("|");
        if (params.length==2 && params[1].startsWith("*")) {
            cyclic=true;
            //TODO: delay & unit
            //TODO: return delay
        }
         */
        long result;
        boolean cyclic = false;
        Date target;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss Z");
        try {
            target = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            try {
                String today = java.time.LocalDate.now(ZoneId.of("UTC")).toString();
                // maybe dateStr is only HH:mm:ss Z
                target = dateFormat.parse(today + " " + dateStr);
            } catch (ParseException ex) {
                logger.warn(ex.getMessage());
                return -1;
            }
        }
        result = target.getTime();
        if (result < System.currentTimeMillis()) {
            if (cyclic) {
                result = result + 24 * 60 * 60 * 1000;
            } else {
                result = eventCreationDate;
            }
        }
        return result;
    }

}
