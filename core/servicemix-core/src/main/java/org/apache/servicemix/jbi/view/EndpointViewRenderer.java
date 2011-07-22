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
package org.apache.servicemix.jbi.view;

import javax.jbi.servicedesc.ServiceEndpoint;

import org.apache.servicemix.jbi.event.EndpointEvent;
import org.apache.servicemix.jbi.event.EndpointListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for renderings of endpoints which can re-render whenever something
 * changes or mark things as dirty so that they can be re-rendered on demand or in a time based way
 * 
 * @version $Revision$
 */
public abstract class EndpointViewRenderer implements EndpointListener {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(EndpointViewRenderer.class);
    
    private boolean dirty;
    private boolean rerenderOnChange = true;

    public void render() throws Exception {
        doRender();
        dirty = false;
    }

    public void internalEndpointRegistered(EndpointEvent event) {
        viewIsDirty(event.getEndpoint());
    }

    public void internalEndpointUnregistered(EndpointEvent event) {
        viewIsDirty(event.getEndpoint());
    }

    public void externalEndpointRegistered(EndpointEvent event) {
        viewIsDirty(event.getEndpoint());
    }

    public void externalEndpointUnregistered(EndpointEvent event) {
        viewIsDirty(event.getEndpoint());
    }

    public void linkedEndpointRegistered(EndpointEvent event) {
        viewIsDirty(event.getEndpoint());
    }

    public void linkedEndpointUnregistered(EndpointEvent event) {
        viewIsDirty(event.getEndpoint());
    }

    public void remoteEndpointRegistered(EndpointEvent event) {
        viewIsDirty(event.getEndpoint());
    }

    public void remoteEndpointUnregistered(EndpointEvent event) {
        viewIsDirty(event.getEndpoint());
    }

    // Properties
    // -------------------------------------------------------------------------
    public boolean isRerenderOnChange() {
        return rerenderOnChange;
    }

    public void setRerenderOnChange(boolean rerenderOnChange) {
        this.rerenderOnChange = rerenderOnChange;
    }

    public boolean isDirty() {
        return dirty;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected abstract void doRender() throws Exception;

    protected void viewIsDirty(ServiceEndpoint endpoint) {
        dirty = true;
        if (rerenderOnChange) {
            try {
                render();
            } catch (Exception e) {
                LOGGER.warn("Failed to render view: {}", e.getMessage(), e);
            }
        }
    }

}
