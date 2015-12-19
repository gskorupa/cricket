/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gskorupa.cricket.in;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
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

}
