package org.cricketmsf.in.file;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author greg
 */
public class NotDirectoryFilter implements FileFilter{

    @Override
    public boolean accept(File file) {
        return file.isFile();
    }
    
}
