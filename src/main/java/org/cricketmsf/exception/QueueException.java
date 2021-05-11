/*
 * Copyright 2017 Grzegorz Skorupa .
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
package org.cricketmsf.exception;

/**
 *
 * @author greg
 */
public class QueueException extends Exception {

    public static final int UNKNOWN = 888;
    public static int NOT_IMPLEMENTED = 887;
    public static int QUEUE_NOT_DEFINED = 886;
    public static int CLIENT_NOT_DEFINED = 885;
    public static int SUBSCRIPTION_NOT_POSSIBLE = 884;

    private int code = NOT_IMPLEMENTED;
    private String message;

    public QueueException(int code) {
        this.code = code;
        switch (code) {
            case 887:
                message = "operation not implemented";
                break;
            case 886:
                message = "queue not defined";
                break;
            case 888:
                message = "unknown error";
                break;
        }
    }

    public QueueException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

}
