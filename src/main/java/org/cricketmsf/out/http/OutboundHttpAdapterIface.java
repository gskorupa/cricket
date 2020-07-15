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
package org.cricketmsf.out.http;

import org.cricketmsf.exception.AdapterException;
import org.cricketmsf.in.http.Result;
import org.cricketmsf.out.OutboundAdapterIface;

/**
 * HttpClient will be better name
 *
 * @author greg
 */
public interface OutboundHttpAdapterIface extends OutboundAdapterIface{

    public Result send(Object data) throws AdapterException;
    public Result send(Request request,  boolean transform) throws AdapterException;
    public Result send(String url, Request request, Object data) throws AdapterException;
    public Result send(Object data, boolean transform) throws AdapterException;
    public Result send(String url, Request request, Object data, boolean transform) throws AdapterException;
    public Result send(String url, Request request, Object data, boolean transform, boolean trustAllCertificates) throws AdapterException;    
    //public HashMap getProperties();
}
