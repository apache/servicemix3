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
package org.apache.servicemix.jbi.event;

import java.util.EventObject;

import org.apache.servicemix.jbi.framework.LocalComponentConnector;

public class ComponentEvent extends EventObject {

    private static final long serialVersionUID = -4075242868959881673L;
    
    public static final int COMPONENT_INSTALLED = 0;
    public static final int COMPONENT_STARTED = 1;
    public static final int COMPONENT_STOPPED = 2;
    public static final int COMPONENT_SHUTDOWN = 3;
    public static final int COMPONENT_UNINSTALLED = 4;
    
    private LocalComponentConnector lcc;
    private int type;
    
    public ComponentEvent(LocalComponentConnector lcc, int type) {
        super(lcc);
        this.lcc = lcc;
        this.type = type;
    }
    
    public LocalComponentConnector getComponent() {
        return lcc;
    }
    
    public int getEventType() {
        return type;
    }

}
