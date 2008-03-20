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


public interface EndpointListener extends ServiceMixListener {

    void internalEndpointRegistered(EndpointEvent event);
    
    void internalEndpointUnregistered(EndpointEvent event);
    
    void externalEndpointRegistered(EndpointEvent event);
    
    void externalEndpointUnregistered(EndpointEvent event);
    
    void linkedEndpointRegistered(EndpointEvent event);
    
    void linkedEndpointUnregistered(EndpointEvent event);
    
    void remoteEndpointRegistered(EndpointEvent event);
    
    void remoteEndpointUnregistered(EndpointEvent event);
    
}
