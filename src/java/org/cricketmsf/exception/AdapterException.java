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
public class AdapterException extends Exception {

    public static final int UNSUPPORTED_CONTENT_TYPE = 700;
    public static final int IO_EXCEPTION = 701;
    public static final int CRYPTOGRAPHY_EXCEPTION = 702;
    public static final int STRANGE_CODE = 703;
    public static final int UNKNOWN = 777;

    private int code = UNKNOWN;
    private String message;

    public AdapterException(int code) {
        this.code = code;
        switch (code) {
            case UNKNOWN:
                message = "unknown error";
                break;
            default:
                this.code= UNKNOWN;
                message = "unknown error";
                break;
        }
    }

    public AdapterException(int code, String message) {
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
