/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gskorupa.cricket.out;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author greg
 */
public interface HtmlReaderAdapterIface {
    
    public String readFile(String path) throws FileNotFoundException, IOException;
    
}
