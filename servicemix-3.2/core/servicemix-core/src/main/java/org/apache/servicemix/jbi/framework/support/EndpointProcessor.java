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
package org.apache.servicemix.jbi.framework.support;

import org.apache.servicemix.jbi.framework.Registry;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;

/**
 * An endpoint processor is usually used to retrieve the interfaces
 * implemented by the given endpoint.
 * 
 * @author gnodet
 */
public interface EndpointProcessor {

    /**
     * Initialize the processor
     * 
     * @param registry 
     */
    void init(Registry registry);

    /**
     * Post process the endpoint.  
     * 
     * @param serviceEndpoint the endpoint
     */
    void process(InternalEndpoint serviceEndpoint);

}
