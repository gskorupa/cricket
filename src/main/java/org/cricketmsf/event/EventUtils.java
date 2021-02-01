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

    /*
    public static Delay getDelayForEvent(Event ev, boolean restored) {
        Delay delay = new Delay();
        if (restored) {
            delay.setUnit(TimeUnit.MILLISECONDS);
            long d = ev.getExecutionTime() - System.currentTimeMillis();
            if (d < MINIMAL_DELAY) {
                d = MINIMAL_DELAY;
            }
            delay.setDelay(d);
            return delay;
        }

        boolean wrongFormat = false;
        String dateDefinition = ev.getTimeDefinition();
        if (null == dateDefinition) {
            delay.setCyclic(ev.isCyclic());
            delay.setDelay(ev.getExecutionTime() - System.currentTimeMillis());
            if (delay.getDelay() < 0) {
                delay.setDelay(MINIMAL_DELAY);
            }
            delay.setUnit(TimeUnit.MILLISECONDS);
        } else {
            delay.setCyclic(dateDefinition.startsWith("*") || dateDefinition.indexOf("|*") > 0);
            if (dateDefinition.startsWith("+") || dateDefinition.startsWith("*")) {
                try {
                    delay.setDelay(Long.parseLong(dateDefinition.substring(1, dateDefinition.length() - 1)));
                } catch (NumberFormatException e) {
                    wrongFormat = true;
                }
                String unit = dateDefinition.substring(dateDefinition.length() - 1);
                switch (unit) {
                    case "d":
                        delay.setUnit(TimeUnit.DAYS);
                        break;
                    case "h":
                        delay.setUnit(TimeUnit.HOURS);
                        break;
                    case "m":
                        delay.setUnit(TimeUnit.MINUTES);
                        break;
                    case "s":
                        delay.setUnit(TimeUnit.SECONDS);
                        break;
                    default:
                        wrongFormat = true;
                }
            } else {
                //parse date and replace with delay from now
                delay.setUnit(TimeUnit.MILLISECONDS);
                delay.setFirstExecutionTime(parseFirstExecutionTime(dateDefinition, delay.isCyclic()));
            }
            if (wrongFormat) {
                logger.info("WARNING unsuported delay format: " + dateDefinition);
                return null;
            }
        }
        return delay;
    }
*/
    public static Delay getDelayForEvent(String dateDefinition) {
        Delay delay = new Delay();
        boolean wrongFormat = false;
        // +10s
        // *10s
        // 2021.01.31 23:59:00 UTC
        // 2021.01.31 23:59:00 UTC|*60s

        String[] params = dateDefinition.split("|");
        for (String param : params) {
            if (param.startsWith("*")) {
                delay.setCyclic(true);
            }
        }
        if (params[0].startsWith("+") || params[0].startsWith("*")) {
            try {
                delay.setDelay(parseDelay(params[0].substring(1, params[0].length() - 1)));
            } catch (NumberFormatException e) {
                wrongFormat = true;
            }
            delay.setUnit(parseUnit(params[0].substring(params[0].length() - 1)));
            if (null == delay.getUnit()) {
                wrongFormat = true;
            }
        } else {
            //parse date and replace with delay from now
            delay.setFirstExecutionTime(parseFirstExecutionTime(params[0], delay.isCyclic()));
            delay.setUnit(TimeUnit.MILLISECONDS);
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
        }
        if (wrongFormat) {
            logger.info("WARNING unsuported delay format: " + dateDefinition);
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

    private static long parseFirstExecutionTime(String dateStr, boolean cyclic) {
        long result;
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
                result = System.currentTimeMillis();
            }
        }
        return result;
    }

}
