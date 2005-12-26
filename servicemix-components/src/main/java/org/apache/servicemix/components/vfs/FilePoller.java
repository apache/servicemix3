/**
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.apache.servicemix.components.vfs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.servicemix.components.util.DefaultFileMarshaler;
import org.apache.servicemix.components.util.FileMarshaler;
import org.apache.servicemix.components.util.PollingComponentSupport;

import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArraySet;

import javax.jbi.JBIException;
import javax.jbi.messaging.RobustInOnly;
import javax.jbi.messaging.NormalizedMessage;
import javax.resource.spi.work.Work;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * A polling component which looks for a file in a file system using the
 * <a href="http://jakarta.apache.org/commons/vfs.html">Jakarta Commons VFS</a> library
 * for handling various file systems like files, Samba, WebDAV, FTP, SFTP and temporary files.
 *
 * @version $Revision$
 */
public class FilePoller extends PollingComponentSupport {
    private static final Log log = LogFactory.getLog(FilePoller.class);

    private FileMarshaler marshaler = new DefaultFileMarshaler();
    private FileObjectEditor editor = new FileObjectEditor();
    private FileObject directory;
    private FileSelector selector;
    private Set workingSet = new CopyOnWriteArraySet();
    private boolean deleteFile = true;

    public void poll() throws Exception {
        FileObject[] files = null;
        // SM-192: Force close the file, so that the cached informations are cleared
        directory.close();
        if (selector != null) {
            files = directory.findFiles(selector);
        }
        else {
            files = directory.getChildren();
        }
        for (int i = 0; i < files.length; i++) {
            final FileObject file = files[i];
            if (!workingSet.contains(file)) {
                workingSet.add(file);
                getWorkManager().scheduleWork(new Work() {
                    public void run() {
                        processFileAndDelete(file);
                    }

                    public void release() {
                    }
                });
            }
        }
    }


    // Properties
    //-------------------------------------------------------------------------
    public FileObject getDirectory() {
        return directory;
    }

    public void setDirectory(FileObject directory) {
        this.directory = directory;
    }

    public FileSelector getSelector() {
        return selector;
    }

    public void setSelector(FileSelector selector) {
        this.selector = selector;
    }

    public String getPath() {
        return editor.getPath();
    }

    public void setPath(String path) {
        editor.setPath(path);
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
        if (directory == null) {
            directory = editor.getFileObject();
        }
        super.init();
    }

    protected void processFileAndDelete(FileObject file) {
        try {
            processFile(file);
            if (isDeleteFile()) {
                if (!file.delete()) {
                    throw new IOException("Could not delete file " + file);
                }
            }
        }
        catch (Exception e) {
            log.error("Failed to process file: " + file + ". Reason: " + e, e);
        }
        finally {
            workingSet.remove(file);
        }
    }

    protected void processFile(FileObject file) throws Exception {
        // SM-192: Force close the file, so that the cached informations are cleared
        file.close();
        String name = file.getName().getURI();
        FileContent content = file.getContent();
        content.close();
        InputStream in = content.getInputStream();
        if (in == null) {
            throw new JBIException("No input available for file!");
        }
        RobustInOnly exchange = getExchangeFactory().createRobustInOnlyExchange();
        NormalizedMessage message = exchange.createMessage();
        exchange.setInMessage(message);
        marshaler.readMessage(exchange, message, in, name);
        getDeliveryChannel().sendSync(exchange);
        in.close();
        content.close();
        if (exchange.getError() != null) {
            throw exchange.getError();
        }
    }
}
