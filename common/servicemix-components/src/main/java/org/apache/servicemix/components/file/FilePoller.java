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
package org.apache.servicemix.components.file;

import org.apache.servicemix.components.util.DefaultFileMarshaler;
import org.apache.servicemix.components.util.FileMarshaler;
import org.apache.servicemix.components.util.PollingComponentSupport;
import org.apache.servicemix.jbi.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArraySet;

import javax.jbi.JBIException;
import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.NormalizedMessage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * A polling component which looks for a file or files in a directory
 * and sends the files into the JBI bus as messages, deleting the files
 * by default when they are processed.
 *
 * @version $Revision$
 */
public class FilePoller extends PollingComponentSupport {

    private static final Logger logger = LoggerFactory.getLogger(FilePoller.class);

    private File archive;
    private File file;
    private FileFilter filter;
    private boolean deleteFile = true;
    private boolean recursive = true;
    private boolean autoCreateDirectory = true;
    private FileMarshaler marshaler = new DefaultFileMarshaler();
    private Set workingSet = new CopyOnWriteArraySet();

    public void poll() throws Exception {
        pollFileOrDirectory(file);
    }

    // Properties
    //-------------------------------------------------------------------------
    public File getFile() {
        return file;
    }

    /**
     * Sets the file to poll, which can be a directory or a file.
     *
     * @param file
     */
    public void setFile(File file) {
        this.file = file;
    }

    public FileFilter getFilter() {
        return filter;
    }

    /**
     * Sets the optional filter to choose which files to process
     */
    public void setFilter(FileFilter filter) {
        this.filter = filter;
    }

    /**
     * Returns whether or not we should delete the file when its processed
     */
    public boolean isDeleteFile() {
        return deleteFile;
    }

    public void setDeleteFile(boolean deleteFile) {
        this.deleteFile = deleteFile;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isAutoCreateDirectory() {
        return autoCreateDirectory;
    }

    public void setAutoCreateDirectory(boolean autoCreateDirectory) {
        this.autoCreateDirectory = autoCreateDirectory;
    }

    public FileMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(FileMarshaler marshaler) {
        this.marshaler = marshaler;
    }
    
    public File getArchive() {
        return archive;
    }
    
    /**
     * Configure a directory to archive files before deleting them.
     * 
     * @param archive the archive directory
     */
    public void setArchive(File archive) {
        this.archive = archive;
    }

    /**
     * The set of FTPFiles that this component is currently working on
     *
     * @return
     */
    public Set getWorkingSet() {
        return workingSet;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void init() throws JBIException {
        if (file == null) {
            throw new IllegalArgumentException("You must specify a file property");
        }
        if (isAutoCreateDirectory() && !file.exists()) {
            file.mkdirs();
        }
        if (archive != null) {
            if (!deleteFile) {
                throw new DeploymentException("Archive shouldn't be specified unless deleteFile='true'");
            }
            if (isAutoCreateDirectory() && !archive.exists()) {
                archive.mkdirs();
            }
            if (!archive.isDirectory()) {
                throw new DeploymentException("Archive should refer to a directory");
            }
        }
        super.init();
    }

    protected void pollFileOrDirectory(File fileOrDirectory) {
    	pollFileOrDirectory(fileOrDirectory, true);
    }
    
    protected void pollFileOrDirectory(File fileOrDirectory, boolean processDir) {
        if (!fileOrDirectory.isDirectory()) {
            pollFile(fileOrDirectory); // process the file
        } else if (processDir) {
            logger.debug("Polling directory " + fileOrDirectory);
            File[] files = fileOrDirectory.listFiles(getFilter());
            for (int i = 0; i < files.length; i++) {
                pollFileOrDirectory(files[i], isRecursive()); // self-recursion
            }
        } else {
            logger.debug("Skipping directory " + fileOrDirectory);
        } 
    }

    protected void pollFile(final File aFile) {
        if (workingSet.add(aFile)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Scheduling file " + aFile + " for processing");
            }
            getExecutor().execute(new Runnable() {
                public void run() {
                    try {
                        processFileAndDelete(aFile);
                    } finally {
                        workingSet.remove(aFile);
                    }
                }
            });
        }
    }

    protected void processFileAndDelete(File aFile) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Processing file " + aFile);
            }
            if (aFile.exists()) {
                processFile(aFile);
                if (isDeleteFile()) {
                    if (archive != null) {
                        FileUtil.moveFile(aFile, archive);
                    } else {
                        if (!aFile.delete()) {
                            throw new IOException("Could not delete file " + aFile);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error("Failed to process file: " + aFile + ". Reason: " + e, e);
        }
    }

    protected void processFile(File aFile) throws Exception {
        InputStream in = null;
        try {
            String name = aFile.getCanonicalPath();
            in = new BufferedInputStream(new FileInputStream(aFile));
            InOnly exchange = getExchangeFactory().createInOnlyExchange();
            NormalizedMessage message = exchange.createMessage();
            exchange.setInMessage(message);
            marshaler.readMessage(exchange, message, in, name);
            getDeliveryChannel().sendSync(exchange);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

}
