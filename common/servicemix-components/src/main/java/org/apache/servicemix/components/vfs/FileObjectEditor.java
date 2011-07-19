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

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import javax.jbi.JBIException;

/**
 * A bean editor to make it easier to create new file system objects using VFS
 *
 * @version $Revision$
 */
public class FileObjectEditor {

    private String path;
    private FileSystemManager fileSystemManager;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public FileSystemManager getFileSystemManager() {
        return fileSystemManager;
    }

    public void setFileSystemManager(FileSystemManager fileSystemManager) {
        this.fileSystemManager = fileSystemManager;
    }

    public FileObject getFileObject() throws JBIException {
        try {
            if (fileSystemManager == null) {
                fileSystemManager = VFS.getManager();
            }
            if (path == null) {
                throw new IllegalArgumentException("You must specify a path property");
            }
            FileObject answer = fileSystemManager.resolveFile(path);
            if (answer == null) {
                throw new JBIException("Could not resolve file: " + path);
            }
            try {
                answer.createFolder();
            }
            catch (FileSystemException e) {
                throw new JBIException("Failed to create folder: " + e, e);
            }
            return answer;
        }
        catch (FileSystemException e) {
            throw new JBIException("Failed to initialize file system manager: " + e, e);
        }
    }

}
