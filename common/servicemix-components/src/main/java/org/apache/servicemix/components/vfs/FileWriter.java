/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.components.vfs;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.servicemix.components.util.DefaultFileMarshaler;
import org.apache.servicemix.components.util.FileMarshaler;
import org.apache.servicemix.components.util.OutBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
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

    private static final Logger logger = LoggerFactory.getLogger(FileWriter.class);

    private FileObject directory;
    private FileObjectEditor editor = new FileObjectEditor();
    private FileMarshaler marshaler = new DefaultFileMarshaler();
    private String uniqueFileName = "ServiceMix";

    // Properties
    //-------------------------------------------------------------------------
    public FileObject getDirectory() {
        return directory;
    }

    public void setDirectory(FileObject directory) {
        this.directory = directory;
    }

    public String getPath() {
        return editor.getPath();
    }

    public void setPath(String path) {
        editor.setPath(path);
    }

    public FileSystemManager getFileSystemManager() {
        return editor.getFileSystemManager();
    }

    public void setFileSystemManager(FileSystemManager fileSystemManager) {
        editor.setFileSystemManager(fileSystemManager);
    }

    public FileMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(FileMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    public String getUniqueFileName() {
        return uniqueFileName;
    }

    /**
     * Sets the name used to make a unique name if no file name is available on the message.
     *
     * @param uniqueFileName the new value of the unique name to use for generating unique names
     */
    public void setUniqueFileName(String uniqueFileName) {
        this.uniqueFileName = uniqueFileName;
    }


    // Implementation methods
    //-------------------------------------------------------------------------
    protected void init() throws JBIException {
        if (directory == null) {
            directory = editor.getFileObject();
        }
        super.init();
    }

    protected void process(MessageExchange exchange, NormalizedMessage message) throws Exception {
        OutputStream out = null;
        try {
            String name = marshaler.getOutputName(exchange, message);
            if (name == null) {
                throw new MessagingException("No output name available. Cannot output message!");
            }
            directory.close(); // remove any cached informations
            FileObject newFile = directory.resolveFile(name);
            newFile.close(); // remove any cached informations
            FileContent content = newFile.getContent();
            content.close();
            if (content != null) {
                out = content.getOutputStream();
            }
            if (out == null) {
                throw new MessagingException("No output stream available for output name: " + name);
            }
            marshaler.writeMessage(exchange, message, out, name);
            done(exchange);
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException e) {
                    logger.error("Caught exception while closing stream on error", e);
                }
            }
        }
    }

}

