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

import java.util.EventObject;

import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;

/**
 * Event sent for components lifecycle.
 * 
 * @author gnodet
 */
public class ComponentEvent extends EventObject {

    public static final int COMPONENT_INSTALLED = 0;
    public static final int COMPONENT_INITIALIZED = 1;
    public static final int COMPONENT_STARTED = 2;
    public static final int COMPONENT_STOPPED = 3;
    public static final int COMPONENT_SHUTDOWN = 4;
    public static final int COMPONENT_UNINSTALLED = 5;
    
    private static final long serialVersionUID = -4075242868959881673L;
    
    private ComponentMBeanImpl component;
    private int type;
    
    public ComponentEvent(ComponentMBeanImpl component, int type) {
        super(component);
        this.component = component;
        this.type = type;
    }
    
    public ComponentMBeanImpl getComponent() {
        return component;
    }
    
    public int getEventType() {
        return type;
    }

}
