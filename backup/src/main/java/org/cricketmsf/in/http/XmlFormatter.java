/*
 * Copyright 2016 Grzegorz Skorupa .
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

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 *
 * @author Grzegorz Skorupa 
 */
public class XmlFormatter {

    private static XmlFormatter instance = null;

    public static XmlFormatter getInstance() {
        if (instance != null) {
            return instance;
        } else {
            instance = new XmlFormatter();
            return instance;
        }
    }

    /**
     * 
     * @param prettyPrint TODO doc
     * @param r response object (Result or Result.getData())
     * @return TODO doc
     */
    public String format(boolean prettyPrint, Object r) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(r.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, prettyPrint);
            StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal(r, sw);
            return sw.toString();
        } catch (JAXBException e) {
            return "<error><code>"+e.getErrorCode()+"</code><text>"+e.toString()+"</text></error>";
        }
    }

    /*
    public String format(boolean prettyPrint, Result r) {
        XStream xstream = new XStream(new StaxDriver());
        xstream.alias("result", r.getClass());
        String result = "";
        if (prettyPrint) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            xstream.marshal(r, new PrettyPrintWriter(new OutputStreamWriter(baos)));
            result = baos.toString();
        } else {
            result = xstream.toXML(r);
        }
        return result;
    }
     */
}
