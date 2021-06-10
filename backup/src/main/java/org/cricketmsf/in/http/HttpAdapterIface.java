/*
 * Copyright 2015 Grzegorz Skorupa .
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

import java.util.Map;
import org.cricketmsf.in.InboundAdapterIface;
import org.cricketmsf.in.openapi.Operation;

/**
 *
 * @author Grzegorz Skorupa 
 */
public interface HttpAdapterIface extends InboundAdapterIface{
    public void defineApi();
    public void addOperationConfig(Operation operation);
    public Map<String,Operation> getOperations();
}
