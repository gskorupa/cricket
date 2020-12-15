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
package org.cricketmsf.in.http;

import org.cricketmsf.RequestObject;
import org.cricketmsf.api.ResponseCode;
import org.cricketmsf.event.ProcedureCall;
import org.cricketmsf.microsite.out.cms.ContentRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class Tester extends HttpPortedAdapter {

    private static final Logger logger = LoggerFactory.getLogger(Tester.class);

    @Override
    protected ProcedureCall preprocess(RequestObject request, long rootEventId) {
        logger.info(dumpRequest(request));
        return ProcedureCall.toRespond(ResponseCode.OK, "");
    }

}
