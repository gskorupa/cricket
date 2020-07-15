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
package org.cricketmsf.exception;

/**
 *
 * @author greg
 */
public class WebsocketException extends Exception {

    public static int CONTEXT_NOT_DEFINED = 1;
    public static int IO_EXCEPTION = 2;
    public static int NOT_IMPLEMENTED = 99;
    public static int UNDEFINED = 100;

    private int code = NOT_IMPLEMENTED;
    private String message;

    public WebsocketException(int code) {
        this.code = code;
        switch (code) {
            case 99:
                message = "operation not implemented";
                break;
            case 1:
                message = "unknown context";
                break;
            case 2:
                message = "IO exception";
                break;
            case 100:
                message = "unknown error";
                break;
        }
    }

    public WebsocketException(int code, String message) {
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
