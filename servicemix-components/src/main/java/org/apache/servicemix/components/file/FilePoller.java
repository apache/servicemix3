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
import org.apache.servicemix.components.util.PollingComponentSupport;

import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArraySet;

import javax.jbi.JBIException;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.NormalizedMessage;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
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
    private static final Log log = LogFactory.getLog(FilePoller.class);

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
        super.init();
    }

    protected void pollFileOrDirectory(File fileOrDirectory) throws WorkException {
        if (log.isDebugEnabled()) {
            log.debug("Polling " + fileOrDirectory);
        }
        File[] files = null;
        if (fileOrDirectory.isDirectory()) {
            if (filter != null) {
                files = fileOrDirectory.listFiles(filter);
            }
            else {
                files = fileOrDirectory.listFiles();
            }
            for (int i = 0; i < files.length; i++) {
                File aFile = files[i];
                if (aFile.isDirectory()) {
                    if (isRecursive()) {
                        pollFileOrDirectory(aFile);
                    }
                }
                else {
                    pollFile(aFile);
                }
            }
        }
        else {
            pollFile(fileOrDirectory);
        }
    }

    protected void pollFile(final File aFile) throws WorkException {
        if (!workingSet.contains(aFile)) {
            workingSet.add(aFile);
            if (log.isDebugEnabled()) {
                log.debug("Scheduling file " + aFile + " for processing");
            }
            getWorkManager().scheduleWork(new Work() {
                public void run() {
                    processFileAndDelete(aFile);
                }

                public void release() {
                }
            });
        }
    }

    protected void processFileAndDelete(File aFile) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Processing file " + aFile);
            }
            if (aFile.exists()) {
                processFile(aFile);
                if (isDeleteFile()) {
                    if (!aFile.delete()) {
                        throw new IOException("Could not delete file " + aFile);
                    }
                }
            }
        }
        catch (Exception e) {
            log.error("Failed to process file: " + aFile + ". Reason: " + e, e);
        }
        finally {
            workingSet.remove(aFile);
        }
    }

    protected void processFile(File aFile) throws Exception {
        String name = aFile.getCanonicalPath();
        InputStream in = new BufferedInputStream(new FileInputStream(aFile));
        InOnly exchange = getExchangeFactory().createInOnlyExchange();
        NormalizedMessage message = exchange.createMessage();
        exchange.setInMessage(message);
        marshaler.readMessage(exchange, message, in, name);
        getDeliveryChannel().sendSync(exchange);
        in.close();
    }
}
