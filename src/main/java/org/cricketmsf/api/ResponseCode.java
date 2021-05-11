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
package org.cricketmsf.api;

/**
 *
 * @author greg
 */
public class ResponseCode {
    public final static int OK = 200;
    public final static int ACCEPTED = 202;
    public final static int CREATED = 201;

    public final static int MOVED_PERMANENTLY = 301;
    public final static int MOVED_TEMPORARY = 302;
    public final static int NOT_MODIFIED = 304;

    public final static int BAD_REQUEST = 400;
    public final static int UNAUTHORIZED = 401;
    public final static int SESSION_EXPIRED = 401;
    public final static int FORBIDDEN = 403;
    public final static int NOT_FOUND = 404;
    public final static int METHOD_NOT_ALLOWED = 405;
    public final static int CONFLICT = 409;

    public final static int INTERNAL_SERVER_ERROR = 500;
    public final static int NOT_IMPLEMENTED = 501;
    public final static int UNAVAILABLE = 503;    
}
