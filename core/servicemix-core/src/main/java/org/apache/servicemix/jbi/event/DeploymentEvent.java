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
package org.apache.servicemix.jbi.event;

import java.io.File;
import java.util.EventObject;

public class DeploymentEvent extends EventObject {

    public static final int FILE_ADDED = 0;
    public static final int FILE_CHANGED = 1;
    public static final int FILE_REMOVED = 2;
    
    private static final long serialVersionUID = 1330139373403204421L;

    private final File file;
    private final int type;

    public DeploymentEvent(File file, int type) {
        super(file);
        this.file = file;
        this.type = type;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

}
