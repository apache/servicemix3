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
package org.apache.servicemix.jbi.util;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supports a simple versioning scheme using the file system
 * 
 * @version $Revision$
 */
public final class FileVersionUtil {
    
    private static final transient Logger LOGGER = LoggerFactory.getLogger(FileVersionUtil.class);

    private static final String VERSION_PREFIX = "version_";

    private static final String[] RESERVED = {VERSION_PREFIX };
    
    private FileVersionUtil() {
    }

    /**
     * Get the latest version number for a directory
     * 
     * @param rootDirectory
     * @return the version number
     */
    public static int getLatestVersionNumber(File rootDirectory) {
        int result = -1;
        if (isVersioned(rootDirectory)) {
            File[] files = rootDirectory.listFiles();
            for (int i = 0; i < files.length; i++) {
                int version = getVersionNumber(files[i].getName());
                if (version > result) {
                    result = version;
                }
            }
        }
        return result;
    }

    /**
     * Get the latest versioned directory
     * 
     * @param rootDirectory
     * @return the directory
     * @throws IOException
     */
    public static File getLatestVersionDirectory(File rootDirectory) {
        File result = null;
        int highestVersion = -1;
        if (rootDirectory != null && isVersioned(rootDirectory)) {
            File[] files = rootDirectory.listFiles();
            for (int i = 0; i < files.length; i++) {
                int version = getVersionNumber(files[i].getName());
                if (version > highestVersion) {
                    highestVersion = version;
                    result = files[i];
                }
            }
        }
        return result;
    }

    /**
     * Create a new version directory
     * 
     * @param rootDirectory
     * @return the created version directory
     * @throws IOException
     */
    public static File createNewVersionDirectory(File rootDirectory) throws IOException {
        File result = getNewVersionDirectory(rootDirectory);
        if (!FileUtil.buildDirectory(result)) {
            throw new IOException("Failed to build version directory: " + result);
        }
        return result;
    }

    /**
     * get's the new version file - without creating the directory
     * 
     * @param rootDirectory
     * @return the version directory
     * @throws IOException
     */
    public static File getNewVersionDirectory(File rootDirectory) throws IOException {
        File result = null;
        if (FileUtil.buildDirectory(rootDirectory)) {
            String versionDirectoryName = VERSION_PREFIX;
            if (isVersioned(rootDirectory)) {
                int versionNumber = getLatestVersionNumber(rootDirectory);
                versionNumber = versionNumber > 0 ? versionNumber + 1 : 1;
                versionDirectoryName += versionNumber;
            } else {
                versionDirectoryName += 1;
            }
            result = FileUtil.getDirectoryPath(rootDirectory, versionDirectoryName);
        } else {
            throw new IOException("Cannot build parent directory: " + rootDirectory);
        }
        return result;
    }

    /**
     * Used to move non-version files/directories to versioned
     * 
     * @param rootDirectory
     * @throws IOException
     */
    public static void initializeVersionDirectory(File rootDirectory) throws IOException {
        if (!isVersioned(rootDirectory)) {
            File newRoot = createNewVersionDirectory(rootDirectory);
            File[] files = rootDirectory.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (!isReserved(files[i].getName())) {
                    LOGGER.info(rootDirectory.getPath() + ": moving non-versioned file " + files[i].getName() + " to " + newRoot.getName());
                    File moveTo = FileUtil.getDirectoryPath(newRoot, files[i].getName());
                    FileUtil.moveFile(files[i], moveTo);
                }
            }
        }
    }

    private static boolean isVersioned(File rootDirectory) {
        boolean result = false;
        if (rootDirectory.exists() && rootDirectory.isDirectory()) {
            File[] files = rootDirectory.listFiles();
            result = files == null || files.length == 0;
            if (!result) {
                for (int i = 0; i < files.length; i++) {
                    if (isReserved(files[i].getName())) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private static boolean isReserved(String name) {
        boolean result = false;
        if (name != null) {
            for (int i = 0; i < RESERVED.length; i++) {
                if (name.startsWith(RESERVED[i])) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private static int getVersionNumber(String name) {
        int result = -1;
        if (name != null && name.startsWith(VERSION_PREFIX)) {
            String number = name.substring(VERSION_PREFIX.length());
            result = Integer.parseInt(number);
        }
        return result;
    }

}