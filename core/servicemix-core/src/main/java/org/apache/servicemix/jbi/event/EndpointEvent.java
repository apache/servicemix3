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

import javax.jbi.servicedesc.ServiceEndpoint;

/**
 * Event sent for endpoint lifeycle.
 * 
 * @author gnodet
 */
public class EndpointEvent extends EventObject {

    public static final int INTERNAL_ENDPOINT_REGISTERED = 0;
    public static final int INTERNAL_ENDPOINT_UNREGISTERED = 1;
    public static final int EXTERNAL_ENDPOINT_REGISTERED = 2;
    public static final int EXTERNAL_ENDPOINT_UNREGISTERED = 3;
    public static final int LINKED_ENDPOINT_REGISTERED = 4;
    public static final int LINKED_ENDPOINT_UNREGISTERED = 5;
    public static final int REMOTE_ENDPOINT_REGISTERED = 6;
    public static final int REMOTE_ENDPOINT_UNREGISTERED = 7;
    
    private static final long serialVersionUID = -4480619483039133388L;
    
    private ServiceEndpoint endpoint;
    private int type;
    
    public EndpointEvent(ServiceEndpoint endpoint, int type) {
        super(endpoint);
        this.endpoint = endpoint;
        this.type = type;
    }
    
    public ServiceEndpoint getEndpoint() {
        return endpoint;
    }

    public int getEventType() {
        return type;
    }

}
