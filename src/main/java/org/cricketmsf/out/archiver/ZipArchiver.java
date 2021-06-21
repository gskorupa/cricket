/*
 * Copyright 2021 Grzegorz Skorupa.
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
package org.cricketmsf.out.archiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.LoggerFactory;

/**
 *
 * @author greg
 */
public class ZipArchiver {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ZipArchiver.class);

    Path filePath;
    private ZipOutputStream out;
    private File file;

    public ZipArchiver(String prefix, String suffix) throws IOException {
        filePath = Files.createTempFile(prefix, suffix);
        file = filePath.toFile();
        out = new ZipOutputStream(new FileOutputStream(file));
    }

    public void addFileContent(String fileName, String fileContent) throws IOException {
        ZipEntry e = new ZipEntry(fileName);
        out.putNextEntry(e);
        byte[] data = fileContent.getBytes();
        out.write(data, 0, data.length);
        out.closeEntry();
    }

    public File getFile() throws IOException {
        out.close();
        return file;
    }

}
