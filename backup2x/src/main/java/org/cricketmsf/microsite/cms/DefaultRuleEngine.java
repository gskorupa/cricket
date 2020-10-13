/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cricketmsf.microsite.cms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.cricketmsf.Adapter;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.OutboundAdapter;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class DefaultRuleEngine extends OutboundAdapter implements Adapter, RuleEngineIface {

    public DefaultRuleEngine() {
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        Kernel.getInstance().getLogger().print("\tadapter-class: " + this.getClass().getName());
    }

    @Override
    public Document processDocument(Document doc, List<String> roles) {
        if (doc == null) {
            return null;
        }
        if (roles == null) {
            return doc.setRights(Document.READONLY);
        }
        if (roles.contains("redactor") || roles.contains("redactor." + doc.getLanguage())) {
            return doc.setRights(Document.READWRITE);
        } else {
            return doc.setRights(Document.READONLY);
        }
    }

    @Override
    public ArrayList<Document> processDocumentsList(List<Document> documents, List<String> roles) {
        ArrayList<Document> result = new ArrayList<>();
        if(documents!=null){
        for (Document document : documents) {
            if (null != roles && (roles.contains("redactor") || roles.contains("redactor." + document.getLanguage()))) {
                result.add(document.setRights(Document.READWRITE));
            } else {
                result.add(document.setRights(Document.READONLY));
            }
        }}
        return result;
    }

    @Override
    public void checkDocument(Document doc, List<String> roles) throws CmsException {
        if (!(roles.contains("redactor") || roles.contains("redactor." + doc.getLanguage()))) {
            throw new CmsException(CmsException.HELPER_EXCEPTION, "not authorized");
        }
    }

}
