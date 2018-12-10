/*
 * Copyright 2005-2006 The Apache Software Foundation.
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
package org.apache.servicemix.components.file;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.components.util.DefaultFileMarshaler;
import org.apache.servicemix.components.util.FileMarshaler;
import org.apache.servicemix.components.util.OutBinding;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A component which receives a message and writes the content to a file using the
 * <a href="http://jakarta.apache.org/commons/vfs.html">Jakarta Commons VFS</a> library
 * for handling various file systems like files, Samba, WebDAV, FTP, SFTP and temporary files.
 *
 * @version $Revision$
 */
public class FileWriter extends OutBinding {
    private static final Log log = LogFactory.getLog(FileWriter.class);

    private File directory;
    private FileMarshaler marshaler = new DefaultFileMarshaler();
    private String tempFilePrefix = "servicemix-";
    private String tempFileSuffix = ".xml";
    private boolean autoCreateDirectory = true;

    // Properties
    //-------------------------------------------------------------------------
    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public FileMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(FileMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    public String getTempFilePrefix() {
        return tempFilePrefix;
    }

    public void setTempFilePrefix(String tempFilePrefix) {
        this.tempFilePrefix = tempFilePrefix;
    }

    public String getTempFileSuffix() {
        return tempFileSuffix;
    }

    public void setTempFileSuffix(String tempFileSuffix) {
        this.tempFileSuffix = tempFileSuffix;
    }

    public boolean isAutoCreateDirectory() {
        return autoCreateDirectory;
    }

    public void setAutoCreateDirectory(boolean autoCreateDirectory) {
        this.autoCreateDirectory = autoCreateDirectory;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void init() throws JBIException {
        if (directory == null) {
            throw new IllegalArgumentException("You must specify the directory property");
        }
        if (isAutoCreateDirectory()) {
            directory.mkdirs();
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("The directory property must be a directory but was: " + directory);
        }

        super.init();
    }

    protected void process(MessageExchange exchange, NormalizedMessage message) throws Exception {
        OutputStream out = null;
        try {
            String name = marshaler.getOutputName(exchange, message);
            File newFile = null;
            if (name == null) {
                newFile = File.createTempFile(tempFilePrefix, tempFileSuffix, directory);
            }
            else {
                newFile = new File(directory, name);
            }
            out = new BufferedOutputStream(new FileOutputStream(newFile));
            marshaler.writeMessage(exchange, message, out, name);
            done(exchange);
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException e) {
                    log.error("Caught exception while closing stream on error: " + e, e);
                }
            }
        }
    }
}

